package com.wenfeng.dish;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DishService {

    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<Dish> listEnabled() {
        return dishRepository.findByEnabledTrueOrderBySortOrderAsc();
    }

    public List<Dish> listAll() {
        return dishRepository.findAllByOrderBySortOrderAsc();
    }

    public Dish get(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜品不存在: " + id));
    }

    public Dish save(Dish dish) {
        return dishRepository.save(dish);
    }

    public void delete(Long id) {
        dishRepository.deleteById(id);
    }
}
