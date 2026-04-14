package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.Cooperative;
import com.api.tinyfarm.model.CooperativeID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CooperativeRepository extends JpaRepository<Cooperative, CooperativeID> {
    Optional<Cooperative> findByUserId(Long userId);
    Optional<Cooperative> findByProductId(Long productId);
    Optional<Cooperative> findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserId(Long userId);
}
