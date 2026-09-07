package com.example.zongshe1.modules.crawler.service;

import com.example.zongshe1.modules.risk.datasource.DataSourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示级数据爬取：解析 HTML 表格写入外部数据缓存，供风控适配器融合使用。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataCrawlerService {

    private final ExternalDataCacheService cacheService;
    private final ResourceLoader resourceLoader;

    @Value("${crawler.credit-bureau.url:classpath:crawler/demo-credit.html}")
    private String creditUrl;

    @Value("${crawler.telecom.url:classpath:crawler/demo-telecom.html}")
    private String telecomUrl;

    @Value("${crawler.cache-ttl-hours:24}")
    private int cacheTtlHours;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Transactional
    public Map<String, Object> runFullCrawl() {
        Map<String, Object> summary = new HashMap<>();
        try {
            summary.put("creditRows", crawlCreditBureau());
            summary.put("telecomRows", crawlTelecom());
            summary.put("message", "爬取完成，数据已写入 t_external_data_cache");
        } catch (Exception e) {
            log.error("数据爬取失败", e);
            throw new IllegalStateException("数据爬取失败：" + e.getMessage(), e);
        }
        log.info("数据爬取完成：{}", summary);
        return summary;
    }

    private int crawlCreditBureau() throws Exception {
        Document doc = loadDocument(creditUrl);
        Elements rows = doc.select("table#credit-data tbody tr");
        int count = 0;
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 3) continue;
            String suffix = cols.get(0).text().trim();
            int overdue = Integer.parseInt(cols.get(1).text().trim());
            int queryCount = Integer.parseInt(cols.get(2).text().trim());
            Map<String, Object> payload = Map.of(
                    "credit_overdue_count", overdue,
                    "credit_query_count_30d", queryCount,
                    "id_suffix", suffix
            );
            cacheService.save(DataSourceType.CREDIT_BUREAU.name(), "SUFFIX_" + suffix, "",
                    payload, creditUrl, cacheTtlHours);
            count++;
        }
        return count;
    }

    private int crawlTelecom() throws Exception {
        Document doc = loadDocument(telecomUrl);
        Elements rows = doc.select("table#telecom-data tbody tr");
        int count = 0;
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 3) continue;
            String suffix = cols.get(0).text().trim();
            int months = Integer.parseInt(cols.get(1).text().trim());
            boolean verified = Boolean.parseBoolean(cols.get(2).text().trim());
            Map<String, Object> payload = Map.of(
                    "telecom_online_months", months,
                    "telecom_real_name_verified", verified,
                    "phone_suffix", suffix
            );
            cacheService.save(DataSourceType.TELECOM.name(), "", "SUFFIX_" + suffix,
                    payload, telecomUrl, cacheTtlHours);
            count++;
        }
        return count;
    }

    private Document loadDocument(String url) throws Exception {
        if (url.startsWith("classpath:")) {
            Resource resource = resourceLoader.getResource(url);
            try (InputStream in = resource.getInputStream()) {
                return Jsoup.parse(in, "UTF-8", url);
            }
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("爬取失败 HTTP " + response.statusCode() + " url=" + url);
        }
        return Jsoup.parse(response.body(), url);
    }
}
