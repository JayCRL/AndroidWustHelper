# 武科大助手安卓端

武科大助手安卓端是一个基于原生 Android View 体系构建的校园服务聚合应用，面向武汉科技大学学生，集成了课表、成绩、图书馆、空教室、校车、校历、黄页、倒计时、桌面小组件，以及一批校园社区类功能入口。

## 项目概览

- 项目名：`AndroidWustHelper`
- 项目类型：原生 Android App
- 主要语言：Java
- 构建工具：Gradle Groovy DSL
- Android Gradle Plugin：`7.4.2`
- 模块结构：单模块，仅 `:app`
- UI 体系：AndroidX + XML + ViewBinding
- 代码组织：传统 Activity/Fragment + 部分 MVP 分层

## 关键配置

- `applicationId`: `com.linghang.wusthelper`
- Manifest package: `com.example.wusthelper`
- `minSdkVersion`: `21`
- `targetSdkVersion`: `30`
- `compileSdkVersion`: `30`
- `versionCode`: `22`
- `versionName`: `5.2.8`
- Java 版本：`1.8`

## 主要目录结构

```text
AndroidWustHelper-main/
├── app/
│   ├── src/main/java/com/example/wusthelper/
│   │   ├── ui/                 # Activity、Fragment、Dialog 等界面层
│   │   ├── mvp/                # MVP 分层代码
│   │   ├── request/            # 网络层、API 常量、请求封装
│   │   ├── helper/             # 辅助逻辑
│   │   ├── utils/              # 工具类
│   │   ├── dbhelper/           # 数据库相关辅助
│   │   └── appwidget/          # 桌面小组件
│   └── src/main/res/           # XML 资源
├── build.gradle                # 顶层 Gradle 配置
├── settings.gradle             # 模块声明
└── README.md
```

## 主要入口

- Application：`app/src/main/java/com/example/wusthelper/MyApplication.java`
- 启动页：`app/src/main/java/com/example/wusthelper/ui/activity/LaunchActivity.java`
- 主页面：`app/src/main/java/com/example/wusthelper/ui/activity/MainMvpActivity.java`
- 首页 Fragment：`app/src/main/java/com/example/wusthelper/ui/fragment/mainviewpager/HomeFragment.java`
- 网络入口：`app/src/main/java/com/example/wusthelper/request/NewApiHelper.java`
- API 常量：`app/src/main/java/com/example/wusthelper/request/WustApi.java`

## 当前首页可见功能

根据 `HomeFragment`，首页当前包含以下主要入口：

- 成绩
- 倒计时
- 学分统计
- 二手平台
- 竞赛组队
- 校园搭子
- 学习互助 / 空教室
- AI 问答
- 校园猫
- 校车
- 校历
- 体育
- 黄页

此外，项目还包含图书馆、扫码、奖学金、设置、桌面组件等模块。

## 网络与后端接入

项目同时存在旧接口体系和新接口体系：

### 1. 旧体系
- 主域名：`https://wusthelper.wustlinghang.cn/mobileapi`
- 用于教务、图书馆、倒计时、部分历史功能

### 2. 新体系 / 直连服务
- basic 服务：`http://8.162.1.176:8082`
- chat 服务：`http://8.162.1.176:8096`
- 社区服务：`https://www.lyzyy.love:8081`
- 网关：`http://lyzyy.love:8088`

当前社区功能主要围绕以下服务：

- `CampusMate`
- `CompetitionGroup`
- `SecondHand`
- `campus-cat` 网关路由

## 主要技术依赖

- AndroidX AppCompat / Material / Navigation
- RecyclerView / CardView
- OkHttp / Retrofit / Gson
- LitePal
- Glide / PhotoView
- BaseRecyclerViewAdapterHelper
- SmartRefreshLayout
- EventBus
- EasyPermissions
- MPAndroidChart
- XUpdate / XUtil
- bga-qrcode-zxing
- Lottie

## 构建方式

### Android Studio
直接以项目根目录导入即可。

### 命令行
在项目根目录执行：

```bash
./gradlew assembleDebug
```

如需完整构建信息，也可执行：

```bash
./gradlew app:assembleDebug
```

## 运行说明

1. 使用 Android Studio 打开项目根目录。
2. 确保本地 Android SDK 与 Gradle 环境可用。
3. 如需访问完整在线能力，需具备可用网络环境以及后端接口可达性。
4. 部分社区能力和新服务依赖远端接口状态，若接口不可达，部分页面会出现降级或空数据表现。

## 项目特点

- Java 老项目，仍以 XML + Activity/Fragment 为主
- 已启用 ViewBinding，适合在现有页面上增量改造
- 既有 MVP 代码，也有较多直接在 Activity 中编排网络和列表逻辑的页面
- 首页功能较多，根目录还混有后端/资料/压缩包等非主工程内容，日常开发应优先聚焦 `app/src/main`

## 开发注意事项

- 根目录存在 `WustChat`、`wust-mywust-basic`、若干 zip、markdown、python 文件，不属于安卓主工程核心改动范围。
- 搜索与修改时优先关注：`app/src/main`、`build.gradle`、`settings.gradle`、`AndroidManifest.xml`。
- `app/build.gradle` 中包含本地签名配置等敏感信息，协作时不要向 README、记忆或外部输出扩散具体值。
- 项目中存在构建产物目录，提交前建议确认只提交真正需要的源码与文档变更。

## 仓库信息

- Git remote: `origin -> https://github.com/JayCRL/AndroidWustHelper.git`

如果后续要继续补齐社区能力，建议优先从 `request/NewApiHelper.java`、`request/WustApi.java` 和首页对应 Activity 入手。
