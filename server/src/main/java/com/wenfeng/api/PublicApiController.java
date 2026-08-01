package com.wenfeng.api;

import com.wenfeng.dish.Dish;
import com.wenfeng.dish.DishService;
import com.wenfeng.reservation.Reservation;
import com.wenfeng.reservation.ReservationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final DishService dishService;
    private final ReservationRepository reservationRepository;

    public PublicApiController(DishService dishService, ReservationRepository reservationRepository) {
        this.dishService = dishService;
        this.reservationRepository = reservationRepository;
    }

    /** 前台首页菜品列表 */
    @GetMapping("/dishes")
    public List<DishDto> dishes() {
        return dishService.listEnabled().stream().map(DishDto::from).toList();
    }

    /** 前台预订提交 */
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createReservation(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setName(request.name().trim());
        reservation.setPhone(request.phone().trim());
        reservation.setDate(request.date());
        reservation.setTime(request.time().trim());
        reservation.setGuests(request.guests());
        reservation.setRoom(request.room() == null || request.room().isBlank() ? "大厅" : request.room().trim());
        reservation.setNote(request.note() == null ? "" : request.note().trim());
        Reservation saved = reservationRepository.save(reservation);
        return Map.of("id", saved.getId(), "status", saved.getStatus().name());
    }

    public record DishDto(Long id, String name, String nameEn, String description,
            BigDecimal price, String image, String category, String tag, boolean recommended) {

        static DishDto from(Dish d) {
            return new DishDto(d.getId(), d.getName(), d.getNameEn(), d.getDescription(),
                    d.getPrice(), d.getImage(), d.getCategory().name(), d.getTag(), d.isRecommended());
        }
    }

    public record ReservationRequest(
            @NotBlank(message = "姓名不能为空") String name,
            @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
            @NotNull(message = "日期不能为空") LocalDate date,
            @NotBlank(message = "时间不能为空") String time,
            @Min(value = 1, message = "人数至少 1 人") @Max(value = 50, message = "人数最多 50 人") int guests,
            String room,
            String note) {
    }
}
