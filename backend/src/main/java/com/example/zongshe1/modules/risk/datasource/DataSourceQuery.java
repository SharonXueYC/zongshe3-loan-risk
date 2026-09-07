package com.example.zongshe1.modules.risk.datasource;

/**
 * 拉取外部数据时的统一查询上下文（按需脱敏后传给各适配器）。
 */
public record DataSourceQuery(
        Long applicationId,
        String userId,
        String phoneNumber,
        String idCardNumber
) {
}
