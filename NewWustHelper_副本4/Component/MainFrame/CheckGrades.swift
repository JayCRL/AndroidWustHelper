import SwiftUI
// 成绩数据模型
struct CourseGrades: Codable, Identifiable,Equatable{
    let id: String
    let term: String
    let courseNumber: String
    let courseName: String
    let groupName: String
    let score: String
    let flag: String
    let credit: String
    let courseHours: String
    let gradePoint: String
    let evaluateMethod: String
    let kind: String
    let courseKind: String
}

// 接口响应模型
struct GradeResponse: Codable {
    let code: Int
    let message: String
    let data: [CourseGrades]
    let timestamp: Int
}
struct NotificationView: View {
    let text: String
    let backgroundColor: Color
    @Binding var isVisible: Bool
    
    var body: some View {
        Text(text)
            .foregroundColor(.white)
            .padding(12)
            .background(backgroundColor)
            .cornerRadius(8)
            .shadow(radius: 5)
            .padding(.top, 20)
            .onAppear {
                // 自动隐藏通知
                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                    withAnimation(.easeOut(duration: 0.3)) {
                        isVisible = false
                    }
                }
            }
    }
}
// 扩展：解析学号和学期的工具方法
extension CheckGrades {
    // 1. 从学号提取入学年份（前4位）
    private func getAdmissionYear() -> Int? {
        guard StudentNumber.count >= 4 else { return nil }
        let yearString = String(StudentNumber.prefix(4))
        return Int(yearString)
    }
    
    // 2. 解析学期字符串（如"2025-2026-1"）为年级学期名称（如"大三上"）
    private func getGradeTermName(from termString: String) -> String {
        // 解析学期字符串（格式："YYYY-YYYY-N"，N=1或2）
        let parts = termString.components(separatedBy: ["-", "-"])
        guard parts.count == 3,
              let startYear = Int(parts[0]),
              let termNumber = Int(parts[2]),
              let admissionYear = getAdmissionYear()
                                                         else {
            return termString // 解析失败时返回原始字符串
        }
        
        // 计算年级（入学年份到起始年份的学年数+1）
        let grade = startYear - admissionYear + 1
        let gradeName: String
        switch grade {
        case 1: gradeName = "大一"
        case 2: gradeName = "大二"
        case 3: gradeName = "大三"
        case 4: gradeName = "大四"
        default: gradeName = "大\(grade)"
        }
        
        // 解析学期（1=上学期，2=下学期）
        let termName = termNumber == 1 ? "上" : "下"
        
        return "\(gradeName)\(termName)"
    }
    
    // 3. 从课程term字段获取显示名称（如"2025-2026-1" -> "大三上"）
    private func getDisplayTerm(for course: CourseGrades) -> String {
        return getGradeTermName(from: course.term)
    }
}
// 核心：通过 UIApplication 获取当前导航控制器，并启用右划返回
extension View {
    func enableSwipeBack() -> some View {
        self.onAppear {
            // 1. 获取当前窗口的根导航控制器
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let window = windowScene.windows.first,
                  let rootVC = window.rootViewController else {
                print("❌ 未找到根视图控制器，右划返回失效")
                return
            }
            
            // 递归查找导航控制器（处理可能的嵌套情况）
            let navVC = findNavigationController(from: rootVC)
            
            guard let navVC = navVC else {
                print("❌ 未找到导航控制器，右划返回失效")
                return
            }
            
            // 2. 启用系统原生右划手势
            navVC.interactivePopGestureRecognizer?.isEnabled = true
            // 3. 设置代理（用于控制手势触发条件）
            navVC.interactivePopGestureRecognizer?.delegate = SwipeBackDelegate.shared
        }
    }
    
    // 递归查找导航控制器（处理嵌套的情况）
    private func findNavigationController(from vc: UIViewController) -> UINavigationController? {
        if let navVC = vc as? UINavigationController {
            return navVC
        }
        for childVC in vc.children {
            if let navVC = findNavigationController(from: childVC) {
                return navVC
            }
        }
        return nil
    }
}

// 标准的手势代理类（控制手势触发条件）
class SwipeBackDelegate: NSObject, UIGestureRecognizerDelegate {
    // 单例模式，避免重复创建
    static let shared = SwipeBackDelegate()
    private override init() {}
    
    // 条件1：只有导航栈中有多个页面时，才允许右划返回（避免根页面误触发）
    func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        // 从手势的视图找到它所在的导航控制器
        guard let view = gestureRecognizer.view,
              let navVC = view.parentNavigationController else {
            return false
        }
        // 导航栈数量 >1 说明不是根页面，允许右划
        return navVC.viewControllers.count > 1
    }
    
    // 条件2：确保右划手势优先于其他手势（如列表点击、弹窗）
    // 修复：方法名已更新为 shouldBeRequiredToFailBy
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldBeRequiredToFailBy otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return true
    }
}

// 扩展 UIView 以获取所在的导航控制器
extension UIView {
    // 查找视图所在的导航控制器
    var parentNavigationController: UINavigationController? {
        var responder: UIResponder? = self
        while responder != nil {
            if let navVC = responder as? UINavigationController {
                return navVC
            }
            responder = responder?.next
        }
        return nil
    }
}
// 奖学金计算视图
struct ScholarshipCalculatorView: View{
    @Binding var isPresented: Bool
    let allGrades: [CourseGrades]
    @State private var selectedTerm: String = "全部课程"
    @State private var selectedCourses: Set<String> = []
    @State private var calculatedGPA: Float = 0.0
    @State private var isCalculating = false
    
    // 从成绩数据中提取可用的学期
    private var availableTerms: [String] {
        let terms = Array(Set(allGrades.map { $0.term })).sorted()
        return ["全部课程"] + terms
    }
    
    // 筛选后的课程（当前选中的学期）
    private var filteredCourses: [CourseGrades] {
        if selectedTerm == "全部课程" {
            return allGrades
        } else {
            return allGrades.filter { $0.term == selectedTerm }
        }
    }
    
    // 关键：判断当前学期的课程是否已全选（不影响其他学期）
    private var isCurrentTermAllSelected: Bool {
        let currentTermCourseIds = Set(filteredCourses.map { $0.id })
        // 检查当前学期的所有课程ID是否都在选中集合中
        return selectedCourses.isSuperset(of: currentTermCourseIds)
    }
    
    var body: some View {
        NavigationView {
            VStack {
                // 学期选择器
                Picker("选择学期", selection: $selectedTerm) {
                    ForEach(availableTerms, id: \.self) { term in
                        Text(term).tag(term)
                    }
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                
                // 课程列表（当前学期的课程，选中状态会保留跨学期）
                List {
                    ForEach(filteredCourses) { course in
                        HStack {
                            Toggle(isOn: Binding(
                                get: { selectedCourses.contains(course.id) },
                                set: { isSelected in
                                    if isSelected {
                                        selectedCourses.insert(course.id)
                                    } else {
                                        selectedCourses.remove(course.id)
                                    }
                                }
                            )) {
                                VStack(alignment: .leading) {
                                    Text(course.courseName)
                                        .font(.headline)
                                    Text("成绩: \(course.score) | 学分: \(course.credit)")
                                        .font(.subheadline)
                                        .foregroundColor(.gray)
                                }
                            }
                        }
                    }
                }
                
                // 全选/全不选按钮（核心修正）
                HStack {
                    Text("选择课程参与计算")
                    Spacer()
                    Button(isCurrentTermAllSelected ? "全不选" : "全选") {
                        let currentTermCourseIds = Set(filteredCourses.map { $0.id })
                        if isCurrentTermAllSelected {
                            // 全不选：仅移除当前学期的课程ID（保留其他学期选中的）
                            selectedCourses.subtract(currentTermCourseIds)
                        } else {
                            // 全选：仅添加当前学期的课程ID（累加至已有选中集合）
                            selectedCourses.formUnion(currentTermCourseIds)
                        }
                    }
                }
                .padding(.horizontal)
                
                // 计算按钮和结果显示
                VStack {
                    Button(action: calculateScholarshipGPA) {
                        if isCalculating {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("计算奖学金绩点")
                                .fontWeight(.bold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                    .padding(.horizontal)
                    .disabled(isCalculating || selectedCourses.isEmpty) // 无选中课程时禁用
                    
                    if calculatedGPA > 0 {
                        Text("奖学金绩点: \(String(format: "%.2f", calculatedGPA))")
                            .font(.title2)
                            .fontWeight(.bold)
                            .padding()
                            .foregroundColor(.blue)
                    }
                }
                .padding()
            }
            .navigationTitle("奖学金计算")
            .navigationBarItems(trailing: Button("完成") {
                isPresented = false
            })
        }
    }
    
    private func calculateScholarshipGPA() {
        isCalculating = true
        
        // 模拟计算过程（计算所有选中的课程，无论哪个学期）
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            var totalPoints: Float = 0
            var totalCredits: Float = 0
            
            // 遍历所有选中的课程ID，找到对应的课程计算
            for courseId in selectedCourses {
                guard let course = allGrades.first(where: { $0.id == courseId }) else { continue }
                if let credit = Float(course.credit),
                   let point = Float(course.gradePoint) {
                    totalPoints += credit * point
                    totalCredits += credit
                }
            }
            
            calculatedGPA = totalCredits > 0 ? totalPoints / totalCredits : 0
            isCalculating = false
        }
    }
}

// 年度绩点柱状图视图
struct YearlyGPAChartView: View {
    let allGrades: [CourseGrades]
    // 按学期分组计算GPA
    private var termGPAs: [(term: String, gpa: Float)] {
        var termMap: [String: (points: Float, credits: Float)] = [:]
        for grade in allGrades {
            if let credit = Float(grade.credit),
               let point = Float(grade.gradePoint) {
                if termMap[grade.term] == nil {
                    termMap[grade.term] = (0, 0)
                }
                if(grade.flag=="缓考"){
                    continue
                }
                termMap[grade.term]?.points += credit * point
                termMap[grade.term]?.credits += credit
            }
        }
        return termMap.map { term, data in
            (term: term, gpa: data.credits > 0 ? data.points / data.credits : 0)
        }.sorted { $0.term < $1.term }
    }
    
    // 找出最大GPA用于图表比例
    private var maxGPA: Float {
        let max = termGPAs.map { $0.gpa }.max() ?? 4.0
        return max > 4.0 ? max : 4.0
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("年度绩点分布")
                        .font(.title2)
                        .fontWeight(.bold)
                        .padding(.horizontal)
                    
                    if termGPAs.isEmpty {
                        Text("暂无成绩数据")
                            .foregroundColor(.gray)
                            .padding()
                    } else {
                        // 柱状图
                        VStack(alignment: .leading, spacing: 10) {
                            ForEach(termGPAs, id: \.term) { termData in
                                HStack {
                                    Text(termData.term)
                                        .frame(width: 100, alignment: .leading)
                                        .font(.caption)
                                    
                                    RoundedRectangle(cornerRadius: 4)
                                        .fill(LinearGradient(
                                            gradient: Gradient(colors: [.blue, .purple]),
                                            startPoint: .leading,
                                            endPoint: .trailing
                                        ))
                                        .frame(width: CGFloat(termData.gpa / maxGPA) * 200, height: 20)
                                    
                                    Text(String(format: "%.2f", termData.gpa))
                                        .font(.caption)
                                        .fontWeight(.bold)
                                }
                            }
                        }
                        .padding()
                        
                        // 绩点统计摘要
                        VStack(alignment: .leading, spacing: 8) {
                            Text("绩点统计:")
                                .font(.headline)
                            
                            let highest = termGPAs.max { $0.gpa < $1.gpa }
                            let lowest = termGPAs.min { $0.gpa < $1.gpa }
                            let average = termGPAs.reduce(0) { $0 + $1.gpa } / Float(termGPAs.count)
                            
                            Text("最高: \(String(format: "%.2f", highest?.gpa ?? 0)) (\(highest?.term ?? ""))")
                            Text("最低: \(String(format: "%.2f", lowest?.gpa ?? 0)) (\(lowest?.term ?? ""))")
                            Text("平均: \(String(format: "%.2f", average))")
                        }
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(10)
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
            .navigationTitle("年度绩点图表")
        }
    }
}
    // 课程成绩详情弹窗组件
struct GradesDetailPopup: View {
    @Binding var isPresented: Bool
    let course: CourseGrades
    
    var body: some View {
        ZStack {
            // 背景层
            if isPresented {
                Color.black.opacity(0.4)
                    .edgesIgnoringSafeArea(.all)
                    .zIndex(0)
                    .transition(.opacity)
                    .animation(.easeInOut(duration: 0.2), value: isPresented)
                    .onTapGesture {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            isPresented = false
                        }
                    }
            }
            
            // 内容层
            if isPresented {
                VStack(spacing: 0) {
                    // 标题和关闭按钮
                    HStack {
                        Spacer()
                        Text(course.courseName)
                        .foregroundColor(Color.black)
                            .fontWeight(.bold)      .font(.system(size: 20))
                        Spacer()
                    }
                    .padding(.all, 5)
                    .padding(.top, 10)
                    
                    // 课程信息列表
                    VStack(alignment: .leading, spacing: 5) {
                        GradeInfoRow(icon: "calendar", label: "上课学期", value: course.term)
                        GradeInfoRow(icon: "creditcard.fill", label: "课程学分", value: course.credit)
                        GradeInfoRow(icon: "clock.fill", label: "课程学时", value: course.courseHours)
                        GradeInfoRow(icon: "checkmark.seal.fill", label: "考核方式", value: course.kind)
                        GradeInfoRow(icon: "tag.fill", label: "课程类型", value: course.courseKind)
                    }
                    .padding()
                    
                    Spacer()
                }
                .frame(width: 240, height: 250)
                .background(Color.white)
                .cornerRadius(20)
                .padding(.horizontal, 30)
                .shadow(radius: 10)
                .zIndex(1)
                .transition(.scale(scale: 0.9, anchor: .center).combined(with: .opacity))
                .animation(.spring(response: 0.4, dampingFraction: 0.8, blendDuration: 0), value: isPresented)
            }
        }
    }
}
// 成绩信息行组件
struct GradeInfoRow: View {
    let icon: String
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .font(.system(size: 13))
                .frame(width: 25)
            Text(label)
                .foregroundColor(.gray)
                .font(.system(size: 13))
            Text(value)
                .fontWeight(.medium)
            
                .foregroundColor(.black)
            .font(.system(size: 12))
        }
        .padding(.vertical, 6) // 减小行高，更紧凑
        .padding(.horizontal)
        .frame(width: 225, alignment: .leading) // 固定宽度
        .background(Color.gray.opacity(0.1))
        .cornerRadius(10)
    }
}
//struct CheckGrades: View {
//    @State private var showScholarshipCalculator = false
//    @State private var showYearlyGPAChart = false
//    @State var showJidianfenduan: Bool = false
//    @State var showChenjishuoming: Bool = false
//    @State private var showCourseDetail = false // 控制详情弹窗显示
//      @State private var currentCourse: CourseGrades? = nil // 存储当前点击的课程
//    @AppStorage("ID") var StudentNumber: String = "202313201025"
//    @State var isNavigationButtonEnabled: Bool = true
//    @State var searchString: String = ""
//    @State var showTermSelector: Bool = false
//    @State var selectedTerm: String = "全部课程"
//    @Environment(\.dismiss) var dismiss: DismissAction
//    @State var gpa: Float = 0
//    @State private var isLoading: Bool = true
//    @State private var errorMessage: String? = nil
//    @AppStorage("authData") private var authData: String = ""
//    @AppStorage("savedGrades") private var savedGradesData: Data?
//    @State private var refreshTrigger = false // 下拉刷新触发器
//    @State private var showNewCourseNotification = false // 控制通知显示
//    @State private var showNoNewCourseNotification = false // 控制通知显示
//    @State private var ShowAlert:Bool=false
//    @State private var ShowGraduatedStudent:Bool=false
//    @State private var errorAlterMessage:String?=nil
//    @State private var webPage:String=""
//    @AppStorage("cookie") private var cookie: String = ""  // 非可选类型，默认空字符串
//    @AppStorage("username") var username:String=""
//    @AppStorage("password") var password:String=""
//    // 成绩数据数组
//    @State private var allGrades: [CourseGrades] = []
//    // 筛选后的成绩数据
//    @State private var filteredGrades: [CourseGrades] = []
//    // 新增：所有课程的原始学期列表（去重）
//      @State private var allRawTerms: [String] = []
//      // 新增：转换后的年级学期名称列表（用于筛选）
//      @State private var displayTerms: [String] = []
//    var body: some View {
//        ZStack{
//            ZStack {
//                AngularGradient(
//                    gradient: Gradient(colors: [Color.blue, Color("pi")]),
//                    center: .bottomTrailing,
//                    angle: .degrees(45.0)
//                ).edgesIgnoringSafeArea(.top)            .ignoresSafeArea(.keyboard, edges: .bottom) // 关键：忽略键盘对底部的安全区域限制
//                VStack {
//                    // 学号和GPA区域
//                    HStack {
//                        Spacer()
//                        Text("学号:").foregroundColor(.white).font(.system(size: 23))
//                        Text("\(StudentNumber)").font(.system(size: 22)) .foregroundColor(.white)
//                        Spacer()
//                        Text("GPA: \(String(format: "%.2f", gpa))").font(.system(size: 23)).foregroundColor(.white)
//                            .padding(.trailing, 10).padding(.leading, 7)
//                        Spacer()
//                    }.padding()
//                    
//                    // 搜索框
//                    HStack {
//                        RoundedRectangle(cornerRadius: 10)
//                            .fill(Color.white)
//                            .frame(width: 347, height: 40)
//                            .overlay {
//                                HStack(spacing: 0) {
//                                    TextField("请输入课程名", text: $searchString)
//                                        .padding(.leading, 15)
//                                    Button {
//                                        filterGrades()
//                                    } label: {
//                                        Image(systemName: "magnifyingglass")
//                                            .resizable().foregroundColor(.blue.opacity(0.6))
//                                            .frame(width: 30, height: 30)
//                                    }
//                                    .padding(.trailing, 10)
//                                }
//                            }
//                            .padding(.trailing, 5)
//                    }
//                    
//                    // 成绩列表
//                    ScrollView {
//                        
//                        if isLoading {
//                            // 加载状态
//                            VStack {
//                                //                                ProgressView()
//                                //                                    .foregroundColor(.white)
//                                Text("正在加载成绩数据...")
//                                    .foregroundColor(.white)
//                                    .padding()
//                            }
//                            .frame(maxWidth: .infinity, minHeight: 200)
//                            
//                        } else {
//                            // 条件判断：根据 filteredGrades 是否为空，渲染不同视图
//                            if filteredGrades.isEmpty {
//                                // 1. 空状态：显示“暂时没有成绩哦”（居中、友好提示）
//                                VStack(alignment: .center, spacing: 16) {
//                                    // 可选：添加图标增强视觉效果（如无成绩图标）
//                                    Image(systemName: "graduationcap.fill") // 系统图标，也可替换为自定义图片
//                                        .font(.system(size: 60))
//                                        .foregroundColor(.gray.opacity(0.4))
//                                    
//                                    Text("暂时没有成绩哦")
//                                        .font(.title2)
//                                        .foregroundColor(.gray.opacity(0.6))
//                                    
//                                    Text("后续成绩会自动同步到这里～")
//                                        .font(.subheadline)
//                                        .foregroundColor(.gray.opacity(0.5))
//                                }
//                                .frame(maxWidth: .infinity, maxHeight: .infinity) // 占满父容器，实现居中
//                                .padding()
//                                
//                            } else{
//                                // 2. 有数据：渲染成绩绩列表（添加成绩不为0的过滤条件）
//                                ForEach(filteredGrades.filter { grade in
//                                    return grade.score != "0"
//                                }) { grade in
//                                    RoundedRectangle(cornerRadius: 8)
//                                        .fill(Color.white)
//                                        .frame(height: 100)
//                                        .overlay(
//                                            VStack(alignment: .leading, spacing: 20) {
//                                                HStack {
//                                                    REWARDVIEW(score: grade.score)
//                                                    Text(grade.courseName)
//                                                        .font(.title3)
//                                                        .foregroundColor(.blue.opacity(0.6))
//                                                    Spacer()
//                                                }
//                                                HStack {
//                                                    Text("成绩: ")
//                                                        .font(.title3)
//                                                        .foregroundColor(.blue.opacity(0.6))
//                                                    Text("\(grade.score)")
//                                                        .font(.title3)
//                                                        .foregroundColor(Color.orange)
//                                                    Spacer()
//                                                    
//                                                    Text("学分: ")
//                                                        .font(.title3)
//                                                        .foregroundColor(Color.blue.opacity(0.6))
//                                                    Text("\(grade.credit)")
//                                                        .font(.title3)
//                                                        .foregroundColor(Color.orange)
//                                                    Spacer()
//                                                 
//                                                    Text("绩点: ")
//                                                        .font(.title3)
//                                                        .foregroundColor(Color.blue.opacity(0.6))
//                                                    Text("\(grade.gradePoint)")
//                                                        .font(.title3)
//                                                        .foregroundColor(Color.orange)
//                                                    Spacer()
//                                                }
//                                            }
//                                            .padding(10)
//                                        )
//                                        // 点击事件（赋值当前课程并显示弹窗）
//                                        .onTapGesture {
//                                            withAnimation(.easeInOut) {
//                                                currentCourse = grade // 存储当前点击的课程
//                                                showCourseDetail = true // 显示弹窗
//                                            }
//                                        }
//                                        // 点击反馈（提升交互感）
//                                        .contentShape(Rectangle())
//                                        .simultaneousGesture(
//                                            TapGesture()
//                                                .onEnded {
//                                                    // 震动反馈（仅iPhone支持）
//                                                    let generator = UIImpactFeedbackGenerator(style: .light)
//                                                    generator.impactOccurred()
//                                                }
//                                        )
//                                        .padding(.horizontal, 16)
//                                        .padding(.vertical, 8)
//                                }
//                            }
//                        }
//                    }.refreshable { // 原生下拉刷新
//                        
//                        startGradesFlow() // 下拉时重新获取数据
//                    }
//                    Spacer()
//                } .ignoresSafeArea(.keyboard, edges: .bottom)
//                // 新课程通知（居中显示）
//                    .navigationBarBackButtonHidden(true)
//                    .toolbar {
//                        ToolbarItem(placement: .principal) {
//                            HStack {
//                                Text(selectedTerm)
//                                    .foregroundColor(.white)
//                                    .font(.title2)
//                                Image(systemName: "control")
//                                    .resizable()
//                                    .frame(width: 18, height: 10)
//                                    .foregroundColor(.white)
//                                    .rotationEffect(.degrees(180))
//                            }
//                            .onTapGesture {
//                                showTermSelector = true
//                            }
//                        }
//                        
//                        ToolbarItem(placement: .navigationBarLeading) {
//                            Button {
//                                dismiss()
//                            } label: {
//                                HStack(spacing: 0) {
//                                    Image(systemName: "chevron.left")
//                                        .foregroundColor(.white)
//                                    Text("返回")
//                                        .foregroundColor(.white)
//                                }
//                            }
//                        }
//                        
//                        ToolbarItem(placement: .navigationBarTrailing) {
//                            Menu {
//                                Button {
//                                    showJidianfenduan = true
//                                    isNavigationButtonEnabled = false
//                                } label: {
//                                    Text("绩点分段")
//                                }
//                                Button {
//                                    showChenjishuoming = true
//                                } label: {
//                                    Text("成绩说明")
//                                }
//                                Button {
//                                           showScholarshipCalculator = true
//                                       } label: {
//                                           Text("奖学金计算")
//                                       }
//                                       Button {
//                                           showYearlyGPAChart = true
//                                       } label: {
//                                           Text("年度绩点柱状图")
//                                       }
//                            } label: {
//                                Image(systemName: "ellipsis.circle").foregroundColor(.white)
//                            }
//                            .disabled(!isNavigationButtonEnabled)
//                        }
//                    }
//                    .alert("出错了😣",isPresented: $ShowAlert) {
//                        Button("知道了", role: .destructive) {
//                            ShowAlert=false
//                        }
//                    } message: {
//                        Text(errorAlterMessage ?? "未知错误")
//                    }
//                    .alert("很抱歉😣",isPresented: $ShowGraduatedStudent) {
//                        Button("知道了", role: .destructive) {
//                            ShowAlert=false
//                            dismiss()
//                        }
//                    } message: {
//                        Text("研究生没有成绩查询哦")
//                    }
//                    .sheet(isPresented: $showTermSelector) {
//                        VStack(spacing: 0) {
//                            HStack {
//                                Button(action: {
//                                    showTermSelector = false
//                                }) {
//                                    Text("取消")
//                                        .foregroundColor(.gray).font(.title2).padding(.leading, 20)
//                                }
//                                Spacer()
//                                Button(action: {
//                                    showTermSelector = false
//                                    filterGradesByDisplayTerm() // 按转换后的名称筛选
//                                }) {
//                                    Text("确定").foregroundColor(.blue.opacity(0.6)).font(.title2).padding(.trailing, 20)
//                                }
//                            }
//                            Picker("选择学期", selection: $selectedTerm) {
//                                // 显示转换后的年级学期名称（如"大三上"）
//                                ForEach(displayTerms, id: \.self) { term in
//                                    Text(term).tag(term)
//                                }
//                            }
//                            .pickerStyle(WheelPickerStyle())
//                            .frame(height: 200)
//                        }
//                        .presentationDetents([.fraction(0.33)])
//                        .presentationDragIndicator(.hidden)
//                    }
//                
////                if showNewCourseNotification {
////                    withAnimation(.spring(response: 0.5, dampingFraction: 0.7)) {
////                            showNewCourseNotification = true
////                        }
////                }
////                
////                if showNoNewCourseNotification {
////                    Text("成绩已经是最新啦～🤔")
////                        .foregroundColor(.white)
////                        .padding(12)
////                        .background(Color.blue.opacity(0.5))
////                        .cornerRadius(8)
////                        .shadow(radius: 5)
////                        .transition(.opacity) // 淡入淡出+缩放动画
////                }
////                
////                if let errorMessage = errorMessage {
////                    Text("出错啦😫:\(errorMessage)")
////                        .foregroundColor(.white)
////                        .padding(12)
////                        .background(Color.blue.opacity(0.5))
////                        .cornerRadius(8)
////                        .shadow(radius: 5)
////                        .transition(.opacity) // 淡入淡出+缩放动画
////                }
////                
//                if showJidianfenduan {
//                    GradesLevel(
//                        show: $showJidianfenduan,
//                        isNavigationButtonEnabled: $isNavigationButtonEnabled
//                    )
//                }
//            }
//            
//            .alert("成绩说明", isPresented: $showChenjishuoming) {
//                Button("知道了", role: .cancel) { }
//            } message: {
//                VStack(alignment: .leading, spacing: 8) {
//                    Text("""
//                本软件的成绩数据仅供参考
//                具体请以教务处官网为准
//                1.计算平均学分绩点(GPA)
//                平均学分绩点=Σ(学分*绩点)÷学分
//                2.平均成绩算法同GPA
//                成绩为ABCD等级制的换算成成绩区间平均数进行计算
//                """)
//                    .multilineTextAlignment(.leading)
//                    .padding(.horizontal)
//                    .fixedSize(horizontal: false, vertical: true)
//                }
//            }
//            .onAppear {
//                if(Identify.chooseIdentify==Identify.Graduate){
//                    ShowGraduatedStudent.toggle()
//                }else{
//                    
//                    // 从网络获取最新数据
//                    let loginInstance = loginframe() // 创建实例
//                    loginInstance.getloginCookie(username: username, password: password){loginSuccess, loginMessage in
//                        if loginSuccess {
//                            self.cookie = loginMessage // 新增这一行！
//                            // 优先加载本地保存的数据
//                            if let savedData = savedGradesData,
//                               let savedGrades = try? JSONDecoder().decode([CourseGrades].self, from: savedData) {
//                                allGrades = savedGrades
//                                filteredGrades = savedGrades
//                                calculateGPA()
//                                isLoading = false
//                            }
//                            startGradesFlow()
//                        }else{
//                            // 优先加载本地保存的数据
//                            if let savedData = savedGradesData,
//                               let savedGrades = try? JSONDecoder().decode([CourseGrades].self, from: savedData) {
//                                allGrades = savedGrades
//                                filteredGrades = savedGrades
//                                calculateGPA()
//                                isLoading = false
//                            }
//                            errorMessage = "网络错误或登录失效 已加载本地缓存"
//                        }
//                    }
//                    
//                }
//            }
//            .onChange(of: searchString) { _ in
//                filterGrades()
//            }
//            .onChange(of: allGrades) { newGrades in
//                // 当课程数据更新时，提取并转换所有学期
//                updateTerms()
//            }
//            .sheet(isPresented: $showScholarshipCalculator) {
//                ScholarshipCalculatorView(isPresented: $showScholarshipCalculator, allGrades: allGrades)
//            }
//
//            .sheet(isPresented: $showYearlyGPAChart) {
//                YearlyGPAChartView(allGrades: allGrades)
//            }
//           
//        }// 关键：启用右划返回手势，触发 dismiss()（与左上角返回按钮逻辑一致）
//        .enableSwipeBack()
//        // 新增：课程详情弹窗（仅当 showCourseDetail 为 true 且 currentCourse 非空时显示）
//        // 在 body 的适当位置添加
//        // 在 CheckGrades 视图的适当位置添加通知视图
//        .overlay(
//            VStack {
//                if showNewCourseNotification {
//                    NotificationView(
//                        text: "新成绩出啦🌟快快查看！",
//                        backgroundColor: .blue.opacity(0.6),
//                        isVisible: $showNewCourseNotification
//                    )
//                    .transition(.move(edge: .top).combined(with: .opacity))
//                    .zIndex(10)
//                }
//                
//                if showNoNewCourseNotification {
//                    NotificationView(
//                        text: "成绩已经是最新啦～🤔",
//                        backgroundColor: .blue.opacity(0.5),
//                        isVisible: $showNoNewCourseNotification
//                    )
//                    .transition(.move(edge: .top).combined(with: .opacity))
//                    .zIndex(9)
//                }
//                
//                if let errorMessage = errorMessage {
//                    NotificationView(
//                        text: "出错啦😫: \(errorMessage)",
//                        backgroundColor: .red.opacity(0.6),
//                        isVisible: .constant(errorMessage != nil)
//                    )
//                    .transition(.move(edge: .top).combined(with: .opacity))
//                    .zIndex(8)
//                    .onChange(of: errorMessage) { newValue in
//                        if newValue == nil {
//                            // 错误消息被清除时，确保视图被移除
//                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
//                                withAnimation {
//                                    self.errorMessage = nil
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            .animation(.spring(response: 0.5, dampingFraction: 0.7, blendDuration: 0), value: showNewCourseNotification)
//            .animation(.spring(response: 0.5, dampingFraction: 0.7, blendDuration: 0), value: showNoNewCourseNotification)
//            .animation(.spring(response: 0.5, dampingFraction: 0.7, blendDuration: 0), value: errorMessage != nil)
//        , alignment: .top)
//        .overlay(
//            Group {
//                if showCourseDetail, let course = currentCourse {
//                    GradesDetailPopup(
//                        isPresented: $showCourseDetail,
//                        course: course
//                    )
//                }
//            }
//        )
//    }
//    func startGradesFlow(completion: @escaping () -> Void = {}) {
//        // 第一步：先执行 fetchCourses 获取 coursesPage
//        fetchScores {
//            // 检查 coursesPage 是否为空（避免空值导致 Parse 失败）
//            guard !self.webPage.isEmpty else {
//                self.errorMessage = "未获取到课程页面数据，无法解析"
//                print("❌ coursesPage 为空，终止课程流程")
//                completion()
//                return
//            }
//            // 第二步：执行 ParseCourses 解析课程
//            self.ParseGrades()
//        }
//    }
//
//    // 1. 修改按学期筛选的方法，添加当前学期GPA计算
//       private func filterGradesByDisplayTerm() {
//           let filtered: [CourseGrades]
//                  if selectedTerm == "全部课程" {
//                      // 选中“全部课程”时显示所有课程
//                      filtered = allGrades
//                      calculateGPA()
//                  } else {
//                      // 否则筛选对应学期的课程
//                      filtered = allGrades.filter { getDisplayTerm(for: $0) == selectedTerm }
//                      filteredGrades = filtered
//                      calculateCurrentTermGPA(for: filtered)
//                  }
//                  
//       }
//       
//    // 新增：更新学期列表（提取原始term并转换为显示名称）
//    private func updateTerms() {
//            // 提取所有不重复的原始term并转换为显示名称
//            let rawTerms = Array(Set(allGrades.map { $0.term })).sorted()
//            var termNames = rawTerms.map { getGradeTermName(from: $0) }
//            
//            // 在学期列表最前面添加“全部课程”选项
//            termNames.insert("全部课程", at: 0)
//            
//            displayTerms = termNames
//            // 默认选中“全部课程”
//            selectedTerm = "全部课程"
//        }
//  
//
//    func refresh(){
//        DispatchQueue.main.asyncAfter(deadline: .now() + 5) {
//            withAnimation {
//                errorMessage = nil
//            }
//        }
//    }
//    // 2. fetchScores（移除weak + 修复httpResponse作用域）
//    private func fetchScores(completion: @escaping () -> Void = {}) {
//        self.isLoading = true
//        self.errorMessage = nil
//        
//        // 检查登录状态
//        guard !self.authData.isEmpty else {
//            self.isLoading = false
//            self.errorMessage = "未登录，请重新登录"
//            completion()
//            return
//        }
//        // 检查 cookie 是否存在
//        guard !self.cookie.isEmpty else {
//            self.isLoading = false
//            self.errorMessage = "登录凭证（cookie）缺失，无法获取课程页面"
//            completion()
//            return
//        }
//        // 配置 URL（移除冗余的 encodedCookie）
//        guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.GetScorePage)") else {
//            self.isLoading = false
//            self.errorMessage = "接口地址无效"
//            completion()
//            return
//        }
//        
//        // 配置请求
//        var request = URLRequest(url: url)
//        request.httpMethod = "GET"
//        request.timeoutInterval = 15
//        request.addValue(self.cookie, forHTTPHeaderField: "Cookie")
//        // 发送请求（移除 [weak self]）
//        let task = URLSession.shared.dataTask(with: request) { data, response, error in
//            DispatchQueue.main.async {
//                // 移除 guard let self = self（值类型无需弱引用）
//                self.isLoading = false
//                
//                // 处理网络错误
//                if let error = error {
//                    self.errorMessage = "网络错误：\(error.localizedDescription)"
//                    completion()
//                    return
//                }
//                
//                // 修复httpResponse作用域：先获取httpResponse，再判断状态码
//                let httpResponse = response as? HTTPURLResponse
//                guard let validResponse = httpResponse, (200...299).contains(validResponse.statusCode) else {
//                    // 直接使用httpResponse变量（即使为nil，也能安全获取状态码）
//                    let statusCode = httpResponse?.statusCode ?? -1
//                    self.errorMessage = "服务器响应错误（状态码：\(statusCode)）"
//                    completion()
//                    return
//                }
//                
//                // 处理无数据
//                guard let responseData = data else {
//                    self.errorMessage = "获取课程页面无返回数据"
//                    completion()
//                    return
//                }
//                
//                let decoder = JSONDecoder()
//                // 第一次解析：处理通用错误
//                if let responseSuccess = try? decoder.decode(Response.self, from: responseData) {
//                    if responseSuccess.code != 200 {
//                        self.errorMessage = responseSuccess.message == "Cookie无效(100101)"
//                            ? "登录失效 请重新进入此界面"
//                            : "获取课程页面失败: \(responseSuccess.message)"
//                        completion()
//                        return
//                    }
//                }
//                do {
//                    let courseResponse = try decoder.decode(CourseResponsePage.self, from: responseData)
////                    print("📥 fetchCourses 原始数据：\(courseResponse.data)")
//                    if courseResponse.code == 200 {
//                        self.webPage = courseResponse.data
//                        print("✅ fetchCourses 成功，coursesPage 已赋值")
//                    } else {
//                        self.errorMessage = "获取课程页面失败: \(courseResponse.message)"
//                    }
//                } catch {
//                    self.errorMessage = "解析课程页面数据失败: \(error.localizedDescription)"
//                    // 调试：打印原始数据
//                    if let dataStr = String(data: responseData, encoding: .utf8) {
//                        print("❌ fetchCourses 解析失败，原始数据：\(dataStr)")
//                    }
//                }
//                completion()
//            }
//        }
//        task.resume()
//    }
//    // 从接口获取成绩数据
//    private func ParseGrades() {
//        // 1. 新增：检查 webpage 是否为空（避免空参数导致 400）
//        guard !webPage.isEmpty else {
//            self.isLoading = false
//            self.errorMessage = "webpage 参数为空，无法发起解析请求"
//            return
//        }
//        // 检查登录状态
//        guard !self.authData.isEmpty else {
//            self.isLoading = false
//            self.errorMessage = "未登录，请重新登录"
//            return
//        }
//        
//        // 配置 URL（移除冗余的 encodedCoursesPage）
//        guard let url = URL(string: "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.GetScore)") else {
//            self.isLoading = false
//            self.errorMessage = "接口地址无效"
//            return
//        }
//        // 配置请求
//        var request = URLRequest(url: url)
//        request.httpMethod = "POST"
//        // 声明请求体格式和期望响应格式
//        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
//        request.setValue("application/json", forHTTPHeaderField: "Accept")
//        // 新增：添加 Authorization 头（如果接口需要，根据实际情况调整）
//        if !self.authData.isEmpty {
//            request.setValue("Wuster \(self.authData)", forHTTPHeaderField: "Authorization")
//        }
//        request.timeoutInterval = 15
//        // 2. 修复：将 requestBody 转换为 JSON 并赋值给 httpBody（之前遗漏！）
//        let requestBody: [String: String] = ["webpage": self.webPage]
////        print(self.webPage)
//        guard let jsonData = try? JSONSerialization.data(withJSONObject: requestBody, options: []) else {
//            self.isLoading = false
//            self.errorMessage = "请求体 JSON 格式转换失败"
//            return
//        }
//        request.httpBody = jsonData // 关键：赋值请求体
//        
//        
//        let task = URLSession.shared.dataTask(with: request) { data, response, error in
//            DispatchQueue.main.async {
//                isLoading = false
//                if let error = error {
//                    errorMessage = "网络错误: \(error.localizedDescription)"
//                    refresh()
//                    return
//                }
//                
//                guard let httpResponse = response as? HTTPURLResponse,
//                      (200...299).contains(httpResponse.statusCode) else {
//                    errorMessage = "服务器响应错误"
//                    refresh()
//                    return
//                }
//                
//                guard let data = data else {
//                    errorMessage = "未获取到数据"
//                    refresh()
//                    return
//                }
//                let decoder = JSONDecoder()
//                // 第一次解析：尝试解析为Response类型
//                if let responseSuccess = try? decoder.decode(Response.self, from: data) {
//                    // 处理Response类型的解析结果
//                    if responseSuccess.code != 200 {
//                        if responseSuccess.message == "Cookie无效(100101)" {
//                            errorMessage = "登录失效 请尝试重新登录"
//                        } else {
//                            errorMessage = "获取失败: \(responseSuccess.message)"
//                        }
//                        // 如果已经捕获到错误，可以提前返回
//                        return
//                    }
//                }
//                do {
//                        let response = try decoder.decode(GradeResponse.self, from: data)
//                        if response.code == 200 {
//                            let newGrades = response.data
//                            print(newGrades)
//                            // 检测是否有新课程（对比本地保存的数据）
//                            let hasNewCourses = checkForNewCourses(newGrades)
//                            allGrades = newGrades
//                            filteredGrades = newGrades
//                            calculateGPA()
//                            saveGradesData(newGrades)
//                            updateTerms()
////                            calculateCurrentTermGPA(for: newGrades)
//                            errorMessage = nil
//                            // 如果有新课程，显示通知
//                            if hasNewCourses {
//                                withAnimation(.easeInOut(duration: 0.3)) { // 明确触发动画
//                                        showNewCourseNotification = true
//                                    }
//                                    // 3秒后隐藏
//                                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
//                                        withAnimation(.easeInOut(duration: 0.3)) {
//                                            showNewCourseNotification = false
//                                        }
//                                    }
//                            }else{
//                                showNoNewCourseNotification = true
//                                // 3秒后自动隐藏通知
//                                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
//                                    withAnimation {
//                                        showNoNewCourseNotification = false
//                                    }
//                                }
//                            }
//                        } else {
//                            errorMessage = "获取失败: \(response.message)"
//                            refresh()
//                        }
//                } catch {
//                    refresh()
//                }
//            }
//        }
//        task.resume()
//    }
//    // 检测是否有新课程（通过id对比）
//       private func checkForNewCourses(_ newGrades: [CourseGrades]) -> Bool {
//           // 本地已保存的课程id集合
//           let existingIds = Set(allGrades.map { $0.id })
//           // 新获取的课程中，id不在本地集合中的即为新课程
//           let newIds = newGrades.filter { !existingIds.contains($0.id) }
//           return !newIds.isEmpty
//       }
//    // 保存成绩数据到本地
//    private func saveGradesData(_ grades: [CourseGrades]) {
//        do {
//            let data = try JSONEncoder().encode(grades)
//            savedGradesData = data
//        } catch {
//            print("保存成绩数据失败: \(error)")
//        }
//    }
//    
//    // 筛选成绩数据（按学期和搜索关键词）
//    private func filterGrades() {
//        
//        let filtered = allGrades.filter { grade in
//                  // “全部课程”时不限制学期，否则匹配对应学期
//                  let termMatch = selectedTerm == "全部课程" || getDisplayTerm(for: grade) == selectedTerm
//                  let searchMatch = searchString.isEmpty || grade.courseName.lowercased().contains(searchString.lowercased())
//                  return termMatch && searchMatch
//              }
//              filteredGrades = filtered
//              calculateCurrentTermGPA(for: filtered)
//    }
//    // 3. 新增：计算当前学期/筛选结果的GPA
//        private func calculateCurrentTermGPA(for courses: [CourseGrades]) {
//            var totalPoints: Float = 0
//            var totalCredits: Float = 0
//            for grade in courses {
//                guard let credit = Float(grade.credit),
//                      let point = Float(grade.gradePoint) else { continue }
//
//                if(grade.flag=="缓考"){
//                    continue
//                }
//                if(grade.kind=="补考一"){
//                    totalPoints += credit * point
//                    continue
//                }
//                totalPoints += credit * point
//                totalCredits += credit
//            }
//            
//            // 更新GPA为当前学期/筛选结果的GPA
//            gpa = totalCredits > 0 ? (totalPoints / totalCredits) : 0
//        }
//    // 计算GPA
//   private func calculateGPA() {
//       var totalPoints: Float = 0  // 总绩点
//       var totalCredits: Float = 0 // 总学分
//       // 字典：key=课程号(courseNumber)，value=该课程已计入的最新绩点(credit*point)
//       var coursePointMap: [String: Float] = [:]
//       
//       for grade in allGrades {
//           // 1. 过滤无效数据（学分/绩点无法转换为数字）
//           guard let credit = Float(grade.credit),
//                 let point = Float(grade.gradePoint) else { continue }
//           
//           // 2. 跳过缓考课程（不参与任何计算）
//           if grade.flag == "缓考" {
//               continue
//           }
//           
//           // 3. 通用变量：当前课程号、当前成绩的绩点（学分×绩点）
//           let courseNumber = grade.courseNumber
//           let currentPoint = credit * point // 无论类型，绩点计算方式一致
//           
//           // 4. 检查字典中是否已有该课程的历史绩点（统一覆盖逻辑）
//           if let historyPoint = coursePointMap[courseNumber] {
//               // 4.1 有历史绩点：先减去历史绩点，再加新绩点（实现覆盖）
//               totalPoints = totalPoints - historyPoint + currentPoint
//               // 更新字典为当前最新绩点
//               coursePointMap[courseNumber] = currentPoint
//           } else {
//               // 4.2 无历史绩点：首次处理该课程
//               totalPoints += currentPoint // 加当前绩点
//               // 仅“非补考”成绩首次出现时加学分（补考不加学分）
//               if grade.kind != "补考一" {
//                   totalCredits += credit
//               }
//               // 将当前绩点存入字典，绑定课程号
//               coursePointMap[courseNumber] = currentPoint
//           }
//       }
//       
//       // 5. 计算最终GPA（避免除以0）
//       gpa = totalCredits > 0 ? (totalPoints / totalCredits) : 0
//   }
//}
struct CheckGrades: View {
    @State private var showScholarshipCalculator = false
    @State private var showYearlyGPAChart = false
    @State var showJidianfenduan: Bool = false
    @State var showChenjishuoming: Bool = false
    @State private var showCourseDetail = false
    @State private var currentCourse: CourseGrades? = nil
    @AppStorage("ID") var StudentNumber: String = "202313201025"
    @State var isNavigationButtonEnabled: Bool = true
    @State var searchString: String = ""
    @State var showTermSelector: Bool = false
    @State var selectedTerm: String = "全部课程"
    @Environment(\.dismiss) var dismiss: DismissAction
    @State var gpa: Float = 0
    @State private var isLoading: Bool = true
    @State private var errorMessage: String? = nil
    @AppStorage("authData") private var authData: String = ""
    @AppStorage("savedGrades") private var savedGradesData: Data?
    @State private var refreshTrigger = false
    @State private var showNewCourseNotification = false
    @State private var showNoNewCourseNotification = false
    @State private var ShowAlert: Bool = false
    @State private var ShowGraduatedStudent: Bool = false
    @State private var errorAlterMessage: String? = nil
    @State private var webPage: String = ""
    @AppStorage("cookie") private var cookie: String = ""
    @AppStorage("username") var username: String = ""
    @AppStorage("password") var password: String = ""
    @State private var allGrades: [CourseGrades] = []
    @State private var filteredGrades: [CourseGrades] = []
    @State private var allRawTerms: [String] = []
    @State private var displayTerms: [String] = []
    
    // 提取主背景视图
    private var mainBackground: some View {
        AngularGradient(
            gradient: Gradient(colors: [Color.blue, Color("pi")]),
            center: .bottomTrailing,
            angle: .degrees(45.0)
        )
        .edgesIgnoringSafeArea(.top)
        .ignoresSafeArea(.keyboard, edges: .bottom)
    }
    
    // 提取学号和GPA区域
    private var studentInfoHeader: some View {
        HStack {
            Spacer()
            Text("学号:")
                .foregroundColor(.white)
                .font(.system(size: 23))
            Text("\(StudentNumber)")
                .font(.system(size: 22))
                .foregroundColor(.white)
            Spacer()
            Text("GPA: \(String(format: "%.2f", gpa))")
                .font(.system(size: 23))
                .foregroundColor(.white)
                .padding(.trailing, 10)
                .padding(.leading, 7)
            Spacer()
        }
        .padding()
    }
    
    // 提取搜索框
    private var searchBar: some View {
        HStack {
            RoundedRectangle(cornerRadius: 10)
                .fill(Color.white)
                .frame(width: 347, height: 40)
                .overlay {
                    HStack(spacing: 0) {
                        TextField("请输入课程名", text: $searchString)
                            .padding(.leading, 15)
                        Button {
                            filterGrades()
                        } label: {
                            Image(systemName: "magnifyingglass")
                                .resizable()
                                .foregroundColor(.blue.opacity(0.6))
                                .frame(width: 30, height: 30)
                        }
                        .padding(.trailing, 10)
                    }
                }
                .padding(.trailing, 5)
        }
    }
    
    // 提取加载视图
    private var loadingView: some View {
        VStack {
            Text("正在加载成绩数据...")
                .foregroundColor(.white)
                .padding()
        }
        .frame(maxWidth: .infinity, minHeight: 200)
    }
    
    // 提取空状态视图
    private var emptyStateView: some View {
        VStack(alignment: .center, spacing: 16) {
            Image(systemName: "graduationcap.fill")
                .font(.system(size: 60))
                .foregroundColor(.gray.opacity(0.4))
            
            Text("暂时没有成绩哦")
                .font(.title2)
                .foregroundColor(.gray.opacity(0.6))
            
            Text("后续成绩会自动同步到这里～")
                .font(.subheadline)
                .foregroundColor(.gray.opacity(0.5))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
    
    // 提取单个成绩卡片视图
    private func gradeCardView(for grade: CourseGrades) -> some View {
        RoundedRectangle(cornerRadius: 8)
            .fill(Color.white)
            .frame(height: 100)
            .overlay(
                VStack(alignment: .leading, spacing: 20) {
                    HStack {
                        REWARDVIEW(score: grade.score)
                        Text(grade.courseName)
                            .font(.title3)
                            .foregroundColor(.blue.opacity(0.6))
                        Spacer()
                    }
                    HStack {
                        Text("成绩: ")
                            .font(.title3)
                            .foregroundColor(.blue.opacity(0.6))
                        Text("\(grade.score)")
                            .font(.title3)
                            .foregroundColor(Color.orange)
                        Spacer()
                        
                        Text("学分: ")
                            .font(.title3)
                            .foregroundColor(Color.blue.opacity(0.6))
                        Text("\(grade.credit)")
                            .font(.title3)
                            .foregroundColor(Color.orange)
                        Spacer()
                     
                        Text("绩点: ")
                            .font(.title3)
                            .foregroundColor(Color.blue.opacity(0.6))
                        Text("\(grade.gradePoint)")
                            .font(.title3)
                            .foregroundColor(Color.orange)
                        Spacer()
                    }
                }
                .padding(10)
            )
            .onTapGesture {
                withAnimation(.easeInOut) {
                    currentCourse = grade
                    showCourseDetail = true
                }
            }
            .contentShape(Rectangle())
            .simultaneousGesture(
                TapGesture()
                    .onEnded {
                        let generator = UIImpactFeedbackGenerator(style: .light)
                        generator.impactOccurred()
                    }
            )
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
    }
    
    // 提取成绩列表内容
    private var gradesListContent: some View {
        Group {
            if filteredGrades.isEmpty {
                emptyStateView
            } else {
                ForEach(filteredGrades.filter { $0.score != "0" }) { grade in
                    gradeCardView(for: grade)
                }
            }
        }
    }
    
    // 提取成绩列表
    private var gradesListView: some View {
        ScrollView {
            if isLoading {
                loadingView
            } else {
                gradesListContent
            }
        }
        .refreshable {
            startGradesFlow()
        }
    }
    
    // 提取工具栏
    private var navigationToolbar: some ToolbarContent {
        ToolbarItem(placement: .principal) {
            HStack {
                Text(selectedTerm)
                    .foregroundColor(.white)
                    .font(.title2)
                Image(systemName: "control")
                    .resizable()
                    .frame(width: 18, height: 10)
                    .foregroundColor(.white)
                    .rotationEffect(.degrees(180))
            }
            .onTapGesture {
                showTermSelector = true
            }
        }
    }
    
    // 提取返回按钮工具栏
    private var backButtonToolbar: some ToolbarContent {
        ToolbarItem(placement: .navigationBarLeading) {
            Button {
                dismiss()
            } label: {
                HStack(spacing: 0) {
                    Image(systemName: "chevron.left")
                        .foregroundColor(.white)
                    Text("返回")
                        .foregroundColor(.white)
                }
            }
        }
    }
    
    // 提取菜单工具栏
    private var menuToolbar: some ToolbarContent {
        ToolbarItem(placement: .navigationBarTrailing) {
            Menu {
                Button {
                    showJidianfenduan = true
                    isNavigationButtonEnabled = false
                } label: {
                    Text("绩点分段")
                }
                Button {
                    showChenjishuoming = true
                } label: {
                    Text("成绩说明")
                }
                Button {
                    showScholarshipCalculator = true
                } label: {
                    Text("奖学金计算")
                }
                Button {
                    showYearlyGPAChart = true
                } label: {
                    Text("年度绩点柱状图")
                }
            } label: {
                Image(systemName: "ellipsis.circle")
                    .foregroundColor(.white)
            }
            .disabled(!isNavigationButtonEnabled)
        }
    }
    
    // 提取通知视图
    private var notificationOverlay: some View {
        VStack {
            if showNewCourseNotification {
                NotificationView(
                    text: "新成绩出啦🌟快快查看！",
                    backgroundColor: .blue.opacity(0.6),
                    isVisible: $showNewCourseNotification
                )
                .transition(.move(edge: .top).combined(with: .opacity))
                .zIndex(10)
            }
            
            if showNoNewCourseNotification {
                NotificationView(
                    text: "成绩已经是最新啦～🤔",
                    backgroundColor: .blue.opacity(0.5),
                    isVisible: $showNoNewCourseNotification
                )
                .transition(.move(edge: .top).combined(with: .opacity))
                .zIndex(9)
            }
            
            if let errorMessage = errorMessage {
                NotificationView(
                    text: "出错啦😫: \(errorMessage)",
                    backgroundColor: .red.opacity(0.6),
                    isVisible: .constant(errorMessage != nil)
                )
                .transition(.move(edge: .top).combined(with: .opacity))
                .zIndex(8)
                .onChange(of: errorMessage) { newValue in
                    if newValue == nil {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            withAnimation {
                                self.errorMessage = nil
                            }
                        }
                    }
                }
            }
        }
        .animation(.spring(response: 0.5, dampingFraction: 0.7, blendDuration: 0), value: showNewCourseNotification)
        .animation(.spring(response: 0.5, dampingFraction: 0.7, blendDuration: 0), value: showNoNewCourseNotification)
        .animation(.spring(response: 0.5, dampingFraction: 0.7, blendDuration: 0), value: errorMessage != nil)
    }
    
    // 提取课程详情弹窗
    private var courseDetailOverlay: some View {
        Group {
            if showCourseDetail, let course = currentCourse {
                GradesDetailPopup(
                    isPresented: $showCourseDetail,
                    course: course
                )
            }
        }
    }
    
    var body: some View {
        ZStack {
            ZStack {
                mainBackground
                
                VStack {
                    studentInfoHeader
                    searchBar
                    gradesListView
                    Spacer()
                }
                .ignoresSafeArea(.keyboard, edges: .bottom)
            }
            .navigationBarBackButtonHidden(true)
            .toolbar { navigationToolbar }
            .toolbar { backButtonToolbar }
            .toolbar { menuToolbar }
            .alert("出错了😣", isPresented: $ShowAlert) {
                Button("知道了", role: .destructive) {
                    ShowAlert = false
                }
            } message: {
                Text(errorAlterMessage ?? "未知错误")
            }
            .alert("很抱歉😣", isPresented: $ShowGraduatedStudent) {
                Button("知道了", role: .destructive) {
                    ShowAlert = false
                    dismiss()
                }
            } message: {
                Text("研究生没有成绩查询哦")
            }
            .sheet(isPresented: $showTermSelector) {
                termSelectorSheet
            }
            
            if showJidianfenduan {
                GradesLevel(
                    show: $showJidianfenduan,
                    isNavigationButtonEnabled: $isNavigationButtonEnabled
                )
            }
        }
        .alert("成绩说明", isPresented: $showChenjishuoming) {
            Button("知道了", role: .cancel) { }
        } message: {
            VStack(alignment: .leading, spacing: 8) {
                Text("""
                本软件的成绩数据仅供参考
                具体请以教务处官网为准
                1.计算平均学分绩点(GPA)
                平均学分绩点=Σ(学分*绩点)÷学分
                2.平均成绩算法同GPA
                成绩为ABCD等级制的换算成成绩区间平均数进行计算
                """)
                .multilineTextAlignment(.leading)
                .padding(.horizontal)
                .fixedSize(horizontal: false, vertical: true)
            }
        }
        .onAppear {
            handleOnAppear()
        }
        .onChange(of: searchString) { _ in
            filterGrades()
        }
        .onChange(of: allGrades) { newGrades in
            updateTerms()
        }
        .sheet(isPresented: $showScholarshipCalculator) {
            ScholarshipCalculatorView(isPresented: $showScholarshipCalculator, allGrades: allGrades)
        }
        .sheet(isPresented: $showYearlyGPAChart) {
            YearlyGPAChartView(allGrades: allGrades)
        }
        .enableSwipeBack()
        .overlay(notificationOverlay, alignment: .top)
        .overlay(courseDetailOverlay)
    }
    
    // 提取学期选择器sheet
    private var termSelectorSheet: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: {
                    showTermSelector = false
                }) {
                    Text("取消")
                        .foregroundColor(.gray)
                        .font(.title2)
                        .padding(.leading, 20)
                }
                Spacer()
                Button(action: {
                    showTermSelector = false
                    filterGradesByDisplayTerm()
                }) {
                    Text("确定")
                        .foregroundColor(.blue.opacity(0.6))
                        .font(.title2)
                        .padding(.trailing, 20)
                }
            }
            Picker("选择学期", selection: $selectedTerm) {
                ForEach(displayTerms, id: \.self) { term in
                    Text(term).tag(term)
                }
            }
            .pickerStyle(WheelPickerStyle())
            .frame(height: 200)
        }
        .presentationDetents([.fraction(0.33)])
        .presentationDragIndicator(.hidden)
    }
    
    // 提取onAppear处理逻辑
    private func handleOnAppear() {
        if Identify.chooseIdentify == Identify.Graduate {
            ShowGraduatedStudent.toggle()
        } else {
            let loginInstance = loginframe()
            loginInstance.undergraduateLoginRequest(username: username, password: password) { loginSuccess, loginMessage in
                if loginSuccess {
                    // 从静态属性获取cookie（登录成功后已保存）
                    self.cookie = loginframe.cookie ?? ""
                    print("✅ CheckGrades获取到Cookie: \(self.cookie.prefix(50))...")
                    if let savedData = savedGradesData,
                       let savedGrades = try? JSONDecoder().decode([CourseGrades].self, from: savedData) {
                        allGrades = savedGrades
                        filteredGrades = savedGrades
                        calculateGPA()
                        isLoading = false
                    }
                    startGradesFlow()
                } else {
                    if let savedData = savedGradesData,
                       let savedGrades = try? JSONDecoder().decode([CourseGrades].self, from: savedData) {
                        allGrades = savedGrades
                        filteredGrades = savedGrades
                        calculateGPA()
                        isLoading = false
                    }
                    errorMessage = "网络错误或登录失效 已加载本地缓存"
                }
            }
        }
    }

    func startGradesFlow(completion: @escaping () -> Void = {}) {
        fetchScores {
        }
    }
   

    private func filterGradesByDisplayTerm() {
        let filtered: [CourseGrades]
        if selectedTerm == "全部课程" {
            filtered = allGrades
            calculateGPA()
        } else {
            filtered = allGrades.filter { getDisplayTerm(for: $0) == selectedTerm }
            filteredGrades = filtered
            calculateCurrentTermGPA(for: filtered)
        }
    }
    
    private func updateTerms() {
        let rawTerms = Array(Set(allGrades.map { $0.term })).sorted()
        var termNames = rawTerms.map { getGradeTermName(from: $0) }
        
        termNames.insert("全部课程", at: 0)
        
        displayTerms = termNames
        selectedTerm = "全部课程"
    }

    func refresh() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) {
            withAnimation {
                errorMessage = nil
            }
        }
    }

//    private func fetchScores(completion: @escaping () -> Void = {}) {
//        self.isLoading = true
//        self.errorMessage = nil
//        
//        guard !self.authData.isEmpty else {
//            self.isLoading = false
//            self.errorMessage = "未登录，请重新登录"
//            completion()
//            return
//        }
//        
//        guard !self.cookie.isEmpty else {
//            self.isLoading = false
//            self.errorMessage = "登录凭证（cookie）缺失，无法获取课程页面"
//            completion()
//            return
//        }
//        
//        guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.GetScorePage)") else {
//            self.isLoading = false
//            self.errorMessage = "接口地址无效"
//            completion()
//            return
//        }
//        
//        var request = URLRequest(url: url)
//        request.httpMethod = "GET"
//        request.timeoutInterval = 15
//        request.addValue(self.cookie, forHTTPHeaderField: "Cookie")
//        
//        let task = URLSession.shared.dataTask(with: request) { data, response, error in
//            DispatchQueue.main.async {
//                self.isLoading = false
//                
//                if let error = error {
//                    self.errorMessage = "网络错误：\(error.localizedDescription)"
//                    completion()
//                    return
//                }
//                
//                let httpResponse = response as? HTTPURLResponse
//                guard let validResponse = httpResponse, (200...299).contains(validResponse.statusCode) else {
//                    let statusCode = httpResponse?.statusCode ?? -1
//                    self.errorMessage = "服务器响应错误（状态码：\(statusCode)）"
//                    completion()
//                    return
//                }
//                
//                guard let responseData = data else {
//                    self.errorMessage = "获取课程页面无返回数据"
//                    completion()
//                    return
//                }
//                
//                let decoder = JSONDecoder()
//                if let responseSuccess = try? decoder.decode(Response.self, from: responseData) {
//                    if responseSuccess.code != 200 {
//                        self.errorMessage = responseSuccess.message == "Cookie无效(100101)"
//                            ? "登录失效 请重新进入此界面"
//                            : "获取课程页面失败: \(responseSuccess.message)"
//                        completion()
//                        return
//                    }
//                }
//                do {
//                    let courseResponse = try decoder.decode(CourseResponsePage.self, from: responseData)
//                    if courseResponse.code == 200 {
//                        self.webPage = courseResponse.data
//                        print("✅ fetchCourses 成功，coursesPage 已赋值")
//                    } else {
//                        self.errorMessage = "获取课程页面失败: \(courseResponse.message)"
//                    }
//                } catch {
//                    self.errorMessage = "解析课程页面数据失败: \(error.localizedDescription)"
//                    if let dataStr = String(data: responseData, encoding: .utf8) {
//                        print("❌ fetchCourses 解析失败，原始数据：\(dataStr)")
//                    }
//                }
//                completion()
//            }
//        }
//        task.resume()
//    }
    // 2. fetchScores（移除weak + 修复httpResponse作用域）
        private func fetchScores(completion: @escaping () -> Void = {}) {
            
            self.isLoading = true
            self.errorMessage = nil
//        
//            // 检查登录状态
//            guard !self.authData.isEmpty else {
//                self.isLoading = false
//                self.errorMessage = "未登录，请重新登录"
//                completion()
//                return
//            }
            // 检查 cookie 是否存在
            guard !self.cookie.isEmpty else {
                self.isLoading = false
                self.errorMessage = "登录凭证（cookie）缺失，无法获取课程页面"
                completion()
                return
            }
            // 配置 URL（移除冗余的 encodedCookie）
            guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.GetScorePage)") else {
                self.isLoading = false
                self.errorMessage = "接口地址无效"
                completion()
                return
            }
    
            
            // 配置请求
            var request = URLRequest(url: url)
            request.httpMethod = "GET"
            request.timeoutInterval = 15
            request.addValue(self.cookie, forHTTPHeaderField: "Cookie")
            // 发送请求（移除 [weak self]）
            let task = URLSession.shared.dataTask(with: request) { data, response, error in
                DispatchQueue.main.async {
                    // 移除 guard let self = self（值类型无需弱引用）
                    self.isLoading = false
    
                    // 处理网络错误
                    if let error = error {
                        self.errorMessage = "网络错误：\(error.localizedDescription)"
                        completion()
                        return
                    }
    
                    // 修复httpResponse作用域：先获取httpResponse，再判断状态码
                    let httpResponse = response as? HTTPURLResponse
                    guard let validResponse = httpResponse, (200...299).contains(validResponse.statusCode) else {
                        // 直接使用httpResponse变量（即使为nil，也能安全获取状态码）
                        let statusCode = httpResponse?.statusCode ?? -1
                        self.errorMessage = "服务器响应错误（状态码：\(statusCode)）"
                        completion()
                        return
                    }
    
                    // 处理无数据
                    guard let responseData = data else {
                        self.errorMessage = "获取课程页面无返回数据"
                        completion()
                        return
                    }
                 
                    let decoder = JSONDecoder()
                    // 第一次解析：处理通用错误
                    if let responseSuccess = try? decoder.decode(Response.self, from: responseData) {
                        if responseSuccess.code != 200 {
                            self.errorMessage = responseSuccess.message == "Cookie无效(100101)"
                                ? "登录失效 请重新进入此界面"
                                : "获取课程页面失败: \(responseSuccess.message)"
                            completion()
                            return
                        }
                    }
                    do {
                        let response = try decoder.decode(GradeResponse.self, from: responseData)
                        if response.code == 200 {
                            let newGrades = response.data
                            print(newGrades)
                            let hasNewCourses = checkForNewCourses(newGrades)
                            allGrades = newGrades
                            filteredGrades = newGrades
                            calculateGPA()
                            saveGradesData(newGrades)
                            updateTerms()
                            errorMessage = nil
                            
                            if hasNewCourses {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    showNewCourseNotification = true
                                }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                                    withAnimation(.easeInOut(duration: 0.3)) {
                                        showNewCourseNotification = false
                                    }
                                }
                            } else {
                                showNoNewCourseNotification = true
                                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                                    withAnimation {
                                        showNoNewCourseNotification = false
                                    }
                                }
                            }
                        } else {
                            errorMessage = "获取失败: \(response.message)"
                            refresh()
                        }
                    } catch {
                        self.errorMessage = "获取课程数据失败: \(error.localizedDescription)"
                        // 调试：打印原始数据
                        if let dataStr = String(data: responseData, encoding: .utf8) {
                            print("❌ 获取成绩数据失败，原始数据：\(dataStr)")
                        }
                    }
                    completion()
                }
            }
            task.resume()
        }

    private func ParseGrades() {
        guard !webPage.isEmpty else {
            self.isLoading = false
            self.errorMessage = "webpage 参数为空，无法发起解析请求"
            return
        }
        
        guard !self.authData.isEmpty else {
            self.isLoading = false
            self.errorMessage = "未登录，请重新登录"
            return
        }
        
        guard let url = URL(string: "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.GetScore)") else {
            self.isLoading = false
            self.errorMessage = "接口地址无效"
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        if !self.authData.isEmpty {
            request.setValue("Wuster \(self.authData)", forHTTPHeaderField: "Authorization")
        }
        request.timeoutInterval = 15
        
        let requestBody: [String: String] = ["webpage": self.webPage]
        guard let jsonData = try? JSONSerialization.data(withJSONObject: requestBody, options: []) else {
            self.isLoading = false
            self.errorMessage = "请求体 JSON 格式转换失败"
            return
        }
        request.httpBody = jsonData
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                if let error = error {
                    errorMessage = "网络错误: \(error.localizedDescription)"
                    refresh()
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    errorMessage = "服务器响应错误"
                    refresh()
                    return
                }
                
                guard let data = data else {
                    errorMessage = "未获取到数据"
                    refresh()
                    return
                }
                
                let decoder = JSONDecoder()
                if let responseSuccess = try? decoder.decode(Response.self, from: data) {
                    if responseSuccess.code != 200 {
                        if responseSuccess.message == "Cookie无效(100101)" {
                            errorMessage = "登录失效 请尝试重新登录"
                        } else {
                            errorMessage = "获取失败: \(responseSuccess.message)"
                        }
                        return
                    }
                }
                
                do {
                    let response = try decoder.decode(GradeResponse.self, from: data)
                    if response.code == 200 {
                        let newGrades = response.data
                        print(newGrades)
                        let hasNewCourses = checkForNewCourses(newGrades)
                        allGrades = newGrades
                        filteredGrades = newGrades
                        calculateGPA()
                        saveGradesData(newGrades)
                        updateTerms()
                        errorMessage = nil
                        
                        if hasNewCourses {
                            withAnimation(.easeInOut(duration: 0.3)) {
                                showNewCourseNotification = true
                            }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    showNewCourseNotification = false
                                }
                            }
                        } else {
                            showNoNewCourseNotification = true
                            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                                withAnimation {
                                    showNoNewCourseNotification = false
                                }
                            }
                        }
                    } else {
                        errorMessage = "获取失败: \(response.message)"
                        refresh()
                    }
                } catch {
                    refresh()
                }
            }
        }
        task.resume()
    }

    private func checkForNewCourses(_ newGrades: [CourseGrades]) -> Bool {
        let existingIds = Set(allGrades.map { $0.id })
        let newIds = newGrades.filter { !existingIds.contains($0.id) }
        return !newIds.isEmpty
    }

    private func saveGradesData(_ grades: [CourseGrades]) {
        do {
            let data = try JSONEncoder().encode(grades)
            savedGradesData = data
        } catch {
            print("保存成绩数据失败: \(error)")
        }
    }
    
    private func filterGrades() {
        let filtered = allGrades.filter { grade in
            let termMatch = selectedTerm == "全部课程" || getDisplayTerm(for: grade) == selectedTerm
            let searchMatch = searchString.isEmpty || grade.courseName.lowercased().contains(searchString.lowercased())
            return termMatch && searchMatch
        }
        filteredGrades = filtered
        calculateCurrentTermGPA(for: filtered)
    }
    
    private func calculateCurrentTermGPA(for courses: [CourseGrades]) {
        var totalPoints: Float = 0
        var totalCredits: Float = 0
        for grade in courses {
            guard let credit = Float(grade.credit),
                  let point = Float(grade.gradePoint) else { continue }

            if grade.flag == "缓考" {
                continue
            }
            if grade.kind == "补考一" {
                totalPoints += credit * point
                continue
            }
            totalPoints += credit * point
            totalCredits += credit
        }
        
        gpa = totalCredits > 0 ? (totalPoints / totalCredits) : 0
    }
    
    private func calculateGPA() {
        var totalPoints: Float = 0
        var totalCredits: Float = 0
        var coursePointMap: [String: Float] = [:]
        
        for grade in allGrades {
            guard let credit = Float(grade.credit),
                  let point = Float(grade.gradePoint) else { continue }
            
            if grade.flag == "缓考" {
                continue
            }
            
            let courseNumber = grade.courseNumber
            let currentPoint = credit * point
            
            if let historyPoint = coursePointMap[courseNumber] {
                totalPoints = totalPoints - historyPoint + currentPoint
                coursePointMap[courseNumber] = currentPoint
            } else {
                totalPoints += currentPoint
                if grade.kind != "补考一" {
                    totalCredits += credit
                }
                coursePointMap[courseNumber] = currentPoint
            }
        }
        
        gpa = totalCredits > 0 ? (totalPoints / totalCredits) : 0
    }
}

struct GradesLevel: View {
    @Binding var show: Bool
    @Binding var isNavigationButtonEnabled: Bool
    
    var body: some View {
        Color.black.opacity(0.5)
            .edgesIgnoringSafeArea(.all)
            .onTapGesture {
                show = false
                isNavigationButtonEnabled = true
            }
        
        VStack(alignment: .center, spacing: 0) {
            RoundedRectangle(cornerRadius: 5)
                .fill(Color.white)
                .frame(width: 300, height: UIScreen.main.bounds.height * 0.48)
                .overlay(
                    ZStack(alignment: .top) {
                        HStack {
                            Spacer()
                            Text("绩点分段")
                                .font(.headline)
                                .foregroundColor(.white)
                            Spacer()
                        }
                        .frame(width: 300, height: 50)
                        .background(Color.blue.opacity(0.5))
                        
                        VStack(alignment: .leading, spacing: 0) {
                            HStack {
                                Text("考核成绩")
                                    .font(.system(size:20))
                                    .foregroundColor(.black)
                                    .frame(width: 120, alignment: .center)
                                Text("等级制")
                                    .font(.system(size:20))
                                    .foregroundColor(.black)
                                    .frame(width: 80, alignment: .center)
                                Text("绩点")
                                    .font(.system(size:20))
                                    .foregroundColor(.black)
                                    .frame(width: 100, alignment: .center)
                            }
                            .padding(.top, 50)
                            
                            GradeRow(scoreRange: "90-100", grade: "A", gpa: "4.0")
                            GradeRow(scoreRange: "85-89", grade: "A-", gpa: "3.7")
                            GradeRow(scoreRange: "82-84", grade: "B+", gpa: "3.3")
                            GradeRow(scoreRange: "78-81", grade: "B", gpa: "3.0")
                            GradeRow(scoreRange: "75-77", grade: "B-", gpa: "2.7")
                            GradeRow(scoreRange: "72-74", grade: "C+", gpa: "2.3")
                            GradeRow(scoreRange: "68-71", grade: "C", gpa: "2.0")
                            GradeRow(scoreRange: "64-67", grade: "C-", gpa: "1.5")
                            GradeRow(scoreRange: "60-63", grade: "D", gpa: "1.0")
                            GradeRow(scoreRange: "60以下", grade: "F", gpa: "0")
                            Spacer()
                        }
                    }
                )
        }
    }
}

struct GradeRow: View {
    let scoreRange: String
    let grade: String
    let gpa: String
    var body: some View {
        HStack {
            Text("   \(scoreRange)")
                .frame(width: 100, height: 33, alignment: .leading)
            Text("          \(grade)")
                .frame(width: 90, height: 33, alignment: .leading)
            Text("  \(gpa)")
                .frame(width: 90, height: 33, alignment: .center)
        }
        .background(Color.white).foregroundColor(Color.black)
        .border(Color.gray.opacity(0.1), width: 1)
        .padding(.leading, 10)
    }
}
struct REWARDVIEW: View {
    let score: String
    @State private var scoreNumber: Int = 0
    var body: some View {
        ZStack {
            // 如果 scoreNumber >= 90，显示一个 Image
            if scoreNumber >= 90||score=="A" {
                Image("reward2") // 你可以替换为任何图像名
                .resizable().frame(width: 35,height: 35).padding(.top,2)            }
        }
        .onAppear {
            // 更新 scoreNumber，转换 score 为 Int
            scoreNumber = changeToInt(a: score)
        }
    }

    func changeToInt(a: String) -> Int {
        if let intValue = Int(a) {
            return intValue
        } else {
            return 0  // 如果转换失败，返回默认值 0
        }
    }
}

#Preview {
    NavigationStack {
        CheckGrades()
    }
}
