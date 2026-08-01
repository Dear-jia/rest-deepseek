package com.wenfeng.dish;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByEnabledTrueOrderBySortOrderAsc();

    List<Dish> findAllByOrderBySortOrderAsc();
}
