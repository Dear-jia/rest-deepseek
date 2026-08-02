/* ============================================================
   文峰小馆 · 餐厅主页交互脚本
   - 菜单由后端 /api/dishes 动态加载，接口不可用时回退到内置数据
   - 预订提交到 /api/reservations，不可用时降级为演示提示
   ============================================================ */

(function () {
  "use strict";

  const header = document.getElementById("header");
  const navToggle = document.querySelector(".nav-toggle");
  const toTop = document.getElementById("to-top");
  const toast = document.getElementById("toast");
  const yearEl = document.getElementById("year");

  /* ---------- 内置菜单数据（后端不可用时的兜底） ---------- */
  const DEFAULT_DISHES = [
    { name: "宫保鸡丁", nameEn: "Kung Pao Chicken", description: "荔枝口的微辣回甜，花生酥脆，鸡丁滑嫩。", price: 48, image: "assets/img/dish-gongbaojiding.jpg", category: "HOT", tag: "招牌", recommended: true },
    { name: "川味牛肉", nameEn: "Szechuan Beef", description: "麻辣鲜香，牛肉嫩滑，大火锁住锅气。", price: 58, image: "assets/img/dish-sichuanniurou.jpg", category: "HOT", tag: "人气", recommended: true },
    { name: "糖醋里脊", nameEn: "Sweet & Sour Pork", description: "外酥里嫩，酸甜适口，大人小孩都爱。", price: 46, image: "assets/img/dish-tangculiji.jpg", category: "HOT", tag: "必点", recommended: true },
    { name: "香橙鸡", nameEn: "Orange Chicken", description: "果香清新，外皮酥脆。", price: 42, image: "assets/img/dish-xiangchengji.jpg", category: "HOT", tag: null, recommended: false },
    { name: "西红柿炒蛋", nameEn: "Tomato Egg Stir Fry", description: "家的味道，汤汁拌饭一绝。", price: 28, image: "assets/img/dish-xihongshichaoji.jpg", category: "HOT", tag: null, recommended: false },
    { name: "干煸四季豆", nameEn: "Stir-Fried Long Beans", description: "椒香干爽，素菜也下饭。", price: 26, image: "assets/img/dish-sidou.jpg", category: "HOT", tag: null, recommended: false },
    { name: "虾仁炒河粉", nameEn: "Shrimp Chow Fun", description: "镬气十足，虾仁弹牙。", price: 42, image: "assets/img/dish-xiarenhefen.jpg", category: "MAIN", tag: "锅气", recommended: true },
    { name: "扬州炒饭", nameEn: "Yangzhou Fried Rice", description: "粒粒分明，配料丰盛，一口满足。", price: 32, image: "assets/img/dish-chaofan.jpg", category: "MAIN", tag: "主食", recommended: true },
    { name: "鲜虾云吞", nameEn: "Shrimp Wontons", description: "现包现煮，汤清味鲜。", price: 36, image: "assets/img/dish-huntun.jpg", category: "MAIN", tag: "招牌", recommended: true },
    { name: "主厨浓汤面", nameEn: "Chef's Noodle Soup", description: "骨汤慢熬，配溏心蛋。", price: 38, image: "assets/img/dish-ramen.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "海鲜烩饭", nameEn: "Seafood Rice", description: "鲜虾贝类，汤汁浓郁。", price: 58, image: "assets/img/dish-haixianfan.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "酸辣汤", nameEn: "Hot & Sour Soup", description: "开胃醒神，料足汤浓。", price: 22, image: "assets/img/dish-suanlatang.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "蛋花汤", nameEn: "Egg Drop Soup", description: "清爽解腻，现打蛋花。", price: 18, image: "assets/img/dish-danhuatang.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "桂花酒酿圆子", nameEn: "Fermented Rice Ball Soup", description: "自制酒酿，桂花飘香。", price: 18, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "杨枝甘露", nameEn: "Mango Pomelo Sago", description: "新鲜芒果，椰香浓郁。", price: 28, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "龙井茶（壶）", nameEn: "Longjing Tea", description: "明前龙井，可续水。", price: 38, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "酸梅汤", nameEn: "Sour Plum Drink", description: "古法熬制，冰镇更佳。", price: 12, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "鲜榨橙汁", nameEn: "Fresh Orange Juice", description: "当季鲜果，现点现榨。", price: 20, image: null, category: "DRINK", tag: null, recommended: false }
  ];

  /* ---------- 菜单渲染 ---------- */
  function esc(s) {
    return String(s === null || s === undefined ? "" : s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function renderSignature(dishes) {
    const grid = document.getElementById("signature-grid");
    if (!grid) return;
    const items = dishes.filter(function (d) { return d.recommended; });
    grid.innerHTML = items.map(function (d, i) {
      return (
        '<article class="dish-card reveal in">' +
          '<div class="dish-media">' +
            (d.image ? '<img src="' + esc(d.image) + '" alt="' + esc(d.name) + '" loading="lazy">' : "") +
            '<span class="dish-tag">' + esc(d.tag || "招牌") + "</span>" +
          "</div>" +
          '<div class="dish-body">' +
            "<h3>" + esc(d.name) + "</h3>" +
            '<p class="dish-en">' + esc(d.nameEn) + "</p>" +
            '<p class="dish-desc">' + esc(d.description) + "</p>" +
            '<div class="dish-foot">' +
              '<span class="price">¥' + d.price + "</span>" +
              '<a class="btn btn--mini" href="#reserve">预订</a>' +
            "</div>" +
          "</div>" +
        "</article>"
      );
    }).join("");
  }

  function renderMenu(dishes) {
    const targets = { HOT: "panel-hot", MAIN: "panel-main", DRINK: "panel-drink" };
    Object.keys(targets).forEach(function (cat) {
      const panel = document.getElementById(targets[cat]);
      if (!panel) return;
      const items = dishes.filter(function (d) { return d.category === cat; });
      panel.innerHTML = items.map(function (d) {
        return (
          '<div class="menu-row reveal in">' +
            (d.image ? '<img class="menu-thumb" src="' + esc(d.image) + '" alt="' + esc(d.name) + '" loading="lazy">' : "") +
            '<div class="menu-row-body">' +
              '<div class="menu-row-top">' +
                '<div class="menu-row-title"><h4>' + esc(d.name) + "</h4>" +
                '<span class="en">' + esc(d.nameEn) + "</span></div>" +
                '<span class="dots" aria-hidden="true"></span>' +
                '<span class="price">¥' + d.price + "</span>" +
              "</div>" +
              '<p class="menu-row-desc">' + esc(d.description) + "</p>" +
            "</div>" +
          "</div>"
        );
      }).join("");
    });
  }

  async function hydrateMenu() {
    renderSignature(DEFAULT_DISHES);
    renderMenu(DEFAULT_DISHES);
    let dishes = DEFAULT_DISHES;
    try {
      const res = await fetch("/api/dishes", { headers: { Accept: "application/json" } });
      if (!res.ok) throw new Error("api unavailable");
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        dishes = data;
        const adminLink = document.getElementById("admin-link");
        if (adminLink) adminLink.hidden = false;
        const headerLogin = document.getElementById("header-login");
        if (headerLogin) headerLogin.hidden = false;
        document.querySelectorAll(".nav-login-item").forEach(function (item) {
          item.hidden = false;
        });
      }
    } catch (e) {
      // 纯静态环境（如 GitHub Pages）下保持内置菜单
    }
    renderSignature(dishes);
    renderMenu(dishes);
  }

  /* ---------- 顶部导航：滚动后切换样式 ---------- */
  function onScrollHeader() {
    header.classList.toggle("scrolled", window.scrollY > 40);
    toTop.classList.toggle("show", window.scrollY > 640);
  }
  window.addEventListener("scroll", onScrollHeader, { passive: true });
  onScrollHeader();

  /* ---------- 移动端菜单 ---------- */
  navToggle.addEventListener("click", function () {
    const open = document.body.classList.toggle("nav-open");
    navToggle.setAttribute("aria-expanded", String(open));
    navToggle.setAttribute("aria-label", open ? "关闭菜单" : "打开菜单");
  });

  document.querySelectorAll(".site-nav a").forEach(function (link) {
    link.addEventListener("click", function () {
      document.body.classList.remove("nav-open");
      navToggle.setAttribute("aria-expanded", "false");
    });
  });

  /* ---------- 滚动显现动画 ---------- */
  const revealEls = document.querySelectorAll(".reveal");
  if ("IntersectionObserver" in window) {
    const io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add("in");
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: "0px 0px -40px 0px" });
    revealEls.forEach(function (el) { io.observe(el); });
  } else {
    revealEls.forEach(function (el) { el.classList.add("in"); });
  }

  /* ---------- 菜单分类切换 ---------- */
  const tabs = document.querySelectorAll(".menu-tab");
  const panels = document.querySelectorAll(".menu-panel");

  tabs.forEach(function (tab) {
    tab.addEventListener("click", function () {
      tabs.forEach(function (t) {
        t.classList.remove("is-active");
        t.setAttribute("aria-selected", "false");
      });
      panels.forEach(function (p) { p.classList.remove("is-active"); });

      tab.classList.add("is-active");
      tab.setAttribute("aria-selected", "true");
      const panel = document.getElementById("panel-" + tab.dataset.tab);
      if (panel) {
        panel.classList.add("is-active");
        panel.querySelectorAll(".reveal").forEach(function (el) {
          el.classList.remove("in");
          void el.offsetWidth;
          el.classList.add("in");
        });
      }
    });
  });

  /* ---------- 高亮当前导航 ---------- */
  const sections = ["home", "about", "signature", "menu", "gallery", "reviews"];
  const navLinks = document.querySelectorAll(".site-nav a");
  const sectionEls = sections
    .map(function (id) { return document.getElementById(id); })
    .filter(Boolean);

  if ("IntersectionObserver" in window) {
    const navIO = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          navLinks.forEach(function (a) {
            a.classList.toggle("is-active", a.getAttribute("href") === "#" + entry.target.id);
          });
        }
      });
    }, { rootMargin: "-45% 0px -50% 0px" });
    sectionEls.forEach(function (s) { navIO.observe(s); });
  }

  /* ---------- 回到顶部 ---------- */
  toTop.addEventListener("click", function () {
    window.scrollTo({ top: 0, behavior: "smooth" });
  });

  /* ---------- 预订表单 ---------- */
  const form = document.getElementById("reserve-form");

  // 用餐日期最小为今天
  const today = new Date();
  const iso = today.getFullYear() + "-" +
    String(today.getMonth() + 1).padStart(2, "0") + "-" +
    String(today.getDate()).padStart(2, "0");
  form.date.min = iso;

  function markInvalid(input, bad) {
    input.classList.toggle("invalid", bad);
    return !bad;
  }

  function showToast(message) {
    toast.textContent = message;
    toast.classList.add("show");
    clearTimeout(showToast._timer);
    showToast._timer = setTimeout(function () {
      toast.classList.remove("show");
    }, 3600);
  }

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = form.name;
    const phone = form.phone;
    const date = form.date;
    const time = form.time;
    const guests = form.guests;

    const checks = [
      { el: name, bad: name.value.trim() === "", msg: "请填写您的姓名" },
      { el: phone, bad: !/^1[3-9]\d{9}$/.test(phone.value.trim()), msg: "手机号格式不正确（需 11 位数字）" },
      { el: date, bad: date.value === "", msg: "请选择用餐日期" },
      { el: time, bad: time.value === "", msg: "请选择用餐时间" },
      { el: guests, bad: guests.value === "", msg: "请选择用餐人数" }
    ];
    let firstError = null;
    checks.forEach(function (c) {
      c.el.classList.toggle("invalid", c.bad);
      if (c.bad && !firstError) firstError = c;
    });
    if (firstError) {
      showToast(firstError.msg);
      firstError.el.focus();
      return;
    }

    const payload = {
      name: name.value.trim(),
      phone: phone.value.trim(),
      date: date.value,
      time: time.value,
      guests: parseInt(guests.value, 10),
      room: form.room ? form.room.value : "大厅",
      note: form.note ? form.note.value.trim() : ""
    };

    try {
      const res = await fetch("/api/reservations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error("bad status");
      showToast("预订成功！我们已收到您的预订，将尽快电话确认。");
      form.reset();
    } catch (e) {
      showToast("当前为演示环境，预订未提交到服务器（部署 Java 后端后即可在线预订）。");
    }
  });

  // 输入时即时清除错误状态
  form.querySelectorAll("input, select").forEach(function (input) {
    input.addEventListener("input", function () {
      input.classList.remove("invalid");
    });
    input.addEventListener("change", function () {
      input.classList.remove("invalid");
    });
  });

  /* ---------- 页脚年份 ---------- */
  if (yearEl) {
    yearEl.textContent = String(new Date().getFullYear());
  }

  /* ---------- 加载菜单 ---------- */
  hydrateMenu();
})();
