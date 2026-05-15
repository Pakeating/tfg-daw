package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.nodes;

import com.inmopaco.AuctionService.application.dto.AuctionSummaryDTO;
import com.inmopaco.AuctionService.application.dto.LotDTO;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.JsoupScraperChainNode;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.dto.JsoupChainContextDTO;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.common.JsoupAuctionScraperClient;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class JsoupAuctionDetailsNodeVer3ChainNode extends AbstractJsoupAuctionDetailsNode implements JsoupScraperChainNode  {

    private static final String REFERER_HEADER = "https://subastas.boe.es/subastas_ava.php";
    private static final int SECTION_NUMBER = 3;
    private final JsoupAuctionScraperClient scraperClient = new JsoupAuctionScraperClient();

    private String url;

    @Override
    public JsoupChainContextDTO execute(JsoupChainContextDTO context) {
        url = buildUrlForSection(context.getSummary().getDetailUrl(), SECTION_NUMBER);
        Document document = fetchAuctionDetails(context.getSummary());
        return parse(document, context);
    }

    private Document fetchAuctionDetails(AuctionSummaryDTO summary) {

        if (summary.getDetailUrl() == null || summary.getDetailUrl().isBlank()) {
            throw new IllegalArgumentException("The auction detail URL is missing for ID: " + summary.getBoeIdentifier());
        }
        try {
            randomDelay();
            Connection.Response response = scraperClient.createConnectionFromUrl(url, Connection.Method.GET)
                    .header("Referer", REFERER_HEADER)
                    .execute();

            return response.parse();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Scraper was interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching detail for auction " + summary.getBoeIdentifier() + ": " + e.getMessage(), e);
        }
    }

    private JsoupChainContextDTO parse(Document doc, JsoupChainContextDTO context) {
        Element mainBlock = doc.selectFirst("#idBloqueDatos3");
        if (mainBlock == null) return context;

        Element labelTab = doc.selectFirst("label[for=dropDownFiltro]");
        boolean isMultiLot = labelTab != null && labelTab.text().equalsIgnoreCase("Lotes");

        if (isMultiLot) {
            log.info("Multi-lot auction detected for ID: {}", context.getSummary().getBoeIdentifier());
            multiLotParsing(doc, context);
        } else {
            monoLotParsing(doc, context);
        }

        return context;
    }

    private void multiLotParsing(Document doc, JsoupChainContextDTO context) {

        List<LotDTO> lotList = new ArrayList<>();
        Elements lotTabs = doc.select("ul.navlistver li a");

        lotTabs.stream()
                .map(a -> "https://subastas.boe.es/".concat(a.attr("href").replaceFirst("\\.", "")))
                .forEach(url -> {
                    Document document = fetchLot(url, context);
                    LotDTO lot = parseLot(document, context);
                    lotList.add(lot);
                });
        log.info("Total lots parsed for multi-lot auction {}: {}", context.getSummary().getBoeIdentifier(), lotList.size());
        context.getBuilder().lots(lotList);
    }

    private void monoLotParsing(Document doc, JsoupChainContextDTO context) {
        Element mainBlock = doc.selectFirst("#idBloqueDatos3");
        if (mainBlock == null) return;

        Element registryBox = mainBlock.selectFirst("div.caja");
        if (registryBox != null) {
            context.getBuilder().registryData(registryBox.text());
            if (registryBox.text().contains("CRU:")) {
                context.getBuilder().cru(registryBox.text().split("CRU:")[1].trim().split(" ")[0].replace(".", ""));
            }
        }

        Element table = mainBlock.selectFirst("table");
        if (table != null) {
            context.getBuilder()
                    .goodsDescription(extract(table, "Descripción"))
                    .cadastralReference(extract(table, "Referencia catastral"))
                    .propertyAddress(extract(table, "Dirección"))
                    .postalCode(extract(table, "Código Postal"))
                    .city(extract(table, "Localidad"))
                    .province(extract(table, "Provincia"))
                    .isHabitualResidence(extract(table, "Vivienda habitual"))
                    .possessionStatus(extract(table, "Situación posesoria"))
                    .isVisitable(extract(table, "Visitable"));
        }
    }

    private Document fetchLot(String lotUrl, JsoupChainContextDTO context) {
        try {
            randomDelay();
            Connection.Response response = scraperClient.createConnectionFromUrl(lotUrl, Connection.Method.GET)
                    .header("Referer", REFERER_HEADER)
                    .execute();

            return response.parse();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Scraper was interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching lot detail for auction " + context.getSummary().getBoeIdentifier() + ": " + e.getMessage(), e);
        }
    }

    private LotDTO parseLot(Document doc, JsoupChainContextDTO context) {

        Element currentTab = doc.selectFirst("ul.navlistver li a.current");
        Element mainBlock = doc.selectFirst("#idBloqueDatos3");

        var lot = LotDTO.builder();

        if (mainBlock != null && currentTab != null) {
            Element economicTable = mainBlock.select("h3:contains(Datos relacionados) + table").first();
            Element lotTable = mainBlock.select("div h4 + table").first();

            lot.lotId(currentTab.text().replaceAll("[^0-9]", ""))
                    .lotTitle(currentTab.attr("title"))
                    .auctionValue(extract(economicTable, "Valor Subasta"))
                    .bidSteps(extract(economicTable, "Tramos entre pujas"))
                    .depositAmount(extract(economicTable, "Importe del depósito"))
                    .goodsDescription(extract(lotTable, "Descripción"))
                    .cadastralReference(extract(lotTable, "Referencia catastral"))
                    .propertyAddress(extract(lotTable, "Dirección"))
                    .city(extract(lotTable, "Localidad"))
                    .province(extract(lotTable, "Provincia"))
                    .possessionStatus(extract(lotTable, "Situación posesoria"))
                    .postalCode(extract(lotTable, "Código Postal"))
                    .isHabitualResidence(extract(lotTable, "Vivienda habitual"))
                    .isVisitable(extract(lotTable, "Visitable"))
                    .build();
        }
        return lot.build();
    }

    private String extract(Element container, String label) {
        Element header = container.selectFirst("th:containsOwn(" + label + ")");
        return (header != null && header.nextElementSibling() != null)
                ? header.nextElementSibling().text().trim() : "";
    }
}
