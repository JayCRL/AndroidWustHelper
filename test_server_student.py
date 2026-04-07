import requests
import json
import time





# WustProject 全功能学生集成测试脚本 (生产环境)
# 运行环境: lyzyy.love

# 配置各服务地址
SERVER_HOST = "lyzyy.love"
BASE_URLS = {
    "basic": f"http://{SERVER_HOST}:8082",      # 基础服务 (鉴权/教务)
    "detail": f"http://{SERVER_HOST}:8085",     # 经验分享
    "secondhand": f"http://{SERVER_HOST}:8083", # 二手交易
    "competition": f"http://{SERVER_HOST}:8084" # 竞赛助手
}

# --- 用户凭据 (请替换为真实的测试学号和密码) ---
STUDENT_UID = "202212345678"
STUDENT_PWD = "your_password"

class AdvancedStudentTester:
    def __init__(self, uid, password):
        self.uid = uid
        self.password = password
        self.token = None
        # 强制禁用代理，防止 7890 端口超时错误
        self.session = requests.Session()
        self.session.proxies = {"http": None, "https": None}
        self.headers = {"Content-Type": "application/json"}

    def log_res(self, module, action, response):
        """统一打印结果"""
        status = "✅" if response.status_code < 400 else "❌"
        print(f"{status} [{module}] {action} - Status: {response.status_code}")
        try:
            res_json = response.json()
            if res_json.get("code") != 200 and res_json.get("code") != 0:
                print(f"   ⚠️ 业务详情: {res_json.get('msg') or res_json.get('message')}")
            return res_json
        except:
            return None

    def login(self):
        """1. 登录并获取 Token"""
        print(f"\n🚀 正在尝试登录基础服务...")
        url = f"{BASE_URLS['basic']}/UnderGraduateStudent/login"
        payload = {"username": self.uid, "password": self.password}
        try:
            r = self.session.post(url, json=payload, timeout=15)
            res = self.log_res("BASIC", "用户登录", r)
            if res and res.get("code") == 200:
                self.token = res.get("data")
                self.session.headers.update({"Authorization": f"Bearer {self.token}"})
                return True
        except Exception as e:
            print(f"🚫 登录失败: {e}")
        return False

    def test_basic_features(self):
        """2. 测试 wust-basic 教务功能"""
        print("\n--- [wust-basic] 正在测试教务数据接口 ---")
        # 个人信息
        self.log_res("BASIC", "获取个人信息", self.session.get(f"{BASE_URLS['basic']}/UnderGraduateStudent/getStudentInfo"))
        # 课表
        self.log_res("BASIC", "获取我的课表", self.session.get(f"{BASE_URLS['basic']}/UnderGraduateStudent/getCourseTable"))
        # 成绩
        self.log_res("BASIC", "获取我的成绩", self.session.get(f"{BASE_URLS['basic']}/UnderGraduateStudent/getScore"))

    def test_detail_features(self):
        """3. 测试 wust-detail 经验分享"""
        print("\n--- [wust-detail] 正在测试经验分享功能 ---")
        # 发布内容
        payload = {
            "title": f"自动化测试_{int(time.time())}",
            "content": "这是一条由测试脚本自动发布的经验心得。",
            "category": "STUDY",
            "tags": ["测试", "自动发布"]
        }
        self.log_res("DETAIL", "发布经验贴", self.session.post(f"{BASE_URLS['detail']}/api/detail/publish", json=payload))
        # 列表查询
        self.log_res("DETAIL", "查询学习板块列表", self.session.get(f"{BASE_URLS['detail']}/api/detail/page", params={"category": "STUDY"}))

    def test_secondhand_features(self):
        """4. 测试 wust-second-hand 二手交易"""
        print("\n--- [wust-second-hand] 正在测试二手交易功能 ---")
        # 发布商品
        payload = {
            "name": "测试教材",
            "price": 99.0,
            "description": "脚本自动发布商品描述",
            "category": "书籍",
            "location": "黄家湖校区"
        }
        publish_res = self.log_res("SECONDHAND", "发布二手商品", self.session.post(f"{BASE_URLS['secondhand']}/commodity/publish", json=payload))
        # 浏览全量商品 (根据常见路径)
        self.log_res("SECONDHAND", "查询商品列表", self.session.get(f"{BASE_URLS['secondhand']}/commodity/show/all"))

    def test_competition_features(self):
        """5. 测试 wust-competition 竞赛助手"""
        print("\n--- [wust-competition] 正在测试竞赛助手功能 ---")
        # 发布竞赛帖子
        payload = {
            "title": "寻找Python高手",
            "competitionName": "蓝桥杯",
            "requirement": "熟悉爬虫与后端",
            "content": "希望能组建一支强力队伍"
        }
        r = self.session.post(f"{BASE_URLS['competition']}/competitionPost", json=payload)
        res = self.log_res("COMPETITION", "发布竞赛组队贴", r)
        
        # 浏览帖子列表
        self.log_res("COMPETITION", "获取竞赛贴列表", self.session.get(f"{BASE_URLS['competition']}/competitionPost"))

def run_all():
    print("="*50)
    print("   WustProject 学生端功能全量压力测试")
    print(f"   目标服务器: {SERVER_HOST}")
    print("="*50)
    
    tester = AdvancedStudentTester(STUDENT_UID, STUDENT_PWD)
    
    if tester.login():
        tester.test_basic_features()
        tester.test_detail_features()
        tester.test_secondhand_features()
        tester.test_competition_features()
        print("\n✨ 所有模块功能测试执行完毕。")
    else:
        print("\n🛑 鉴权失败，请检查学号、密码以及 wust-basic 服务状态。")

if __name__ == "__main__":
    run_all()
