//
//  ActivityCard.swift
//  study_test
//
//  Created by wust_lh on 2025/8/1.
//

import SwiftUI

// 活动卡片视图
struct ActivityCard: View {
    let activity: Activity
    @EnvironmentObject var userData: UserData
    @State private var isExpanded = false
    @State private var hasInitialized = false
    // 状态管理
    var isLiked:Bool{
        if(userData.likedActivityIDs.contains(activity.id)){
            true
        }else{
            false
        }
    }
    var isfavorite:Bool{
        if(userData.FavoriteActivityIDs.contains(activity.id)){
            true
        }else{
            false
        }
    }
    @State private var showComments = false
    var isApplied: Bool {
        userData.applicationStatusMap[activity.id] != nil
    }

    var appliedStatus: String {
        guard let status = userData.applicationStatusMap[activity.id] else {
            return "申请参加"
        }
        return status == "PENDING" ? "已申请 ✓" : status
    }
    @State var showAlter:Bool=false
    @State private var isLoading = false
    @State private var showLikeError = false
    @State private var showingApplySheet = false
    @State private var applyReason  = ""
    @State private var showWriteReason:Bool=false
    @State private var showDeleteConfirmation = false
    @State private var isDeleting = false
    @State private var deleteError: String?
     var commentCount:Int
    @AppStorage("ID") var StudentNumber:String=""
    //全局绑定的选中的id
    @Binding  var activityID:Int
    // 颜色定义
    let primaryColor = Color.blue
    let likedColor = Color.red
    let favoriteColor = Color.orange
    @StateObject private var viewModel = ImageUploadViewModel()
    var image_url: String? {

        // 2. 安全解包 viewModel.pictureDetail（避免多次写 ?. ，且防止 nil 调用）
        guard let pictureDetail = viewModel.pictureDetail else {
            return nil // 若 pictureDetail 为 nil，返回 nil
        }
        
        if pictureDetail.status == 1  {
            return pictureDetail.url // 条件成立，返回图片URL
        } else {
            return nil // 条件不成立，返回 nil（补充你的需求）
        }
    }
    // 环境对象
    @Environment(\.presentationMode) var presentationMode
    // 新增：删除回调
    var onDelete: (() -> Void)
    private var canDelete: Bool {
        // 假设 StudentNumber 是字符串类型
        if String(activity.creatorId) == StudentNumber {
            return true
        } else {
            return false
        }
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
                        onDelete()
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
                    Text(activity.title) .foregroundColor(.black)
                        .font(.headline)
                    Text("\(activity.campus ?? "") \(activity.college ?? "")")
                        .font(.subheadline)
                        .foregroundColor(.black)
                }
                Spacer()
                Text(activity.type)
                    .font(.caption)
                    .padding(8)
                    .background(primaryColor.opacity(0.1))
                    .foregroundColor(primaryColor)
                    .cornerRadius(8)
                // 添加删除按钮（条件显示）
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
                }
               
            }
           
            
            // 活动详情
            Text(activity.description)
                .font(.body) .foregroundColor(.black)
                .lineLimit(isExpanded ? nil : 3)
            
            // 活动时间地点
            VStack(alignment: .leading, spacing: 5) {
                HStack {
                    Image(systemName: "calendar")
                    Text("\(activity.activityTime)")
                }
                
                HStack {
                    Image(systemName: "mappin.and.ellipse").foregroundColor(Color.orange)
                    Text(activity.location).foregroundColor(Color.black)
                }
            }
            .font(.subheadline)
            .foregroundColor(.black)
            .padding(.top, 5)
            
            // 人数限制
            HStack {
                Image(systemName: "person.2").foregroundColor(Color.blue).padding(.leading,-2)
                Text("\(activity.minPeople)-\(activity.maxPeople)人").foregroundColor(Color.black)
            }
            .font(.subheadline)
            .foregroundColor(.black)
            
            // 操作按钮
            HStack {
                // 1. 点赞按钮
                Button(action: toggleLike) {
                    HStack {
                        Image(systemName: isLiked ? "heart.fill" : "heart")
                            .foregroundColor(isLiked ? likedColor : .black)
                        Text("\(userData.activityStats[activity.id]?.likeCount ?? 0)").foregroundColor(.black)
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
                        Image(systemName: "message").foregroundColor(.black)
                        Text("\(commentCount)").foregroundColor(.black)
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
                        Text("\(userData.activityStats[activity.id]?.favoriteCount ?? 0)").foregroundColor(.black)
                    }
                }
                .buttonStyle(PlainButtonStyle())
                .padding(.trailing, 15)
                Spacer()
                // 3. 申请参加按钮
                Button(action: {
                    // 检查是否是自己的活动
                    if let userId = userData.userInfo?.userId, activity.creatorId == userId {
                        showAlter = true // 显示提示，不能申请自己的活动
                    } else {
                        if(isApplied){
                            applyAction() // 执行申请操作
                        }else{
                            showingApplySheet.toggle()
                        }
                    }
                }){
                    Text(appliedStatus)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 8)
                        .background(isApplied ? Color.green : primaryColor)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                        .font(.subheadline)
                }.alert("提示", isPresented: $showAlter) {
                    Button("确定", role: .cancel) { }
                } message: {
                    Text("不能申请自己发布的活动哦～")
                }
            }
            .sheet(isPresented: $showingApplySheet) {
                VStack(spacing: 20) {
                    // 标题
                    Text("申请加入")
                        .font(.headline)
                        .padding(.top, 20)
                    
                    // 输入框
                    TextField("快来加入吧！说说你的理由～🥳", text: $applyReason)
                        .submitLabel(.done)
                        .multilineTextAlignment(.center)
                        .frame(height: 80)
                        .padding(.horizontal, 16)
                        .foregroundColor(Color("courseTitleColor"))
                        .background(Color.white)
                        .cornerRadius(10)
                        .shadow(color: Color.orange.opacity(0.3), radius: 10, x: 0, y: 10)
                        .padding(.horizontal, 20)
                    
                    // 按钮区域
                    HStack(spacing: 24) {
                        // 取消按钮 - 现代浅灰色风格
                        Button("取消") {
                            showingApplySheet = false
                            applyReason = "" // 清空输入
                        }
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.secondary)
                        .padding(.horizontal, 28)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color(.systemBackground))
                                .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
                        )
                        .hoverEffect(.lift) // 鼠标悬停效果（macOS/iOS 15+）
                        
                        // 确认按钮 - 现代强调色风格
                        Button("确认发送") {
                            showingApplySheet = false
                            // 在这里添加发送申请的逻辑
                            applyAction()
                        }
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 28)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.orange)
                                .shadow(color: Color.orange.opacity(0.2), radius: 6, x: 0, y: 4)
                        )
                        .hoverEffect(.lift) // 鼠标悬停效果
                        .animation(.easeInOut, value: applyReason) // 微妙的动画反馈
                    }
                    .padding(.bottom, 24)
                }
                .frame(maxWidth: .infinity)
                .presentationDetents([.fraction(0.4)]) // 控制弹窗高度为屏幕的40%
                .presentationBackground(.ultraThinMaterial) // 半透明背景
            }
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
        .onAppear(){
            viewModel.fetchPictureDetail(with: activity.imageId ?? 0)
        }
    }
    // 点赞功能
    private func toggleLike() {
        if isLiked{
            userData.likedActivityIDs.remove(activity.id)
            userData.activityStats[activity.id]?.likeCount = (userData.activityStats[activity.id]?.likeCount ?? 0) - 1
        }else{
            userData.likedActivityIDs.insert(activity.id)
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
        if isfavorite{
            userData.FavoriteActivityIDs.remove(activity.id)
            userData.activityStats[activity.id]?.favoriteCount = (userData.activityStats[activity.id]?.favoriteCount ?? 0) - 1
        }else{
            userData.FavoriteActivityIDs.insert(activity.id)
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
    private func applyAction() {
        if(!isApplied){
            userData.applyForActivity(activityId: activity.id, reason: applyReason) { success in
                DispatchQueue.main.async {
                    if success {
                        // 关键：更新全局映射表，触发计算属性自动刷新
                        self.userData.applicationStatusMap[self.activity.id] = "PENDING"
                        // 无需手动修改 isApplied 和 appliedStatus（它们会自动同步）
                        self.presentationMode.wrappedValue.dismiss()
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                            print("已成功申请参加活动 \(self.activity.id)")
                        }
                    }
                }
            }
        }else{
            print("取消申请ing～～～")
            guard let appid=userData.applicationIdMap[self.activity.id] else {
                print("找不到要取消的信息")
                return
            }
            userData.CancleApplyForActivity(appid: appid) { success in
                DispatchQueue.main.async {
                    if success {
                        // 关键：更新全局映射表，触发计算属性自动刷新
                        self.userData.applicationStatusMap[self.activity.id] = nil
                        // 无需手动修改 isApplied 和 appliedStatus（它们会自动同步）
                        self.presentationMode.wrappedValue.dismiss()
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                            print("已成功取消申请 \(self.activity.id)")
                        }
                    }
                }
            }
        }
    }
}

