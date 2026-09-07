package com.example.zongshe1.modules.crawler.service;

import com.example.zongshe1.modules.risk.datasource.DataSourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataCrawlerServiceTest {

    @Mock
    private ExternalDataCacheService cacheService;

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private DataCrawlerService dataCrawlerService;

    @Test
    void runFullCrawl_shouldParseDemoHtmlAndSaveCache() {
        org.springframework.core.io.Resource creditRes = new DefaultResourceLoader()
                .getResource("classpath:crawler/demo-credit.html");
        org.springframework.core.io.Resource telecomRes = new DefaultResourceLoader()
                .getResource("classpath:crawler/demo-telecom.html");
        when(resourceLoader.getResource("classpath:crawler/demo-credit.html")).thenReturn(creditRes);
        when(resourceLoader.getResource("classpath:crawler/demo-telecom.html")).thenReturn(telecomRes);
        ReflectionTestUtils.setField(dataCrawlerService, "creditUrl", "classpath:crawler/demo-credit.html");
        ReflectionTestUtils.setField(dataCrawlerService, "telecomUrl", "classpath:crawler/demo-telecom.html");
        ReflectionTestUtils.setField(dataCrawlerService, "cacheTtlHours", 24);

        Map<String, Object> summary = dataCrawlerService.runFullCrawl();

        assertEquals(10, summary.get("creditRows"));
        assertEquals(10, summary.get("telecomRows"));

        ArgumentCaptor<String> sourceCaptor = ArgumentCaptor.forClass(String.class);
        verify(cacheService, atLeast(20)).save(sourceCaptor.capture(), any(), any(), anyMap(), anyString(), eq(24));
        assertTrue(sourceCaptor.getAllValues().contains(DataSourceType.CREDIT_BUREAU.name()));
    }
}
