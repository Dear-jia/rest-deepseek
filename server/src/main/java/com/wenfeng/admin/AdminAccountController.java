package com.wenfeng.admin;

import com.wenfeng.user.User;
import com.wenfeng.user.UserRepository;
import java.security.Principal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/account")
public class AdminAccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String accountPage() {
        return "admin/account";
    }

    @PostMapping
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
            @RequestParam String confirmPassword, Principal principal, Model model,
            RedirectAttributes redirect) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("当前用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("error", "原密码不正确");
            return "admin/account";
        }
        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("error", "新密码至少需要 6 位");
            return "admin/account";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的新密码不一致");
            return "admin/account";
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            model.addAttribute("error", "新密码不能与原密码相同");
            return "admin/account";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirect.addFlashAttribute("flash", "密码已更新，下次登录请使用新密码");
        return "redirect:/admin/account";
    }
}
