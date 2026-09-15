package com.example.zongshe1.modules.risk.service;

import com.example.zongshe1.common.dto.FeatureSnapshotDTO;
import com.example.zongshe1.modules.risk.feature.PublicFeatureSnapshotAssemblerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskFeatureService {

    private final PublicFeatureSnapshotAssemblerService publicFeatureSnapshotAssemblerService;

    public FeatureSnapshotDTO fetchPublicFeatureSnapshot(Long userId) {
        return publicFeatureSnapshotAssemblerService.assembleForUser(userId);
    }
}
