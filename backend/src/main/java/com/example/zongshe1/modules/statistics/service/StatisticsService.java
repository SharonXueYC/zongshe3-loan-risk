package com.example.zongshe1.service;

import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getDashboardStatistics();

    Map<String, Object> getApplicationTrend();

    Map<String, Object> getProductSales();

    Map<String, Object> getLoanStatusDistribution();

    Map<String, Object> getChartData();

    java.util.List<java.util.Map<String, Object>> getRecentLoans(int limit);
}