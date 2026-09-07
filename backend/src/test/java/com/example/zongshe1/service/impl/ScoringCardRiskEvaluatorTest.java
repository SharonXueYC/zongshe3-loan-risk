package com.example.zongshe1.service.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.service.scoring.ScoringCardEngine;
import com.example.zongshe1.service.scoring.ScoringCardOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoringCardRiskEvaluatorTest {

    @Mock
    private ScoringCardEngine scoringCardEngine;

    private ScoringCardRiskEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ScoringCardRiskEvaluator(scoringCardEngine);
        ReflectionTestUtils.setField(evaluator, "enabled", true);
        ReflectionTestUtils.setField(evaluator, "passMinPoints", 40);
    }

    @Test
    void evaluateRisk_shouldPassWhenPointsAboveThreshold() {
        when(scoringCardEngine.evaluate(any(), any())).thenReturn(
                new ScoringCardOutcome(65, 100, "demo-v1", List.of())
        );

        Map<String, Object> result = evaluator.evaluateRisk(new User(), new LoanApplication());

        assertTrue((Boolean) result.get("passed"));
        assertEquals(35, result.get("riskScore"));
    }

    @Test
    void evaluateRisk_shouldRejectWhenPointsBelowThreshold() {
        when(scoringCardEngine.evaluate(any(), any())).thenReturn(
                new ScoringCardOutcome(25, 100, "demo-v1", List.of())
        );

        Map<String, Object> result = evaluator.evaluateRisk(new User(), new LoanApplication());

        assertFalse((Boolean) result.get("passed"));
        assertTrue(((String) result.get("reason")).contains("未达阈值"));
    }
}
