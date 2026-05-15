package com.inmopaco.AuctionService.infrastructure.scraper.impl;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.usecases.AuctionsPersistenceUsecase;
import com.inmopaco.AuctionService.infrastructure.scraper.AuctionScraperProviderService;
import com.inmopaco.AuctionService.infrastructure.scraper.config.AuctionConfig;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.JsoupScraperProviderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class AuctionScraperProviderServiceImpl implements AuctionScraperProviderService {

    @Autowired
    private AuctionsPersistenceUsecase auctionsPersistence;
    @Autowired
    private JsoupScraperProviderService jsoupProvider;
    @Autowired
    private AuctionConfig auctionConfig;

    @Override
    public Integer fetchAllSearchResults() {
        List<String> auctionIdList =  new ArrayList<>();

        auctionConfig.getProvinces().forEach((name, code) -> {
           try {
               log.info("[fetchSearchResults] Fetching auctions for province: {}, code: {}", name, code);
               List<AuctionDetailsDTO> auctionList = chooseProvider(code);
               try {
                   auctionsPersistence.smartSaveObtainedAuctions(auctionList);
                   auctionIdList.addAll(auctionList.stream().map(AuctionDetailsDTO::getAuctionId).toList());
               } catch (Exception e) {
                   log.warn("[fetchSearchResults] Error saving {} auctions for {}, code : {}", auctionList.size(), name, code);
                   //TODO: DEBERIA CONFIGURAR RETRIES...
               }
           }catch (Exception e){
                log.warn("[fetchSearchResults] Error fetching auctions for province: {}, code: {}. Error: {}", name, code, e.getMessage(), e);
           }
        });

        log.info("[fetchSearchResults] Finished fetching and saving auctions. Total auctions fetched: {}", auctionIdList.size());
        log.info("[fetchSearchResults] Auction IDs: {}", auctionIdList.stream().reduce((a, b) -> a + ", " + b).orElse("No auctions found"));
        return auctionIdList.size();
    }

    @Override
    public Integer fetchSearchResultsByProvince(String province) {
        List<AuctionDetailsDTO> auctionList = chooseProvider(province);
        auctionsPersistence.smartSaveObtainedAuctions(auctionList);

        log.info("[fetchSearchResultsByProvince] Finished fetching and saving auctions for province {}. Total auctions fetched: {}", province, auctionList.size());
        log.info("[fetchSearchResultsByProvince] Auction IDs: {}", auctionList.stream().map(AuctionDetailsDTO::getAuctionId).reduce((a, b) -> a + ", " + b).orElse("No auctions found"));

        return auctionList.size();
    }

    private List<AuctionDetailsDTO> chooseProvider(String province){
        return jsoupProvider.fetchSearchResults(province);
    }

}
