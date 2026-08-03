package com.wenfeng.config;

import com.wenfeng.dish.Dish;
import com.wenfeng.dish.DishCategory;
import com.wenfeng.dish.DishRepository;
import com.wenfeng.user.User;
import com.wenfeng.user.UserRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String configuredAdminPassword;

    public DataInitializer(UserRepository userRepository, DishRepository dishRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initAdmin();
        initDishes();
    }

    private void initAdmin() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername(adminUsername);
            boolean explicitlyConfigured = configuredAdminPassword != null
                    && !configuredAdminPassword.isBlank()
                    && !"admin123".equals(configuredAdminPassword);
            String password = explicitlyConfigured ? configuredAdminPassword : randomPassword();
            if (!explicitlyConfigured) {
                log.warn("════════════════════════════════════════════════════════════");
                log.warn("未配置 ADMIN_PASSWORD，已生成随机管理员初始密码（请立即登录修改）：{}", password);
                log.warn("════════════════════════════════════════════════════════════");
            }
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole("ROLE_ADMIN");
            admin.setMustChangePassword(!explicitlyConfigured);
            userRepository.save(admin);
        }
        // 兼容老库：若管理员密码仍是公开默认值 admin123，强制其下次登录修改
        userRepository.findByUsername(adminUsername).ifPresent(admin -> {
            if (passwordEncoder.matches("admin123", admin.getPassword())) {
                admin.setMustChangePassword(true);
                userRepository.save(admin);
            }
        });
    }

    private String randomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void initDishes() {
        if (dishRepository.count() > 0 && !isOldMenu()) {
            return;
        }
        if (dishRepository.count() > 0) {
            log.warn("检测到旧菜单（家常菜），正在替换为樱梦女仆咖啡厅菜单…");
            dishRepository.deleteAll();
        }
        seedMenu();
    }

    private boolean isOldMenu() {
        return dishRepository.findAll().stream()
                .anyMatch(d -> "宫保鸡丁".equals(d.getName()));
    }

    private void seedMenu() {
        dishRepository.save(dish("女仆特制蛋包饭", "Maid Omelette Rice", "松软蛋皮裹着香糯炒饭，番茄酱画出爱心～",
                38, "assets/img/dish-omelette.jpg", DishCategory.HOT, "招牌", true, 1));
        dishRepository.save(dish("草莓松饼", "Strawberry Pancakes", "现烤松饼叠草莓与奶油，甜度刚刚好。",
                32, "assets/img/dish-pancake.jpg", DishCategory.HOT, "人气", true, 2));
        dishRepository.save(dish("奶油培根意面", "Spaghetti Carbonara", "浓郁奶油裹着培根与蛋香，一口满足。",
                36, "assets/img/dish-carbonara.jpg", DishCategory.HOT, null, true, 3));
        dishRepository.save(dish("泰式绿咖喱饭", "Thai Green Curry", "椰香微辣的绿咖喱，配热米饭正合适。",
                34, "assets/img/dish-curry.jpg", DishCategory.HOT, null, true, 4));
        dishRepository.save(dish("纽约芝士蛋糕", "New York Cheesecake", "绵密芝士配酥脆饼底，下午茶首选。",
                28, "assets/img/dish-cheesecake.jpg", DishCategory.HOT, "甜品", true, 5));
        dishRepository.save(dish("巧克力布朗尼", "Chocolate Brownies", "外脆内软的布朗尼，树莓点缀酸甜解腻。",
                26, "assets/img/dish-brownie.jpg", DishCategory.HOT, null, true, 6));
        dishRepository.save(dish("番茄肉酱意面", "Spaghetti Bolognese", "慢炖番茄肉酱，经典好味道。",
                30, "assets/img/dish-bolognese.jpg", DishCategory.MAIN, null, false, 7));
        dishRepository.save(dish("姜饼华夫饼", "Gingerbread Waffles", "外酥里软的华夫，淋上枫糖浆。",
                26, "assets/img/dish-waffle.jpg", DishCategory.MAIN, null, false, 8));
        dishRepository.save(dish("树莓慕斯", "Raspberry Mousse", "轻盈树莓慕斯，酸甜绵密入口即化。",
                22, "assets/img/dish-mousse.jpg", DishCategory.MAIN, null, false, 9));
        dishRepository.save(dish("苹果蛋糕", "Apple Cake", "肉桂苹果的温暖香气，配红茶刚好。",
                24, "assets/img/dish-apple-cake.jpg", DishCategory.MAIN, null, false, 10));
        dishRepository.save(dish("草莓塔", "Strawberry Tart", "酥脆塔壳配新鲜草莓与卡仕达酱。",
                26, "assets/img/dish-strawberry-tart.jpg", DishCategory.MAIN, null, false, 11));
        dishRepository.save(dish("咖啡冰淇淋", "Espresso Ice Cream", "浓缩咖啡遇上香草冰淇淋，冷热交融。",
                25, "assets/img/dish-icecream.jpg", DishCategory.MAIN, null, false, 12));
        dishRepository.save(dish("女仆拿铁", "Maid Latte", "拉花里藏着爱心，杯边还有小猫爪。",
                22, null, DishCategory.DRINK, "招牌", false, 13));
        dishRepository.save(dish("抹茶拿铁", "Matcha Latte", "宇治抹茶与醇厚牛奶的温柔相遇。",
                24, null, DishCategory.DRINK, null, false, 14));
        dishRepository.save(dish("草莓奶昔", "Strawberry Milkshake", "新鲜草莓打成绵密奶昔，少女心满分。",
                26, null, DishCategory.DRINK, null, false, 15));
        dishRepository.save(dish("珍珠奶茶", "Bubble Tea", "Q 弹珍珠配经典奶茶，快乐加倍。",
                18, null, DishCategory.DRINK, null, false, 16));
        dishRepository.save(dish("蜜桃气泡水", "Peach Soda", "蜜桃香气在气泡里跳舞。",
                20, null, DishCategory.DRINK, null, false, 17));
        dishRepository.save(dish("樱花苏打", "Sakura Soda", "淡粉色樱花风味苏打，颜值担当。",
                22, null, DishCategory.DRINK, null, false, 18));
    }

    private Dish dish(String name, String nameEn, String description, int price, String image,
            DishCategory category, String tag, boolean recommended, int sortOrder) {
        Dish d = new Dish();
        d.setName(name);
        d.setNameEn(nameEn);
        d.setDescription(description);
        d.setPrice(new BigDecimal(price));
        d.setImage(image);
        d.setCategory(category);
        d.setTag(tag);
        d.setRecommended(recommended);
        d.setSortOrder(sortOrder);
        return d;
    }
}
