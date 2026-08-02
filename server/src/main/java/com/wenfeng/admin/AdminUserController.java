package com.wenfeng.admin;

import com.wenfeng.customer.Customer;
import com.wenfeng.customer.CustomerRepository;
import com.wenfeng.wallet.BalanceTransaction;
import com.wenfeng.wallet.BalanceTransactionRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final CustomerRepository customerRepository;
    private final BalanceTransactionRepository transactionRepository;

    public AdminUserController(CustomerRepository customerRepository,
            BalanceTransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerRepository.findAllByOrderByCreatedAtDesc());
        return "admin/users";
    }

    @PostMapping("/{id}/balance")
    public String adjustBalance(@PathVariable Long id, @RequestParam BigDecimal amount,
            @RequestParam(required = false) String note, RedirectAttributes redirect) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            redirect.addFlashAttribute("error", "调整金额不能为 0");
            return "redirect:/admin/users";
        }
        BigDecimal newBalance = customer.getBalance().add(amount).max(BigDecimal.ZERO);
        customer.setBalance(newBalance);
        customerRepository.save(customer);

        BalanceTransaction tx = new BalanceTransaction();
        tx.setUserId(customer.getId());
        tx.setAmount(amount);
        tx.setType("ADMIN_ADJUST");
        tx.setNote(note == null || note.isBlank() ? "管理员调整余额" : note.trim());
        transactionRepository.save(tx);

        redirect.addFlashAttribute("flash", "已调整用户「" + customer.getNickname() + "」余额："
                + (amount.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + amount + "，当前余额 ¥" + newBalance);
        return "redirect:/admin/users";
    }
}
