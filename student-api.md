# WustChat 学生端 API 文档

## 1. 基本信息

### 服务说明
WustChat 提供校园知识问答与学生投稿能力，移动端学生用户可携带登录后的 Bearer Token 调用接口。

### Base URL
本地开发示例：

```text
http://localhost:8096
```

如果走网关，则以网关实际地址为准，例如：

```text
http://localhost:8088/wust-chat
```

---

## 2. 认证方式

### 认证说明
当前学生端接口需要登录后访问，请在请求头中携带：

```http
Authorization: Bearer <token>
```

### Token 来源
`WustChat` 本身不负责学生登录。
学生登录应走现有统一登录链路 / `wust-mywust-basic` 登录体系，拿到 token 后再访问 WustChat。

### 请求头示例

```http
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 3. 通用返回格式

接口统一返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| code | number | 状态码，`200` 表示成功 |
| msg | string | 提示信息 |
| data | any | 具体业务数据 |

### 常见错误码

| code | 含义 |
|---|---|
| 200 | 成功 |
| 401 | 未登录或登录已失效 |
| 403 | 没有操作权限 |
| 500 | 服务异常 |

---

## 4. 接口列表

### 4.1 智能问答

#### 接口说明
根据学生输入的问题，返回知识库匹配后的回答。

#### 请求信息

- **URL**

```http
GET /api/rag/chat
```

- **认证要求**
需要 Bearer Token

- **请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
|---|---|---|---|---|
| question | string | 是 | query | 学生输入的问题 |

#### 请求示例

```http
GET /api/rag/chat?question=图书馆晚上几点关门
Authorization: Bearer <token>
```

#### 成功响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "question": "图书馆晚上几点关门",
    "matchedTag": "图书馆",
    "answer": "图书馆一般开放至晚上 22:00，具体以学校通知为准。",
    "hasStudentSource": false,
    "cacheHit": true
  }
}
```

#### data 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| question | string | 原始问题 |
| matchedTag | string | 匹配到的知识标签 |
| answer | string | 最终回答内容 |
| hasStudentSource | boolean | 回答是否命中了学生投稿内容 |
| cacheHit | boolean | 是否命中缓存 |

#### 失败响应示例

```json
{
  "code": 401,
  "msg": "Unauthorized",
  "data": null
}
```

---

### 4.2 学生投稿知识

#### 接口说明
学生可以提交新的知识内容，进入待审核区，由管理员审核后决定是否进入正式语料库。

#### 请求信息

- **URL**

```http
POST /api/rag/student/submit
```

- **认证要求**
需要 Bearer Token

- **Content-Type**

```http
application/json
```

#### 请求体字段

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| text | string | 是 | 投稿内容正文 |
| source | string | 否 | 来源说明，如“学生投稿”“移动端提交” |
| tag | string | 否 | 标签，如“选课”“宿舍”“图书馆” |
| expireAt | string | 否 | 失效时间，格式建议：`yyyy-MM-dd HH:mm` |

#### 请求示例

```json
{
  "text": "南湖校区图书馆自习室一般晚上10点关闭，考试周可能延长。",
  "source": "移动端学生投稿",
  "tag": "图书馆",
  "expireAt": "2026-06-30 22:00"
}
```

#### 成功响应示例

```json
{
  "code": 200,
  "msg": "已提交审核",
  "data": null
}
```

#### 失败响应示例

```json
{
  "code": 401,
  "msg": "Unauthorized",
  "data": null
}
```

---

## 5. 学生端调用建议

### 5.1 问答接口调用流程
1. 学生先通过统一登录体系获取 token。
2. 移动端调用 `GET /api/rag/chat?question=...`。
3. 在请求头中附带 `Authorization: Bearer <token>`。
4. 展示返回的 `data.answer`。

### 5.2 投稿接口调用流程
1. 学生填写知识内容。
2. 选择标签、来源、可选失效时间。
3. 调用 `POST /api/rag/student/submit`。
4. 提示“已提交审核”。

---

## 6. 移动端错误处理建议

### 401 未登录
说明 token 缺失、过期或无效。
建议移动端：
- 清理本地 token
- 跳转登录页

### 403 无权限
当前学生端一般不应频繁遇到；如果遇到，说明账号无访问权限或后端鉴权失败。
建议提示：

```text
暂无访问权限，请重新登录后重试
```

### 500 服务异常
建议统一提示：

```text
服务暂时不可用，请稍后再试
```

---

## 7. 示例请求

### GET 问答

```http
GET /api/rag/chat?question=奖学金什么时候评定
Authorization: Bearer <token>
```

### POST 投稿

```http
POST /api/rag/student/submit
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "text": "国家奖学金一般在秋季学期组织评定，具体时间以学院通知为准。",
  "source": "App学生投稿",
  "tag": "奖学金",
  "expireAt": ""
}
```

---

## 8. 当前不建议学生端使用的接口

以下接口是管理员接口，学生端不要调用：

- `POST /api/rag/config/api-key`
- `GET /api/rag/corpus`
- `GET /api/rag/pending-corpus`
- `POST /api/rag/pending-corpus/approve`
- `POST /api/rag/pending-corpus/reject`
- `POST /api/rag/corpus/delete`
- `POST /api/rag/ingest`
- `POST /api/rag/ingest-file`
