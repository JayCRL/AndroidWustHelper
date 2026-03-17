# AndroidWustHelper

## 课表二维码生成

### 数据封装逻辑

课表二维码的内容是一个经过 Base64 编码的字符串，用于在用户之间共享课表。生成逻辑如下：

1.  **构造原始字符串**:
    原始字符串由以下部分通过 `?+/` 分隔符拼接而成：
    `"kjbk?+/` + `学生姓名` + `?+/` + `学号` + `?+/` + `登录令牌(token)` + `?+/` + `学期"`

2.  **Base64 编码**:
    将构造的原始字符串进行 Base64 编码。

3.  **混淆**:
    为了增加安全性，编码后的字符串会经过混淆处理。在字符串的特定位置（第 0, 1, 2, 7, 8, 9 位）插入随机生成的 6 个数字。

### 解码逻辑

解码过程是编码的逆向操作：

1.  **移除混淆**:
    从字符串中移除用于混淆的 6 个随机数字。
2.  **Base64 解码**:
    对处理过的字符串进行 Base64 解码，得到原始的、以 `?+/` 分隔的字符串。
3.  **解析数据**:
    通过 `?+/` 分隔符解析出学生姓名、学号、登录令牌和学期信息。

---

## 空教室查询

应用提供了两种查询空教室的方式：旧版 WebView 和新版 API。

### 1. 旧版查询 (WebView)

此方法通过加载一个内嵌的网页来显示空教室信息。

*   **URL**: `https://wusthelper.wustlinghang.cn/class/emptyroom`
*   **参数**:
    *   `weekNum`: 周次 (例如, `weekNum=10`)

### 2. 新版查询 (API)

新版查询通过调用后端 API 来获取数据，提供了更灵活的查询方式。

*   **API 端点**: `/v2/clsroom/find-empty-classroom`
*   **请求方法**: GET
*   **请求参数**:
    *   `buildingName` (String): 教学楼名称 (例如, "教一楼")
    *   `areaNum` (String): 区域编号 (例如, "1")
    *   `campusName` (String): 校区名称 (例如, "青山校区")
    *   `week` (String): 周次
    *   `weekDay` (String): 星期几
    *   `section` (String): 节次 (例如, "1,2")

此外，还有一些辅助接口用于获取查询条件：

*   `/v2/clsroom/get-college-list`: 获取学院列表
*   `/v2/clsroom/get-course-name-list`: 获取课程名称列表
*   `/v2/clsroom/get-course-info`: 获取课程详细信息
*   `/v2/clsroom/search-in-college`: 在指定学院内搜索
*   `/v2/clsroom/search`: 全局搜索
# AndroidWustHelper
