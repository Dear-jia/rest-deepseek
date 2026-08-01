/* ============================================================
   山云小馆 · 餐厅主页交互脚本
   ============================================================ */

(function () {
  "use strict";

  const header = document.getElementById("header");
  const navToggle = document.querySelector(".nav-toggle");
  const toTop = document.getElementById("to-top");
  const toast = document.getElementById("toast");
  const yearEl = document.getElementById("year");

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
        // 面板内的显现动画重新触发
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
  const phoneInput = document.getElementById("r-phone");
  const dateInput = document.getElementById("r-date");

  // 用餐日期最小为今天
  const today = new Date();
  const iso = today.getFullYear() + "-" +
    String(today.getMonth() + 1).padStart(2, "0") + "-" +
    String(today.getDate()).padStart(2, "0");
  dateInput.min = iso;

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

  form.addEventListener("submit", function (event) {
    event.preventDefault();

    const name = form.name;
    const phone = form.phone;
    const date = form.date;
    const time = form.time;
    const guests = form.guests;

    let ok = true;
    ok = markInvalid(name, name.value.trim() === "") && ok;
    ok = markInvalid(phone, !/^1[3-9]\d{9}$/.test(phone.value.trim())) && ok;
    ok = markInvalid(date, date.value === "") && ok;
    ok = markInvalid(time, time.value === "") && ok;
    ok = markInvalid(guests, guests.value === "") && ok;

    if (!ok) {
      showToast("请检查并完善预订信息（手机号需为 11 位）");
      return;
    }

    showToast("预订已提交！我们将尽快电话确认，感谢您的信任。");
    form.reset();
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
})();
