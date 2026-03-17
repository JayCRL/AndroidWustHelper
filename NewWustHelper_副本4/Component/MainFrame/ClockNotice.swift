import SwiftUI
import WebKit
import Foundation
import Kanna

struct Exam {
    var semester: String
    var courseCode: String
    var courseName: String
    var week: String
    var dayOfWeek: String
    var session: String
    var time: String
    var seatNumber: String
    var SerialNumber: String // 唯一序号（统一+自主不重复）
    var campus: String
    var examType: String
    var location: String // 新增：单独存储场地信息（修复之前场地显示错误）
}

class HTMLParser {
    func parseHTML(content: String) -> [Exam] {
        var exams: [Exam] = []
        var maxSerialNumber = 0 // 记录统一安排考试的最大序号，用于自主申请考试序号递增
        
        do {
            if let doc = try? HTML(html: content, encoding: .utf8) {
                let tables = doc.css("table#dataList")
                guard tables.count > 0 else { return exams }
                
                // 第一步：解析统一安排考试（先获取最大序号）
                if let unifiedTable = tables.first {
                    let rows = unifiedTable.css("tr")
                    for row in rows {
                        let columns = row.css("td")
                        guard columns.count >= 12 else { continue }
                        
                        // 序号（第0列）
                        let serialNumStr = columns[0].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                        guard !serialNumStr.isEmpty, let serialNum = Int(serialNumStr) else { continue }
                        
                        // 更新最大序号
                        if serialNum > maxSerialNumber {
                            maxSerialNumber = serialNum
                        }
                        
                        var currentExam = Exam(
                            semester: columns[1].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            courseCode: columns[2].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            courseName: columns[3].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            week: columns[5].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            dayOfWeek: columns[6].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            session: columns[7].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            time: columns[8].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            seatNumber: columns[9].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            SerialNumber: serialNumStr,
                            campus: columns[11].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            examType: "统一安排考试",
                            location: columns[10].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "" // 场地从第11列（索引10）获取
                        )
                        
                        if !currentExam.courseName.isEmpty {
                            exams.append(currentExam)
                        }
                    }
                }
                
                // 第二步：解析自主申请考试（序号从 maxSerialNumber+1 开始）
                if tables.count >= 2 {
                    let selfApplyTable = tables[1]
                    let rows = selfApplyTable.css("tr")
                    var currentSerial = maxSerialNumber + 1 // 确保序号不重复
                    
                    for row in rows {
                        let columns = row.css("td")
                        guard columns.count >= 10 else { continue }
                        
                        var currentExam = Exam(
                            semester: columns[0].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            courseCode: columns[1].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            courseName: columns[2].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            week: columns[3].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            dayOfWeek: columns[4].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            session: columns[5].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            time: columns[6].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            seatNumber: columns[7].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            SerialNumber: "\(currentSerial)", // 唯一序号
                            campus: columns[9].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "",
                            examType: "自主申请考试",
                            location: columns[8].text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "" // 场地从第9列（索引8）获取
                        )
                        
                        currentSerial += 1 // 序号递增
                        
                        if !currentExam.courseName.isEmpty {
                            exams.append(currentExam)
                        }
                    }
                }
            }
        }
        return exams
    }
}

struct ClockNotice: View {
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @State private var exams: [Exam] = []
    @AppStorage("ExamPlan") private var previousHtmlContent: String = ""
    @Environment(\.dismiss) var dismiss: DismissAction
    @State private var showNewCourseNotification = false
    @AppStorage("authData") private var authData: String = ""
    @State private var isRefreshing = false
    @State private var ShowAlert: Bool = false
    @State private var ShowGraduated: Bool = false
    @State private var errorAlterMessage: String? = nil
    private let htmlParser = HTMLParser()
    @AppStorage("username") var username: String = ""
    @AppStorage("password") var password: String = ""
    @AppStorage("cookie") private var cookie: String = ""
    @State private var webPage: String = ""
    
   
    struct Response: Codable {
        var code: Int
        var message: String
    }
    struct CourseResponsePage: Codable {
        var code: Int
        var message: String
        var data: String
    }
    
    var body: some View {
        ZStack {
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 0.05, green: 0.15, blue: 0.3),
                    Color(red: 0.2, green: 0.05, blue: 0.25)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .edgesIgnoringSafeArea(.top)
            
            ScrollView {
                VStack(spacing: 24) {
                    headerView
                    contentView
                }
                .padding(.horizontal)
                .padding(.top, 24)
            }
            .refreshable {
                await refreshData()
            }
            
            if showNewCourseNotification {
                newExamNotification
            }
        }
        .alert("出错了😣", isPresented: $ShowAlert) {
            Button("知道了", role: .destructive) {
                ShowAlert = false
            }
        } message: {
            Text(errorAlterMessage ?? "未知错误")
        }
        .alert("很抱歉😣", isPresented: $ShowGraduated) {
            Button("知道了", role: .destructive) {
                ShowGraduated = false
                dismiss()
            }
        } message: {
            Text("研究生没有考试安排哦")
        }
        .onAppear {
            if Identify.chooseIdentify == Identify.Graduate {
                ShowGraduated.toggle()
            } else {
                self.isLoading = true
                let loginInstance = loginframe()
                loginInstance.undergraduateLoginRequest(username: username, password: password) { loginSuccess, loginMessage in
                    DispatchQueue.main.async {
                        if loginSuccess {
                            self.cookie = loginframe.cookie ?? ""
                            print("✅ ClockNotice获取到Cookie: \(self.cookie.prefix(50))...")
                            fetchHTMLContent()
                        } else {
                            self.errorAlterMessage = "用户信息失效"
                            self.isLoading = false
                            self.ShowAlert = true
                        }
                    }
                }
            }
        }
        .navigationTitle("考试安排")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("考试安排")
                    .font(.title2)
                    .foregroundColor(.white)
            }
        }
    }
    
    // MARK: - Subviews
    private var headerView: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "calendar.badge.clock")
                    .font(.title)
                    .foregroundColor(.white)
                
                Text("考试日程")
                    .font(.system(size: 28, weight: .bold, design: .rounded))
                    .foregroundColor(.white)
                
                Spacer()
                
                Button(action: {
                    fetchHTMLContent(forceReload: true)
                }) {
                    Image(systemName: "arrow.clockwise")
                        .font(.title3)
                        .padding(10)
                        .background(Circle().fill(Color.white.opacity(0.2)))
                }
            }
            
            Text("已找到 \(exams.count) 场考试安排")
                .font(.subheadline)
                .foregroundColor(Color.white.opacity(0.8))
        }
        .padding(.bottom, 8)
    }
    
    private var contentView: some View {
        Group {
            if isLoading {
                loadingIndicator
            } else if let error = errorMessage {
                errorView(error)
            } else if exams.isEmpty {
                emptyStateView
            } else {
                examCardsView
            }
        }
    }
    
    private var loadingIndicator: some View {
        VStack(spacing: 20) {
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                .scaleEffect(1.5)
            
            Text("正在获取考试安排...")
                .font(.headline)
                .foregroundColor(.white)
                .transition(.opacity)
        }
        .frame(maxWidth: .infinity, minHeight: 300)
    }
    
    private func errorView(_ message: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundColor(.yellow)
            
            Text("加载失败")
                .font(.title2.bold())
                .foregroundColor(.white)
            
            Text(message)
                .font(.body)
                .foregroundColor(Color.white.opacity(0.9))
                .multilineTextAlignment(.center)
            
            Button("重试") {
                fetchHTMLContent(forceReload: true)
            }
            .buttonStyle(PrimaryButtonStyle())
            .padding(.top, 8)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.white.opacity(0.1))
        )
        .padding(.vertical)
    }
    
    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Image(systemName: "text.book.closed")
                .font(.system(size: 60))
                .foregroundColor(Color.white.opacity(0.5))
            
            Text("暂无考试安排")
                .font(.title2)
                .fontWeight(.medium)
                .foregroundColor(.white)
            
            Text("当前没有找到任何考试安排信息")
                .font(.body)
                .foregroundColor(Color.white.opacity(0.7))
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, minHeight: 300)
        .padding()
    }
    
    // ForEach 的 id 现在是唯一的 SerialNumber
    private var examCardsView: some View {
        LazyVStack(spacing: 20) {
            ForEach(exams, id: \.SerialNumber) { exam in
                examCardView(exam: exam)
                    .transition(.asymmetric(
                        insertion: .move(edge: .trailing).combined(with: .opacity),
                        removal: .move(edge: .leading).combined(with: .opacity)
                    ))
            }
        }
    }
    
    // 修复场地显示：使用单独的 location 字段
    private func examCardView(exam: Exam) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text(exam.examType)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white)
                    .padding(4)
                    .background(exam.examType == "统一安排考试" ? Color.blue.opacity(0.7) : Color.purple.opacity(0.7))
                    .cornerRadius(4)
                
                Spacer()
                
                Text("序号: \(exam.SerialNumber)")
                    .font(.system(size: 14))
                    .foregroundColor(Color.white.opacity(0.8))
            }
            
            HStack {
                Text(exam.courseName)
                    .font(.headline.bold())
                    .foregroundColor(.white)
                    .lineLimit(1)
                
                Spacer()
                
                Text(exam.courseCode)
                    .font(.system(size: 14, design: .monospaced))
                    .padding(6)
                    .background(Capsule().fill(Color.white.opacity(0.2)))
            }
            
            Divider().background(Color.white.opacity(0.3))
            
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                infoCell(title: "学期", value: exam.semester)
                infoCell(title: "校区", value: exam.campus)
                infoCell(title: "周数", value: "\(exam.week)周")
                infoCell(title: "星期", value: exam.dayOfWeek)
                infoCell(title: "节次", value: exam.session)
                infoCell(title: "时间", value: exam.time)
                infoCell(title: "座位号", value: exam.seatNumber.isEmpty ? "未分配" : exam.seatNumber)
                infoCell(title: "场地", value: exam.location.isEmpty ? "未指定" : exam.location) // 显示正确的场地信息
            }
        }
        .padding()
        .background(
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 0.2, green: 0.25, blue: 0.5, opacity: 0.7),
                    Color(red: 0.3, green: 0.1, blue: 0.4, opacity: 0.7)
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.white.opacity(0.5), Color.clear]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1
                )
        )
        .shadow(color: Color.black.opacity(0.3), radius: 10, x: 0, y: 5)
        .frame(maxWidth: .infinity)
        .fixedSize(horizontal: false, vertical: true)
    }
    
    private func infoCell(title: String, value: String) -> some View {
        VStack(alignment: .leading) {
            Text(title.uppercased())
                .font(.system(size: 12))
                .foregroundColor(Color.white.opacity(0.7))
            Text(value)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    
    private var newExamNotification: some View {
        VStack {
            Spacer()
            HStack {
                Image(systemName: "bell.badge.fill")
                    .foregroundColor(.yellow)
                Text("发现新的考试安排")
                    .fontWeight(.medium)
                Spacer()
                Button("关闭") {
                    withAnimation {
                        showNewCourseNotification = false
                    }
                }
                .font(.subheadline)
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 15)
                    .fill(Color(red: 0.2, green: 0.15, blue: 0.3))
                    .shadow(radius: 10)
            )
            .padding(.horizontal)
            .transition(.move(edge: .bottom))
        }
    }
    
    // MARK: - Data Handling
    private func refreshData() async {
        try? await Task.sleep(nanoseconds: 500_000_000)
        fetchHTMLContent(forceReload: true)
    }
    
    func fetchHTMLContent(forceReload: Bool = false) {
        self.isLoading = true
        self.errorMessage = nil
        let loginInstance = loginframe()
        
        loginInstance.undergraduateLoginRequest(username: username, password: password) { loginSuccess, loginMessage in
            DispatchQueue.main.async {
                guard loginSuccess else {
                    self.isLoading = false
                    self.errorAlterMessage = loginMessage
                    self.ShowAlert = true
                    return
                }
                
                self.cookie = loginframe.cookie ?? ""
                print("✅ ClockNotice.fetchHTMLContent获取到Cookie: \(self.cookie.prefix(50))...")
                
                guard !self.cookie.isEmpty else {
                    self.isLoading = false
                    self.errorMessage = "登录凭证（cookie）缺失，无法获取考试页面"
                    return
                }
                
                guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.GetExamPage)?term=2025-2026-1") else {
                    self.isLoading = false
                    self.errorMessage = "接口地址无效"
                    return
                }
                
                var request = URLRequest(url: url)
                request.httpMethod = "GET"
                request.timeoutInterval = 15
                request.addValue(self.cookie, forHTTPHeaderField: "Cookie")
                
                let task = URLSession.shared.dataTask(with: request) { data, response, error in
                    DispatchQueue.main.async {
                        self.isLoading = false
                        
                        if let error = error {
                            self.errorMessage = "网络错误：\(error.localizedDescription)"
                            return
                        }
                        
                        let httpResponse = response as? HTTPURLResponse
                        guard let validResponse = httpResponse, (200...299).contains(validResponse.statusCode) else {
                            let statusCode = httpResponse?.statusCode ?? -1
                            self.errorMessage = "服务器响应错误（状态码：\(statusCode)）"
                            return
                        }
                        
                        guard let responseData = data else {
                            self.errorMessage = "获取考试页面无返回数据"
                            return
                        }
                        
                        let decoder = JSONDecoder()
                        if let errorResponse = try? decoder.decode(Response.self, from: responseData) {
                            if errorResponse.code != 200 {
                                self.errorMessage = errorResponse.message == "Cookie无效(100101)"
                                    ? "登录失效 请尝试重新登录"
                                    : "获取考试页面失败: \(errorResponse.message)"
                                return
                            }
                        }
                        
                        do {
                            let courseResponse = try decoder.decode(CourseResponsePage.self, from: responseData)
                            if courseResponse.code == 200 {
                                print("📥 考试页面HTML：\(courseResponse.data.prefix(100))...")
                                let parsedExams = self.htmlParser.parseHTML(content: courseResponse.data)
                                
                                withAnimation {
                                    self.exams = parsedExams
                                }
                                
                                if !forceReload && !parsedExams.isEmpty && self.exams.count != parsedExams.count {
                                    self.showNewCourseNotification = true
                                }
                                
                                print("✅ 成功解析 \(parsedExams.count) 场考试（统一安排+\(parsedExams.filter { $0.examType == "统一安排考试" }.count)，自主申请+\(parsedExams.filter { $0.examType == "自主申请考试" }.count)）")
                            } else {
                                self.errorMessage = "获取考试页面失败: \(courseResponse.message)"
                            }
                        } catch {
                            self.errorMessage = "解析考试页面数据失败: \(error.localizedDescription)"
                            if let dataStr = String(data: responseData, encoding: .utf8) {
                                print("❌ 解析失败原始数据：\(dataStr.prefix(200))...")
                            }
                        }
                    }
                }
                task.resume()
            }
        }
    }
    
    struct PrimaryButtonStyle: ButtonStyle {
        func makeBody(configuration: Configuration) -> some View {
            configuration.label
                .font(.headline)
                .foregroundColor(.white)
                .padding(.vertical, 12)
                .padding(.horizontal, 30)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.blue, Color.purple]),
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .clipShape(Capsule())
                .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
                .opacity(configuration.isPressed ? 0.9 : 1.0)
                .animation(.easeOut(duration: 0.2), value: configuration.isPressed)
        }
    }
}

// 预览
struct ClockNotice_Previews: PreviewProvider {
    static var previews: some View {
        NavigationStack {
            ClockNotice()
        }
    }
}
