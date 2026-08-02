package com.wenfeng.wallet;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {

    List<BalanceTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
