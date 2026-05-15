package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.common;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Component
public class JsoupAuctionScraperClient {

    public final String BASE_URL = "https://subastas.boe.es";
    public final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    public final int TIMEOUT_MILLIS = 20000;
    public final String ACCEPT_HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8";

    public Connection createConnection(String endpoint, Connection.Method method) {
        return Jsoup.connect(BASE_URL + endpoint)
                .sslSocketFactory(socketFactory())
                .userAgent(USER_AGENT)
                .method(method)
                .timeout(TIMEOUT_MILLIS)
                .header("Accept", ACCEPT_HEADER)
                ;
    }

    public Connection createConnectionFromUrl(String url, Connection.Method method) {
        return Jsoup.connect(url)
                .sslSocketFactory(socketFactory())
                .userAgent(USER_AGENT)
                .method(method)
                .timeout(TIMEOUT_MILLIS)
                .header("Accept", ACCEPT_HEADER)
                ;
    }

    // Trust all SSL certificates for native image compatibility
    public SSLSocketFactory socketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL socket factory", e);
        }
    }

}
