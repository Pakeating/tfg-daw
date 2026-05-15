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
public class JsoupAuctionDetailsNodeVer5ChainNode extends AbstractJsoupAuctionDetailsNode implements JsoupScraperChainNode  {

    private static final String REFERER_HEADER = "https://subastas.boe.es/subastas_ava.php";
    private static final int SECTION_NUMBER = 5;
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

    // Parses the document to determine if bids have been placed, real bids needs to be logged in
    private JsoupChainContextDTO parse(Document doc, JsoupChainContextDTO context) {
        // Seleccionamos el párrafo con la clase 'centrador' dentro del bloque de pujas
        Element infoElement = doc.selectFirst("#idBloqueDatos8 .centrador");

        if (infoElement != null) {
            String text = infoElement.text().toLowerCase();

            // Lógica booleana basada en el texto oficial del BOE:
            // "La subasta ha recibido alguna puja..." -> true
            // "La subasta no ha recibido pujas." -> false
            boolean bidsDetected = text.contains("ha recibido alguna puja");

            context.getBuilder().hasBids(bidsDetected);
        } else {
            // Por seguridad, si no encontramos el elemento, marcamos false
            context.getBuilder().hasBids(false);
        }
        return context;
    }
}
