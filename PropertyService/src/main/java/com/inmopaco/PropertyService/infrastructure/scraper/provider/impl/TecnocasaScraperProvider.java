    package com.inmopaco.PropertyService.infrastructure.scraper.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import com.inmopaco.PropertyService.domain.enums.ContractType;
import com.inmopaco.PropertyService.domain.enums.PropertyType;
import com.inmopaco.PropertyService.infrastructure.persistence.service.PropertyPersistenceService;
import com.inmopaco.PropertyService.infrastructure.scraper.client.ScraperClient;
import com.inmopaco.PropertyService.infrastructure.scraper.config.TecnocasaConfig;
import com.inmopaco.PropertyService.infrastructure.scraper.config.TecnocasaConfig.ProvinceItem;
import com.inmopaco.PropertyService.infrastructure.scraper.provider.PropertyScraperProvider;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Log4j2
public class TecnocasaScraperProvider implements PropertyScraperProvider {

    private static final String BASE_URL = "https://www.tecnocasa.es";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();
    private static final int BATCH_SIZE = 5;
    private static final int PARALLEL_THREADS = 5;
    private static final String TECNOCASA_PREFIX = "TECNOCASA-";
    private static final Pattern PAGE_PATTERN = Pattern.compile("pag-(\\d+)");
    private static final Pattern ACTIVE_PAGE_PATTERN = Pattern.compile("class=\"active\"[^>]*>\\s*<a[^>]*>(\\d+)</a>");
    private static final Pattern ACTIVE_PAGE_FALLBACK = Pattern.compile("class=\"[^\"]*active[^\"]*\"[^>]*>\\s*<a[^>]*href[^>]*>(\\d+)</a>");

    private final ScraperClient scraperClient;
    private final PropertyPersistenceService persistenceService;
    private final TecnocasaConfig tecnocasaConfig;
    private final ExecutorService executor;

    @Value("${property.scraper.provider.tecnocasa.enabled:true}")
    private boolean enabled;

    @Value("${property.scraper.delay.min:100}")
    private int baseDelayMs;

    @Value("${property.scraper.delay.max:500}")
    private int maxRandomDelayMs;

    public TecnocasaScraperProvider(ScraperClient scraperClient,
                                     PropertyPersistenceService persistenceService,
                                     TecnocasaConfig tecnocasaConfig) {
        this.scraperClient = scraperClient;
        this.persistenceService = persistenceService;
        this.tecnocasaConfig = tecnocasaConfig;
        this.executor = Executors.newFixedThreadPool(PARALLEL_THREADS);
    }

    private record PageData(List<String> propertyUrls, boolean hasNextPage) {}
    private record SaveResult(int saved, int updated) {}

    @Override
    public String getProviderName() {
        return "tecnocasa";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<PropertyDTO> scrapeByCity(String city) {
        if (!enabled) return List.of();

        log.info("Starting scrape for {} ({} parallel executors, {}-{}ms delay)",
            city, PARALLEL_THREADS, baseDelayMs, baseDelayMs + maxRandomDelayMs);

        try {
            final Document firstDoc = scraperClient.fetchUrl(buildSearchUrl(city));
            final int totalInmuebles = extractTotalInmueblesFromDoc(firstDoc);

            log.info("{} offers found for Tecnocasa - {}", totalInmuebles, city);
            if (totalInmuebles == 0) {
                log.info("No offers found for {}", city);
                return List.of();
            }

            return scrapeAllPages(firstDoc, city, totalInmuebles);
        } catch (Exception e) {
            log.error("Error during Tecnocasa scraping for city {}: {}", city, e.getMessage(), e);
            return List.of();
        }
    }

    private List<PropertyDTO> scrapeAllPages(Document firstDoc, String city, int totalInmuebles) throws Exception {
        final int maxPages = (int) Math.ceil((double) totalInmuebles / 15.0);
        log.info("Calculated max pages: {}", maxPages);

        final List<PropertyDTO> allProperties = new ArrayList<>();
        final String baseUrl = buildSearchUrl(city);
        int currentPage = 1;

        while (currentPage <= maxPages) {
            final String pageUrl = currentPage == 1 ? baseUrl : baseUrl + "?page=" + currentPage;
            log.info("Scraping page {} for {}: {}", currentPage, city, pageUrl);

            delayBetweenRequests();


            final Document pageDoc = currentPage == 1 ? firstDoc : scraperClient.fetchUrl(pageUrl);
            final PageData pageData = extractPageDataFromDoc(pageDoc, currentPage);

            if (pageData.propertyUrls().isEmpty()) {
                log.info("No more properties found on page {} for {}", currentPage, city);
                break;
            }

            log.info("Found {} property URLs on page {}", pageData.propertyUrls().size(), currentPage);
            allProperties.addAll(fetchPropertiesInParallel(pageData.propertyUrls(), city));
            log.info("Page {} completed for {} - Total collected: {}", currentPage, city, allProperties.size());

            if (!pageData.hasNextPage()) {
                log.info("No next page button found, stopping pagination for {}", city);
                break;
            }
            currentPage++;
        }

        log.info("Scraping completed for {} - Total properties: {}", city, allProperties.size());
        return allProperties;
    }

    @Override
    public List<PropertyDTO> scrapeByProvince(String province) {
        return tecnocasaConfig.findProvinceByName(province)
            .map(p -> scrapeByCity(p.getName()))
            .orElseGet(() -> scrapeByCity(province));
    }

    @Override
    public List<PropertyDTO> scrapeAll() {
        final List<String> provinces = tecnocasaConfig.getAllProvinceNames();
        log.info("Starting Tecnocasa scraping for {} provinces with {} parallel executors, {}-{}ms delay",
            provinces.size(), PARALLEL_THREADS, baseDelayMs, baseDelayMs + maxRandomDelayMs);

        int totalSaved = 0;

        for (String province : provinces) {
            try {
                final List<PropertyDTO> properties = scrapeByCity(province);
                final SaveResult result = savePropertiesBatch(properties);
                totalSaved += result.saved();
                log.info("Completed {}, Found: {}, Saved: {}, Updated: {}",
                    province, properties.size(), result.saved(), result.updated());
            } catch (Exception e) {
                log.error("Failed to scrape province {}: {}", province, e.getMessage());
            }
        }

        log.info("Completed Tecnocasa scraping. Total properties saved: {}", totalSaved);
        return List.of();
    }

    private List<PropertyDTO> fetchPropertiesInParallel(List<String> propertyUrls, String city) {
        final List<PropertyDTO> results = new ArrayList<>();

        for (int i = 0; i < propertyUrls.size(); i += BATCH_SIZE) {
            final int end = Math.min(i + BATCH_SIZE, propertyUrls.size());
            final List<String> batch = propertyUrls.subList(i, end);

            final List<CompletableFuture<Optional<PropertyDTO>>> futures = batch.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> fetchPropertyFromDetailPage(url, city), executor))
                .toList();

            for (CompletableFuture<Optional<PropertyDTO>> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS).ifPresent(results::add);
                } catch (Exception e) {
                    log.warn("Failed to fetch property in parallel: {}", e.getMessage());
                }
            }

            if (end < propertyUrls.size()) {
                delayBetweenRequests();
            }
        }

        return results;
    }

    private Optional<PropertyDTO> fetchPropertyFromDetailPage(String url, String province) {
        try {
            final Document doc = scraperClient.fetchUrl(url);
            final String propertyId = extractPropertyIdFromUrl(url);
            final String fullUrl = normalizeUrl(url);

            final PropertyDTO.PropertyDTOBuilder builder = PropertyDTO.builder()
                .propertyId(TECNOCASA_PREFIX + propertyId)
                .url(fullUrl)
                .country("ES")
                .province(province)
                .contractType(ContractType.SALE);

            extractDataFromEmbeddedJson(doc, builder, url);
            extractDataFromDetailPage(doc, builder, url);

            return Optional.of(builder.build());
        } catch (Exception e) {
            log.warn("Failed to fetch property detail from {}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private String normalizeUrl(String url) {
        if (url.startsWith("//")) return "https:" + url;
        return url;
    }

    private String extractPropertyIdFromUrl(String url) {
        final String lastSegment = url.substring(url.lastIndexOf('/') + 1);
        return lastSegment.replace(".html", "");
    }

    private SaveResult savePropertiesBatch(List<PropertyDTO> properties) {
        int saved = 0;
        int updated = 0;

        for (PropertyDTO property : properties) {
            try {
                final boolean existed = persistenceService.existsByUrl(property.getUrl());
                persistenceService.saveProperty(property);
                if (existed) updated++;
                else saved++;
            } catch (Exception e) {
                log.error("Failed to save property {}: {}", property.getPropertyId(), e.getMessage());
            }
        }

        return new SaveResult(saved, updated);
    }

    private int extractTotalInmueblesFromDoc(Document doc) {
        final String text = doc.select("template[slot=estates-list-heading] strong").text();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            log.warn("Could not parse total inmuebles: {}", text);
            return 0;
        }
    }

    private PageData extractPageDataFromDoc(Document doc) {
        return extractPageDataFromDoc(doc, 1);
    }

    private PageData extractPageDataFromDoc(Document doc, int currentPage) {
        final List<String> urls = new ArrayList<>();

        try {
            urls.addAll(extractUrlsFromEstateCards(doc));
            if (urls.isEmpty()) {
                urls.addAll(extractUrlsFromLinks(doc));
            }
        } catch (Exception e) {
            log.error("Failed to extract page data: {}", e.getMessage());
            return new PageData(List.of(), false);
        }

        final boolean hasNextPage = hasNextPage(doc, currentPage);
        return new PageData(urls, hasNextPage);
    }

    private List<String> extractUrlsFromEstateCards(Document doc) {
        final List<String> urls = new ArrayList<>();
        doc.select("estate-card").forEach(card -> {
            final String estateJson = card.attr(":estate");
            if (estateJson.isEmpty()) return;
            try {
                final JsonNode estate = OBJECT_MAPPER.readTree(unescapeJson(estateJson));
                if (estate.has("detail_url") && estate.has("id")) {
                    final String detailUrl = estate.get("detail_url").asText();
                    if (!detailUrl.isEmpty()) {
                        urls.add(detailUrl);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse estate card JSON: {}", e.getMessage());
            }
        });
        return urls;
    }

    private List<String> extractUrlsFromLinks(Document doc) {
        return doc.select(
            "a[href*='/venta/piso/'], a[href*='/venta/casa/'], a[href*='/venta/atico/'], a[href*='/venta/chalet/']"
        ).stream()
            .map(a -> a.attr("href"))
            .filter(href -> href.contains(".html") && !href.contains("buscar") && href.matches(".*/\\d+\\.html$"))
            .distinct()
            .map(href -> href.startsWith("/") ? BASE_URL + href : href)
            .collect(Collectors.toList());
    }

    private boolean hasNextPage(Document doc, int currentPage) {
        try {
            final String paginationContent = doc.select("template[slot=pagination]").html();
            if (paginationContent.trim().isEmpty()) return false;

            final int lastPageNumber = extractLastPageNumber(paginationContent);
            final int activePage = extractActivePage(paginationContent, currentPage);
            return lastPageNumber > 0 && activePage < lastPageNumber;
        } catch (Exception e) {
            log.error("Failed to extract pagination: {}", e.getMessage());
            return false;
        }
    }

    private int extractLastPageNumber(String paginationHtml) {
        final Matcher matcher = PAGE_PATTERN.matcher(paginationHtml);
        int maxPage = 0;
        while (matcher.find()) {
            try {
                maxPage = Math.max(maxPage, Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ignored) {}
        }
        return maxPage;
    }

    private int extractActivePage(String paginationHtml, int currentPage) {
        int page = extractFirstMatch(paginationHtml, ACTIVE_PAGE_PATTERN);
        if (page > 0) return page;
        page = extractFirstMatch(paginationHtml, ACTIVE_PAGE_FALLBACK);
        return page > 0 ? page : currentPage;
    }

    private int extractFirstMatch(String html, Pattern pattern) {
        final Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private String unescapeJson(String json) {
        if (json == null || json.isEmpty()) return json;

        return json
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace("&apos;", "'");
    }

    private void extractDataFromEmbeddedJson(Document doc, PropertyDTO.PropertyDTOBuilder builder, String url) {
        try {
            final String bodyHtml = doc.body().html();
            extractFromEstateAttribute(doc, bodyHtml, builder);
            extractFromScriptTags(doc, builder);
        } catch (Exception e) {
            log.warn("Error extracting data from embedded JSON {}: {}", url, e.getMessage());
        }
    }

    private void extractFromEstateAttribute(Document doc, String bodyHtml, PropertyDTO.PropertyDTOBuilder builder) {
        String rawJson = null;

        final int estateStart = findEstateAttributeStart(bodyHtml);
        if (estateStart > 0) {
            final int jsonStart = bodyHtml.indexOf("{", estateStart);
            if (jsonStart > 0) {
                rawJson = extractBalancedJson(bodyHtml, jsonStart);
            }
        }

        if (rawJson == null || rawJson.isEmpty()) {
            Element estateEl = doc.selectFirst("estate-show-v2");
            if (estateEl != null) {
                rawJson = estateEl.attr(":estate");
            }
        }

        if (rawJson == null || rawJson.isEmpty()) return;

        final String unescaped = unescapeJson(rawJson);

        try {
            final JsonNode estate = OBJECT_MAPPER.readTree(unescaped);
            extractEstateData(estate, builder);
            log.debug("Successfully extracted estate data from embedded JSON");
        } catch (Exception e) {
            log.debug("Failed to parse estate JSON: {}", e.getMessage());
        }
    }

    private int findEstateAttributeStart(String bodyHtml) {
        final int start = bodyHtml.indexOf(":estate=\"{");
        if (start >= 0) return start;
        return bodyHtml.indexOf(":estate=&quot;{");
    }

    private String extractBalancedJson(String html, int jsonStart) {
        int braceCount = 0;
        boolean inString = false;
        boolean escaped = false;
        int jsonEnd = jsonStart;

        for (int i = jsonStart; i < html.length(); i++) {
            final char c = html.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        jsonEnd = i + 1;
                        break;
                    }
                }
            }
        }

        return html.substring(jsonStart, jsonEnd);
    }

    private void extractFromScriptTags(Document doc, PropertyDTO.PropertyDTOBuilder builder) {
        try {
            final PropertyDTO preview = builder.build();
            if (preview.getPrice() != null) return;
        } catch (Exception e) {
            return;
        }

        log.debug("Price still null, trying script tags");
        doc.select("script").forEach(script -> {
            final String content = script.html();
            if (!content.contains("\"estate\"") || !content.contains("\"price\"")) return;

            try {
                final int priceIdx = content.indexOf("\"price\"");
                final int braceStart = content.lastIndexOf("{", priceIdx);
                if (braceStart <= 0) return;

                final String jsonPart = extractBalancedJson(content, braceStart);
                final String unescaped = unescapeJson(jsonPart);
                final JsonNode root = OBJECT_MAPPER.readTree(unescaped);
                findEstateInJson(root, builder);
            } catch (Exception e) {
                log.debug("Failed to parse script JSON: {}", e.getMessage());
            }
        });
    }

    private void findEstateInJson(JsonNode root, PropertyDTO.PropertyDTOBuilder builder) {
        if (root.isObject()) {
            root.fields().forEachRemaining(entry -> {
                if ("estate".equals(entry.getKey()) && entry.getValue().isObject()) {
                    extractEstateData(entry.getValue(), builder);
                } else if (entry.getValue().isObject() || entry.getValue().isArray()) {
                    findEstateInJson(entry.getValue(), builder);
                }
            });
        } else if (root.isArray()) {
            root.forEach(node -> findEstateInJson(node, builder));
        }
    }

    private void extractDataFromDetailPage(Document doc, PropertyDTO.PropertyDTOBuilder builder, String url) {
        try {
            extractTitleAndStreetFromH1(doc, builder);
            extractPriceFromCosts(doc, builder);

            final String bodyHtml = doc.body().html();
            extractYearFromBody(bodyHtml, builder);
            extractBedroomsFromBody(bodyHtml, builder);
            extractBathroomsFromBody(bodyHtml, builder);
            extractSurfaceFromBody(bodyHtml, builder);
            extractZoneFromSubtitle(doc, builder);
            extractCityFromSubtitle(doc, builder);
            extractDescriptionFromTemplate(doc, builder);
            extractImagesFromPage(doc, builder);

            final String propertyType = detectPropertyTypeFromUrl(url);
            builder.propertyType(mapPropertyType(propertyType));
        } catch (Exception e) {
            log.warn("Error extracting data from detail page {}: {}", url, e.getMessage());
        }
    }

    private void extractTitleAndStreetFromH1(Document doc, PropertyDTO.PropertyDTOBuilder builder) {
        final Element titleEl = doc.selectFirst("h1.estate-title");
        final Element fallbackEl = titleEl != null ? null : doc.selectFirst("h1");
        final Element activeEl = titleEl != null ? titleEl : fallbackEl;

        String title = null;
        if (activeEl != null) {
            title = activeEl.text();
        }
        if (title == null || title.isEmpty()) {
            final String pageTitle = doc.title();
            if (pageTitle != null && pageTitle.contains(" - ")) {
                title = pageTitle.substring(0, pageTitle.lastIndexOf(" - ")).trim();
            } else {
                title = pageTitle;
            }
        }
        if (title == null || title.isEmpty()) return;

        builder.title(title);

        extractStreetFromTitle(title, builder);
    }

    private void extractStreetFromTitle(String title, PropertyDTO.PropertyDTOBuilder builder) {
        final String[] parts = title.split(" en ");
        if (parts.length <= 1) return;

        final String candidate = parts.length > 2 ? parts[2].trim() : parts[1].trim();
        if (candidate.isEmpty()) return;

        final boolean looksLikeAddress = candidate.matches(".*(Av|Calle|C/|Avenida|Plaza|Paseo|Ronda|Camino|Carretera|Urbanización|Avda|Callejón|Pasaje|Travesía).*")
            || candidate.matches(".*\\d+.*")
            || candidate.length() > 15;

        if (looksLikeAddress) {
            builder.street(candidate);
        }
    }

    private void extractPriceFromCosts(Document doc, PropertyDTO.PropertyDTOBuilder builder) {
        doc.select("template[slot=estate-costs] strong").forEach(strong -> {
            final String text = strong.text();
            if (!text.contains("€") || text.contains("/")) return;

            final String priceClean = text.replaceAll("[^0-9]", "");
            if (!priceClean.isEmpty() && priceClean.length() > 3) {
                try {
                    builder.price(Long.parseLong(priceClean));
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    private void extractYearFromBody(String bodyHtml, PropertyDTO.PropertyDTOBuilder builder) {
        final int yearIdx = bodyHtml.indexOf("Año de construcción:");
        if (yearIdx <= 0) return;

        final int start = yearIdx + 25;
        final int end = Math.min(start + 10, bodyHtml.length());
        final String yearPart = bodyHtml.substring(start, end);
        final String yearClean = yearPart.replaceAll("[^0-9]", "");

        if (yearClean.length() == 4) {
            try {
                builder.yearConstruction(Integer.parseInt(yearClean));
            } catch (NumberFormatException ignored) {}
        }
    }

    private void extractBedroomsFromBody(String bodyHtml, PropertyDTO.PropertyDTOBuilder builder) {
        if (builder.build().getBedrooms() != null) return;
        extractIntFromBody(bodyHtml, "dormitorios", builder::bedrooms);
    }

    private void extractBathroomsFromBody(String bodyHtml, PropertyDTO.PropertyDTOBuilder builder) {
        if (builder.build().getBathrooms() != null) return;
        extractIntFromBody(bodyHtml, "baño", builder::bathrooms);
    }

    private void extractSurfaceFromBody(String bodyHtml, PropertyDTO.PropertyDTOBuilder builder) {
        if (builder.build().getSurface() != null) return;
        extractIntFromBody(bodyHtml, "m2,", builder::surface);
    }

    private void extractIntFromBody(String bodyHtml, String keyword, java.util.function.Consumer<Integer> setter) {
        final int idx = bodyHtml.indexOf(keyword);
        if (idx <= 0) return;

        final String before = bodyHtml.substring(Math.max(0, idx - 10), idx);
        final String num = before.replaceAll("[^0-9]", "");
        if (num.isEmpty()) return;

        try {
            setter.accept(Integer.parseInt(num));
        } catch (NumberFormatException ignored) {}
    }

    private void extractZoneFromSubtitle(Document doc, PropertyDTO.PropertyDTOBuilder builder) {
        final Element subtitleEl = doc.selectFirst("h2.estate-subtitle");
        if (subtitleEl == null) return;

        final String subtitle = subtitleEl.text();
        if (!subtitle.contains(",")) return;

        final String district = subtitle.split(",")[0].trim();
        if (!district.isEmpty()) {
            builder.zone(district);
        }
    }

    private void extractCityFromSubtitle(Document doc, PropertyDTO.PropertyDTOBuilder builder) {
        final Element subtitleEl = doc.selectFirst("h2.estate-subtitle");
        if (subtitleEl == null) return;

        final String subtitle = subtitleEl.text();
        if (subtitle.isEmpty()) return;

        final String city;
        if (subtitle.contains(",")) {
            final String[] parts = subtitle.split(",");
            city = parts[parts.length - 1].trim();
        } else {
            city = subtitle.trim();
        }

        if (!city.isEmpty()) {
            builder.city(city);
        }
    }

    private void extractImagesFromPage(Document doc, PropertyDTO.PropertyDTOBuilder builder) {
        try {
            if (builder.build().getImageUrls() != null && !builder.build().getImageUrls().isEmpty()) return;
        } catch (Exception ignored) { return; }

        final String galleryJson = extractGalleryJsonFromScripts(doc);
        if (galleryJson != null) {
            builder.imageUrls(galleryJson);
            return;
        }

        final List<String> imageUrls = new ArrayList<>();
        final int maxImages = 5;

        doc.select("img[src*='/estates/']").forEach(img -> {
            if (imageUrls.size() >= maxImages) return;
            final String src = img.attr("src");
            if (!src.isEmpty() && !src.contains("data:image")) {
                imageUrls.add(src.startsWith("//") ? "https:" + src : src);
            }
        });

        if (!imageUrls.isEmpty()) {
            try {
                builder.imageUrls(OBJECT_MAPPER.writeValueAsString(imageUrls));
            } catch (Exception ignored) {}
        }
    }

    private String extractGalleryJsonFromScripts(Document doc) {
        for (Element script : doc.select("script")) {
            final String content = script.html();
            if (!content.contains("\"estate\"") || !content.contains("\"images\"")) continue;
            try {
                final int imagesIdx = content.indexOf("\"images\"");
                final int braceStart = content.lastIndexOf("{", imagesIdx);
                if (braceStart <= 0) continue;

                final String jsonPart = extractBalancedJson(content, braceStart);
                final String unescaped = unescapeJson(jsonPart);
                final JsonNode root = OBJECT_MAPPER.readTree(unescaped);

                final List<String> urls = new ArrayList<>();
                extractImageUrlsFromNode(root, urls, 5);
                if (!urls.isEmpty()) {
                    return OBJECT_MAPPER.writeValueAsString(urls);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void extractImageUrlsFromNode(JsonNode node, List<String> urls, int max) {
        if (urls.size() >= max) return;
        if (node.isObject()) {
            if (node.has("url") && node.get("url").isObject()) {
                final JsonNode urlNode = node.get("url");
                final String url = Stream.of("gallery", "detail")
                    .map(key -> urlNode.has(key) && !urlNode.get(key).isNull() ? urlNode.get(key).asText() : "")
                    .filter(v -> !v.isEmpty())
                    .findFirst()
                    .orElse("");
                if (!url.isEmpty()) urls.add(url);
            } else {
                node.fields().forEachRemaining(entry -> {
                    if (entry.getValue().isObject() || entry.getValue().isArray()) {
                        extractImageUrlsFromNode(entry.getValue(), urls, max);
                    }
                });
            }
        } else if (node.isArray()) {
            node.forEach(n -> extractImageUrlsFromNode(n, urls, max));
        }
    }

    private void extractDescriptionFromTemplate(Document doc, PropertyDTO.PropertyDTOBuilder builder) {
        final Element descTemplate = doc.selectFirst("template[slot=estate-description]");
        if (descTemplate == null) return;

        final String description = descTemplate.text();
        if (!description.isEmpty()) {
            builder.description(description);
        }
    }

    private void extractEstateData(JsonNode estate, PropertyDTO.PropertyDTOBuilder builder) {
        extractLongField(estate, "price", n -> n.asLong(), builder::price);
        extractIntField(estate, "surface", n -> n.asInt(), builder::surface);
        extractIntField(estate, "rooms", n -> n.asInt(), builder::bedrooms);
        extractIntField(estate, "baths", n -> n.asInt(), builder::bathrooms);
        extractIntField(estate, "bathrooms", n -> n.asInt(), builder::bathrooms);
        extractIntField(estate, "year", n -> n.asInt(), builder::yearConstruction);
        extractIntField(estate, "year_construction", n -> n.asInt(), builder::yearConstruction);
        extractTextField(estate, "floor", builder::floor);
        extractTextField(estate, "address", builder::street);
        extractTextField(estate, "district", builder::zone);
        extractTextField(estate, "title", builder::title);
        extractObjectTextField(estate, "city", "title", builder::city);
        extractObjectTextField(estate, "province", "title", builder::province);
        extractEnergyClass(estate, builder);
        extractBooleanField(estate, "elevator", builder::elevator);
        extractTextField(estate, "heating", builder::heating);
        extractDescription(estate, builder);
        extractPropertyTypeField(estate, "typology", builder);
        extractPropertyTypeField(estate, "property_type", builder);
        extractImagesV2(estate, builder);
        extractAgencyData(estate, builder);
    }

    private void extractLongField(JsonNode estate, String fieldName,
                                   java.util.function.Function<JsonNode, Long> converter,
                                   java.util.function.Consumer<Long> setter) {
        if (!estate.has(fieldName) || estate.get(fieldName).isNull()) return;
        try {
            setter.accept(converter.apply(estate.get(fieldName)));
        } catch (Exception ignored) {}
    }

    private void extractIntField(JsonNode estate, String fieldName,
                                  java.util.function.Function<JsonNode, Integer> converter,
                                  java.util.function.Consumer<Integer> setter) {
        if (!estate.has(fieldName) || estate.get(fieldName).isNull()) return;
        try {
            setter.accept(converter.apply(estate.get(fieldName)));
        } catch (Exception ignored) {}
    }

    private void extractTextField(JsonNode estate, String fieldName,
                                   java.util.function.Consumer<String> setter) {
        if (!estate.has(fieldName) || estate.get(fieldName).isNull()) return;
        setter.accept(estate.get(fieldName).asText());
    }

    private void extractObjectTextField(JsonNode parent, String objectField, String textField,
                                         java.util.function.Consumer<String> setter) {
        if (!parent.has(objectField) || parent.get(objectField).isNull()) return;
        final JsonNode obj = parent.get(objectField);
        if (obj.has(textField) && !obj.get(textField).isNull()) {
            setter.accept(obj.get(textField).asText());
        }
    }

    private void extractBooleanField(JsonNode estate, String fieldName,
                                      java.util.function.Consumer<Boolean> setter) {
        if (!estate.has(fieldName) || estate.get(fieldName).isNull()) return;
        setter.accept(estate.get(fieldName).asBoolean());
    }

    private void extractEnergyClass(JsonNode estate, PropertyDTO.PropertyDTOBuilder builder) {
        Stream.of("energy", "energy_class")
            .map(field -> estate.has(field) && !estate.get(field).isNull() ? estate.get(field).asText() : "")
            .filter(value -> !value.isEmpty() && value.length() <= 2)
            .findFirst()
            .ifPresent(builder::energyClass);
    }

    private void extractDescription(JsonNode estate, PropertyDTO.PropertyDTOBuilder builder) {
        if (!estate.has("description") || estate.get("description").isNull()) return;
        final String rawDesc = estate.get("description").asText();
        final String cleanDesc = Jsoup.parse(rawDesc).text();
        builder.description(cleanDesc);
    }

    private void extractPropertyTypeField(JsonNode estate, String fieldName,
                                           PropertyDTO.PropertyDTOBuilder builder) {
        if (!estate.has(fieldName) || estate.get(fieldName).isNull()) return;
        final String type = estate.get(fieldName).asText().toLowerCase();
        builder.propertyType(mapPropertyType(type));
    }

    private void extractImagesV2(JsonNode estate, PropertyDTO.PropertyDTOBuilder builder) {
        if (!estate.has("images") || estate.get("images").isNull() || !estate.get("images").isArray()) return;

        final int maxImages = 5;
        final List<String> imageUrls = new ArrayList<>();
        final var images = estate.get("images");

        for (int i = 0; i < images.size() && imageUrls.size() < maxImages; i++) {
            final JsonNode img = images.get(i);
            if (img.has("url") && !img.get("url").isNull()) {
                final JsonNode urlNode = img.get("url");
                final String url = Stream.of("gallery", "detail")
                    .map(key -> urlNode.has(key) && !urlNode.get(key).isNull() ? urlNode.get(key).asText() : "")
                    .filter(value -> !value.isEmpty())
                    .findFirst()
                    .orElse("");
                if (!url.isEmpty()) {
                    imageUrls.add(url);
                }
            }
        }

        if (!imageUrls.isEmpty()) {
            try {
                final String json = OBJECT_MAPPER.writeValueAsString(imageUrls);
                builder.imageUrls(json);
            } catch (Exception ignored) {}
        }
    }

    private void extractAgencyData(JsonNode estate, PropertyDTO.PropertyDTOBuilder builder) {
        if (!estate.has("agency") || estate.get("agency").isNull()) return;

        final JsonNode agency = estate.get("agency");
        extractRegionFromAgency(agency, builder);
        appendAgencyInfo(agency, builder);
    }

    private void extractRegionFromAgency(JsonNode agency, PropertyDTO.PropertyDTOBuilder builder) {
        if (!agency.has("region") || agency.get("region").isNull()) return;
        final JsonNode regionNode = agency.get("region");
        if (regionNode.has("title")) {
            builder.autonomousCommunity(regionNode.get("title").asText());
        }
    }

    private void appendAgencyInfo(JsonNode agency, PropertyDTO.PropertyDTOBuilder builder) {
        final StringBuilder agencyInfo = new StringBuilder();
        if (agency.has("name")) {
            agencyInfo.append("Name: ").append(agency.get("name").asText());
        }
        if (agency.has("phone")) {
            if (!agencyInfo.isEmpty()) agencyInfo.append("; ");
            agencyInfo.append("Phone: ").append(agency.get("phone").asText());
        }
        if (agencyInfo.isEmpty()) return;

        try {
            final String currentDesc = builder.build().getDescription();
            if (currentDesc != null && !currentDesc.isEmpty()) {
                builder.description(currentDesc + " | Agency: " + agencyInfo);
            } else {
                builder.description("Agency: " + agencyInfo);
            }
        } catch (Exception ignored) {}
    }

    private String detectPropertyTypeFromUrl(String url) {
        return Stream.of("/piso/", "/casa/", "/atico/", "/chalet/", "/adosado/", "/villa/", "/estudio/", "/loft/")
            .filter(url::contains)
            .findFirst()
            .map(pattern -> pattern.replaceAll("/", ""))
            .orElse("piso");
    }

    private String buildSearchUrl(String city) {
        final Optional<ProvinceItem> province = tecnocasaConfig.findProvinceByName(city);
        final Optional<String> communityName = tecnocasaConfig.getCommunityNameForProvince(city);

        if (province.isPresent() && communityName.isPresent()) {
            final String communitySlug = normalizeSlug(communityName.get());
            return BASE_URL + "/venta/inmuebles/" + communitySlug + "/" + province.get().getSlug() + ".html";
        }

        return BASE_URL + "/venta/inmuebles/" + normalizeSlug(city) + ".html";
    }

    private String normalizeSlug(String text) {
        return text.toLowerCase()
            .replace(" ", "-")
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ü", "u")
            .replace("ñ", "n")
            .replace("'", "");
    }

    private void delayBetweenRequests() {
        try {
            final int delayMs = baseDelayMs + RANDOM.nextInt(maxRandomDelayMs);
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private PropertyType mapPropertyType(String category) {
        if (category == null) return PropertyType.OTHER;
        return switch (category.toLowerCase()) {
            case "piso", "apartamento" -> PropertyType.APARTMENT;
            case "casa", "chalet" -> PropertyType.HOUSE;
            case "atico" -> PropertyType.PENTHOUSE;
            case "villa" -> PropertyType.VILLA;
            case "adosado" -> PropertyType.TOWNHOUSE;
            case "estudio" -> PropertyType.STUDIO;
            case "loft" -> PropertyType.LOFT;
            default -> PropertyType.OTHER;
        };
    }
}
