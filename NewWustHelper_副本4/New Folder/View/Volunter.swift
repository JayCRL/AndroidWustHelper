//
//  Volunter.swift
//  study_test
//
//  Created by wust_lh on 2025/7/8.
//

import SwiftUI
struct Volunter: View {
    @AppStorage("studentInfo") private var studentInfoJson: String = ""
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
    @State var GScode: String = ""
    @State var rzcode: [String] = ["", "", "", "", "", ""]
    enum CodeField: Int, CaseIterable {
        case field0, field1, field2, field3, field4, field5
    }

    @FocusState private var focusedField: CodeField?
    @State private var showContent = false

    var isCodeComplete: Bool {
        !rzcode.contains(where: { $0.isEmpty })
    }

    var body: some View {
        NavigationStack {
            ZStack {
                if showContent {
                    content
                        .transition(.opacity)
                        .animation(.easeIn(duration: 0.3), value: showContent)
                }
            }
            .onAppear {
                // 延迟呈现页面内容，避免角落滑入动画
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    showContent = true
                    // 再延迟设置焦点，避免焦点动画影响布局
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                        withAnimation(.none) {
                            focusedField = .field0
                        }
                    }
                }
            }
        }
    }

    var content: some View {
        ZStack(alignment: .center) {
            AngularGradient(
                gradient: Gradient(colors: [Color.blue, Color("pi")]),
                center: .bottomTrailing,
                angle: .degrees(45.0)
            ).edgesIgnoringSafeArea(.top)

            ScrollView {
                VStack(alignment: .center) {
                    HStack {
                        Spacer()
                        Image(systemName: "questionmark.circle")
                            .resizable()
                            .frame(width: 25, height: 25)
                            .padding(.top, 10)
                            .padding(.trailing, 10)
                            .foregroundColor(.white)
                    }

                    VStack {
                        Text(studentInfo.name)
                            .font(.largeTitle)
                            .foregroundColor(.white)
                            .bold()
                        Text(studentInfo.studentNumber)
                            .font(.headline)
                            .foregroundColor(.white)
                            .bold()
                    }

                    VStack(spacing: 10) {
                        Text(studentInfo.college)
                            .foregroundColor(.white)
                            .font(.headline)
                    }

                    RoundedRectangle(cornerRadius: 25)
                        .fill(Color.white)
                        .frame(width: 370, height: 250)
                        .shadow(color: Color.blue.opacity(0.3), radius: 10)
                        .overlay {
                            VStack {
                                HStack(spacing: 8) {
                                    ForEach(0..<6, id: \.self) { index in
                                        TextField("", text: $rzcode[index])
                                            .focused($focusedField, equals: CodeField(rawValue: index))
                                            .multilineTextAlignment(.center)
                                            .frame(width: 50, height: 60)
                                            .font(.title2)
                                            .keyboardType(.asciiCapable)
                                            .tint(.black)
                                            .onChange(of: rzcode[index]) { newValue in
                                                if newValue.count > 1 {
                                                    rzcode[index] = String(newValue.prefix(1))
                                                }
                                                if newValue.count == 1, index < 5 {
                                                    focusedField = CodeField(rawValue: index + 1)
                                                }
                                            }
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 10)
                                                    .stroke(Color.gray, lineWidth: 1)
                                            )
                                    }
                                }
                                Text("在上方输入正确的认证码，即可提交工时")
                                    .foregroundColor(.gray)
                                    .padding(.top, 10)

                                Button {
                                    // 提交逻辑
                                } label: {
                                    RoundedRectangle(cornerRadius: 10)
                                        .fill(isCodeComplete ? Color.blue.opacity(0.6) : Color.blue.opacity(0.2))
                                        .frame(width: 300, height: 50)
                                        .overlay(
                                            Text("提交工时")
                                                .foregroundColor(.white)
                                                .font(.title)
                                        )
                                        .padding(.top, 10)
                                }
                                .disabled(!isCodeComplete)
                            }
                        }

                    // 底部导航按钮
                    HStack {
                        NavigationLink(destination: Text("个人信息页面")) {
                            VStack {
                                Image(systemName: "pencil.and.list.clipboard")
                                    .resizable()
                                    .renderingMode(.template)
                                    .aspectRatio(contentMode: .fit)
                                    .frame(width: 40, height: 40)
                                    .foregroundColor(.white)
                                Text("个人信息")
                                    .font(Font.custom("PingFang SC", size: 16))
                                    .foregroundColor(.white)
                            }
                            .frame(width: 80)
                            .padding(.trailing, 10)
                        }

                        Spacer()

                        NavigationLink(destination: Text("所有活动页面")) {
                            VStack {
                                Image(systemName: "checkmark.seal")
                                    .resizable()
                                    .renderingMode(.template)
                                    .aspectRatio(contentMode: .fit)
                                    .frame(width: 40, height: 40)
                                    .foregroundColor(.white)
                                Text("所有活动")
                                    .font(.callout)
                                    .foregroundColor(.white)
                            }
                            .frame(width: 80)
                            .padding(.trailing, 20)
                        }

                        Spacer()

                        NavigationLink(destination: Text("举报页面")) {
                            VStack {
                                Image(systemName: "exclamationmark.triangle")
                                    .resizable()
                                    .renderingMode(.template)
                                    .aspectRatio(contentMode: .fit)
                                    .frame(width: 40, height: 40)
                                    .foregroundColor(.white)
                                Text("举报")
                                    .font(.callout)
                                    .foregroundColor(.white)
                            }
                            .frame(width: 80)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.top, 15)
                    .padding(.trailing, 60)
                    .padding(.leading, 50)

                    // 活动列表
                    VStack(spacing: 10) {
                        activityCard(verified: true)
                        activityCard(verified: false)
                    }
                    .padding(.top, 10)
                }
            }
        }
    }

    // 活动卡片封装
    func activityCard(verified: Bool) -> some View {
        HStack {
            RoundedRectangle(cornerRadius: 10)
                .fill(Color.white)
                .frame(width: 245, height: 100)
                .overlay(
                    VStack(alignment: .leading) {
                        Text("第二次志愿活动")
                            .foregroundColor(Color.blue.opacity(0.5))
                            .font(.title)

                        HStack {
                            VStack(alignment: .leading) {
                                Text("2025-03-30 张雅思")
                                    .foregroundColor(.gray)
                                    .font(.caption)
                                Text("李四光工程训练协会")
                                    .foregroundColor(.gray)
                                    .font(.caption)
                            }
                            Text("5h")
                                .font(.largeTitle)
                                .foregroundColor(Color.blue.opacity(0.5))
                                .padding(.leading, 60)
                        }
                    }
                )

            VStack {
                Circle()
                    .fill(verified ? Color.green : Color.orange)
                    .frame(width: 50, height: 50)
                Text(verified ? "已认证" : "未认证")
                    .foregroundColor(.white)
            }
            .padding(.leading, 40)
        }
    }
}
#Preview {
    Volunter()
}
