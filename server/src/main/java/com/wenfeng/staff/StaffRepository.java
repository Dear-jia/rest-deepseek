package com.wenfeng.staff;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findByEnabledTrueOrderBySortOrderAsc();

    List<Staff> findAllByOrderBySortOrderAsc();

    Optional<Staff> findByImage(String image);
}
