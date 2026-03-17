//
//  framebootcamp.swift
//  study_test
//
//  Created by wust_lh on 2025/6/26.
//
import SwiftUI
import UIKit
//import AVFoundation

@main
struct NewWustHelper: App {
    var body: some Scene {
        WindowGroup {
            framebootcamp().defaultAppStorage(UserDefaults(suiteName: "group.linghang.wustHelper")!)
        }
    }
}
// 通知管理器
class NotificationManager: ObservableObject {
    static let shared = NotificationManager()
    @Published var showAlert = false
    @Published var alertMessage = ""
    @Published var alertTitle = "通知"
    @Published var currentNotificationIndex = 0
    @Published var notifications: [OperationLog] = []
    @Published var dontRemindAgainIds: Set<Int> = []
    private init() {
        // 从UserDefaults加载不再提醒的通知ID
        if let savedIds = UserDefaults.standard.array(forKey: "dontRemindAgainIds") as? [Int] {
            dontRemindAgainIds = Set(savedIds)
        }
    }
    // 设置通知列表并开始展示
    func setNotifications(_ notifications: [OperationLog]) {
        // 过滤掉用户选择不再提醒的通知
        let filteredNotifications = notifications.filter { !dontRemindAgainIds.contains($0.id) }
        
        guard !filteredNotifications.isEmpty else {
            // 如果没有需要显示的通知，直接返回
            return
        }
        
        self.notifications = filteredNotifications
        self.currentNotificationIndex = 0
        self.showNextNotification()
    }
    
    // 显示下一个通知
    func showNextNotification() {
        guard currentNotificationIndex < notifications.count else {
            // 所有通知已显示完毕
            notifications.removeAll()
            return
        }
        
        let notification = notifications[currentNotificationIndex]
        alertTitle = notification.title
        alertMessage = notification.context
        showAlert = true
    }
    
    // 处理用户选择
    func handleUserChoice(dontRemindAgain: Bool) {
        if dontRemindAgain, currentNotificationIndex < notifications.count {
            let currentId = notifications[currentNotificationIndex].id
            dontRemindAgainIds.insert(currentId)
            // 保存到UserDefaults
            UserDefaults.standard.set(Array(dontRemindAgainIds), forKey: "dontRemindAgainIds")
        }
        currentNotificationIndex += 1
        showAlert = false
        // 短暂延迟后显示下一个通知
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            self.showNextNotification()
        }
    }
    
    // 清除所有不再提醒的设置
    func clearDontRemindSettings() {
        dontRemindAgainIds.removeAll()
        UserDefaults.standard.removeObject(forKey: "dontRemindAgainIds")
    }
}

struct mainframe: View{
    //身份URL的选择
    @AppStorage("chooseIdentify")
    private var storedChooseIdentify: String = "/UnderGraduate/Support"
    //解析服务器URL的选择
    @AppStorage("chooseParseIdentify")
    //存储的身份URL的选择
    private var storedChooseParseIdentify: String = "/mywustBasic/UnderGraduateStudent"
    //默认跳转的页面 确保每次用户重新打开跳转的就是课表
    @State var selectedTab:Int=2
    @AppStorage("authData") private var authData: String = ""
    @AppStorage("cookie") private var cookie: String = ""
    @AppStorage("isLoggedIn") private var isLoggedIn: Bool = false
    @State private var isLoadingInfo: Bool = false
    @AppStorage("courses") private var coursesData: Data = Data()
    @State private var infoPageWeb: String=" "
    @AppStorage("ID") var StudentNumber:String="202313201025"
    @AppStorage("savedGrades") private var savedGradesData: Data?
    @State private var networkError: String? = nil
    @AppStorage("studentInfo") private var studentInfoJson: String = ""
    @StateObject private var notificationManager = NotificationManager.shared
    
    var studentInfo: StudentInfo {
        get {
            guard let data = studentInfoJson.data(using: .utf8),
                  let decoded = try? JSONDecoder().decode(StudentInfo.self, from: data)
            else {
                return StudentInfo()  // 默认值
            }
            return decoded
        }
        set {
            if let data = try? JSONEncoder().encode(newValue),
               let jsonString = String(data: data, encoding: .utf8) {
                studentInfoJson = jsonString
            }
        }
    }
    private func saveStudentInfo(_ info: StudentInfo) {
        if let data = try? JSONEncoder().encode(info),
           let jsonString = String(data: data, encoding: .utf8) {
            studentInfoJson = jsonString
        }
    }
    // 1. 修改fetchInformation，添加完成回调（告知是否成功获取infoPageWeb）
    public func fetchInformation(completion: @escaping (Bool) -> Void) {
        isLoadingInfo = true
        networkError = nil
        guard cookie.isEmpty else {
            isLoadingInfo = false
            networkError = "未获取到登录凭证（cookie），请先登录"
            completion(false) // 失败回调
            return
        }
        // 构建URL
        guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.getStudentInfoPage)") else {
            isLoadingInfo = false
            networkError = "接口地址无效"
            completion(false)
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        // 将cookie添加到请求头中，使用标准的Cookie字段
        request.addValue(cookie, forHTTPHeaderField: "Cookie")
        request.timeoutInterval = 15//超时时间
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                self.isLoadingInfo = false
                if let error = error {
                    if let urlError = error as? URLError {
                        switch urlError.code {
                        case .notConnectedToInternet:
                            self.networkError = "网络未连接，请检查WiFi或移动数据"
                        case .timedOut:
                            self.networkError = "请求超时，请求服务器响应过慢"
                        case .cannotFindHost:
                            self.networkError = "无法连接服务器，请稍后重试"
                        default:
                            self.networkError = "网络错误：\(urlError.localizedDescription)"
                        }
                    } else {
                        self.networkError = "请求失败：\(error.localizedDescription)"
                    }
                    completion(false) // 网络错误，回调失败
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse else {
                    self.networkError = "请求服务器返回无效响应"
                    completion(false)
                    return
                }
                
                guard (200...299).contains(httpResponse.statusCode) else {
                    self.networkError = "请求服务器错误（状态码：\(httpResponse.statusCode)）"
                    completion(false)
                    return
                }
                
                guard let responseData = data, !responseData.isEmpty else {
                    self.networkError = "请求接口未返回有效数据"
                    completion(false)
                    return
                }
                do {
                    let decoder = JSONDecoder()
                    let infoPage = try decoder.decode(InfoCoursePageResponse.self, from: responseData)
                    
                    if infoPage.code == 200 {
                        self.infoPageWeb = infoPage.data // 保存页面数据
                        completion(true) // 成功获取，回调true
                    } else {
                        self.networkError = infoPage.message
                        completion(false) // 业务失败，回调false
                    }
                } catch {
                    let rawDataStr = String(data: responseData, encoding: .utf8) ?? "无法转换为字符串"
                    print("【解析错误】详情：\(error)")
                    print("【原始数据】：\(rawDataStr)")
                    self.networkError = "数据解析失败，请稍后重试"
                    completion(false)
                }
            }
        }
        task.resume()
    }
    public func parseInformation() {
        isLoadingInfo = true
        networkError = nil
        // 先检查infoPageWeb是否已获取
        guard !infoPageWeb.isEmpty else {
            isLoadingInfo = false
            networkError = "未获取到页面数据，请先执行fetch"
            return
        }
        guard !cookie.isEmpty else {
            isLoadingInfo = false
            networkError = "未获取到登录凭证（cookie），请先登录"
            return
        }
        
        
        // 构建POST请求的URL（不再包含webpage参数）
        guard let url = URL(string: "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.getStudentInfo)") else {
            isLoadingInfo = false
            networkError = "接口地址无效"
            return
        }
        
        // 构建请求体（JSON格式，仅包含webpage字段）
        let requestBody: [String: String] = ["webpage": infoPageWeb]
        guard let jsonData = try? JSONSerialization.data(withJSONObject: requestBody) else {
            isLoadingInfo = false
            networkError = "请求数据格式错误"
            return
        }
        
        // 配置POST请求
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")  // 声明JSON格式
        request.addValue("application/json", forHTTPHeaderField: "Accept")  // 期望接收JSON响应
        request.httpBody = jsonData  // 设置请求体
        request.timeoutInterval = 15
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                self.isLoadingInfo = false
                
                if let error = error {
                    if let urlError = error as? URLError {
                        switch urlError.code {
                        case .notConnectedToInternet:
                            self.networkError = "网络未连接，请检查WiFi或移动数据"
                        case .timedOut:
                            self.networkError = "解析超时，解析服务器响应过慢"
                        case .cannotFindHost:
                            self.networkError = "无法连接服务器，请稍后重试"
                        default:
                            self.networkError = "网络错误：\(urlError.localizedDescription)"
                        }
                    } else {
                        self.networkError = "解析失败：\(error.localizedDescription)"
                    }
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse else {
                    self.networkError = "解析服务器返回无效响应"
                    return
                }
                
                guard (200...299).contains(httpResponse.statusCode) else {
                    self.networkError = "解析服务器错误（状态码：\(httpResponse.statusCode)）"
                    return
                }
                
                guard let responseData = data, !responseData.isEmpty else {
                    self.networkError = "解析接口未返回有效数据"
                    return
                }
                
                do {
                    let decoder = JSONDecoder()
                    let infoResponse = try decoder.decode(InfoCourseResponse.self, from: responseData)
                    if infoResponse.code == 200 {
                        self.saveStudentInfo(infoResponse.data) // 解析成功后保存
                    } else {
                        self.networkError = infoResponse.message
                    }
                } catch {
                    let rawDataStr = String(data: responseData, encoding: .utf8) ?? "无法转换为字符串"
                    print("【解析错误】详情：\(error)")
                    print("【原始数据】：\(rawDataStr)")
                    self.networkError = "数据解析失败，请稍后重试"
                }
            }
        }
        task.resume()
    }
    var body: some View{
        ZStack(){
            if isLoadingInfo&&networkError==nil{
                ProgressView("获取用户信息中...")
            }
            TabView(selection: $selectedTab){
                mainframecamp().tabItem{
                    Image(systemName: "house")
                    Text("主页")
                }.tag(0)
                volunteer().tabItem{
                    Image(systemName: "person.badge.shield.checkmark.fill")
                    Text("志愿者")
                }.tag(1)
                courseframe().tabItem{
                    Image(systemName: "note.text")
                    Text("课程表")
                }.tag(2)
                NewsView().tabItem{
                    Image(systemName: "newspaper")
                    Text("校园")
                }.tag(3)
                Me(onLoginOut: {
                    // 1. 清除用户认证相关缓存
                    authData = ""
                    isLoggedIn = false
                    // 2. 清除课程成绩缓存
                    savedGradesData = nil
                    // 3. 清除学号等用户信息缓存
                    studentInfoJson=""
                    StudentNumber = ""
                    coursesData=Data()
                }).tabItem{
                    Image(systemName: "person.fill")
                    Text("我")
                }.tag(4)
            } // 全局Alert通知 - 修改为支持多个选项
            .alert(isPresented: $notificationManager.showAlert) {
                Alert(
                    title: Text(notificationManager.alertTitle),
                    message: Text(notificationManager.alertMessage),
                    primaryButton: .default(Text("不再提醒")) { // 核心操作 → LTR 下居右
                        notificationManager.handleUserChoice(dontRemindAgain: true)
                    },
                    secondaryButton: .default(Text("确认")) { // 次要操作 → LTR 下居左
                        notificationManager.handleUserChoice(dontRemindAgain: false)
                    }
                )
            }.task {
                // 从 AppStorage 恢复标识符到全局变量
                Identify.chooseIdentify = storedChooseIdentify
                Identify.chooseParseIdentify = storedChooseParseIdentify
                // 获取用户信息
                fetchInformation { fetchSuccess in
                    if fetchSuccess {
                        self.parseInformation()
                    }
                }
                // 获取通知列表
                fetchOperationLogs()
            }
        }
        // 3. 在task中按顺序调用：先fetch，成功后再parse
        .task {
            // 从 AppStorage 恢复标识符到全局变量
            Identify.chooseIdentify = storedChooseIdentify
            Identify.chooseParseIdentify = storedChooseParseIdentify
            // 获取用户信息
            fetchInformation { fetchSuccess in
                if fetchSuccess {
                    self.parseInformation()
                }
            }
        }
    }

    // 添加获取通知列表的方法
    func fetchOperationLogs() {
        guard !cookie.isEmpty else {
            print("未获取到登录凭证（cookie），无法获取通知")
            return
        }
        // 构建URL
        guard let url = URL(string: "\(BasicValue.baseParseUrl)/mywustBasic/operationLog/list/publishedButIos") else {
            print("通知接口地址无效")
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")  // 声明JSON格式
        request.addValue("application/json", forHTTPHeaderField: "Accept")  //
        request.timeoutInterval = 15
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("获取通知失败: \(error.localizedDescription)")
                    return
                }
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    print("通知服务器返回错误响应")
                    return
                }
                guard let responseData = data, !responseData.isEmpty else {
                    print("通知接口未返回有效数据")
                    return
                }
                do {
                    let decoder = JSONDecoder()
                    let operationLogResponse = try decoder.decode(OperationLogListResponse.self, from: responseData)
                    
                    if operationLogResponse.code == 200 {
                        // 成功获取通知，传递给NotificationManager
                        NotificationManager.shared.setNotifications(operationLogResponse.data)
                    } else {
                        print("获取通知失败: \(operationLogResponse.message)")
                    }
                } catch {
                    #if DEBUG
                    let rawDataStr = String(data: responseData, encoding: .utf8) ?? "无法转换为字符串"
                    print("【通知解析错误】详情：\(error)")
                    print("【原始数据】：\(rawDataStr)")
                    #endif
                }
            }
        }
        task.resume()
    }
}
struct framebootcamp: View {
    //判断是否登录
    @State var showmainframe: Bool = false
    @AppStorage("isLoggedIn") private var isLoggedIn: Bool = false
    var body: some View {
        ZStack {
            if isLoggedIn {
                mainframe()
                    .transition(.move(edge: .trailing))
                    // 使用新的动画修饰符，绑定到isLoggedIn状态
                    .animation(.spring(), value: isLoggedIn)
            } else {
                loginframe()
            }
        }
        .onAppear {
            // 在onAppear中更新状态时也应该使用动画
            withAnimation(.spring()) {
                showmainframe = isLoggedIn
            }
        }
    }
}
#Preview {
    framebootcamp()
}
struct loginframe: View {
    @State private var isInputBoxShow  = false
    static var cookie: String? // 静态属性，属于类本身
    // 新增：用于存储登录成功后的 Cookie（供外部调用者获取）
        //var cookie: String = ""
    static var StudentNumber: String? // 静态属性
    @State var showalter:Bool=false
    @State var altertype:myalter?=nil
    @State var signal:Int=0
    @State var showp:Bool=false
    @State var showproblem=false
    @State var showRegisterErrorAlert: Bool = false  // 注册系统错误弹窗
    @AppStorage("username") var username:String=""
    @AppStorage("password") var password:String=""
    @AppStorage("chooseIdentify")
    private var storedChooseIdentify: String = "/UnderGraduate/Support"
    @AppStorage("chooseParseIdentify")
    private var storedChooseParseIdentify: String = "/mywustBasic/UnderGraduateStudent"
    //身份按钮的颜色
    @State var choosecolor:Color=Color.green
    //没有选中身份的颜色
    @State var notchoosecolor:Color=Color.brown.opacity(0.7)
    //第一个身份按钮的颜色
    @State var firstcolor:Color=Color.green.opacity(0.7)
    //第二个身份按钮的颜色
    @State var secondcolor:Color=Color.brown.opacity(0.7)
    //本系统的token
    @AppStorage("authData") private var authData: String = ""  // 非可选类型，默认空字符串
    //登录教务系统的cookie
    @AppStorage("cookie") private var cookie: String = ""  // 非可选类型，默认空字符串
    //是否登录
    @AppStorage("isLoggedIn") private var isLoggedIn: Bool = false  // 存储登录状态
    //学号
    @AppStorage("ID") var StudentNumber:String="202313201025"
    
    //样式
    // 阴影色：浅色用橙色低透明度，深色用橙色高透明度
    private var inputShadowColor: Color {
        colorScheme == .dark ? Color.white.opacity(0.3) : Color.orange.opacity(0.3)
    }
    // 1. 定义动态颜色（核心：区分深色/浅色模式的颜色）
    // 背景色：浅色用白色，深色用深灰色
    private var inputBgColor: Color {
        colorScheme == .dark ? Color(red: 0.15, green: 0.15, blue: 0.15) : Color.white
    }
    // 输入框文字色：浅色用黑色，深色用白色
    private var inputTextColor: Color {
        colorScheme == .dark ? .white : .black
    }
    // 占位符文字色：浅色用浅灰，深色用深灰（确保与背景有足够对比度）
    private var placeholderColor: Color {
        colorScheme == .dark ? Color(red: 0.5, green: 0.5, blue: 0.5) : Color(red: 0.7, green: 0.7, blue: 0.7)
    }
    @Environment(\.colorScheme) var colorScheme
    //输入文本的颜色
    private var textColor: Color {
        colorScheme == .dark ? .black : .white
    }
    // 新增：网络请求相关状态
    @State private var isLoading: Bool = false  // 加载状态
    @State private var networkError: String? = nil  // 网络错误信息
    @AppStorage("studentInfo") private var studentInfoJson: String = ""
    var studentInfo: StudentInfo {
        get {
            guard let data = studentInfoJson.data(using: .utf8),
                  let decoded = try? JSONDecoder().decode(StudentInfo.self, from: data)
            else {
                return StudentInfo()  // 默认值
            }
            return decoded
        }
        set {
            if let data = try? JSONEncoder().encode(newValue),
               let jsonString = String(data: data, encoding: .utf8) {
                studentInfoJson = jsonString
            }
        }
    }
    // MARK: - 错误弹窗计算属性
    private var errorIcon: String {
        guard let errorMessage = networkError else { return "exclamationmark.triangle" }
        
        if errorMessage.contains("网络连接问题") || errorMessage.contains("网络连接") {
            return "wifi.slash"
        } else if errorMessage.contains("服务器") {
            return "server.rack"
        } else if errorMessage.contains("密码错误") {
            return "person.badge.key"
        } else {
            return "exclamationmark.triangle"
        }
    }
    
    private var errorColor: Color {
        guard let errorMessage = networkError else { return .orange }
        
        if errorMessage.contains("网络连接问题") || errorMessage.contains("网络连接") {
            return .orange
        } else if errorMessage.contains("服务器") {
            return .red
        } else if errorMessage.contains("密码错误") {
            return .purple
        } else {
            return .orange
        }
    }
//    @State private var player: AVPlayer?
//
//    func playMusic() {
//        // URL 进行字符编码，确保特殊字符被正确处理
//        guard var urlString = "https://er-sycdn.kuwo.cn/3a779e5bd98e463292f9f2040f07bb62/691ec718/resource/30106/trackmedia/M500002usg9o4GTAKf.mp3?bitrate$128&from=vip".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
//            print("无效的URL")
//            return
//        }
//
//        guard let url = URL(string: urlString) else {
//            print("无效的URL")
//            return
//        }
//        
//        print("尝试播放的音频 URL: \(url.absoluteString)")
//        
//        let player = AVPlayer(url: url)
//        
//        // 监听 AVPlayer 错误通知
//        NotificationCenter.default.addObserver(forName: .AVPlayerItemFailedToPlayToEndTime, object: player.currentItem, queue: .main) { notification in
//            if let error = notification.userInfo?[AVPlayerItemFailedToPlayToEndTimeErrorKey] as? Error {
//                print("播放失败，错误信息：\(error.localizedDescription)")
//            }
//        }
//        
//        // 开始播放
//        player.play()
//        
//        print("音频播放开始")
//        self.player = player // 保存引用以便后续操作（如暂停）
//    }
    private var errorTitle: String {
        guard let errorMessage = networkError else { return "登录失败" }
        
        if errorMessage.contains("网络连接问题") || errorMessage.contains("网络连接") {
            return "网络连接失败"
        } else if errorMessage.contains("服务器") {
            return "服务器错误"
        } else if errorMessage.contains("密码错误") {
            return "账号密码错误"
        } else {
            return "登录失败"
        }
    }
    
    private var solutionItems: [String] {
        guard let errorMessage = networkError else {
            return ["检查网络连接", "确认账号密码正确", "稍后重试","联系管理员"]
        }
        
        if errorMessage.contains("网络连接问题") || errorMessage.contains("网络连接") {
            return [
                "检查手机网络连接",
                "尝试切换WiFi或移动数据",
                "确认网络信号良好",
                "稍后重试"
            ]
        } else if errorMessage.contains("服务器") {
            return [
                "服务器暂时繁忙，请稍后重试",
                "检查服务器状态",
                "联系技术支持"
            ]
        } else if errorMessage.contains("密码错误") {
            return [
                "确认账号密码为教务系统账号密码",
                "默认密码为学号或身份证后四位",
                "修改密码请去教务系统修改"
            ]
        } else if errorMessage.contains("用户被封禁") {
            return [
                "确认账号密码为教务系统账号密码",
                "确认此密码可以在教务系统上登录",
                "等待10分钟自动解封"
            ]
        }else {
            return [
                "检查网络连接",
                "确认账号密码正确",
                "稍后重试"
            ]
        }
    }
    enum myalter {
          case error
          case success
          case nullinformation
          case networkError(String)  // 新增：网络错误类型
    }
    // 更新Alert生成逻辑，支持网络错误
    func getalter() -> Alert {
            switch altertype {
            case .error:
                // 使用网络错误信息，如果没有则显示默认消息
                let errorMessage = networkError ?? "用户名或密码错误"
                return Alert(title: Text("错误"), message: Text(errorMessage))
            case .success:
                return Alert(
                    title: Text("成功"),
                    message: Text("登录成功！！！"),
                    dismissButton: .default(Text("ok"), action: {
                        isLoggedIn = true  // 登录成功后跳转主界面
                    })
                )
            case .networkError(let msg):
                return Alert(title: Text("网络错误"), message: Text(msg))
            case .nullinformation:
                return Alert(title: Text("错误"), message: Text("用户名或密码不能为空"))
            default:
                return Alert(title: Text("未知错误"))
            }
    }

    // 初始化身份状态
    func initializeIdentityState() {
        // 根据存储的身份标识初始化UI状态
        if storedChooseIdentify == Identify.UnderGraduate {
            signal = 0
            firstcolor = choosecolor
            secondcolor = notchoosecolor
        } else if storedChooseIdentify == Identify.Graduate {
            signal = 1
            secondcolor = choosecolor
            firstcolor = notchoosecolor
        } else {
            // 默认本科生
            signal = 0
            firstcolor = choosecolor
            secondcolor = notchoosecolor
            storedChooseIdentify = Identify.UnderGraduate
            storedChooseParseIdentify = Identify.ParseUnderGraduate
        }
        // 确保全局变量同步
        Identify.chooseIdentify = storedChooseIdentify
        Identify.chooseParseIdentify = storedChooseParseIdentify
    }
    func changesignal(choosenumber: Int) {
        // 使用动画包装状态更新，确保UI立即响应
        withAnimation(.easeInOut(duration: 0.3)) {
            signal = choosenumber
            switch signal {
            case 0:
                // 本科生
                firstcolor = choosecolor
                secondcolor = notchoosecolor
                // 更新 AppStorage 中的标识
                storedChooseIdentify = Identify.UnderGraduate
                storedChooseParseIdentify = Identify.ParseUnderGraduate
                // 更新全局变量
                Identify.chooseParseIdentify = Identify.ParseUnderGraduate
                Identify.chooseIdentify = Identify.UnderGraduate
            case 1:
                // 研究生
                secondcolor = choosecolor
                firstcolor = notchoosecolor
                // 更新 AppStorage 中的标识
                storedChooseIdentify = Identify.Graduate
                storedChooseParseIdentify = Identify.ParseGraduate
                // 更新全局变量
                Identify.chooseIdentify = Identify.Graduate
                Identify.chooseParseIdentify = Identify.ParseGraduate
            default:
                break
            }
        }
    }
    //登录武科大助手后台系统
    func parseRequest(completion: @escaping (Bool, String?) -> Void){
        parseRequestInternal(completion: completion, retryCount: 0, maxRetries: 1)
    }
    
    // MARK: - 内部请求方法（支持502重试）
    private func parseRequestInternal(completion: @escaping (Bool, String?) -> Void, retryCount: Int, maxRetries: Int) {
        // 先对 username 进行 URL 编码
        guard let encodedUsername = username.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            completion(false, "用户名包含无法编码的字符")
            return
        }
        // 拼接编码后的用户名
        let urlString = "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.ParseLogin)?username=\(encodedUsername)"
        // 再创建 URL
        guard let url = URL(string: urlString) else {
            completion(false, "接口地址无效")
            return
        }
        // 2. 配置 POST 请求（无 HTTP Body，删除 Content-Type 头）
        var request = URLRequest(url: url)
        request.httpMethod = "POST" // 保留 POST 方法
        request.timeoutInterval = 5  // 缩短超时时间，避免长时间阻塞（注册系统独立，不影响主流程）
        //创建异步任务
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            // 切换到主线程处理结果（网络请求在后台线程执行）
            DispatchQueue.main.async {
                // 打印请求信息的辅助函数
                let printRequestInfo = {
                    print("【parseRequest 请求信息】")
                    print("  URL: \(request.url?.absoluteString ?? "未知")")
                    print("  方法: \(request.httpMethod ?? "未知")")
                    print("  请求头:")
                    if let headers = request.allHTTPHeaderFields, !headers.isEmpty {
                        for (key, value) in headers.sorted(by: { $0.key < $1.key }) {
                            // 敏感信息部分隐藏
                            if key.lowercased() == "cookie" || key.lowercased() == "authorization" {
                                let valueStr = String(value)
                                if valueStr.count > 50 {
                                    print("    \(key): \(String(valueStr.prefix(50)))...")
                                } else {
                                    print("    \(key): \(valueStr)")
                                }
                            } else {
                                print("    \(key): \(value)")
                            }
                        }
                    } else {
                        print("    (无)")
                    }
                    if let body = request.httpBody {
                        if let bodyStr = String(data: body, encoding: .utf8) {
                            print("  请求体: \(bodyStr)")
                        } else {
                            print("  请求体: \(body.count) 字节（二进制数据）")
                        }
                    } else {
                        print("  请求体: (无)")
                    }
                }
                
                if let error = error {
                    // 网络错误（如无网络、超时等）- 注册系统独立，失败不影响主流程
                    printRequestInfo()
                    print("【parseRequest 注册系统网络错误】错误信息：\(error.localizedDescription)")
                    print("【parseRequest】注册系统失败不影响登录和其他功能")
                    if let data = data, let rawDataStr = String(data: data, encoding: .utf8) {
                        print("【parseRequest 原始数据】：\(rawDataStr)")
                    } else if let data = data {
                        print("【parseRequest 原始数据（无法解析为字符串）】：\(data.count) 字节")
                        print("【parseRequest 原始数据（十六进制）】：\(data.map { String(format: "%02x", $0) }.joined())")
                    }
                    // 注册系统失败不影响主流程，只返回失败状态用于显示提示
                    completion(false, error.localizedDescription)
                    return
                }
                
                // 检查HTTP响应状态码
                if let httpResponse = response as? HTTPURLResponse {
                    if !(200...299).contains(httpResponse.statusCode) {
                        printRequestInfo()
                        print("【parseRequest HTTP错误】状态码：\(httpResponse.statusCode)")
                        if let data = data, let rawDataStr = String(data: data, encoding: .utf8) {
                            print("【parseRequest 原始数据】：\(rawDataStr)")
                        } else if let data = data {
                            print("【parseRequest 原始数据（无法解析为字符串）】：\(data.count) 字节")
                            print("【parseRequest 原始数据（十六进制）】：\(data.map { String(format: "%02x", $0) }.joined())")
                        }
                        
                        // 如果是502错误且还有重试次数，则等待3秒后重试（注册系统独立，失败不影响主流程）
                        if httpResponse.statusCode == 502 && retryCount < maxRetries {
                            print("【parseRequest 注册系统】检测到502错误，等待3秒后自动重试（第\(retryCount + 1)/\(maxRetries)次）...")
                            DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                                self.parseRequestInternal(completion: completion, retryCount: retryCount + 1, maxRetries: maxRetries)
                            }
                            return
                        }
                        
                        print("【parseRequest 注册系统】服务器错误（状态码：\(httpResponse.statusCode)），不影响主流程")
                        completion(false, "服务器错误（状态码：\(httpResponse.statusCode)）")
                        return
                    }
                }
                // 解析服务器返回的JSON数据（注册系统独立，失败不影响主流程）
                guard let data = data else {
                    printRequestInfo()
                    print("【parseRequest 注册系统错误】无返回数据，不影响主流程")
                    completion(false, "无返回数据")
                    return
                }
                do {
                    let decoder = JSONDecoder()
                    let response = try decoder.decode(LoginResponse.self, from: data)
                    if response.code == 200 {
                    // 注册系统成功，仅保存 token（不设置登录状态，登录状态由主流程控制）
                    authData = response.data  // 直接存储 data 字段到 AppStorage
                    UserDefaults.standard.set(authData, forKey: "authToken")
                        print("🔑 本地存储的Token: \(authData ?? "未找到")")
                    // 注意：不再设置 isLoggedIn，登录状态应该由主流程控制
                    if retryCount > 0 {
                        print("【parseRequest】注册系统重试成功！")
                    }
                    print("【parseRequest】注册系统成功，已保存 authData")
                    completion(true, nil)
                    } else {
                        print("【parseRequest】注册系统失败：\(response.message)")
                        completion(false, response.message)
                    }
                } catch {
                    // 解析错误时打印原始数据（注册系统独立，失败不影响主流程）
                    printRequestInfo()
                    print("【parseRequest 注册系统解析错误】错误信息：\(error.localizedDescription)，不影响主流程")
                    if let rawDataStr = String(data: data, encoding: .utf8) {
                        print("【parseRequest 原始数据】：\(rawDataStr)")
                    } else {
                        print("【parseRequest 原始数据（无法解析为字符串）】：\(data.count) 字节")
                        print("【parseRequest 原始数据（十六进制）】：\(data.map { String(format: "%02x", $0) }.joined())")
                    }
                    completion(false, "解析错误：\(error.localizedDescription)")
                }
            }
        }
        task.resume()
    }
    /// 本科生专属登录请求（获取 Cookie）
    /// - Parameters:
    ///   - username: 本科生用户名/学号
    ///   - password: 登录密码
    ///   - completion: 登录结果回调（成功/失败 + 提示信息）
    func undergraduateLoginRequest(
        username: String,
        password: String,
        completion: @escaping (Bool, String) -> Void
    ) {
        // 1. 本科生专属接口 URL（直接硬编码，无需判断）
        let urlString = "\(BasicValue.baseGetUrl)\(Identify.UnderGraduate)\(Method.UnderGraduateloginGetCookie)"
        guard let url = URL(string: urlString) else {
            completion(false, "本科生接口地址无效: \(urlString)")
            return
        }
        
        // 2. 本科生专属请求参数（键为 "username"）
        let params: [String: String] = [
            "username": username,
            "password": password
        ]
        
        // 3. 参数转为 JSON Data（通用逻辑，但归属本科生方法）
        guard let jsonData = try? JSONSerialization.data(withJSONObject: params) else {
            completion(false, "本科生登录参数格式错误")
            return
        }
        
        // 4. 发送本科生专属请求（响应模型指定为 LoginResponse）
        let headers = ["Content-Type": "application/json"]
        NetworkErrorHandler.post(
            url: url,
            headers: headers,
            body: jsonData,
            responseType: LoginResponse.self // 本科生专属响应模型
        ) { result in
            switch result {
            case .success(let response):
                if response.code == 200 {
                    // 本科生登录成功：保存 Cookie 和学号（同时保存到实例属性和静态属性）
                    let cookieValue = response.data // LoginResponse 的 data 是 Cookie 字符串
                    self.cookie = cookieValue // 保存到 @AppStorage 实例属性
                    loginframe.cookie = cookieValue // 保存到静态属性（供外部调用）
                    loginframe.StudentNumber = username // 保存学号到静态属性
                    self.StudentNumber = username // 保存到实例属性
                    print("✅ 本科生登录成功，Cookie已保存: \(cookieValue.prefix(50))...")
                    completion(true, "本科生登录成功")
                } else {
                    // 本科生业务错误：分类错误信息
                    let errorMsg = self.categorizeLoginError(response.message)
                    networkError = errorMsg
                    completion(false, errorMsg)
                }
            case .failure(let networkError):
                // 本科生登录错误处理
                var errorMessage: String
                
                // 检查是否是超时错误
                if case .timeout = networkError {
                    errorMessage = "教务系统崩溃 请耐心等待官方修复😯"
                }
                // 检查是否是服务器错误（500或502）
                else if let statusCode = networkError.httpStatusCode {
                    switch statusCode {
                    case 500:
                        // 500错误：显示返回体的message，如果是timeout则显示"教务系统崩溃"
                        if let message = networkError.serverMessage {
                            if message.lowercased() == "timeout" {
                                errorMessage = "教务系统崩溃 请耐心等待官方修复😯"
                            } else {
                                errorMessage = message
                            }
                        } else {
                            errorMessage = "本科生11登录错误：\(networkError.userFriendlyMessage)"
                        }
                    case 502:
                        // 502错误：显示特定提示
                        errorMessage = "服务器错误，等一两分钟稍后重试即可🤔"
                    default:
                        errorMessage = "本科生登录错误：\(networkError.userFriendlyMessage)"
                    }
                } else {
                    // 其他错误（网络错误等）
                    errorMessage = "本科生登录错误：\(networkError.userFriendlyMessage)"
                }
                
                completion(false, errorMessage)
            }
        }
    }
    private func saveStudentInfo(_ info: StudentInfo) {
        if let data = try? JSONEncoder().encode(info),
           let jsonString = String(data: data, encoding: .utf8) {
            studentInfoJson = jsonString
        }
    }
    /// 研究生专属登录请求（获取学生信息）
    /// - Parameters:
    ///   - studentId: 研究生学号（对应参数键 student_id）
    ///   - password: 登录密码
    ///   - completion: 登录结果回调（成功/失败 + 提示信息）
    func graduateLoginRequest(
        studentId: String,
        password: String,
        completion: @escaping (Bool, String) -> Void
    ) {
        // 1. 研究生专属接口 URL（直接硬编码）
        let urlString = "\(BasicValue.graduateSystemBaseUrl)\(Method.GraduateLogin)"
        guard let url = URL(string: urlString) else {
            completion(false, "研究生接口地址无效: \(urlString)")
            return
        }
        
        // 2. 研究生专属请求参数（键为 "student_id"）
        let params: [String: String] = [
            "student_id": studentId, // 研究生参数键与本科生不同
            "password": password
        ]
        
        // 3. 参数转为 JSON Data（通用逻辑，归属研究生方法）
        guard let jsonData = try? JSONSerialization.data(withJSONObject: params) else {
            completion(false, "研究生登录参数格式错误")
            return
        }
        
        // 4. 发送研究生专属请求（响应模型指定为 LoginResponseYJS）
        let headers = ["Content-Type": "application/json"]
        NetworkErrorHandler.post(
            url: url,
            headers: headers,
            body: jsonData,
            responseType: LoginResponseYJS.self // 研究生专属响应模型
        ) {result in
            switch result {
            case .success(let response):
                if response.code == 200 {
                    // 研究生登录成功：从 StudentData 中提取信息并保存
                    let studentData = response.data // LoginResponseYJS 的 data 是 StudentData
                    self.StudentNumber = studentData.studentNumber // 从学生信息中拿学号（更准确）
                    self.username=studentId;
                    self.password=password;
                    let info:StudentInfo=StudentInfo(studentNumber: studentData.studentNumber, name: studentData.name, college: studentData.college, major: studentData.major, clazz: studentData.clazz, birthday: studentData.birthday, sex: studentData.sex, nationality: studentData.nationality, hometown: studentData.hometown, idNumber: studentData.idNumber)
                    self.saveStudentInfo(info)
                    let decoder = JSONDecoder()
                    completion(true, "研究生登录成功")
                } else {
                    // 研究生业务错误：分类错误信息
                    let errorMsg = self.categorizeLoginError(response.message)
                    completion(false, errorMsg)
                }
            case .failure(let networkError):
                // 研究生网络错误
                completion(false, "研究生登录网络错误：\(networkError.userFriendlyMessage)")
            }
        }
    }
    // 抽取通用逻辑：处理"登录→解析→弹窗"的完整流程（避免代码重复）
    private func handleLoginAndParse(loginSuccess: Bool, loginMessage: String) {
        if loginSuccess {
            // 登录成功：立即完成主流程，不等待注册系统
            self.isLoading = false
            self.altertype = .success
            self.showproblem = false
            self.showalter = true
            // 在后台异步调用注册系统，不影响主流程
            DispatchQueue.global(qos: .utility).async {
                self.parseRequest { parseSuccess, parseMessage in
                    DispatchQueue.main.async {
                        // 如果注册系统失败，只显示独立提示，不影响其他功能
                        if !parseSuccess {
                            self.showRegisterErrorAlert = true
                        }
                    }
                }
            }
        } else {
            // 登录失败：直接显示错误弹窗，结束加载
            self.isLoading = false
            self.altertype = .error
            self.networkError = loginMessage
            self.showalter = true
        }
    }
    private func performLogin() {
        // 清除之前的错误信息，重置加载状态
        networkError = nil
        isLoading = true
        
        // 根据用户类型，分支调用专属登录方法
        if Identify.chooseIdentify == Identify.Graduate {
            // 1. 研究生登录：调用研究生专属登录方法
            graduateLoginRequest(
                studentId: self.username, // 研究生用 "student_id" 字段，参数值取当前用户名（学号）
                password: self.password
            ) { loginSuccess, loginMessage in
                if loginSuccess {
                    // 登录成功：立即完成主流程，不等待注册系统
                    self.isLoading = false
                    self.altertype = .success
                    self.showproblem = false
                    self.showalter = true
                    
                    // 在后台异步调用注册系统，不影响主流程
                    DispatchQueue.global(qos: .utility).async {
                        self.parseRequest { parseSuccess, parseMessage in
                            DispatchQueue.main.async {
                                // 如果注册系统失败，只显示独立提示，不影响其他功能
                                if !parseSuccess {
                                    self.showRegisterErrorAlert = true
                                }
                            }
                        }
                    }
                } else {
                    // 登录失败：直接显示错误弹窗，结束加载
                    self.isLoading = false
                    self.altertype = .error
                    self.networkError = loginMessage
                    self.showalter = true
                }
            }
        } else {
            // 2. 本科生登录：调用本科生专属登录方法
            undergraduateLoginRequest(
                username: self.username, // 本科生用 "username" 字段
                password: self.password
            ) { loginSuccess, loginMessage in
                self.handleLoginResult(loginSuccess: loginSuccess, loginMessage: loginMessage)
            }
        }
    }

    // 抽取通用逻辑：处理登录结果（无论本科生还是研究生，结果处理逻辑一致）
    private func handleLoginResult(loginSuccess: Bool, loginMessage: String) {
        if loginSuccess {
            // 登录成功：立即完成主流程，不等待注册系统
            self.isLoading = false
            self.altertype = .success
            self.showproblem = false
            self.showalter = true
            
            // 在后台异步调用注册系统，不影响主流程
            DispatchQueue.global(qos: .utility).async {
                self.parseRequest { parseSuccess, parseMessage in
                    DispatchQueue.main.async {
                        // 如果注册系统失败，只显示独立提示，不影响其他功能
                        if !parseSuccess {
                            self.showRegisterErrorAlert = true
                        }
                    }
                }
            }
        } else {
            // 登录失败：直接显示错误弹窗，结束加载
            self.isLoading = false
            self.altertype = .error
            self.networkError = loginMessage
            self.showalter = true
        }
    }
    
    // MARK: - 错误分类方法
    private func categorizeLoginError(_ message: String) -> String {
        let lowercasedMessage = message.lowercased()
        
        // 检查是否是网络相关错误
        if lowercasedMessage.contains("网络") || 
           lowercasedMessage.contains("连接") || 
           lowercasedMessage.contains("超时") ||
           lowercasedMessage.contains("timeout") ||
           lowercasedMessage.contains("network") ||
           lowercasedMessage.contains("connection") {
            return "网络连接问题: \(message)"
        }
        
        // 检查是否是服务器相关错误
        if lowercasedMessage.contains("服务器") || 
           lowercasedMessage.contains("服务") || 
           lowercasedMessage.contains("server") ||
           lowercasedMessage.contains("500") ||
           lowercasedMessage.contains("502") ||
           lowercasedMessage.contains("503") ||
           lowercasedMessage.contains("504") {
            return "服务器问题: \(message)"
        }
        
        // 检查是否是认证相关错误
        if lowercasedMessage.contains("账号") || 
           lowercasedMessage.contains("密码") || 
           lowercasedMessage.contains("用户名") ||
           lowercasedMessage.contains("认证") ||
           lowercasedMessage.contains("登录") ||
           lowercasedMessage.contains("username") ||
           lowercasedMessage.contains("password") ||
           lowercasedMessage.contains("auth") {
            return "账号密码错误: \(message)"
        }
        
        // 其他错误
        return "登录失败: \(message)"
    }
    
    //登录界面
    var body: some View{
        ZStack(alignment:.top){
            // 2. 页面背景色适配：浅色用橙色淡背景，深色用纯黑背景
            let pageBgColor = colorScheme == .dark ? Color.black : Color.orange.opacity(0.1)
                      pageBgColor.edgesIgnoringSafeArea(.all)
            ScrollView(){
                VStack(alignment: .center, spacing: 0){
                    // 使用HStack和Spacer实现左右居中
                    HStack {
                        Spacer()
                        VStack(alignment: .center, spacing: 0){
                            HStack(){
                                Image("lhlogo").resizable().frame(width: 80,height: 80).clipShape(RoundedRectangle(cornerRadius: 30)).shadow(color:Color.orange.opacity(0.6),radius:10)
                                VStack(){
                                    Text("武科大助手").font(.system(size: 30))
                                    Text("随时随地 开启校园之旅").font(.system(size: 13));
                                }
                            }.padding(.top,60)
                            
                            HStack(){
                                Button{
                                    changesignal(choosenumber: 0)
                                    isInputBoxShow=false
                                }label: {
                                    RoundedRectangle(cornerRadius: 65)
                                        .fill(firstcolor.opacity(0.6))
                                        .frame(width: 100, height: 40)
                                        .overlay(
                                            Text("本科生")
                                                .foregroundColor(Color.white)
                                                .font(.system(size: 16, weight: .medium))
                                        )
                                        .scaleEffect(signal == 0 ? 1.05 : 1.0)
                                        .animation(.easeInOut(duration: 0.2), value: signal)
                                }
                                .buttonStyle(PlainButtonStyle())
                                
                                Button{
                                    changesignal(choosenumber: 1)
                                    isInputBoxShow = true
                                }label: {
                                    RoundedRectangle(cornerRadius: 65)
                                        .fill(secondcolor.opacity(0.7))
                                        .frame(width: 100, height: 40)
                                        .overlay(
                                            Text("研究生")
                                                .foregroundColor(Color.white)
                                                .font(.system(size: 16, weight: .medium))
                                        )
                                        .scaleEffect(signal == 1 ? 1.05 : 1.0)
                                        .animation(.easeInOut(duration: 0.2), value: signal)
                                    
                                }
                                .buttonStyle(PlainButtonStyle())
                                
                            }.padding(.top,40)
                                // 自定义输入框（解决占位符颜色问题）
                                                            CustomTextField(
                                                                text: $username,
                                                                placeholder: "输入学号",
                                                                textColor: inputTextColor,
                                                                placeholderColor: placeholderColor,
                                                                bgColor: inputBgColor,
                                                                shadowColor: inputShadowColor
                                                            )
                                                            .submitLabel(.next)
                                                            .frame(width: 260, height: 55)
                                                            .padding(.top,30)
                                
                                                            // 密码输入框（复用之前适配好的 PasswordInputField）
                                                            PasswordInputField(
                                                                password: $password,
                                                                // 传递动态颜色给密码输入框
                                                                inputBgColor: inputBgColor,
                                                                inputTextColor: inputTextColor,
                                                                placeholderColor: placeholderColor,
                                                                inputShadowColor: inputShadowColor
                                                            )
                                
                                                            Text("默认密码为学号或身份证后六位").foregroundColor(Color.gray).font(.system(size: 12))
                            HStack(spacing: 0){
                                Text("登录即表示您同意").foregroundColor(Color.gray).font(.system(size: 12)).padding(.top,30)
                                Button{
                                    showp.toggle()
                                }label: {
                                    Text("《武科大助手用户协议》").foregroundColor(Color.blue.opacity(0.7)).font(.system(size: 12)).padding(.top,30)
                                }
                            }
                            Button {
                                // 清空之前的错误信息
                                networkError = nil
                                // 简单校验输入不为空
                                guard !username.isEmpty, !password.isEmpty else {
                                    altertype = .nullinformation
                                    showalter.toggle()
                                    return
                                }
                                // 显示加载状态（整个流程开始）
                                isLoading = true
                                
                                // 核心修改：按用户类型调用对应的专属登录方法
                                if Identify.chooseIdentify == Identify.Graduate {
                                    // 简单校验输入不为空
                                    guard !username.isEmpty, !password.isEmpty else {
                                        altertype = .nullinformation
                                        showalter.toggle()
                                        return
                                    }
                                    // 研究生：调用研究生专属登录方法
                                    graduateLoginRequest(studentId: username, password: password) { loginSuccess, loginMessage in
                                        handleLoginAndParse(loginSuccess: loginSuccess, loginMessage: loginMessage)
                                    }
                                } else {
                                    // 本科生：调用本科生专属登录方法
                                    undergraduateLoginRequest(username: username, password: password) { loginSuccess, loginMessage in
                                        handleLoginAndParse(loginSuccess: loginSuccess, loginMessage: loginMessage)
                                    }
                                }
                            }
                                label:{
                                if isLoading {
                                    // 加载中显示活动指示器
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .frame(width: 270, height: 60)
                                        .background(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(AngularGradient(
                                                    gradient: Gradient(colors: [.green, .blue]),
                                                    center: .topLeading,
                                                    angle: .degrees(180+45)
                                                ))
                                        )
                                    
                                } else {
                                    // 正常状态显示登录按钮
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(AngularGradient(
                                            gradient: Gradient(colors: [.green, .blue]),
                                            center: .topLeading,
                                            angle: .degrees(180+45)
                                        ))
                                        .overlay(
                                            Text("登录")
                                                .foregroundColor(.white)
                                                .fontWeight(.bold)
                                        )
                                        .frame(width: 270, height: 60)
                                }
                                }.padding(.all, 10).padding(.horizontal,-3)
                                .disabled(isLoading)  // 加载中禁用按钮
                           
                            Button{
                                showproblem.toggle()
                            }label: {
                                Text("登录遇到问题？").foregroundColor(Color.gray).font(.system(size: 12)).padding(.top,20)
                            }
                            Spacer()
                            Text("copyright @ 领航工作室").foregroundColor(Color.gray.opacity(0.7)).fontWeight(.medium).padding(.bottom,10)
                                .alert(isPresented: $showalter,content: {
                                    getalter()                })
                                // 注册系统错误弹窗（独立，不影响其他功能）
                                .alert("提示", isPresented: $showRegisterErrorAlert) {
                                    Button("确定", role: .cancel) { }
                                } message: {
                                    Text("校内服务器错误 不影响其它功能")
                                }
                        }
                        Spacer()
                    }
                }
            }.scrollIndicators(.hidden)
            if showp {
                //协议弹窗
                ZStack(alignment: .top) { // 对齐方式改为顶部
                // 背景半透明黑色，点击可关闭
                Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
                .onTapGesture {
                    showp = false
                }
                .transition(.opacity) // 背景使用渐隐渐显过渡
                    // 使用 GeometryReader 获取屏幕尺寸
                GeometryReader { geo in
                        VStack(alignment: .leading, spacing: 0) {
                            // 内容容器，使用屏幕高度的 50%
                            RoundedRectangle(cornerRadius: 10)
                                .fill(Color.white)
                                .frame(width:350,height: geo.size.height * 0.6)
                                .overlay(
                                    ZStack(alignment: .top) {
                                        //底层框
                                        Capsule()
                                            .fill(Color.gray.opacity(0.3))
                                            .frame(width: 50, height: 5)
                                            .padding(.top, 15).zIndex(0)
                                        ScrollView {
                                                    VStack(alignment: .leading, spacing: 15) {
                                                        Text("用户协议")
                                                            .font(.largeTitle)
                                                            .fontWeight(.bold)
                                                            .padding(.top, 10)
                                                           
                                                        Text("欢迎使用 武科大助手！\n在使用本应用之前，请您仔细阅读本《用户协议》以及本应用的隐私政策。如果您同意这些条款，请继续使用本应用。如果您不同意，请立即停止使用本应用。")

                                                        Divider()

                                                        Text("1. 协议的接受与生效")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("1.1 协议生效\n您通过登录或使用本应用的任何服务即表示您同意并接受本《用户协议》及其更新版本。此协议对您与本应用的所有服务使用行为具有法律效力。")
                                                        Text("1.2 用户资格\n您必须根据您所在地区的法律，具有独立签署本协议的能力。如果您不符合上述条件，请立即停止使用本应用。")

                                                        Divider()

                                                        Text("2. 服务内容与使用限制")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("2.1 服务内容\n本应用提供如下服务：课表查询, 成绩查询, 组队发布, 校园搭子，二手平台等服务,")
                                                        Text("2.2 使用权限\n您同意不将本应用用于任何违法、侵权、欺诈、骚扰、恶意攻击等不当行为。您不得通过本应用进行任何形式的破坏性操作，如尝试获取未经授权的数据或进行网络攻击。")
                                                        Text("2.3 账户管理\n用户无需注册，只需输入学号密码即可使用某些功能。您应确保提供的信息真实、准确，并及时更新。您应对您的账户和密码的保密性负责，所有通过您的账户进行的活动均由您承担责任。")

                                                        Divider()

                                                        Text("3. 用户隐私与数据保护")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("3.1 隐私政策\n本应用会根据隐私政策收集、使用、存储您的个人信息。请您在使用本应用之前，仔细阅读我们的隐私政策，了解我们如何保护您的个人隐私。")
                                                        Text("3.2 数据安全\n我们采取合理的技术和管理措施来保障您的个人数据的安全。但请注意，没有任何一种技术能够完全防止数据泄露。因此，您在使用本应用时，应充分了解并自愿承担相关风险。")

                                                        Divider()

                                                        Text("4. 知识产权")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("4.1 版权归属\n本应用中的所有内容（包括但不限于文字、图片、音频、视频、软件及其源代码等）均为本应用或其授权方的知识产权，受法律保护。未经授权，您不得复制、修改、分发或以其他方式使用这些内容。")
                                                        Text("4.2 用户内容\n您上传、发布或通过本应用服务传输的任何内容（如评论、图片、视频等），您应保证自己拥有该内容的合法使用权，并且不会侵犯任何第三方的知识产权或隐私权。")

                                                        Divider()

                                                        Text("5. 责任声明")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("5.1 服务的中断与修改\n本应用有权随时修改、暂停或终止部分或全部服务，无需事先通知用户。本应用对因不可抗力或其他原因导致的服务中断、数据丢失等问题不承担责任。")
                                                        Text("5.2 用户责任\n您使用本应用时，应遵守相关法律法规及本协议的规定。因您违反本协议或法律法规而导致的任何损失，均由您自行承担。")
                                                        Text("5.3 免责条款\n对于因不可抗力因素（如自然灾害、网络故障、服务器故障等）导致的服务中断、信息丢失等情况，本应用不承担任何责任。")

                                                        Divider()

                                                        Text("6. 协议的修改与终止")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("6.1 协议修改\n本应用有权随时对本协议进行修改，修改后的协议将在本应用内公布，并立即生效。如果您继续使用本应用，则视为您已接受修改后的协议。")
                                                        Text("6.2 协议终止\n您可以随时停止使用本应用的服务，并注销您的账户。若您违反本协议的条款，本应用有权随时暂停或终止对您的服务。")

                                                        Divider()

                                                        Text("7. 法律适用与争议解决")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("7.1 法律适用\n本协议适用中华人民共和国法律。若您与本应用之间发生任何争议，应首先通过友好协商解决；若协商不成，任何一方可向本应用所在地的法院提起诉讼。")
                                                        Text("7.2 争议解决\n对于因本协议引发的任何争议，双方同意通过友好协商解决；协商不成时，应提交本协议签署地的有管辖权的法院进行处理。")

                                                        Divider()

                                                        Text("8. 其他条款")
                                                            .font(.title2)
                                                            .fontWeight(.bold)

                                                        Text("8.1 完全协议\n本《用户协议》构成您与本应用之间关于使用本应用服务的完整协议，并取代任何先前的协议。")
                                                        Text("8.2 可分性\n如果本协议中的任何条款被判定为无效或不可执行，该条款应视为可分的，不影响其他条款的有效性和可执行性。")

                                                        Divider()

                                                        Text("本协议的最终解释权归 武汉科技大学领航工作室/武科大助手 所有。")

                                                        Divider()

                                                        Text("联系信息\n如有任何问题或意见，您可以通过以下方式联系我们：")
                                                        Text("邮箱：1113292406@qq.com")
                                                    } .foregroundColor(Color.gray)
                                                    .padding(.horizontal)
                                        }.zIndex(1).padding(.top, 50)
                                    }
                                )
                                .padding(.leading,25).padding(.top,50)
                                .transition(.move(edge: .top)) // 从顶部滑入/滑出
                        }
                    }
                }
            }
            if showproblem{
                //问题弹窗
                    // 优化的错误弹窗
                    ZStack {
                        // 背景遮罩 - 确保完全覆盖屏幕
                        Color.black.opacity(0.4)
                            .ignoresSafeArea(.all)
                            .onTapGesture {
                                withAnimation(.spring(response: 0.5, dampingFraction: 0.8, blendDuration: 0)) {
                                    showproblem = false
                                }
                            }
                        
                        // 错误弹窗内容
                        VStack(spacing: 0) {
                            // 顶部拖动条
                            Capsule()
                                .fill(Color.gray.opacity(0.3))
                                .frame(width: 50, height: 5)
                                .padding(.top, 15)
                                .padding(.bottom, 20)
                            
                            // 错误图标和标题
                            VStack(spacing: 16) {
                                Image(systemName: errorIcon)
                                    .font(.system(size: 48))
                                    .foregroundColor(errorColor)
                                
                                Text(errorTitle)
                                    .font(.title2)
                                    .fontWeight(.bold)
                                    .foregroundColor(colorScheme == .dark ? .white : .black)
                                
                                if let errorMessage = networkError {
                                    Text(errorMessage)
                                        .font(.body)
                                        .foregroundColor(colorScheme == .dark ? .white.opacity(0.8) : .black.opacity(0.7))
                                        .multilineTextAlignment(.center)
                                        .lineLimit(3)
                                        .padding(.horizontal, 20)
                                }
                            }
                            .padding(.bottom, 24)
                            
                            // 解决方案
                            VStack(alignment: .leading, spacing: 12) {
                                Text("解决方案")
                                    .font(.headline)
                                    .foregroundColor(.blue)
                                    .padding(.bottom, 8)
                                
                                ForEach(solutionItems, id: \.self) { item in
                                    HStack(alignment: .top, spacing: 8) {
                                        Text("•")
                                            .foregroundColor(.blue)
                                            .font(.body)
                                        Text(item)
                                            .font(.body)
                                            .foregroundColor(colorScheme == .dark ? .white.opacity(0.8) : .black.opacity(0.7))
                                    }
                                }
                            }
                            .padding(.horizontal, 24)
                            .padding(.bottom, 24)
                            
                            // 操作按钮
                            HStack(spacing: 16) {
                                Button("重试") {
                                    // 重试逻辑：重新执行登录流程
                                    performLogin()
                                }
                                .buttonStyle(ModernRetryButtonStyle())
                                
                                Button("关闭") {
                                    withAnimation(.spring(response: 0.5, dampingFraction: 0.8, blendDuration: 0)) {
                                        showproblem = false
                                    }
                                }
                                .buttonStyle(ModernDismissButtonStyle())
                            }
                            .padding(.horizontal, 24)
                            .padding(.bottom, 24)
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(.ultraThinMaterial)
                                .background(
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(colorScheme == .dark ? Color.black.opacity(0.8) : Color.white.opacity(0.95))
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 20)
                                        .stroke(errorColor.opacity(0.3), lineWidth: 1)
                                )
                        )
                        .shadow(color: Color.black.opacity(0.2), radius: 20, x: 0, y: 10)
                        .padding(.horizontal, 20)
                        .zIndex(1) // 确保弹窗内容始终在最上层
                        .transition(.asymmetric(
                            insertion: .scale(scale: 0.8).combined(with: .opacity).combined(with: .move(edge: .bottom)),
                            removal: .scale(scale: 0.7).combined(with: .opacity).combined(with: .move(edge: .bottom))
                        ))
                    }
                    .zIndex(999) // 确保整个弹窗在最上层

                }
             
        }.onAppear(){
            //若没认证信息
            if authData==""{
                isLoggedIn=false
            }else{
                isLoggedIn=true
            }
//            playMusic()
            // 初始化身份状态
            initializeIdentityState()
        }
    }
}

// 设置视图 - 添加清除不再提醒设置的选项
// 修改后的通知列表响应模型
struct OperationLogListResponse: Codable {
    let code: Int
    let message: String
    let data: [OperationLog]
    let timestamp: Int
}

// 修改后的单个通知模型
struct OperationLog: Codable, Identifiable {
    let id: Int
    let title: String
    let context: String
    let status: Int
    let platform: Int
    let catogories: Int
    let createdAt: String
    let createdId: Int
    let updatedAt: String
}
// 5. 修复：自定义TextField组件（统一阴影样式，确保深色模式生效）
struct CustomTextField: View {
    @Binding var text: String
    let placeholder: String
    let textColor: Color
    let placeholderColor: Color
    let bgColor: Color
    let shadowColor: Color // 外部传递的动态阴影色
    
    // 新增：监听输入框焦点状态（是否被点击激活）
    @FocusState private var isInputFocused: Bool
    
    var body: some View {
        ZStack(alignment: .leading) {
            // 输入框背景：统一阴影参数（与密码输入框一致）
            RoundedRectangle(cornerRadius: 15)
                .fill(bgColor)
                .frame(width: 260, height: 55) // 与密码输入框高度统一（55）
                .shadow(
                    color: shadowColor,
                    radius: 10,
                    x: 0,
                    y: 10
                )
            // 占位符：仅当「输入框无内容」且「未获得焦点」时显示（居中对齐）
            if text.isEmpty && !isInputFocused {
                Text(placeholder)
                    .foregroundColor(placeholderColor)
                    .padding(.horizontal, 95) // 移除固定95，统一内边距
            }
            // 实际输入框：绑定焦点状态，控制占位符消失
            TextField("", text: $text)
                .foregroundColor(textColor)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 16) // 确保文字不贴边
                .focused($isInputFocused) // 绑定焦点状态
        }
    }
}// 6. 修改 PasswordInputField 组件（支持接收外部动态颜色）
struct PasswordInputField: View {
    @Binding var password: String
    @State private var showPassword = false
    @Environment(\.colorScheme) var colorScheme
    // 接收外部传递的动态颜色（若未传递则用默认值）
    let inputBgColor: Color?
    let inputTextColor: Color?
    let placeholderColor: Color?
    let inputShadowColor: Color? // 外部传递的动态阴影色
    
    // 新增：监听密码输入框焦点状态
    @FocusState private var isPasswordFocused: Bool
    
    // 内部动态颜色（优先使用外部传递的颜色，否则用默认逻辑）
    private var bgColor: Color {
        inputBgColor ?? (colorScheme == .dark ? Color(red: 0.15, green: 0.15, blue: 0.15) : Color.white)
    }
    private var textColor: Color {
        inputTextColor ?? (colorScheme == .dark ? .white : .black)
    }
    // 关键修复：使用外部传递的阴影色，无则用默认逻辑
    private var shadowColor: Color {
        inputShadowColor ?? (colorScheme == .dark ? Color.orange.opacity(0.5) : Color.orange.opacity(0.3))
    }
    
    var body: some View {
        ZStack(alignment: .trailing) { // 保持右侧按钮的对齐
            // 输入框背景：与CustomTextField统一样式
            RoundedRectangle(cornerRadius: 15)
                .fill(bgColor)
                .frame(width: 260, height: 55) // 与学号输入框高度统一（55）
                .shadow(
                    color: shadowColor,
                    radius: 10,
                    x: 0,
                    y: 10
                )
            
            // 占位符 + 输入框（使用居中对齐容器）
            ZStack(alignment: .center) {
                // 占位符：仅当「密码为空」且「未获得焦点」时显示
                if password.isEmpty && !isPasswordFocused {
                    Text("输入密码")
                        .foregroundColor(placeholderColor)
                        .padding(.horizontal, 16)
                }
                
                // 实际输入框（区分明文/密文，绑定焦点状态）
                if showPassword {
                    TextField("", text: $password)
                        .multilineTextAlignment(.center)
                        .foregroundColor(textColor)
                        .padding(.horizontal, 16) // 统一左右内边距
                        .focused($isPasswordFocused) // 绑定焦点
                } else {
                    SecureField("", text: $password)
                        .multilineTextAlignment(.center)
                        .foregroundColor(textColor)
                        .padding(.horizontal, 16) // 统一左右内边距
                        .focused($isPasswordFocused) // 绑定焦点
                }
            }
            .frame(width: 260, height: 55) // 与背景框大小一致
            // 密码可见性切换按钮（颜色适配）
            Button(action: {
                withAnimation {
                    showPassword.toggle()
                    // 切换可见性后，重新激活输入框焦点
                    DispatchQueue.main.async {
                        isPasswordFocused = true
                    }
                }
            }) {
                Image(systemName: showPassword ? "eye.fill" : "eye.slash.fill")
                    .foregroundColor(placeholderColor)
                    .padding(.trailing, 15)
            }
        }
        .padding(.bottom, 20)
        .padding(.top, 25)
    }
}
struct homeview: View {
    var body: some View {
        Color.red.frame(width: .infinity,height: .infinity)    }
}
struct volunteerView: View {
    var body: some View {
        Color.blue.frame(width: .infinity,height: .infinity)
    }
}

struct courseView: View {
    var body: some View {
        Color.green.frame(width: .infinity,height: .infinity)
    }
}

struct campusView: View {
    var body: some View {
        Text("123")
    }
}
struct me: View {
    var body: some View {
        Color.orange.frame(width: .infinity,height: .infinity)
    }
}
//本科生
struct LoginResponse: Codable {
    let code: Int
    let message: String
    let data: String
    let timestamp: TimeInterval
}
//研究生
struct LoginResponseYJS: Codable {
    let code: Int
    let message: String
    let data: StudentData
    let timestamp: Int64  // 改为 Int64，因为服务器返回的是毫秒级时间戳（如 1763120913414）
}

struct InfoCourseResponse: Codable {
    let code: Int
    let message: String
    let data: StudentInfo  // 课表数组
    let timestamp: TimeInterval
}


struct InfoCoursePageResponse: Codable {
    let code: Int
    let message: String
    let data: String  // 课表数组
    let timestamp: TimeInterval
}
