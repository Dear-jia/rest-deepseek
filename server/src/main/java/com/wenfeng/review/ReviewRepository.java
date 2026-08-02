package com.wenfeng.review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByStatusOrderByCreatedAtDesc(String status);
}
