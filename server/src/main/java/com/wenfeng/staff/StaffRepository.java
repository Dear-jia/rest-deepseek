package com.wenfeng.staff;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findByEnabledTrueOrderBySortOrderAsc();

    List<Staff> findAllByOrderBySortOrderAsc();
}
