//
//  MyActivityCard.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/8/20.
//

import SwiftUI
struct MyActivityCard: View {
        let activity: Activity
        @State private var isExpanded = false
        @State private var hasInitialized = false
        // 状态管理
        @State  var isLiked:Bool
        @State  var isfavorite:Bool
        @State private var showComments = false
        @State private var isLoading = false
        @State private var showLikeError = false
        @State private var showingApplySheet = false
        @State private var applyReason = ""
        @State private var showDeleteConfirmation = false
        @State private var isDeleting = false
        @State private var deleteError: String?
        @State private var unprocessedCount: Int = 0 // 示例值，根据实际情况更新
         var commentCount:Int
        @AppStorage("ID") var StudentNumber:String=""
        //全局绑定的选中的id
        @Binding  var activityID:Int
        // 颜色定义
        let primaryColor = Color.blue
        let likedColor = Color.red
        let favoriteColor = Color.orange
        // 环境对象
        @EnvironmentObject var userData: UserData
        @Environment(\.presentationMode) var presentationMode
        // 新增：删除回调
        var onDelete: (() -> Void)?
        private var canDelete: Bool {
            // 假设 StudentNumber 是字符串类型
            if String(activity.creatorId) == StudentNumber {
                return true
            } else {
                return false
            }
        }
    @StateObject private var viewModel = ImageUploadViewModel()
    var image_url: String? {
        //如果已发布
        viewModel.pictureDetail?.url
    }
    func fetchUnprocessedApplicationsCount(for activityId: Int, completion: @escaping (Int) -> Void) {
        print("正在获取活动ID为 \(activityId) 的申请信息")
        
        // 检查用户认证状态
        guard !userData.authToken.isEmpty else {
            print("用户未登录，无法获取申请信息")
            completion(0)
            return
        }
        
        // 构建请求URL
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/applications")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        
        // 发起网络请求
        URLSession.shared.dataTask(with: request) { data, response, error in
            // 错误处理
            if let error = error {
                print("获取申请信息失败: \(error.localizedDescription)")
                completion(0)
                return
            }
            
            // 数据验证
            guard let data = data else {
                print("未接收到有效数据")
                completion(0)
                return
            }
            
            // 调试输出
            if let jsonString = String(data: data, encoding: .utf8) {
                print("服务器响应: \(jsonString)")
            }
            
            do {
                // 解析响应数据
                let apiResponse = try JSONDecoder().decode(ApiResponse<[Application]>.self, from: data)
                
                if apiResponse.code == 0, let applications = apiResponse.data {
                    let unprocessedCount = applications.filter { $0.updatedAt == "2024-01-15T12:28:59" }.count
                    print("成功获取到 \(unprocessedCount) 条未处理申请")
                    completion(unprocessedCount)
                } else {
                    print("服务器返回错误代码: \(apiResponse.code), 消息: \(apiResponse.msg)")
                    completion(0)
                }
            } catch {
                print("JSON解析失败: \(error.localizedDescription)")
                completion(0)
            }
        }.resume()
    }
        // 新增删除方法
            private func deleteActivity() {
                isDeleting = true
                deleteError = nil
                userData.deleteActivity(activityId: activity.id) { success in
                    DispatchQueue.main.async {
                        isDeleting = false
                        if success {
                            // 删除成功后执行回调
                            onDelete?()
                        } else {
                            deleteError = "删除失败，请稍后重试"
                        }
                    }
                }
            }
        var body: some View {
            VStack(alignment: .leading) {
                // 活动头部信息
                HStack {
                    VStack(alignment: .leading) {
                        Text(activity.title)
                            .font(.headline).foregroundColor(Color.black)
                        
                        Text("\(activity.campus ?? "") · \(activity.college ?? "")")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                    }
                    
                    Spacer()
                    
                    Text(activity.type)
                        .font(.caption)
                        .padding(8)
                        .background(primaryColor.opacity(0.1))
                        .foregroundColor(primaryColor)
                        .cornerRadius(8)
                    if canDelete {
                        Button(action: {
                            showDeleteConfirmation = true
                        }) {
                            Image(systemName: "trash")
                                .foregroundColor(.red)
                                .padding(8)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                // 活动图片
                if let imageUrl = image_url, !imageUrl.isEmpty {
                    ZStack(){
                        AsyncImage(url: URL(string: imageUrl)) { image in
                            image.resizable()
                        } placeholder: {
                            ProgressView()
                        }
                        .frame(width:320,height: 200)
                        .cornerRadius(12)
                        .padding(.vertical, 5)
                    }.overlay{
                        // 圆形状态指示器（直接在 View 中写判断逻辑）
                        Circle()
                            .fill(
                                // 1. 先处理 pictureDetail 为 nil 的情况（默认透明，避免崩溃）
                                viewModel.pictureDetail?.status == nil ? Color.clear :
                                    // 2. 按 status 匹配对应颜色
                                viewModel.pictureDetail?.status == 0 ? Color.orange :
                                    viewModel.pictureDetail?.status == 1 ? Color.green :
                                    viewModel.pictureDetail?.status == 2 ? Color.red :
                                    viewModel.pictureDetail?.status == 3 ? Color.gray :
                                    // 3. 兜底颜色（status 为其他值时用浅灰）
                                Color.gray.opacity(0.5)
                            )
                            .frame(width: 20, height: 20)
                            .padding(.leading, 320)
                            .padding(.bottom, 200)
                             //优化：仅当有详情数据时显示，无数据时隐藏
                            .opacity(viewModel.pictureDetail != nil ? viewModel.pictureDetail?.status == 1 ? 0:1 : 0)
                    }
                }
                
                // 活动详情
                Text(activity.description)
                    .font(.body).foregroundColor(Color.black)
                    .lineLimit(isExpanded ? nil : 3)
                
                // 活动时间地点
                VStack(alignment: .leading, spacing: 5) {
                    HStack {
                        Image(systemName: "calendar").foregroundColor(Color.black)
                        Text("\(activity.activityTime)").foregroundColor(Color.black)
                    }
                    
                    HStack {
                        Image(systemName: "mappin.and.ellipse").foregroundColor(Color.orange)
                        Text(activity.location).foregroundColor(Color.black)
                    }
                }
                .font(.subheadline)
                .foregroundColor(.secondary)
                .padding(.top, 5)
                
                // 人数限制
                HStack {
                    Image(systemName: "person.2").foregroundColor(Color.blue).padding(.leading,-2)
                    Text("\(activity.minPeople)-\(activity.maxPeople)人").foregroundColor(Color.black)
                }
                .font(.subheadline)
                .foregroundColor(.secondary)
                
                // 操作按钮
                HStack {
                    // 1. 点赞按钮
                    Button(action: toggleLike) {
                        HStack {
                            Image(systemName: isLiked ? "heart.fill" : "heart")
                                .foregroundColor(isLiked ? likedColor : .black)
                            Text("\(userData.activityStats[activity.id]?.likeCount ?? 0)").foregroundColor(Color.black)
                        }
                    }
                    .buttonStyle(PlainButtonStyle())
                    .padding(.trailing, 15)
                    
                    // 2. 评论按钮
                    Button(action: {
                        activityID=activity.id
                        showComments.toggle()
                    }) {
                        HStack {
                            Image(systemName: "message").foregroundColor(Color.black)
                            Text("\(commentCount)").foregroundColor(Color.black)
                        }
                    }
                    .buttonStyle(PlainButtonStyle())
                    .sheet(isPresented: $showComments) {
                        //传入选中的id
                        MyCommentView( ActivityId: $activityID, CreatedId: activity.creatorId, onSave: {
                            text in
                            userData.addComment(activityId: activity.id, content: text, completion: {
                                success in
                                   if success {
                                       //刷新当前评论
                                       userData.fetchComments(for: activity.id)
                                       print("评论发布成功")
                                   } else {
                                       print("评论发布失败")
                                   }
                            })
                        }, onRefresh: {
                            userData.fetchComments(for: activity.id)
                            print("评论删除成功")
                        }).environmentObject(userData)
                   
                    }
                    .padding(.trailing, 15)
                    // 1. 收藏按钮
                    Button(action: toggleFavorite) {
                        HStack {
                            Image(systemName: isfavorite ? "star.fill" : "star")
                                .foregroundColor(isfavorite ? favoriteColor : .black)
                            Text("\(userData.activityStats[activity.id]?.favoriteCount ?? 0)").foregroundColor(Color.black)
                        }
                    }
                    .buttonStyle(PlainButtonStyle())
                    .padding(.trailing, 15)
                    Spacer()
                    // 3. 申请参加按钮 - 现代化设计
                    NavigationLink(destination: ApplyAction(activityId:activity.id), label: {
                        Text("处理申请")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 10)
                            .background(
                                LinearGradient(
                                    gradient: Gradient(colors: [Color.orange, Color.pink.opacity(0.7)]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .foregroundColor(.white)
                            .cornerRadius(12)
                            .shadow(color: Color.orange.opacity(0.3), radius: 5, x: 0, y: 2)
                            .overlay(
                                // 右上角小红点，显示未处理信息数量
                                Group {
                                    if unprocessedCount>0 {
                                            Text("\(unprocessedCount)")
                                            .font(.system(size: 15))
                                            .foregroundColor(.white)
                                            .padding(5)
                                            .background(Color.red)
                                            .clipShape(Circle())
                                            .offset(x: 10, y: -15)
                                    }
                                },
                                alignment: .topTrailing
                            )
                    })
                }
            }.onAppear(){
                fetchUnprocessedApplicationsCount(for: activity.id) { count in
                    print("未处理申请数量: \(count)")
                    // 在这里更新UI或处理结果
                    DispatchQueue.main.async {
                        self.unprocessedCount = count
                    }
                }
                viewModel.fetchPictureDetail(with: activity.imageId ?? 0)
            }
            .padding()
            .background(Color.white)
            .cornerRadius(12)
            .shadow(radius: 2)
            .onTapGesture {
                withAnimation {
                    isExpanded.toggle()
                }
            }.onChange(of: isLiked) { _ in
                print("isLiked changed: \(isLiked)")
            }
            .alert(isPresented: $showDeleteConfirmation) {
                Alert(
                    title: Text("删除活动"),
                    message: Text("确定要永久删除此活动吗？此操作不可撤销。"),
                    primaryButton: .destructive(Text("删除")) {
                        deleteActivity()
                    },
                    secondaryButton: .cancel()
                )
            }
        // 删除状态提示
            .overlay {
                if isDeleting {
                    ProgressView("删除中...")
                        .padding(20)
                        .background(Color.black.opacity(0.8))
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
            
        }
        // 点赞功能
        private func toggleLike() {
            isLiked.toggle()
            //
            if !isLiked{
                userData.activityStats[activity.id]?.likeCount = (userData.activityStats[activity.id]?.likeCount ?? 0) - 1
            }else{
                userData.activityStats[activity.id]?.likeCount = (userData.activityStats[activity.id]?.likeCount ?? 0) + 1
            }
            userData.likeActivity(activityId: activity.id) { success in
                DispatchQueue.main.async {
                    isLoading = false
                    if success {
                        print("成功🏅")
                    } else {
                       print("失败☹️")
                    }
                }
            }
        }
        //收藏功能
        private func toggleFavorite() {
            isfavorite.toggle()
            if !isfavorite{
                userData.activityStats[activity.id]?.favoriteCount = (userData.activityStats[activity.id]?.favoriteCount ?? 0) - 1
            }else{
                userData.activityStats[activity.id]?.favoriteCount = (userData.activityStats[activity.id]?.favoriteCount ?? 0) + 1
            }
            userData.FavoriteActivity(activityId: activity.id) { success in
                DispatchQueue.main.async {
                    isLoading = false
                    if success {
                        print("成功🏅")
                    } else {
                       print("失败☹️")
                    }
                }
            }
        }
    }
struct ApplyAction: View {
    @EnvironmentObject var userData: UserData
    @Environment(\.presentationMode) var presentationMode
    @State var showInformation:Bool=false
    let activityId:Int
    @State var otherPeopleUid:Int = 0
    @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式
    private var backgroundColor: Color {
        colorScheme == .dark ? Color(red: 0.08, green: 0.08, blue: 0.1) : Color(red: 1.0, green: 0.96, blue: 0.9)
    }
        // 根据颜色模式动态返回颜色
        private var primaryColor: Color {
            colorScheme == .dark ? Color(red: 1.0, green: 0.5, blue: 0.3) : Color(red: 1.0, green: 0.4, blue: 0.2)
        }
    @State var applications: [Application] = []
    @State var profileDict: [Int: String] = [:]    // 在视图中添加计算属性
    var sortedApplications: [Application] {
        applications.sorted { app1, app2 in
            let isPending1 = app1.status == "PENDING"
            let isPending2 = app2.status == "PENDING"
            
            if isPending1 && !isPending2 {
                return true
            } else if !isPending1 && isPending2 {
                return false
            } else {
                return app1.createdAt > app2.createdAt // 相同状态按时间倒序
            }
        }
    }
    func getProfilePictures(activityId:Int) {
        print("获取此活动申请")
        guard !userData.authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/profilePictures")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else { return }
            do {
                let response = try JSONDecoder().decode(ApiResponse<[UserProfileInfo]>.self, from: data)
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                if response.code == 0, let profilePictures = response.data {
                    // 创建字典用于存储数据
                    var profileDict: [Int: String] = [:]
                    // 遍历数组，将数据存入字典
                    for profile in profilePictures {
                        profileDict[profile.appid] = profile.profilePicturesUrl
                    }
                    DispatchQueue.main.async {
                        print("成功获取到数据~~~~~~~~")
                        self.profileDict = profileDict
                        // 可以在这里使用 profileDict，比如赋值给属性
                        self.profileDict = profileDict
                    }
                }
            } catch {
                print("Error decoding notifications: \(error)")
            }
        }.resume()
    }

    // 获取通知列表
    func fetchNotifications(activityId:Int) {
        print("获取此活动申请")
        guard !userData.authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/applications")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else { return }
            do {
                let response = try JSONDecoder().decode(ApiResponse<[Application]>.self, from: data)
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                if response.code == 0, let applications = response.data {
                    DispatchQueue.main.async {
                        print("成功获取到数据")
                        self.applications = applications
                    }
                }
            } catch {
                print("Error decoding notifications: \(error)")
            }
        }.resume()
    }

   
    var body: some View {
        NavigationView{
            ZStack(){
//                LinearGradient(
//                    gradient: Gradient(colors: [Color(red: 1.0, green: 0.8, blue: 0.4).opacity(0.3), Color(red: 1.0, green: 0.96, blue: 0.9)]),
//                    startPoint: .topLeading,
//                    endPoint: .bottomTrailing
//                ).edgesIgnoringSafeArea(.top)
                LinearGradient(
                    gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .edgesIgnoringSafeArea(.top)
                if(sortedApplications.count==0){
                    VStack(){
                        Text("暂时没有活动的申请😯～").foregroundColor(Color.gray).padding()
                        Text("再等等看 搭子正在路上～").foregroundColor(Color.gray)
                    }
                }
                VStack(){
                    ForEach(sortedApplications) { application in
                        ApplicationRow(showOtherInformation: $showInformation, OtherPeopleUid: $otherPeopleUid, application: application, ProfilePicturesUrl: profileDict[application.id] ?? "").environmentObject(userData)
                            .listRowBackground(Color.clear)
                            .listRowInsets(EdgeInsets(top: 8, leading: 0, bottom: 8, trailing: 0))
                        
                    }
                    .listStyle(PlainListStyle())
                    .sheet(isPresented: $showInformation, content: {
                        OtherPeopleProfile(otherPeopleUid: $otherPeopleUid).presentationDetents([.fraction(0.8)])
                        
                    })
                    Spacer()
                }
         
            }.toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()  // 返回上一视图
                    }) {
                        HStack {
                            Image(systemName: "chevron.left")  // 返回箭头
                            Text("我的发布")  // 可选的文本
                        }
                    }
                }
            }.onAppear(){
                fetchNotifications(activityId: activityId)
                getProfilePictures(activityId: activityId )
                
            }
            .navigationBarHidden(true)
        }
    }
    struct ApplicationRow: View {
        @Binding var showOtherInformation:Bool
        @Binding var OtherPeopleUid:Int
        var ifAccept: Bool {
            application.status == "ACCEPTED"
        }
        @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式
        @State var application: Application
        private let primaryColor = Color(red: 1.0, green: 0.4, blue: 0.2) // 活力珊瑚橙
        private let secondaryColor = Color(red: 1.0, green: 0.8, blue: 0.4) // 阳光黄
        @EnvironmentObject var userData: UserData
        @State var ProfilePicturesUrl:String
        func AcceptNotifications(completion: @escaping (Bool) -> Void) {
            print("接受此活动申请")
            guard !userData.authToken.isEmpty else { return }
            let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/applications/\(application.id)/accept")!
            var request = URLRequest(url: url)
            request.httpMethod = "PUT"
            request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
            URLSession.shared.dataTask(with: request) { data, response, error in
                guard let data = data else {
                    completion(false)
                    return
                }
                do {
                    if let jsonString = String(data: data, encoding: .utf8) {
                        print("Response JSON: \(jsonString)")
                    }
                    let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                    DispatchQueue.main.async {
                        if response.code == 0 {
                            completion(true)
                        } else {
                            completion(false)
                        }
                    }
                } catch {
                    print("Error decoding application response: \(error)")
                    completion(false)
                }
                }.resume()
            }
        func CancleAcceptNotifications(completion: @escaping (Bool) -> Void) {
            print("撤销接受此活动申请")
            guard !userData.authToken.isEmpty else { return }
            let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/applications/\(application.id)/regret")!
            var request = URLRequest(url: url)
            request.httpMethod = "PUT"
            request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
            URLSession.shared.dataTask(with: request) { data, response, error in
                guard let data = data else {
                    completion(false)
                    return
                }
                do {
                    if let jsonString = String(data: data, encoding: .utf8) {
                        print("Response JSON: \(jsonString)")
                    }
                    let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                    DispatchQueue.main.async {
                        if response.code == 0 {
                            completion(true)
                        } else {
                            completion(false)
                        }
                    }
                } catch {
                    print("Error decoding application response: \(error)")
                    completion(false)
                }
                }.resume()
            }
        func getProfileInfo(userid:Int,completion: @escaping (Bool) -> Void) {
            print("获取个人信息申请")
            guard !userData.authToken.isEmpty else { return }
            let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/applications/\(application.id)/regret")!
            var request = URLRequest(url: url)
            request.httpMethod = "PUT"
            request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
            URLSession.shared.dataTask(with: request) { data, response, error in
                guard let data = data else {
                    completion(false)
                    return
                }
                do {
                    if let jsonString = String(data: data, encoding: .utf8) {
                        print("Response JSON: \(jsonString)")
                    }
                    let response = try JSONDecoder().decode(ApiResponse<UserInfo>.self, from: data)
                    DispatchQueue.main.async {
                        if response.code == 0 {
                            completion(true)
                        } else {
                            completion(false)
                        }
                    }
                } catch {
                    print("Error decoding application response: \(error)")
                    completion(false)
                }
                }.resume()
        }
        var body: some View {
            HStack(alignment: .top, spacing: 10) {
                // 头像按钮
                Button {
                    print("???")
                    print(application.applicantId)
                    OtherPeopleUid=application.applicantId
                    showOtherInformation = true
                } label: {
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [primaryColor, secondaryColor]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 60, height: 60)
                            .shadow(color: primaryColor.opacity(0.3), radius: 10, x: 0, y: 5)
                        // 如果 profilePicturesUrl 是非可选类型
                        if !ProfilePicturesUrl.isEmpty {
                            AsyncImage(url: URL(string: ProfilePicturesUrl)) { phase in
                                switch phase {
                                case .success(let image):
                                    image
                                        .resizable()
                                        .scaledToFill()
                                case .failure:
                                    Image(systemName: "person.fill")
                                        .resizable()
                                        .padding(20)
                                        .background(Color.white)
                                case .empty:
                                    ProgressView()
                                @unknown default:
                                    ProgressView()
                                }
                            }
                            .frame(width: 50, height: 50)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(Color.white, lineWidth: 3))
                        } else {
                            // 处理空字符串的情况
                            Image(systemName: "person.crop.circle")
                                .resizable().foregroundColor(Color.orange)
                                .frame(width: 40, height: 40)
                                .padding(10)
                                .background(Color.white)
                                .clipShape(Circle())
                                .overlay(Circle().stroke(Color.white, lineWidth: 3))
                        }
                    }
                    .frame(width: 60, height: 60) // ✅ 保证整个按钮只有 60x60
                }
                .buttonStyle(PlainButtonStyle()) // ✅ 去掉系统自动扩展点击范围
                // 文本信息
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(application.reason ?? "")")
                        .font(.caption2)
                        .lineLimit(1)
                        .foregroundColor(.gray)
                    Text(application.createdAt.toRelativeDateString())
                        .font(.caption2)
                        .foregroundColor(primaryColor)
                }
                Spacer()
                // 同意按钮
                    Button {
                       if !ifAccept{
                            withAnimation(.easeInOut(duration: 0.3)) {
                                application.status="ACCEPTED"
                                AcceptNotifications() { success in
                                    
                                }
                            }
                        }else{
                            withAnimation(.easeInOut(duration: 0.3)) {
                                application.status="PENDING"
                                CancleAcceptNotifications() { success in
                                }
                            }
                        }
                    } label: {
                        Text(!ifAccept ?"同意":"已同意")
                            .foregroundColor(.white)
                            .frame(width: 80, height: 40)
                            .background(
                                RoundedRectangle(cornerRadius: 10)
                                    .fill(!ifAccept ? Color.orange.opacity(0.8) : Color.green.opacity(0.8))
                            ).padding(.trailing).padding(.top,10)
                    }
                    .buttonStyle(PlainButtonStyle()) // ✅ 禁止 SwiftUI 默认放大点按区域
                    .animation(.easeInOut(duration: 0.3), value: ifAccept)
                // 未读小圆点
                if application.updatedAt=="2024-01-15T12:28:59" {
                    Circle()
                        .fill(primaryColor)
                        .frame(width: 8, height: 8)
                }
            }
            .padding(10)
            .background(Color.white)
            .cornerRadius(12)
            .contentShape(Rectangle()) // ✅ 外层容器不抢占点击事件

        }
        
    }

}
struct UserProfileInfo: Codable{
    let appid:Int
    let profilePicturesUrl:String
}
