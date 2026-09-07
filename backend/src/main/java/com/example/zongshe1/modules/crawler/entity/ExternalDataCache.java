package com.example.zongshe1.modules.crawler.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_external_data_cache", indexes = {
        @Index(name = "idx_cache_lookup", columnList = "sourceType,idCardNumber,phoneNumber")
})
@Data
public class ExternalDataCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "id_card_number", length = 18)
    private String idCardNumber;

    @Column(name = "phone_number", length = 11)
    private String phoneNumber;

    @Column(name = "payload_json", columnDefinition = "TEXT", nullable = false)
    private String payloadJson;

    @Column(name = "crawl_source_url", length = 512)
    private String crawlSourceUrl;

    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
