package com.wenfeng.admin;

import com.wenfeng.reservation.Reservation;
import com.wenfeng.reservation.ReservationRepository;
import com.wenfeng.reservation.ReservationStatus;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationRepository reservationRepository;

    public AdminReservationController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReservationStatus status, Model model) {
        List<Reservation> reservations = status == null
                ? reservationRepository.findAllByOrderByCreatedAtDesc()
                : reservationRepository.findByStatusOrderByCreatedAtDesc(status);
        model.addAttribute("reservations", reservations);
        model.addAttribute("currentStatus", status);
        return "admin/reservations";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes redirect) {
        setStatus(id, ReservationStatus.CONFIRMED);
        redirect.addFlashAttribute("flash", "预订 #" + id + " 已确认");
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        setStatus(id, ReservationStatus.CANCELLED);
        redirect.addFlashAttribute("flash", "预订 #" + id + " 已取消");
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        reservationRepository.deleteById(id);
        redirect.addFlashAttribute("flash", "预订 #" + id + " 已删除");
        return "redirect:/admin/reservations";
    }

    private void setStatus(Long id, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预订不存在: " + id));
        reservation.setStatus(status);
        reservationRepository.save(reservation);
    }
}
