import SwiftUI
import WebKit

struct TrainingPlanView: View {
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @State private var htmlContent: String? = nil
    @AppStorage("TrainingPlan") private var previousHtmlContent: String = ""  // 存储上次的 HTML 内容
    @State private var showNewCourseNotification = false
    @AppStorage("authData") private var authData: String = ""
    @AppStorage("cookie") private var cookie: String = ""  // 存储cookie
    @AppStorage("username") var username: String = ""
    @AppStorage("password") var password: String = ""
    
    var body: some View {
        ScrollView {
            ZStack{
                // 加载指示器
                if isLoading {
                    ProgressView("加载中...")
                        .progressViewStyle(CircularProgressViewStyle())
                        .padding()
                }
               
                // 显示HTML内容
                else if let htmlContent = htmlContent {
                    WebView(htmlContent: htmlContent)
                        .frame(height: 800)
                } else {
                    Text("暂无内容")
                        .padding()
                }
                
                // 数据更新通知
                if showNewCourseNotification {
                    Text("😃数据更新!")
                        .foregroundColor(.white)
                        .padding(12)
                        .background(Color.green.opacity(0.6))
                        .cornerRadius(8)
                        .shadow(radius: 5)
                        .transition(.opacity.combined(with: .scale))
                        .padding()
                }
                
                // 错误信息
                if let errorMessage = errorMessage {
                    Text("错误😫:\(errorMessage)")
                        .foregroundColor(.white)
                        .padding(12)
                        .background(Color.red.opacity(0.6))
                        .cornerRadius(8)
                        .shadow(radius: 5)
                        .transition(.opacity.combined(with: .scale))
                        .padding()
                }
            }
            .refreshable {
                fetchHTMLContent(forceReload: true)
            }
            .onAppear {
                fetchHTMLContent(forceReload: false)
            }
        }
    }

    // 自动隐藏通知
    func refresh() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            withAnimation {
                showNewCourseNotification = false
            }
        }
    }
    
    // 自动隐藏错误信息
    func refreshError() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            withAnimation {
                errorMessage = nil
            }
        }
    }
    
    // 获取HTML内容主函数
    func fetchHTMLContent(forceReload: Bool = false, isRetry: Bool = false) {
        // 如果不是强制刷新且已有缓存内容，直接使用缓存
        if !forceReload, !isRetry, let _ = htmlContent {
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        // 研究生：直接使用研究生API获取培养方案
        if Identify.chooseIdentify == Identify.Graduate {
            fetchGraduateCultivationPlan()
            return
        }
        
        // 本科生：使用原有的Cookie逻辑
        guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.getTrainingPlanPage)") else {
            isLoading = false
            errorMessage = "接口地址无效"
            refreshError()
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue(cookie, forHTTPHeaderField: "Cookie")
        request.timeoutInterval = 20
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                // 处理响应，区分首次请求和重试请求
                handleResponse(data: data, response: response, error: error, isRetry: isRetry)
            }
        }
        task.resume()
    }
    
    // 研究生获取培养方案
    func fetchGraduateCultivationPlan() {
        guard !username.isEmpty, !password.isEmpty else {
            isLoading = false
            errorMessage = "用户名或密码为空"
            refreshError()
            return
        }
        
        let credentials = GraduateLoginCredentials(studentId: username, password: password)
        GraduateNetworkService.fetchCultivationPlan(credentials: credentials) { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let html):
                    // 检查内容是否更新
                    if self.htmlContent != html {
                        self.htmlContent = html
                        self.showNewCourseNotification = true
                        self.refresh()
                        self.previousHtmlContent = html // 保存到本地缓存
                    } else {
                        self.htmlContent = html // 即使内容相同也设置，确保显示
                    }
                case .failure(let error):
                    self.errorMessage = error.localizedDescription
                    self.refreshError()
                    // 如果有缓存内容，使用缓存
                    if !self.previousHtmlContent.isEmpty {
                        self.htmlContent = self.previousHtmlContent
                    }
                }
            }
        }
    }
    
    // 处理网络响应
    func handleResponse(data: Data?, response: URLResponse?, error: Error?, isRetry: Bool) {
        // 1. 处理网络错误（只在首次请求时处理）
        if let error = error {
            errorMessage = "网络错误: \(error.localizedDescription)"
            refreshError()
            // 网络错误时尝试使用缓存内容
            if !previousHtmlContent.isEmpty {
                htmlContent = previousHtmlContent
            }
            return
        }
        
        // 2. 处理HTTP响应状态
        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            // 如果是首次请求失败，尝试获取新cookie后重试
            if !isRetry {
                getNewCookieAndRetry()
            } else {
                // 重试后仍失败，提示登录失效
                errorMessage = "登录失效，请重新登录"
                refreshError()
            }
            return
        }
        
        // 3. 处理数据为空的情况
        guard let data = data else {
            errorMessage = "未获取到数据"
            refreshError()
            return
        }
        
        // 4. 解析JSON响应
        do {
            let response = try JSONDecoder().decode(Response.self, from: data)
            if response.code == 200, let newContent = response.data {
                // 检查内容是否更新
                if htmlContent != newContent {
                    htmlContent = newContent
                    showNewCourseNotification = true
                    refresh()
                    previousHtmlContent = newContent // 保存到本地缓存
                }
            } else {
                // 接口返回错误码
                if !isRetry {
                    getNewCookieAndRetry()
                } else {
                    errorMessage = "登录失效，请重新登录"
                    refreshError()
                }
            }
        } catch {
            // 解析错误
            if !isRetry {
                getNewCookieAndRetry()
            } else {
                errorMessage = "登录失效，请重新登录"
                refreshError()
            }
        }
    }
    
    // 获取新的Cookie并重试请求
    func getNewCookieAndRetry() {
        // 显示加载状态
        isLoading = true
        // 调用登录函数获取新cookie
        TrainingPlanView.getloginCookie(username: username, password: password) { success, result in
            DispatchQueue.main.async {
                isLoading = false
                if success {
                    // 更新cookie并重试请求
                    cookie = result;  
                    fetchHTMLContent(forceReload: true, isRetry: true)
                } else {
                    // 获取cookie失败
                    errorMessage = "登录失效，请重新登录"
                    refreshError()
                }
            }
        }
    }
    // 获取新的Cookie并重试请求
//    func getNewCookieAndRetry() {
//        isLoading = true
//        // 移除 [weak self]（因为TrainingPlanView是结构体，不支持弱引用）
//        TrainingPlanView.getloginCookie(username: username, password: password) { success, newCookie, errorMsg in
//            DispatchQueue.main.async {
//                self.isLoading = false  // 直接使用self（结构体中安全）
//                if success, let cookieStr = newCookie {
//                    //self.cookie = cookieStr
//                    self.fetchHTMLContent(forceReload: true, isRetry: true)
//                } else {
//                    self.errorMessage = errorMsg ?? "登录失效，请重新登录"
//                    self.refreshError()
//                }
//            }
//        }
//    }
    // 登录获取Cookie的静态方法
    static func getloginCookie(username: String, password: String, completion: @escaping (Bool, String) -> Void) {
        // 配置请求URL
        // 1. 配置请求URL（替换为你的后端接口地址）
        // 1. 根据用户类型（本科/研究生）确定正确的接口URL
        let urlString: String
        if Identify.chooseIdentify == Identify.Graduate {
            // 研究生登录接口
            urlString = "\(BasicValue.baseGetUrl)\(Method.GraduateLogin)"
        } else {
            // 本科生登录接口（默认）
            urlString = "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.UnderGraduateloginGetCookie)"
        }
    
        // 验证URL有效性（统一处理）
        guard let url = URL(string: urlString) else {
            completion(false, "接口地址无效: \(urlString)")
                return
        }
            
        // 研究生接口处理
        if Identify.chooseIdentify == Identify.Graduate {
            guard let gradUrl = URL(string: "\(BasicValue.graduateCourseBaseUrl)\(Method.GraduateLogin)") else {
                completion(false,"接口地址无效")
                return
            }
            // 使用研究生URL
            performLoginRequest(url: gradUrl, username: username, password: password, completion: completion)
        } else {
            // 本科生接口
            performLoginRequest(url: url, username: username, password: password, completion: completion)
        }
    }
    
    // 执行登录请求的私有方法
    private static func performLoginRequest(url: URL, username: String, password: String, completion: @escaping (Bool,  String) -> Void) {
        let params: [String: String] = [
            "username": username,
            "password": password
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: params) else {
            completion(false, "数据格式错误")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = jsonData
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 10
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    completion(false,error.localizedDescription)
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    completion(false, "请求服务器响应错误")
                    return
                }
                
                guard let data = data else {
                    completion(false, "无返回数据")
                    return
                }
                
                do {
                    let response = try JSONDecoder().decode(LoginResponse.self, from: data)
                    if response.code == 200 {
                        completion(true, response.data)
                    } else {
                        completion(false, response.message)
                    }
                } catch {
                    completion(false, "解析错误：\(error.localizedDescription)")
                }
//                do {
//                    // 直接解析为用户信息结构体
//                    let response = try JSONDecoder().decode(LoginResponse.self, from: data)
//                    // 解析成功即返回数据（无需判断code）
//                    completion(true, response,nil)
//                } catch {
//                    completion(false, nil,"解析错误：\(error.localizedDescription)")
//                }
            }
        }
        task.resume()
    }
}

// WebView显示HTML内容
struct WebView: UIViewRepresentable {
    var htmlContent: String
    
    func makeUIView(context: Context) -> WKWebView {
        return WKWebView()
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        uiView.loadHTMLString(htmlContent, baseURL: nil)
    }
}

// 响应数据模型
struct Response: Codable {
    let code: Int
    let message: String
    let data: String?
    let timestamp: Int
}

struct FirstDateResponse: Codable {
    let code: Int
    let message: String
    let data: FirstData
    let timestamp: Int
}

struct FirstData: Codable {
    let year: Int
    let month: Int
    let day: Int
}
