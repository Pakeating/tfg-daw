package com.inmopaco.PropertyService.infrastructure.scraper.client.impl;

import com.inmopaco.PropertyService.infrastructure.scraper.client.ScraperClient;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class JsoupScraperClient implements ScraperClient {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Value("${property.scraper.delay.min:1000}")
    private int minDelay;

    @Value("${property.scraper.delay.max:3000}")
    private int maxDelay;

    @Value("${property.scraper.timeout:30000}")
    private int timeout;

    @Value("${property.scraper.user-agent:}")
    private String customUserAgent;

    private final Random random = new Random();

    @Override
    public Document fetchUrl(String url) throws Exception {
        delay();
        String userAgent = customUserAgent.isEmpty() ? DEFAULT_USER_AGENT : customUserAgent;
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeout)
                .sslSocketFactory(createTrustAllSocketFactory())
                .get();
    }

    @Override
    public Document postUrl(String url, String... keyValues) throws Exception {
        delay();
        String userAgent = customUserAgent.isEmpty() ? DEFAULT_USER_AGENT : customUserAgent;
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeout)
                .sslSocketFactory(createTrustAllSocketFactory())
                .data(keyValues)
                .post();
    }

    private void delay() throws InterruptedException {
        int delayMs = minDelay + random.nextInt(maxDelay - minDelay);
        TimeUnit.MILLISECONDS.sleep(delayMs);
    }

    private SSLSocketFactory createTrustAllSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create SSL socket factory", e);
        }
    }
}