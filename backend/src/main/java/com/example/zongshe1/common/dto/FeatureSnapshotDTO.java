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

    // 第1周已有字段
    private Boolean dishonestHit;
    private String dishonestName;
    private String courtName;
    private LocalDateTime publishDate;
    private BigDecimal lpr1y;
    private BigDecimal lpr5y;
    private LocalDateTime lprPublishDate;
    private Boolean stale;

    // 第2周补充字段：用于真实抓取结果回填，保持兼容
    private String caseNumber;
    private String sourceUrl;
    private Map<String, Object> extra;

}
