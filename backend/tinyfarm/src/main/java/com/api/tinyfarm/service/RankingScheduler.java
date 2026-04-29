package com.api.tinyfarm.service;

import com.api.tinyfarm.repository.RankingRepository;
import com.api.tinyfarm.repository.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RankingScheduler {
    @Autowired
    private RankingRepository repository;
    @Autowired
    private RankingService service;

    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void refreshRanking() {
        // Rankings are periodically recomputed from latest aggregated stats.
        List<Stats> stats = repository.getStats();
        service.updateRanking(stats);
    }
}
