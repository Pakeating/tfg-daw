package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.impl;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.dto.AuctionSummaryDTO;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.JsoupScraperChainNode;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.JsoupScraperChainService;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.dto.JsoupChainContextDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.LinkedList;


@Service
@RequiredArgsConstructor
@Log4j2
public class JsoupScraperChainServiceImpl implements JsoupScraperChainService {
    private final Deque<JsoupScraperChainNode> nodeList = new LinkedList<>();
    private JsoupChainContextDTO context;

    //this should be done via reflection, but I prefer not to do it due to reflection problems with native compilation
//    @Autowired
//    private JsoupAuctionDetailsNodeVer1ChainNode ver1Node;

    private JsoupScraperChainServiceImpl(AuctionSummaryDTO summary){
        this.context = new JsoupChainContextDTO(
                summary,
                AuctionDetailsDTO.builder()
                        .auctionId(summary.getBoeIdentifier())
                        .courtName(summary.getCourtName())
                        .expediente(summary.getExpediente())
        );
    }

    @Override
    public AuctionDetailsDTO execute() {
        log.info("Executing JsoupScraperChain with {} nodes for {}", nodeList.size(), context.getSummary().getBoeIdentifier());
        while (!nodeList.isEmpty()) {
            JsoupScraperChainNode node = nodeList.pollFirst();
            node.execute(context);
        }
        return context.getBuilder().build();
    }

    @Override
    public JsoupScraperChainService add(JsoupScraperChainNode node) {
        nodeList.addLast(node);
        return this;
    }

    @Override
    public JsoupScraperChainService create(AuctionSummaryDTO summary) {
        var instance = new JsoupScraperChainServiceImpl(summary);
        instance.nodeList.clear();
        return instance;
    }

}
