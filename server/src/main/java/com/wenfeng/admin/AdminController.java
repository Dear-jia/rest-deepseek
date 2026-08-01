package com.wenfeng.admin;

import com.wenfeng.dish.DishRepository;
import com.wenfeng.reservation.ReservationRepository;
import com.wenfeng.reservation.ReservationStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final DishRepository dishRepository;
    private final ReservationRepository reservationRepository;

    public AdminController(DishRepository dishRepository, ReservationRepository reservationRepository) {
        this.dishRepository = dishRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/admin/login")
    public String login(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout, Model model) {
        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);
        return "admin/login";
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        long pending = reservationRepository.countByStatus(ReservationStatus.PENDING);
        long today = reservationRepository.countByDate(LocalDate.now());
        List<com.wenfeng.reservation.Reservation> recent =
                reservationRepository.findAllByOrderByCreatedAtDesc().stream().limit(5).toList();
        model.addAttribute("dishCount", dishRepository.count());
        model.addAttribute("pendingCount", pending);
        model.addAttribute("todayCount", today);
        model.addAttribute("totalCount", reservationRepository.count());
        model.addAttribute("recent", recent);
        return "admin/dashboard";
    }
}
