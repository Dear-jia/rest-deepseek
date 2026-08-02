# 文峰小馆 · 餐厅官网 + 后台管理系统

一个中式家常餐厅的完整网站：前台官网（纯静态，可发布到 GitHub Pages）+ 后台管理系统（Spring Boot 3 + Java 17）。

## 前台官网（静态版）

直接用浏览器打开 `index.html`，或起一个静态服务（推荐，避免个别浏览器对本地文件的限制）：

```powershell
python -m http.server 8080
```

然后访问 http://localhost:8080

前台菜单由后端接口 `/api/dishes` 动态加载；接口不可用时自动回退到内置菜单，因此静态版页面始终可用。预订表单同理：后端可用时提交到服务器，否则显示演示提示。

## 目录结构

```text
project-rest/
├── index.html            # 前台页面结构
├── css/style.css         # 前台主题样式与响应式布局
├── js/main.js            # 前台交互 + 菜单/预订对接后端接口
├── assets/img/           # 前台图片素材
├── server/               # Java 后端（Spring Boot）
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/wenfeng/   # 实体、控制器、安全配置、初始化数据
│       └── resources/
│           ├── application.yml # 端口、数据库、后台账号等配置
│           └── templates/admin/ # 后台 Thymeleaf 页面
├── docker-compose.yml    # 一键 Docker 部署
├── README.md
└── CREDITS.md        # 图片来源与授权说明
```

## 后台管理系统

后台地址：`http://localhost:8080/admin`

默认账号：`admin` / `admin123`（首次部署后请务必修改，见下方「修改管理员密码」）

功能：

- 数据看板：菜品数、今日预订、待确认数、累计预订、最新预订
- 菜品管理：新增 / 编辑 / 删除菜品，设置分类、价格、标签、招牌推荐、上下架、排序
- 预订管理：查看前台提交的预订，按状态筛选（全部/待确认/已确认/已取消），一键确认 / 取消 / 删除
- 修改密码：后台右上角「修改密码」可随时更换管理员密码

### 长期存储数据（PostgreSQL）

默认使用本地 H2 文件数据库；需要数据长期保存（例如 Render 免费实例的临时磁盘会丢数据）时，接入 PostgreSQL：

1. 注册一个免费 PostgreSQL（任选其一）：
   - [Neon](https://neon.tech)（免费 0.5GB，无到期时间，推荐）
   - [Supabase](https://supabase.com)（免费 500MB）
   - Render 自带的 PostgreSQL（付费版持久，免费版有限制）
2. 创建数据库后，把连接信息配置为环境变量（Render 的 Environment 或本地启动前设置）：

   ```powershell
   $env:DATABASE_URL  = "jdbc:postgresql://主机:端口/数据库名"
   $env:DB_DRIVER     = "org.postgresql.Driver"
   $env:DB_USERNAME   = "用户名"
   $env:DB_PASSWORD   = "密码"
   ```

3. 重启应用，首次启动会自动建表并写入默认菜品。

> 注意：从 H2 切换到 PostgreSQL 相当于全新开始（旧数据不会自动迁移）；之后数据写入 PostgreSQL，重新部署、重启都不丢失。

### 本地运行后端

需要 JDK 17 与 Maven：

```powershell
cd server
mvn spring-boot:run
```

或打包后运行：

```powershell
mvn -DskipTests package
java -jar target/wenfeng-kitchen-0.1.0.jar
```

启动后：

- 前台：http://localhost:8080/
- 后台：http://localhost:8080/admin
- 数据库（H2 文件模式）：`server/data/wenfeng.mv.db`，首次启动自动建表并写入默认菜品
- H2 控制台：http://localhost:8080/h2-console（JDBC URL：`jdbc:h2:file:./data/wenfeng`，用户 `sa`）

### 后端接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/dishes` | 前台菜品列表（仅上架菜品） |
| POST | `/api/reservations` | 提交预订（JSON：name/phone/date/time/guests/room/note） |

### 修改管理员密码

通过环境变量配置（生产环境推荐），例如：

```powershell
$env:ADMIN_USER="admin"
$env:ADMIN_PASSWORD="你的强密码"
java -jar target/wenfeng-kitchen-0.1.0.jar
```

> 注意：只有首次启动（数据库为空）时才会创建管理员，之后再改环境变量不会更新已有密码；如需重置，删除 `server/data/` 下的数据库文件后重启。

## 本地开发指南（自己扩展后端功能）

### 环境与启动

本机已装好 JDK 17（`C:\Program Files\Java\jdk-17`）和 Maven（`%LOCALAPPDATA%\codex-tools\apache-maven-3.9.16`）。
注意：系统 PATH 里的 `java` 是 8，运行后端请用 JDK 17。

一键启动（推荐）：

```powershell
.\server\run-dev.ps1
```

如果 8080 端口被其他程序占用（比如同时运行着别的 Java 服务），换一个端口：

```powershell
.\server\run-dev.ps1 -Port 8088
```

或手动启动：

```powershell
cd server
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
mvn spring-boot:run
```

### 代码结构

```text
server/src/main/java/com/wenfeng/
├── WenfengApplication.java     # 启动类
├── config/                     # 安全配置、CORS、初始数据
├── dish/                       # 菜品模块（实体 / 仓库 / 服务）
├── reservation/                # 预订模块（实体 / 仓库）
├── api/                        # 前台公开接口 /api/**
└── admin/                      # 后台页面控制器 /admin/**
server/src/main/resources/
├── application.yml             # 端口、数据库、账号等配置
└── templates/admin/            # 后台 Thymeleaf 页面
```

### 新增一个功能模块（以「评论管理」为例）

1. 建实体：`review/Review.java`（加 `@Entity` 注解和字段）
2. 建仓库：`review/ReviewRepository.java`（继承 `JpaRepository`）
3. 建服务：`review/ReviewService.java`（业务逻辑，可选）
4. 建控制器：后台页 `admin/AdminReviewController.java`；公开接口在 `api/PublicApiController.java` 里加
5. 建页面：`templates/admin/reviews.html`，并在 `templates/admin/fragments.html` 顶栏加导航
6. 重启应用本地验证，然后 `git add -A` → `git commit -m "..."` → `git push`，Render 会自动部署

### 常用命令

```powershell
mvn spring-boot:run              # 本地运行（热更新配置/模板）
mvn -DskipTests package          # 打包
java -jar target/wenfeng-kitchen-0.1.0.jar   # 运行打包产物
```

连接 PostgreSQL 时，启动前设置 `DATABASE_URL` / `DB_DRIVER` / `DB_USERNAME` / `DB_PASSWORD` 四个环境变量即可，其余不用改代码。

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

### 方案一：GitHub Pages（免费，前台静态版）

仓库已内置自动部署工作流，推送到 `main` 分支后会自动发布。

1. 在 GitHub 新建一个空仓库（不要勾选 README 初始化）：https://github.com/new
2. 在本地关联并推送：

   ```powershell
   git remote add origin git@github.com:<你的用户名>/<仓库名>.git
   git push -u origin main
   ```

3. 打开仓库 Settings → Pages，将 Source 选为 **GitHub Actions**。
4. 等待工作流跑完，访问 `https://<你的用户名>.github.io/<仓库名>/`。

> 项目内所有资源均使用相对路径，放在任何子路径下都能正常显示。GitHub Pages 只能托管前台静态页面，后台管理需要运行 Java 服务（见方案三）。

### 方案二：Vercel（前台静态版）

1. 在项目目录运行：

   ```powershell
   npx vercel
   ```

2. 按提示用浏览器登录（首次会创建 Vercel 账号），Framework Preset 选 **Other**。
3. 发布后得到 `https://<项目名>.vercel.app` 地址，之后每次 `npx vercel --prod` 更新。

### 方案三：Render 免费部署（完整版，含后台）

1. 注册 Render 账号：https://render.com （建议直接用 GitHub 账号登录）
2. 登录后点击 **New +** → **Blueprint**
3. 选择仓库 `Dear-jia/rest-deepseek`，Render 会读取仓库里的 `render.yaml` 自动创建服务
4. 等待构建完成（首次约 10–20 分钟，含 Maven 编译），完成后访问
   `https://wenfeng-kitchen.onrender.com/`（前台）与 `https://wenfeng-kitchen.onrender.com/admin`（后台）

注意事项：

- 免费实例在 15 分钟无访问后会休眠，再次访问需等待约 30–60 秒唤醒
- 免费实例使用临时磁盘，重启/重新部署后 H2 数据会清空；请按上面「长期存储数据」接入 PostgreSQL
- 首次部署后请到 Render Dashboard 的 Environment 中修改 `ADMIN_PASSWORD`，或登录后台后使用「修改密码」页面更换
- 之后每次 `git push`，Render 会自动重新构建部署

### 方案四：自有服务器 Docker 部署（完整版，含后台）

服务器安装 Docker 后，在项目根目录执行：

```bash
docker compose up -d --build
```

然后访问 `http://服务器IP:8080/`（前台）与 `http://服务器IP:8080/admin`（后台）。预订数据保存在 `server/data/` 卷中，重启不丢失。

如需换端口，修改 `docker-compose.yml` 中的 `ports` 映射即可。

### 自定义域名

- GitHub Pages：在仓库 Settings → Pages 中填写自定义域名，并把 CNAME 记录指向 `<你的用户名>.github.io`，同时在仓库根目录添加 `CNAME` 文件。
- Vercel：项目 Settings → Domains 中添加域名，按提示配置 DNS。

> 提示：如果访客主要在国内，建议绑定自己的域名并完成 ICP 备案，或使用国内 CDN（如腾讯云 CDN、阿里云 OSS）回源到上面的站点，访问会更稳定。

## 图片来源

本演示站点使用的菜品与餐厅图片来自 Pexels 与 TheMealDB，仅作展示用途。上线前请替换为门店实拍图，具体清单见 [CREDITS.md](CREDITS.md)。
