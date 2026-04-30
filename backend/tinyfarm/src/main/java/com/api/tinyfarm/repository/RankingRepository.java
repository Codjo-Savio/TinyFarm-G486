package com.api.tinyfarm.repository;

import com.api.tinyfarm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<User, Long> {
    @Query(value = """
        SELECT 
            u.uid AS uid,
            u.name AS name, 
            COALESCE(SUM(t.quantity), 0) AS production,
            COUNT(DISTINCT a.aid) AS capacity,
            u.ecus AS ecus
         FROM "user" u
         LEFT JOIN transaction t ON t.seller = u.uid
         LEFT JOIN animal a ON a.uid = u.uid
         
         GROUP BY u.uid, u.name, u.ecus
    """, nativeQuery = true)
    List<Stats> getStats();
}
