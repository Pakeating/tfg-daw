package com.inmopaco.AuctionService.infrastructure.pdf;

import lombok.extern.log4j.Log4j2;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class PdfProcessingServiceImpl implements PdfProcessingService {

    private final HttpClient httpClient;
    private final Tesseract tesseract;

    public PdfProcessingServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(getTessDataPath());
        this.tesseract.setLanguage("spa");
        this.tesseract.setOcrEngineMode(1);
    }

    private String getTessDataPath() {
        String envPath = System.getenv("TESSDATA_PATH");
        if (envPath != null && !envPath.isBlank()) {
            log.info("Using TESSDATA_PATH from environment: {}", envPath);
            return envPath;
        }

        String dockerPath = "/usr/share/tessdata";
        if (new java.io.File(dockerPath, "spa.traineddata").exists()) {
            log.info("Using Docker tessdata path: {}", dockerPath);
            return dockerPath;
        }

        String userDir = System.getProperty("user.dir");
        String localPath = userDir + "/tessdata";
        if (new java.io.File(localPath, "spa.traineddata").exists()) {
            log.info("Using local tessdata path: {}", localPath);
            return localPath;
        }

        log.warn("No tessdata path with 'spa.traineddata' found, defaulting to: {}", localPath);
        return localPath;
    }

    @Override
    public String getTextFromPdfUrl(String pdfUrl) {
        try {
            log.info("Processing PDF from URL: {}", pdfUrl);
            byte[] pdfBytes = downloadPdf(pdfUrl);
            return extractTextFromPdf(pdfBytes);
        } catch (Exception e) {
            log.error("Failed to process PDF from URL: {}", pdfUrl, e);
            throw new RuntimeException("Error processing PDF", e);
        }
    }

    private byte[] downloadPdf(String pdfUrl) throws Exception {
        log.debug("Downloading PDF...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(pdfUrl))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "application/pdf")
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download PDF. HTTP status: " + response.statusCode());
        }

        return response.body();
    }

    private String extractTextFromPdf(byte[] pdfData) throws IOException, TesseractException {
        log.debug("Extracting text from PDF data of size {} bytes", pdfData.length);
        
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document);
            
            log.debug("Extracted {} characters using PDFTextStripper", extractedText.length());
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                log.info("PDFTextStripper returned empty text, attempting OCR fallback...");
                return performOcrFallback(document);
            }
            
            return extractedText;
        }
    }

    //Limito las páginas ya que si no consume muchisimos recursos, y la informacion relevante suele estar en las primeras
    private static final int MAX_OCR_PAGES = 25;

    private String performOcrFallback(PDDocument document) throws IOException, TesseractException {
        int totalPages = document.getNumberOfPages();
        int pagesToProcess = Math.min(totalPages, MAX_OCR_PAGES);
        log.info("Starting OCR fallback for PDF with {} total pages. Limiting to first {} pages.", totalPages, pagesToProcess);
        
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<String> pageTexts = new ArrayList<>();
        
        for (int pageNum = 0; pageNum < pagesToProcess; pageNum++) {
            log.debug("Processing page {} for OCR", pageNum + 1);
            
            BufferedImage image = pdfRenderer.renderImageWithDPI(pageNum, 300);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            String ocrText = performOcrOnImage(imageBytes);
            
            if (ocrText != null && !ocrText.trim().isEmpty()) {
                pageTexts.add(ocrText);
                log.debug("OCR extracted {} characters from page {}", ocrText.length(), pageNum + 1);
            } else {
                log.warn("OCR returned empty text for page {}", pageNum + 1);
            }
            
            image.flush();
        }
        
        String fullText = String.join("\n\n", pageTexts);
        log.info("OCR fallback extracted {} total characters from {} pages", fullText.length(), document.getNumberOfPages());
        
        return fullText;
    }

    private String performOcrOnImage(byte[] imageBytes) throws TesseractException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);
            
            String result = tesseract.doOCR(image);
            
            return result;
        } catch (IOException e) {
            log.error("Failed to read image for OCR", e);
            throw new TesseractException("Failed to process image for OCR", e);
        }
    }
}
