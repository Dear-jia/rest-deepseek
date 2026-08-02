package com.wenfeng.admin;

import com.wenfeng.review.Review;
import com.wenfeng.review.ReviewRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    public AdminReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());
        return "admin/reviews";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirect) {
        setStatus(id, "APPROVED");
        redirect.addFlashAttribute("flash", "评价 #" + id + " 已通过，将展示在首页");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirect) {
        setStatus(id, "REJECTED");
        redirect.addFlashAttribute("flash", "评价 #" + id + " 已驳回");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        reviewRepository.deleteById(id);
        redirect.addFlashAttribute("flash", "评价 #" + id + " 已删除");
        return "redirect:/admin/reviews";
    }

    private void setStatus(Long id, String status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评价不存在: " + id));
        review.setStatus(status);
        reviewRepository.save(review);
    }
}
