//
//  AIAssistantView.swift
//  NewWustHelper
//
//  Created by wust_lh on 2026/01/14.
//

import SwiftUI

// MARK: - Models

struct ChatMessage: Identifiable, Equatable {
    let id = UUID()
    let text: String
    let isUser: Bool
    let date: Date
}

// MARK: - View

struct AIAssistantView: View {
    @State private var messages: [ChatMessage] = [
        ChatMessage(text: "你好！我是你的AI学习助手。你可以问我关于课程、考研、或者校园生活的问题。", isUser: false, date: Date())
    ]
    @State private var inputText: String = ""
    @State private var showUploadSheet = false
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack(alignment: .bottom) {
            Color(UIColor.systemGroupedBackground)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                HStack {
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        Image(systemName: "chevron.left")
                            .font(.title2)
                            .foregroundColor(.primary)
                    }
                    Spacer()
                    Text("AI 学习助手")
                        .font(.headline)
                    Spacer()
                    Button(action: { showUploadSheet = true }) {
                        Image(systemName: "arrow.up.doc")
                            .font(.title2)
                            .foregroundColor(.blue)
                    }
                }
                .padding()
                .background(Color(UIColor.systemBackground))
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 5)
                .zIndex(1)
                
                // Chat List
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(messages) { message in
                                MessageBubble(message: message)
                                    .id(message.id)
                            }
                        }
                        .padding()
                        .padding(.bottom, 60) // Space for input bar
                    }
                    .onChange(of: messages) { _ in
                        if let lastId = messages.last?.id {
                            withAnimation {
                                proxy.scrollTo(lastId, anchor: .bottom)
                            }
                        }
                    }
                }
            }
            
            // Input Bar
            HStack(spacing: 12) {
                TextField("输入你的问题...", text: $inputText)
                    .padding(10)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(20)
                
                Button(action: sendMessage) {
                    Image(systemName: "paperplane.fill")
                        .font(.title2)
                        .foregroundColor(inputText.isEmpty ? .gray : .blue)
                        .rotationEffect(.degrees(45))
                }
                .disabled(inputText.isEmpty)
            }
            .padding()
            .background(Color(UIColor.systemBackground))
            .overlay(Divider(), alignment: .top)
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showUploadSheet) {
            UploadCorpusView()
        }
    }
    
    private func sendMessage() {
        guard !inputText.isEmpty else { return }
        
        let userMsg = ChatMessage(text: inputText, isUser: true, date: Date())
        messages.append(userMsg)
        
        let query = inputText
        inputText = ""
        
        // Mock AI Response
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            let aiResponse = generateMockResponse(for: query)
            let aiMsg = ChatMessage(text: aiResponse, isUser: false, date: Date())
            withAnimation {
                messages.append(aiMsg)
            }
        }
    }
    
    private func generateMockResponse(for query: String) -> String {
        if query.contains("考研") {
            return "关于考研，建议你先确定目标院校和专业。对于计算机专业，数学和专业课是拉分项。我可以为你提供历年真题的分析，需要吗？"
        } else if query.contains("高数") || query.contains("数学") {
            return "高等数学确实比较难。你可以尝试多做习题，特别是极限和微积分部分。需要我为你解释某个具体的定理吗？"
        } else if query.contains("食堂") {
            return "南苑食堂二楼的拌面和北苑的小火锅都很受欢迎哦！"
        } else {
            return "这个问题很有趣！作为一个AI助手，我正在不断学习中。你可以尝试上传相关资料到语料库，让我变得更聪明。"
        }
    }
}

struct MessageBubble: View {
    let message: ChatMessage
    
    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            if !message.isUser {
                Image(systemName: "sparkles")
                    .resizable()
                    .frame(width: 24, height: 24)
                    .padding(6)
                    .background(Color.blue.opacity(0.1))
                    .clipShape(Circle())
                    .foregroundColor(.blue)
            } else {
                Spacer()
            }
            
            Text(message.text)
                .padding(12)
                .background(message.isUser ? Color.blue : Color(UIColor.secondarySystemBackground))
                .foregroundColor(message.isUser ? .white : .primary)
                .cornerRadius(16)
                .frame(maxWidth: UIScreen.main.bounds.width * 0.75, alignment: message.isUser ? .trailing : .leading)
            
            if message.isUser {
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .frame(width: 24, height: 24)
                    .foregroundColor(.gray)
            } else {
                Spacer()
            }
        }
    }
}

// MARK: - Upload View

struct UploadCorpusView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var textContent = ""
    @State private var isUploading = false
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("上传资料")) {
                    TextEditor(text: $textContent)
                        .frame(height: 200)
                        .overlay(
                            Group {
                                if textContent.isEmpty {
                                    Text("粘贴文本或输入资料内容，帮助AI更好地回答问题...")
                                        .foregroundColor(.gray)
                                        .padding(.top, 8)
                                        .padding(.leading, 4)
                                }
                            }, alignment: .topLeading
                        )
                }
                
                Section {
                    Button(action: {
                        isUploading = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            isUploading = false
                            presentationMode.wrappedValue.dismiss()
                        }
                    }) {
                        HStack {
                            Spacer()
                            if isUploading {
                                ProgressView()
                            } else {
                                Text("提交到语料库")
                            }
                            Spacer()
                        }
                    }
                    .disabled(textContent.isEmpty || isUploading)
                }
            }
            .navigationTitle("补充知识库")
            .navigationBarItems(leading: Button("取消") { presentationMode.wrappedValue.dismiss() })
        }
    }
}
