import SwiftUI

struct MatchListView: View {
    @State private var matches: [CompetitionPost] = []
    @State private var searchText = ""
    @State private var showingAddView = false
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var currentPage = 1
    @State private var totalPages = 1
    @State private var showingProfile = false
    @AppStorage("authData") private var authData: String = ""
    @Environment(\.dismiss) var dismiss
    private let pageSize = 10
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 搜索栏
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.secondary)
                    TextField("搜索比赛", text: $searchText)
                        .onSubmit {
                            currentPage = 1
                            fetchMatches()
                        }
                    
                    if !searchText.isEmpty {
                        Button(action: {
                            searchText = ""
                            currentPage = 1
                            fetchMatches()
                        }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(10)
                .background(Color(.systemGray6))
                .cornerRadius(10)
                .padding(.horizontal)
                .padding(.top, 8)
                
                if isLoading {
                    ProgressView("加载中...")
                        .padding(.vertical, 30)
                } else if let error = errorMessage {
                    VStack {
                        Text("错误: \(error)")
                            .foregroundColor(.red)
                            .padding()
                        
                        Button("重试") {
                            fetchMatches()
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding(.vertical, 30)
                } else if matches.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "trophy")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary)
                        Text("暂无比赛")
                            .font(.title2)
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 50)
                } else {
                    List {
                        ForEach(matches) { match in
                            NavigationLink(destination: MatchDetailView(match: match)) {
                                MatchCardView(match: match)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        
                        if currentPage < totalPages {
                            Button(action: loadMore) {
                                HStack {
                                    Spacer()
                                    Text("加载更多")
                                    Image(systemName: "arrow.down")
                                    Spacer()
                                }
                                .padding()
                                .background(Color(.systemGray6))
                                .cornerRadius(10)
                            }
                        }
                    }
                    .listStyle(PlainListStyle()) // 更接近 ScrollView 的外观

                }
                
                Spacer()
                
                // 添加组队按钮
                Button(action: {
                    showingAddView = true
                }) {
                    Label("添加组队信息", systemImage: "plus")
                        .font(.headline)
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                }
                .padding(.horizontal)
                .padding(.bottom)
            }
            .navigationTitle("比赛列表")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                // 自定义返回按钮
                   ToolbarItem(placement: .navigationBarLeading) {
                       Button(action: {
                           // 关闭当前视图，返回上一层或首页
                           dismiss()  // 或者 dismiss()
                       }) {
                           HStack {
                               Image(systemName: "chevron.left")
                               Text("首页")
                           }
                       }
                   }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        showingProfile = true
                    }) {
                        Image(systemName: "person.crop.circle")
                            .font(.title2)
                    }
                }
            }
            .sheet(isPresented: $showingAddView) {
                AddMatchView(onAdd: { newMatch in
                    createMatch(newMatch)
                })
            }
            .sheet(isPresented: $showingProfile) {
                PersonalProfileView(ifShow:$showingProfile)
            }
            .onAppear {
                if matches.isEmpty {
                    fetchMatches()
                }
            }
        }.navigationBarBackButtonHidden(true)
        .accentColor(.blue)
    }
    
    // 获取比赛列表
    private func fetchMatches() {
        isLoading = true
        errorMessage = nil
        
        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }
        
        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/competitionPost/page") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }
        print("Wuster \(authData)")
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15
        
        // 请求体模型
        let requestBody: [String: Any] = [
            "studentId": "",
            "status": 0,  // 0表示查询所有
            "competitionName": searchText,
            "page": currentPage,
            "pageSize": pageSize
        ]
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: requestBody, options: [])
        } catch {
            isLoading = false
            errorMessage = "创建请求失败: \(error.localizedDescription)"
            return
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                
                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }
                
                guard let data = data else {
                    errorMessage = "没有收到数据"
                    return
                }
                
                do {
                    let decoder = JSONDecoder()
                    let formatter = DateFormatter()
                    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
                    decoder.dateDecodingStrategy = .formatted(formatter)
                    
                    let response = try decoder.decode(APIResponse<PageData<CompetitionPost>>.self, from: data)
                    
                    if response.code == 1 {
                        let newMatches = response.data?.records ?? []
                        
                        if currentPage == 1 {
                            matches = newMatches
                        } else {
                            matches.append(contentsOf: newMatches)
                        }
                        
                        totalPages = (response.data?.total ?? 0 + pageSize - 1) / pageSize
                    } else {
                        errorMessage = response.msg ?? "未知错误"
                    }
                } catch {
                    errorMessage = "解析错误: \(error.localizedDescription)"
                }
            }
        }.resume()
    }
    
    // 加载更多
    private func loadMore() {
        currentPage += 1
        fetchMatches()
    }
    
    // 创建新比赛
    private func createMatch(_ newMatch: CompetitionPost) {
        isLoading = true
        errorMessage = nil
        
        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }
        
        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/competitionPost") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let createRequest = CreateCompetitionPostRequest(
            studentId: newMatch.studentId,
            competitionName: newMatch.competitionName,
            competitionIntroduction: newMatch.competitionIntroduction,
            requirement: newMatch.requirement,
            contactInformation: newMatch.contactInformation
        )
        
        do {
            request.httpBody = try JSONEncoder().encode(createRequest)
        } catch {
            isLoading = false
            errorMessage = "创建请求失败: \(error.localizedDescription)"
            return
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                
                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }
                
                // 重新加载列表
                currentPage = 1
                fetchMatches()
            }
        }.resume()
    }
    
    // 删除比赛
    private func deleteMatch(at indexSet: IndexSet) {
        guard let index = indexSet.first else { return }
        let match = matches[index]
        
        isLoading = true
        errorMessage = nil
        
        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }
        
        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/competitionPost/\(match.cid)") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15
        
        URLSession.shared.dataTask(with: request) { _, response, error in
            DispatchQueue.main.async {
                isLoading = false
                
                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }
                
                // 重新加载列表
                currentPage = 1
                fetchMatches()
            }
        }.resume()
    }
}

// MARK: - 现代化UI组件
struct MatchCardView: View {
    let match: CompetitionPost
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(match.competitionName)
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Spacer()
                
//                statusBadge
            }
            
            Text(match.competitionIntroduction)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .lineLimit(2)
            
            HStack {

                Text("点击查看详细")
                    .font(.caption)
                    .foregroundColor(.blue)
                
                Spacer()
                
                Text(formattedDate(match.endUpdateTime))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
        .padding(.horizontal)
    }
    
//    private var statusBadge: some View {
//        Text(statusString(for: match.status))
//            .font(.caption2)
//            .padding(.horizontal, 8)
//            .padding(.vertical, 4)
//            .background(statusColor(for: match.status))
//            .foregroundColor(.white)
//            .cornerRadius(12)
//    }
    
//    private func statusString(for status: Int) -> String {
//        switch status {
//        case 0: return "招募中"
//        case 1: return "进行中"
//        case 2: return "已结束"
//        default: return "未知状态"
//        }
//    }
    
    private func statusColor(for status: Int) -> Color {
        switch status {
        case 0: return Color.green
        case 1: return Color.blue
        case 2: return Color.gray
        default: return Color.secondary
        }
    }
    
    private func formattedDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter.string(from: date)
    }
}

// MARK: - 添加比赛视图（核心修改：新增联系方式类型选择）
struct AddMatchView: View {
    @Environment(\.dismiss) var dismiss
    @State private var name = ""
    @State private var intro = ""
    @State private var requirements = ""
    @State private var contact = ""
    @State private var studentId = ""//默认学号占位 没用
    @State private var errorMessage: String?
    @State private var showingErrorAlert = false
    
    // 新增1：定义联系方式类型枚举（QQ/邮箱）
    enum ContactType: String, CaseIterable, Identifiable {
        case qq = "QQ"
        case email = "邮箱"
        var id: Self { self }
    }
    // 新增2：默认选择QQ类型
    @State private var selectedContactType: ContactType = .qq
    
    // 正则表达式：匹配纯数字（QQ号）和标准邮箱
    private let qqRegex = "^\\d{5,15}$" // QQ号限制5-15位数字（符合实际规则）
    private let emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    
    var onAdd: (CompetitionPost) -> Void
    
    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("比赛信息").font(.headline)) {
                    TextField("比赛名称*", text: $name)
                    TextField("比赛简介*", text: $intro)
                    TextField("比赛要求*", text: $requirements)
                    
                    // 新增3：联系方式类型选择器（SegmentedPicker）
                    Picker("联系方式类型", selection: $selectedContactType) {
                        ForEach(ContactType.allCases) { type in
                            Text(type.rawValue)
                        }
                    }
                    .pickerStyle(SegmentedPickerStyle())
                    
                    // 新增4：根据选择的类型，动态修改输入框占位提示
                    TextField(
                        selectedContactType == .qq ? "请输入QQ号（纯数字）*" : "请输入邮箱地址*",
                        text: $contact
                    )
                    // 可选：实时校验输入格式，显示辅助提示
                    .overlay(
                        Text(validateContactPreview(contact))
                            .font(.caption)
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .trailing),
                        alignment: .trailing
                    )
                }
            }
            .alert("错误", isPresented: $showingErrorAlert, actions: {
                Button("确定", role: .cancel) {}
            }, message: {
                if let errorMessage = errorMessage {
                    Text(errorMessage)
                }
            })
            .navigationTitle("添加比赛")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("保存") {
                        saveMatch()
                    }
                    // 禁用逻辑：字段为空 或 格式不合法时禁用保存
                    .disabled(name.isEmpty || intro.isEmpty || requirements.isEmpty || contact.isEmpty || !isValidContact(contact))
                }
            }
        }
    }
    
    private func saveMatch() {
        // 1. 基础空格trim校验
        let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedIntro = intro.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedRequirements = requirements.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedContact = contact.trimmingCharacters(in: .whitespacesAndNewlines)
        
        guard !trimmedName.isEmpty,
              !trimmedIntro.isEmpty,
              !trimmedRequirements.isEmpty,
              !trimmedContact.isEmpty else {
            errorMessage = "所有字段都必须填写，且不能为空格"
            showingErrorAlert = true
            return
        }
        
        // 2. 联系方式格式校验（根据选择的类型匹配对应规则）
        guard isValidContact(trimmedContact) else {
            errorMessage = selectedContactType == .qq
                ? "QQ号格式错误！请输入5-15位纯数字"
                : "邮箱格式错误！请输入标准邮箱（如xxx@xxx.com）"
            showingErrorAlert = true
            return
        }
        
        // 3. 新增5：根据选择的类型，自动拼接前缀（QQ: / 邮箱：）
        let contactWithPrefix = selectedContactType == .qq
            ? "QQ:\(trimmedContact)"
            : "邮箱：\(trimmedContact)"
        
        // 4. 提交带前缀的联系方式到后台
        let newMatch = CompetitionPost(
            cid: 0,
            studentId: studentId,
            competitionName: trimmedName,
            competitionIntroduction: trimmedIntro,
            requirement: trimmedRequirements,
            contactInformation: contactWithPrefix, // 传带前缀的联系方式
            status: 0,
            endUpdateTime: Date()
        )
        
        onAdd(newMatch)
        dismiss()
    }
    
    // 新增6：根据选择的类型，校验联系方式格式
    private func isValidContact(_ contact: String) -> Bool {
        let trimmedContact = contact.trimmingCharacters(in: .whitespacesAndNewlines)
        let predicate: NSPredicate
        
        // 选择QQ则匹配数字规则，选择邮箱则匹配邮箱规则
        if selectedContactType == .qq {
            predicate = NSPredicate(format: "SELF MATCHES %@", qqRegex)
        } else {
            predicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        }
        
        return predicate.evaluate(with: trimmedContact)
    }
    
    // 新增7：实时预览校验结果（辅助用户输入，可选但体验更好）
    private func validateContactPreview(_ contact: String) -> String {
        let trimmedContact = contact.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedContact.isEmpty else { return "" }
        
        if selectedContactType == .qq {
            return NSPredicate(format: "SELF MATCHES %@", qqRegex).evaluate(with: trimmedContact)
                ? ""
                : "需5-15位纯数字"
        } else {
            return NSPredicate(format: "SELF MATCHES %@", emailRegex).evaluate(with: trimmedContact)
                ? ""
                : "格式错误"
        }
    }
}

// MARK: - 比赛详情视图
struct MatchDetailView: View {
    let match: CompetitionPost
    @State private var comments: [ResponsePost] = []
    @State private var newComment = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showingCommentSheet = false
    @AppStorage("authData") private var authData: String = ""
    @AppStorage("ID") var Sid:String=""
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // 比赛名称 + 状态
                    HStack {
                        Text(match.competitionName)
                            .font(.title)
                            .fontWeight(.bold)
                        
                        Spacer()
                    }
                    .padding(.bottom, 5)
                    // 比赛信息卡片
                    VStack(alignment: .leading, spacing: 15) {
                        CompetitionInfoRow(icon: "text.bubble", title: "比赛简介", content: match.competitionIntroduction)
                        CompetitionInfoRow(icon: "list.bullet", title: "比赛要求", content: match.requirement)
                        CompetitionInfoRow(icon: "phone", title: "联系方式", content: match.contactInformation)
                        CompetitionInfoRow(icon: "calendar", title: "更新时间", content: formattedDate(match.endUpdateTime))
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                    // 申请展示区
                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                Text("申请")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                //如果是自己发布的帖子
                                if(match.studentId==Sid){
                                    Text("(\(comments.count))")
                                        .foregroundColor(.secondary)
                                }
                            }
                            if comments.isEmpty {
                                Text("暂无申请")
                                    .foregroundColor(.secondary)
                                    .frame(maxWidth: .infinity, alignment: .center)
                                    .padding(.vertical, 30)
                            } else {
                                ForEach(comments) { comment in
                                    //如果是自己发的 显示所有申请
                                    if(match.studentId==Sid){
                                        CommentView(comment: comment, authData: $authData, onSave:{status in
                                            fetchComments()
                                        })
                                    }else{
                                    //如果不是自己发的 只显示自己发的
                                        if(comment.studentId==Sid){
                                            CommentView(comment: comment, authData: $authData, onSave:{status in
                                                fetchComments()
                                            })
                                        }
                                    }
                                }
                            }
                        }
                        .padding(.top, 10)
                        
                        Spacer()
                    
                }
                .padding()
            }
            
            // 右下角评论按钮
            Button(action: { showingCommentSheet.toggle() }) {
                Image(systemName: "plus")
                    .font(.title.weight(.semibold))
                    .padding(20)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .clipShape(Circle())
                    .shadow(radius: 5)
            }
            .padding()
        }.task {
            fetchComments()
        }
        .navigationTitle("比赛详情")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showingCommentSheet) {
            CommentSheetView(newComment: $newComment, onSubmit: submitComment)
        }
    }
    private func formattedDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        return formatter.string(from: date)
    }
    // 获取评论
    private func fetchComments() {
        isLoading = true
        errorMessage = nil
        
        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }
        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/responsePost/getPage") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15
        
        let body: [String: Any] = [
            "cid": match.cid,
            "studentId": "",
            "page": 1,
            "pageSize": 20
        ]
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
        } catch {
            isLoading = false
            errorMessage = "请求参数序列化失败: \(error.localizedDescription)"
            return
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                
                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }
                
                guard let data = data else {
                    errorMessage = "没有收到数据"
                    return
                }
                
                do {
                    let decoder = JSONDecoder()
                    let formatter = DateFormatter()
                    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
                    decoder.dateDecodingStrategy = .formatted(formatter)
                    let response = try decoder.decode(APIResponse<PageData<ResponsePost>>.self, from: data)
                    if response.code == 1 {
                        comments = response.data?.records ?? []
                    } else {
                        errorMessage = response.msg ?? "未知错误"
                    }
                } catch {
                    errorMessage = "解析错误: \(error.localizedDescription)"
                }
            }
        }.resume()
    }
    
    // 提交评论
    private func submitComment() {
        isLoading = true
        errorMessage = nil
        
        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }
        
        guard !newComment.isEmpty else {
            isLoading = false
            errorMessage = "评论内容不能为空"
            return
        }
        
        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/responsePost") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let createRequest = CreateResponsePostRequest(
            cid: match.cid,
            studentId: "当前用户ID", // 需要替换为实际用户ID
            response: newComment
        )
        
        do {
            request.httpBody = try JSONEncoder().encode(createRequest)
        } catch {
            isLoading = false
            errorMessage = "创建请求失败: \(error.localizedDescription)"
            return
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                
                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }
                
                // 清空输入框并重新加载评论
                newComment = ""
                fetchComments()
            }
        }.resume()
    }
}

// MARK: - 辅助视图
struct CompetitionInfoRow: View {
    let icon: String
    let title: String
    let content: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .frame(width: 24)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.secondary)
                
                Text(content)
                    .foregroundColor(.primary)
            }
        }
    }
}

struct CommentView: View {
    let comment: ResponsePost
    @Binding  var authData: String
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State var isDelete:Bool=false
    var onSave:(Int) -> Void
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if !isDelete {
            HStack {
                Text("申请信息:")
                    .font(.subheadline)
                    .fontWeight(.medium)
                Spacer()
                Text(formattedDate(comment.endUpdateTime))
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .frame(minWidth: 100, alignment: .leading)
            }
            
                ZStack(alignment: .topTrailing) {
                    // 背景区块
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color(.systemGray6))
                    
                    // 文字内容，可自动换行
                    Text(comment.response)
                        .padding(12)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // 删除按钮固定在右上角
                    Button {
                        deleteComment()
                    } label: {
                        Circle()
                            .fill(Color.blue.opacity(0.5))
                            .frame(width: 20, height: 20)
                            .overlay {
                                Image(systemName: "xmark")
                                    .resizable()
                                    .foregroundColor(.white)
                                    .frame(width: 10, height: 10)
                            }
                    }
                    .padding(5) // 离右上角的距离
                }
                .fixedSize(horizontal: false, vertical: true) // 宽度可随父容器扩展，高度随文字换行自适应
                .padding(.trailing, 30) // 可选：避免紧贴屏幕右边
            }
        }
        .padding(.vertical, 8)

    }
    private func deleteComment() {
        isLoading = true
        errorMessage = nil
        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }
        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/responsePost/\(comment.rid)") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15
        URLSession.shared.dataTask(with: request) { _, response, error in
            DispatchQueue.main.async {
                isLoading = false
                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }
                // 成功删除，刷新
                isDelete=true
            }
        }.resume()
    }
    private func formattedDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MM-dd HH:mm"
        return formatter.string(from: date)
    }
}

// MARK: - 个人中心视图
enum MatchViewType: String, CaseIterable {
    case published = "我的发布"
    case registered = "已报名"
}
struct PersonalProfileView: View {
    @State private var myMatches: [CompetitionPost] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var currentPage = 1
    @State private var totalPages = 1
    @State private var selectedView: MatchViewType = .published
    @Binding var ifShow: Bool
    @AppStorage("authData") private var authData: String = ""
    private let pageSize = 10
    var body: some View {
        NavigationStack {
            VStack {
                // 顶部 Picker 切换视图
                Picker("选择视图", selection: $selectedView) {
                    ForEach(MatchViewType.allCases, id: \.self) { type in
                        Text(type.rawValue).tag(type)
                    }
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding(.horizontal)
                .onChange(of: selectedView) { _ in
                    currentPage = 1
                    fetchMyMatches()
                }

                if isLoading {
                    ProgressView("加载中...")
                        .padding(.vertical, 30)
                } else if let error = errorMessage {
                    VStack {
                        Text("错误: \(error)")
                            .foregroundColor(.red)
                            .padding()
                        Button("重试") {
                            fetchMyMatches()
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding(.vertical, 30)
                } else if myMatches.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "doc.plaintext")
                            .font(.system(size: 60))
                            .foregroundColor(.secondary)
                        Text("暂无相关比赛")
                            .font(.title2)
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 50)
                } else {
                    List {
                        ForEach(myMatches) { match in
                            NavigationLink(destination: MatchDetailView(match: match)) {
                                MatchCardView(match: match)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        .onDelete(perform: selectedView == .published ? deleteMatch : nil)
                        
                        if currentPage < totalPages {
                            Button(action: loadMore) {
                                HStack {
                                    Spacer()
                                    Text("加载更多")
                                    Image(systemName: "arrow.down")
                                    Spacer()
                                }
                                .padding()
                                .background(Color(.systemGray6))
                                .cornerRadius(10)
                            }
                        }
                    }
                    .listStyle(PlainListStyle())
                }

                Spacer()
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("完成") {
                        ifShow = false
                    }
                }
            }
            .onAppear {
                if myMatches.isEmpty {
                    fetchMyMatches()
                }
            }
        }
    }

    private func fetchMyMatches() {
        isLoading = true
        errorMessage = nil

        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }

        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/competitionPost/page") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15

        let statusValue = (selectedView == .published) ? 1 : 2

        let requestBody: [String: Any] = [
            "status": statusValue,
            "page": currentPage,
            "pageSize": pageSize
        ]

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: requestBody, options: [])
        } catch {
            isLoading = false
            errorMessage = "创建请求失败: \(error.localizedDescription)"
            return
        }

        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false

                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }

                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }

                guard let data = data else {
                    errorMessage = "没有收到数据"
                    return
                }

                do {
                    let decoder = JSONDecoder()
                    let formatter = DateFormatter()
                    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
                    decoder.dateDecodingStrategy = .formatted(formatter)

                    let response = try decoder.decode(APIResponse<PageData<CompetitionPost>>.self, from: data)

                    if response.code == 1 {
                        let newMatches = response.data?.records ?? []
                        if currentPage == 1 {
                            myMatches = newMatches
                        } else {
                            myMatches.append(contentsOf: newMatches)
                        }

                        totalPages = (response.data?.total ?? 0 + pageSize - 1) / pageSize
                    } else {
                        errorMessage = response.msg ?? "未知错误"
                    }
                } catch {
                    errorMessage = "解析错误: \(error.localizedDescription)"
                }
            }
        }.resume()
    }

    private func deleteMatch(at indexSet: IndexSet) {
        guard selectedView == .published else { return }
        guard let index = indexSet.first else { return }
        let match = myMatches[index]

        isLoading = true
        errorMessage = nil

        guard !authData.isEmpty else {
            isLoading = false
            errorMessage = "未登录，请重新登录"
            return
        }

        guard let url = URL(string: "\(BasicValue.CompetitionbaseUrl)/competitionPost/\(match.cid)") else {
            isLoading = false
            errorMessage = "无效的API地址"
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15

        URLSession.shared.dataTask(with: request) { _, response, error in
            DispatchQueue.main.async {
                isLoading = false

                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    return
                }

                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器错误: \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                    return
                }

                // 成功删除，刷新第一页
                currentPage = 1
                fetchMyMatches()
            }
        }.resume()
    }
    private func loadMore() {
        currentPage += 1
        fetchMyMatches()
    }
}

// MARK: - 评论输入视图
struct CommentSheetView: View {
    @Environment(\.dismiss) var dismiss
    @Binding var newComment: String
    var onSubmit: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("添加评论")
                .font(.title2)
                .fontWeight(.bold)
                .padding(.top)
            
            TextEditor(text: $newComment)
                .frame(minHeight: 150)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(12)
            
            HStack {
                Button("取消") {
                    dismiss()
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color(.systemGray5))
                .foregroundColor(.primary)
                .cornerRadius(10)
                
                Button("提交") {
                    onSubmit()
                    dismiss()
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
            }
            .padding(.bottom)
        }
        .padding()
    }
}

// MARK: - 数据结构
struct APIResponse<T: Codable>: Codable {
    let code: Int
    let msg: String?
    let data: T?
}

struct PageData<T: Codable>: Codable {
    let total: Int
    let records: [T]
}

struct CompetitionPost: Codable, Identifiable {
    let cid: Int
    let studentId: String
    let competitionName: String
    let competitionIntroduction: String
    let requirement: String
    let contactInformation: String
    let status: Int
    let endUpdateTime: Date
    var id: Int { cid }
}

struct CreateCompetitionPostRequest: Codable {
    let studentId: String
    let competitionName: String
    let competitionIntroduction: String
    let requirement: String
    let contactInformation: String
}

struct ResponsePost: Codable, Identifiable {
    let rid: Int
    let cid: Int
    let studentId: String
    let response: String
    let endUpdateTime: Date
    var id: Int { rid }
}

struct CreateResponsePostRequest: Codable {
    let cid: Int
    let studentId: String
    let response: String
}

// MARK: - 预览
struct GroupCompetition: View {
    var body: some View {
        MatchListView()
    }
}

#Preview {
    GroupCompetition()
}
