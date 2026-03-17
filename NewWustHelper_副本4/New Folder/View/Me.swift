import SwiftUI
struct Me: View {
    // 用户信息
    @AppStorage("authData") private var authData: String = ""
    @AppStorage("ID") private var studentNumber: String = "" // 学号缓存
    @AppStorage("studentInfo") private var studentInfoJson: String = ""
    @AppStorage("cookie") private var cookie: String = ""  // 非可选类型，默认空字符串
    @AppStorage("courses") private var coursesData: Data = Data()
    @State private var infoPageWeb: String=""
    @State private var showingSettings = false
    // 新增：获取当前颜色模式（用于深浅色适配）
    @Environment(\.colorScheme) var colorScheme

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
    @State private var isLoadingInfo: Bool = true
    @State private var errorMessage: String? = nil

    var onLoginOut: () -> Void

    // 菜单项模型
    struct MenuItem: Identifiable {
        let id = UUID()
        let icon: String
        let title: String
        let destination: AnyView
    }

    var menuItems: [MenuItem] {
        [
            MenuItem(icon: "feedback", title: "提交反馈", destination: AnyView(suggestionView())),
            MenuItem(icon: "qqgroup", title: "用户群聊", destination: AnyView(QQGroup())),
            MenuItem(icon: "update", title: "检查更新", destination: AnyView(CheckUpdateView())),
            MenuItem(icon: "GitHub", title: "关于软件", destination: AnyView(AboutAppView()))
        ]
    }
    @AppStorage("username") var username:String=""
    @AppStorage("password") var password:String=""
    var body: some View {
        NavigationView {
            ZStack {
                Color("bgcolor").edgesIgnoringSafeArea(.all)
                
                // 主内容：添加水平padding约束，避免卡片超出屏幕
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        headerView // 头像信息卡片
                        menuList   // 功能菜单列表
                        settingsButton // 设置按钮
                        logoutButton  // 退出登录按钮
                        Spacer()
                    }
                    .padding(.top, 30)
                    .padding(.horizontal, 20) // 关键：统一水平内边距，约束卡片位置
                }
                .refreshable {
                    // 下拉刷新功能
                    //refreshStudentInfo()
                }
                
                // 错误信息提示
                if let errorMessage = errorMessage {
                    VStack {
                        Spacer()
                        ErrorToast(
                            error: .unknown(errorMessage),
                            onDismiss: {
                                withAnimation(.easeOut(duration: 0.5)) {
                                    self.errorMessage = nil
                                }
                            }
                        )
                        Spacer()
                    }
                    .transition(.opacity)
                    .animation(.easeInOut(duration: 0.3), value: errorMessage)
                    .onAppear {
                        // 3秒后自动消失
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                            withAnimation(.easeOut(duration: 0.5)) {
                                self.errorMessage = nil
                            }
                        }
                    }
                }
                
            }
            .navigationBarHidden(true)
            .onAppear {
//                print("Me视图出现，准备发起请求")
                // 先尝试加载本地缓存数据（立即显示）
                loadCachedStudentInfo()
//
//                // 延迟一小段时间再刷新，避免立即覆盖缓存数据显示
//                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
//                    refreshStudentInfo()
//                }
            }
        }
        .navigationViewStyle(StackNavigationViewStyle()) // 强制使用Stack样式，避免导航冲突
    }

    // MARK: - 头部信息卡片（修复阴影+浅色不透明）
    private var headerView: some View {
        ZStack {
            // 玻璃效果背景：浅色模式提升底色不透明度（0.9→解决透明问题）
            RoundedRectangle(cornerRadius: 20)
                .fill(.ultraThinMaterial)
                .background(
                    RoundedRectangle(cornerRadius: 20)
                        // 关键：浅色模式用高不透明度（0.9），深色模式保留0.4
                        .fill(colorScheme == .dark ? Color("meCard").opacity(0.4) : Color("meCard").opacity(0.9))
                )
                .blur(radius: 5)
                // 光泽效果
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                    .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                    .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .rotationEffect(.degrees(30))
                        .blur(radius: 2)
                        .mask(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [.black, .clear]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                        )
                )
                // 边框光泽
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                    .clear,
                                    .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                ]),
                                startPoint: .init(x: 0, y: 0),
                                endPoint: .init(x: 1, y: 1)
                            ),
                            lineWidth: 1
                        )
                )
                // 阴影适配：添加x偏移（5→解决左侧超出），缩小半径（8→6）更精致
                .shadow(
                    color: colorScheme == .dark ? Color("meCardShadow").opacity(0.3) : Color("meCardShadow").opacity(0.15),
                    radius: 6, x: 5, y: 4 // 关键：x=5→阴影右移，避免左侧超出
                )
                .shadow(
                    color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                    radius: 2, x: 2, y: 1 // 高光阴影同步右移，保持对称
                )
            
            // 内容
            HStack(alignment: .center, spacing: 15) {
                Image("avatar")
                    .resizable()
                    .frame(width: 60, height: 60)
                    .clipShape(Circle())
                    .shadow(
                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                        radius: 3, x: 0, y: 2
                    )
                
                VStack(alignment: .leading, spacing: 5) {
                    Text(studentInfo.name.isEmpty ? "未获取姓名":studentInfo.name)
                        .font(.title)
                        .fontWeight(.medium)
                        .foregroundColor(colorScheme == .dark ? .white : Color("textColor"))
                        .shadow(
                            color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                            radius: 1, x: 0, y: 1
                        )
                    
                    Text(studentInfo.studentNumber.isEmpty ? "未获取学号" : studentInfo.studentNumber)
                        .foregroundColor(colorScheme == .dark ? .white.opacity(0.9) : Color("textColor").opacity(0.8))
                        .shadow(
                            color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                            radius: 1, x: 0, y: 1
                        )
                }
                
                Spacer()
            }
            .padding(.horizontal, 30)
            .padding(.vertical, 20)
        }
        .frame(width: UIScreen.main.bounds.width - 40, height: 120) // 关键：用屏幕宽度-40，避免超出
        .overlay(FlashEffectEnhanced())
    }

    // MARK: - 菜单列表（修复阴影+浅色不透明）
    private var menuList: some View {
        ForEach(menuItems) { item in
            NavigationLink(destination: item.destination) {
                ZStack {
                    // 玻璃效果背景：浅色模式提升不透明度（0.9）
                    RoundedRectangle(cornerRadius: 20)
                        .fill(.ultraThinMaterial)
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(colorScheme == .dark ? Color("meCard").opacity(0.4) : Color("meCard").opacity(0.9))
                        )
                        .blur(radius: 5)
                        // 光泽效果
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [
                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                        ]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .rotationEffect(.degrees(30))
                                .blur(radius: 2)
                                .mask(
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(
                                            LinearGradient(
                                                gradient: Gradient(colors: [.black, .clear]),
                                                startPoint: .topLeading,
                                                endPoint: .bottomTrailing
                                            )
                                        )
                                )
                        )
                        // 边框光泽
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(
                                    LinearGradient(
                                        gradient: Gradient(colors: [
                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                            .clear,
                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                        ]),
                                        startPoint: .init(x: 0, y: 0),
                                        endPoint: .init(x: 1, y: 1)
                                    ),
                                    lineWidth: 1
                                )
                        )
                        // 阴影适配：x偏移5，避免左侧超出
                        .shadow(
                            color: colorScheme == .dark ? Color("meCardShadow").opacity(0.3) : Color("meCardShadow").opacity(0.15),
                            radius: 6, x: 5, y: 4
                        )
                        .shadow(
                            color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                            radius: 2, x: 2, y: 1
                        )
                    
                    // 内容
                    HStack(alignment: .center, spacing: 10) {
                        Image(item.icon)
                            .resizable()
                            .frame(width: 24, height: 24)
                            .foregroundColor(colorScheme == .dark ? .white : Color("textColor"))
                            .shadow(
                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                radius: 1, x: 0, y: 1
                            )
                        
                        Text(item.title)
                            .font(.subheadline)
                            .foregroundColor(colorScheme == .dark ? .white : Color("textColor"))
                            .shadow(
                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                radius: 1, x: 0, y: 1
                            )
                        
                        Spacer()
                        
                        Image(systemName: "chevron.right")
                            .foregroundColor(colorScheme == .dark ? .white.opacity(0.7) : Color("textColor").opacity(0.7))
                            .shadow(
                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                radius: 1, x: 0, y: 1
                            )
                    }
                    .padding(.horizontal, 40)
                    .padding(.vertical, 18)
                }
                .frame(width: UIScreen.main.bounds.width - 40, height: 60) // 屏幕宽度-40，适配所有设备
                .overlay(FlashEffectEnhanced())
            }
        }
    }

    // MARK: - 设置按钮（修复阴影+浅色不透明）
    private var settingsButton: some View {
        NavigationLink(destination: SettingsView()) {
            ZStack {
                // 玻璃效果背景：浅色模式提升不透明度（0.9）
                RoundedRectangle(cornerRadius: 20)
                    .fill(.ultraThinMaterial)
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(colorScheme == .dark ? Color("meCard").opacity(0.4) : Color("meCard").opacity(0.9))
                    )
                    .blur(radius: 5)
                    // 光泽效果
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                        .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                        .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                    ]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .rotationEffect(.degrees(30))
                            .blur(radius: 2)
                            .mask(
                                RoundedRectangle(cornerRadius: 20)
                                    .fill(
                                        LinearGradient(
                                            gradient: Gradient(colors: [.black, .clear]),
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                            )
                    )
                    // 边框光泽
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                        .clear,
                                        .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                    ]),
                                    startPoint: .init(x: 0, y: 0),
                                    endPoint: .init(x: 1, y: 1)
                                ),
                                lineWidth: 1
                            )
                    )
                    // 阴影适配：x偏移5，避免左侧超出
                    .shadow(
                        color: colorScheme == .dark ? Color("meCardShadow").opacity(0.3) : Color("meCardShadow").opacity(0.15),
                        radius: 6, x: 5, y: 4
                    )
                    .shadow(
                        color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                        radius: 2, x: 2, y: 1
                    )
                
                // 内容
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: "gearshape")
                        .frame(width: 24, height: 24)
                        .foregroundColor(colorScheme == .dark ? .white : Color("textColor"))
                        .shadow(
                            color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                            radius: 1, x: 0, y: 1
                        )
                    
                    Text("设置")
                        .font(.subheadline)
                        .foregroundColor(colorScheme == .dark ? .white : Color("textColor"))
                        .shadow(
                            color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                            radius: 1, x: 0, y: 1
                        )
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .foregroundColor(colorScheme == .dark ? .white.opacity(0.7) : Color("textColor").opacity(0.7))
                        .shadow(
                            color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                            radius: 1, x: 0, y: 1
                        )
                }
                .padding(.horizontal, 40)
                .padding(.vertical, 18)
            }
            .frame(width: UIScreen.main.bounds.width - 40, height: 60) // 屏幕宽度-40，避免超出
            .overlay(FlashEffectEnhanced())
        }
    }
  
    // MARK: - 设置页面（适配深浅色文字）
    struct SettingsView: View {
        @StateObject private var notificationManager = NotificationManager.shared
        @State var showAlert:Bool=false
        @Environment(\.colorScheme) var colorScheme
        
        var body: some View {
            Form {
                Section(header: Text("通知设置")
                            .foregroundColor(colorScheme == .dark ? .white : .black)) {
                    Button("清除不再提醒的设置") {
                        showAlert.toggle()
                    }
                    .foregroundColor(colorScheme == .dark ? .white : .blue)
                }
            }
            .background(Color("bgcolor").edgesIgnoringSafeArea(.all))
            .alert(isPresented: $showAlert) {
                Alert(
                    title: Text("确认清除吗")
                        .foregroundColor(colorScheme == .dark ? .white : .black),
                    message: Text(""),
                    primaryButton: .default(Text("取消")) {
                        showAlert.toggle()
                    },
                    secondaryButton: .default(Text("确认")) {
                        notificationManager.clearDontRemindSettings()
                        showAlert.toggle()
                    }
                )
            }
            .navigationTitle("设置")
            .navigationBarTitleDisplayMode(.inline)
            .foregroundColor(colorScheme == .dark ? .white : .black)
        }
    }

    // MARK: - 退出登录按钮（修复阴影+浅色不透明）
    private var logoutButton: some View {
        Button {
            onLoginOut()
        } label: {
            ZStack {
                // 玻璃效果背景：浅色模式提升红色底色不透明度（0.2→解决透明）
                RoundedRectangle(cornerRadius: 20)
                    .fill(.ultraThinMaterial)
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(colorScheme == .dark ? Color.red.opacity(0.3) : Color.red.opacity(0.2))
                    )
                    .blur(radius: 5)
                    // 光泽效果
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                        .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                        .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                    ]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .rotationEffect(.degrees(30))
                            .blur(radius: 2)
                            .mask(
                                RoundedRectangle(cornerRadius: 20)
                                    .fill(
                                        LinearGradient(
                                            gradient: Gradient(colors: [.black, .clear]),
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                            )
                    )
                    // 边框光泽
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        .red.opacity(colorScheme == .dark ? 0.6 : 0.9),
                                        .clear,
                                        .red.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                    ]),
                                    startPoint: .init(x: 0, y: 0),
                                    endPoint: .init(x: 1, y: 1)
                                ),
                                lineWidth: 1
                            )
                    )
                    // 阴影适配：x偏移5，避免左侧超出
                    .shadow(
                        color: colorScheme == .dark ? Color.red.opacity(0.3) : Color.red.opacity(0.15),
                        radius: 6, x: 5, y: 4
                    )
                    .shadow(
                        color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                        radius: 2, x: 2, y: 1
                    )
                
                // 内容
                Text("退出登录")
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(colorScheme == .dark ? .white : .red)
                    .shadow(
                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                        radius: 1, x: 0, y: 1
                    )
            }
            .frame(width: UIScreen.main.bounds.width - 40, height: 60) // 屏幕宽度-40，适配所有设备
            .overlay(FlashEffectEnhanced())
        }
    }

    // MARK: - 数据保存/请求逻辑
    private func saveStudentInfo(_ info: StudentInfo) {
        if let data = try? JSONEncoder().encode(info),
           let jsonString = String(data: data, encoding: .utf8) {
            studentInfoJson = jsonString
            print("✅ 学生信息已保存: \(info.name) - \(info.studentNumber)")
            // @AppStorage 会自动触发视图更新，不需要手动设置 isLoadingInfo
        }
    }
    
    // MARK: - 缓存和刷新机制（参考CheckGrades）
    private func loadCachedStudentInfo() {
        // 检查登录状态：如果有 cookie 说明是登录态
        let isLoggedIn = !cookie.isEmpty
        
        if isLoggedIn {
            // 登录态：先加载本地缓存（如果有）
            if !studentInfoJson.isEmpty {
                print("✅ 登录态：加载本地缓存的学生信息")
                isLoadingInfo = false
                errorMessage = nil
                // 在后台刷新数据（不阻塞UI）
//                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
//                    self.refreshStudentInfo()
//                }
            } else {
                // 没有缓存，直接刷新
                print("🔄 登录态：无本地缓存，从网络获取学生信息")
                refreshStudentInfo()
            }
        } else {
            // 未登录态：清除缓存
            if !studentInfoJson.isEmpty {
                print("⚠️ 未登录态：清除学生信息缓存")
                studentInfoJson = ""
            }
            isLoadingInfo = false
            errorMessage = nil
        }
    }
    private func refreshStudentInfo() {
        print("🔄 开始刷新学生信息")
        let loginInstance = loginframe() // 创建 loginframe 实例
        // 按用户类型调用对应的专属登录方法
        if Identify.chooseIdentify == Identify.Graduate {
            // 研究生：调用 graduateLoginRequest
            loginInstance.graduateLoginRequest(
                studentId: username, // 研究生用 studentId（学号）
                password: password
            ) { loginSuccess, loginMessage in
                self.handleLoginResult(loginSuccess: loginSuccess, loginMessage: loginMessage, loginInstance: loginInstance)
            }
        } else {
            // 本科生：调用 undergraduateLoginRequest
            loginInstance.undergraduateLoginRequest(
                username: username, // 本科生用 username
                password: password
            ) { loginSuccess, loginMessage in
                self.handleLoginResult(loginSuccess: loginSuccess, loginMessage: loginMessage, loginInstance: loginInstance)
            }
        }
    }
    // 抽取通用逻辑：处理登录结果（避免代码重复）
    private func handleLoginResult(loginSuccess: Bool, loginMessage: String, loginInstance: loginframe) {
        if loginSuccess {
            if Identify.chooseIdentify == Identify.Graduate {
                // 研究生：登录时已经获取并保存了学生信息，不需要再调用 fetchInformation
                // 学生信息已经在 graduateLoginRequest 中通过 saveStudentInfo 保存了
                print("✅ 研究生登录成功，学生信息已在登录时获取")
                isLoadingInfo = false
                errorMessage = nil
                // 不需要额外的网络请求，学生信息已经在登录时保存了
            } else {
                // 本科生：需要获取 Cookie 后，再获取学生信息
                print("✅ Cookie获取成功，开始获取学生信息")
                // 从 loginInstance 中获取登录成功后的 Cookie
                self.cookie = loginframe.cookie ?? ""
                // 继续执行学生信息获取逻辑
                self.fetchInformation { fetchSuccess in
                    if fetchSuccess {
                    } else {
                        // 获取失败但有本地缓存：显示提示
                        if !self.studentInfoJson.isEmpty {
                            self.errorMessage = "网络错误，已显示本地缓存数据"
                        }
                    }
                }
            }
        } else {
            print("❌ 登录失败")
            // 登录失败的缓存判断逻辑（保持不变）
            if !self.studentInfoJson.isEmpty {
                self.errorMessage = "网络错误或登录失效，已显示本地缓存数据"
            } else {
                self.errorMessage = "登录失效，请尝试重新登录"
            }
        }
    }
    func graduateInformation(
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
                    self.studentNumber = studentData.studentNumber
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
    public func fetchInformation(completion: @escaping (Bool) -> Void) {
        // 如果是登录态且有本地缓存，先不显示加载状态（保持显示缓存数据）
        let hasCachedData = !studentInfoJson.isEmpty && !cookie.isEmpty
        if !hasCachedData {
            isLoadingInfo = true
        }
        errorMessage = nil
        
        guard !cookie.isEmpty else {
            isLoadingInfo = false
            errorMessage = NetworkError.authenticationFailed.userFriendlyMessage
            completion(false)
            return
        }
        
        guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.getStudentInfoPage)") else {
            isLoadingInfo = false
            errorMessage = "接口地址无效"
            completion(false)
            return
        }
        
        let headers = ["Cookie": cookie]
        
        NetworkErrorHandler.get(
            url: url,
            headers: headers,
            responseType: InfoCourseResponse.self
        ) { result in
            self.isLoadingInfo = false
            switch result {
            case .success(let infoResponse):
                if infoResponse.code == 200 {
                    self.saveStudentInfo(infoResponse.data)
                    print("✅ 学生信息已从网络更新")
                    completion(true)
                } else {
                    self.errorMessage = infoResponse.message
                    // 如果有缓存数据，网络失败时使用缓存
                    if hasCachedData {
                        print("⚠️ 网络获取失败，继续使用本地缓存")
                    }
                    completion(false)
                }
            case .failure(let networkError):
                self.errorMessage = networkError.userFriendlyMessage
                // 如果有缓存数据，网络失败时使用缓存
                if hasCachedData {
                    print("⚠️ 网络错误，继续使用本地缓存: \(networkError.userFriendlyMessage)")
                }
                completion(false)
            }
        }
    }

//    public func parseInformation() {
//        isLoadingInfo = true
//        errorMessage = nil
//        
//        guard !infoPageWeb.isEmpty else {
//            isLoadingInfo = false
//            errorMessage = "未获取到页面数据，请先执行fetch"
//            return
//        }
//        
//        guard !authData.isEmpty else {
//            isLoadingInfo = false
//            errorMessage = NetworkError.authenticationFailed.userFriendlyMessage
//            return
//        }
//        
//        guard let url = URL(string: "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.getStudentInfo)") else {
//            isLoadingInfo = false
//            errorMessage = "接口地址无效"
//            return
//        }
//        
//        let requestBody: [String: String] = ["webpage": infoPageWeb]
//        guard let jsonData = try? JSONSerialization.data(withJSONObject: requestBody) else {
//            isLoadingInfo = false
//            errorMessage = "请求数据格式错误"
//            return
//        }
//        
//        let headers = [
//            "Authorization": "Wuster \(authData)",
//            "Content-Type": "application/json",
//            "Accept": "application/json"
//        ]
//        
//        NetworkErrorHandler.post(
//            url: url,
//            headers: headers,
//            body: jsonData,
//            responseType: InfoCourseResponse.self
//        ) { result in
//            DispatchQueue.main.async {
//                self.isLoadingInfo = false
//                
//                switch result {
//                case .success(let infoResponse):
//                    if infoResponse.code == 200 {
//                        print("✅ 解析学生信息成功")
//                        self.saveStudentInfo(infoResponse.data)
//                    } else {
//                        print("❌ 解析学生信息失败: \(infoResponse.message)")
//                        self.errorMessage = infoResponse.message
//                    }
//                case .failure(let networkError):
//                    print("❌ 网络错误: \(networkError.userFriendlyMessage)")
//                    self.errorMessage = networkError.userFriendlyMessage
//                }
//            }
//        }
//    }
}

// MARK: - 提交反馈页面（适配深浅色）
struct suggestionView: View {
    private let urlString = "https://support.qq.com/products/776359"
    @State private var showAlert = true
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        CustomWebView(urlString: urlString)
            .alert("提示", isPresented: $showAlert) {
                Button("知道了", role: .cancel) {
                    showAlert = false
                }
                .foregroundColor(colorScheme == .dark ? .white : .blue)
            } message: {
                Text("提交反馈请选择游客登录")
                    .foregroundColor(colorScheme == .dark ? .white : .black)
            }
            .background(Color("bgcolor").edgesIgnoringSafeArea(.all))
    }
}

// MARK: - 学生信息模型（保留原有）
struct StudentInfo: Codable {
    let studentNumber: String
    let name: String
    let college: String
    let major: String
    let clazz: String
    let birthday: String
    let sex: String
    let nationality: String?
    let hometown: String?
    let idNumber: String?

    init() {
        self.studentNumber = ""
        self.name = ""
        self.college = ""
        self.major = ""
        self.clazz = ""
        self.birthday = ""
        self.sex = ""
        self.nationality = nil
        self.hometown = nil
        self.idNumber = nil
    }

    init(studentNumber: String,
         name: String,
         college: String,
         major: String,
         clazz: String,
         birthday: String,
         sex: String,
         nationality: String?,
         hometown: String?,
         idNumber: String?) {
        self.studentNumber = studentNumber
        self.name = name
        self.college = college
        self.major = major
        self.clazz = clazz
        self.birthday = birthday
        self.sex = sex
        self.nationality = nationality
        self.hometown = hometown
        self.idNumber = idNumber
    }
}
//研究生
struct StudentData: Codable {
    // 1. 字段定义：与原 StudentData 对齐，按 StudentInfo 原有顺序排列，
    //    并将 nationality/hometown/idNumber 设为可选（与示例逻辑一致）
    let studentNumber: String   // 学号（必选，原 StudentData 中为 String）
    let name: String            // 姓名（必选）
    let college: String         // 学院（必选）
    let major: String           // 专业（必选）
    let clazz: String           // 班级（必选，对应原 StudentData 的 clazz）
    let birthday: String        // 生日（必选，原 StudentData 中为 String）
    let sex: String             // 性别（必选）
    let nationality: String?    // 民族（可选，允许为 nil）
    let hometown: String?       // 籍贯（可选）
    let idNumber: String?       // 身份证号（可选）

    // 2. 默认初始化器：所有必选字段设为空字符串，可选字段设为 nil
    init() {
        self.studentNumber = ""
        self.name = ""
        self.college = ""
        self.major = ""
        self.clazz = ""
        self.birthday = ""
        self.sex = ""
        self.nationality = nil
        self.hometown = nil
        self.idNumber = nil
    }

    // 3. 指定初始化器：接收所有字段参数，显式赋值（与示例逻辑一致）
    init(studentNumber: String,
         name: String,
         college: String,
         major: String,
         clazz: String,
         birthday: String,
         sex: String,
         nationality: String?,
         hometown: String?,
         idNumber: String?) {
        self.studentNumber = studentNumber
        self.name = name
        self.college = college
        self.major = major
        self.clazz = clazz
        self.birthday = birthday
        self.sex = sex
        self.nationality = nationality
        self.hometown = hometown
        self.idNumber = idNumber
    }

    // 自定义解码逻辑：处理空字符串和 null 的情况
    enum CodingKeys: String, CodingKey {
        case studentNumber, name, college, major, clazz, birthday, sex, nationality, hometown, idNumber
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        studentNumber = try container.decode(String.self, forKey: .studentNumber)
        name = try container.decode(String.self, forKey: .name)
        college = try container.decode(String.self, forKey: .college)
        major = try container.decode(String.self, forKey: .major)
        clazz = try container.decode(String.self, forKey: .clazz)
        birthday = try container.decode(String.self, forKey: .birthday)
        sex = try container.decode(String.self, forKey: .sex)
        
        // 处理可选字段：使用 decodeIfPresent 处理 null，然后检查空字符串
        if let nationalityStr = try container.decodeIfPresent(String.self, forKey: .nationality), !nationalityStr.isEmpty {
            nationality = nationalityStr
        } else {
            nationality = nil
        }
        
        if let hometownStr = try container.decodeIfPresent(String.self, forKey: .hometown), !hometownStr.isEmpty {
            hometown = hometownStr
        } else {
            hometown = nil
        }
        
        if let idNumberStr = try container.decodeIfPresent(String.self, forKey: .idNumber), !idNumberStr.isEmpty {
            idNumber = idNumberStr
        } else {
            idNumber = nil
        }
    }
    
    // 实现 encode 方法以完整实现 Codable 协议
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(studentNumber, forKey: .studentNumber)
        try container.encode(name, forKey: .name)
        try container.encode(college, forKey: .college)
        try container.encode(major, forKey: .major)
        try container.encode(clazz, forKey: .clazz)
        try container.encode(birthday, forKey: .birthday)
        try container.encode(sex, forKey: .sex)
        try container.encodeIfPresent(nationality, forKey: .nationality)
        try container.encodeIfPresent(hometown, forKey: .hometown)
        try container.encodeIfPresent(idNumber, forKey: .idNumber)
    }
    
    // （可选）若 JSON 字段名与模型字段名一致，无需额外定义 CodingKeys；
    // 若后续 JSON 字段名变更（如 "clazz" 改为 "class"），可添加以下映射：
    // enum CodingKeys: String, CodingKey {
    //     case studentNumber, name, college, major, birthday, sex, nationality, hometown, idNumber
    //     case clazz = "class"  // 示例：JSON 是 "class"，模型用 "clazz"
    // }
}
// MARK: - 关于软件页面
struct AboutAppView: View {
    @Environment(\.colorScheme) var colorScheme
    @State private var showVersionInfo = false
    @State private var isViewAppeared = false
    private var headerSection: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 100, height: 100)
                    .shadow(color: .black.opacity(0.1), radius: 10, x: 0, y: 5)
                
                Image("applogo")
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: 100, height: 100)
                    .clipShape(Circle())
            }
            
            // App名称、版本和作者
            VStack(spacing: 8) {
                Text("武科大助手")
                    .font(.system(size: 28, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)
                
                HStack(spacing: 12) {
                    Text("v3.8.0")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.secondary)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(
                            Capsule()
                                .fill(.blue.opacity(0.1))
                        )
                    
                    Text("by JSPV")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.secondary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(
                            Capsule()
                                .fill(.gray.opacity(0.1))
                        )
                }
            }
        }
        .padding(.top, 20)
        .padding(.bottom, 30)
        .frame(width: 500)
        .background(
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.blue.opacity(0.1),
                    Color.purple.opacity(0.05)
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
    var body: some View {
        ScrollView(.vertical, showsIndicators: false) {
            LazyVStack(spacing: 0) {
                // 优化的顶部区域
                headerSection
                
                // 懒加载的内容区域
                LazyVStack(spacing: 24) {
                    // 重构说明卡片
                    ModernCard {
                        VStack(alignment: .leading, spacing: 16) {
                            HStack {
                                Image(systemName: "sparkles")
                                    .font(.title2)
                                    .foregroundColor(.blue)
                                
                                Text("完全原生重构")
                                    .font(.title2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.primary)
                                
                                Spacer()
                            }
                            
                            Text("本版本采用SwiftUI完全重构，充分利用原生优势，带来更流畅的用户体验和更现代的界面设计。")
                                .font(.body)
                                .foregroundColor(.secondary)
                                .lineSpacing(4)
                            
                            HStack {
                                Label("开发周期", systemImage: "clock")
                                    .font(.caption)
                                    .foregroundColor(.blue)
                                
                                Spacer()
                                
                                Text("约一个月")
                                    .font(.caption)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.primary)
                            }
                            .padding(.top, 8)
                        }
                    }
                    
                    // 技术特色
                    VStack(alignment: .leading, spacing: 20) {
                        Text("技术特色")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                        
                        LazyVGrid(columns: [
                            GridItem(.flexible()),
                            GridItem(.flexible())
                        ], spacing: 16) {
                            FeatureCard(
                                icon: "swift",
                                title: "SwiftUI",
                                description: "原生框架",
                                color: .orange
                            )
                            
                            FeatureCard(
                                icon: "iphone",
                                title: "iOS原生",
                                description: "性能优化",
                                color: .blue
                            )
                            
                            FeatureCard(
                                icon: "network",
                                title: "网络优化",
                                description: "智能重试",
                                color: .green
                            )
                            
                            FeatureCard(
                                icon: "paintbrush",
                                title: "现代UI",
                                description: "美观设计",
                                color: .purple
                            )
                        }
                    }
                    
                    // 功能亮点
                    ModernCard {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("功能亮点")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                            
                            VStack(spacing: 12) {
                                FeatureRow(
                                    icon: "calendar",
                                    title: "智能课表",
                                    description: "多节课显示，ICS导出"
                                )
                                
                                FeatureRow(
                                    icon: "chart.bar",
                                    title: "成绩查询",
                                    description: "实时同步，历史记录"
                                )
                                
                                FeatureRow(
                                    icon: "person.2",
                                    title: "校园社交",
                                    description: "组队发布，二手市场"
                                )
                                
                                FeatureRow(
                                    icon: "wifi",
                                    title: "网络优化",
                                    description: "智能重试，错误分类"
                                )
                            }
                        }
                    }
                    
                    // 开发者团队
                    ModernCard {
                        VStack(alignment: .leading, spacing: 20) {
                            Text("开发者团队")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                            
                            VStack(spacing: 16) {
                                // 后端团队
                                DeveloperTeamSection(
                                    title: "后端开发",
                                    icon: "server.rack",
                                    color: .blue,
                                    members: [
                                        ("核心", ["刘阳", "朱永淇","程万"]),
                                        ("校园搭子", ["崔江杰","周玉钊"]),
                                        ("二手平台", ["程万", "韩智轩", "廖宇晨"]),
                                        ("竞赛组队", ["张炫", "于天一"])
                                    ]
                                )
                                
                                Divider()
                                    .background(Color.gray.opacity(0.3))
                                
                                // 运维团队
                                DeveloperTeamSection(
                                    title: "运维",
                                    icon: "gear",
                                    color: .purple,
                                    members: [
                                        ("运维团队", ["刘阳","周玉钊","程万"])
                                    ]
                                )
                                
                                Divider()
                                    .background(Color.gray.opacity(0.3))
                                
                                // iOS团队
                                DeveloperTeamSection(
                                    title: "iOS开发",
                                    icon: "iphone",
                                    color: .orange,
                                    members: [
                                        ("开发团队", ["刘阳", "沈歆钰"])
                                    ]
                                )
                            }
                        }
                    }
                    
                    // 技术栈信息
                    ModernCard {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("技术栈")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                            
                            VStack(spacing: 12) {
                                AboutInfoRow(
                                    label: "开发者",
                                    value: "JSPV",
                                    icon: "person.circle"
                                )
                                
                                AboutInfoRow(
                                    label: "开发语言",
                                    value: "Swift 5.9+",
                                    icon: "swift"
                                )
                                
                                AboutInfoRow(
                                    label: "UI框架",
                                    value: "SwiftUI",
                                    icon: "paintbrush"
                                )
                                
                                AboutInfoRow(
                                    label: "最低版本",
                                    value: "iOS 15.0+",
                                    icon: "iphone"
                                )
                                
                                AboutInfoRow(
                                    label: "开发时间",
                                    value: "2025年8月",
                                    icon: "calendar"
                                )
                            }
                        }
                    }
                    
                    // 致谢信息
                    ModernCard {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("致谢")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                            
                            Text("感谢所有用户的反馈和建议，让武科大助手不断进步。由于开发时间较短，如有不足之处，敬请谅解。")
                                .font(.body)
                                .foregroundColor(.secondary)
                                .lineSpacing(4)
                            
                            HStack {
                                Image(systemName: "heart.fill")
                                    .foregroundColor(.red)
                                
                                Text("用心为武科大学子服务")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                
                                Spacer()
                            }
                            .padding(.top, 8)
                        }
                    }
                }
                
                // 版本信息按钮
                Button(action: {
                    // 只有在视图已出现时才执行动画
                    guard isViewAppeared else { return }
                    withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) {
                        showVersionInfo.toggle()
                    }
                }) {
                    HStack {
                        Image(systemName: "info.circle")
                        Text("查看版本详情")
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                    }
                    .font(.body)
                    .foregroundColor(.blue)
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.blue.opacity(0.1))
                    )
                }
                .buttonStyle(PlainButtonStyle())
                .disabled(!isViewAppeared) // 防止在视图未完全加载时点击
                
                if showVersionInfo && isViewAppeared {
                    VersionDetailView()
                        .transition(.asymmetric(
                            insertion: .opacity.combined(with: .scale(scale: 0.95)),
                            removal: .opacity.combined(with: .scale(scale: 0.95))
                        ))
                }
                
                Spacer(minLength: 50)
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)
        }      .onAppear {
            // 标记视图已出现，防止导航冲突
            isViewAppeared = true
        }
        .onDisappear {
            // 清理状态，防止内存泄漏
            isViewAppeared = false
            showVersionInfo = false
        }
        .navigationTitle("关于软件")
        .navigationBarTitleDisplayMode(.inline)
    }
  
}// MARK: - 现代化卡片组件
struct ModernCard<Content: View>: View {
    let content: Content
    @Environment(\.colorScheme) var colorScheme
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        VStack {
            content
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(colorScheme == .dark ? Color(.systemGray6) : Color.white)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    .blue.opacity(0.2),
                                    .purple.opacity(0.1),
                                    .clear
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1
                        )
                )
        )
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
}

// MARK: - 功能卡片组件
struct FeatureCard: View {
    let icon: String
    let title: String
    let description: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(color.opacity(0.2))
                    .frame(width: 50, height: 50)
                
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(color)
            }
            
            VStack(spacing: 4) {
                Text(title)
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
}

// MARK: - 功能行组件
struct FeatureRow: View {
    let icon: String
    let title: String
    let description: String
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(.blue)
                .frame(width: 24)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
    }
}

// MARK: - 信息行组件
struct AboutInfoRow: View {
    let label: String
    let value: String
    let icon: String
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.body)
                .foregroundColor(.blue)
                .frame(width: 20)
            
            Text(label)
                .font(.body)
                .foregroundColor(.primary)
            
            Spacer()
            
            Text(value)
                .font(.body)
                .fontWeight(.medium)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - 版本详情视图
struct VersionDetailView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("版本详情")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            VStack(spacing: 8) {
                VersionRow(version: "v3.8", date: "2025.11.10", description: "原生重构版本")
                VersionRow(version: "v3.7", date: "2023.11.15", description: "功能优化版本")
                VersionRow(version: "v3.3", date: "2021-06", description: "新增iOS桌面小组件")
                VersionRow(version: "v3.0", date: "2021-06", description: "使用纯Flutter重新设计与开发iOS端")
                VersionRow(version: "v2.0", date: "2020-04", description: "重构代码、重写UI、新增校历/考试提醒/工时查询等功能")
                VersionRow(version: "v1.0.4", date: "2019-03", description: "优化课程表、改进UI")
                VersionRow(version: "v1.0.3", date: "2019-02", description: "修复图书馆借阅、学分统计问题，改进UI")
                VersionRow(version: "v1.0", date: "2019-01", description: "初始版本上线")
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
}

struct VersionRow: View {
    let version: String
    let date: String
    let description: String
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(version)
                    .font(.body)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Text(date)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - 检查更新页面
struct CheckUpdateView: View {
    @Environment(\.colorScheme) var colorScheme
    @State private var isChecking = false
    @State private var updateStatus: UpdateStatus = .unknown
    @State private var showAlert = false
    @State private var alertMessage = ""
    
    enum UpdateStatus {
        case unknown
        case checking
        case upToDate
        case updateAvailable
        case error
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 30) {
                // 顶部图标和标题
                VStack(spacing: 20) {
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [.blue.opacity(0.3), .purple.opacity(0.2)]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 100, height: 100)
                        
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 40))
                            .foregroundColor(.blue)
                    }
                    
                    VStack(spacing: 8) {
                        Text("检查更新")
                            .font(.title)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                        
                        Text("确保您使用的是最新版本")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.top, 40)
                
                // 当前版本信息
                ModernCard {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("当前版本")
                            .font(.headline)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                        
                        HStack {
                            Image(systemName: "app.badge")
                                .foregroundColor(.blue)
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("武科大助手")
                                    .font(.body)
                                    .fontWeight(.medium)
                                    .foregroundColor(.primary)
                                
                                Text("v\(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "3.8.0")")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            Text("最新")
                                .font(.caption)
                                .fontWeight(.semibold)
                                .foregroundColor(.green)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(
                                    Capsule()
                                        .fill(.green.opacity(0.2))
                                )
                        }
                    }
                }
                
                // 更新状态卡片
                updateStatusCard
                
                // 检查更新按钮
                Button(action: checkForUpdates) {
                    HStack {
                        if isChecking {
                            ProgressView()
                                .scaleEffect(0.8)
                                .foregroundColor(.white)
                        } else {
                            Image(systemName: "arrow.clockwise")
                        }
                        
                        Text(isChecking ? "检查中..." : "检查更新")
                            .fontWeight(.semibold)
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [.blue, .purple]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(12)
                }
                .disabled(isChecking)
                
                // 更新说明
                ModernCard {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("更新说明")
                            .font(.headline)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                        
                        VStack(alignment: .leading, spacing: 8) {
                            UpdateNoteRow(version: "v3.8.0", note: "完全原生重构，性能大幅提升")
                            UpdateNoteRow(version: "v3.7.0", note: "功能优化版本")
                            UpdateNoteRow(version: "v3.3.0", note: "新增iOS桌面小组件")
                            UpdateNoteRow(version: "v3.0.0", note: "使用纯Flutter重新设计与开发iOS端")
                            UpdateNoteRow(version: "v2.0.0", note: "重构代码、重写UI、新增校历/考试提醒/工时查询等功能")
                            UpdateNoteRow(version: "v1.0.4", note: "优化课程表、改进UI")
                            UpdateNoteRow(version: "v1.0.3", note: "修复图书馆借阅、学分统计问题，改进UI")
                            UpdateNoteRow(version: "v1.0.0", note: "初始版本上线")
                        }
                    }
                }
                
                Spacer(minLength: 50)
            }
            .padding(.horizontal, 20)
        }
        .navigationTitle("检查更新")
        .navigationBarTitleDisplayMode(.inline)
        .alert("更新提示", isPresented: $showAlert) {
            if updateStatus == .updateAvailable {
                Button("前往App Store") {
                    openAppStore()
                }
                Button("稍后提醒", role: .cancel) { }
            } else {
                Button("确定") { }
            }
        } message: {
            Text(alertMessage)
        }
        .onAppear {
            // 修复：确保页面稳定显示，避免自动返回
            updateStatus = .unknown
        }
    }
    
    @ViewBuilder
    private var updateStatusCard: some View {
        ModernCard {
            VStack(alignment: .leading, spacing: 16) {
                Text("更新状态")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(.primary)
                
                HStack {
                    Image(systemName: statusIcon)
                        .foregroundColor(statusColor)
                        .font(.title2)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(statusTitle)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundColor(.primary)
                        
                        Text(statusDescription)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                }
            }
        }
    }
    
    private var statusIcon: String {
        switch updateStatus {
        case .unknown:
            return "questionmark.circle"
        case .checking:
            return "arrow.clockwise"
        case .upToDate:
            return "checkmark.circle"
        case .updateAvailable:
            return "exclamationmark.triangle"
        case .error:
            return "xmark.circle"
        }
    }
    
    private var statusColor: Color {
        switch updateStatus {
        case .unknown:
            return .gray
        case .checking:
            return .blue
        case .upToDate:
            return .green
        case .updateAvailable:
            return .orange
        case .error:
            return .red
        }
    }
    
    private var statusTitle: String {
        switch updateStatus {
        case .unknown:
            return "未检查"
        case .checking:
            return "检查中"
        case .upToDate:
            return "已是最新版本"
        case .updateAvailable:
            return "发现新版本"
        case .error:
            return "检查失败"
        }
    }
    
    private var statusDescription: String {
        switch updateStatus {
        case .unknown:
            return "点击下方按钮检查更新"
        case .checking:
            return "正在连接服务器..."
        case .upToDate:
            return "您使用的是最新版本"
        case .updateAvailable:
            return "建议立即更新到最新版本"
        case .error:
            return "请检查网络连接后重试"
        }
    }
    
    private func checkForUpdates() {
        isChecking = true
        updateStatus = .checking
        
        // 获取当前App的Bundle ID
        guard let bundleId = Bundle.main.bundleIdentifier else {
            handleUpdateError("无法获取应用信息")
            return
        }
        
        // 构建App Store API URL
        let appStoreURL = "https://itunes.apple.com/lookup?bundleId=\(bundleId)"
        
        guard let url = URL(string: appStoreURL) else {
            handleUpdateError("无效的App Store链接")
            return
        }
        
        // 发起网络请求
        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                self.isChecking = false
                
                if let error = error {
                    self.handleUpdateError("网络连接失败: \(error.localizedDescription)")
                    return
                }
                
                guard let data = data else {
                    self.handleUpdateError("未收到服务器响应")
                    return
                }
                
                self.processAppStoreResponse(data)
            }
        }.resume()
    }
    
    private func processAppStoreResponse(_ data: Data) {
        do {
            guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
                  let results = json["results"] as? [[String: Any]],
                  let appInfo = results.first else {
                // 应用未在App Store上架或未找到
                updateStatus = .upToDate
                alertMessage = "您当前使用的是最新版本 v3.8.0"
                showAlert = true
                return
            }
            
            // 获取App Store上的版本信息
            guard let storeVersion = appInfo["version"] as? String,
                  let trackViewUrl = appInfo["trackViewUrl"] as? String else {
                handleUpdateError("无法解析版本信息")
                return
            }
            
            // 获取当前版本号
            let currentVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "3.8.0"
            let hasUpdate = compareVersions(currentVersion, storeVersion) < 0
            
            if hasUpdate {
                updateStatus = .updateAvailable
                alertMessage = "发现新版本 \(storeVersion)，是否前往App Store更新？"
                showAlert = true
            } else {
                updateStatus = .upToDate
                alertMessage = "您当前使用的是最新版本 v\(currentVersion)"
                showAlert = true
            }
            
        } catch {
            handleUpdateError("解析版本信息失败: \(error.localizedDescription)")
        }
    }
    
    private func handleUpdateError(_ message: String) {
        updateStatus = .error
        alertMessage = message
        showAlert = true
    }
    
    private func compareVersions(_ version1: String, _ version2: String) -> Int {
        let v1Components = version1.split(separator: ".").compactMap { Int($0) }
        let v2Components = version2.split(separator: ".").compactMap { Int($0) }
        
        let maxLength = max(v1Components.count, v2Components.count)
        
        for i in 0..<maxLength {
            let v1Value = i < v1Components.count ? v1Components[i] : 0
            let v2Value = i < v2Components.count ? v2Components[i] : 0
            
            if v1Value < v2Value {
                return -1
            } else if v1Value > v2Value {
                return 1
            }
        }
        
        return 0
    }
    
    private func openAppStore() {
        // 获取当前App的Bundle ID
        guard let bundleId = Bundle.main.bundleIdentifier else {
            alertMessage = "无法获取应用信息"
            showAlert = true
            return
        }
        
        // 构建App Store链接
        let appStoreURL = "https://apps.apple.com/app/id\(bundleId)"
        
        if let url = URL(string: appStoreURL) {
            UIApplication.shared.open(url)
        } else {
            // 如果直接链接失败，尝试使用通用App Store搜索
            let searchURL = "https://apps.apple.com/search?term=武科大助手"
            if let searchUrl = URL(string: searchURL) {
                UIApplication.shared.open(searchUrl)
            }
        }
    }
}

struct UpdateNoteRow: View {
    let version: String
    let note: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Text(version)
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundColor(.blue)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(
                    Capsule()
                        .fill(.blue.opacity(0.2))
                )
            
            Text(note)
                .font(.caption)
                .foregroundColor(.secondary)
            
            Spacer()
        }
    }
}

// MARK: - 开发者团队组件
struct DeveloperTeamSection: View {
    let title: String
    let icon: String
    let color: Color
    let members: [(String, [String])]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // 团队标题
            HStack(spacing: 8) {
                ZStack {
                    Circle()
                        .fill(color.opacity(0.2))
                        .frame(width: 32, height: 32)
                    
                    Image(systemName: icon)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(color)
                }
                
                Text(title)
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Spacer()
                
                // 技术栈Logo
                if title == "后端开发" {
                    SpringBootLogo()
                } else if title == "iOS开发" {
                    SwiftLogo()
                } else if title == "运维" {
                    DevOpsLogo()
                }
            }
            
            // 团队成员
            VStack(alignment: .leading, spacing: 8) {
                ForEach(Array(members.enumerated()), id: \.offset) { index, member in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(member.0)
                            .font(.caption)
                            .fontWeight(.medium)
                            .foregroundColor(.secondary)
                        
                        HStack(spacing: 8) {
                            ForEach(member.1, id: \.self) { name in
                                DeveloperNameTag(name: name, color: color)
                            }
                            Spacer()
                        }
                    }
                }
            }
        }
    }
}

// MARK: - 开发者姓名标签
struct DeveloperNameTag: View {
    let name: String
    let color: Color
    
    var body: some View {
        Text(name)
            .font(.caption)
            .fontWeight(.medium)
            .foregroundColor(color)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(
                RoundedRectangle(cornerRadius: 6)
                    .fill(color.opacity(0.15))
                    .overlay(
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(color.opacity(0.3), lineWidth: 1)
                    )
            )
    }
}

// MARK: - SpringBoot Logo
struct SpringBootLogo: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 6)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [.blue.opacity(0.8), .blue.opacity(0.6)]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: 24, height: 24)
            
            Text("S")
                .font(.system(size: 12, weight: .bold, design: .rounded))
                .foregroundColor(.white)
        }
    }
}

// MARK: - Swift Logo
struct SwiftLogo: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 6)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [.orange.opacity(0.8), .orange.opacity(0.6)]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: 24, height: 24)
            
            Text("S")
                .font(.system(size: 12, weight: .bold, design: .rounded))
                .foregroundColor(.white)
        }
    }
}

// MARK: - DevOps Logo
struct DevOpsLogo: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 6)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [.purple.opacity(0.8), .purple.opacity(0.6)]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: 24, height: 24)
            
            Text("D")
                .font(.system(size: 12, weight: .bold, design: .rounded))
                .foregroundColor(.white)
        }
    }
}

#Preview {
    Me(){
        
    }
}
