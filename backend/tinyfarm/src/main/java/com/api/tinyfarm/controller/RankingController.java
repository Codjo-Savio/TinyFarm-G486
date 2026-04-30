package com.api.tinyfarm.controller;

import com.api.tinyfarm.dto.FarmerRankingRequest;
import com.api.tinyfarm.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    @Autowired
    private RankingService rankingService;

    @GetMapping("")
    public ResponseEntity<List<FarmerRankingRequest>> getRanking() {
        try {
            return ResponseEntity.ok(rankingService.getRanking());
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshRanking() {
        try {
            rankingService.refreshNow();
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

}
