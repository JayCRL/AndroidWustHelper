import SwiftUI
import Foundation

//数据结构

// 定义颜色常量
let primaryColor = Color(red: 78/255, green: 84/255, blue: 200/255)
let secondaryColor = Color(red: 143/255, green: 148/255, blue: 251/255)
let successColor = Color(red: 46/255, green: 204/255, blue: 113/255)
let warningColor = Color(red: 243/255, green: 156/255, blue: 18/255)
let dangerColor = Color(red: 231/255, green: 76/255, blue: 60/255)
let lightColor = Color(red: 248/255, green: 249/255, blue: 250/255)
let darkColor = Color(red: 52/255, green: 58/255, blue: 64/255)
let grayColor = Color(red: 108/255, green: 117/255, blue: 125/255)
let borderColor = Color(red: 224/255, green: 224/255, blue: 224/255)
// 网络响应模型
struct ApiResponse<T: Codable>: Codable {
    let code: Int
    let msg: String
    let data: T?
}
struct CatApiResponse<T: Codable>: Codable {
    let code: Int
    let message: String
    let data: T?
}
// 示例统计数据结构（根据接口返回的数据结构修改）
struct ActivityStats: Codable {
    var likeCount: Int?
    var favoriteCount: Int?
}


// 用户数据结构
struct User: Identifiable {
    let id = UUID()
    let name: String
    let avatar: String
}
// 用户信息模型
struct UserInfo: Codable,Identifiable {
    //表主键
    let id: Int
    //学号
    let userId: Int
    //学号
    let studentId: String?
    //学院
    var college: String?
    //教室
    var campus: String?
    //性别
    let gender: String?
    //年级
    var grade: String?
    //专业
    var major: String?
    //头像url
    var avatarUrlId: Int?
    //个性签名
    var signature: String?
    //兴趣
    var interests: String?
    //技能特长
    var skills: String?
    //qq号
    var qq: String?
    //微信号
    var wechat: String?
    //手机号
    var phone: String?
    //是否可见
    var contactVisibility: String?
    //是否可见
    var infoVisibility: String?
    //创建时间
    let createdAt: String?
    //更新时间
    let updatedAt: String?
}

// 活动模型
struct Activity: Codable, Identifiable {
    let id: Int
    let creatorId: Int
    let title: String
    let description: String
    let type: String
    let activityTime: String
    let location: String
    let minPeople: Int
    let maxPeople: Int
    let expireTime: String
    let campus: String?
    let college: String?
    let tags: String?
    let status: String
    let createdAt: String
    let updatedAt: String?
    let imageId: Int?
}

// 活动申请模型
struct ActivityApplication: Codable {
    let id: Int
    let activityId: Int
    let userId: Int
    let reason: String
    let status: String
    let createdAt: String
    let updatedAt: String
}

// 评论模型
struct Comment:Identifiable {
    let activityId: Int
    let userId: String
    let content: String
    let parentId: Int?
    let createdAt: String
    let id = UUID()
    let user: User
    var timestamp: Date
    var likes: Int
    var isLiked: Bool = false
}
// 通知模型
struct Noti: Codable, Identifiable {
    let id: Int
    let recipientId: Int
    let createdId: Int
    var type: String
    let content: String
    let relatedId: Int
    let profilePictureId:String?
    var isRead: Bool
    let createdAt: String
}
// 申请模型 - 根据API响应修正
struct Application: Codable, Identifiable {
    let id: Int
    let activityId: Int
    let applicantId: Int
    let userId: Int
    let reason: String?
    var status: String
    let createdAt: String
    let updatedAt: String
    let contactInfo: String?
}
// 草稿模型
struct Draft: Codable, Identifiable {
    let id: Int
    let userId: Int
    let activityId: Int?
    let reason: String?
    let createdAt: String
    let title: String
    let description: String
    let type: String
    let activityTime: String
    let location: String
    let minPeople: Int
    let maxPeople: Int
    let expireTime: String
    let campus: String?
    let college: String?
    let tags: String?
    let status: String?
    let updatedAt: String?
    let imageUrl: String?
}
struct LikeRecord: Codable, Identifiable {
    let id: Int
    let activityId: Int
    let userId: Int
    let createdAt: String
}
struct ApplyRecord: Codable, Identifiable {
    let id: Int
    let activityId: Int
    let applicantId:Int
    let userId: Int
    let reason:String
    let status:String
    let createdAt: String
    let updatedAt: String
    let contactInfo: String?
}
struct Favorite: Codable, Identifiable {
    let id: Int
    let activityId: Int
    let userId: Int
    let createdAt: String
}


// 视图
// 主界面
struct Group1MainView: View {
    @State private var selectedTab = 0
    @State private var isDataLoaded = false
    @StateObject var userData = UserData()  // 使用 StateObject 管理加载数据的状态
    @AppStorage("ID") private var studentNumber: String = "" // 学号缓存
    var notificationCount:Int{
        userData.Noti.filter { !$0.isRead }.count
    }
    var body: some View {
        ZStack {
            // 主内容
            ZStack(alignment: .bottomTrailing) {
                switch selectedTab {
                case 0:
                    HomeView()
                        .environmentObject(userData)
                case 1:
                    PublishView(selectedTab: $selectedTab)
                        .environmentObject(userData)
                case 2:
                    ProfileView()
                        .environmentObject(userData)
                case 3:
                    NotificationTable()
                        .environmentObject(userData)
                default:
                    HomeView()
                        .environmentObject(userData)
                }
                DraggableFAB(selectedTab: $selectedTab, notificationCount: notificationCount)
            }
            
         
        } .enableSwipeBack()
            .onAppear {
            let group = DispatchGroup()
            group.enter()
            userData.fetchLikedId()
            // 获取收藏ID
            group.enter()
            userData.fetchFavoriteId()
            // 获取用户信息
            group.enter()
            userData.fetchUserInfo()
            // 获取通知
            group.enter()
            userData.fetchNotifications()
            // 获取活动
            group.enter()
            userData.fetchActivities()
            group.enter()
            userData.fetchAllActivityStats()
            // 获取点赞ID
            // 获取申请信息
            group.enter()
            userData.fetchApply()
            
            // 使用延迟来模拟完成（注意：移除了重复的fetchNotifications）
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                for _ in 0..<6 { // 现在只有6个任务
                    group.leave()
                }
            }
            // 所有任务完成后更新界面
            group.notify(queue: .main) {
                self.isDataLoaded = true
                print("所有数据加载完成，界面可以显示了")
            }
        }
    }
        struct DraggableFAB: View{
            @Binding var selectedTab: Int
            @State private var offset: CGSize = .zero
            @State private var dragAmount: CGSize = .zero
            @State private var isDragging = false
            @State private var isExpanded = false   // 是否展开菜单
            var notificationCount:Int
            let buttonSize: CGFloat = 65
            let padding: CGFloat = 20
            let menuOffset1 = CGSize(width: -80, height: 0) // 右上小球
            let menuOffset2 = CGSize(width: -40, height: -50) // 左上小球
            let menuOffset3 = CGSize(width: -40, height: +50) // 左下小球
            var body: some View {
                GeometryReader { geo in
                    ZStack {
                        // 小球菜单
                        if isExpanded {
                            Button {
                                withAnimation(.easeInOut) {
                                    selectedTab = 1
                                }
                            } label: {
                                Circle().fill(Color.blue.opacity(0.7))
                                    .frame(width: 50, height: 50)
                                    .overlay(Image(systemName: "plus").foregroundColor(.white).font(.system(size: 20, weight: .bold)))
                            }
                            .contentShape(Circle())
                            .offset(menuOffset(menuOffset1))
                            Button {
                                withAnimation(.easeInOut) {
                                    if (selectedTab == 0){
                                        selectedTab = 2
                                    }else{
                                        selectedTab = 0
                                    }
                                }
                            } label: {
                                Circle().fill(Color.yellow.opacity(0.7))
                                    .frame(width: 50, height: 50)
                                    .overlay(Image( systemName: selectedTab==0 ? "person":"sparkles" ).foregroundColor(.white).font(.system(size: 20, weight: .bold)))
                            }
                            .contentShape(Circle())
                            .offset(menuOffset(menuOffset3))
                            Button {
                                withAnimation(.easeInOut) {
                                    selectedTab = 3
                                }
                            } label: {
                                ZStack(alignment: .topTrailing) {
                                    // 主按钮
                                    Circle()
                                        .fill(Color.green.opacity(0.7))
                                        .frame(width: 50, height: 50)
                                        .overlay(
                                            Image(systemName: "bell")
                                                .foregroundColor(.white)
                                                .font(.system(size: 20, weight: .bold))
                                        )
                                    
                                    // 红点 + 数字
                                    if notificationCount > 0 {
                                        Text("\(notificationCount)")
                                            .font(.system(size: 12, weight: .bold))
                                            .foregroundColor(.white)
                                            .padding(5)
                                            .background(
                                                Circle()
                                                    .fill(Color.red)
                                            )
                                            .offset(x: 9, y: -9)
                                    }
                                }
                            }
                            .contentShape(Circle())
                            .offset(menuOffset(menuOffset2))
                            
                        }
                        
                        // --- 主悬浮球 ---
                        Circle()
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [Color.orange, Color.yellow]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: buttonSize, height: buttonSize)
                            .shadow(color: Color.orange.opacity(0.7), radius: 10, x: 0, y: 5)
                            .overlay(
                                Image(systemName: isExpanded ? "xmark" : "square.grid.2x2.fill")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(.white)
                            )
                            .overlay(Circle().stroke(Color.white, lineWidth: 2))
                            .opacity(isAtBottom(geo) ? 0.5 : 1)
                            .scaleEffect(isAtBottom(geo) ? 0.8 : 1)
                            .offset(x: offset.width + (isDragging ? dragAmount.width : 0),
                                    y: offset.height + (isDragging ? dragAmount.height : 0))
                            .gesture(
                                DragGesture(minimumDistance: 0)
                                    .onChanged { value in
                                        if abs(value.translation.width) > 5 || abs(value.translation.height) > 5 {
                                            isDragging = true
                                        }
                                        dragAmount = value.translation
                                    }
                                    .onEnded { value in
                                        let halfW = geo.size.width / 2 - buttonSize/2 - padding
                                        let halfH = geo.size.height / 2 - buttonSize/2 - padding
                                        var newX = offset.width + value.translation.width
                                        var newY = offset.height + value.translation.height
                                        newX = min(max(newX, -halfW), halfW)
                                        newY = min(max(newY, -halfH), halfH)
                                        
                                        if isDragging {
                                            // 自动吸附左右边界
                                            withAnimation(.spring(response: 0.5, dampingFraction: 0.7)) {
                                                offset = CGSize(width: newX > 0 ? halfW : -halfW, height: newY)
                                            }
                                            // 拖拽结束后立即清零 dragAmount，不触发动画
                                            dragAmount = .zero
                                            isDragging = false
                                        } else {
                                            withAnimation(.spring()) {
                                                isExpanded.toggle()
                                            }
                                        }
                                    }
                            )
                        // 只对 offset 添加动画，拖拽过程不动画
                            .animation(.spring(response: 0.4, dampingFraction: 0.7), value: offset)
                    }
                    .frame(width: geo.size.width, height: geo.size.height)
                    .onAppear {
                                    // 设置初始位置在右下角
                                    let halfW = geo.size.width / 2 - buttonSize/2 - padding
                                    let halfH = geo.size.height / 2 - buttonSize/2 - padding-230
                                    offset = CGSize(width: halfW, height: halfH)
                                }
                }
            }
            
            // 计算菜单小球偏移
            private func menuOffset(_ menuOffset: CGSize) -> CGSize {
                CGSize(
                    width: offset.width + (isDragging ? dragAmount.width : 0) + menuOffset.width,
                    height: offset.height + (isDragging ? dragAmount.height : 0) + menuOffset.height
                )
            }
            
            private func isAtBottom(_ geo: GeometryProxy) -> Bool {
                let bottomThreshold: CGFloat = 80
                return offset.height > (geo.size.height / 2 - buttonSize/2 - bottomThreshold)
            }
        }
  
    }

// 首页视图
struct HomeView: View {
    @EnvironmentObject var userData: UserData
        @Environment(\.presentationMode) var presentationMode
        @State private var filterCampus: String?
        @State private var filterCollege: String?
        @State private var filterType: String?
        @State private var searchText = ""
        @State var activityClickedid: Int = 0
    @Environment(\.colorScheme) var colorScheme // 用于检测当前颜色模式

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
        
        private var cardColor: Color {
            colorScheme == .dark ? Color(red: 0.15, green: 0.15, blue: 0.2).opacity(0.8) : Color.white
        }
        
        // 文本颜色（自动适应模式）
        private var textColor: Color {
            colorScheme == .dark ? .white : .black
        }
        
        // 次要文本颜色（自动适应模式）
        private var secondaryTextColor: Color {
            colorScheme == .dark ? .gray : .gray
        }
    var body: some View {

        NavigationView {
            ZStack(){
                
            LinearGradient(
                gradient: Gradient(colors: [secondaryColor.opacity(0.3), backgroundColor]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .edgesIgnoringSafeArea(.top)
            ScrollView {
                LazyVStack {
                    // 搜索栏
                    SearchBar(text: $searchText, placeholder: "搜索活动...")
                        .padding()
                    if(filteredActivities.isEmpty){
                        Text("欢迎来到活动广场 快发布活动吧！！！🌟").foregroundColor(Color.gray).padding()
                    }else{
                        // 活动列表
                        ForEach(filteredActivities) { activity in
                            ActivityCard(activity: activity, commentCount: userData.activityComments[activity.id]?.count ?? 0, activityID: $activityClickedid, onDelete: {
                                userData.fetchActivities()
                            })
                            .padding(.horizontal)
                            .padding(.vertical, 5)
                            .environmentObject(userData)  // <--- 这里传递环境对象
                        }
                    }
                }
            }.scrollIndicators(.hidden) // 隐藏滚动指示器
.refreshable {
                userData.fetchActivities()
            }.padding(.bottom,1)
            .navigationTitle("校园搭子")
            .navigationBarTitleDisplayMode(.inline)  // 强制标题显示为内联模式
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
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        // 校区筛选
                        Menu("校区") {
                            Button("全部校区") { filterCampus = nil }
                            Button("黄家湖校区") { filterCampus = "黄家湖" }
                            Button("青山校区") { filterCampus = "青山" }
                        }
                        
                        // 学院筛选
                        Menu("学院") {
                            Button("全部学院") { filterCollege = nil }
                            Button("材料学部") { filterCollege = "材料学部" }
                            Button("城市建设学院") { filterCollege = "城市建设学院" }
                            Button("管理学院") { filterCollege = "管理学院" }
                            Button("国际学院") { filterCollege = "国际学院" }
                            Button("化学与化工学院") { filterCollege = "化学与化工学院" }
                            Button("机械自动化学院") { filterCollege = "机械自动化学院" }
                            Button("计算机科学与技术学院") { filterCollege = "计算机科学与技术学院" }
                            Button("理学院") { filterCollege = "理学院" }
                            Button("临床学院") { filterCollege = "临床学院" }
                            Button("马克思主义学院") { filterCollege = "马克思主义学院" }
                            Button("汽车与交通工程学院") { filterCollege = "汽车与交通工程学院" }
                            Button("生命科学与健康学院") { filterCollege = "生命科学与健康学院" }
                            Button("体育学院") { filterCollege = "体育学院" }
                            Button("外国语学院") { filterCollege = "外国语学院" }
                            Button("法学与经济学院") { filterCollege = "法学与经济学院" }
                            Button("信息科学与工程学院(人工智能学院)") { filterCollege = "信息科学与工程学院(人工智能学院)" }
                            Button("艺术与设计学院") { filterCollege = "艺术与设计学院" }
                            Button("资源与环境工程学院") { filterCollege = "资源与环境工程学院" }
                            Button("冶金与能源学院") { filterCollege = "冶金与能源学院" }
                        }
                        // 活动类型筛选
                        Menu("活动类型") {
                            Button("全部类型") { filterType = nil }
                            Button("休闲娱乐") { filterType = "休闲娱乐" }
                            Button("运动健身") { filterType = "运动健身" }
                            Button("学习互助") { filterType = "学习互助" }
                        }
                    } label: {
                        Image(systemName: "line.3.horizontal.decrease.circle")
                    }
                }
            }
        }
        }.navigationBarHidden(true)
        //在改变的时候更新视图
        .onChange(of: filterCampus) { _ in applyFilters() }
        .onChange(of: filterCollege) { _ in applyFilters() }
        .onChange(of: filterType) { _ in applyFilters() }
    }
    
    func applyFilters() {
        userData.fetchActivities(
            campus: filterCampus,
            college: filterCollege,
            type: filterType
        )
    }
    private var filteredActivities: [Activity] {
        // 如果搜索文本为空，返回所有活动
        guard !searchText.isEmpty else {
            return userData.activities
        }
        // 模糊查询逻辑：不区分大小写，匹配标题、描述或地点
        return userData.activities.filter { activity in
            let searchLowercased = searchText.lowercased()
            return activity.title.lowercased().contains(searchLowercased) ||
                   activity.description.lowercased().contains(searchLowercased) ||
                   activity.location.lowercased().contains(searchLowercased) ||
                   activity.type.lowercased().contains(searchLowercased)
        }
    }
}



// 评论视图
struct MyCommentView: View{
    @Binding var ActivityId: Int
    let CreatedId: Int
    @State var newComment = ""
    @EnvironmentObject var userData: UserData
    @Environment(\.dismiss) var dismiss
    var onSave: (String) -> Void
    var onRefresh: () -> Void
    var otherCommantsCount:Int{
        commentCount-authorReplies.count
    }
    @State var showRepeatAlert:Bool = false
    // 暖色调配色方案
    private let warmBgColor = Color(red: 1.0, green: 0.96, blue: 0.88)
    private let warmCardColor = Color(red: 1.0, green: 0.98, blue: 0.92)
    private let warmPrimary = Color(red: 0.95, green: 0.5, blue: 0.2)
    private let warmSecondary = Color(red: 0.85, green: 0.45, blue: 0.15)
    private let warmTextPrimary = Color(red: 0.35, green: 0.2, blue: 0.1)
    private let warmTextSecondary = Color(red: 0.55, green: 0.35, blue: 0.2)
    private var commentCount: Int {
        userData.activityComments[ActivityId]?.count ?? 0
    }
    
    // 分离作者回复和其他评论 - 使用正确的类型
    private var authorReplies: [UserData.Comment] {
        guard let comments = userData.activityComments[ActivityId] else { return [] }
        return comments.filter { $0.userId == CreatedId }
    }
    
    private var otherComments: [UserData.Comment] {
        guard let comments = userData.activityComments[ActivityId] else { return [] }
        return comments.filter { $0.userId != CreatedId }
    }
    private var mineComments: [UserData.Comment] {
        guard let comments = userData.activityComments[ActivityId] else { return [] }
        return comments.filter { $0.userId == userData.userInfo?.userId }
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                warmBgColor.ignoresSafeArea()
                VStack(spacing: 0) {
//                    HStack(){
//                        Spacer()
//                        Text("提问").font(.title2)
//                        Spacer()
//                        Button("完成") {
//                            dismiss()
//                            userData.fetchComments(for: ActivityId)
//                        }
//                        .foregroundColor(warmPrimary)
//                    }
                    // 评论列表
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            // 作者回复部分
                            if !authorReplies.isEmpty {
                                VStack(alignment: .leading, spacing: 12) {
                                    HStack {
                                        Image(systemName: "person.crop.circle.fill.badge.checkmark")
                                            .foregroundColor(warmPrimary)
                                        Text("作者回复")
                                            .font(.system(size: 18, weight: .semibold))
                                            .foregroundColor(warmTextPrimary)
                                        Spacer()
                                    }
                                    
                                    ForEach(authorReplies, id: \.id) { comment in
                                        CommentRow(comment: comment, isAuthorReply: true, Refresh:{
                                            onRefresh()
                                        })
                                    }
                                }
                                .padding()
                                .background(warmCardColor)
                                .cornerRadius(16)
                                .padding(.horizontal)
                            }
                            
                            // 其他评论部分
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Image(systemName: "bubble.left.and.bubble.right")
                                        .foregroundColor(warmSecondary)
                                    Text("全部评论 (\(otherCommantsCount))")
                                        .font(.system(size: 18, weight: .semibold))
                                        .foregroundColor(warmTextPrimary)
                                    Spacer()
                                }
                                if otherComments.isEmpty  {
                                    EmptyCommentsView()
                                } else {
                                    ForEach(otherComments, id: \.id) { comment in
                                        CommentRow(comment: comment, isAuthorReply: false, Refresh: {
                                            onRefresh()
                                        })
                                    }
                                }
                            }
                            .padding()
                            .background(warmCardColor)
                            .cornerRadius(16)
                            .padding(.horizontal)
                        }
                        .padding(.vertical)
                    }.scrollIndicators(.hidden) // 隐藏滚动指示器
                    
                    // 输入区域
                    VStack(spacing: 0) {
                        Divider()
                        HStack(alignment: .bottom, spacing: 12) {
                            ZStack(alignment: .leading) {
                                if newComment.isEmpty {
                                    Text("说点什么...")
                                        .foregroundColor(.gray.opacity(0.7))
                                    
                                }
                                TextEditor(text: $newComment)
                                    .frame(minHeight: 30,maxHeight: 40)
                                    .opacity(newComment.isEmpty ? 0.25 : 1)
                            }
                            
                            Button(action:{
                                if !mineComments.isEmpty && CreatedId != userData.userInfo?.userId {
                                    self.showRepeatAlert = true
                                } else {
                                    submitComment()
                                }
                            }
                            ) {
                                Image(systemName: "paperplane.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(.white)
                                    .frame(width: 40, height: 40)
                                    .background(warmPrimary)
                                    .clipShape(Circle())
                            }
                            .disabled(newComment.isEmpty)
                            .opacity(newComment.isEmpty ? 0.6 : 1)
                        }
                        .padding()
                        .background(warmCardColor)
                    }
                }
            }            .navigationTitle("评论")
                        .navigationBarTitleDisplayMode(.inline)
                        .toolbar {
                            ToolbarItem(placement: .navigationBarTrailing) {
                                Button("完成") {
                                    dismiss()
                                    userData.fetchComments(for: ActivityId)
                                }
                                .foregroundColor(warmPrimary)
                            }
                        }
        }.alert(isPresented: $showRepeatAlert) {
            Alert(
                title: Text("提问上限⚠️"),
                message: Text("您一次最多只能提一个问题"),
                primaryButton: .destructive(Text("确认")) {
                },
                secondaryButton: .cancel()
            )
        }
            .onAppear {
                userData.fetchComments(for: ActivityId)
            }
        }
    private func submitComment() {
        guard !newComment.isEmpty else { return }
        onSave(newComment)
        newComment = ""
        // 延迟一点刷新，让服务器有时间处理
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            userData.fetchComments(for: ActivityId)
        }
    }
}




// 评论行视图 - 使用正确的类型
struct CommentRow: View {
    let comment: UserData.Comment
    let isAuthorReply: Bool
    @State var showDeleteConfirmation:Bool=false
    @EnvironmentObject var userData: UserData
    // 暖色调配色方案
    private let warmPrimary = Color(red: 0.95, green: 0.5, blue: 0.2)
    private let warmCardColor = Color(red: 1.0, green: 0.98, blue: 0.92)
    private let warmTextPrimary = Color(red: 0.35, green: 0.2, blue: 0.1)
    private let warmTextSecondary = Color(red: 0.55, green: 0.35, blue: 0.2)
    @StateObject private var profilePictureGetTool = ImageUploadViewModel()
    //头像图片url
    var profile_url: String?{
        //只有发布的图片才能看到
        if(profilePictureGetTool.pictureDetail?.status==1){
            profilePictureGetTool.pictureDetail?.url ?? ""
        }else{
            " "
        }
    }
    var Refresh: () -> Void
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top) {
                ZStack(){
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
                                    .foregroundColor(warmPrimary)
                                    .frame(width: 40, height: 40) // 明确设置图标尺寸
                                    .padding(10) // 调整内边距
                                    .background(Color.white)
                            @unknown default:
                                Image(systemName: "person.fill")
                                    .resizable()
                                    .frame(width: 50, height: 50) // 明确设置图标尺寸
                                    .padding(10) // 调整内边距
                                    .background(Color.white)
                            }
                        }.frame(width: 50, height: 50)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(Color.white, lineWidth: 3))
                    }else{
                        Image("avatar").resizable().frame(width: 50, height: 50)
                            .font(.system(size: 36))
                            .foregroundColor(warmPrimary)
                    }
                }
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("用户")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(warmTextPrimary)
                        
                        if isAuthorReply {
                            Text("作者")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(warmPrimary)
                                .cornerRadius(4)
                        }
                        Spacer()
                        Text(formatDate(comment.createdAt.toRelativeDateString()))
                            .font(.system(size: 12))
                            .foregroundColor(warmTextSecondary)
                    }
                    HStack(){
                        Text(comment.content)
                            .font(.system(size: 16))
                            .foregroundColor(warmTextPrimary)
                            .fixedSize(horizontal: false, vertical: true)
                        Spacer()
                        if(comment.userId==userData.userInfo?.userId){
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
                }
            }
            .padding(12)
            .background(isAuthorReply ? warmPrimary.opacity(0.1) : warmCardColor)
            .cornerRadius(12)
            
            if isAuthorReply {
                Divider()
                    .padding(.leading, 48)
            }
        }.alert(isPresented: $showDeleteConfirmation) {
            Alert(
                title: Text("删除活动"),
                message: Text("确定要删除此评论吗？"),
                primaryButton: .destructive(Text("删除")) {
                    userData.DeleteComments(commentID: comment.id){
                        success in
                        print("success")
                            Refresh()
                        
                    }
                    
                },
                secondaryButton: .cancel()
            )
        }.onAppear(){
            userData.getProfilePictureId(userId: comment.userId){
                id in
                profilePictureGetTool.fetchPictureDetail(with:id)
            }
        }
    }

    private func formatDate(_ dateString: String?) -> String {
        guard let dateString = dateString else { return "" }
        
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        
        if let date = formatter.date(from: dateString) {
            let relativeFormatter = RelativeDateTimeFormatter()
            relativeFormatter.unitsStyle = .abbreviated
            return relativeFormatter.localizedString(for: date, relativeTo: Date())
        }
        return dateString
    }
}

// 空评论状态视图
struct EmptyCommentsView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "bubble.left")
                .font(.system(size: 48))
                .foregroundColor(.gray.opacity(0.5))
            
            Text("暂无评论")
                .font(.system(size: 16))
                .foregroundColor(.gray)
            
            Text("来发表第一条评论吧")
                .font(.system(size: 14))
                .foregroundColor(.gray.opacity(0.7))
        }
        .padding(.vertical, 40).padding(.leading,100)
    }
}

// 评论行视图


// 发布视图
struct PublishView: View {
    @State var imageId:Int?
    @EnvironmentObject var userData: UserData
    @State private var title = ""
    @State private var description = ""
    @State private var type = "休闲娱乐"
    @State private var campus = "黄家湖校区"
    @State private var college = ""
    @State private var minPeople = ""
    @State private var maxPeople = ""
    @State private var location = ""
    @State private var activityTime = Date()
    @State private var expireTime = Date()
    @State private var isPublishing = false
    @State private var showSuccess = false
    @State private var showFailure = false
    @Binding var selectedTab:Int
    @Environment(\.presentationMode) var presentationMode
    let types = ["休闲娱乐", "运动健身", "学习互助", "其他"]
    let campuss = ["黄家湖校区", "青山校区", "校外活动"]
    let colleges = [
        "不限",
        "材料学部",
        "城市建设学院",
        "管理学院(恒大管理学院)",
        "国际学院",
        "化学与化工学院",
        "机械自动化学院",
        "计算机科学与技术学院",
        "理学院",
        "临床学院",
        "马克思主义学院",
        "汽车与交通工程学院",
        "生命科学与健康学院",
        "体育学院",
        "外国语学院",
        "法学与经济学院",
        "信息科学与工程学院(人工智能学院)",
        "艺术与设计学院",
        "资源与环境工程学院",
        "冶金与能源学院",
        "医学部"  // 包含医学院、公共卫生学院等医学相关单位
    ];

    var body: some View {
        NavigationView {
            Form {
                // 图片上传
                Section() {
                    ImageUploadHeightView(pictureId: $imageId)
                }.listRowBackground(Color.clear) // 将背景色设置为透明
                // 活动基本信息
                Section(header: Text("活动信息")) {
                    TextField("活动标题", text: $title)
                    TextField("活动描述", text: $description)
                    
                    Picker("活动类型", selection: $type) {
                        ForEach(types, id: \.self) { Text($0) }
                    }
                    Picker("校区", selection: $campus) {
                        ForEach(campuss, id: \.self) { Text($0) }
                    }
                    Picker("学院", selection: $college) {
                        ForEach(colleges, id: \.self) { Text($0) }
                    }
                    HStack {
                        TextField("最小人数", text: $minPeople)
                            .keyboardType(.numberPad)
                        TextField("最大人数", text: $maxPeople)
                            .keyboardType(.numberPad)
                    }
                    
                    TextField("地点", text: $location)
                }
                
                // 时间设置
                Section(header: Text("时间设置")) {
                    DatePicker("活动时间", selection: $activityTime, displayedComponents: [.date, .hourAndMinute])
                    DatePicker("报名截止", selection: $expireTime, in: Date()..., displayedComponents: [.date, .hourAndMinute])
                }
                
              
                // 发布按钮
                Section {
                    Button(action: publishActivity) {
                        if isPublishing {
                            ProgressView()
                        } else {
                            Text("发布活动")
                                .frame(maxWidth: .infinity)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .disabled(title.isEmpty || description.isEmpty || minPeople.isEmpty || maxPeople.isEmpty || location.isEmpty)
                }
            }
            .alert("发布成功", isPresented: $showSuccess) {
                Button("确定", role: .cancel) { resetForm() }
            }
            .alert("发布失败", isPresented: $showFailure) {
                Button("确定", role: .cancel) { }
            }
            .navigationTitle("发布活动")
            .navigationBarTitleDisplayMode(.inline)  // 强制标题显示为内联模式
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        selectedTab=0
                    }) {
                        HStack {
                            Image(systemName: "chevron.left")  // 返回箭头
                            Text("广场")  // 可选的文本
                        }
                    }
                }
            }
        }.navigationBarHidden(true)
       
    }
    func publishActivity() {
        isPublishing = true
        let dateFormatter = ISO8601DateFormatter()
        dateFormatter.formatOptions = [.withInternetDateTime]
        let newActivity = Activity(
            id: 0,
            creatorId: userData.userInfo?.userId ?? 0,
            title: title,
            description: description,
            type: type,
            activityTime: dateFormatter.string(from: activityTime),
            location: location,
            minPeople: Int(minPeople) ?? 2,
            maxPeople: Int(maxPeople) ?? 10,
            expireTime: dateFormatter.string(from: expireTime),
            campus: campus,
            college: college,
            tags: nil,
            status: "进行中",
            createdAt: dateFormatter.string(from: Date()),
            updatedAt: nil,
            imageId: imageId
        )
        
        userData.createActivity(activity: newActivity) { success in
            isPublishing = false
            if success {
                showSuccess = true
            } else {
                showFailure = true
            }
        }
    }
    
    func resetForm() {
        title = ""
        description = ""
        type = "休闲娱乐"
        campus = ""
        college = ""
        minPeople = ""
        maxPeople = ""
        location = ""
        imageId = 0
    }
}
//组件
struct SearchBar: View {
    @Binding var text: String
    var placeholder: String
    var body: some View {
        HStack {
            TextField(placeholder, text: $text)
                .padding(8)
                .padding(.horizontal, 24)
                .background(Color(.white))
                .cornerRadius(8)
                .overlay(
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.gray)
                            .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                            .padding(.leading, 8)
                    }
                )
            if !text.isEmpty {
                Button(action: {
                    text = ""
                }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                        .padding(.trailing, 8)
                }
            }
        }
    }
}
// 图片选择器
struct ImagePicker: UIViewControllerRepresentable {
    @Binding var imageUrl: String
    @Environment(\.presentationMode) var presentationMode
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = .photoLibrary
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            // 模拟图片上传
            parent.imageUrl = "https://example.com/uploaded_image.jpg"
            parent.presentationMode.wrappedValue.dismiss()
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.presentationMode.wrappedValue.dismiss()
        }
    }
}

// 绑定扩展，用于处理可选字符串
extension Binding where Value == String? {
    init(_ source: Binding<String>, replacingNilWith nilProxy: String) {
        self.init(
            get: { source.wrappedValue },
            set: { newValue in
                if let newValue = newValue {
                    source.wrappedValue = newValue
                } else {
                    source.wrappedValue = nilProxy
                }
            }
        )
    }
}
extension Binding {
    init<T>(_ base: Binding<T?>, replacingNilWith nilProxy: T) where Value == T {
        self.init(
            get: { base.wrappedValue ?? nilProxy },
            set: { base.wrappedValue = $0 }
        )
    }
}
