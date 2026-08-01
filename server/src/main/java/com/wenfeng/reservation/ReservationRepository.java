package com.wenfeng.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByCreatedAtDesc();

    List<Reservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status);

    long countByDate(LocalDate date);

    long countByStatus(ReservationStatus status);
}
