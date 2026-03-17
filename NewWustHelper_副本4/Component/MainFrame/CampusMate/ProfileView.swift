import SwiftUI

// 个人资料视图
struct ProfileView: View {
    @EnvironmentObject var userData: UserData
    @State private var isEditing = false
    @AppStorage("authData") private var authData: String = ""
    @State private var editedInfo = UserInfo(
//        id: 0,
//        userId: 0,
//        studentId: nil,
//        college: nil,
//        campus: nil,
//        gender: nil,
//        grade: nil,
//        major: nil,
//        avatarUrlId: 0, // 头像ID（初始为0，上传后更新）
//        signature: nil,
//        interests: nil,
//        skills: nil,
//        qq: nil,
//        wechat: nil,
//        phone: nil,
//        contactVisibility: nil,
//        infoVisibility: nil,
//        createdAt: nil,
//        updatedAt: nil
        id: 0,
        userId: 0,
        studentId: nil,
        college: nil,
        campus: nil,
        gender: nil,
        grade: nil,
        major: nil,
        avatarUrlId: nil,  // 应该为 nil 而不是 0
        signature: nil,
        interests: nil,
        skills: nil,
        qq: nil,
        wechat: nil,
        phone: nil,
        contactVisibility: nil,
        infoVisibility: nil,
        createdAt: nil,
        updatedAt: nil
    )
    
    // 新增1：头像上传相关状态
    @State private var showAvatarUploadSheet = false // 控制上传弹窗显示
    @State private var avatarPid: Int? = nil // 上传成功后的头像ID
    @StateObject private var profilePictureGetTool = ImageUploadViewModel() // 头像加载/上传VM
    
    // 暖色调配色方案
    @Environment(\.colorScheme) var colorScheme
    private var primaryColor: Color {
        colorScheme == .dark ? Color(red: 1.0, green: 0.5, blue: 0.3) : Color(red: 1.0, green: 0.4, blue: 0.2)
    }
    private var secondaryColor: Color {
        colorScheme == .dark ? Color(red: 0.6, green: 0.8, blue: 1.0): Color(red: 1.0, green: 0.8, blue: 0.4)
    }
    private var backgroundColor: Color {
        colorScheme == .dark ? Color(red: 0.08, green: 0.08, blue: 0.1) : Color(red: 1.0, green: 0.96, blue: 0.9)
    }
    private var cardColor: Color {
        colorScheme == .dark ? Color(red: 0.15, green: 0.15, blue: 0.2).opacity(0.8) : Color.white
    }
    
    @Environment(\.presentationMode) var presentationMode
    
    // 新增2：头像URL（优先用上传后的URL，其次用原有加载的URL）
    private var avatarUrl: String? {
        profilePictureGetTool.pictureDetail?.url ?? (avatarPid != nil ? nil : profile_url)
    }
    private var profile_url: String? {
        profilePictureGetTool.pictureDetail?.url
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                // 渐变背景
                LinearGradient(
                    gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .edgesIgnoringSafeArea(.top)
                
                ScrollView {
                    VStack(spacing: 24) {
                        // 头像区域
                        profileHeader
                            .padding(.top, 20)
                            .onTapGesture {
                                showAvatarUploadSheet = true
                            }
                        
                        // 卡片式设计
                        VStack(spacing: 16) {
                            formAndButtonsSection
                                .cardStyle(backgroundColor: cardColor)
                        }
                        .padding(.horizontal)
                    }
                }.scrollIndicators(.hidden) // 隐藏滚动指示器

            }
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        HStack {
                            Image(systemName: "chevron.left")
                            Text("首页")
                        }
                    }
                }
            }
            .navigationTitle("个人中心")
            .navigationBarTitleDisplayMode(.inline)
            // 头像上传弹窗
            .sheet(isPresented: $showAvatarUploadSheet) {
                // 弹窗内容容器 - 这里是设置高度的关键位置
                VStack {
                    NavigationView {
                        VStack {
                            // 复用已有上传组件，绑定头像ID
                            ImageUploadView(pictureId: $avatarPid)
                                .padding(.vertical, 20)
                            // 上传成功后同步到服务器的按钮
                            if let pid = avatarPid {
                                Button(action: {
                                    editedInfo.avatarUrlId = pid
                                    saveProfileChanges()
                                    
                                    // 关键新增：用新的头像ID（pid）重新请求头像详情，更新数据源
                                    profilePictureGetTool.fetchPictureDetail(with: pid)
                                    
                                    showAvatarUploadSheet = false // 关闭弹窗
                                }) {
                                    Text("确认使用此头像")
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 12)
                                        .background(primaryColor)
                                        .foregroundColor(.white)
                                        .cornerRadius(12)
                                        .padding(.horizontal, 30)
                                }
                                .shadow(color: primaryColor.opacity(0.3), radius: 4, x: 0, y: 2)
                            }else {
                                Text("确认使用此头像")
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                                    .background(Color.gray)
                                    .foregroundColor(.white)
                                    .cornerRadius(12)
                                    .padding(.horizontal, 30)
                            }
                            Spacer()
                        }
                        .navigationTitle("上传头像")
                        .navigationBarTitleDisplayMode(.inline)
                        .toolbar {
                            ToolbarItem(placement: .cancellationAction) {
                                Button("取消") {
                                    showAvatarUploadSheet = false
                                }
                            }
                        }
                    }
                }
                // 关键修改：将高度限制直接应用在sheet内容上
                .frame(maxHeight: .infinity)
                .presentationDetents([.fraction(0.6)]) // 占屏幕60%
                .presentationDragIndicator(.visible) // 显示拖拽指示器
            }
            .onAppear {
                // 初始化：加载用户已有信息和头像
                editedInfo = userData.userInfo ?? editedInfo
                if let pid = userData.userInfo?.avatarUrlId {
                    profilePictureGetTool.fetchPictureDetail(with: pid)
                }
            }
            .onChange(of: avatarPid) { newPid in
                if let pid = newPid {
                    editedInfo.avatarUrlId = pid
                    userData.userInfo?.avatarUrlId = pid
                }
            }
        }
        .navigationBarHidden(true)
    }

    // MARK: - 原有子视图（仅头像部分微调）
        private var profileHeader: some View {
            VStack {
                ZStack(alignment: .bottomTrailing) { // 新增：右下角添加上传图标提示
                    
                    // 头像背景装饰
                    ZStack(){
                        Circle()
                            .fill(LinearGradient(
                                gradient: Gradient(colors: [primaryColor, secondaryColor]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ))
                            .frame(width: 120, height: 120)
                            .shadow(color: primaryColor.opacity(0.3), radius: 10, x: 0, y: 5)
                        
                        // 头像图片（优先用上传后的URL）
                        if let avatarUrl = avatarUrl, !avatarUrl.isEmpty {
                            AsyncImage(url: URL(string: avatarUrl)) { phase in
                                if let image = phase.image {
                                    image
                                        .resizable()
                                        .scaledToFill()
                                } else if phase.error != nil {
                                    defaultAvatar
                                } else {
                                    ProgressView()
                                        .frame(width: 110, height: 110)
                                }
                            }
                            .frame(width: 110, height: 110)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(Color.white, lineWidth: 3))
                        } else {
                            defaultAvatar
                        }
                    }.overlay{
                        // 圆形状态指示器（直接在 View 中写判断逻辑）
                        Circle()
                            .fill(
                                // 1. 先处理 pictureDetail 为 nil 的情况（默认透明，避免崩溃）
                                profilePictureGetTool.pictureDetail?.status == nil ? Color.clear :
                                    // 2. 按 status 匹配对应颜色
                                profilePictureGetTool.pictureDetail?.status == 0 ? Color.orange :
                                    profilePictureGetTool.pictureDetail?.status == 1 ? Color.green :
                                    profilePictureGetTool.pictureDetail?.status == 2 ? Color.red :
                                    profilePictureGetTool.pictureDetail?.status == 3 ? Color.gray :
                                    // 3. 兜底颜色（status 为其他值时用浅灰）
                                Color.gray.opacity(0.5)
                            )
                            .frame(width: 20, height: 20)
                            .padding(.leading,90).padding(.bottom,80)
                        // 优化：仅当有详情数据时显示，无数据时隐藏
                            .opacity(profilePictureGetTool.pictureDetail != nil ? profilePictureGetTool.pictureDetail?.status == 1 ? 0:1 : 0)
                    }
                    // 新增9：头像右下角的上传图标提示
                    Circle()
                        .fill(Color.white)
                        .frame(width: 32, height: 32)
                        .overlay(
                            Image(systemName: "camera")
                                .resizable()
                                .frame(width: 16, height: 16)
                                .foregroundColor(primaryColor)
                        )
                        .shadow(radius: 2)
                        .offset(x: -5, y: 5)
                }
                .padding(.bottom, 8)
                
                // 个人信息（保持不变）
                VStack(spacing: 6) {
                    Text(userData.userInfo?.studentId ?? "未设置学号")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.primary)
                    
                    Text(userData.userInfo?.college ?? "未设置学院")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .background(secondaryColor.opacity(0.2))
                        .cornerRadius(12)
                }
            }.alert(isPresented: $showAlert) {
                Alert(
                    title: Text("保存失败⚠️"),
                    message: Text("QQ、微信、电话不能全部为空，请至少填写一项"),
                    dismissButton: .default(Text("确定"))
                )
            }
        }
        
        // 新增10：默认头像（统一样式）
        private var defaultAvatar: some View {
            Image(systemName: "person.fill")
                .resizable()
                .padding(20)
                .frame(width: 110, height: 110)
                .background(Color.white)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color.white, lineWidth: 3))
                .foregroundColor(primaryColor)
        }
    private var formAndButtonsSection: some View {
        VStack(spacing: 0) {
            // 分区标题
            SectionTitleView(title: "个人信息", icon: "person.text.rectangle").padding(.horizontal) // 统一控制水平边距

            VStack(alignment: .leading, spacing: 16) {
                if isEditing {
                    editingFormContent
                } else {
                    readOnlyFormContent
                }
            }
            .padding(.horizontal) // 统一控制水平边距
            
//            Divider().padding(.horizontal)
            
            buttonsSection
                .padding()
        }
    }
    
    private var editingFormContent: some View {
        Group {
            // 个人信息编辑
            Group {
                InfoTextField(title: "校区", text: Binding($editedInfo.campus, replacingNilWith: ""), icon: "building")
                InfoTextField(title: "学院", text: Binding($editedInfo.college, replacingNilWith: ""), icon: "graduationcap")
                InfoTextField(title: "专业", text: Binding($editedInfo.major, replacingNilWith: ""), icon: "book")
                InfoTextField(title: "年级", text: Binding($editedInfo.grade, replacingNilWith: ""), icon: "calendar")
                InfoTextField(title: "签名", text: Binding($editedInfo.signature, replacingNilWith: ""), icon: "quote.bubble")
                InfoTextField(title: "兴趣爱好", text: Binding($editedInfo.interests, replacingNilWith: ""), icon: "heart")
                InfoTextField(title: "技能特长", text: Binding($editedInfo.skills, replacingNilWith: ""), icon: "star")
            }
            
            // 联系方式
            SectionTitleView(title: "联系方式", icon: "bubble.left.and.bubble.right")
            Group {
                InfoTextField(title: "QQ", text: Binding($editedInfo.qq, replacingNilWith: ""), icon: "q.circle")
                InfoTextField(title: "微信", text: Binding($editedInfo.wechat, replacingNilWith: ""), icon: "message")
                InfoTextField(title: "电话", text: Binding($editedInfo.phone, replacingNilWith: ""), icon: "phone")
            }
            
            // 隐私设置
            SectionTitleView(title: "隐私设置", icon: "lock")
            Group {
                ToggleWithIcon(
                    isOn: Binding(
                        get: { editedInfo.contactVisibility == "public" },
                        set: { editedInfo.contactVisibility = $0 ? "public" : "private"
                            
                        }
                    ),
                    title: "公开联系方式",
                    icon: "globe"
                )
                
                ToggleWithIcon(
                    isOn: Binding(
                        get: { editedInfo.infoVisibility == "public" },
                        set: { editedInfo.infoVisibility = $0 ? "public" : "private" }
                    ),
                    title: "公开个人信息",
                    icon: "person.2"
                )
            }
        }
    }
    
    private var readOnlyFormContent: some View {
        Group {
            // 个人信息展示
            Group {
                MyInfoRow(title: "校区", value: userData.userInfo?.campus ?? "未设置", icon: "building")
                MyInfoRow(title: "学院", value: userData.userInfo?.college ?? "未设置", icon: "graduationcap")
                MyInfoRow(title: "专业", value: userData.userInfo?.major ?? "未设置", icon: "book")
                MyInfoRow(title: "年级", value: userData.userInfo?.grade ?? "未设置", icon: "calendar")
                MyInfoRow(title: "签名", value: userData.userInfo?.signature ?? "未设置", icon: "quote.bubble")
                MyInfoRow(title: "兴趣爱好", value: userData.userInfo?.interests ?? "未设置", icon: "heart")
                MyInfoRow(title: "技能特长", value: userData.userInfo?.skills ?? "未设置", icon: "star")
            }
            
            // 联系方式
            SectionTitleView(title: "联系方式", icon: "bubble.left.and.bubble.right")
            Group {
                MyInfoRow(title: "QQ", value: userData.userInfo?.qq ?? "未设置", icon: "q.circle")
                MyInfoRow(title: "微信", value: userData.userInfo?.wechat ?? "未设置", icon: "message")
                MyInfoRow(title: "电话", value: userData.userInfo?.phone ?? "未设置", icon: "phone")
            }
            
            // 隐私设置
            SectionTitleView(title: "隐私设置", icon: "lock")
            Group {
                ToggleWithIcon(
                    isOn: Binding(
                        get: { editedInfo.contactVisibility == "public" },
                        set: { editedInfo.contactVisibility = $0 ? "public" : "private"
                            saveProfileChanges()
                        }
                    ),
                    title: "公开联系方式",
                    icon: "globe"
                )
                
                ToggleWithIcon(
                    isOn: Binding(
                        get: { editedInfo.infoVisibility == "public" },
                        set: { editedInfo.infoVisibility = $0 ? "public" : "private"
                            saveProfileChanges()
                        }
                    ),
                    title: "公开个人信息",
                    icon: "person.2"
                )
            }
        }
    }
    
    private var buttonsSection: some View {
        Group {
            if isEditing {
                HStack(spacing: 16) {
                    Button(action: { isEditing = false }) {
                        Text("取消")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.gray.opacity(0.15))
                            .foregroundColor(.secondary)
                            .cornerRadius(12)
                    }
                    
                    Button(action: saveProfileChanges) {
                        Text("保存更改")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(primaryColor)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                            .shadow(color: primaryColor.opacity(0.3), radius: 4, x: 0, y: 2)
                    }
                }
            } else {
                Button(action: {
                    editedInfo = userData.userInfo ?? editedInfo
                    isEditing = true
                }) {
                    HStack {
                        Image(systemName: "pencil")
                        Text("编辑资料")
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(LinearGradient(
                        gradient: Gradient(colors: [primaryColor, secondaryColor]),
                        startPoint: .leading,
                        endPoint: .trailing
                    ))
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .shadow(color: primaryColor.opacity(0.3), radius: 6, x: 0, y: 3)
                }
            }
        }
    }

    @State private var showAlert = false
    func saveProfileChanges() {
        // 1. 安全判断：QQ/微信/电话 均为「nil 或 空字符串」时触发弹窗
        let isQqEmpty = editedInfo.qq?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ?? true
        let isWechatEmpty = editedInfo.wechat?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ?? true
        let isPhoneEmpty = editedInfo.phone?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ?? true
        
        // 条件：三个联系方式都为空（nil 或 纯空格/空字符串）
        if isQqEmpty && isWechatEmpty && isPhoneEmpty {
            showAlert = true
            return // 若需阻止保存，可加 return；若需「弹窗但仍保存」，则删除 return
        }
        
        // 2. 调用更新接口，补充错误处理（避免成功/失败分支不对称）
        userData.updateUserInfo(userInfo: editedInfo) { success in
            if success {
                self.isEditing = false
                // 可选：添加保存成功的提示（如 Toast/Alert）
                // self.showSuccessAlert = true
            } else {
                // 修复：补充失败分支逻辑（原代码为空，用户无反馈）
                self.showAlert = true // 需提前定义 @State private var showErrorAlert = false
                // 可选：记录错误日志
                print("修改个人信息失败")
            }
        }
    }
}

// MARK: - 自定义修饰符和组件
extension View {
    func cardStyle(backgroundColor: Color) -> some View {
        self
            .background(backgroundColor)
            .cornerRadius(20)
            .shadow(color: Color.black.opacity(0.08), radius: 10, x: 0, y: 5)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color.gray.opacity(0.1), lineWidth: 1)
            )
    }
}
struct SectionTitleView: View {
    let title: String
    let icon: String?
    
    init(title: String, icon: String? = nil) {
        self.title = title
        self.icon = icon
    }
    
    var body: some View {
        HStack {
            if let icon = icon {
                Image(systemName: icon)
                    .foregroundColor(Color(red: 1.0, green: 0.4, blue: 0.2))
            }
            Text(title)
                .font(.headline)
                .padding(.vertical, 14)
            Spacer()
        }
        .foregroundColor(.primary)
    }
}

struct MyInfoRow: View {
    let title: String
    let value: String
    let icon: String
    
    var body: some View {
        HStack(alignment: .top) {
            Image(systemName: icon)
                .frame(width: 24)
                .foregroundColor(Color(red: 1.0, green: 0.4, blue: 0.2).opacity(0.8))
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(value)
                    .foregroundColor(.primary)
            }
            Spacer()
        }
    }
}

struct InfoTextField: View {
    let title: String
    @Binding var text: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(Color(red: 1.0, green: 0.4, blue: 0.2).opacity(0.8))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
                TextField("", text: $text)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
            }
        }
    }
}

struct ToggleWithIcon: View {
    @Binding var isOn: Bool
    let title: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(Color(red: 1.0, green: 0.4, blue: 0.2).opacity(0.8))
            
            Toggle(title, isOn: $isOn)
                .toggleStyle(SwitchToggleStyle(tint: Color(red: 1.0, green: 0.4, blue: 0.2)))
        }
    }
}

extension String {
    func toRelativeDateString() -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withDashSeparatorInDate, .withColonSeparatorInTime]
        formatter.timeZone = TimeZone.current // 使用本地时区解析
        
        // 尝试解析带时区的格式（实际是本地时区）
        if let date = formatter.date(from: self) {
            return processDate(date: date)
        }
        
        // 尝试解析不带时区的格式（明确使用本地时区）
        let formatterWithoutTimeZone = ISO8601DateFormatter()
        formatterWithoutTimeZone.formatOptions = [.withInternetDateTime, .withDashSeparatorInDate, .withColonSeparatorInTime]
        formatterWithoutTimeZone.timeZone = TimeZone.current // 改为本地时区，原代码是UTC
        
        if let date = formatterWithoutTimeZone.date(from: self) {
            return processDate(date: date)
        }
        
        // 备选格式解析（使用本地时区）
        let alternativeFormatter = DateFormatter()
        alternativeFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        alternativeFormatter.timeZone = TimeZone.current // 改为本地时区，原代码是UTC
        alternativeFormatter.locale = Locale(identifier: "en_US_POSIX")
        
        if let date = alternativeFormatter.date(from: self) {
            return processDate(date: date)
        }
        
        return self // 所有解析尝试都失败就原样返回
    }

    private func processDate(date: Date) -> String {
        let calendar = Calendar.current
        let now = Date()
        
        // 计算日期差值（基于本地时区的起始日）
        let components = calendar.dateComponents([.day],
                                                from: calendar.startOfDay(for: date),
                                                to: calendar.startOfDay(for: now))
        let dayDiff = components.day ?? 0
        
        let timeFormatter = DateFormatter()
        timeFormatter.dateFormat = "HH:mm"
        timeFormatter.timeZone = TimeZone.current // 保持本地时区显示
        let timeString = timeFormatter.string(from: date)
        
        switch dayDiff {
        case 0:
            return "今天 \(timeString)"
        case 1:
            return "昨天 \(timeString)"
        case 2:
            return "前天 \(timeString)"
        default:
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "MM-dd HH:mm"
            dateFormatter.timeZone = TimeZone.current // 保持本地时区显示
            return dateFormatter.string(from: date)
        }
    }
}
// MARK: - 辅助扩展
extension Binding where Value == String? {
    init(_ source: Binding<Value>, replacingNilWith nilPlaceholder: String) {
        self.init(
            get: {
                source.wrappedValue ?? nilPlaceholder
            },
            set: { newValue in
                if newValue == nilPlaceholder {
                    source.wrappedValue = nil
                } else {
                    source.wrappedValue = newValue
                }
            }
        )
    }
}
