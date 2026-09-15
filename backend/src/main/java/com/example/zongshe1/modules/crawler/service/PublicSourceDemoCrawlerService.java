package com.example.zongshe1.modules.crawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicSourceDemoCrawlerService {

    @Value("${risk.crawler.primary.dishonest.url:https://tzcourt.taizhou.gov.cn/jbjjzxnzl/sxbzxrpgt/art/2026/art_00f7520de1d4470bbb70bd84cb06293d.html}")
    private String dishonestUrl;

    @Value("${risk.crawler.primary.lpr.url:https://www.chinamoney.com.cn/chinese/rdgz/20260820/3399885.html}")
    private String lprUrl;

    @Value("${risk.crawler.http.timeout-ms:10000}")
    private int timeoutMs;

    @Value("${risk.crawler.ua:ZongShe3-Crawler/1.0 (team:岳炜杰; email:your.email@example.com)}")
    private String userAgent;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(10000))
            .build();

    public Map<String, Object> fetchDemoSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("dishonest", fetchDishonestInfo());
        snapshot.put("lpr", fetchLprInfo());
        snapshot.put("stale", false);
        return snapshot;
    }

    public Map<String, Object> fetchDishonestInfo() {
        try {
            Document doc = fetchDocument(dishonestUrl);
            Map<String, Object> data = new LinkedHashMap<>();

            String courtName = findFirstText(doc, "div.title, h1, div.header");
            String publishDate = findFirstText(doc, "*:matchesOwn(\\d{4}[-年]\\d{1,2}[-月]\\d{1,2})");
            String dishonestName = findValueNearKeyword(doc, "被执行人");
            String caseNumber = findValueNearKeyword(doc, "案号");

            data.put("dishonestHit", dishonestName != null && !dishonestName.isBlank());
            data.put("dishonestName", dishonestName == null ? "" : dishonestName);
            data.put("courtName", courtName == null ? "" : courtName);
            data.put("publishDate", publishDate == null ? LocalDate.now().toString() : publishDate);
            data.put("caseNumber", caseNumber == null ? "" : caseNumber);
            data.put("sourceUrl", dishonestUrl);
            return data;
        } catch (Exception e) {
            log.warn("抓取失信公示失败，返回演示值", e);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("dishonestHit", false);
            fallback.put("dishonestName", "");
            fallback.put("courtName", "");
            fallback.put("publishDate", LocalDate.now().toString());
            fallback.put("caseNumber", "");
            fallback.put("sourceUrl", dishonestUrl);
            return fallback;
        }
    }

    public Map<String, Object> fetchLprInfo() {
        try {
            Document doc = fetchDocument(lprUrl);
            Map<String, Object> data = new LinkedHashMap<>();

            String body = doc.text();
            BigDecimal lpr1y = parseFirstDecimalNearKeyword(body, "1年期");
            BigDecimal lpr5y = parseFirstDecimalNearKeyword(body, "5年期");
            String lprDate = extractDate(body);

            data.put("lpr1y", lpr1y == null ? BigDecimal.ZERO : lpr1y);
            data.put("lpr5y", lpr5y == null ? BigDecimal.ZERO : lpr5y);
            data.put("lprPublishDate", lprDate == null ? LocalDate.now().toString() : lprDate);
            data.put("sourceUrl", lprUrl);
            return data;
        } catch (Exception e) {
            log.warn("抓取 LPR 数据失败，返回演示值", e);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("lpr1y", BigDecimal.valueOf(3.65));
            fallback.put("lpr5y", BigDecimal.valueOf(4.30));
            fallback.put("lprPublishDate", LocalDate.now().toString());
            fallback.put("sourceUrl", lprUrl);
            return fallback;
        }
    }

    private Document fetchDocument(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("User-Agent", userAgent)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }

        return Jsoup.parse(response.body(), url);
    }

    private String findFirstText(Document doc, String css) {
        Elements elements = doc.select(css);
        if (elements.isEmpty()) {
            return null;
        }
        String text = elements.first().text();
        return text == null || text.isBlank() ? null : text;
    }

    private String findValueNearKeyword(Document doc, String keyword) {
        Elements candidates = doc.select("table tr, p, div, li");
        for (var element : candidates) {
            String text = element.text();
            if (text.contains(keyword)) {
                return text.replace(keyword, "").trim();
            }
        }
        return null;
    }

    private BigDecimal parseFirstDecimalNearKeyword(String body, String keyword) {
        Pattern pattern = Pattern.compile(keyword + "[^0-9]*([0-9]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }

    private String extractDate(String body) {
        Pattern pattern = Pattern.compile("(\\d{4}年\\d{1,2}月\\d{1,2}日)|(\\d{4}-\\d{1,2}-\\d{1,2})");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
