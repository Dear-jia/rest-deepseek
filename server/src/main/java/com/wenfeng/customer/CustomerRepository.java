package com.wenfeng.customer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUsername(String username);

    List<Customer> findAllByOrderByCreatedAtDesc();

    boolean existsByUsername(String username);
}
