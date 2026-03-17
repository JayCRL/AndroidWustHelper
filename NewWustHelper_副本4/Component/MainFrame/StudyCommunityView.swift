//
//  StudyCommunityView.swift
//  NewWustHelper
//
//  Created by wust_lh on 2026/01/14.
//

import SwiftUI

// MARK: - Models

struct CourseModel: Identifiable {
    let id = UUID()
    let name: String
    let code: String
    let teacher: String
    let difficulty: Double // 1-5
    let passRate: String
    let description: String
    let icon: String
    let color: Color
}

struct StudyPost: Identifiable {
    let id = UUID()
    let title: String
    let content: String
    let author: String
    let authorAvatar: String
    let date: String
    let likes: Int
    let comments: Int
    let tags: [String]
    let type: PostType
}

enum PostType: String {
    case experience = "经验"
    case question = "提问"
    case resource = "资料"
    case strategy = "攻略"
}

// MARK: - Data Manager

class StudySystemManager: ObservableObject {
    @Published var courses: [CourseModel] = [
        CourseModel(name: "高等数学(上)", code: "MATH101", teacher: "张老师", difficulty: 4.5, passRate: "85%", description: "理工科必修课，微积分基础。", icon: "function", color: .blue),
        CourseModel(name: "大学英语IV", code: "ENG104", teacher: "李老师", difficulty: 3.0, passRate: "95%", description: "四级考试强化训练。", icon: "textformat", color: .orange),
        CourseModel(name: "Java程序设计", code: "CS201", teacher: "王老师", difficulty: 4.0, passRate: "88%", description: "面向对象编程，包含Swing和多线程。", icon: "laptopcomputer", color: .purple),
        CourseModel(name: "线性代数", code: "MATH102", teacher: "赵老师", difficulty: 3.8, passRate: "90%", description: "矩阵运算与向量空间。", icon: "grid", color: .teal),
        CourseModel(name: "马克思主义原理", code: "POL101", teacher: "孙老师", difficulty: 2.0, passRate: "99%", description: "背就完事了。", icon: "book.closed", color: .red)
    ]
    
    @Published var recentPosts: [StudyPost] = [
        StudyPost(title: "高数期末复习重点整理(含手写笔记)", content: "整理了这一学期的重点题型，都在附件里了，大家自取。", author: "学霸君", authorAvatar: "person.circle.fill", date: "2小时前", likes: 234, comments: 56, tags: ["高数", "资料"], type: .resource),
        StudyPost(title: "计算机考研408怎么复习？", content: "本人大三，准备考本校，想问问专业课什么时候开始看比较好？", author: "迷茫学弟", authorAvatar: "person.circle", date: "5小时前", likes: 45, comments: 12, tags: ["考研", "计算机"], type: .question),
        StudyPost(title: "Java课设组队，缺个前端", content: "题目是图书管理系统，已有后端和文档，来个会Swing或者JavaFX的。", author: "代码搬运工", authorAvatar: "desktopcomputer", date: "1天前", likes: 12, comments: 4, tags: ["组队", "Java"], type: .question)
    ]
    
    // Mock Data for Postgraduate Zone
    @Published var postgraduatePosts: [StudyPost] = [
        StudyPost(title: "2026考研时间轴规划", content: "从现在开始到明年12月的详细复习计划表。", author: "研途漫漫", authorAvatar: "calendar", date: "3天前", likes: 1024, comments: 88, tags: ["规划", "考研"], type: .strategy),
        StudyPost(title: "本校计算机专硕报录比分析", content: "近三年数据统计，分数线有上涨趋势。", author: "数据帝", authorAvatar: "chart.bar", date: "1周前", likes: 560, comments: 45, tags: ["择校", "数据"], type: .resource)
    ]
    
    // Mock Data for Extracurricular
    @Published var extraPosts: [StudyPost] = [
        StudyPost(title: "吉他社招新啦！", content: "喜欢音乐的同学快来，零基础也可以。", author: "吉他社长", authorAvatar: "music.note", date: "昨天", likes: 88, comments: 20, tags: ["社团", "音乐"], type: .experience),
        StudyPost(title: "Python爬虫入门实战", content: "教你如何爬取学校教务处的课表数据。", author: "极客", authorAvatar: "ant.fill", date: "2天前", likes: 233, comments: 15, tags: ["编程", "爬虫"], type: .experience)
    ]
    
    func getExperience(for courseName: String) -> [StudyPost] {
        return [
            StudyPost(title: "\(courseName) 期末避坑指南", content: "这门课老师重点在第三章，平时分给的很高，但是期末一定要写满。", author: "过儿", authorAvatar: "person.fill", date: "1年前", likes: 88, comments: 10, tags: ["经验"], type: .experience),
            StudyPost(title: "\(courseName) 课堂笔记分享", content: "字迹潦草，凑合看吧。", author: "好学生", authorAvatar: "note.text", date: "2个月前", likes: 45, comments: 5, tags: ["笔记"], type: .resource)
        ]
    }
    
    func getQAPosts() -> [StudyPost] {
        return recentPosts.filter { $0.type == .question }
    }
    
    func addPost(_ post: StudyPost) {
        recentPosts.insert(post, at: 0)
    }
}

// MARK: - Main View (Study Hub)

struct StudyCommunityView: View {
    @StateObject private var manager = StudySystemManager()
    @Environment(\.presentationMode) var presentationMode
    @State private var searchText = ""
    @State private var showCreatePost = false
    
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Color(UIColor.systemGroupedBackground)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Custom Header
                HStack(spacing: 12) {
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        Image(systemName: "chevron.left")
                            .font(.title2)
                            .foregroundColor(.primary)
                    }
                    
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.gray)
                        TextField("搜索课程、资料、问题...", text: $searchText)
                    }
                    .padding(10)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(10)
                    
                    Button(action: { showCreatePost = true }) {
                        Image(systemName: "plus.circle.fill")
                            .font(.title2)
                            .foregroundColor(.blue)
                    }
                }
                .padding()
                .background(Color(UIColor.systemBackground))
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 5)
                .zIndex(10)
                
                ScrollView {
                    VStack(spacing: 24) {
                        
                        // 1. Function Grid
                        VStack(alignment: .leading, spacing: 16) {
                            Text("学习中心")
                                .font(.title3)
                                .fontWeight(.bold)
                                .padding(.horizontal)
                            
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                                NavigationLink(destination: QASquareView(manager: manager)) {
                                    StudySystemFeatureCard(title: "互助答疑", subtitle: "考研 · 课业 · 生活", icon: "questionmark.bubble.fill", color: .blue)
                                }
                                .buttonStyle(PlainButtonStyle())
                                
                                NavigationLink(destination: CourseWikiView(manager: manager)) {
                                    StudySystemFeatureCard(title: "课程攻略", subtitle: "经验 · 资料 · 评价", icon: "book.closed.fill", color: .orange)
                                }
                                .buttonStyle(PlainButtonStyle())
                                
                                NavigationLink(destination: PostgraduateView(manager: manager)) {
                                    StudySystemFeatureCard(title: "考研专区", subtitle: "上岸秘籍 · 择校", icon: "graduationcap.fill", color: .purple)
                                }
                                .buttonStyle(PlainButtonStyle())
                                
                                NavigationLink(destination: ExtraCurricularView(manager: manager)) {
                                    StudySystemFeatureCard(title: "课外兴趣", subtitle: "技能 · 爱好 · 考证", icon: "star.fill", color: .pink)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                            .padding(.horizontal)
                        }
                        .padding(.top, 10)
                        
                        // 2. Hot Courses
                        VStack(alignment: .leading, spacing: 12) {
                            HStack {
                                Text("热门课程")
                                    .font(.headline)
                                Spacer()
                                NavigationLink(destination: CourseWikiView(manager: manager)) {
                                    Text("全部")
                                        .font(.subheadline)
                                        .foregroundColor(.blue)
                                }
                            }
                            .padding(.horizontal)
                            
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 16) {
                                    ForEach(manager.courses) { course in
                                        NavigationLink(destination: CourseDetailView(course: course, manager: manager)) {
                                            CourseMiniCard(course: course)
                                        }
                                        .buttonStyle(PlainButtonStyle())
                                    }
                                }
                                .padding(.horizontal)
                            }
                        }
                        
                        // 3. Recent Feed
                        VStack(alignment: .leading, spacing: 16) {
                            Text("最新动态")
                                .font(.headline)
                                .padding(.horizontal)
                            
                            LazyVStack(spacing: 16) {
                                ForEach(manager.recentPosts) { post in
                                    StudySystemPostCard(post: post)
                                }
                            }
                            .padding(.horizontal)
                        }
                        
                        Spacer(minLength: 80)
                    }
                    .padding(.top, 16)
                }
            }
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showCreatePost) {
            StudyPostCreationView(manager: manager)
        }
    }
}

// MARK: - Sub-Feature: Postgraduate Zone

struct PostgraduateView: View {
    @ObservedObject var manager: StudySystemManager
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { presentationMode.wrappedValue.dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundColor(.primary)
                }
                Spacer()
                Text("考研专区")
                    .font(.headline)
                Spacer()
            }
            .padding()
            .background(Color(UIColor.systemBackground))
            
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Banner
                    RoundedRectangle(cornerRadius: 12)
                        .fill(LinearGradient(gradient: Gradient(colors: [.purple, .blue]), startPoint: .leading, endPoint: .trailing))
                        .frame(height: 120)
                        .overlay(
                            VStack(alignment: .leading) {
                                Text("一战成硕")
                                    .font(.title)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                Text("坚持就是胜利，加油考研人！")
                                    .foregroundColor(.white.opacity(0.9))
                            }
                            .padding()
                            , alignment: .bottomLeading
                        )
                        .padding(.horizontal)
                    
                    Text("精选攻略")
                        .font(.headline)
                        .padding(.horizontal)
                    
                    LazyVStack(spacing: 16) {
                        ForEach(manager.postgraduatePosts) { post in
                            StudySystemPostCard(post: post)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.top)
            }
        }
        .navigationBarHidden(true)
        .background(Color(UIColor.systemGroupedBackground))
    }
}

// MARK: - Sub-Feature: Extra Curricular

struct ExtraCurricularView: View {
    @ObservedObject var manager: StudySystemManager
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: { presentationMode.wrappedValue.dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundColor(.primary)
                }
                Spacer()
                Text("课外兴趣")
                    .font(.headline)
                Spacer()
            }
            .padding()
            .background(Color(UIColor.systemBackground))
            
            ScrollView {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                    ForEach(manager.extraPosts) { post in
                        VStack(alignment: .leading) {
                            HStack {
                                Image(systemName: post.authorAvatar)
                                    .foregroundColor(.pink)
                                Spacer()
                                Text(post.type.rawValue)
                                    .font(.caption2)
                                    .padding(4)
                                    .background(Color.pink.opacity(0.1))
                                    .cornerRadius(4)
                            }
                            Text(post.title)
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .lineLimit(2)
                                .padding(.vertical, 4)
                            Text(post.content)
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .lineLimit(3)
                        }
                        .padding()
                        .background(Color(UIColor.systemBackground))
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.05), radius: 3, x: 0, y: 2)
                    }
                }
                .padding()
            }
        }
        .navigationBarHidden(true)
        .background(Color(UIColor.systemGroupedBackground))
    }
}

// MARK: - Sub-Feature: Course Wiki View

struct CourseWikiView: View {
    @ObservedObject var manager: StudySystemManager
    @Environment(\.presentationMode) var presentationMode
    @State private var searchText = ""
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: { presentationMode.wrappedValue.dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundColor(.primary)
                }
                Spacer()
                Text("课程攻略库")
                    .font(.headline)
                Spacer()
            }
            .padding()
            .background(Color(UIColor.systemBackground))
            
            HStack {
                Image(systemName: "magnifyingglass").foregroundColor(.gray)
                TextField("搜索课程名称...", text: $searchText)
            }
            .padding(10)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(10)
            .padding()
            
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(manager.courses) { course in
                        NavigationLink(destination: CourseDetailView(course: course, manager: manager)) {
                            CourseListRow(course: course)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                .padding()
            }
        }
        .navigationBarHidden(true)
        .background(Color(UIColor.systemGroupedBackground))
    }
}

// MARK: - Sub-Feature: Course Detail View

struct CourseDetailView: View {
    let course: CourseModel
    @ObservedObject var manager: StudySystemManager
    @Environment(\.presentationMode) var presentationMode
    @State private var selectedTab = 0
    
    var body: some View {
        VStack(spacing: 0) {
            ZStack(alignment: .topLeading) {
                Rectangle()
                    .fill(LinearGradient(gradient: Gradient(colors: [course.color.opacity(0.8), course.color]), startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(height: 180)
                    .ignoresSafeArea()
                
                VStack(alignment: .leading) {
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        Image(systemName: "arrow.left")
                            .foregroundColor(.white)
                            .padding()
                            .background(Color.black.opacity(0.2))
                            .clipShape(Circle())
                    }
                    .padding(.top, 40)
                    .padding(.leading, 16)
                    
                    Spacer()
                    
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: course.icon)
                            Text(course.name)
                                .font(.title)
                                .fontWeight(.bold)
                        }
                        Text("\(course.code) | 授课教师: \(course.teacher)")
                            .font(.subheadline)
                            .opacity(0.9)
                        
                        HStack(spacing: 16) {
                            Label("难度: \(String(format: "%.1f", course.difficulty))", systemImage: "chart.bar.fill")
                            Label("通过率: \(course.passRate)", systemImage: "checkmark.shield.fill")
                        }
                        .font(.caption)
                        .padding(.top, 4)
                    }
                    .foregroundColor(.white)
                    .padding(20)
                }
            }
            .frame(height: 200)
            
            HStack {
                TabButton(title: "经验分享", isSelected: selectedTab == 0) { selectedTab = 0 }
                TabButton(title: "课程资料", isSelected: selectedTab == 1) { selectedTab = 1 }
                TabButton(title: "讨论答疑", isSelected: selectedTab == 2) { selectedTab = 2 }
            }
            .padding(.top, 8)
            .background(Color(UIColor.systemBackground))
            
            ScrollView {
                VStack(spacing: 16) {
                    if selectedTab == 0 {
                        ForEach(manager.getExperience(for: course.name)) { post in
                            StudySystemPostCard(post: post)
                        }
                    } else if selectedTab == 1 {
                        ResourceRow(title: "\(course.name) 课件PPT.zip", size: "120MB", downloads: 1024)
                        ResourceRow(title: "2024期末复习题.pdf", size: "5.2MB", downloads: 550)
                        ResourceRow(title: "历年真题合集.rar", size: "200MB", downloads: 300)
                    } else {
                        VStack(spacing: 20) {
                            Button(action: {}) {
                                Text("发起提问")
                                    .font(.headline)
                                    .foregroundColor(.white)
                                    .padding()
                                    .frame(maxWidth: .infinity)
                                    .background(Color.blue)
                                    .cornerRadius(12)
                            }
                            Text("暂无提问，快来抢沙发！")
                                .foregroundColor(.gray)
                        }
                    }
                }
                .padding()
            }
            .background(Color(UIColor.systemGroupedBackground))
        }
        .navigationBarHidden(true)
        .edgesIgnoringSafeArea(.top)
    }
}

// MARK: - Sub-Feature: Q&A Square

struct QASquareView: View {
    @ObservedObject var manager: StudySystemManager
    @Environment(\.presentationMode) var presentationMode
    @State private var selectedCategory = "全部"
    let categories = ["全部", "考研", "课内", "平时", "课外"]
    
    var filteredPosts: [StudyPost] {
        let allQA = manager.recentPosts.filter { $0.type == .question }
        if selectedCategory == "全部" {
            return allQA
        }
        return allQA.filter { $0.tags.contains(selectedCategory) }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: { presentationMode.wrappedValue.dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundColor(.primary)
                }
                Spacer()
                Text("互助答疑广场")
                    .font(.headline)
                Spacer()
                Button(action: { }) {
                    Image(systemName: "plus.bubble")
                        .font(.title2)
                        .foregroundColor(.blue)
                }
            }
            .padding()
            .background(Color(UIColor.systemBackground))
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(categories, id: \.self) { cat in
                        Button(action: { selectedCategory = cat }) {
                            Text(cat)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(selectedCategory == cat ? Color.blue : Color(UIColor.secondarySystemBackground))
                                .foregroundColor(selectedCategory == cat ? .white : .primary)
                                .cornerRadius(20)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }
            .background(Color(UIColor.systemBackground))
            
            ScrollView {
                LazyVStack(spacing: 16) {
                    ForEach(filteredPosts) { post in
                        StudySystemPostCard(post: post)
                    }
                    if filteredPosts.isEmpty {
                        Text("暂无相关问题")
                            .foregroundColor(.gray)
                            .padding(.top, 50)
                    }
                }
                .padding()
            }
            .background(Color(UIColor.systemGroupedBackground))
        }
        .navigationBarHidden(true)
    }
}

// MARK: - Post Creation View

struct StudyPostCreationView: View {
    @ObservedObject var manager: StudySystemManager
    @Environment(\.presentationMode) var presentationMode
    @State private var title = ""
    @State private var content = ""
    @State private var selectedType: PostType = .question
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("类型")) {
                    Picker("类型", selection: $selectedType) {
                        Text("提问").tag(PostType.question)
                        Text("经验").tag(PostType.experience)
                    }
                    .pickerStyle(SegmentedPickerStyle())
                }
                
                Section(header: Text("内容")) {
                    TextField("标题", text: $title)
                    TextEditor(text: $content)
                        .frame(height: 150)
                        .overlay(
                            Group {
                                if content.isEmpty {
                                    Text("详细描述...")
                                        .foregroundColor(.gray)
                                        .padding(.top, 8)
                                        .padding(.leading, 4)
                                }
                            }, alignment: .topLeading
                        )
                }
                
                Section {
                    Button(action: {
                        let newPost = StudyPost(title: title, content: content, author: "我", authorAvatar: "person.fill", date: "刚刚", likes: 0, comments: 0, tags: ["新帖"], type: selectedType)
                        manager.addPost(newPost)
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("发布")
                            .frame(maxWidth: .infinity)
                            .foregroundColor(.blue)
                    }
                    .disabled(title.isEmpty || content.isEmpty)
                }
            }
            .navigationTitle("发布帖子")
            .navigationBarItems(leading: Button("取消") { presentationMode.wrappedValue.dismiss() })
        }
    }
}

// MARK: - Components

struct StudySystemFeatureCard: View {
    let title: String
    let subtitle: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.headline)
                        .foregroundColor(.primary)
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(color)
                    .padding(8)
                    .background(color.opacity(0.1))
                    .clipShape(Circle())
            }
        }
        .padding()
        .background(Color(UIColor.systemBackground))
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}

struct CourseMiniCard: View {
    let course: CourseModel
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Image(systemName: course.icon)
                    .foregroundColor(.white)
                    .padding(8)
                    .background(course.color)
                    .cornerRadius(8)
                Spacer()
                Text(String(format: "%.1f", course.difficulty))
                    .font(.caption)
                    .fontWeight(.bold)
                    .padding(4)
                    .background(Color.yellow.opacity(0.2))
                    .foregroundColor(.orange)
                    .cornerRadius(4)
            }
            
            Spacer()
            
            Text(course.name)
                .font(.headline)
                .foregroundColor(.primary)
                .lineLimit(1)
            Text(course.teacher)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(12)
        .frame(width: 140, height: 110)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.05), radius: 3, x: 0, y: 2)
    }
}

struct CourseListRow: View {
    let course: CourseModel
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: course.icon)
                .font(.title2)
                .foregroundColor(.white)
                .frame(width: 50, height: 50)
                .background(course.color)
                .cornerRadius(12)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(course.name)
                    .font(.headline)
                    .foregroundColor(.primary)
                Text("代码: \(course.code) | 教师: \(course.teacher)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.03), radius: 2, x: 0, y: 1)
    }
}

struct StudySystemPostCard: View {
    let post: StudyPost
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                // Label
                Text(post.type.rawValue)
                    .font(.caption2)
                    .fontWeight(.bold)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 3)
                    .background(
                        post.type == .resource ? Color.green.opacity(0.1) :
                        (post.type == .question ? Color.red.opacity(0.1) :
                        (post.type == .strategy ? Color.purple.opacity(0.1) : Color.blue.opacity(0.1)))
                    )
                    .foregroundColor(
                        post.type == .resource ? .green :
                        (post.type == .question ? .red :
                        (post.type == .strategy ? .purple : .blue))
                    )
                    .cornerRadius(4)
                
                Text(post.title)
                    .font(.headline)
                    .lineLimit(1)
            }
            
            Text(post.content)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .lineLimit(2)
            
            HStack {
                ForEach(post.tags, id: \.self) { tag in
                    Text("#\(tag)")
                        .font(.caption)
                        .foregroundColor(.blue)
                }
                
                Spacer()
                
                HStack(spacing: 12) {
                    Label("\(post.comments)", systemImage: "bubble.left").font(.caption)
                    Label("\(post.likes)", systemImage: "hand.thumbsup").font(.caption)
                }
                .foregroundColor(.gray)
            }
        }
        .padding()
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.03), radius: 2, x: 0, y: 1)
    }
}

struct ResourceRow: View {
    let title: String
    let size: String
    let downloads: Int
    
    var body: some View {
        HStack {
            Image(systemName: "doc.text.fill")
                .font(.title2)
                .foregroundColor(.blue)
                .padding(10)
                .background(Color.blue.opacity(0.1))
                .cornerRadius(10)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                HStack {
                    Text(size)
                    Text("•")
                    Text("\(downloads) 次下载")
                }
                .font(.caption)
                .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Button(action: {}) {
                Image(systemName: "arrow.down.circle.fill")
                    .font(.title2)
                    .foregroundColor(.blue)
            }
        }
        .padding()
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
    }
}

struct TabButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(isSelected ? .blue : .gray)
                
                Rectangle()
                    .fill(isSelected ? Color.blue : Color.clear)
                    .frame(height: 3)
            }
            .frame(maxWidth: .infinity)
        }
    }
}