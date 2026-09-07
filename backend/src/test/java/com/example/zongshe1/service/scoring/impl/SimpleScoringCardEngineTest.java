package com.example.zongshe1.service.scoring.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.modules.risk.feature.CurrentRiskFeatures;
import com.example.zongshe1.modules.risk.feature.FeatureSnapshot;
import com.example.zongshe1.service.scoring.ScoringCardOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleScoringCardEngineTest {

    @Mock
    private CurrentRiskFeatures currentRiskFeatures;

    private SimpleScoringCardEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SimpleScoringCardEngine(currentRiskFeatures);
        ReflectionTestUtils.setField(engine, "cardVersion", "test-v1");
    }

    @Test
    void evaluate_highCreditAndLowAmount_shouldPassThreshold() {
        User user = new User();
        user.setCreditScore(720);
        LoanApplication app = new LoanApplication();
        app.setLoanAmount(new BigDecimal("30000"));

        when(currentRiskFeatures.isReady()).thenReturn(false);

        ScoringCardOutcome outcome = engine.evaluate(user, app);

        assertTrue(outcome.totalPoints() >= 40);
        assertEquals(100, outcome.maxPoints());
        assertEquals("test-v1", outcome.cardVersion());
    }

    @Test
    void evaluate_withExternalFeatures_shouldIncludeOverdueAndTelecomPoints() {
        User user = new User();
        user.setCreditScore(550);
        LoanApplication app = new LoanApplication();
        app.setLoanAmount(new BigDecimal("40000"));

        FeatureSnapshot snapshot = new FeatureSnapshot();
        snapshot.put("credit_score", 550);
        snapshot.put("loan_amount", new BigDecimal("40000"));
        snapshot.put("credit_overdue_count", 0);
        snapshot.put("telecom_online_months", 24);

        when(currentRiskFeatures.isReady()).thenReturn(true);
        when(currentRiskFeatures.get()).thenReturn(snapshot);

        ScoringCardOutcome outcome = engine.evaluate(user, app);

        assertEquals(4, outcome.breakdown().size());
        assertTrue(outcome.totalPoints() > 50);
    }
}
