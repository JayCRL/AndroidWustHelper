//
//  NotificationTable.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/8/19.
//

import SwiftUI
// 1. 定义枚举：包含所有可能的跳转目标页面
enum NavigationDestination: Hashable {
    case notificationList    // 通知列表页
    case myPublish           // 我的发布页
    case myApply             // 我的申请页
    case myFavorite          // 我的收藏页
    case myLike              // 我的喜欢页
}
struct NotificationTable: View {
    @Environment(\.colorScheme) var colorScheme
    @EnvironmentObject var userData: UserData
    @State var showInformation:Bool=false
    //通知栏
    @Environment(\.presentationMode) var presentationMode
    // 暖色调配色方案
    // 根据颜色模式动态返回颜色
    private var primaryColor: Color {
        colorScheme == .dark ? Color(red: 1.0, green: 0.5, blue: 0.3) : Color(red: 1.0, green: 0.4, blue: 0.2)
    }
    
    private var secondaryColor: Color {
        colorScheme == .dark ? Color(red: 0.6, green: 0.8, blue: 1.0): Color(red: 1.0, green: 0.8, blue: 0.4)
    }
    
    private var backgroundColor: Color {
        colorScheme == .dark ? Color(red: 0.08, green: 0.08, blue: 0.1) : Color(red: 1.0, green: 0.96, blue: 0.9)
    }
    
    private let cardColor = Color.white
    // 2. 状态变量：控制当前选中的跳转目标（nil表示无选中）
    @State private var selectedDestination: NavigationDestination?
    var NOTICENUMBER: Int {
        userData.Noti.filter { !$0.isRead }.count
    }

    // ---------------------- 核心：控制跳转的函数 ----------------------
      /// 1. 手动触发跳转：指定目标页面
      private func jumpToDestination(_ destination: NavigationDestination) {
          selectedDestination = destination
      }
      
     
    var body: some View {
        NavigationView{
            ZStack{
                // 渐变背景
                LinearGradient(
                    gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                ).edgesIgnoringSafeArea(.top)
            VStack(){
                // 3. 动态导航链接：通过 tag/selection 绑定目标
                // 通知列表链接
                NavigationLink(
                    tag: .notificationList,
                    selection: $selectedDestination,
                    destination: {
                        NotificationListView( selectedDestination: $selectedDestination).environmentObject(userData)
                    },
                    label: { EmptyView() }  // 隐藏默认链接样式（用自定义按钮触发）
                )
                
                // 我的发布链接
                NavigationLink(
                    tag: .myPublish,
                    selection: $selectedDestination,
                    destination: { MyPublishActivities() },
                    label: { EmptyView() }
                )
                
                // 我的申请链接
                NavigationLink(
                    tag: .myApply,
                    selection: $selectedDestination,
                    destination: { MyApply() },
                    label: { EmptyView() }
                )
                
                // 我的收藏链接
                NavigationLink(
                    tag: .myFavorite,
                    selection: $selectedDestination,
                    destination: { MyFavorite() },
                    label: { EmptyView() }
                )
                
                // 我的喜欢链接
                NavigationLink(
                    tag: .myLike,
                    selection: $selectedDestination,
                    destination: { MyLike() },
                    label: { EmptyView() }
                )
                
                // ---------------------- 原有UI布局 ----------------------
                // 1. 通知按钮（点击触发函数，自动判断跳转）
                    Button(action: {
                        // 点击时调用函数，自动跳转“通知列表”
                        jumpToDestination(.notificationList)
                    }) {
                        RoundedRectangle(cornerRadius: 25)
                            .frame(width: 150, height: 50)
                            .foregroundColor(Color.white)
                            .shadow(color: Color.orange.opacity(0.3), radius: 10)
                            .overlay {
                                HStack {
                                    Image(systemName: "bell")
                                        .font(.system(size: 20))
                                        .foregroundColor(Color(red: 1.0, green: 0.4, blue: 0.2))
                                        .frame(width: 30)
                                    if NOTICENUMBER != 0 {
                                        Text("\(NOTICENUMBER)条新消息")
                                    }else{
                                        Text("通知中心")
                                    }
                                }
                            }
                    }
                    .padding(.top, 30)
                    .padding(.bottom, 30)
                
                
                // 2. 其他设置项（点击触发对应跳转）
                Button(action: { jumpToDestination(.myPublish) }) {
                    SettingRow(icon: "doc.text.fill", title: "我的发布", color: .blue)
                }
                
                Button(action: { jumpToDestination(.myApply) }) {
                    SettingRow(icon: "hand.raised", title: "我的申请", color: .green)
                }
                
                Button(action: { jumpToDestination(.myFavorite) }) {
                    SettingRow(icon: "star.fill", title: "我的收藏", color: .orange)
                }
                
                Button(action: { jumpToDestination(.myLike) }) {
                    SettingRow(icon: "heart.fill", title: "我的喜欢", color: .pink)
                }
                
                Spacer()
            }.navigationTitle("活动管理")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button(action: {
                            presentationMode.wrappedValue.dismiss()  // 返回上一视图
                        }) {
                            HStack {
                                Image(systemName: "chevron.left")  // 返回箭头
                                Text("首页")  // 可选的文本
                            }
                        }
                    }
                }
             }
        }.onAppear(){
            // 模拟通知数据
            userData.fetchNotifications()
        }.navigationBarHidden(true)
    }
    
    //选项栏
    private func SettingRow(icon: String, title: String, color: Color) -> some View {
        HStack {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(color)
                .frame(width: 30)
            
            Text(title)
                .font(.headline)
                .foregroundColor(.black)
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.white.opacity(0.5))
        }
        .padding()
        .background(Color.white.opacity(0.9))
        .clipShape(Capsule())           // ✅ 改成胶囊形状
        .padding(.horizontal, 16)       // ✅ 距离左右两边 16pt

    }
}
struct MyPublishActivities:View{
    struct Comment: Codable, Identifiable {
        // 根据服务器返回的评论结构定义相应的属性
        let id: Int
        let activityId: Int
        let content: String
        let parentId: Int?
        let createdAt: String
    }
    @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式

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
    
    //收藏内容
    @State private var filterCampus: String?
    @State private var filterCollege: String?
    @State private var filterType: String?
    @State var Favoriteactivities: [Activity] = []
    @EnvironmentObject var userData: UserData
    @State private var searchText = ""
    @State var activityClickedid:Int=0
    @State var activityComments: [Int: [Comment]] = [:]
    @State var likedActivityIDs: Set<Int> = []
    @State var activityStats: [Int: ActivityStats] = [:]
    @Environment(\.presentationMode) var presentationMode

    private var filteredActivities: [Activity] {
        // 如果搜索文本为空，返回所有活动
        guard !searchText.isEmpty else {
            return Favoriteactivities
        }
        // 模糊查询逻辑：不区分大小写，匹配标题、描述或地点
        return Favoriteactivities.filter { activity in
            let searchLowercased = searchText.lowercased()
            return activity.title.lowercased().contains(searchLowercased) ||
                   activity.description.lowercased().contains(searchLowercased) ||
                   activity.location.lowercased().contains(searchLowercased) ||
                   activity.type.lowercased().contains(searchLowercased)
        }
    }
    // 获取活动评论列表
    func fetchComments(
        for activityId: Int,
        page: String? = nil,
        size: String? = nil
    ) {
        guard !userData.authToken.isEmpty else { return }
        var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/comments/activity/getAllComments/\(activityId)")!
        var queryItems: [URLQueryItem] = []
        if let page = page { queryItems.append(URLQueryItem(name: "page", value: page)) }
        if let size = size { queryItems.append(URLQueryItem(name: "size", value: size)) }
        components.queryItems = queryItems
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[Comment]>.self, from: data)
                if decoded.code == 0, let comments = decoded.data {
                    DispatchQueue.main.async {
                        // 将获取到的评论写入 activityComments 字典
                        self.activityComments[activityId] = comments
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }
    func fetchLikedId() {
        guard !userData.authToken.isEmpty else { return }
        var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/Activity/getAllLikedId")!
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            if let rawString = String(data: data, encoding: .utf8) {
                print("🔥 Raw response: \(rawString)")
            } else {
                print("⚠️ Response is nil or not UTF-8")
            }
            
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[LikeRecord]>.self, from: data)
                if decoded.code == 0, let likeRecords = decoded.data {
                    DispatchQueue.main.async {
                        self.likedActivityIDs = Set(likeRecords.map { $0.activityId })
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }
    //获取状态
    func fetchActivityStats(activityId: Int) {
        guard !userData.authToken.isEmpty else { return }
        let urlString = "\(BasicValue.baseParseUrl):8083/api/activities/\(activityId)/stats"
        guard let url = URL(string: urlString) else {
            print("❌ 无效的 URL")
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        print("ok")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("❌ 网络请求失败: \(error?.localizedDescription ?? "未知错误")")
                return
            }
            if let jsonString = String(data: data, encoding: .utf8) {
                print("Response JSON: \(jsonString)")
            }
            do {
                let response = try JSONDecoder().decode(ApiResponse<ActivityStats>.self, from: data)
                if response.code == 0, let stats = response.data {
                    DispatchQueue.main.async {
                        print(stats)
                        self.activityStats[activityId]=stats // 假设你有个变量来存
                    }
                } else {
                    print("⚠️ 接口返回错误: \(response.msg)")
                }
            } catch {
                print("❌ JSON 解码失败: \(error)")
            }
        }.resume()
    }
    func fetchAllActivityStats() {
        for activity in Favoriteactivities {
            fetchActivityStats(activityId: activity.id)
        }
    }
    func fetchMyPublishedActivities() {
        guard !userData.authToken.isEmpty else { return }
        let components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/user/created")!
       
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
               guard let data = data else { return }
               if let rawString = String(data: data, encoding: .utf8) {
                   print("🔥 Raw Activity : \(rawString)")
               } else {
                  print("⚠️ Response  Activity")
                  }
               do {
                   let response = try JSONDecoder().decode(ApiResponse<[Activity]>.self, from: data)
                   if response.code == 0, let activities = response.data {
                       DispatchQueue.main.async {
                           self.Favoriteactivities = activities
                           // ✅ 加载评论
                           for activity in activities {
                               self.fetchComments(for: activity.id)
                           }
                           //获取点赞id
                           self.fetchLikedId()
                           self.fetchAllActivityStats()
                       }
                   }
               } catch {
                   print("Error decoding activities: \(error)")
               }
           }.resume()
    }
    var body: some View{
        ZStack(){
            // 渐变背景
            LinearGradient(
                gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ).edgesIgnoringSafeArea(.top)
            NavigationView {
                ZStack(){
                    LinearGradient(
                        gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                    .edgesIgnoringSafeArea(.all)
                    
                    ScrollView {
                        LazyVStack {
                            // 搜索栏
                            SearchBar(text: $searchText, placeholder: "搜索活动...")
                                .padding()
                            // 活动列表
                            if(filteredActivities.isEmpty){
                                VStack(){
                                    Text("还没有发布活动哦😯").foregroundColor(Color.gray).padding()
                                    Text("去发布新活动吧🎉🎉🎉").foregroundColor(Color.gray)

                                }
                                }else{
                                ForEach(filteredActivities) { activity in
                                    MyActivityCard(activity: activity, isLiked: userData.likedActivityIDs.contains(activity.id), isfavorite: userData.FavoriteActivityIDs.contains(activity.id), commentCount: userData.activityComments[activity.id]?.count ?? 0, activityID: $activityClickedid, onDelete: {
                                        self.fetchMyPublishedActivities()
                                    })
                                        .padding(.horizontal)
                                        .padding(.vertical, 5)
                                        .environmentObject(userData)  // <--- 这里传递环境对象
                                }
                            }
                        }
                    }.scrollIndicators(.hidden) // 隐藏滚动指示器
.refreshable {
                        fetchMyPublishedActivities()
                        userData.fetchLikedId()
                        userData.fetchFavoriteId()
                    }
                    .navigationTitle("我的发布")
                    .navigationBarTitleDisplayMode(.inline)  // 强制标题显示为内联模式
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                                                        Button(action: {
                                                            presentationMode.wrappedValue.dismiss()  // 返回上一视图
                                                            userData.fetchNotifications()
                                                        }) {
                                                            HStack {
                                                                Image(systemName: "chevron.left")  // 返回箭头
                                                                Text("活动管理")  // 可选的文本
                                                            }
                                                        }
                        }
                    }
                }
            }
                        .navigationBarHidden(true)
            //                在改变的时候更新视图
//
            .onAppear(){
                fetchMyPublishedActivities()
                userData.fetchLikedId()
                userData.fetchFavoriteId()
                userData.fetchAllActivityStats()
            }
        }
    }

}
struct MyApply:View{
    struct Comment: Codable, Identifiable {
        // 根据服务器返回的评论结构定义相应的属性
        let id: Int
        let activityId: Int
        let content: String
        let parentId: Int?
        let createdAt: String
    }
    @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式

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
    
    //收藏内容
    @State private var filterCampus: String?
    @State private var filterCollege: String?
    @State private var filterType: String?
    @State var ApplyActivities: [Activity] = []
    @EnvironmentObject var userData: UserData
    @State private var searchText = ""
    @State var activityClickedid:Int=0
    @State var activityComments: [Int: [Comment]] = [:]
    @State var likedActivityIDs: Set<Int> = []
    @State var activityStats: [Int: ActivityStats] = [:]
    @Environment(\.presentationMode) var presentationMode
    private var filteredActivities: [Activity] {
        // 如果搜索文本为空，返回所有活动
        guard !searchText.isEmpty else {
            return ApplyActivities
        }
        // 模糊查询逻辑：不区分大小写，匹配标题、描述或地点
        return ApplyActivities.filter { activity in
            let searchLowercased = searchText.lowercased()
            return activity.title.lowercased().contains(searchLowercased) ||
                   activity.description.lowercased().contains(searchLowercased) ||
                   activity.location.lowercased().contains(searchLowercased) ||
                   activity.type.lowercased().contains(searchLowercased)
        }
    }
    func fetchLikedId() {
        guard !userData.authToken.isEmpty else { return }
        var components = URLComponents(string: "\(BasicValue.baseParseUrl):8083/api/activities/Activity/getAllLikedId")!
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            if let rawString = String(data: data, encoding: .utf8) {
                print("🔥 Raw response: \(rawString)")
            } else {
                print("⚠️ Response is nil or not UTF-8")
            }
            
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[LikeRecord]>.self, from: data)
                if decoded.code == 0, let likeRecords = decoded.data {
                    DispatchQueue.main.async {
                        self.likedActivityIDs = Set(likeRecords.map { $0.activityId })
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }
    //获取状态
    func fetchAllActivityStats() {
        for activity in ApplyActivities {
            fetchActivityStats(activityId: activity.id)
        }
    }
    // 修改数据获取函数
        func fetchAppliedActivities() {
            guard !userData.authToken.isEmpty else { return }
            let components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/user/applications")!
            var request = URLRequest(url: components.url!)
            request.httpMethod = "GET"
            request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
            
            URLSession.shared.dataTask(with: request) { data, response, error in
                guard let data = data else {
                    print("No data received")
                    return
                }
                
                // 调试输出
                if let rawString = String(data: data, encoding: .utf8) {
                    print("🔥 Raw Applied Activities: \(rawString)")
                }
                
                do {
                    let response = try JSONDecoder().decode(ApiResponse<[Activity]>.self, from: data)
                    if response.code == 0, let activities = response.data {
                        DispatchQueue.main.async {
                            print("✅ Fetched \(activities.count) applied activities")
                            self.ApplyActivities = activities // 直接赋值，不要使用append
                            
                            // 并行获取评论和统计数据
                            let group = DispatchGroup()
                            
                            for activity in activities {
                                group.enter()
                                self.fetchComments(for: activity.id) {
                                    group.leave()
                                }
                                
                                group.enter()
                                self.fetchActivityStats(activityId: activity.id) {
                                    group.leave()
                                }
                            }
                            
                            group.notify(queue: .main) {
                                print("All data loaded successfully")
                            }
                        }
                    } else {
                        print("Server error: \(response.msg)")
                    }
                } catch {
                    print("Error decoding activities: \(error)")
                    // 添加更详细的错误信息
                    if let decodingError = error as? DecodingError {
                        switch decodingError {
                        case .keyNotFound(let key, let context):
                            print("Key not found: \(key), context: \(context)")
                        case .typeMismatch(let type, let context):
                            print("Type mismatch: \(type), context: \(context)")
                        case .valueNotFound(let type, let context):
                            print("Value not found: \(type), context: \(context)")
                        case .dataCorrupted(let context):
                            print("Data corrupted: \(context)")
                        @unknown default:
                            print("Unknown decoding error")
                        }
                    }
                }
            }.resume()
        }
        
        // 修改评论获取函数，添加完成回调
        func fetchComments(
            for activityId: Int,
            page: String? = nil,
            size: String? = nil,
            completion: (() -> Void)? = nil
        ) {
            guard !userData.authToken.isEmpty else {
                completion?()
                return
            }
            
            var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/comments/activity/getAllComments/\(activityId)")!
            var queryItems: [URLQueryItem] = []
            if let page = page { queryItems.append(URLQueryItem(name: "page", value: page)) }
            if let size = size { queryItems.append(URLQueryItem(name: "size", value: size)) }
            components.queryItems = queryItems
            
            var request = URLRequest(url: components.url!)
            request.httpMethod = "GET"
            request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")

            URLSession.shared.dataTask(with: request) { data, response, error in
                defer { completion?() }
                
                guard let data = data else {
                    print("No comments data for activity \(activityId): \(error?.localizedDescription ?? "Unknown error")")
                    return
                }
                
                do {
                    let decoded = try JSONDecoder().decode(ApiResponse<[Comment]>.self, from: data)
                    if decoded.code == 0, let comments = decoded.data {
                        DispatchQueue.main.async {
                            self.activityComments[activityId] = comments
                            print("✅ Loaded \(comments.count) comments for activity \(activityId)")
                        }
                    } else {
                        print("Server error for comments: \(decoded.msg)")
                    }
                } catch {
                    print("Decoding comments error for activity \(activityId): \(error)")
                }
            }.resume()
        }
        
        // 修改统计获取函数，添加完成回调
        func fetchActivityStats(activityId: Int, completion: (() -> Void)? = nil) {
            guard !userData.authToken.isEmpty else {
                completion?()
                return
            }
            
            let urlString = "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/stats"
            guard let url = URL(string: urlString) else {
                print("❌ Invalid URL for stats")
                completion?()
                return
            }
            
            var request = URLRequest(url: url)
            request.httpMethod = "GET"
            request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
            
            URLSession.shared.dataTask(with: request) { data, response, error in
                defer { completion?() }
                
                guard let data = data else {
                    print("❌ Network request failed for stats: \(error?.localizedDescription ?? "Unknown error")")
                    return
                }
                
                do {
                    let response = try JSONDecoder().decode(ApiResponse<ActivityStats>.self, from: data)
                    if response.code == 0, let stats = response.data {
                        DispatchQueue.main.async {
                            self.activityStats[activityId] = stats
                            print("✅ Loaded stats for activity \(activityId): \(stats)")
                        }
                    } else {
                        print("⚠️ API returned error for stats: \(response.msg)")
                    }
                } catch {
                    print("❌ JSON decoding failed for stats: \(error)")
                }
            }.resume()
        }
        var body: some View{
        ZStack(){
            // 渐变背景
            LinearGradient(
                gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ).edgesIgnoringSafeArea(.top)
            NavigationView {
                ZStack(){
                    LinearGradient(
                        gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                    .edgesIgnoringSafeArea(.all)
                 
                    ScrollView {
                        LazyVStack {
                            // 搜索栏
                            SearchBar(text: $searchText, placeholder: "搜索活动...")
                                .padding()
                            // 活动列表
                            if(filteredActivities.isEmpty){
                                VStack(){
                                    Text("暂时没有申请的活动哦😮").foregroundColor(Color.gray).padding()
                                    Text("去活动广场看看吧～").foregroundColor(Color.gray)
                                }
                            }
                                ForEach(filteredActivities) { activity in
                                    ActivityCard(activity: activity, commentCount: userData.activityComments[activity.id]?.count ?? 0, activityID: $activityClickedid, onDelete: {
                                        self.fetchAppliedActivities()
                                    })
                                        .padding(.horizontal)
                                        .padding(.vertical, 5)
                                        .environmentObject(userData)  // <--- 这里传递环境对象
                                }
                            
                        }
                    }.scrollIndicators(.hidden) // 隐藏滚动指示器
.refreshable {
                        fetchAppliedActivities()
                    }
                    .navigationTitle("我的申请✋ ")
                    .navigationBarTitleDisplayMode(.inline)  // 强制标题显示为内联模式
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                                                        Button(action: {
                                                            presentationMode.wrappedValue.dismiss()  // 返回上一视图
                                                            userData.fetchNotifications()
                                                        }) {
                                                            HStack {
                                                                Image(systemName: "chevron.left")  // 返回箭头
                                                                Text("活动管理")  // 可选的文本
                                                            }
                                                        }
                        }
                    }
                }
            }
                        .navigationBarHidden(true)
            //                在改变的时候更新视图
            .onAppear(){
                //获取展示信息
                fetchAppliedActivities()
                //更新操作数据
                userData.fetchLikedId()
                userData.fetchFavoriteId()
                userData.fetchApply()
            }
        }
    }
}
struct MyLike:View{
    struct Comment: Codable, Identifiable {
        // 根据服务器返回的评论结构定义相应的属性
        let id: Int
        let activityId: Int
        let content: String
        let parentId: Int?
        let createdAt: String
    }
    @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式

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
    
    //收藏内容
    @State private var filterCampus: String?
    @State private var filterCollege: String?
    @State private var filterType: String?
    @State var LikedActivities: [Activity] = []
    @EnvironmentObject var userData: UserData
    @State private var searchText = ""
    @State var activityClickedid:Int=0
    @State var activityComments: [Int: [Comment]] = [:]
    @State var likedActivityIDs: Set<Int> = []
    @State var activityStats: [Int: ActivityStats] = [:]
    @Environment(\.presentationMode) var presentationMode
    private var filteredActivities: [Activity] {
        // 如果搜索文本为空，返回所有活动
        guard !searchText.isEmpty else {
            return LikedActivities
        }
        // 模糊查询逻辑：不区分大小写，匹配标题、描述或地点
        return LikedActivities.filter { activity in
            let searchLowercased = searchText.lowercased()
            return activity.title.lowercased().contains(searchLowercased) ||
                   activity.description.lowercased().contains(searchLowercased) ||
                   activity.location.lowercased().contains(searchLowercased) ||
                   activity.type.lowercased().contains(searchLowercased)
        }
    }
    // 获取活动评论列表
    func fetchComments(
        for activityId: Int,
        page: String? = nil,
        size: String? = nil
    ) {
        guard !userData.authToken.isEmpty else { return }
        var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/comments/activity/getAllComments/\(activityId)")!
        var queryItems: [URLQueryItem] = []
        if let page = page { queryItems.append(URLQueryItem(name: "page", value: page)) }
        if let size = size { queryItems.append(URLQueryItem(name: "size", value: size)) }
        components.queryItems = queryItems
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[Comment]>.self, from: data)
                if decoded.code == 0, let comments = decoded.data {
                    DispatchQueue.main.async {
                        // 将获取到的评论写入 activityComments 字典
                        self.activityComments[activityId] = comments
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }
    //获取状态
    func fetchActivityStats(activityId: Int) {
        guard !userData.authToken.isEmpty else { return }
        let urlString = "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/stats"
        guard let url = URL(string: urlString) else {
            print("❌ 无效的 URL")
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        print("ok")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("❌ 网络请求失败: \(error?.localizedDescription ?? "未知错误")")
                return
            }
            if let jsonString = String(data: data, encoding: .utf8) {
                print("Response JSON: \(jsonString)")
            }
            do {
                let response = try JSONDecoder().decode(ApiResponse<ActivityStats>.self, from: data)
                if response.code == 0, let stats = response.data {
                    DispatchQueue.main.async {
                        print(stats)
                        self.activityStats[activityId]=stats // 假设你有个变量来存
                    }
                } else {
                    print("⚠️ 接口返回错误: \(response.msg)")
                }
            } catch {
                print("❌ JSON 解码失败: \(error)")
            }
        }.resume()
    }
    func fetchAllActivityStats() {
        for activity in LikedActivities {
            fetchActivityStats(activityId: activity.id)
        }
    }
    func fetchLikedActivities() {
        guard !userData.authToken.isEmpty else { return }
        let components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/Activity/getAllLikedActivity")!
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
               guard let data = data else { return }
               if let rawString = String(data: data, encoding: .utf8) {
                   print("🔥 Raw Activity : \(rawString)")
               } else {
                  print("⚠️ Response  Activity")
                  }
               do {
                   let response = try JSONDecoder().decode(ApiResponse<[Activity]>.self, from: data)
                   if response.code == 0, let activities = response.data {
                       DispatchQueue.main.async {
                           self.LikedActivities = activities
                           // ✅ 加载评论
                           for activity in activities {
                               self.fetchComments(for: activity.id)
                           }
                           //获取点赞id
                           self.fetchAllActivityStats()
                       }
                   }
               } catch {
                   print("Error decoding activities: \(error)")
               }
           }.resume()
    }
    var body: some View{
        ZStack(){
            // 渐变背景
            LinearGradient(
                gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ).edgesIgnoringSafeArea(.top)
            NavigationView {
                ZStack(){
                    LinearGradient(
                        gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                    .edgesIgnoringSafeArea(.all)
                    ScrollView {
                        LazyVStack {
                            // 搜索栏
                            SearchBar(text: $searchText, placeholder: "搜索活动...")
                                .padding()
                            // 活动列表
                            if(filteredActivities.isEmpty){
                                VStack(){
                                    Text("暂时没有喜欢的活动哦😯").foregroundColor(Color.gray).padding()
                                    Text("去活动广场看看吧～").foregroundColor(Color.gray)

                                }
                                }else{
                                ForEach(filteredActivities) { activity in
                                    ActivityCard(activity: activity,commentCount: userData.activityComments[activity.id]?.count ?? 0, activityID: $activityClickedid, onDelete: {
                                        self.fetchLikedActivities()
                                    })
                                        .padding(.horizontal)
                                        .padding(.vertical, 5)
                                        .environmentObject(userData)  // <--- 这里传递环境对象
                                }
                            }
                        }
                    }.scrollIndicators(.hidden) // 隐藏滚动指示器
.refreshable {
                        fetchLikedActivities()
                    }
                    .navigationTitle("我的喜欢❤️")
                    .navigationBarTitleDisplayMode(.inline)  // 强制标题显示为内联模式
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                                                        Button(action: {
                                                            presentationMode.wrappedValue.dismiss()  // 返回上一视图
                                                            userData.fetchNotifications()
                                                        }) {
                                                            HStack {
                                                                Image(systemName: "chevron.left")  // 返回箭头
                                                                Text("活动管理")  // 可选的文本
                                                            }
                                                        }
                        }
                    }
                }
            }
                        .navigationBarHidden(true)
            //                在改变的时候更新视图
            .onAppear(){
                fetchLikedActivities()
                userData.fetchLikedId()
                userData.fetchFavoriteId()
            }
        }
    }
}
struct MyFavorite:View{
    struct Comment: Codable, Identifiable {
        // 根据服务器返回的评论结构定义相应的属性
        let id: Int
        let activityId: Int
        let content: String
        let parentId: Int?
        let createdAt: String
    }
    @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式

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
    
    //收藏内容
    @State private var filterCampus: String?
    @State private var filterCollege: String?
    @State private var filterType: String?
    @State var Favoriteactivities: [Activity] = []
    @EnvironmentObject var userData: UserData
    @State private var searchText = ""
    @State var activityClickedid:Int=0
    @State var activityComments: [Int: [Comment]] = [:]
    @State var likedActivityIDs: Set<Int> = []
    @State var activityStats: [Int: ActivityStats] = [:]
    @Environment(\.presentationMode) var presentationMode

    private var filteredActivities: [Activity] {
        // 如果搜索文本为空，返回所有活动
        guard !searchText.isEmpty else {
            return Favoriteactivities
        }
        // 模糊查询逻辑：不区分大小写，匹配标题、描述或地点
        return Favoriteactivities.filter { activity in
            let searchLowercased = searchText.lowercased()
            return activity.title.lowercased().contains(searchLowercased) ||
                   activity.description.lowercased().contains(searchLowercased) ||
                   activity.location.lowercased().contains(searchLowercased) ||
                   activity.type.lowercased().contains(searchLowercased)
        }
    }
    // 获取活动评论列表
    func fetchComments(
        for activityId: Int,
        page: String? = nil,
        size: String? = nil
    ) {
        guard !userData.authToken.isEmpty else { return }
        var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/comments/activity/getAllComments/\(activityId)")!
        var queryItems: [URLQueryItem] = []
        if let page = page { queryItems.append(URLQueryItem(name: "page", value: page)) }
        if let size = size { queryItems.append(URLQueryItem(name: "size", value: size)) }
        components.queryItems = queryItems
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[Comment]>.self, from: data)
                if decoded.code == 0, let comments = decoded.data {
                    DispatchQueue.main.async {
                        // 将获取到的评论写入 activityComments 字典
                        self.activityComments[activityId] = comments
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }

    //获取状态
    func fetchActivityStats(activityId: Int) {
        guard !userData.authToken.isEmpty else { return }
        let urlString = "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/stats"
        guard let url = URL(string: urlString) else {
            print("❌ 无效的 URL")
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        print("ok")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("❌ 网络请求失败: \(error?.localizedDescription ?? "未知错误")")
                return
            }
            if let jsonString = String(data: data, encoding: .utf8) {
                print("Response JSON: \(jsonString)")
            }
            do {
                let response = try JSONDecoder().decode(ApiResponse<ActivityStats>.self, from: data)
                if response.code == 0, let stats = response.data {
                    DispatchQueue.main.async {
                        print(stats)
                        self.activityStats[activityId]=stats // 假设你有个变量来存
                    }
                } else {
                    print("⚠️ 接口返回错误: \(response.msg)")
                }
            } catch {
                print("❌ JSON 解码失败: \(error)")
            }
        }.resume()
    }
    func fetchAllActivityStats() {
        for activity in Favoriteactivities {
            fetchActivityStats(activityId: activity.id)
        }
    }
    func fetchFavoriteActivities(campus: String? = nil, college: String? = nil, type: String? = nil) {
        guard !userData.authToken.isEmpty else { return }
        var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/user/favorites")!
        var queryItems: [URLQueryItem] = []
        if let campus = campus { queryItems.append(URLQueryItem(name: "campus", value: campus)) }
        if let college = college { queryItems.append(URLQueryItem(name: "college", value: college)) }
        if let type = type { queryItems.append(URLQueryItem(name: "type", value: type)) }
        components.queryItems = queryItems
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
               guard let data = data else { return }
               if let rawString = String(data: data, encoding: .utf8) {
                   print("🔥 Raw Activity : \(rawString)")
               } else {
                  print("⚠️ Response  Activity")
                  }
               do {
                   let response = try JSONDecoder().decode(ApiResponse<[Activity]>.self, from: data)
                   if response.code == 0, let activities = response.data {
                       DispatchQueue.main.async {
                           self.Favoriteactivities = activities
                           // ✅ 加载评论
                           for activity in activities {
                               self.fetchComments(for: activity.id)
                           }
                           //获取点赞id
                           self.fetchAllActivityStats()
                       }
                   }
               } catch {
                   print("Error decoding activities: \(error)")
               }
           }.resume()
    }
    var body: some View{
        ZStack(){
            // 渐变背景
            LinearGradient(
                gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ).edgesIgnoringSafeArea(.top)
            NavigationView {
                ZStack(){
                    LinearGradient(
                        gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                    .edgesIgnoringSafeArea(.all)
                    ScrollView {
                        LazyVStack {
                            // 搜索栏
                            SearchBar(text: $searchText, placeholder: "搜索活动...")
                                .padding()
                            // 活动列表
                            if filteredActivities.count==0{
                                VStack(){
                                    Text("暂时没有收藏的活动😯～").foregroundColor(Color.gray).padding()
                                    Text("去活动广场看看吧～").foregroundColor(Color.gray)
                                }
                            }else{
                                ForEach(filteredActivities) { activity in
                                    ActivityCard(activity: activity, commentCount: userData.activityComments[activity.id]?.count ?? 0, activityID: $activityClickedid, onDelete: {
                                        self.fetchFavoriteActivities()
                                    })
                                        .padding(.horizontal)
                                        .padding(.vertical, 5)
                                        .environmentObject(userData)  // <--- 这里传递环境对象
                                }
                            }
                        }
                    }.scrollIndicators(.hidden) // 隐藏滚动指示器
.refreshable {
                        fetchFavoriteActivities()
                    }
                    .navigationTitle("我的收藏🌟")
                    .navigationBarTitleDisplayMode(.inline)  // 强制标题显示为内联模式
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                                                        Button(action: {
                                                            presentationMode.wrappedValue.dismiss()  // 返回上一视图
                                                            userData.fetchNotifications()
                                                        }) {
                                                            HStack {
                                                                Image(systemName: "chevron.left")  // 返回箭头
                                                                Text("活动管理")  // 可选的文本
                                                            }
                                                        }
                        }
//                        ToolbarItem(placement: .navigationBarTrailing) {
//                            Menu {
//                                // 校区筛选
//                                Menu("校区") {
//                                    Button("全部校区") { filterCampus = nil }
//                                    Button("黄家湖校区") { filterCampus = "黄家湖" }
//                                    Button("青山校区") { filterCampus = "青山" }
//                                }
//                                
//                                // 学院筛选
//                                Menu("学院") {
//                                    Button("全部学院") { filterCollege = nil }
//                                    Button("材料学部") { filterCollege = "材料学部" }
//                                    Button("城市建设学院") { filterCollege = "城市建设学院" }
//                                    Button("管理学院") { filterCollege = "管理学院" }
//                                    Button("国际学院") { filterCollege = "国际学院" }
//                                    Button("化学与化工学院") { filterCollege = "化学与化工学院" }
//                                    Button("机械自动化学院") { filterCollege = "机械自动化学院" }
//                                    Button("计算机科学与技术学院") { filterCollege = "计算机科学与技术学院" }
//                                    Button("理学院") { filterCollege = "理学院" }
//                                    Button("临床学院") { filterCollege = "临床学院" }
//                                    Button("马克思主义学院") { filterCollege = "马克思主义学院" }
//                                    Button("汽车与交通工程学院") { filterCollege = "汽车与交通工程学院" }
//                                    Button("生命科学与健康学院") { filterCollege = "生命科学与健康学院" }
//                                    Button("体育学院") { filterCollege = "体育学院" }
//                                    Button("外国语学院") { filterCollege = "外国语学院" }
//                                    Button("法学与经济学院") { filterCollege = "法学与经济学院" }
//                                    Button("信息科学与工程学院(人工智能学院)") { filterCollege = "信息科学与工程学院(人工智能学院)" }
//                                    Button("艺术与设计学院") { filterCollege = "艺术与设计学院" }
//                                    Button("资源与环境工程学院") { filterCollege = "资源与环境工程学院" }
//                                    Button("冶金与能源学院") { filterCollege = "冶金与能源学院" }
//                                }
//                                // 活动类型筛选
//                                Menu("活动类型") {
//                                    Button("全部类型") { filterType = nil }
//                                    Button("休闲娱乐") { filterType = "休闲娱乐" }
//                                    Button("运动健身") { filterType = "运动健身" }
//                                    Button("学习互助") { filterType = "学习互助" }
//                                }
//                            } label: {
//                                Image(systemName: "line.3.horizontal.decrease.circle")
//                            }
//                        }
                    }
                }
            }
            .navigationBarHidden(true)
            //                在改变的时候更新视图
//            .onChange(of: filterCampus) { _ in applyFilters() }
//            .onChange(of: filterCollege) { _ in applyFilters() }
//            .onChange(of: filterType) { _ in applyFilters() }
            .onAppear(){
                userData.fetchFavoriteId()
                userData.fetchLikedId()
                fetchFavoriteActivities()
            }
        }
    }
    func applyFilters() {
        fetchFavoriteActivities(
            campus: filterCampus,
            college: filterCollege,
            type: filterType
        )
    }
}
struct NotificationRow: View {
    @Binding var selectedDestination: NavigationDestination?
    @Binding var showOtherInformation: Bool
    @State var ifAccept: Bool = false
    @State var noti: Noti
    @Binding var otherPeopleUid: Int
    
    private let primaryColor = Color(red: 1.0, green: 0.4, blue: 0.2) // 活力珊瑚橙
    private let secondaryColor = Color(red: 1.0, green: 0.8, blue: 0.4) // 阳光黄
    @EnvironmentObject var userData: UserData
    private let warmPrimary = Color(red: 0.95, green: 0.5, blue: 0.2)
    
    @State private var profilePictureId: Int?
    @State private var activity: Activity?
    @State var activityClickedid: Int = 0
    
    // 获取图片详细的工具类
    @StateObject private var viewModel = ImageUploadViewModel()
    @StateObject private var profilePictureGetTool = ImageUploadViewModel()
    
    // 头像图片url
    var profile_url: String? {
        if(profilePictureGetTool.pictureDetail?.status==1){
            profilePictureGetTool.pictureDetail?.url
        }else{
            ""
        }
    }
    
    // 活动图片url
    var image_url: String? {
        if( viewModel.pictureDetail?.status==1){
            viewModel.pictureDetail?.url
        }else{
                ""
        }
    }
    
    // 使用动态字体大小和最小缩放因子以提高可访问性
    private let typeFont: Font = .system(size: 16, weight: .medium)
    private let contentFont: Font = .system(size: 14)
    private let dateFont: Font = .system(size: 12)
    
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        ZStack(alignment: .topTrailing) {
            HStack(alignment: .top, spacing: 12) {
                // 头像按钮
                Button {
                    otherPeopleUid = noti.createdId
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
                            .frame(width: 50, height: 50)
                            .shadow(color: primaryColor.opacity(0.3), radius: 8, x: 0, y: 4)
                        
                        if let url = profile_url {
                            AsyncImage(url: URL(string: url)) { phase in
                                switch phase {
                                case .success(let image):
                                    image
                                        .resizable()
                                        .scaledToFill()
                                case .failure, .empty:
                                    Image(systemName: "person.crop.circle")
                                        .resizable()
                                        .scaledToFit()
                                        .padding(8)
                                        .background(Color.white)
                                @unknown default:
                                    Image(systemName: "person.crop.circle")
                                        .resizable()
                                        .scaledToFit()
                                        .padding(8)
                                        .background(Color.white)
                                }
                            }
                            .frame(width: 44, height: 44)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(Color.white, lineWidth: 2))
                        } else {
                            Image(systemName: "person.crop.circle")
                                .resizable()
                                .scaledToFit()
                                .padding(8)
                                .background(Color.white)
                                .clipShape(Circle())
                                .overlay(Circle().stroke(Color.white, lineWidth: 2))
                                .foregroundColor(primaryColor)
                                .frame(width: 44, height: 44)
                        }
                    }
                    .frame(width: 50, height: 50)
                }
                .buttonStyle(PlainButtonStyle())
                
                // 文本信息 - 使用灵活的空间分配
                VStack(alignment: .leading, spacing: 6) {
                    // 通知类型
                    Text(noti.type)
                        .font(typeFont)
                        .foregroundColor(.black)
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                    
                    // 通知内容
                    Text(noti.content)
                        .font(contentFont)
                        .foregroundColor(.gray)
                        .lineLimit(3) // 增加行数限制
                        .minimumScaleFactor(0.8)
                        .fixedSize(horizontal: false, vertical: true) // 允许垂直扩展
                        .contextMenu {
                            Button(action: {
                                UIPasteboard.general.string = noti.content
                            }) {
                                Text("复制内容")
                                Image(systemName: "doc.on.doc")
                            }
                        }
                    
                    // 通知日期
                    Text(noti.createdAt.toRelativeDateString())
                        .font(dateFont)
                        .foregroundColor(primaryColor)
                        .minimumScaleFactor(0.8)
                }
                .frame(maxWidth: .infinity, alignment: .leading) // 占据剩余空间
                .padding(.trailing, 8)
                
                // 活动信息按钮
                Button {
                    // 如果活动创建者是本人
                    if activity?.creatorId == userData.userInfo?.userId {
                        selectedDestination = .myPublish
                    } else {
                        selectedDestination = .myApply
                    }
                    userData.signRead(notifyId: noti.id)
                } label: {
                    VStack(spacing: 6) {
                        // 活动图片
                        if let imageUrlString = image_url, !imageUrlString.isEmpty, let url = URL(string: imageUrlString) {
                            AsyncImage(url: url) { phase in
                                switch phase {
                                case .success(let image):
                                    image
                                        .resizable()
                                        .scaledToFill()
                                case .failure, .empty:
                                    Image(systemName: "globe.asia.australia.fill")
                                        .resizable()
                                        .scaledToFit()
                                        .foregroundColor(.gray)
                                @unknown default:
                                    Image(systemName: "globe.asia.australia.fill")
                                        .resizable()
                                        .scaledToFit()
                                        .foregroundColor(.gray)
                                }
                            }
                            .frame(width: 50, height: 50)
                            .clipShape(RoundedRectangle(cornerRadius: 8)).padding(.top,2)
                        } else {
                            Image(systemName: "globe.asia.australia.fill")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 50, height: 50)
                                .foregroundColor(.gray)
                                .background(Color(.systemGray6))
                                .clipShape(RoundedRectangle(cornerRadius: 8)).padding(.top,2)
                        }
                        
                        // 活动标题
                        Text(activity?.title ?? "活动标题")
                            .font(.system(size: 14, weight: .semibold))
                            .lineLimit(2) // 允许两行显示
                            .multilineTextAlignment(.center)
                            .minimumScaleFactor(0.8)
                            .frame(width: 70) // 固定宽度但允许换行
                            .foregroundColor(Color("courseTitleColor"))
                        Spacer()
                    }.frame(height:100)
                    .padding(8)
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                }
            }
            .padding(12)
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
            
            // 未读标记
            if !noti.isRead {
                Circle()
                    .fill(primaryColor)
                    .frame(width: 10, height: 10)
                    .padding(.trailing, 8)
                    .padding(.top, 8)
            }
        }
        .onAppear {
            // 获取头像uid
            userData.getProfilePictureId(userId: noti.createdId) { id in
                profilePictureGetTool.fetchPictureDetail(with: id)
            }
            
            if noti.type == "ACCEPT" {
                noti.type = "申请通过🎉🎉🎉"
            }
            
            // 获取活动详情
            userData.fetchActivity(RelatedId: noti.relatedId) { getActivity in
                activity = getActivity
                // 获取活动图片
                viewModel.fetchPictureDetail(with: activity?.imageId ?? 0)
            }
        }
    }
}

struct NotificationListView: View {
    @State private var sheetHeight: CGFloat = 300 // 用于存储动态计算的高度

    // 1. 定义枚举：包含所有可能的跳转目标页面
    @EnvironmentObject var userData: UserData
    @State var showInformation:Bool=false
    @State var otherPeopleUid:Int = 0
    @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式
    @Binding var selectedDestination:NavigationDestination?
    let primaryColor = Color(red: 1.0, green: 0.4, blue: 0.2)
    private var secondaryColor: Color {
        colorScheme == .dark ? Color(red: 0.6, green: 0.8, blue: 1.0): Color(red: 1.0, green: 0.8, blue: 0.4)
    }
    
    private var backgroundColor: Color {
        colorScheme == .dark ? Color(red: 0.08, green: 0.08, blue: 0.1) : Color(red: 1.0, green: 0.96, blue: 0.9)
    }
    var body: some View {
        VStack(){
            if(userData.Noti.isEmpty){
                Text("暂时没有新通知哦🌟").foregroundColor(Color.gray).padding()
            }
            List {
                ForEach(userData.Noti) { notification in
                    NotificationRow(selectedDestination: $selectedDestination, showOtherInformation: $showInformation, noti: notification, otherPeopleUid: $otherPeopleUid).environmentObject(userData)
                        .listRowBackground(Color.clear)
                        .listRowInsets(EdgeInsets(top: 8, leading: 0, bottom: 8, trailing: 0))
                }
            }.refreshable {
                userData.fetchNotifications()
                print(userData.Noti)
            }
            .listStyle(PlainListStyle())
            }.background(
            LinearGradient(
                gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ).edgesIgnoringSafeArea(.top)
        )// 在使用 Sheet 的地方
            .sheet(isPresented: $showInformation) {
                OtherPeopleProfile(otherPeopleUid: $otherPeopleUid)
                    .environmentObject(UserData())
                    // 关键：使用动态高度
                    .presentationDetents([.height(sheetHeight)])
                    .presentationDragIndicator(.visible)
            }
            // 监听高度变化
            .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("ProfileHeightUpdated"))) { notification in
                if let height = notification.object as? CGFloat {
                    // 使用动画过渡高度变化
                    withAnimation(.easeInOut(duration: 0.3)) {
                        sheetHeight = height
                    }
                }
            }
        .navigationTitle("所有通知")
    }
}
// 首先在OtherPeopleProfile视图中添加一个计算属性来确定内容高度
struct OtherPeopleProfile: View {
    @Binding var otherPeopleUid: Int
    @State var otherPeopleInfo: UserInfo?
    @EnvironmentObject var userData: UserData
    @State private var showInformation = false
    
    // 计算内容高度的属性
    private var contentHeight: CGFloat {
        guard let userInfo = otherPeopleInfo else { return 300 } // 加载状态默认高度
        
        var height: CGFloat = 200 // 基础高度（头像和标题）
        
        // 个人信息行高度（每行约40pt）
        if userInfo.campus?.isEmpty == false { height += 50 }
        if userInfo.college?.isEmpty == false { height += 50 }
        if userInfo.major?.isEmpty == false { height += 50 }
        if userInfo.grade?.isEmpty == false { height += 50 }
        if userInfo.signature?.isEmpty == false { height += 50 }
        if userInfo.interests?.isEmpty == false { height += 50 }
        if userInfo.skills?.isEmpty == false { height += 50 }
        
        // 联系方式区域（标题+每行40pt）
        let hasContactInfo = [userInfo.qq, userInfo.wechat, userInfo.phone].compactMap { $0 }.isEmpty == false
        if hasContactInfo {
            height += 50 // 标题高度
            if userInfo.qq?.isEmpty == false { height += 50 }
            if userInfo.wechat?.isEmpty == false { height += 50 }
            if userInfo.phone?.isEmpty == false { height += 50 }
        }
        
        // 最小高度限制
        return max(height, 300)
    }
    
    // 其他颜色和属性设置保持不变...
    @Environment(\.colorScheme) private var colorScheme
    
    private var bgColor: Color {
        colorScheme == .dark ? Color(.systemGray6) : Color(red: 1.0, green: 0.96, blue: 0.88)
    }
    
    private var cardColor: Color {
        colorScheme == .dark ? Color(.systemGray5) : Color(red: 1.0, green: 0.98, blue: 0.92)
    }
    
    private var textPrimary: Color {
        colorScheme == .dark ? Color.orange.opacity(0.8) : Color(red: 0.65, green: 0.32, blue: 0.1)
    }
    
    private var iconColor: Color {
        colorScheme == .dark ? Color.orange : Color(red: 0.92, green: 0.45, blue: 0.1)
    }
    
    @StateObject private var profilePictureGetTool = ImageUploadViewModel()
    
    var profile_url: String? {
        profilePictureGetTool.pictureDetail?.status == 1 ? profilePictureGetTool.pictureDetail?.url : nil
    }
    
    // fetchOtherPeopleUserInfo方法保持不变...
    func fetchOtherPeopleUserInfo() {
        guard !userData.authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/user/\(otherPeopleUid)")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(userData.authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else { return }
            do {
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("返回的原始JSON字符串：\n\(jsonString)")
                }
                let response = try JSONDecoder().decode(ApiResponse<UserInfo>.self, from: data)
                if response.code == 0, let userInfo = response.data {
                    DispatchQueue.main.async {
                        otherPeopleInfo = userInfo
                        DispatchQueue.main.async {
                            otherPeopleInfo = userInfo
                            // 数据更新后发送高度通知
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                                NotificationCenter.default.post(name: NSNotification.Name("ProfileHeightUpdated"), object: contentHeight)
                            }
                        }
                    }
                }
            } catch {
                print("Error decoding user info: \(error)")
            }
        }.resume()
    }
    
    var body: some View {
        ZStack {
            bgColor.ignoresSafeArea(.all)
            
            ScrollView {
                // 内容结构保持不变，但移除固定高度约束
                VStack(spacing: 20) {
                    if let userInfo = otherPeopleInfo {
                        VStack(spacing: 12) {
                            AsyncImage(url: URL(string: profile_url ?? "")) { phase in
                                switch phase {
                                case .success(let image):
                                    image.resizable().scaledToFill()
                                case .empty, .failure:
                                    Image(systemName: "person.circle.fill").resizable().foregroundColor(iconColor)
                                @unknown default:
                                    EmptyView()
                                }
                            }
                            .frame(width: 100, height: 100)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(iconColor.opacity(0.3), lineWidth: 2))
                            .shadow(color: iconColor.opacity(colorScheme == .dark ? 0.1 : 0.2), radius: 4, x: 0, y: 2)
                            
                            Text(userInfo.studentId ?? "未知用户")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                        }
                        .padding(.top, 20)
                    } else {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: iconColor))
                            .padding(.top, 40)
                    }
                    
                    VStack(spacing: 15) {
                        if let userInfo = otherPeopleInfo {
                            VStack(spacing: 8) {
                                if let campus = userInfo.campus, !campus.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                    OtherInfoRow(title: "校区", value: campus, icon: "building", textColor: textPrimary, iconColor: iconColor)
                                }
                                // 其他信息行保持不变...
                                if let college = userInfo.college, !college.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                    OtherInfoRow(title: "学院", value: college, icon: "graduationcap", textColor: textPrimary, iconColor: iconColor)
                                }
                                if let major = userInfo.major, !major.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                    OtherInfoRow(title: "专业", value: major, icon: "book", textColor: textPrimary, iconColor: iconColor)
                                }
                                if let grade = userInfo.grade, !grade.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                    OtherInfoRow(title: "年级", value: grade, icon: "calendar", textColor: textPrimary, iconColor: iconColor)
                                }
                                if let signature = userInfo.signature, !signature.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                    OtherInfoRow(title: "签名", value: signature, icon: "quote.bubble", textColor: textPrimary, iconColor: iconColor)
                                }
                                if let interests = userInfo.interests, !interests.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                    OtherInfoRow(title: "兴趣爱好", value: interests, icon: "heart", textColor: textPrimary, iconColor: iconColor)
                                }
                                if let skills = userInfo.skills, !skills.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                    OtherInfoRow(title: "技能特长", value: skills, icon: "star", textColor: textPrimary, iconColor: iconColor)
                                }
                            }
                            .padding()
                            .background(cardColor)
                            .cornerRadius(16)
                            .shadow(color: iconColor.opacity(colorScheme == .dark ? 0.05 : 0.1), radius: 6, x: 0, y: 3)
                        }
                        
                        if let userInfo = otherPeopleInfo,
                           [userInfo.qq, userInfo.wechat, userInfo.phone].compactMap({ $0 }).isEmpty == false {
                            
                            VStack(spacing: 10) {
                                SSectionTitleView(title: "联系方式", icon: "bubble.left.and.bubble.right", textColor: textPrimary, iconColor: iconColor)
                                
                                VStack(spacing: 8) {
                                    if let qq = userInfo.qq, !qq.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                        OtherInfoRow(title: "QQ", value: qq, icon: "q.circle", textColor: textPrimary, iconColor: iconColor)
                                    }
                                    if let wechat = userInfo.wechat, !wechat.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                        OtherInfoRow(title: "微信", value: wechat, icon: "message", textColor: textPrimary, iconColor: iconColor)
                                    }
                                    if let phone = userInfo.phone, !phone.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                        OtherInfoRow(title: "电话", value: phone, icon: "phone", textColor: textPrimary, iconColor: iconColor)
                                    }
                                }
                            }
                            .padding()
                            .background(cardColor)
                            .cornerRadius(16)
                            .shadow(color: iconColor.opacity(colorScheme == .dark ? 0.05 : 0.1), radius: 6, x: 0, y: 3)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 30)
                }
            }.scrollIndicators(.hidden) // 隐藏滚动指示器

        }
        .onAppear {
             fetchOtherPeopleUserInfo()
             print("OtherPeopleUid:\(otherPeopleUid)")
             userData.getProfilePictureId(userId: otherPeopleUid) { id in
                 profilePictureGetTool.fetchPictureDetail(with: id)
             }
         }
        // 添加高度变化监听器
               .onChange(of: contentHeight) { newHeight in
                   // 当内容高度变化时通知外部更新
                   NotificationCenter.default.post(
                       name: NSNotification.Name("ProfileHeightUpdated"),
                       object: newHeight
                   )
               }
    }
}
// 假设 MyInfoRow 和 SectionTitleView 需要接受颜色参数
// 以下是可能需要修改的辅助视图示例

struct OtherInfoRow: View {
    let title: String
    let value: String
    let icon: String
    var textColor: Color
    var iconColor: Color
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(iconColor)
                .frame(width: 20)
            
            Text(title)
                .foregroundColor(.secondary)
                .frame(width: 80, alignment: .leading)
            
            Text(value)
                .foregroundColor(textColor)
                .fontWeight(.medium)
            
            Spacer()
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 12)
    }
}

struct SSectionTitleView: View {
    let title: String
    let icon: String
    var textColor: Color
    var iconColor: Color
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(iconColor)
            
            Text(title)
                .font(.headline)
                .foregroundColor(textColor)
            
            Spacer()
        }
    }
}
