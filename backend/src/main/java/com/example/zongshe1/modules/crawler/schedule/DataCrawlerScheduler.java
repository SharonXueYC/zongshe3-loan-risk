package com.example.zongshe1.modules.crawler.schedule;

import com.example.zongshe1.modules.crawler.service.DataCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "crawler.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class DataCrawlerScheduler {

    private final DataCrawlerService dataCrawlerService;

    /** 启动后 2 分钟首次爬取，之后每 6 小时刷新缓存 */
    @Scheduled(initialDelayString = "${crawler.scheduled.initial-delay-ms:120000}", fixedDelayString = "${crawler.scheduled.fixed-delay-ms:21600000}")
    public void scheduledCrawl() {
        try {
            dataCrawlerService.runFullCrawl();
        } catch (Exception e) {
            log.warn("定时爬取失败：{}", e.getMessage());
        }
    }
}
