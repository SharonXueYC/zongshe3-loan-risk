package com.example.zongshe1.modules.crawler.repository;

import com.example.zongshe1.modules.crawler.entity.ExternalDataCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExternalDataCacheRepository extends JpaRepository<ExternalDataCache, Long> {

    Optional<ExternalDataCache> findFirstBySourceTypeAndIdCardNumberAndPhoneNumberAndExpiresAtAfterOrderByCrawledAtDesc(
            String sourceType, String idCardNumber, String phoneNumber, LocalDateTime now);

    Optional<ExternalDataCache> findFirstBySourceTypeAndExpiresAtAfterOrderByCrawledAtDesc(
            String sourceType, LocalDateTime now);
}
