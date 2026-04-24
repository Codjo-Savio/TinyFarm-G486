package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.CooperativeID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CooperativeRepository extends JpaRepository<Cooperative, CooperativeID> {
    Optional<Cooperative> findByCooperativeIdUserId(Long userId);
    Optional<Cooperative> findByCooperativeIdProductId(Long productId);
    Optional<Cooperative> findByCooperativeIdUserIdAndCooperativeIdProductId(Long userId, Long productId);
    void deleteByCooperativeIdUserIdAndCooperativeIdProductId(Long userId, Long productId);
    void deleteByCooperativeIdUserId(Long userId);
}
