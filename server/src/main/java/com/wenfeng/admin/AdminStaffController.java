package com.wenfeng.admin;

import com.wenfeng.staff.Staff;
import com.wenfeng.staff.StaffRepository;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/staff")
public class AdminStaffController {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private final StaffRepository staffRepository;

    public AdminStaffController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("staffList", staffRepository.findAllByOrderBySortOrderAsc());
        return "admin/staff";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("staff", new Staff());
        model.addAttribute("isNew", true);
        return "admin/staff-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("staff", staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在: " + id)));
        model.addAttribute("isNew", false);
        return "admin/staff-form";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam(required = false) String role,
            @RequestParam(required = false) String description, @RequestParam(required = false) MultipartFile image,
            @RequestParam(defaultValue = "0") int sortOrder, @RequestParam(defaultValue = "true") boolean enabled,
            RedirectAttributes redirect) {
        Staff staff = new Staff();
        fill(staff, name, role, description, sortOrder, enabled);
        applyImage(staff, image, redirect);
        staffRepository.save(staff);
        redirect.addFlashAttribute("flash", "已添加成员「" + staff.getName() + "」");
        return "redirect:/admin/staff";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String name,
            @RequestParam(required = false) String role, @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile image, @RequestParam(defaultValue = "0") int sortOrder,
            @RequestParam(defaultValue = "true") boolean enabled, RedirectAttributes redirect) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在: " + id));
        fill(staff, name, role, description, sortOrder, enabled);
        applyImage(staff, image, redirect);
        staffRepository.save(staff);
        redirect.addFlashAttribute("flash", "已更新成员「" + staff.getName() + "」");
        return "redirect:/admin/staff";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在: " + id));
        staffRepository.deleteById(id);
        redirect.addFlashAttribute("flash", "已删除成员「" + staff.getName() + "」");
        return "redirect:/admin/staff";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("成员不存在: " + id));
        staff.setEnabled(!staff.isEnabled());
        staffRepository.save(staff);
        redirect.addFlashAttribute("flash", "成员「" + staff.getName() + "」已" + (staff.isEnabled() ? "上架" : "下架"));
        return "redirect:/admin/staff";
    }

    private void fill(Staff staff, String name, String role, String description, int sortOrder, boolean enabled) {
        staff.setName(name.trim());
        staff.setRole(role == null || role.isBlank() ? "" : role.trim());
        staff.setDescription(description == null || description.isBlank() ? "" : description.trim());
        staff.setSortOrder(sortOrder);
        staff.setEnabled(enabled);
    }

    /** 上传图片存入云数据库；未选择文件时保持原样 */
    private void applyImage(Staff staff, MultipartFile file, RedirectAttributes redirect) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String original = file.getOriginalFilename();
        String ext = original == null ? "" : original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            redirect.addFlashAttribute("error", "仅支持 jpg/png/webp/gif 图片");
            return;
        }
        if (file.getSize() > MAX_SIZE) {
            redirect.addFlashAttribute("error", "图片不能超过 5MB");
            return;
        }
        try {
            staff.setImageData(file.getBytes());
            staff.setImageType("image/" + ("jpg".equals(ext) ? "jpeg" : ext));
            staff.setImage("/uploads/staff-" + UUID.randomUUID() + "." + ext);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "图片保存失败，请稍后重试");
        }
    }
}
