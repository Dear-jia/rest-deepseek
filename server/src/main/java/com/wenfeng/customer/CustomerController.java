package com.wenfeng.customer;

import com.wenfeng.coupon.Coupon;
import com.wenfeng.coupon.CouponRepository;
import com.wenfeng.review.Review;
import com.wenfeng.review.ReviewRepository;
import com.wenfeng.wallet.BalanceTransaction;
import com.wenfeng.wallet.BalanceTransactionRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Random;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerController {

    private static final int DAILY_DRAW_LIMIT = 3;
    private static final int COUPON_VALID_DAYS = 30;

    private record Prize(String title, BigDecimal value, BigDecimal minSpend, int weight) {
    }

    private static final Prize[] PRIZES = {
        new Prize("5 元无门槛券", new BigDecimal("5"), BigDecimal.ZERO, 30),
        new Prize("10 元无门槛券", new BigDecimal("10"), BigDecimal.ZERO, 25),
        new Prize("满 50 减 10 券", new BigDecimal("10"), new BigDecimal("50"), 20),
        new Prize("满 100 减 30 券", new BigDecimal("30"), new BigDecimal("100"), 10),
        new Prize("谢谢参与", BigDecimal.ZERO, BigDecimal.ZERO, 15)
    };

    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final BalanceTransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    public CustomerController(CustomerRepository customerRepository, CouponRepository couponRepository,
            BalanceTransactionRepository transactionRepository, ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.transactionRepository = transactionRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/user/login")
    public String login(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registered, Model model, Authentication authentication) {
        // 已登录用户访问登录页时，直接进入用户中心
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            boolean isUser = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
            return isUser ? "redirect:/user" : "redirect:/admin";
        }
        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);
        model.addAttribute("registered", registered != null);
        return "user/login";
    }

    @GetMapping("/user/register")
    public String registerPage() {
        return "user/register";
    }

    @PostMapping("/user/register")
    public String register(@RequestParam String username, @RequestParam String password,
            @RequestParam String confirmPassword, @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String phone, Model model) {
        if (username == null || username.trim().length() < 3) {
            model.addAttribute("error", "用户名至少 3 个字符");
            return "user/register";
        }
        if (password == null || password.length() < 6) {
            model.addAttribute("error", "密码至少 6 位");
            return "user/register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "user/register";
        }
        if (customerRepository.existsByUsername(username.trim())) {
            model.addAttribute("error", "该用户名已被注册");
            return "user/register";
        }
        Customer customer = new Customer();
        customer.setUsername(username.trim());
        customer.setPassword(passwordEncoder.encode(password));
        customer.setNickname(nickname == null || nickname.isBlank() ? username.trim() : nickname.trim());
        customer.setPhone(phone == null ? "" : phone.trim());
        customerRepository.save(customer);
        return "redirect:/user/login?registered";
    }

    @GetMapping("/user")
    public String center(Principal principal, Model model) {
        Customer customer = current(principal);
        model.addAttribute("customer", customer);
        model.addAttribute("coupons", couponRepository.findByUserIdOrderByCreatedAtDesc(customer.getId()));
        model.addAttribute("transactions", transactionRepository.findByUserIdOrderByCreatedAtDesc(customer.getId()));
        model.addAttribute("myReviews", reviewRepository.findByUserIdOrderByCreatedAtDesc(customer.getId()));
        model.addAttribute("drawsLeft", drawLimit(customer));
        return "user/center";
    }

    @PostMapping("/user/recharge")
    public String recharge(@RequestParam BigDecimal amount, Principal principal, RedirectAttributes redirect) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal("10000")) > 0) {
            redirect.addFlashAttribute("error", "请输入 0.01–10000 之间的充值金额");
            return "redirect:/user";
        }
        Customer customer = current(principal);
        customer.setBalance(customer.getBalance().add(amount));
        customerRepository.save(customer);
        addTransaction(customer.getId(), amount, "RECHARGE", "在线充值（演示，无真实支付）");
        redirect.addFlashAttribute("flash", "充值成功，已到账 ¥" + amount);
        return "redirect:/user";
    }

    @PostMapping("/user/draw")
    public String draw(Principal principal, RedirectAttributes redirect) {
        Customer customer = current(principal);
        if (drawLimit(customer) <= 0) {
            redirect.addFlashAttribute("error", "今天的抽奖次数已用完，明天再来吧");
            return "redirect:/user";
        }
        if (customer.getLastDrawDate() == null || !customer.getLastDrawDate().equals(LocalDate.now())) {
            customer.setLastDrawDate(LocalDate.now());
            customer.setDrawsToday(0);
        }
        customer.setDrawsToday(customer.getDrawsToday() + 1);
        Prize prize = drawPrize();
        if (prize.value().compareTo(BigDecimal.ZERO) > 0) {
            Coupon coupon = new Coupon();
            coupon.setUserId(customer.getId());
            coupon.setTitle(prize.title());
            coupon.setValue(prize.value());
            coupon.setMinSpend(prize.minSpend());
            coupon.setValidUntil(LocalDate.now().plusDays(COUPON_VALID_DAYS));
            couponRepository.save(coupon);
            redirect.addFlashAttribute("flash", "🎉 恭喜抽中「" + prize.title() + "」！已放入我的优惠券");
        } else {
            redirect.addFlashAttribute("flash", "很遗憾，这次是「谢谢参与」，再接再厉！");
        }
        customerRepository.save(customer);
        return "redirect:/user";
    }

    @PostMapping("/user/review")
    public String submitReview(@RequestParam int rating, @RequestParam String content,
            Principal principal, RedirectAttributes redirect) {
        Customer customer = current(principal);
        if (rating < 1 || rating > 5) {
            redirect.addFlashAttribute("error", "评分需要在 1–5 星之间");
            return "redirect:/user";
        }
        if (content == null || content.trim().length() < 5) {
            redirect.addFlashAttribute("error", "评价内容至少 5 个字");
            return "redirect:/user";
        }
        Review review = new Review();
        review.setUserId(customer.getId());
        review.setNickname(customer.getNickname() == null || customer.getNickname().isBlank()
                ? customer.getUsername() : customer.getNickname());
        review.setRating(rating);
        review.setContent(content.trim());
        reviewRepository.save(review);
        redirect.addFlashAttribute("flash", "评价已提交，审核通过后会展示在首页");
        return "redirect:/user";
    }

    private Customer current(Principal principal) {
        return customerRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("用户不存在"));
    }

    private int drawLimit(Customer customer) {
        if (customer.getLastDrawDate() == null || !customer.getLastDrawDate().equals(LocalDate.now())) {
            return DAILY_DRAW_LIMIT;
        }
        return Math.max(0, DAILY_DRAW_LIMIT - customer.getDrawsToday());
    }

    private Prize drawPrize() {
        int total = 0;
        for (Prize p : PRIZES) {
            total += p.weight();
        }
        int roll = random.nextInt(total) + 1;
        int acc = 0;
        for (Prize p : PRIZES) {
            acc += p.weight();
            if (roll <= acc) {
                return p;
            }
        }
        return PRIZES[PRIZES.length - 1];
    }

    private void addTransaction(Long userId, BigDecimal amount, String type, String note) {
        BalanceTransaction tx = new BalanceTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setNote(note);
        transactionRepository.save(tx);
    }
}
