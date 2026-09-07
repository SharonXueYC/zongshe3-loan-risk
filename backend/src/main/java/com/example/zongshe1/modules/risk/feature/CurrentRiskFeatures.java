package com.example.zongshe1.modules.risk.feature;

import org.springframework.stereotype.Component;

/**
 * 当前线程内的特征快照（一次 performRiskAssessment 内有效）。
 */
@Component
public class CurrentRiskFeatures {

    private static final ThreadLocal<FeatureSnapshot> HOLDER = new ThreadLocal<>();

    public void set(FeatureSnapshot snapshot) {
        HOLDER.set(snapshot);
    }

    public FeatureSnapshot get() {
        return HOLDER.get();
    }

    public boolean isReady() {
        return HOLDER.get() != null;
    }

    public void clear() {
        HOLDER.remove();
    }
}
