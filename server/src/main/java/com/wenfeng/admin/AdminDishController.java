package com.wenfeng.admin;

import com.wenfeng.dish.Dish;
import com.wenfeng.dish.DishCategory;
import com.wenfeng.dish.DishService;
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
@RequestMapping("/admin/dishes")
public class AdminDishController {

    private final DishService dishService;

    public AdminDishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("dishes", dishService.listAll());
        return "admin/dishes";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("dish", new Dish());
        model.addAttribute("categories", DishCategory.values());
        model.addAttribute("isNew", true);
        return "admin/dish-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("dish", dishService.get(id));
        model.addAttribute("categories", DishCategory.values());
        model.addAttribute("isNew", false);
        return "admin/dish-form";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam(required = false) String nameEn,
            @RequestParam(required = false) String description, @RequestParam BigDecimal price,
            @RequestParam(required = false) String image, @RequestParam String category,
            @RequestParam(required = false) String tag, @RequestParam(defaultValue = "false") boolean recommended,
            @RequestParam(defaultValue = "0") int sortOrder,
            RedirectAttributes redirect) {
        Dish dish = new Dish();
        fill(dish, name, nameEn, description, price, image, category, tag, recommended, sortOrder);
        dishService.save(dish);
        redirect.addFlashAttribute("flash", "菜品「" + dish.getName() + "」已添加");
        return "redirect:/admin/dishes";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String name,
            @RequestParam(required = false) String nameEn, @RequestParam(required = false) String description,
            @RequestParam BigDecimal price, @RequestParam(required = false) String image,
            @RequestParam String category, @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "false") boolean recommended,
            @RequestParam(defaultValue = "0") int sortOrder,
            @RequestParam(defaultValue = "true") boolean enabled,
            RedirectAttributes redirect) {
        Dish dish = dishService.get(id);
        fill(dish, name, nameEn, description, price, image, category, tag, recommended, sortOrder);
        dish.setEnabled(enabled);
        dishService.save(dish);
        redirect.addFlashAttribute("flash", "菜品「" + dish.getName() + "」已更新");
        return "redirect:/admin/dishes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Dish dish = dishService.get(id);
        dishService.delete(id);
        redirect.addFlashAttribute("flash", "菜品「" + dish.getName() + "」已删除");
        return "redirect:/admin/dishes";
    }

    private void fill(Dish dish, String name, String nameEn, String description, BigDecimal price,
            String image, String category, String tag, boolean recommended, int sortOrder) {
        dish.setName(name.trim());
        dish.setNameEn(nameEn == null || nameEn.isBlank() ? "" : nameEn.trim());
        dish.setDescription(description == null || description.isBlank() ? "" : description.trim());
        dish.setPrice(price);
        dish.setImage(image == null || image.isBlank() ? null : image.trim());
        dish.setCategory(DishCategory.valueOf(category));
        dish.setTag(tag == null || tag.isBlank() ? null : tag.trim());
        dish.setRecommended(recommended);
        dish.setSortOrder(sortOrder);
    }
}
