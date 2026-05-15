package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.nodes;

import com.inmopaco.AuctionService.application.dto.AuctionDocumentDTO;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
@Component
public class JsoupAuctionDetailsNodeVer1ChainNode extends AbstractJsoupAuctionDetailsNode implements JsoupScraperChainNode  {

    private static final String REFERER_HEADER = "https://subastas.boe.es/subastas_ava.php";
    private static final int SECTION_NUMBER = 1;
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
        Element tableBody = doc.select("#idBloqueDatos1 table tbody").first();

        List<AuctionDocumentDTO> pdfs = doc.select("li.puntoPDF a").stream()
                .map(a -> AuctionDocumentDTO.builder()
                        .documentUrl(a.attr("abs:href"))
                        .build()) // url absoluta
                .toList();

        if (tableBody == null) {
            return context; // Retorna DTO vacío si no hay tabla
        }

        context.getBuilder()
                .auctionId(extractValue(tableBody, "Identificador"))
                .type(extractValue(tableBody, "Tipo de subasta"))
                .countingAccount(extractValue(tableBody, "Cuenta expediente"))
                .dateOfStart(date2Instant(extractValue(tableBody, "Fecha de inicio")))
                .dateOfEnd(date2Instant(extractValue(tableBody, "Fecha de conclusión")))
                .claimedAmount(extractValue(tableBody, "Cantidad reclamada"))
                .lotsNumber(extractValue(tableBody, "Lotes"))
                .boeAnnouncement(extractValue(tableBody, "Anuncio BOE"))
                .auctionValue(extractValue(tableBody, "Valor subasta"))
                .appraisal(extractValue(tableBody, "Tasación"))
                .minimumBid(extractValue(tableBody, "Puja mínima"))
                .bidIncrements(extractValue(tableBody, "Tramos entre pujas"))
                .depositAmount(extractValue(tableBody, "Importe del depósito"))
                .documents(pdfs)
                ;
        return context;
    }

    private Instant date2Instant(String date){
        String isoDate = date.split("\\(ISO:")[1]
                .replace(")", "")
                .trim();
        return OffsetDateTime.parse(isoDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
    }
}
