package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.nodes;

import com.inmopaco.AuctionService.application.dto.AuctionSummaryDTO;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.JsoupScraperChainNode;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.dto.JsoupChainContextDTO;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.common.JsoupAuctionScraperClient;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
public class JsoupAuctionDetailsNodeVer2ChainNode extends AbstractJsoupAuctionDetailsNode implements JsoupScraperChainNode  {

    private static final String REFERER_HEADER = "https://subastas.boe.es/subastas_ava.php";
    private static final int SECTION_NUMBER = 2;
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

    public JsoupChainContextDTO parse(Document doc, JsoupChainContextDTO context) {
        // Seleccionamos específicamente el bloque de la autoridad
        Element block = doc.selectFirst("#idBloqueDatos2 table");

        if (block != null) {
            context.getBuilder()
                    .authorityCode(extract(block, "Código"))
                    .authorityDescription(extract(block, "Descripción"))
                    .authorityAddress(extract(block, "Dirección"))
                    .authorityPhone(extract(block, "Teléfono"))
                    .authorityFax(extract(block, "Fax"))
                    .authorityEmail(extract(block, "Correo electrónico"));
        }
        return context;
    }

    private String extract(Element container, String label) {
        // Buscamos el th que contiene la etiqueta exacta
        Element header = container.selectFirst("th:containsOwn(" + label + ")");
        if (header != null && header.nextElementSibling() != null) {
            return header.nextElementSibling().text().trim();
        }
        return "";
    }
}
