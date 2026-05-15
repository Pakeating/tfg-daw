package com.inmopaco.PropertyService.infrastructure.scraper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Component
public class TecnocasaConfig {

    private CommunitiesData communitiesData = new CommunitiesData();

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            ClassPathResource resource = new ClassPathResource("tecnocasa-communities.yml");
            try (InputStream is = resource.getInputStream()) {
                ScrapingRoot root = mapper.readValue(is, ScrapingRoot.class);
                this.communitiesData = new CommunitiesData();
                this.communitiesData.setCommunities(new ArrayList<>(root.getScraping().getCommunities()));
                log.info("Loaded {} communities with {} provinces",
                    root.getScraping().getCommunities().size(),
                    root.getScraping().getCommunities().stream()
                        .mapToInt(c -> c.getProvinces().size())
                        .sum());
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load Tecnocasa config", e);
        }
    }

    public List<CommunityItem> getCommunities() {
        return communitiesData.getCommunities();
    }

    public List<String> getAllProvinceNames() {
        return communitiesData.getCommunities().stream()
            .flatMap(community -> community.getProvinces().stream())
            .map(ProvinceItem::getName)
            .toList();
    }

    public Optional<ProvinceItem> findProvinceByName(String name) {
        return communitiesData.getCommunities().stream()
            .flatMap(community -> community.getProvinces().stream())
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    public Optional<String> getCommunityNameForProvince(String provinceName) {
        return communitiesData.getCommunities().stream()
            .filter(community -> community.getProvinces().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(provinceName)))
            .map(CommunityItem::getName)
            .findFirst();
    }

    public static class ScrapingRoot {
        private Scraping scraping = new Scraping();

        public Scraping getScraping() { return scraping; }
        public void setScraping(Scraping scraping) { this.scraping = scraping; }
    }

    public static class Scraping {
        private Communities communities = new Communities();

        public Communities getCommunities() { return communities; }
        public void setCommunities(List<CommunityItem> communities) { this.communities = new Communities(communities); }
    }

    public static class Communities extends ArrayList<CommunityItem> {
        public Communities() {}
        public Communities(List<CommunityItem> items) { super(items); }
    }

    public static class CommunitiesData {
        private List<CommunityItem> communities = new ArrayList<>();

        public List<CommunityItem> getCommunities() { return communities; }
        public void setCommunities(List<CommunityItem> communities) { this.communities = communities; }
    }

    public static class CommunityItem {
        private String name;
        private ArrayList<ProvinceItem> provinces = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public ArrayList<ProvinceItem> getProvinces() { return provinces; }
        public void setProvinces(ArrayList<ProvinceItem> provinces) { this.provinces = provinces; }
    }

    public static class ProvinceItem {
        private String name;
        private String slug;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
    }
}