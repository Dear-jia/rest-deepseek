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
    { name: "女仆特制蛋包饭", nameEn: "Maid Omelette Rice", description: "松软蛋皮裹着香糯炒饭，番茄酱画出爱心～", price: 38, image: "assets/img/dish-omelette.jpg", category: "HOT", tag: "招牌", recommended: true },
    { name: "草莓松饼", nameEn: "Strawberry Pancakes", description: "现烤松饼叠草莓与奶油，甜度刚刚好。", price: 32, image: "assets/img/dish-pancake.jpg", category: "HOT", tag: "人气", recommended: true },
    { name: "奶油培根意面", nameEn: "Spaghetti Carbonara", description: "浓郁奶油裹着培根与蛋香，一口满足。", price: 36, image: "assets/img/dish-carbonara.jpg", category: "HOT", tag: null, recommended: true },
    { name: "泰式绿咖喱饭", nameEn: "Thai Green Curry", description: "椰香微辣的绿咖喱，配热米饭正合适。", price: 34, image: "assets/img/dish-curry.jpg", category: "HOT", tag: null, recommended: true },
    { name: "纽约芝士蛋糕", nameEn: "New York Cheesecake", description: "绵密芝士配酥脆饼底，下午茶首选。", price: 28, image: "assets/img/dish-cheesecake.jpg", category: "HOT", tag: "甜品", recommended: true },
    { name: "巧克力布朗尼", nameEn: "Chocolate Brownies", description: "外脆内软的布朗尼，树莓点缀酸甜解腻。", price: 26, image: "assets/img/dish-brownie.jpg", category: "HOT", tag: null, recommended: true },
    { name: "番茄肉酱意面", nameEn: "Spaghetti Bolognese", description: "慢炖番茄肉酱，经典好味道。", price: 30, image: "assets/img/dish-bolognese.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "姜饼华夫饼", nameEn: "Gingerbread Waffles", description: "外酥里软的华夫，淋上枫糖浆。", price: 26, image: "assets/img/dish-waffle.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "树莓慕斯", nameEn: "Raspberry Mousse", description: "轻盈树莓慕斯，酸甜绵密入口即化。", price: 22, image: "assets/img/dish-mousse.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "苹果蛋糕", nameEn: "Apple Cake", description: "肉桂苹果的温暖香气，配红茶刚好。", price: 24, image: "assets/img/dish-apple-cake.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "草莓塔", nameEn: "Strawberry Tart", description: "酥脆塔壳配新鲜草莓与卡仕达酱。", price: 26, image: "assets/img/dish-strawberry-tart.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "咖啡冰淇淋", nameEn: "Espresso Ice Cream", description: "浓缩咖啡遇上香草冰淇淋，冷热交融。", price: 25, image: "assets/img/dish-icecream.jpg", category: "MAIN", tag: null, recommended: false },
    { name: "女仆拿铁", nameEn: "Maid Latte", description: "拉花里藏着爱心，杯边还有小猫爪。", price: 22, image: null, category: "DRINK", tag: "招牌", recommended: false },
    { name: "抹茶拿铁", nameEn: "Matcha Latte", description: "宇治抹茶与醇厚牛奶的温柔相遇。", price: 24, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "草莓奶昔", nameEn: "Strawberry Milkshake", description: "新鲜草莓打成绵密奶昔，少女心满分。", price: 26, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "珍珠奶茶", nameEn: "Bubble Tea", description: "Q 弹珍珠配经典奶茶，快乐加倍。", price: 18, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "蜜桃气泡水", nameEn: "Peach Soda", description: "蜜桃香气在气泡里跳舞。", price: 20, image: null, category: "DRINK", tag: null, recommended: false },
    { name: "樱花苏打", nameEn: "Sakura Soda", description: "淡粉色樱花风味苏打，颜值担当。", price: 22, image: null, category: "DRINK", tag: null, recommended: false }
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
        const headerUser = document.getElementById("header-user-link");
        if (headerUser) headerUser.hidden = false;
        const footerUser = document.getElementById("footer-user-link");
        if (footerUser) footerUser.hidden = false;
        document.querySelectorAll(".nav-login-item").forEach(function (item) {
          item.hidden = false;
        });
        document.querySelectorAll(".nav-user-item").forEach(function (item) {
          item.hidden = false;
        });
      }
    } catch (e) {
      // 纯静态环境（如 GitHub Pages）下保持内置菜单
    }
    renderSignature(dishes);
    renderMenu(dishes);
  }

  /* ---------- 首页顾客评价（来自后端审核通过的评论，接口不可用时保留内置） ---------- */
  async function hydrateReviews() {
    try {
      const res = await fetch("/api/reviews", { headers: { Accept: "application/json" } });
      if (!res.ok) throw new Error("api unavailable");
      const data = await res.json();
      const grid = document.getElementById("review-grid");
      if (!grid || !Array.isArray(data) || data.length === 0) return;
      grid.innerHTML = data.map(function (r) {
        const rate = Math.max(1, Math.min(5, r.rating));
        const stars = "★".repeat(rate) + "☆".repeat(5 - rate);
        return (
          '<blockquote class="review-card reveal in">' +
            '<div class="stars" aria-label="' + rate + ' 星好评">' + stars + "</div>" +
            '<p class="review-text">' + esc(r.content) + "</p>" +
            '<footer class="review-author">' +
              '<span class="avatar" aria-hidden="true">' + esc((r.nickname || "客").charAt(0)) + "</span>" +
              "<div><strong>" + esc(r.nickname || "顾客") + "</strong><span>" + esc(r.createdAt || "") + " · 用户评价</span></div>" +
            "</footer>" +
          "</blockquote>"
        );
      }).join("");
    } catch (e) {
      // 保留内置评价
    }
  }

  /* ---------- 女仆与主厨（来自后端，可上传照片；接口不可用时保留内置） ---------- */
  const STAFF_AVATAR_COLORS = ["#ffd1e3", "#e8e2ff", "#ffe9c9", "#d3f3e8", "#ffe1f2", "#dbeafe"];

  async function hydrateStaff() {
    try {
      const res = await fetch("/api/staff", { headers: { Accept: "application/json" } });
      if (!res.ok) throw new Error("api unavailable");
      const data = await res.json();
      const grid = document.getElementById("staff-grid");
      if (!grid || !Array.isArray(data) || data.length === 0) return;
      grid.innerHTML = data.map(function (s, i) {
        const photo = s.image
          ? '<img class="staff-photo" src="' + esc(s.image) + '" alt="' + esc(s.name) + '" loading="lazy">'
          : '<div class="staff-avatar" style="background:' + STAFF_AVATAR_COLORS[i % STAFF_AVATAR_COLORS.length] + ';">' +
              esc((s.name || "樱").charAt(0)) + "</div>";
        return (
          '<article class="staff-card reveal in">' +
            photo +
            "<h3>" + esc(s.name) + "</h3>" +
            '<p class="staff-role">' + esc(s.role || "") + "</p>" +
            '<p class="staff-desc">' + esc(s.description || "") + "</p>" +
          "</article>"
        );
      }).join("");
    } catch (e) {
      // 保留内置女仆卡片
    }
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
    if (form.dataset.submitting === "1") return;

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

    const submitBtn = form.querySelector('button[type="submit"]');
    const btnLabel = submitBtn ? submitBtn.textContent : "";
    form.dataset.submitting = "1";
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = "提交中…";
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
    form.dataset.submitting = "";
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = btnLabel;
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
  hydrateReviews();
  hydrateStaff();
})();
