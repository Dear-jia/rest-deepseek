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
        if (dishRepository.count() > 0) {
            return;
        }
        dishRepository.save(dish("宫保鸡丁", "Kung Pao Chicken", "荔枝口的微辣回甜，花生酥脆，鸡丁滑嫩。",
                48, "assets/img/dish-gongbaojiding.jpg", DishCategory.HOT, "招牌", true, 1));
        dishRepository.save(dish("川味牛肉", "Szechuan Beef", "麻辣鲜香，牛肉嫩滑，大火锁住锅气。",
                58, "assets/img/dish-sichuanniurou.jpg", DishCategory.HOT, "人气", true, 2));
        dishRepository.save(dish("糖醋里脊", "Sweet & Sour Pork", "外酥里嫩，酸甜适口，大人小孩都爱。",
                46, "assets/img/dish-tangculiji.jpg", DishCategory.HOT, "必点", true, 3));
        dishRepository.save(dish("香橙鸡", "Orange Chicken", "果香清新，外皮酥脆。",
                42, "assets/img/dish-xiangchengji.jpg", DishCategory.HOT, null, false, 4));
        dishRepository.save(dish("西红柿炒蛋", "Tomato Egg Stir Fry", "家的味道，汤汁拌饭一绝。",
                28, "assets/img/dish-xihongshichaoji.jpg", DishCategory.HOT, null, false, 5));
        dishRepository.save(dish("干煸四季豆", "Stir-Fried Long Beans", "椒香干爽，素菜也下饭。",
                26, "assets/img/dish-sidou.jpg", DishCategory.HOT, null, false, 6));
        dishRepository.save(dish("虾仁炒河粉", "Shrimp Chow Fun", "镬气十足，虾仁弹牙。",
                42, "assets/img/dish-xiarenhefen.jpg", DishCategory.MAIN, "锅气", true, 7));
        dishRepository.save(dish("扬州炒饭", "Yangzhou Fried Rice", "粒粒分明，配料丰盛，一口满足。",
                32, "assets/img/dish-chaofan.jpg", DishCategory.MAIN, "主食", true, 8));
        dishRepository.save(dish("鲜虾云吞", "Shrimp Wontons", "现包现煮，汤清味鲜。",
                36, "assets/img/dish-huntun.jpg", DishCategory.MAIN, "招牌", true, 9));
        dishRepository.save(dish("主厨浓汤面", "Chef's Noodle Soup", "骨汤慢熬，配溏心蛋。",
                38, "assets/img/dish-ramen.jpg", DishCategory.MAIN, null, false, 10));
        dishRepository.save(dish("海鲜烩饭", "Seafood Rice", "鲜虾贝类，汤汁浓郁。",
                58, "assets/img/dish-haixianfan.jpg", DishCategory.MAIN, null, false, 11));
        dishRepository.save(dish("酸辣汤", "Hot & Sour Soup", "开胃醒神，料足汤浓。",
                22, "assets/img/dish-suanlatang.jpg", DishCategory.MAIN, null, false, 12));
        dishRepository.save(dish("蛋花汤", "Egg Drop Soup", "清爽解腻，现打蛋花。",
                18, "assets/img/dish-danhuatang.jpg", DishCategory.MAIN, null, false, 13));
        dishRepository.save(dish("桂花酒酿圆子", "Fermented Rice Ball Soup", "自制酒酿，桂花飘香。",
                18, null, DishCategory.DRINK, null, false, 14));
        dishRepository.save(dish("杨枝甘露", "Mango Pomelo Sago", "新鲜芒果，椰香浓郁。",
                28, null, DishCategory.DRINK, null, false, 15));
        dishRepository.save(dish("龙井茶（壶）", "Longjing Tea", "明前龙井，可续水。",
                38, null, DishCategory.DRINK, null, false, 16));
        dishRepository.save(dish("酸梅汤", "Sour Plum Drink", "古法熬制，冰镇更佳。",
                12, null, DishCategory.DRINK, null, false, 17));
        dishRepository.save(dish("鲜榨橙汁", "Fresh Orange Juice", "当季鲜果，现点现榨。",
                20, null, DishCategory.DRINK, null, false, 18));
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
