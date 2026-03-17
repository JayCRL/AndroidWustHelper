//
//  CampusCat.swift
//  NewWustHelper
//
//  Created by wust_lh on 2026/01/14.
//

import SwiftUI

// MARK: - Models

enum CatTag: String, CaseIterable, Identifiable, Codable {
    case all = "全部"
    case stray = "流浪"
    case adopt = "领养"
    case daily = "日常"
    case help = "求助"
    case showOff = "晒猫"
    
    var id: String { rawValue }
    
    var color: Color {
        switch self {
        case .all: return .gray
        case .stray: return .orange
        case .adopt: return .green
        case .daily: return .blue
        case .help: return .red
        case .showOff: return .purple
        }
    }
}

struct CatPost: Identifiable, Codable {
    let id: UUID
    var title: String
    var content: String
    var images: [String] // URLs or asset names
    var authorName: String
    var authorAvatar: String
    var tags: [CatTag]
    var likes: Int
    var commentsCount: Int
    var createDate: Date
    
    // Helper for formatting date
    var timeAgo: String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: createDate, relativeTo: Date())
    }
}

// MARK: - Mock Data Manager

class CatDataManager: ObservableObject {
    @Published var posts: [CatPost] = [
        CatPost(id: UUID(), title: "图书馆门口的小花猫", content: "今天在图书馆门口偶遇这只小可爱，一点都不怕人，还蹭我的腿！有没有人知道它的名字呀？给它买了根火腿肠吃得好香。", images: ["cat_placeholder_1", "cat_placeholder_2"], authorName: "爱猫的学姐", authorAvatar: "person.crop.circle.fill", tags: [.stray, .daily], likes: 128, commentsCount: 32, createDate: Date().addingTimeInterval(-3600)),
        CatPost(id: UUID(), title: "紧急求助！", content: "在南苑食堂后面发现一只受伤的小猫，腿好像断了，有没有懂救助的同学帮忙看看？我已经联系了校医院但还没回复。", images: ["cat_placeholder_3"], authorName: "热心同学", authorAvatar: "person.crop.circle.fill", tags: [.help, .stray], likes: 45, commentsCount: 12, createDate: Date().addingTimeInterval(-86400)),
        CatPost(id: UUID(), title: "求领养：三花妹妹", content: "室友猫毛过敏实在养不了了，找个好心人领养。三花妹妹，3个月大，已驱虫，未绝育。性格超级好，粘人精。", images: ["cat_placeholder_4"], authorName: "找铲屎官", authorAvatar: "person.crop.circle.fill", tags: [.adopt], likes: 89, commentsCount: 56, createDate: Date().addingTimeInterval(-172800)),
        CatPost(id: UUID(), title: "今日份的快乐", content: "看这睡姿，也是没谁了哈哈哈。", images: ["cat_placeholder_5"], authorName: "橘猫大队长", authorAvatar: "person.crop.circle.fill", tags: [.showOff, .daily], likes: 233, commentsCount: 15, createDate: Date().addingTimeInterval(-300)),
        CatPost(id: UUID(), title: "关于校园流浪猫绝育计划", content: "我们将于本周末开展新一轮的TNR行动，欢迎大家报名志愿者！", images: [], authorName: "动保协会", authorAvatar: "shield.fill", tags: [.stray, .help], likes: 567, commentsCount: 88, createDate: Date().addingTimeInterval(-500000))
    ]
    
    func addPost(_ post: CatPost) {
        posts.insert(post, at: 0)
    }
}

// MARK: - Compatibility Wrapper
struct Group_4: View {
    var body: some View {
        CampusCatView()
    }
}

// MARK: - Main View

struct CampusCatView: View {
    @StateObject private var dataManager = CatDataManager()
    @State private var selectedTag: CatTag = .all
    @State private var showUploadSheet = false
    @State private var searchText = ""
    @Environment(\.presentationMode) var presentationMode
    
    var filteredPosts: [CatPost] {
        var result = dataManager.posts
        
        // Filter by Tag
        if selectedTag != .all {
            result = result.filter { $0.tags.contains(selectedTag) }
        }
        
        // Filter by Search
        if !searchText.isEmpty {
            result = result.filter {
                $0.title.localizedCaseInsensitiveContains(searchText) ||
                $0.content.localizedCaseInsensitiveContains(searchText)
            }
        }
        
        return result
    }
    
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Color(UIColor.systemGroupedBackground)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header Area
                VStack(spacing: 12) {
                    // Custom Search Bar
                    HStack(spacing: 12) {
                        Button(action: { presentationMode.wrappedValue.dismiss() }) {
                            Image(systemName: "chevron.left")
                                .font(.title2)
                                .foregroundColor(.primary)
                        }
                        
                        HStack {
                            Image(systemName: "magnifyingglass")
                                .foregroundColor(.gray)
                            TextField("搜索喵星人...", text: $searchText)
                        }
                        .padding(10)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(10)
                        
                        Button(action: { showUploadSheet = true }) {
                            Image(systemName: "plus.circle.fill")
                                .font(.title2)
                                .foregroundColor(.blue)
                        }
                    }
                    .padding(.horizontal)
                    
                    // Tag Filter ScrollView
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(CatTag.allCases) { tag in
                                TagButton(tag: tag, isSelected: selectedTag == tag) {
                                    withAnimation {
                                        selectedTag = tag
                                    }
                                }
                            }
                        }
                        .padding(.horizontal)
                        .padding(.bottom, 8)
                    }
                }
                .background(Color(UIColor.systemBackground))
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 5)
                .zIndex(1)
                
                // Main Content Grid
                ScrollView {
                    LazyVGrid(columns: [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)], spacing: 12) {
                        ForEach(filteredPosts) { post in
                            NavigationLink(destination: CatDetailView(post: post)) {
                                CatPostCard(post: post)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding(12)
                    .padding(.bottom, 80) // Space for FAB if we had one, or tab bar
                }
            }
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showUploadSheet) {
            CatUploadView(dataManager: dataManager)
        }
    }
}

// MARK: - Subviews

struct TagButton: View {
    let tag: CatTag
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(tag.rawValue)
                .font(.system(size: 14, weight: isSelected ? .semibold : .medium))
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? tag.color.opacity(0.1) : Color(UIColor.secondarySystemBackground))
                .foregroundColor(isSelected ? tag.color : .gray)
                .cornerRadius(20)
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(isSelected ? tag.color : Color.clear, lineWidth: 1)
                )
        }
    }
}

struct CatPostCard: View {
    let post: CatPost
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Image Area
            GeometryReader { geo in
                ZStack {
                    if let firstImage = post.images.first, !firstImage.isEmpty {
                        // Placeholder for actual image loading logic
                        Color.gray.opacity(0.2)
                        Image(systemName: "photo") // Fallback
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 40)
                            .foregroundColor(.gray)
                    } else {
                        Rectangle()
                            .fill(LinearGradient(gradient: Gradient(colors: [Color.blue.opacity(0.1), Color.purple.opacity(0.1)]), startPoint: .topLeading, endPoint: .bottomTrailing))
                        
                        Image(systemName: "pawprint.fill")
                            .font(.largeTitle)
                            .foregroundColor(.white.opacity(0.5))
                    }
                    
                    if post.images.count > 1 {
                        VStack {
                            Spacer()
                            HStack {
                                Spacer()
                                Image(systemName: "square.fill.on.square.fill")
                                    .font(.caption)
                                    .foregroundColor(.white)
                                    .padding(4)
                                    .background(Color.black.opacity(0.5))
                                    .cornerRadius(4)
                                    .padding(4)
                            }
                        }
                    }
                }
            }
            .aspectRatio(1, contentMode: .fit)
            .clipped()
            
            // Content Area
            VStack(alignment: .leading, spacing: 8) {
                Text(post.title)
                    .font(.system(size: 15, weight: .bold))
                    .lineLimit(2)
                    .foregroundColor(.primary)
                
                HStack {
                    if let firstTag = post.tags.first {
                        Text(firstTag.rawValue)
                            .font(.system(size: 10))
                            .foregroundColor(firstTag.color)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(firstTag.color.opacity(0.1))
                            .cornerRadius(4)
                    }
                    Spacer()
                }
                
                // Author & Stats
                HStack(spacing: 8) {
                    Image(systemName: post.authorAvatar)
                        .resizable()
                        .frame(width: 16, height: 16)
                        .foregroundColor(.gray)
                        .clipShape(Circle())
                    
                    Text(post.authorName)
                        .font(.system(size: 11))
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                    
                    Spacer()
                    
                    HStack(spacing: 2) {
                        Image(systemName: "heart")
                            .font(.system(size: 10))
                        Text("\(post.likes)")
                            .font(.system(size: 10))
                    }
                    .foregroundColor(.gray)
                }
            }
            .padding(10)
        }
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
    }
}

// MARK: - Detail View

struct CatDetailView: View {
    let post: CatPost
    @State private var commentText = ""
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Large Image Gallery Header
                TabView {
                    if post.images.isEmpty {
                        Rectangle()
                            .fill(Color.gray.opacity(0.1))
                            .overlay(Image(systemName: "pawprint.fill").font(.largeTitle).foregroundColor(.gray))
                    } else {
                        ForEach(0..<max(1, post.images.count), id: \.self) { index in
                            Color.gray.opacity(0.2)
                                .overlay(
                                    Image(systemName: "photo")
                                        .font(.largeTitle)
                                        .foregroundColor(.secondary)
                                )
                        }
                    }
                }
                .tabViewStyle(PageTabViewStyle())
                .frame(height: 350)
                
                VStack(alignment: .leading, spacing: 16) {
                    // Title and Tags
                    VStack(alignment: .leading, spacing: 8) {
                        Text(post.title)
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        HStack {
                            ForEach(post.tags) { tag in
                                Text(tag.rawValue)
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                                    .background(tag.color)
                                    .cornerRadius(8)
                            }
                            Spacer()
                            Text(post.timeAgo)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    // Author Info
                    HStack {
                        Image(systemName: post.authorAvatar)
                            .resizable()
                            .frame(width: 40, height: 40)
                            .foregroundColor(.gray)
                            .background(Color.gray.opacity(0.1))
                            .clipShape(Circle())
                        
                        VStack(alignment: .leading) {
                            Text(post.authorName)
                                .font(.headline)
                            Text("校园认证用户")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Button(action: {}) {
                            Text("关注")
                                .font(.subheadline)
                                .fontWeight(.medium)
                                .foregroundColor(.blue)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 6)
                                .background(Color.blue.opacity(0.1))
                                .cornerRadius(16)
                        }
                    }
                    .padding(.vertical, 8)
                    
                    Divider()
                    
                    // Content
                    Text(post.content)
                        .font(.body)
                        .lineSpacing(6)
                        .foregroundColor(Color(UIColor.label))
                    
                    Divider()
                    
                    // Comments Section (Mock)
                    VStack(alignment: .leading, spacing: 16) {
                        Text("评论 (\(post.commentsCount))")
                            .font(.headline)
                        
                        ForEach(0..<3) { i in
                            HStack(alignment: .top) {
                                Image(systemName: "person.circle.fill")
                                    .foregroundColor(.gray)
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("路人同学\(i+1)")
                                        .font(.subheadline)
                                        .fontWeight(.medium)
                                        .foregroundColor(.secondary)
                                    Text("这只猫猫真的太可爱了！下次我也要去偶遇。")
                                        .font(.body)
                                        .fixedSize(horizontal: false, vertical: true)
                                }
                            }
                        }
                    }
                }
                .padding()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {}) {
                    Image(systemName: "square.and.arrow.up")
                }
            }
        }
        // Bottom Bar
        .safeAreaInset(edge: .bottom) {
            VStack(spacing: 0) {
                Divider()
                HStack {
                    HStack {
                        Image(systemName: "pencil")
                            .foregroundColor(.gray)
                        TextField("说点什么...", text: $commentText)
                    }
                    .padding(8)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(20)
                    
                    HStack(spacing: 20) {
                        Button(action: {}) {
                            VStack(spacing: 2) {
                                Image(systemName: "heart")
                                    .font(.title3)
                                Text("\(post.likes)")
                                    .font(.caption2)
                            }
                        }
                        Button(action: {}) {
                            VStack(spacing: 2) {
                                Image(systemName: "star")
                                    .font(.title3)
                                Text("收藏")
                                    .font(.caption2)
                            }
                        }
                    }
                    .foregroundColor(.gray)
                }
                .padding()
                .background(Color(UIColor.systemBackground))
            }
        }
    }
}

// MARK: - Upload View

struct CatUploadView: View {
    @ObservedObject var dataManager: CatDataManager
    @Environment(\.presentationMode) var presentationMode
    
    @State private var title = ""
    @State private var content = ""
    @State private var selectedTags: Set<CatTag> = []
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    HStack {
                        Button(action: {}) {
                            VStack {
                                Image(systemName: "camera.fill")
                                    .font(.title2)
                                    .foregroundColor(.blue)
                                Text("添加照片")
                                    .font(.caption)
                                    .foregroundColor(.blue)
                            }
                            .frame(width: 80, height: 80)
                            .background(Color.blue.opacity(0.1))
                            .cornerRadius(12)
                        }
                        
                        ScrollView(.horizontal) {
                            HStack {
                                // Image previews would go here
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }
                
                Section(header: Text("基本信息")) {
                    TextField("标题 (例如: 图书馆惊现神兽)", text: $title)
                    ZStack(alignment: .topLeading) {
                        if content.isEmpty {
                            Text("详细描述一下猫猫的情况...")
                                .foregroundColor(.gray)
                                .padding(.top, 8)
                                .padding(.leading, 4)
                        }
                        TextEditor(text: $content)
                            .frame(minHeight: 100)
                    }
                }
                
                Section(header: Text("标签 (可多选)")) {
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 80))], spacing: 10) {
                        ForEach(CatTag.allCases) { tag in
                            if tag != .all {
                                Button(action: {
                                    if selectedTags.contains(tag) {
                                        selectedTags.remove(tag)
                                    } else {
                                        selectedTags.insert(tag)
                                    }
                                }) {
                                    Text(tag.rawValue)
                                        .font(.system(size: 14))
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(selectedTags.contains(tag) ? tag.color : Color.gray.opacity(0.2))
                                        .foregroundColor(selectedTags.contains(tag) ? .white : .primary)
                                        .cornerRadius(16)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }
            }
            .navigationTitle("发布喵星人")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("发布") {
                        let newPost = CatPost(
                            id: UUID(),
                            title: title,
                            content: content,
                            images: [],
                            authorName: "我",
                            authorAvatar: "person.crop.circle.fill",
                            tags: Array(selectedTags).sorted(by: { $0.rawValue < $1.rawValue }),
                            likes: 0,
                            commentsCount: 0,
                            createDate: Date()
                        )
                        dataManager.addPost(newPost)
                        presentationMode.wrappedValue.dismiss()
                    }
                    .disabled(title.isEmpty || content.isEmpty || selectedTags.isEmpty)
                }
            }
        }
    }
}

// MARK: - Previews
struct CampusCat_Previews: PreviewProvider {
    static var previews: some View {
        Group_4()
    }
}
