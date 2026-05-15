package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.impl;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.dto.AuctionSummaryDTO;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.JsoupScraperProviderService;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.JsoupScraperChainService;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.nodes.*;
import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.common.JsoupAuctionScraperClient;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class JsoupScraperProviderServiceImpl implements JsoupScraperProviderService {
    @Autowired
    private JsoupScraperChainService chainService;

    @Autowired
    private JsoupAuctionScraperClient scraperClient;

    private static final String ENDPOINT = "/subastas_ava.php";
    private static final Connection.Method METHOD = Connection.Method.POST;

    @Override
    public List<AuctionDetailsDTO> fetchSearchResults(String province) {
        try {
            // TODO: Hay que montar form-data dinamicamente, ahora mismo solo metemos el cod provincia
            Map<String, String> formData = new HashMap<>();
            formData.put("campo[0]", "SUBASTA.ORIGEN");
            formData.put("dato[0]", "");
            formData.put("campo[1]", "SUBASTA.AUTORIDAD");
            formData.put("dato[1]", "");
            formData.put("campo[2]", "SUBASTA.ESTADO.CODIGO");
            formData.put("dato[2]", "EJ"); // En ejecución
            formData.put("campo[3]", "BIEN.TIPO");
            formData.put("dato[3]", "I");
            formData.put("dato[4]", "");
            formData.put("campo[5]", "BIEN.DIRECCION");
            formData.put("dato[5]", "");
            formData.put("campo[6]", "BIEN.CODPOSTAL");
            formData.put("dato[6]", "");
            formData.put("campo[7]", "BIEN.LOCALIDAD");
            formData.put("dato[7]", "");
            formData.put("campo[8]", "BIEN.COD_PROVINCIA");
            formData.put("dato[8]", province); // Valladolid (ejemplo)
            formData.put("campo[9]", "SUBASTA.POSTURA_MINIMA_MINIMA_LOTES");
            formData.put("dato[9]", "");
            formData.put("campo[10]", "SUBASTA.NUM_CUENTA_EXPEDIENTE_1");
            formData.put("dato[10]", "");
            formData.put("campo[11]", "SUBASTA.NUM_CUENTA_EXPEDIENTE_2");
            formData.put("dato[11]", "");
            formData.put("campo[12]", "SUBASTA.NUM_CUENTA_EXPEDIENTE_3");
            formData.put("dato[12]", "");
            formData.put("campo[13]", "SUBASTA.NUM_CUENTA_EXPEDIENTE_4");
            formData.put("dato[13]", "");
            formData.put("campo[14]", "SUBASTA.NUM_CUENTA_EXPEDIENTE_5");
            formData.put("dato[14]", "");
            formData.put("campo[15]", "SUBASTA.ID_SUBASTA_BUSCAR");
            formData.put("dato[15]", "");
            formData.put("campo[16]", "SUBASTA.ACREEDORES");
            formData.put("dato[16]", "");
            formData.put("campo[17][0]", ""); // Fecha fin desde
            formData.put("campo[17][1]", ""); // Fecha fin hasta
            formData.put("campo[18][0]", ""); // Fecha inicio desde
            formData.put("campo[18][1]", ""); // Fecha inicio hasta
            formData.put("page_hits", "500");
            formData.put("sort_field[0]", "SUBASTA.FECHA_FIN");
            formData.put("sort_order[0]", "asc");
            formData.put("accion", "Buscar");

            Connection.Response response = scraperClient.createConnection(ENDPOINT, METHOD)
                    .data(formData)
                    .followRedirects(true)
                    .execute();

            Document doc = response.parse();

            var summaryResults = parseResults(doc);

            log.info("Total summary results parsed: {}", summaryResults.size());

            var detailedAuctionList = summaryResults.stream()
                    .map(a -> chainService.create(a)
                            .add(new JsoupAuctionDetailsNodeVer1ChainNode())
                            .add(new JsoupAuctionDetailsNodeVer2ChainNode())
                            .add(new JsoupAuctionDetailsNodeVer3ChainNode())
                            .add(new JsoupAuctionDetailsNodeVer4ChainNode())
                            .add(new JsoupAuctionDetailsNodeVer5ChainNode())
                            .execute())
                    .toList();

            log.info("Detailed Auctions Fetched: {}", detailedAuctionList.size());
            detailedAuctionList.stream()
                    .map(AuctionDetailsDTO::getAuctionId)
                    .reduce((a,b)-> a + ", " + b)
                    .ifPresent(ids -> log.info("Auctions fully retrieved: {}", ids));

            return detailedAuctionList;
        } catch (IOException e) {
            System.err.println("Error connecting to BOE: " + e.getMessage());
            throw new RuntimeException("Error connecting to BOE: " + e.getMessage(), e);
        }
    }

    //TODO: No me mola como me ha quedado el codigo, refactorizar
    private List<AuctionSummaryDTO> parseResults(Document doc) {
        List<AuctionSummaryDTO> results = new ArrayList<>();

        // Seleccionamos cada bloque de subasta
        Elements auctionBlocks = doc.select("li.resultado-busqueda");

        for (Element block : auctionBlocks) {

            // Extraemos los datos base
            String rawTitle = block.select("h3").text();
            String id = rawTitle.replace("SUBASTA", "").split("\\(")[0].trim();
            String court = block.select("h4").text();

            // Buscamos el link de detalle (URL absoluta)
            Element linkElement = block.select("a.resultado-busqueda-link-defecto").first();
            String url = (linkElement != null) ? linkElement.attr("abs:href") : "";

            // Variables temporales para los párrafos variables
            String exp = "";
            String stat = "";
            String dead = "";
            String desc = "";

            Elements paragraphs = block.select("p");
            for (Element p : paragraphs) {
                String text = p.text();
                if (text.startsWith("Expediente:")) {
                    exp = text.replace("Expediente:", "").trim();
                } else if (text.startsWith("Estado:")) {
                    stat = text.split("-")[0].replace("Estado:", "").trim();
                    if (text.contains("[Conclusión prevista:")) {
                        dead = text.substring(text.indexOf(":") + 1, text.indexOf("]")).trim();
                    }
                } else {
                    // Si no es ni expediente ni estado, es la descripción del bien
                    desc = text;
                }
            }

            AuctionSummaryDTO dto = AuctionSummaryDTO.builder()
                    .boeIdentifier(id)
                    .courtName(court)
                    .expediente(exp)
                    .status(stat)
                    .deadline(dead)
                    .description(desc)
                    .detailUrl(url)
                    .build();

            results.add(dto);
        }

        return results;
    }
}
