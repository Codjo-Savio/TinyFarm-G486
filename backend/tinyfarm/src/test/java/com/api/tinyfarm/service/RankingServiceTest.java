package com.api.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.api.tinyfarm.dto.FarmerRankingRequest;
import com.api.tinyfarm.repository.RankingRepository;
import com.api.tinyfarm.repository.Stats;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RankingRepository rankingRepository;

    @InjectMocks
    private RankingService rankingService;

    @Test
    void shouldReturnEmptyRankingWhenStatsAreEmpty() {
        List<FarmerRankingRequest> ranking = rankingService.computeRanking(List.of());
        assertTrue(ranking.isEmpty());
    }

    @Test
    void shouldComputeScoresSortAndRankFarmers() {
        Stats alice = stats(1L, "Alice", 100d, 10d, 100d);
        Stats bob = stats(2L, "Bob", 50d, 20d, 100d);

        List<FarmerRankingRequest> ranking = rankingService.computeRanking(List.of(alice, bob));

        assertEquals(2, ranking.size());

        FarmerRankingRequest first = ranking.get(0);
        FarmerRankingRequest second = ranking.get(1);

        assertEquals("Alice", first.getName());
        assertEquals(1, first.getRank());
        assertEquals(55.0, first.getScore(), 0.0001);

        assertEquals("Bob", second.getName());
        assertEquals(2, second.getRank());
        assertEquals(45.0, second.getScore(), 0.0001);
    }

    @Test
    void shouldAssignSameRankForTieAndSkipNextRank() {
        Stats alice = stats(1L, "Alice", 100d, 10d, 100d);
        Stats bob = stats(2L, "Bob", 100d, 10d, 100d);
        Stats charlie = stats(3L, "Charlie", 0d, 0d, 0d);

        List<FarmerRankingRequest> ranking = rankingService.computeRanking(List.of(alice, bob, charlie));

        assertEquals(3, ranking.size());
        assertEquals(1, ranking.get(0).getRank());
        assertEquals(1, ranking.get(1).getRank());
        assertEquals(3, ranking.get(2).getRank());
    }

    @Test
    void shouldHandleNullValuesAsZero() {
        Stats empty = stats(1L, "NoData", null, null, null);
        Stats producer = stats(2L, "Producer", 10d, 5d, 100d);

        List<FarmerRankingRequest> ranking = rankingService.computeRanking(List.of(empty, producer));

        FarmerRankingRequest last = ranking.get(1);
        assertEquals("NoData", last.getName());
        assertEquals(0.0, last.getScore(), 0.0001);
    }

    @Test
    void shouldExcludeCooperativeFromRankingAndTotals() {
        Stats alice = stats(1L, "Alice", 100d, 10d, 100d);
        Stats cooperative = stats(2L, "Cooperative", 900d, 900d, 900d);

        List<FarmerRankingRequest> ranking = rankingService.computeRanking(List.of(alice, cooperative));

        assertEquals(1, ranking.size());
        assertEquals("Alice", ranking.get(0).getName());
        assertEquals(1, ranking.get(0).getRank());
        assertEquals(100.0, ranking.get(0).getScore(), 0.0001);
    }

    @Test
    void refreshNowShouldUpdateCachedRankingFromRepository() {
        Stats alice = stats(1L, "Alice", 100d, 10d, 100d);
        when(rankingRepository.getStats()).thenReturn(List.of(alice));

        rankingService.refreshNow();
        List<FarmerRankingRequest> ranking = rankingService.getRanking();

        assertEquals(1, ranking.size());
        assertEquals("Alice", ranking.get(0).getName());
        assertEquals(1, ranking.get(0).getRank());
    }

    private Stats stats(Long uid, String name, Double production, Double capacity, Double ecus) {
        return new Stats() {
            @Override
            public Long getUid() {
                return uid;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Double getProduction() {
                return production;
            }

            @Override
            public Double getCapacity() {
                return capacity;
            }

            @Override
            public Double getEcus() {
                return ecus;
            }
        };
    }
}
