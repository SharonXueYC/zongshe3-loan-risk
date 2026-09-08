package com.example.zongshe1.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FeatureSnapshotDTO {

    private Long userId;
    private Integer age;
    private Integer creditScore;
    private Integer overdueCount;
    private BigDecimal monthlyIncome;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private Boolean dishonestHit;
    private String dishonestName;
    private String courtName;
    private LocalDateTime publishDate;
    private BigDecimal lpr1y;
    private BigDecimal lpr5y;
    private LocalDateTime lprPublishDate;
    private Boolean stale;
    private Map<String, Object> extra;

}
