# 安卓端联调 API 文档

本文档整理**可直接交给安卓端联调**的接口能力。

实测环境：
- 网关地址：`http://your-gateway-domain:8088`
- 验证脚本：`test_all_system.py`
- 最新实测结果：
  - 学生登录成功
  - 管理员登录成功
  - 搜课程成功
  - 空教室成功
  - 安卓公告成功
  - 轮播图成功
  - 校历成功
  - 二手基础接口成功
  - 竞赛分页接口可达

> 说明：校园猫、校园搭子本轮已在主流程中标记为 `skipped`，不属于本次安卓端主交付范围。

---

## 1. 接入范围

本轮安卓端建议优先接入以下能力：

1. 学生登录
2. 搜课程 / 蹭课搜索
3. 空教室查询
4. 安卓公告
5. 首页轮播图
6. 校历
7. 二手基础接口
8. 竞赛基础接口

---

## 2. 网关入口与鉴权

### 2.1 网关入口
安卓端统一走网关：

```text
http://your-gateway-domain:8088
```

不要使用服务内部端口直连。

### 2.2 Token 传递方式
学生登录成功后，后续请求建议统一带上：

```http
Authorization: Bearer <token>
```

### 2.3 响应结构
项目当前线上返回以 JSON 为主，统一按下面结构处理：

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

兼容说明：
- `code`：业务状态码，`200` 或 `0` 一般表示成功
- `message` / `msg`：业务提示信息
- `data`：业务数据
- `timestamp`：部分接口会返回

### 2.4 空数据处理约定
本轮多个线上接口返回的是空数据，但接口本身是成功的。安卓端请按以下方式处理：

- `data: []`：按“暂无数据”处理
- `data: null`：按“暂无内容”处理
- 不要把空数组或 `null` 视为接口失败

---

## 3. 登录接口

## 3.1 本科生登录
- **方法：** `POST`
- **路径：** `/wust-basic/UnderGraduateStudent/login`
- **是否需要登录：** 否
- **Content-Type：** `application/json`

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | String | 是 | 学号 |
| password | String | 是 | 教务系统密码 |

### 请求示例

```json
{
  "username": "your_student_id",
  "password": "your_password"
}
```

### 响应示例

```json
{
  "code": 200,
  "message": "Success",
  "data": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 安卓接入建议
- 将 `data` 保存为学生登录 token
- 后续请求通过拦截器自动附加 `Authorization: Bearer <token>`
- 若返回 `401`，提示重新登录

---

## 4. 本科生工具接口

## 4.1 搜课程 / 蹭课搜索
- **方法：** `GET`
- **路径：** `/wust-basic/UnderGraduateStudent/searchCourses`
- **是否需要登录：** 建议带学生 token

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| term | String | 否 | 学期，不传则使用服务端默认学期 |
| name | String | 否 | 课程名 |
| teacher | String | 否 | 教师名 |
| classroom | String | 否 | 教室 |
| weekDay | Integer | 否 | 星期几 |
| section | Integer | 否 | 节次 |

### 本轮实测请求

```http
GET /wust-basic/UnderGraduateStudent/searchCourses?name=大学英语
Authorization: Bearer <student_token>
```

### 本轮实测响应

```json
{
  "code": 200,
  "message": "Success",
  "data": [],
  "timestamp": 1773037318876
}
```

### 安卓接入建议
- 支持按课程名、教师、教室组合筛选
- `data` 返回空数组时，展示“暂无匹配课程”
- 空结果不属于异常

---

## 4.2 空教室查询
- **方法：** `GET`
- **路径：** `/wust-basic/UnderGraduateStudent/getEmptyClassrooms`
- **是否需要登录：** 建议带学生 token

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| term | String | 否 | 学期，不传则使用服务端默认学期 |
| week | Integer | 是 | 第几周 |
| weekDay | Integer | 是 | 星期几，一般为 1-7 |
| section | Integer | 是 | 节次 |

### 本轮实测请求

```http
GET /wust-basic/UnderGraduateStudent/getEmptyClassrooms?week=8&weekDay=2&section=1
Authorization: Bearer <student_token>
```

### 本轮实测响应

```json
{
  "code": 200,
  "message": "Success",
  "data": [],
  "timestamp": 1773037319007
}
```

### 安卓接入建议
- 页面建议使用周次、星期、节次下拉选择
- `data` 为教室名称数组
- 若为空数组，则展示“当前时段暂无可用教室”

---

## 5. 首页公共展示接口

## 5.1 安卓公告
- **方法：** `GET`
- **路径：** `/wust-basic/operationLog/list/publishedButAndroid`
- **是否需要登录：** 否

### 本轮实测请求

```http
GET /wust-basic/operationLog/list/publishedButAndroid
```

### 本轮实测响应

```json
{
  "code": 200,
  "message": "Success",
  "data": [],
  "timestamp": 1773037319111
}
```

### 有数据时常见字段
- `title`
- `context`
- `createdAt`
- `catogories`

### 安卓接入建议
- 首页公告区直接消费 `data`
- 空数组时展示“暂无公告”或隐藏模块
- 公告详情建议支持富文本/多行展开

---

## 5.2 首页轮播图
- **方法：** `GET`
- **路径：** `/wust-basic/admin/common/getCarousels`
- **是否需要登录：** 否

### 本轮实测请求

```http
GET /wust-basic/admin/common/getCarousels
```

### 本轮实测响应

```json
{
  "code": 200,
  "message": "Success",
  "data": [],
  "timestamp": 1773037319232
}
```

### 数据说明
- `data` 为图片 URL 数组

### 安卓接入建议
- 直接作为 Banner 图片源
- 空数组时隐藏轮播区域
- 建议增加占位图与加载失败兜底

---

## 5.3 校历
- **方法：** `GET`
- **路径：** `/wust-basic/admin/common/getCalendar`
- **是否需要登录：** 否

### 本轮实测请求

```http
GET /wust-basic/admin/common/getCalendar
```

### 本轮实测响应

```json
{
  "code": 200,
  "message": "Success",
  "data": null,
  "timestamp": 1773037319344
}
```

### 数据说明
- 正常情况下 `data` 为校历图片 URL
- 本轮线上返回 `null`，表示当前暂无已发布校历图

### 安卓接入建议
- 若 `data` 是字符串 URL，则进入图片预览或详情页
- 若 `data == null`，则显示“暂无校历”或隐藏入口

---

## 6. 已验证可继续接入的接口

## 6.1 二手交易基础接口

### 6.1.1 发布商品
- **方法：** `POST`
- **路径：** `/second-hand/commodity/publish`
- **是否需要登录：** 是
- **Content-Type：** `application/json`

#### 请求参数示例

```json
{
  "name": "二手耳机",
  "price": 99,
  "contact": "123456789",
  "status": 0,
  "type": 1,
  "introduce": "九成新，功能正常",
  "imageId": 1
}
```

#### 本轮验证结果
- 成功

---

### 6.1.2 商品广场列表
- **方法：** `GET`
- **路径：** `/second-hand/commodity/show/all/1/10`
- **是否需要登录：** 当前脚本带 token 验证成功

#### 本轮验证结果
- 成功

---

### 6.1.3 添加收藏
- **方法：** `POST`
- **路径：** `/second-hand/commodity/collection/add/{pid}`
- **是否需要登录：** 是

#### 本轮验证结果
- 成功

### 安卓接入建议
- 商品列表可优先接入
- 发布、收藏属于登录后能力

---

## 6.2 竞赛基础接口

### 6.2.1 竞赛帖子分页
- **方法：** `POST`
- **路径：** `/second-hand/competitionPost/page`
- **是否需要登录：** 是
- **Content-Type：** `application/json`

### 请求示例

```json
{
  "status": 0,
  "page": 1,
  "pageSize": 10
}
```

### 本轮验证结果
- 接口可达
- 本轮返回业务文案：`服务器繁忙，请稍后再试`

### 安卓接入建议
- 路径以当前实测网关路径为准：`/second-hand/competitionPost/page`
- 页面先按可请求能力接入
- 建议安卓端对“服务器繁忙，请稍后再试”做普通错误提示，不要当作鉴权失败

---

## 7. 常见错误码与联调说明

| code | 含义 | 安卓端建议 |
| --- | --- | --- |
| 200 / 0 | 成功 | 正常解析 `data` |
| 401 | 登录失效 / 会话过期 | 清理 token，重新登录 |
| 500 | 服务端异常 | 展示统一错误提示 |

补充说明：
- 本轮多个接口成功但返回空数据，这是当前线上数据状态，不是接口不可用
- `searchCourses`、`getEmptyClassrooms` 当前都已实测成功
- `getCalendar` 当前返回 `null` 属于正常空态

---

## 8. 本轮交付结论

### 已实测通过，可直接交给安卓联调
- `POST /wust-basic/UnderGraduateStudent/login`
- `GET /wust-basic/UnderGraduateStudent/searchCourses`
- `GET /wust-basic/UnderGraduateStudent/getEmptyClassrooms`
- `GET /wust-basic/operationLog/list/publishedButAndroid`
- `GET /wust-basic/admin/common/getCarousels`
- `GET /wust-basic/admin/common/getCalendar`
- 二手基础接口
- 竞赛基础接口

### 本轮跳过，不纳入安卓主交付范围
- 校园猫
- 校园搭子

---

## 9. 安卓端接入建议顺序

推荐安卓端按下面顺序联调：

1. 登录
2. 首页公共展示
   - 安卓公告
   - 轮播图
   - 校历
3. 本科生工具
   - 搜课程
   - 空教室
4. 二手基础列表与发布
5. 竞赛分页

---

## 10. 给安卓端的最简说明

安卓端统一使用：

```text
http://your-gateway-domain:8088
```

优先联调路径：

```text
POST /wust-basic/UnderGraduateStudent/login
GET  /wust-basic/UnderGraduateStudent/searchCourses
GET  /wust-basic/UnderGraduateStudent/getEmptyClassrooms
GET  /wust-basic/operationLog/list/publishedButAndroid
GET  /wust-basic/admin/common/getCarousels
GET  /wust-basic/admin/common/getCalendar
POST /second-hand/competitionPost/page
```

当前线上空态说明：
- 搜课程：成功，但当前返回空数组
- 空教室：成功，但当前返回空数组
- 安卓公告：成功，但当前返回空数组
- 轮播图：成功，但当前返回空数组
- 校历：成功，但当前返回 `null`

这些都按“接口可用、当前无内容”处理即可。
