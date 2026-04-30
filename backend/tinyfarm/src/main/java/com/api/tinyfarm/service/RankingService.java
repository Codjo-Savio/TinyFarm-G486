package com.api.tinyfarm.service;

import com.api.tinyfarm.dto.FarmerRankingRequest;
import com.api.tinyfarm.repository.RankingRepository;
import com.api.tinyfarm.repository.Stats;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {
    @Autowired
    private RankingRepository rankingRepository;
    private List<FarmerRankingRequest> cachedRanking = new ArrayList<>();

    public List<FarmerRankingRequest> getRanking() {
        return cachedRanking;
    }

    public void updateRanking(List<Stats> stats) {
        this.cachedRanking = computeRanking(stats);
    }

    // Initialize ranking cache before periodic recomputation starts.
    @PostConstruct
    public void init() {
        // Ranking cache is warm-started at boot to avoid an empty first response.
        try {
            refreshNow();
        } catch (Exception e) {
            System.err.println("Erreur init ranking: " + e.getMessage());
        }
    }

    public void refreshNow() {
        List<Stats> stats = rankingRepository.getStats();
        this.cachedRanking = computeRanking(stats);
    }

    // Compute ranking from aggregated stats.
    public List<FarmerRankingRequest> computeRanking(List<Stats> stats){
        // Composite score weights: production 50%, capacity 20%, ecus 30%.
        if (stats.isEmpty()){
            return List.of();
        }
        double totalProduction = stats.stream()
                .mapToDouble(s -> safe(s.getProduction()))
                .sum();

        double totalCapacity = stats.stream()
                .mapToDouble(s -> safe(s.getCapacity()))
                .sum();

        double totalEcus = stats.stream()
                .mapToDouble(s -> safe(s.getEcus()))
                .sum();

        List<FarmerRankingRequest> ranking = new java.util.ArrayList<>(stats.stream()
                .map(s -> {

                    double productionScore = safeRatio(s.getProduction(), totalProduction) * 50;
                    double capacityScore = safeRatio(s.getCapacity(), totalCapacity) * 20;
                    double ecusScore = safeRatio(s.getEcus(), totalEcus) * 30;

                    double score = productionScore + capacityScore + ecusScore;

                    FarmerRankingRequest dto = new FarmerRankingRequest();
                    dto.setUid(s.getUid());
                    dto.setName(s.getName());
                    dto.setProduction(s.getProduction());
                    dto.setCapacity(s.getCapacity());
                    dto.setEcus(s.getEcus());
                    dto.setScore(score);

                    return dto;
                })
                .toList());

        ranking.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        int rank = 1;
        for (int i = 0; i < ranking.size(); i++) {
            if (i > 0 && ranking.get(i).getScore() < ranking.get(i - 1).getScore()) {
                rank = i + 1;
            }
            ranking.get(i).setRank(rank);
        }

        return ranking;
    }

    private double safe(Number n) {
        return n == null ? 0 : n.doubleValue();
    }

    private double safeRatio(Number value, double total) {
        // Defensive ratio: null values or zero denominator contribute zero score.
        if (value == null || total == 0) return 0;
        return value.doubleValue() / total;
    }
}
