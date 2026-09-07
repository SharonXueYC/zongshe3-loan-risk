package com.example.zongshe1.modules.risk.datasource;

/**
 * 外部数据源适配器：每个渠道一个实现，输出统一特征 Map。
 */
public interface ExternalDataSourceAdapter {

    DataSourceType getSourceType();

    boolean isEnabled();

    /** 是否关键源：失败时是否应阻断整单（由编排层结合配置决定） */
    default boolean isRequired() {
        return false;
    }

    long getTimeoutMs();

    ExternalDataFetchResult fetch(DataSourceQuery query);
}
