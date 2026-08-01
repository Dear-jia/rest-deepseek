# 山云小馆 · 餐厅主页

一个纯前端的中式家常餐厅主页，无构建工具、无外部运行时依赖，图片全部存放在本地，双击即可打开。

## 快速开始

直接用浏览器打开 `index.html`，或起一个静态服务（推荐，避免个别浏览器对本地文件的限制）：

```powershell
python -m http.server 8080
```

然后访问 http://localhost:8080

## 目录结构

```text
project-rest/
├── index.html        # 页面结构（导航/Hero/关于/招牌菜/菜单/环境/评价/预订/页脚）
├── css/style.css     # 主题样式与响应式布局
├── js/main.js        # 交互（滚动导航、菜单切换、表单校验、动画）
├── assets/img/       # 本地图片素材（菜品与餐厅环境）
├── README.md
└── CREDITS.md        # 图片来源与授权说明
```

## 页面内容

- 首屏 Hero：餐厅环境大图 + 主标语 + 双按钮
- 数据条：经营年份、菜品数、评分、食材理念
- 关于我们：门店故事 + 三大特色
- 招牌菜：六道招牌菜卡片
- 今日菜单：三个分类 Tab（热菜 / 主食汤品 / 甜品饮品）
- 环境掠影：图片墙
- 顾客评价：三条口碑
- 在线预订：联系信息 + 预订表单（前端校验 + 提交提示）
- 页脚：导航、营业时间、联系方式

## 自定义指南

### 修改门店信息

在 `index.html` 中搜索以下占位信息并替换：

- 门店地址：`上海市徐汇区衡山路 88 号`
- 预订电话：`021-8888-6666`
- 营业时间：`10:30 – 22:00` 等
- 邮箱：`hello@shanyun.example.com`
- 店名：`山云小馆` / `SHAN YUN KITCHEN`（出现在导航、Hero、页脚）

### 替换菜品与图片

1. 将门店实拍图放入 `assets/img/`，文件名覆盖同名文件即可（保持 `jpg` 格式），无需改代码。
2. 或修改 `index.html` 中 `src="assets/img/xxx.jpg"` 指向新文件。
3. 菜品名称、描述、价格直接在对应卡片/菜单行里改。

### 修改配色

主题色定义在 `css/style.css` 顶部的 `:root` 变量中：

| 变量 | 说明 | 当前值 |
| --- | --- | --- |
| `--accent` | 主色（朱砂红） | `#b5431f` |
| `--gold` | 点缀色（鎏金） | `#c99a3f` |
| `--paper` | 页面底色 | `#faf6ee` |
| `--ink` | 正文色 | `#241c14` |

### 预订表单

当前为纯前端演示：提交后显示成功提示，不会真正发送数据。接入后端时，在 `js/main.js` 的 `form.addEventListener("submit", ...)` 中把 `showToast(...)` 前替换为 `fetch` 请求即可。

## 技术说明

- 原生 HTML/CSS/JS，无框架、无构建步骤
- 图片全部本地化，页面可离线打开
- 响应式布局：桌面 3 列 / 平板 2 列 / 手机单列，含移动端汉堡菜单
- 无障碍：语义化标签、alt 文本、`aria` 属性、键盘焦点样式
- 尊重 `prefers-reduced-motion` 系统设置

## 部署上线

### 方案一：GitHub Pages（推荐，免费）

仓库已内置自动部署工作流，推送到 `main` 分支后会自动发布。

1. 在 GitHub 新建一个空仓库（不要勾选 README 初始化）：https://github.com/new
2. 在本地关联并推送：

   ```powershell
   git remote add origin git@github.com:<你的用户名>/<仓库名>.git
   git push -u origin main
   ```

3. 打开仓库 Settings → Pages，将 Source 选为 **GitHub Actions**。
4. 等待工作流跑完，访问 `https://<你的用户名>.github.io/<仓库名>/`。

> 项目内所有资源均使用相对路径，放在任何子路径下都能正常显示。

### 方案二：Vercel（最快，无需建仓库）

1. 在项目目录运行：

   ```powershell
   npx vercel
   ```

2. 按提示用浏览器登录（首次会创建 Vercel 账号），Framework Preset 选 **Other**。
3. 发布后得到 `https://<项目名>.vercel.app` 地址，之后每次 `npx vercel --prod` 更新。

### 自定义域名

- GitHub Pages：在仓库 Settings → Pages 中填写自定义域名，并把 CNAME 记录指向 `<你的用户名>.github.io`，同时在仓库根目录添加 `CNAME` 文件。
- Vercel：项目 Settings → Domains 中添加域名，按提示配置 DNS。

> 提示：如果访客主要在国内，建议绑定自己的域名并完成 ICP 备案，或使用国内 CDN（如腾讯云 CDN、阿里云 OSS）回源到上面的站点，访问会更稳定。

## 图片来源

本演示站点使用的菜品与餐厅图片来自 Pexels 与 TheMealDB，仅作展示用途。上线前请替换为门店实拍图，具体清单见 [CREDITS.md](CREDITS.md)。
