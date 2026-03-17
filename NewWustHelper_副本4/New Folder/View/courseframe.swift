//  courseframe.swift
//  study_test
//  Created by wust_lh on 2025/7/2.
import SwiftUI
import AVFoundation
import PhotosUI
#if canImport(WidgetKit)
import WidgetKit
#endif
// 图片保存的路径
// 二维码扫描视图（桥接UIKit的相机功能）
struct QRCodeScannerView: UIViewControllerRepresentable {
    @Binding var isPresented: Bool  // 控制扫码视图显示/隐藏
    @Binding var scannedCode: String  // 存储扫描结果
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    func makeUIViewController(context: Context) -> UIViewController {
        let scannerVC = QRScannerViewController()
        scannerVC.delegate = context.coordinator
        return scannerVC
    }
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
    // 修改QRCodeScannerView的Coordinator部分
    class Coordinator: NSObject, QRScannerDelegate {
        let parent: QRCodeScannerView
        init(_ parent: QRCodeScannerView) {
            self.parent = parent
        }
        
        func didScanQRCode(_ code: String) {
            print("扫描到的原始内容: \(code)") // 添加调试日志
            
            // 检查是否是Base64编码的数据
            if let data = Data(base64Encoded: code) {
                // 尝试解码Base64数据
                if let decodedString = String(data: data, encoding: .utf8) {
                    print("Base64解码后的内容: \(decodedString)") // 添加调试日志
                    parent.scannedCode = decodedString
                } else {
                    // 如果不是UTF-8字符串，直接使用原始Base64数据
                    parent.scannedCode = code
                }
            } else {
                // 如果不是Base64，直接使用扫描到的内容
                parent.scannedCode = code
            }
//            // 尝试将 Base64 字符串解码为 Data
//                        if let base64Data = Data(base64Encoded: code),
//                           let nsData = base64Data as NSData? {
//                            
//                            // --- 修复关键点：尝试 zlib 解压缩 ---
//                            if let decompressedData = try? nsData.decompressed(using: .zlib) as Data {
//                                
//                                // 成功解压缩后，尝试将其转换为 UTF-8 字符串 (即原始 JSON)
//                                if let decodedString = String(data: decompressedData, encoding: .utf8) {
//                                    print("Base64 & Zlib 解码成功后的内容: \(decodedString)")
//                                    parent.scannedCode = decodedString
//                                } else {
//                                    // 解压成功但非有效 UTF-8 字符串 (不太可能)
//                                    print("Zlib 解压成功，但 UTF-8 转换失败。")
//                                    parent.scannedCode = code
//                                }
//                            } else {
//                                // 解压缩失败，作为未压缩的 Base64 编码数据 (原始 JSON) 处理
//                                print("Zlib 解压失败，尝试作为未压缩的 Base64 JSON 处理...")
//                                if let decodedString = String(data: base64Data, encoding: .utf8) {
//                                    parent.scannedCode = decodedString
//                                } else {
//                                    // 无法作为 UTF-8 解码
//                                    print("Base64 解码后的内容无法转换为 UTF-8。")
//                                    parent.scannedCode = code
//                                }
//                            }
//                        } else {
//                            // 不是有效的 Base64 编码，作为纯文本处理
//                            print("不是有效的 Base64 编码，作为纯文本处理。")
//                            parent.scannedCode = code
//                        }
            parent.isPresented = false  // 关闭扫码视图
        }
        
        func didCancel() {
            parent.isPresented = false  // 取消扫描
        }
    }
}
// 扫码核心逻辑（UIKit部分）
class QRScannerViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
    weak var delegate: QRScannerDelegate?
    private let captureSession = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer!
    override func viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
        setupCancelButton()
    }
    private func setupCamera() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            startCaptureSession()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    if granted {
                        self?.startCaptureSession()
                    } else {
                        // 权限被拒绝时，先显示提示再关闭
                        let alert = UIAlertController(
                            title: "权限不足",
                            message: "请在设置中允许相机访问，以使用扫码功能",
                            preferredStyle: .alert
                        )
                        alert.addAction(UIAlertAction(title: "确定", style: .default) { _ in
                            self?.delegate?.didCancel()
                        })
                        self?.present(alert, animated: true)
                    }
                }
            }
        case .denied, .restricted:
            // 权限已被拒绝，显示提示
            let alert = UIAlertController(
                title: "权限被禁用",
                message: "请在设置 > 隐私与安全性 > 相机中开启权限",
                preferredStyle: .alert
            )
            alert.addAction(UIAlertAction(title: "确定", style: .default) { [weak self] _ in
                self?.delegate?.didCancel()
            })
            present(alert, animated: true)
        @unknown default:
            delegate?.didCancel()
        }
    }
    private func startCaptureSession() {
        guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
            print("无法获取后置相机设备") // 新增日志
            delegate?.didCancel()
            return
        }
        
        do {
            let input = try AVCaptureDeviceInput(device: videoDevice)
            if captureSession.canAddInput(input) {
                captureSession.addInput(input)
                
                let metadataOutput = AVCaptureMetadataOutput()
                if captureSession.canAddOutput(metadataOutput) {
                    captureSession.addOutput(metadataOutput)
                    metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
                    metadataOutput.metadataObjectTypes = [.qr]
                } else {
                    print("无法添加元数据输出") // 新增日志
                    delegate?.didCancel()
                    return
                }
            } else {
                print("无法添加相机输入") // 新增日志
                delegate?.didCancel()
                return
            }
            
            // 修复预览层添加逻辑
            previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
            previewLayer.frame = view.bounds
            previewLayer.videoGravity = .resizeAspectFill
            view.layer.insertSublayer(previewLayer, at: 0) // 确保在最底层，覆盖整个视图
            print("预览层已添加") // 新增日志
            
            captureSession.startRunning()
            print("相机会话启动成功") // 新增日志
            
        } catch {
            print("相机配置失败：\(error.localizedDescription)") // 细化日志
            delegate?.didCancel()
            return
        }
    }
     //添加取消按钮
    private func setupCancelButton() {
        let cancelButton = UIButton(type: .system)
        cancelButton.setTitle("取消", for: .normal)
        cancelButton.setTitleColor(.white, for: .normal)
        cancelButton.backgroundColor = .black.withAlphaComponent(0.5)
        cancelButton.layer.cornerRadius = 15
        cancelButton.frame = CGRect(x: 20, y: 40, width: 60, height: 30)
        cancelButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
        view.addSubview(cancelButton)
    }
    
    @objc private func cancelTapped() {
        delegate?.didCancel()
    }
//    private func setupCancelButton() {
//            let cancelButton = UIButton(type: .system)
//            cancelButton.setTitle("取消", for: .normal)
//            cancelButton.setTitleColor(.white, for: .normal)
//            cancelButton.backgroundColor = .black.withAlphaComponent(0.5)
//            cancelButton.layer.cornerRadius = 15
//            cancelButton.translatesAutoresizingMaskIntoConstraints = false
//            
//            view.addSubview(cancelButton)
//            
//            NSLayoutConstraint.activate([
//                cancelButton.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
//                cancelButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
//                cancelButton.widthAnchor.constraint(equalToConstant: 60),
//                cancelButton.heightAnchor.constraint(equalToConstant: 30)
//            ])
//            
//            cancelButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
//        }
//        
//        @objc private func cancelTapped() {
//            delegate?.didCancel()
//        }
//    
    // 处理扫描到的二维码
    // 在QRScannerViewController中添加更多调试信息
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        captureSession.stopRunning()
        
        guard let metadataObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
              let code = metadataObject.stringValue else {
            print("未找到可识别的二维码") // 添加调试日志
            delegate?.didCancel()
            return
        }
        
        print("成功扫描到二维码: \(code)") // 添加调试日志
        delegate?.didScanQRCode(code)  // 回调扫描结果
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if captureSession.isRunning {
            captureSession.stopRunning()
        }
    }
}

// 扫码回调协议
protocol QRScannerDelegate: AnyObject {
    func didScanQRCode(_ code: String)
    func didCancel()
}
// 外层响应模型
struct CourseResponse: Codable {
    let code: Int
    let message: String
    let data: [Course]  // 课表数组
    let timestamp: TimeInterval
}
struct CourseResponsePage: Codable {
    let code: Int
    let message: String
    let data: String  // 课表数组
    let timestamp: TimeInterval
}
// 课程模型（单个课程信息）
struct Course: Codable, Identifiable {
    let id = UUID()  // 用于ForEach遍历
    var name: String  // 课程名称
    var teacher: String  // 教师
    var teachClass: String  // 班级
    var startWeek: Int  // 开始周
    var endWeek: Int  // 结束周
    var weekDay: Int  // 星期（0-6，需确认接口定义）
    var startSection: Int  // 开始节次
    var endSection: Int  // 结束节次
    var classroom: String  // 教室
}
// 周选择栏的单个单元格子视图
struct WeekCell: View {
    let titleColor=Color("courseTitleColor")
    let index: Int  // 周索引（0-24，对应第1-25周）
    let isCurrentWeek: Bool  // 是否是当前周
    let onTap: (Int) -> Void  // 点击回调
    @AppStorage("thisweek") var thisweek:Int=2
    let allWeeksCourseMap: [Int: [Int: [Int]]]  // 所有周的课程哈希表
        // 当前周的课程哈希表
    private var currentWeekCourses: [Int: [Int]] {
            // 安全获取当前周的课程数据，没有则返回空字典
            return allWeeksCourseMap[index + 1] ?? [:]
    }
    
    // MARK: - Dot Grid Components
    private var dotGridColumns: [GridItem] {
        Array(repeating: GridItem(.fixed(3), spacing: 2), count: 7)
    }
    
    private var courseDotGrid: some View {
        LazyVGrid(columns: dotGridColumns, spacing: 2) {
            ForEach(0..<42) { dotIndex in
                CourseDotView(
                    dotIndex: dotIndex,
                    currentWeekCourses: currentWeekCourses
                )
            }
        }
    }
    var body: some View {
        Button(action: { onTap(index + 1) }) {
            VStack(spacing: 0) {
                if isCurrentWeek {
                    // 当前周样式（保持原有设计）
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color("bgcolor"))
                        .frame(width: 70, height: 85)
                        .overlay {
                            VStack(spacing: 5) {
                                Text("第\(index + 1)周")
                                    .foregroundColor(titleColor)
                                                                       .font(.system(size: 12)) // 使用固定字体大小
                                                                       .fontWeight(.light)
                                                                       .minimumScaleFactor(0.8) // 添加最小缩放因子
                                                                       .lineLimit(1)
                                // 课程状态点阵（简化逻辑）
                                courseDotGrid
                                if(index+1==thisweek){
                                    Text("本周")
                                        .foregroundColor(titleColor)
                                                                                .font(.system(size: 11))
                                                                                .fontWeight(.light)
                                                                                .minimumScaleFactor(0.8)
                                                                                .lineLimit(1)
                                }
                            }
                        }
                } else {
                    // 非当前周样式（保持原有设计）
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.gray.opacity(0))
                        .frame(width: 70, height: 85)
                        .overlay {
                            VStack(spacing: 5) {
                                Text("第\(index + 1)周")
                                    .font(.system(size: 12))
                                                                        .fontWeight(.light)
                                                                        .foregroundColor(titleColor)
                                                                        .minimumScaleFactor(0.8)
                                                                        .lineLimit(1)
                                // 课程状态点阵（简化逻辑）
                                courseDotGrid
                                if(index+1==thisweek){
                                    Text("本周")
                                        .foregroundColor(titleColor)
                                                                              .font(.system(size: 11))
                                                                              .fontWeight(.light)
                                                                              .minimumScaleFactor(0.8)
                                                                              .lineLimit(1)
                                }
                            }
                        }
                }
            }
            .padding(.leading, 2)
        }
    }
}

// MARK: - Course Dot View
struct CourseDotView: View {
    let dotIndex: Int
    let currentWeekCourses: [Int: [Int]]
    
    private var weekDay: Int {
        dotIndex % 7 + 1  // 1-7
    }
    
    private var section: Int {
        dotIndex / 7 + 1   // 1-6
    }
    
    private var hasCourse: Bool {
        currentWeekCourses[weekDay]?.contains(section) ?? false
    }
    
    var body: some View {
        Circle()
            .fill(hasCourse ? Color("hasCourseSet") : Color.gray.opacity(0.3))
            .frame(width: 4, height: 4)
    }
}

// 单独的课程单元格子视图，封装复杂逻辑
struct CourseCell: View {
    let index: Int  // 单元格索引（0-41）
    var courses: [Course]  // 所有课程数据
    @Binding var showBackGround:Bool
    @Binding var weeknumber: Int  // 当前周数
    var activeNumber: Int  // 当前选中的单元格
    var onTap: (Int) -> Void  // 点击回调
    var onCourseTap: (Course) -> Void
    var onCourseLongPress: (Course) -> Void
    var onMultipleCoursesTap: ([Course], Int) -> Void  // 多节课点击回调
    @AppStorage("showIfNotThisWeek") var showIfNotThisWeek:Bool=false

    // 计算当前单元格对应的星期和节次
    private var weekDay: Int {
        index % 7 + 1  // 1-7（周一到周日）
    }
    
    private var section: Int {
        index / 7 + 1  // 1-6（第1到第6节课）
    }
    
    private let courseColors: [Color] = [
        // 主色调
        Color(red: 0.89, green: 0.36, blue: 0.59), // 莓果玫红
        Color(red: 0.18, green: 0.75, blue: 0.65), // 都市青绿
        Color(red: 0.98, green: 0.61, blue: 0.38), // 🟧 蜜橙橘（新）
        Color(red: 0.30, green: 0.72, blue: 0.88), // 冰湖蓝
        Color(red: 0.54, green: 0.48, blue: 0.94), // 潮流紫
        Color(red: 0.96, green: 0.48, blue: 0.45), // 🔴 番茄红（新）

        // 时尚冷色
        Color(red: 0.22, green: 0.58, blue: 0.96), // 高级蔚蓝
        Color(red: 0.22, green: 0.68, blue: 0.62), // 冷翠绿
        Color(red: 0.95, green: 0.45, blue: 0.58), // 鲜蔷薇
        Color(red: 0.64, green: 0.50, blue: 0.88), // 柔紫灰蓝

        // 明亮柔色（含橙调）
        
        Color(red: 0.92, green: 0.55, blue: 0.68), // 粉番茄
        Color(red: 0.72, green: 0.65, blue: 0.95), // 雾紫

        // 点缀系（增强橙感）
        Color(red: 0.98, green: 0.72, blue: 0.42), // 🍊 杏黄橘（新）
        Color(red: 0.85, green: 0.35, blue: 0.55), // 高级莓红
        Color(red: 0.60, green: 0.45, blue: 0.90), // 雅紫
        Color(red: 0.28, green: 0.65, blue: 0.88)  // 碧蓝
    ]


    // 存储课程名称到颜色索引的映射（使用静态变量确保全局一致性）
    private static var courseNameToColorIndex: [String: Int] = [:]
    
    // 查找当前单元格是否有课程
    private var currentCourse: Course? {
        courses.first { course in
            // 课程在当前周范围内，且匹配星期和节次
            weeknumber >= course.startWeek &&
            weeknumber <= course.endWeek &&
            course.weekDay == weekDay &&
            (course.endSection/2 == section||(course.startSection-1)/2+1==section)
        }
    }
    //查看不是这周的课
    private var currentCourseNotThisWeek: Course? {
        courses.first { course in
            course.weekDay == weekDay&&(course.endSection/2 == section||(course.startSection-1)/2+1==section)&&(course.weekDay == weekDay)
        }
    }
    
    // 获取当前格子的所有课程（包括本周和非本周）
    private var allCoursesInCell: [Course] {
        courses.filter { course in
            course.weekDay == weekDay && (course.endSection/2 == section || (course.startSection-1)/2+1 == section)
        }
    }
    
    // 获取当前格子的本周课程
    private var currentWeekCoursesInCell: [Course] {
        allCoursesInCell.filter { course in
            weeknumber >= course.startWeek && weeknumber <= course.endWeek
        }
    }
    // 课程颜色（基于课程名称的哈希值，同一课程始终同色）
    private var courseColor: Color {
        guard let course = currentCourse else {
                return .gray.opacity(0.3) // 无课程时的默认颜色
        }
        // 如果课程名称已存在映射，使用已有颜色索引
        if let index = Self.courseNameToColorIndex[course.name] {
            return courseColors[index].opacity(0.7)
        }
        // 否则，为新课程名称分配一个新的颜色索引
        let newIndex = Self.courseNameToColorIndex.count % courseColors.count
        Self.courseNameToColorIndex[course.name] = newIndex
        
        return courseColors[newIndex].opacity(0.7)
    }
    
    var body: some View {
        Button(action: {
            onTap(index + 1)
            
            // 检查是否有课程
            let currentWeekCourses = currentWeekCoursesInCell
            let allCourses = allCoursesInCell
            
            if currentWeekCourses.count > 1 {
                // 如果本周有多节课，显示多节课详情
                onMultipleCoursesTap(currentWeekCourses, index)
            } else if allCourses.count > 1 {
                // 如果所有课程中有多节，显示多节课详情
                onMultipleCoursesTap(allCourses, index)
            } else if let course = currentCourse {
                // 只有一节课，显示单节课详情
                onCourseTap(course)
            } else if let course = currentCourseNotThisWeek {
                // 非本周课程
                onCourseTap(course)
            }
        }) {
            ZStack {
                // 单元格背景
                RoundedRectangle(cornerRadius: 15)
                    .fill(showBackGround ? Color.gray.opacity(0): ( activeNumber == index + 1 ? Color.gray.opacity(0.2) : Color("bgcolor")))
                    .frame(height: 120)
                
                // 如果有课程，显示课程卡片
                if let course = currentCourse {
                    RoundedRectangle(cornerRadius: 15)
                        .fill(courseColor)
                        .frame(width: 47, height: 120)
                        .overlay(
                            VStack(alignment: .leading, spacing: 2) {
                                Text("").frame(width: 10, height: 20)
                                Text(course.name)
                                    .font(.system(size: 11)) // 使用固定字体大小
                                                                       .fontWeight(.bold)
                                                                       .lineLimit(2)
                                                                       .multilineTextAlignment(.leading)
                                let cleanedClassroom = course.classroom
                                    .replacingOccurrences(of: "(黄家湖)", with: "")
                                    .trimmingCharacters(in: .whitespacesAndNewlines)
                                Text(cleanedClassroom)
                                    .font(.system(size: 10))
                                                                       .lineLimit(2)
                                                                       .minimumScaleFactor(0.7)
                                                                       .multilineTextAlignment(.center)
                                
                                Spacer()
                            }
                            .padding(2)
                            .foregroundColor(.white)
                        )
                    //显示非当前周课程 灰色
                }else if let course = currentCourseNotThisWeek{
                    if showIfNotThisWeek{
                        RoundedRectangle(cornerRadius: 15)
                            .fill(courseColor)
                            .frame(width: 47, height: 120)
                            .overlay(
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("").frame(width: 10, height: 20)
                                    Text(course.name)
                                        .font(.system(size: 11)) // 使用固定字体大小
                                        .fontWeight(.bold)
                                        .lineLimit(2)
                                        .multilineTextAlignment(.leading)
                                    let cleanedClassroom = course.classroom
                                        .replacingOccurrences(of: "(黄家湖)", with: "")
                                        .trimmingCharacters(in: .whitespacesAndNewlines)
                                    Text(cleanedClassroom)
                                        .font(.system(size: 10))
                                        .lineLimit(2)
                                        .minimumScaleFactor(0.7)
                                        .multilineTextAlignment(.center)
                                    
                                    Spacer()
                                }
                                    .padding(2)
                                    .foregroundColor(.white)
                            )
                    }
                }else if activeNumber == index + 1 {
                    Image(systemName: "plus").foregroundColor(Color.gray)
                        .font(.system(size: 16)) // 调整加号图标大小
                }
            }
        }
        .simultaneousGesture(
            // 长按手势（与按钮点击事件同时生效）
            LongPressGesture(minimumDuration: 0.3)
                .onEnded { _ in
                    if let course = currentCourse {
                        onCourseLongPress(course)
                        print("长按成功：\(course.name)")
                    } else {
                        // 根据当前单元格的section计算正确的startSection和endSection
                        let targetSection = section  // 当前单元格对应的节次（1-6）
                        let start = (targetSection - 1) * 2 + 1  // 如section=1 → start=1
                        let end = start + 1  // 如start=1 → end=2（对应1-2节）
                        var newcourse = Course(
                            name: "新课程",
                            teacher: "教师",
                            teachClass: "班级",
                            startWeek: weeknumber,  // 使用当前周数作为开始周
                            endWeek: weeknumber,    // 使用当前周数作为结束周（可以后续编辑）
                            weekDay: weekDay,  // 当前单元格的星期（正确）
                            startSection: start,  // 修正为当前节次对应的start
                            endSection: end,      // 修正为当前节次对应的end
                            classroom: "教室"
                        )
                        onCourseLongPress(newcourse)
                        print("长按创建新课程：周数=\(weeknumber)，节次=\(targetSection)，start=\(start)，end=\(end)")
                    }
                }
        )
    }
}
struct TimeSidebar: View {
    @Binding var showBackGroud: Bool
    @Binding var courseTimes: [CourseTime]
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ForEach(courseTimes) { time in
                Rectangle().fill(Color("bgcolor").opacity(showBackGroud ? 0:1))
                    .frame(width: 39, height: 125)
                    .background(Color.white.opacity(showBackGroud ? 0:1))
                    .overlay {
                        VStack {
                            Text(time.start)
                                .font(.system(size: 11)) // 使用固定字体大小
                                                                .fontWeight(.light)
                                                                .foregroundColor(.gray)
                                                                .minimumScaleFactor(0.7) // 添加最小缩放因子
                                                                .lineLimit(1)
                            Text("｜")
                                .font(.system(size: 10))
                                                                .fontWeight(.light)
                                                                .foregroundColor(.gray)
                                                                .lineLimit(1)
                            Text(time.end)
                                .font(.system(size: 11))
                                                               .fontWeight(.light)
                                                               .foregroundColor(.gray)
                                                               .minimumScaleFactor(0.7)
                                                               .lineLimit(1)
                        }
                    }
            }
            Spacer()
        }
    }
}

struct CourseTime: Identifiable {
    let id = UUID()
    var start: String
    var end: String
}



struct courseframe: View {
    @State private var initTask: Task<Void, Never>? = nil
    @AppStorage("showOtherCourseDetail") var showOtherCourseDetail:Bool=false
    @AppStorage("showOtherCourseId") var showOtherCourseId:Int=9999
    let titleColor=Color("courseTitleColor")
    @AppStorage("loverCourses") private var loverCoursesData: Data = Data()
    @State private var showLoverCourses: Bool = false
    @State private var loverCourses: [Course] = []
    @State private var qrCodeContent: String = ""
    //请求地址
    // 获取存储的authData
    @AppStorage("username") var username:String=""
    @AppStorage("password") var password:String=""
    @AppStorage("authData") private var authData: String = ""
    @AppStorage("thisweek") var thisweek:Int=1
    @State var monthNUmber:String = " "
    // 存储解析后的课表数据
    @AppStorage("courses") private var coursesData: Data = Data()
    @State private var coursesPage: String=""    // 加载状态
    @State private var courses: [Course] = []    // 加载状态
    @State private var isLoading: Bool = true
    @State private var hasLoadedOnce: Bool = false  // 记录是否已首次加载
    // 错误信息
    @State private var errorMessage: String? = nil
    @AppStorage("weeknumber") var weeknumber:Int=2 //当前周数
    @AppStorage("month") var month:Int=6
    @AppStorage("day") var day:Int=1
    @AppStorage("firstYear") var firstYear:Int=2025 //起始日年份
    @AppStorage("firstMonth") var firstMonth:Int=9 //起始日月
    @AppStorage("firstDay") var firstDay:Int=1 //起始日
    @State var activeNumber:Int=100
    @State var ischoose:Bool=false
    @State private var weekFullDates: [Date] = []  // 存储周一到周日的Date对象
    @State var weekdate: [String] = Array(repeating: "0/0", count: 7)
    @State var ShowAlert:Bool=false
    @State var scrollToTopTrigger:Bool=false
    @AppStorage("IsQingShan") var IsQingShan:Bool=false
    @State var CourseStates:[Bool]=[true,true,true,false,false,true,true,true,false,false,true,true,true,false,false,true,true,true,false,false,true,true,true,false,false,true,true,true,false,false,true,true,true,false,false,true,true,true,false,false,true,true,true,false,false,true,true]
    @State  var showCourseDetail: Bool = false
    @State private var showEditView: Bool = false
    @State  var selectedCourse: Course? = nil
    @State private var showMultipleCoursesDetail: Bool = false
    @State private var selectedCourses: [Course] = []
    @State private var selectedCellIndex: Int = 0
    @State private var currentCoursePageIndex: Int = 0
    @State var showBackGroudn:Bool=true
    @State private var showScanner = false  // 控制扫码视图显示
    @State private var scannedCode = ""     // 临时存储扫描结果
    @AppStorage("codeValue") private var codeValue: String = ""  // 持久化存储扫码结果
    @AppStorage("backgroundImagePath") private var backgroundImagePath: String = ""
    @State private var selectedBackgroundImage: Image? = nil
    @State private var showImagePicker: Bool = false
    @State private var showCodeConfirm = false
    @State var showError:Bool=false
    let coloumsT : [GridItem]=[
        GridItem(.fixed(3),spacing: 3,alignment: nil),
        GridItem(.fixed(3),spacing: 3,alignment: nil),
        GridItem(.fixed(3),spacing: 3,alignment: nil),
        GridItem(.fixed(3),spacing: 3,alignment: nil),
        GridItem(.fixed(3),spacing: 3,alignment: nil),
        GridItem(.fixed(3),spacing: 3,alignment: nil),
        GridItem(.fixed(3),spacing: 3,alignment: nil)
    ]
    @State var webPage:String=" "
    //保存图片的透明度
    @AppStorage("pictureOpacity") var pictureOpacity:Double=0.3
    //是否展示背景图
    @State var showBackGroudView:Bool=false
    //是否展示我的课表二维码
    @State var showMyCode:Bool=false
    @AppStorage("cookie") private var cookie: String = ""  // 非可选类型，默认空字符串
    //缓存背景图
    @State private var cachedBackgroundImage: Image?
    // 记录最后一次点击的时间
    @State private var lastClickTime: Date = Date.distantPast
    @AppStorage("showIfNotThisWeek") var showIfNotThisWeek:Bool=false
    @State var cleanQLKB:Bool=false
    // 在 CourseScheduleAppView 内部定义：
    @State private var scannedJSON: String = ""
    @State private var isGeneratingQRCode: Bool = false
    // App Group 配置（与小组件一致）
    // 注意：由于主 App 已设置 defaultAppStorage，@AppStorage 已自动使用 App Group
    // 这里直接使用同一个实例，确保一致性
    private var sharedDefaults: UserDefaults? {
        UserDefaults(suiteName: "group.linghang.wustHelper")
    }
    
    // 验证 App Group 是否可用
    private func verifyAppGroup() -> Bool {
        guard let sharedDefaults = sharedDefaults else {
            print("❌ App Group 不可用")
            return false
        }
        print("✅ App Group 可用：group.linghang.wustHelper")
        print("✅ 主 App Bundle ID: \(Bundle.main.bundleIdentifier ?? "未知")")
        
        // 验证是否与 @AppStorage 使用同一个实例
        // 由于主 App 设置了 defaultAppStorage，@AppStorage 应该已经使用 App Group
        // 这里直接写入验证
        sharedDefaults.set("test", forKey: "__test_key__")
        sharedDefaults.synchronize()
        if sharedDefaults.string(forKey: "__test_key__") == "test" {
            sharedDefaults.removeObject(forKey: "__test_key__")
            print("✅ App Group 写入/读取测试成功")
            
            // 列出当前 App Group 中的所有键
            let allKeys = sharedDefaults.dictionaryRepresentation().keys
            print("✅ 主 App 看到的 App Group 键（\(allKeys.count) 个）：\(allKeys.sorted().joined(separator: ", "))")
            return true
        } else {
            print("❌ App Group 写入/读取测试失败")
            return false
        }
    }
    
    // 同步数据到 App Group（供小组件使用）
    private func syncDataToAppGroup() {
        guard let sharedDefaults = sharedDefaults else {
            print("⚠️ App Group 未配置，无法同步数据到小组件")
            return
        }
        
        // 同步课程数据（使用 coursesData，如果为空则尝试从 courses 状态变量编码）
        if !coursesData.isEmpty {
            sharedDefaults.set(coursesData, forKey: "courses")
            print("✅ 同步课程数据到 App Group（\(coursesData.count) 字节）")
        } else if !courses.isEmpty {
            // 如果 coursesData 为空但 courses 不为空，重新编码并同步
            do {
                let encoder = JSONEncoder()
                let encodedData = try encoder.encode(courses)
                sharedDefaults.set(encodedData, forKey: "courses")
                print("✅ 从 courses 状态变量同步课程数据到 App Group（\(encodedData.count) 字节，\(courses.count) 门课程）")
            } catch {
                print("❌ 编码课程数据失败：\(error.localizedDescription)")
            }
        } else {
            print("⚠️ 课程数据为空，无法同步")
        }
        
        // 同步周数
        sharedDefaults.set(thisweek, forKey: "thisweek")
        sharedDefaults.set(weeknumber, forKey: "weeknumber")
        print("✅ 同步周数：thisweek=\(thisweek), weeknumber=\(weeknumber)")
        
        // 同步学期起始日期
        sharedDefaults.set(firstYear, forKey: "firstYear")
        sharedDefaults.set(firstMonth, forKey: "firstMonth")
        sharedDefaults.set(firstDay, forKey: "firstDay")
        print("✅ 同步学期起始日期：\(firstYear)-\(firstMonth)-\(firstDay)")
        
        // 同步校区设置
        sharedDefaults.set(IsQingShan, forKey: "IsQingShan")
        print("✅ 同步校区设置：IsQingShan=\(IsQingShan)")
        
        // 确保数据立即同步
        sharedDefaults.synchronize()
        WidgetCenter.shared.reloadAllTimelines()
            print("✅ 已通知 WidgetCenter 刷新所有小组件")
        
        // 验证数据是否真的写入了
        if let verifyData = sharedDefaults.data(forKey: "courses") {
            print("✅ 验证：App Group 中 courses 数据存在（\(verifyData.count) 字节）")
            let verifyWeek = sharedDefaults.integer(forKey: "thisweek")
            print("✅ 验证：App Group 中 thisweek=\(verifyWeek)")
            
            // 列出所有键用于调试
            let allKeys = sharedDefaults.dictionaryRepresentation().keys
            print("✅ 验证：App Group 中的所有键：\(allKeys.joined(separator: ", "))")
        } else {
            print("❌ 验证失败：App Group 中 courses 数据不存在！")
            print("❌ 可能原因：App Group 配置不正确或数据写入失败")
        }
        
        print("✅ 所有数据已同步到 App Group")
    }
    
    // 在用户选择图片后保存图片并更新 AppStorage
    func saveCourses(_ courses: [Course]) {
        do {
            let encoder = JSONEncoder()
            let encodedData = try encoder.encode(courses)
            coursesData = encodedData // 保存编码后的数据到 @AppStorage
            
            print("💾 保存课程数据：\(courses.count) 门课程，编码后大小：\(encodedData.count) 字节")
            
            // 验证 App Group 是否可用
            guard verifyAppGroup(), let sharedDefaults = sharedDefaults else {
                print("❌ App Group 验证失败，无法同步数据到小组件")
                return
            }
            
            // 立即同步到 App Group（供小组件使用）
            // 使用刚编码的数据，确保数据是最新的
            // 注意：由于主 App 设置了 defaultAppStorage，coursesData = encodedData 也会写入 App Group
            // 但这里直接写入确保数据立即可用
            sharedDefaults.set(encodedData, forKey: "courses")
            sharedDefaults.set(thisweek, forKey: "thisweek")
            sharedDefaults.set(weeknumber, forKey: "weeknumber")
            sharedDefaults.set(firstYear, forKey: "firstYear")
            sharedDefaults.set(firstMonth, forKey: "firstMonth")
            sharedDefaults.set(firstDay, forKey: "firstDay")
            sharedDefaults.set(IsQingShan, forKey: "IsQingShan")
            
            // 强制同步
            sharedDefaults.synchronize()
            
            // 验证数据是否真的写入了
            if let verifyData = sharedDefaults.data(forKey: "courses") {
                print("✅ 课程数据已直接同步到 App Group（验证：\(verifyData.count) 字节）")
                let verifyWeek = sharedDefaults.integer(forKey: "thisweek")
                print("✅ 周数验证：thisweek=\(verifyWeek)")
                
                // 列出所有键用于调试
                let allKeys = sharedDefaults.dictionaryRepresentation().keys
                print("✅ 验证：App Group 中的所有键：\(allKeys.joined(separator: ", "))")
            } else {
                print("❌ 警告：数据写入后验证失败，App Group 可能未正确配置")
            }
            
            // 刷新小组件
            #if canImport(WidgetKit)
            WidgetCenter.shared.reloadAllTimelines()
            print("🔄 已刷新小组件时间线")
            #else
            print("保存课程数据")
            #endif
        } catch {
            print("❌ 无法保存课程数据: \(error.localizedDescription)")
        }
    }
    // 加载课程数据的方法
    func loadCourses(forceRefresh: Bool = false) {
        // 如果已加载过且不是强制刷新，且有本地数据，则直接加载本地数据
        if hasLoadedOnce && !forceRefresh {
            if let localCourses = fetchLocalCourses() {
                self.courses = localCourses
                self.isLoading = false
                print("从本地加载课程数据（已加载过，不刷新）")
                updateCurrentDate()
                calculateWeekDates(for: thisweek)
                return
            }
        }
        
        // 如果是强制刷新，跳过本地数据检查，直接从网络获取
        if forceRefresh {
            print("🔄 强制刷新：从网络获取最新课程数据")
            self.isLoading = true
            // 根据用户身份选择不同的课程获取方式
            if Identify.chooseIdentify == Identify.Graduate {
                // 研究生：直接使用新的研究生API
                fetchGraduateCourses { [self] in
                    self.isLoading = false
                    self.hasLoadedOnce = true  // 标记已加载过
                    updateCurrentDate()
                    calculateWeekDates(for: thisweek)
                    if errorMessage != nil {
                        showError = true
                    }
                }
            } else {
                // 本科生：使用原有的获取流程
                getFirstDay { [self] in
                    let loginInstance = loginframe() // 创建实例
                    loginInstance.undergraduateLoginRequest(username: username, password: password){loginSuccess, loginMessage in
                        if loginSuccess {
                            // 从静态属性获取cookie（登录成功后已保存）
                            cookie = loginframe.cookie ?? ""
                            print("✅ courseframe获取到Cookie: \(cookie.prefix(50))...")
                            // 然后获取课程数据
                            startCourseFlow { [self] in
                                // 更新当前日期
                                updateCurrentDate()
                                // 计算日期
                                calculateWeekDates(for: thisweek)
                                if errorMessage != nil {
                                    showError = true
                                }
                            }
                        }else{
                            self.isLoading = false
                            self.errorMessage="登录失效 请尝试重新登录"
                        }
                    }
                }
            }
            return
        }
        
        // 1. 检查本地是否有课程数据（首次加载，非强制刷新）
        if let localCourses = fetchLocalCourses() {
            // 如果本地有课程数据，加载它
            self.courses = localCourses
            self.isLoading = false
            self.hasLoadedOnce = true  // 标记已加载过
            print("从本地加载课程数据（首次）")
            // 更新日期
            updateCurrentDate()
            calculateWeekDates(for: thisweek)
        } else {
            // 2. 没有本地数据，需要从网络获取
            self.isLoading = true
            // 根据用户身份选择不同的课程获取方式
            if Identify.chooseIdentify == Identify.Graduate {
                // 研究生：直接使用新的研究生API
                fetchGraduateCourses { [self] in
                    self.isLoading = false
                    self.hasLoadedOnce = true  // 标记已加载过
                    updateCurrentDate()
                    calculateWeekDates(for: thisweek)
                    if errorMessage != nil {
                        showError = true
                    }
                }
            } else {
                // 本科生：使用原有的获取流程
                getFirstDay { [self] in
                    let loginInstance = loginframe() // 创建实例
                    loginInstance.undergraduateLoginRequest(username: username, password: password){loginSuccess, loginMessage in
                        if loginSuccess {
                            // 从静态属性获取cookie（登录成功后已保存）
                            cookie = loginframe.cookie ?? ""
                            print("✅ courseframe获取到Cookie: \(cookie.prefix(50))...")
                            // 然后获取课程数据
                            startCourseFlow { [self] in
                                // 更新当前日期
                                updateCurrentDate()
                                // 计算日期
                                calculateWeekDates(for: thisweek)
                                if errorMessage != nil {
                                    showError = true
                                }
                            }
                        }else{
                            self.isLoading = false
                            self.errorMessage="登录失效 请尝试重新登录"
                        }
                    }
                }
            }
        }
    }
    // 从 @AppStorage 中加载课程数据
    private func fetchLocalCourses() -> [Course]? {
        // 检查 @AppStorage 中是否有存储的课程数据
        guard !coursesData.isEmpty else {
            print("没有存储的课程数据")
            return nil
        }
        do {
            let decoder = JSONDecoder()
            let decodedCourses = try decoder.decode([Course].self, from: coursesData)
            print("成功从 @AppStorage 解码课程数据")
            // 不再在这里设置isLoading，由调用者管理
            return decodedCourses
        } catch {
            print("无法从 @AppStorage 解码课程数据: \(error.localizedDescription)")
            return nil
        }
    }
    func loadImageFromDocuments() -> Image? {
        let fileManager = FileManager.default
        print("尝试读取图片，路径：\(backgroundImagePath)") // 新增日志
        
        if !backgroundImagePath.isEmpty {
            let imageURL = URL(fileURLWithPath: backgroundImagePath)
            print("路径是否存在：\(fileManager.fileExists(atPath: backgroundImagePath))") // 新增日志
            
            // 检查文件是否存在
            if fileManager.fileExists(atPath: backgroundImagePath) {
                do {
                    let imageData = try Data(contentsOf: imageURL)
                    print("读取到图片数据，大小：\(imageData.count) 字节") // 新增日志
                    if let uiImage = UIImage(data: imageData) {
                        return Image(uiImage: uiImage)
                    } else {
                        print("图片数据无效，无法转为 UIImage") // 新增日志
                    }
                } catch {
                    print("读取图片数据失败，错误信息：\(error.localizedDescription)") // 输出详细的错误信息
                }
            } else {
                print("路径不存在或文件已删除，请检查路径：\(backgroundImagePath)") // 文件不存在
            }
        } else {
            print("backgroundImagePath 为空，未保存图片") // 路径为空
        }
        return nil
    }
    // 发送请求获取课表数据
    // 1. 统一触发课程流程的入口函数（直接请求接口获取已解析的课程数据）
    func startCourseFlow(completion: @escaping () -> Void = {}) {
        isLoading = true
        errorMessage = nil
        
        // 直接请求接口获取已解析的课程数据
        fetchCoursesDirectly {
            print("✅ 课程流程结束（直接获取）")
            self.isLoading = false
            self.hasLoadedOnce = true  // 标记已加载过
            // 确保课程数据已保存
            if !self.courses.isEmpty {
                print("✅ 课程数据已保存，共 \(self.courses.count) 门课程")
            } else {
                print("⚠️ 警告：课程数据为空")
            }
            completion()
        }
    }
    private func getSectionRange(startSecion:Int) -> (start: Int, end: Int) {
        switch startSecion {
        case 1: return (1, 2)    // 第1单元格：1-2节
        case 2: return (3, 4)    // 第2单元格：3-4节（三四节）
        case 3: return (5, 6)    // 第3单元格：5-6节
        case 4: return (7, 8)    // 第4单元格：7-8节
        case 5: return (9, 10)   // 第5单元格：9-10节
        case 6: return (11, 12)  // 第6单元格：11-12节
        default: return (0, 0)
        }
    }
    //刷新认证信息
    // 2. 直接获取已解析的课程数据（不再需要两步流程）
    private func fetchCoursesDirectly(completion: @escaping () -> Void = {}) {
        self.errorMessage = nil
        // 检查登录状态
//        guard !self.authData.isEmpty else {
//            self.isLoading = false
//            self.errorMessage = "未登录，请重新登录"
//            completion()
//            return
//        }
        // 检查 cookie 是否存在
        guard !self.cookie.isEmpty else {
            self.isLoading = false
            self.errorMessage = "登录凭证（cookie）缺失，无法获取课程数据"
            completion()
            return
        }
        
        // 配置 URL（直接请求解析接口，返回已解析的课程数据）
        guard let url = URL(string: "\(BasicValue.baseGetUrl)\(Identify.chooseIdentify)\(Method.getCoursePage)?term=2025-2026-1") else {
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
//        // 添加 Authorization 头
//        if !self.authData.isEmpty {
//            request.setValue("Wuster \(self.authData)", forHTTPHeaderField: "Authorization")
//        }
//        request.addValue(self.cookie, forHTTPHeaderField: "Cookie")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        // 发送请求
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                // 处理网络错误
                if let error = error {
                    self.errorMessage = "网络错误：\(error.localizedDescription)"
                    completion()
                    return
                }
                
                // 先获取httpResponse，再判断状态码
                let httpResponse = response as? HTTPURLResponse
                guard let validResponse = httpResponse, (200...299).contains(validResponse.statusCode) else {
                    let statusCode = httpResponse?.statusCode ?? -1
                    self.errorMessage = "服务器响应错误（状态码：\(statusCode)）"
                    completion()
                    return
                }
                
                // 处理无数据
                guard let responseData = data else {
                    self.errorMessage = "获取课程数据无返回数据"
                    completion()
                    return
                }
                
                let decoder = JSONDecoder()
                // 第一次解析：处理通用错误
                if let responseSuccess = try? decoder.decode(Response.self, from: responseData) {
                    if responseSuccess.code != 200 {
                        self.errorMessage = responseSuccess.message == "Cookie无效(100101)"
                        ? "登录失效 请尝试重新登录"
                        : "获取课程数据失败: \(responseSuccess.message)"
                        completion()
                        return
                    }
                }
                
                // 直接解析为 CourseResponse（已解析的格式）
                do {
                    let courseResponse = try decoder.decode(CourseResponse.self, from: responseData)
                    if courseResponse.code == 200 {
                        self.courses = courseResponse.data
                        self.saveCourses(self.courses)
                        print("✅ fetchCoursesDirectly 成功，课程数据已保存")
                    } else {
                        self.errorMessage = "获取课程数据失败: \(courseResponse.message)"
                    }
                } catch {
                    self.errorMessage = "解析课程数据失败: \(error.localizedDescription)"
                    // 调试：打印原始数据
                    if let dataStr = String(data: responseData, encoding: .utf8) {
                        print("❌ fetchCoursesDirectly 解析失败，原始数据：\(dataStr)")
                    }
                }
                completion()
            }
        }
        task.resume()
    }
    // MARK: - 研究生课程获取
    /// 获取研究生课程（使用新的研究生管理系统API）
    private func fetchGraduateCourses(completion: @escaping () -> Void = {}) {
        // isLoading 由调用者统一管理（在loadCourses中）
        self.errorMessage = nil
        // 检查登录凭证
        guard !username.isEmpty && !password.isEmpty else {
            self.isLoading = false
            self.errorMessage = "未登录或学校服务器异常,不影响主体功能🤔，可耐心等待或重新登录"
            completion()
            return
        }
        
        // 使用研究生网络服务获取课程
        let credentials = GraduateLoginCredentials(studentId: username, password: password)
        
        GraduateNetworkService.fetchCourseSchedule(credentials: credentials) { result in
            DispatchQueue.main.async {
            switch result {
            case .success(let graduateCourses):
                // 将研究生课程格式转换为通用Course格式
                let convertedCourses = graduateCourses.map { graduateCourse in
                    Course(
                        name: graduateCourse.name,
                        teacher: graduateCourse.teacher,
                        teachClass: graduateCourse.teachClass,
                        startWeek: graduateCourse.startWeek,
                        endWeek: graduateCourse.endWeek,
                        weekDay: graduateCourse.weekDay,
                        startSection: graduateCourse.startSection,
                        endSection: graduateCourse.startSection+1,
                        classroom: graduateCourse.classroom
                    )
                }
                self.courses = convertedCourses
                self.saveCourses(convertedCourses)
                    self.isLoading = false
                print("✅ 研究生课程获取成功，共 \(convertedCourses.count) 门课程")
                completion()
            case .failure(let error):
                self.errorMessage = error.localizedDescription
                self.isLoading = false
                print("❌ 研究生课程获取失败: \(error.localizedDescription)")
                completion()
            }
        }
    }
    }
    // 3. ParseCourses（移除weak + 修复httpResponse作用域）
    private func ParseCourses(completion: @escaping () -> Void = {}) {
        // 不再在这里设置isLoading，由startCourseFlow统一管理
        self.errorMessage = nil
        // 1. 新增：检查 webpage 是否为空（避免空参数导致 400）
        guard !webPage.isEmpty else {
            self.isLoading = false
            self.errorMessage = "webpage 参数为空，无法发起解析请求"
            completion()
            return
        }
        // 检查登录状态
        guard !self.authData.isEmpty else {
            self.isLoading = false
            self.errorMessage = "未登录，请重新登录"
            completion()
            return
        }
        // 1. 根据用户类型（本科/研究生）确定正确的接口URL
        let urlString: String
        if Identify.chooseIdentify == Identify.Graduate {
            // 研究生登录接口
            urlString = "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.getCourses)"
        } else {
            // 本科生登录接口（默认）
            urlString = "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.getCourses)"
        }
        
        // 配置 URL（移除冗余的 encodedCoursesPage）
        guard let url = URL(string: "\(BasicValue.baseParseUrl)\(Identify.chooseParseIdentify)\(Method.getCourses)") else {
            self.isLoading = false
            self.errorMessage = "接口地址无效"
            completion()
            return
        }
        // 配置请求
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        // 声明请求体格式和期望响应格式
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        // 新增：添加 Authorization 头（如果接口需要，根据实际情况调整）
        if !self.authData.isEmpty {
            request.setValue("Wuster \(self.authData)", forHTTPHeaderField: "Authorization")
        }
        request.timeoutInterval = 15
        // 2. 修复：将 requestBody 转换为 JSON 并赋值给 httpBody（之前遗漏！）
        let requestBody: [String: String] = ["webpage": self.webPage]
        guard let jsonData = try? JSONSerialization.data(withJSONObject: requestBody, options: []) else {
            self.isLoading = false
            self.errorMessage = "请求体 JSON 格式转换失败"
            completion()
            return
        }
        request.httpBody = jsonData // 关键：赋值请求体
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                // 不再在这里设置isLoading = false，统一在startCourseFlow的completion中设置
                // 4. 新增：打印【原始响应信息】
                print("\n📥 收到响应：")
                // 打印响应状态码和响应头
                if let httpResponse = response as? HTTPURLResponse {
                    print("响应状态码: \(httpResponse.statusCode)")
                    print("响应头:")
                    httpResponse.allHeaderFields.forEach { key, value in
                        print("  \(key): \(value)")
                    }
                } else {
                    print("响应: 非 HTTP 响应")
                }
                // 打印原始响应数据（无论成功失败，便于定位）
                if let responseData = data {
                    if let responseStr = String(data: responseData, encoding: .utf8) {
                        print("原始响应数据: \(responseStr)")
                    } else {
                        print("原始响应数据: 无法转换为字符串（非 UTF-8 编码），数据长度: \(responseData.count) 字节")
                    }
                } else {
                    print("原始响应数据: 无数据")
                }
                
                // 处理网络错误
                if let error = error {
                    self.errorMessage = "网络错误：\(error.localizedDescription)"
                    completion()
                    return
                }
                
                // 处理非 200-299 状态码（如 400）
                let httpResponse = response as? HTTPURLResponse
                guard let validResponse = httpResponse, (200...299).contains(validResponse.statusCode) else {
                    let statusCode = httpResponse?.statusCode ?? -1
                    self.errorMessage = "服务器响应错误（状态码：\(statusCode)）"
                    completion()
                    return
                }
                
                // 处理无响应数据
                guard let responseData = data else {
                    self.errorMessage = "解析课程无返回数据"
                    completion()
                    return
                }
                
                let decoder = JSONDecoder()
                // 第一次解析：处理通用错误
                if let responseSuccess = try? decoder.decode(Response.self, from: responseData) {
                    if responseSuccess.code != 200 {
                        self.errorMessage = responseSuccess.message == "Cookie无效(100101)"
                        ? "登录失效 请尝试重新登录"
                        : "解析课程失败: \(responseSuccess.message)"
                        completion()
                        return
                    }
                }
                // 第二次解析：获取最终课程数据
                do {
                    let courseResponse = try decoder.decode(CourseResponse.self, from: responseData)
                    if courseResponse.code == 200 {
                        self.courses = courseResponse.data
                        self.saveCourses(self.courses)
                        print("✅ ParseCourses 成功，课程数据已保存")
                    } else {
                        self.errorMessage = "解析课程失败: \(courseResponse.message)"
                    }
                } catch {
                    self.errorMessage = "解析课程数据失败: \(error.localizedDescription)"
                    // 调试：打印原始数据（已在上方统一打印，此处可保留或删除）
                    if let dataStr = String(data: responseData, encoding: .utf8) {
                        print("❌ ParseCourses 解析失败，原始数据：\(dataStr)")
                    }
                }
                
                completion()
            }
        }
        task.resume()
    }
    // 发送请求获取本学期日期数据
    private func getFirstDay(completion: @escaping () -> Void = {}) {
        // 不再在这里设置isLoading，由调用者统一管理
        errorMessage = nil
        // 1. 检查authData是否存在
//        guard !authData.isEmpty else {
//            isLoading = false
//            errorMessage = "未登录，请重新登录"
//            completion() // 补全回调，避免流程卡住
//             //可选：跳转回登录界面
////             showLoginView = true
//            return
//        }
        // 检查 cookie 是否存在
        guard !cookie.isEmpty else {
            isLoading = false
            errorMessage = "登录凭证（cookie）缺失，无法获取起始日数据"
            completion()
            return
        }
        // 2. 配置请求URL（使用 baseGetUrl + chooseIdentify 格式）
        // 验证URL有效性（统一处理）
        guard let url = URL(string: "\(BasicValue.baseGetUrl)/UnderGraduate/Support\(Method.getData)") else {
            isLoading = false
            errorMessage = "接口地址无效"
            completion()
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 15
        // 3. 配置请求
        // 添加 Cookie 头
        request.addValue(cookie, forHTTPHeaderField: "Cookie")
        // 添加 Authorization 头（如果需要）
        if !authData.isEmpty {
            request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        }
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        // 4. 发送请求
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                // 不再在这里设置isLoading，由调用者统一管理（但错误时需要设置）
                if let error = error {
                    isLoading = false
                    errorMessage = error.localizedDescription
                    completion()
                    return
                }
                // 验证HTTP响应
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    isLoading = false
                    errorMessage = "服务器响应错误"
                    completion()
                    return
                }
                
                // 解析数据
                guard let data = data else {
                    isLoading = false
                    errorMessage = "无返回数据"
                    completion()
                    return
                }
                let decoder = JSONDecoder()
                // 第一次解析：尝试解析为Response类型
                if let responseSuccess = try? decoder.decode(Response.self, from: data) {
                    // 处理Response类型的解析结果
                    if responseSuccess.code != 200 {
                        isLoading = false
                        if responseSuccess.message == "Cookie无效(100101)" {
                            errorMessage = "登录失效 请尝试重新登录"
                        } else {
                            errorMessage = "获取失败: \(responseSuccess.message)"
                        }
                        completion()
                        return
                    }
                }
                // 第二次解析：解析为CourseResponse类型
                do {
                    let FirstDataResponse = try decoder.decode(FirstDateResponse.self, from: data)
                    if FirstDataResponse.code == 200 {
                        firstYear=FirstDataResponse.data.year
                        firstMonth=FirstDataResponse.data.month
                        firstDay=FirstDataResponse.data.day
                        print("本学期起始日：\(FirstDataResponse.data.year) \(FirstDataResponse.data.month) \(FirstDataResponse.data.day)")
                        completion() // 添加完成回调
                    } else {
                        isLoading = false
                        errorMessage = FirstDataResponse.message
                        completion()
                    }
                } catch {
                    // 捕获第二次解析的错误
                    isLoading = false
                    errorMessage = "获取本学期起始日失败: \(error.localizedDescription)"
                    completion() // 即使出错也要调用完成回调
                }
                
            }
        }
        task.resume()
    }
    // 获取当前周的主要月份（显示出现次数最多的月份）
    private var currentMonth: String {
        guard !weekFullDates.isEmpty else { return "0" }
        // 统计当前周各月份出现的次数
        var monthCount: [Int: Int] = [:]
        for date in weekFullDates {
            let month = Calendar.current.component(.month, from: date)
            monthCount[month] = (monthCount[month] ?? 0) + 1
        }
        // 取出现次数最多的月份
        return monthCount.max { $0.value < $1.value }?.key.description ?? "0"
    }
    @State var selectPictureView=false
    
    // MARK: - Navigation Bar Components
    private var leadingNavigationBar: some View {
        HStack {
            Button {
                // 获取当前时间
                let currentTime = Date()
                // 如果距离上次点击小于2秒，不响应点击
                if currentTime.timeIntervalSince(lastClickTime) < 1 {
                    return
                }
                // 更新上次点击时间
                lastClickTime = currentTime
                // 1️⃣ 先触发 ScrollView 滚动到顶部
                scrollToTopTrigger.toggle()  // 如果是 Bool 类型，每次点击切换即可触发 onChange
                // 执行点击逻辑
                withAnimation(.easeInOut(duration: 0.5)) {
                    ischoose.toggle()  // 切换选择状态，控制 Picker 显示
                }
                // 延迟 0.3 秒（动画结束后）再更新 weekdate
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                    weeknumber = thisweek
                }
            } label: {
                Text("第\(thisweek)周")
                    .font(.largeTitle)
                    .foregroundColor(titleColor)
                Image(systemName: "control")
                    .resizable()
                    .frame(width: 18, height: 10)
                    .foregroundColor(titleColor)
                    .rotationEffect(.degrees(180)) // 上下翻转
            }
        }
    }
    
    private var trailingNavigationBar: some View {
        HStack {
            Button {
                showScanner = true  // 点击打开扫码视图
            } label: {
                Image(systemName: "qrcode.viewfinder")
                    .resizable() .foregroundColor(titleColor)
                    .frame(width: 30, height: 30)
            }
            Button {
                ShowAlert=true
            } label: {
                Image(systemName: "arrow.clockwise")
                    .resizable()
                    .foregroundColor(titleColor).frame(width: 23, height: 23)
            }
            Menu {
                Button {
                    showBackGroudView=true
                } label: {
                    Text("设置背景图片").fixedSize()
                }
                Button {
                    IsQingShan.toggle()
                    // 同步到 App Group
                    syncDataToAppGroup()
                } label: {
                    if !IsQingShan{
                        Text("校区时切换 黄家湖✓ 青山").fixedSize()
                    }else{
                        Text("校区时切换 黄家湖  青山✓").fixedSize()
                    }
                }
                if(showIfNotThisWeek){
                    Button {
                        showIfNotThisWeek.toggle()
                    } label: {
                        Text("只显示当前周课程")
                    }
                }else{
                    Button {
                        showIfNotThisWeek.toggle()
                    } label: {
                        Text("显示非本周课程课程")
                    }
                }
                Button {
                    showLoverCourses.toggle()
                    // 切换时更新显示的课程数据
                    if showLoverCourses && loverCourses.isEmpty {
                        loadLoverCourses()
                    }
                } label: {
                    if showLoverCourses {
                        Text("切换回个人课表")
                    } else {
                        Text("显示情侣课表")
                    }
                }
                Button {
                    cleanQLKB=true
                } label: {
                    Text("清除情侣课表数据")
                }
                
                Button {
                    // 生成我的课表二维码
                    generateMyQRCode()
                } label: {
                    if isGeneratingQRCode {
                        ProgressView()
                            .scaleEffect(0.5)
                        Text("生成中...")
                    } else {
                        Text("生成我的课表二维码")
                    }
                }
                .disabled(isGeneratingQRCode) // 生成过程中禁用按钮
                
                Button {
                    // 导出课表为ICS格式
                    exportToICS()
                } label: {
                    Text("导出课表为ICS")
                }
                
            } label: {
                Image(systemName: "ellipsis")
                    .resizable()
                    .foregroundColor(titleColor)
                    .scaledToFit()
                    .rotationEffect(.degrees(90))
                    .frame(width: 20, height: 23)
            }
            .padding(.trailing, 20)
            .frame(width: 30)  // 限制菜单宽度
        }
    }
    
    // MARK: - Main Content View
    private var mainContentView: some View {
        VStack {
            // 使用 withAnimation 来平滑切换
            if ischoose {
                DetailWeek(weeknumber: $weeknumber, month: month, day: day, activeNumber: activeNumber, ischoose: ischoose, courses: displayedCourses)
                    .animation(.easeInOut(duration: 0.5), value: ischoose)
            }
            // 日期栏部分
            DataBar(currentMonth: $monthNUmber, weekNumber:$weeknumber,thisWeek:$thisweek,showBackGround: $showBackGroudn, day: day,whichWeekDay:$weekdayNum, weekdate:$weekdate)
                .padding(.top, 5)
            mainBar(isQingShan:$IsQingShan,weeknumber: $weeknumber, courses:  Binding(
                get: { self.displayedCourses },
                set: { newValue in
                    if self.showLoverCourses {
                        self.loverCourses = newValue
                        // 保存情侣课表更改
                        self.saveLoverCourses()
                    } else {
                        self.courses = newValue
                        self.saveCourses(self.courses)
                    }
                }
            ), activeNumber: $activeNumber, showCourseDetail: $showCourseDetail, showEditView: $showEditView, selectedCourse: $selectedCourse, showMultipleCoursesDetail: $showMultipleCoursesDetail, selectedCourses: $selectedCourses, selectedCellIndex: $selectedCellIndex, showBackGround: $showBackGroudn,scrollToTopTrigger:$scrollToTopTrigger)
        }
        .animation(.easeInOut(duration: 0.5), value: ischoose)  // 添加动画
        .background(
            cachedBackgroundImage?
                .resizable()
                .scaledToFill()
                .opacity(pictureOpacity)
        )
    }
    
    // MARK: - 多节课详情弹窗（横向滚动版本）
    private var multipleCoursesDetailView: some View {
        ZStack {
            // 背景遮罩
            Color.black.opacity(0.4)
                .ignoresSafeArea(.all)
                .onTapGesture {
                    withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
                        showMultipleCoursesDetail = false
                    }
                }
            
            // 弹窗内容
            VStack(spacing: 0) {
                // 顶部标题
                HStack {
                    Text("课程详情 (\(currentCoursePageIndex + 1)/\(selectedCourses.count))")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                    
                    Spacer()
                    
                    Button("关闭") {
                        withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
                            showMultipleCoursesDetail = false
                        }
                    }
                    .foregroundColor(.blue)
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                .padding(.bottom, 20)
                
                // 横向翻页的课程列表
                TabView(selection: $currentCoursePageIndex) {
                        ForEach(Array(selectedCourses.enumerated()), id: \.offset) { index, course in
                            CourseDetailCard(course: course, index: index + 1)
                            .tag(index)
                            .padding(.horizontal, 10)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never)) // 移除自动指示器，使用自定义指示器
                .frame(height: 400)
                .padding(.bottom, 8) // 增加底部间距，避免与指示器重叠
                
                // 自定义页面指示器（小圆点）
                HStack(spacing: 6) {
                    ForEach(0..<selectedCourses.count, id: \.self) { index in
                        Circle()
                            .fill(index == currentCoursePageIndex ? Color.blue : Color.gray.opacity(0.3))
                            .frame(width: 8, height: 8)
                            .animation(.easeInOut, value: currentCoursePageIndex)
                    }
                }
                .padding(.bottom, 20)
            }
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(.ultraThinMaterial)
                    .background(
                        RoundedRectangle(cornerRadius: 20)
                            .fill(Color(.systemBackground))
                    )
            )
            .shadow(color: Color.black.opacity(0.2), radius: 20, x: 0, y: 10)
            .padding(.horizontal, 20)
            .frame(maxHeight: UIScreen.main.bounds.height * 0.6) // 增加高度以容纳内容
            .transition(.asymmetric(
                insertion: .scale(scale: 0.8).combined(with: .opacity).combined(with: .move(edge: .bottom)),
                removal: .scale(scale: 0.7).combined(with: .opacity).combined(with: .move(edge: .bottom))
            ))
            .onAppear {
                // 重置到第一页
                currentCoursePageIndex = 0
            }
        }
        .zIndex(999)
    }
    // MARK: - 拆分：主视图内部内容（避免 ZStack 代码拥挤）
    private var loadingView: some View {
        ProgressView("加载课表中...")
    }

    private var mainContentWithNavItems: some View {
        mainContentView
            .navigationBarItems(leading: leadingNavigationBar, trailing: trailingNavigationBar)
    }

    private var courseDetailPopups: some View {
        Group {
            // 课程详情弹窗
            if let course = selectedCourse, showCourseDetail {
                CourseDetailPopup(
                    isPresented: $showCourseDetail,
                    course: course
                )
            }
            // 多节课详情弹窗
            if showMultipleCoursesDetail {
                multipleCoursesDetailView
            }
        }
    }
    private var rootZStack: some View {
        ZStack {
            Color("bgcolor").frame(maxWidth: .infinity, maxHeight: .infinity)
            // 加载状态 vs 主内容
            if isLoading && errorMessage == nil {
                loadingView
            } else {
                mainContentWithNavItems
            }
            // 各类弹窗（独立分组）
            courseDetailPopups
        } // 9. onAppear 初始化任务
        .onAppear {
            // 只在首次出现时执行初始化任务，避免每次切换都刷新
            if !hasLoadedOnce {
                initTask = Task {
                    // 1. 定义3个并行执行的异步任务
                    let getFirstDayTask = Task { getFirstDay() }
                    await getFirstDayTask.value
                    let updateDateTask = Task { updateCurrentDate() }
                    await updateDateTask.value
                    let loadCourse = Task { loadCourses() }
                    await loadCourse.value
                    let calculateWeekDate = Task { calculateWeekDates(for: thisweek) }
                    await calculateWeekDate.value
                    let loadImageTask = Task { cachedBackgroundImage = loadImageFromDocuments() }
                    await loadImageTask.value
                    monthNUmber = currentMonth  // 初始化当前月份（UI显示用）
                    weeknumber = thisweek  // 初始化当前周次（UI显示用）
                    // 4. 任务取消检查（避免无效操作）
                    guard !Task.isCancelled else { return }  // 若当前任务被取消（如视图消失），直接退出，不执行后续操作
                    
                    // 同步数据到 App Group（供小组件使用）
                    syncDataToAppGroup()
                }
            } else {
                // 已经加载过，只更新日期相关数据
                updateCurrentDate()
                calculateWeekDates(for: thisweek)
                monthNUmber = currentMonth
                weeknumber = thisweek
                
                // 同步数据到 App Group
                syncDataToAppGroup()
            }
        }
    }
    // MARK: - 拆分：复杂业务逻辑（登录、重新获取课程）
    /// 确认重新获取课程（对应第一个 alert 的确认按钮）
    private func confirmReloadCourses() {
        // 使用forceRefresh强制刷新
        loadCourses(forceRefresh: true)
        ShowAlert = false
    }
    /// 重试登录并获取课程（对应 error alert 的重试按钮）
    private func retryLoginAndFetchCourses() {
        // 使用forceRefresh强制刷新
        loadCourses(forceRefresh: true)
    }

    /// 处理扫码确认后的课程绑定逻辑
    private func handleScannedCourseBinding() {
        if let courses = decompressCoursesData(scannedCode) {
            do {
                let jsonData = try JSONEncoder().encode(courses)
                loverCoursesData = jsonData
                loverCourses = courses
                showLoverCourses = true
                print("成功绑定情侣课表，包含 \(courses.count) 门课程")
            } catch {
                errorMessage = "保存课表数据失败: \(error.localizedDescription)"
                showError = true
            }
        } else {
            errorMessage = "无法解析课表数据"
            showError = true
        }
        scannedCode = ""
    }
    // MARK: - 拆分：所有视图修饰符（alert、sheet、fullScreenCover 等）
    private func withAllModifiers(_ content: some View) -> some View {
        content
            // 1. 确认重新获取课程 alert
            .alert("确认", isPresented: $ShowAlert) {
                Button("取消", role: .cancel) {}
                Button("确认", role: .destructive) { confirmReloadCourses() }
            } message: {
                Text("确认重新获取课程？")
            }
            // 2. 清除情侣课表 alert
            .alert("确认", isPresented: $cleanQLKB) {
                Button("取消", role: .cancel) { cleanQLKB.toggle() }
                Button("确认", role: .destructive) {
                    loverCoursesData = Data()
                    loverCourses = []
                    cleanQLKB.toggle()
                }
            } message: {
                Text("确认清除情侣课表数据")
            }
            // 3. 编辑课程 sheet
            .sheet(isPresented: $showEditView) {
                if let course = selectedCourse {
                    EditView(
                        isPresented: $showEditView,
                        course: course,
                        onSave: { updatedCourse in
                            print("待更新课程ID：\(updatedCourse.id)")
                            if showLoverCourses {
                                // 情侣课表模式
                                if let index = loverCourses.firstIndex(where: { $0.id == updatedCourse.id }) {
                                    loverCourses[index] = updatedCourse
                                    print("已更新索引：\(index)，课程名：\(updatedCourse.name)")
                                } else {
                                    loverCourses.append(updatedCourse)
                                    print("新建课程ID\(updatedCourse.startSection)")
                                }
                                saveLoverCourses()
                            } else {
                                // 个人课表模式
                            if let index = courses.firstIndex(where: { $0.id == updatedCourse.id }) {
                                courses[index] = updatedCourse
                                print("已更新索引：\(index)，课程名：\(updatedCourse.name)")
                            } else {
                                courses.append(updatedCourse)
                                print("新建课程ID\(updatedCourse.startSection)")
                            }
                            saveCourses(courses)
                            }
                        },
                        onDelete: { deletedCourse in
                            if showLoverCourses {
                                loverCourses.removeAll { $0.id == deletedCourse.id }
                                saveLoverCourses()
                            } else {
                            courses.removeAll { $0.id == deletedCourse.id }
                            saveCourses(courses)
                            }
                            print("课程已删除：\(deletedCourse.name)")
                        }
                    )
                }
            }
            // 4. 选择背景图 sheet
            .sheet(isPresented: $showBackGroudView) {
                ChoosePictureView(
                    backgroundImagePath: $backgroundImagePath,
                    opacityNumber: $pictureOpacity,
                    showSignal: $showBackGroudView,
                    onSave: { _ in
                        cachedBackgroundImage = loadImageFromDocuments()
                    }
                )
            }
            // 5. 我的二维码 sheet
            .sheet(isPresented: $showMyCode) {
                qrCodeSheetContent
            }
            // 6. 扫码全屏视图
            .fullScreenCover(isPresented: $showScanner) {
                QRCodeScannerView(
                    isPresented: $showScanner,
                    scannedCode: $scannedCode
                )
                .ignoresSafeArea()
                .onDisappear {
                    if !scannedCode.isEmpty {
                        showCodeConfirm = true
                    }
                }
            }
            // 7. 扫码确认 alert
            .alert("扫描成功", isPresented: $showCodeConfirm) {
                Button("确认绑定") { handleScannedCourseBinding() }
                Button("取消", role: .cancel) { scannedCode = "" }
            } message: {
                Text("是否绑定此课表？")
            }
            // 8. 错误提示 alert
            .alert("出错了😫", isPresented: $showError) {
                Button("取消") { showError = false }
                Button("重试") { retryLoginAndFetchCourses() }
            } message: {
                VStack {
                    Text("加载失败：\(errorMessage ?? "未知错误")")
                        .foregroundColor(.red)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            }
           
            // 10. onDisappear 取消任务
            .onDisappear {
                initTask?.cancel()
                initTask = nil
            }
            // 11. 周数变化监听
            .onChange(of: thisweek) { _ in
                // 当 thisweek 变化时，同步到 App Group
                syncDataToAppGroup()
            }
            .onChange(of: weeknumber) { newWeek in
                calculateWeekDates(for: newWeek)
                monthNUmber = currentMonth
            }
            // 12. 错误信息变化监听
            .onChange(of: errorMessage) { newError in
                showError = newError != nil
            }
    }

    // 单独拆分二维码 sheet 内容（避免修饰符容器过于拥挤）
    private var qrCodeSheetContent: some View {
        VStack {
            if !qrCodeContent.isEmpty {
                Text("我的课表二维码")
                    .font(.headline)
                    .padding()
                QRCodeView(content: qrCodeContent, size: 300)
                    .padding()
            } else {
                ProgressView("生成二维码中...")
                    .padding()
            }
            Button("关闭") {
                showMyCode = false
            }
            .padding()
        }
    }
    var body: some View {
        HStack(alignment: .top) {
            NavigationView {
                // 对 rootZStack 应用修饰符，再放入 NavigationView
                withAllModifiers(rootZStack)
                    .navigationBarTitleDisplayMode(.inline)
                    .navigationTitle("")  // 设置空标题，但保留导航栏以显示按钮
            }
            .navigationViewStyle(StackNavigationViewStyle())  // 固定导航视图样式，避免在Tab切换时重建导致跳动
        }
    }
    // 添加数据压缩方法
    private func compressCoursesData(_ courses: [Course]) -> String {
        do {
            // 将课程数据转换为JSON
            let jsonData = try JSONEncoder().encode(courses)
            
            // 尝试压缩数据
            if let compressedData = try? (jsonData as NSData).compressed(using: .zlib) {
                return compressedData.base64EncodedString()
            }
            
            // 如果压缩失败，使用原始数据
            return jsonData.base64EncodedString()
        } catch {
            print("压缩课程数据失败: \(error)")
            return ""
        }
    }
    // 处理扫描到的 JSON 数据
        private func processScannedData(_ jsonString: String) {
            guard let jsonData = jsonString.data(using: .utf8) else {
                errorMessage = "扫描内容无法解析为有效的 JSON 文本。"
                showError = true
                return
            }
            
            do {
                let decoder = JSONDecoder()
                let newCourses = try decoder.decode([Course].self, from: jsonData)
                
                self.loverCourses = newCourses
                self.loverCoursesData = jsonData // 保存原始 JSON Data，方便持久化
                self.showLoverCourses = true
                
                print("成功导入 \(newCourses.count) 条情侣课表数据。")
                
            } catch {
                errorMessage = "解析课表数据失败，请确认二维码内容正确: \(error.localizedDescription)"
                showError = true
                print("解析错误: \(error)")
            }
            
            // 清除扫描结果
            self.scannedJSON = ""
        }
    // 添加加载情侣课表的方法
    private func loadLoverCourses() {
        guard !loverCoursesData.isEmpty else {
            errorMessage = "暂无情侣课表数据"
            showError = true
            showLoverCourses = false // 切换回个人课表
            return
        }
        
        do {
            let decoder = JSONDecoder()
            loverCourses = try decoder.decode([Course].self, from: loverCoursesData)
        } catch {
            errorMessage = "解析情侣课表失败: \(error.localizedDescription)"
            showError = true
            showLoverCourses = false
        }
    }
    
    // 修改生成二维码的方法，使用异步处理
    private func generateMyQRCode() {
        isGeneratingQRCode = true
        
        DispatchQueue.global(qos: .userInitiated).async {
            // 压缩课程数据
            let compressedContent = self.compressCoursesData(self.courses)
            
            DispatchQueue.main.async {
                self.qrCodeContent = compressedContent
                self.isGeneratingQRCode = false
                self.showMyCode = true
                print("压缩后的二维码内容长度: \(compressedContent.count)")
            }
        }
    }
    
    // MARK: - 导出ICS功能
    private func exportToICS() {
        let icsContent = generateICSContent()
        // 创建临时文件
        let fileName = "课表_\(Date().formatted(.dateTime.year().month().day()))"
        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent("\(fileName).ics")
        
        do {
            try icsContent.write(to: tempURL, atomically: true, encoding: .utf8)
            
            // 显示分享界面
            let activityVC = UIActivityViewController(activityItems: [tempURL], applicationActivities: nil)
            
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let window = windowScene.windows.first {
                window.rootViewController?.present(activityVC, animated: true)
            }
        } catch {
            print("导出ICS文件失败: \(error)")
        }
    }
    
    private func generateICSContent() -> String {
        var icsContent = """
        BEGIN:VCALENDAR
        VERSION:2.0
        PRODID:-//武科大助手//课表导出//CN
        CALSCALE:GREGORIAN
        METHOD:PUBLISH
        X-WR-CALNAME:武科大课表
        X-WR-CALDESC:武汉科技大学课程表
        X-WR-TIMEZONE:Asia/Shanghai
        """
        
        // 获取当前学期开始日期（这里需要根据实际情况调整）
        let semesterStartDate = getSemesterStartDate()
        
        // 根据校区模式选择对应的时间段配置
        let courseTimeSlots: [CourseTime] = IsQingShan ? [
            CourseTime(start: "08:00", end: "09:40"),   // 第1-2节（青山校区）
            CourseTime(start: "10:10", end: "11:50"),   // 第3-4节（青山校区）
            CourseTime(start: "14:00", end: "15:40"),   // 第5-6节
            CourseTime(start: "16:00", end: "17:40"),   // 第7-8节
            CourseTime(start: "18:40", end: "20:20"),   // 第9-10节
            CourseTime(start: "20:30", end: "22:10")    // 第11-12节
        ] : [
            CourseTime(start: "08:20", end: "10:00"),   // 第1-2节（黄家湖校区）
            CourseTime(start: "10:20", end: "12:00"),   // 第3-4节（黄家湖校区）
            CourseTime(start: "14:00", end: "15:40"),   // 第5-6节
            CourseTime(start: "16:00", end: "17:40"),   // 第7-8节
            CourseTime(start: "18:40", end: "20:20"),   // 第9-10节
            CourseTime(start: "20:30", end: "22:10")    // 第11-12节
        ]
        
        for course in courses {
            // 为每个课程生成多个事件（根据周数）
            for week in course.startWeek...course.endWeek {
                let eventDate = getDateForWeekAndDay(week: week, weekday: course.weekDay, semesterStart: semesterStartDate)
                
                // 根据课程节次获取对应的时间段
                let timeSlot = getCourseTimeSlot(startSection: course.startSection, endSection: course.endSection, timeSlots: courseTimeSlots)
                
                let startDateTime = formatDateTime(date: eventDate, time: timeSlot.start)
                let endDateTime = formatDateTime(date: eventDate, time: timeSlot.end)
                
                icsContent += """
                
                BEGIN:VEVENT
                UID:\(UUID().uuidString)@wust.edu.cn
                DTSTART:\(startDateTime)
                DTEND:\(endDateTime)
                SUMMARY:\(course.name)
                DESCRIPTION:教师: \(course.teacher)\\n班级: \(course.teachClass)\\n教室: \(course.classroom)\\n第\(week)周
                LOCATION:\(course.classroom)
                RRULE:FREQ=WEEKLY;COUNT=1
                END:VEVENT
                """
            }
        }
        icsContent += "\nEND:VCALENDAR"
        return icsContent
    }
    
    // 根据课程节次获取对应的时间段
    private func getCourseTimeSlot(startSection: Int, endSection: Int, timeSlots: [CourseTime]) -> CourseTime {
        // 节次到时间段索引的映射
        let sectionToSlotMap: [Int: Int] = [
            1: 0, 2: 0,    // 第1-2节 -> 时间段0
            3: 1, 4: 1,    // 第3-4节 -> 时间段1
            5: 2, 6: 2,    // 第5-6节 -> 时间段2
            7: 3, 8: 3,    // 第7-8节 -> 时间段3
            9: 4, 10: 4,   // 第9-10节 -> 时间段4
            11: 5, 12: 5   // 第11-12节 -> 时间段5
        ]
        
        // 获取开始节次对应的时间段索引
        guard let slotIndex = sectionToSlotMap[startSection],
              slotIndex < timeSlots.count else {
            // 如果找不到对应的时间段，根据校区返回默认时间段
            let defaultTime = IsQingShan ? 
                CourseTime(start: "08:00", end: "09:40") : 
                CourseTime(start: "08:20", end: "10:00")
            return defaultTime
        }
        
        return timeSlots[slotIndex]
    }
    
    private func getSemesterStartDate() -> Date {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.timeZone = TimeZone(identifier: "Asia/Shanghai")
        
        // 直接使用远程获取的日期数据
        let dateString = String(format: "%d-%02d-%02d", firstYear, firstMonth, firstDay)
        let semesterStartDate = formatter.date(from: dateString) ?? Date()
        
        return semesterStartDate
    }
    
    
    private func getDateForWeekAndDay(week: Int, weekday: Int, semesterStart: Date) -> Date {
        let calendar = Calendar.current
        let daysToAdd = (week - 1) * 7 + (weekday - 1)
        return calendar.date(byAdding: .day, value: daysToAdd, to: semesterStart) ?? Date()
    }
    
    private func getTimeForSection(_ section: Int) -> String {
        // 根据节次返回时间
        let timeMap: [Int: String] = [
            1: "08:20", 2: "08:50", 3: "10:00", 4: "10:20",
            5: "12:00", 6: "14:00", 7: "14:50", 8: "15:40",
            9: "16:40", 10: "17:40", 11: "18:40", 12: "20:20"
        ]
        return timeMap[section] ?? "08:20"
    }
    
    private func formatDateTime(date: Date, time: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyyMMdd"
        formatter.timeZone = TimeZone(identifier: "Asia/Shanghai")
        
        let dateString = formatter.string(from: date)
        return "\(dateString)T\(time.replacingOccurrences(of: ":", with: ""))00"
    }
    
    private func decompressCoursesData(_ base64String: String) -> [Course]? {
        guard let compressedData = Data(base64Encoded: base64String) else {
            return nil
        }
        
        do {
            // 尝试解压缩数据
            if let decompressedData = try? (compressedData as NSData).decompressed(using: .zlib) {
                return try JSONDecoder().decode([Course].self, from: decompressedData as Data)
            }
            
            // 如果解压缩失败，尝试直接解析
            return try JSONDecoder().decode([Course].self, from: compressedData)
        } catch {
            print("解压缩课程数据失败: \(error)")
            return nil
        }
    }
    // 1. 声明存储“星期数字”的变量（类/结构体内部）
    @AppStorage("weepdauNum") var weekdayNum: Int = 1 // 默认值设为1（对应周一，符合日常认知）
    /// 专门方法：基于 firstYear/firstMonth/firstDay 基准日期，更新 thisweek 为当前周数
    private func updateThisweekToCurrent() {
        // 1. 配置上海时区（保持与其他日期计算逻辑一致）
        guard let shanghaiTimeZone = TimeZone(identifier: "Asia/Shanghai") else {
            print("⚠️ 上海时区初始化失败，thisweek 保持默认值 1")
            thisweek = 1
            return
        }
        // 2. 配置日历：周起点为“周一”（关键：国内周定义）
        var calendar = Calendar.current
        calendar.timeZone = shanghaiTimeZone
        calendar.firstWeekday = 2 // 2=周一（必须设置，否则周起点会混乱）
        
        // 3. 构建“学期第1周周一”（基准日期，清除时分秒）
        var baseDateComponents = DateComponents()
        baseDateComponents.year = firstYear
        baseDateComponents.month = firstMonth
        baseDateComponents.day = firstDay
        baseDateComponents.hour = 0
        baseDateComponents.minute = 0
        baseDateComponents.second = 0
        baseDateComponents.timeZone = shanghaiTimeZone
        guard let baseDate = calendar.date(from: baseDateComponents) else {
            print("⚠️ 基准日期构建失败，thisweek 保持默认值 1")
            thisweek = 1
            return
        }
        
        // 4. 处理当前日期：清除时分秒（确保与基准日期“时间对齐”）
        let currentDate = Date()
        let currentDateComponents = calendar.dateComponents([.year, .month, .day], from: currentDate)
        guard let currentDateWithoutTime = calendar.date(from: currentDateComponents) else {
            print("⚠️ 当前日期处理失败，thisweek 保持默认值 1")
            thisweek = 1
            return
        }
        
        // 5. 修复核心：计算“当前日期所在周的周一”（重点修正周日逻辑）
        let currentWeekday = calendar.component(.weekday, from: currentDateWithoutTime)
        var daysToMonday: Int
        if currentWeekday == 1 { // 周日（系统weekday=1）→ 向前偏移6天到本周一
            daysToMonday = -6
        } else { // 周一~周六（系统weekday=2~7）→ 向前偏移到本周一
            daysToMonday = -(currentWeekday - 2)
        }
        // 根据偏移量得到“当前周周一”
        guard let currentWeekMonday = calendar.date(
            byAdding: .day,
            value: daysToMonday,
            to: currentDateWithoutTime
        ) else {
            print("⚠️ 当前周周一计算失败，thisweek 保持默认值 1")
            thisweek = 1
            return
        }
        
        // 6. 计算周偏移量（当前周周一 与 基准日期的周数差）
        guard let weekOffset = calendar.dateComponents(
            [.weekOfYear],
            from: baseDate,
            to: currentWeekMonday
        ).weekOfYear else {
            print("⚠️ 周偏移量计算失败，thisweek 保持默认值 1")
            thisweek = 1
            return
        }
        
        // 7. 最终周数（偏移量+1，因为基准是第1周），避免周数为0或负数
        let currentWeekNumber = max(1, weekOffset + 1)
        thisweek = currentWeekNumber
        
        // 同步到 App Group（供小组件使用）
        syncDataToAppGroup()
        
        print("✅ 周数更新完成：当前周=\(thisweek)，当前周周一=\(currentWeekMonday)")
    }
    // 修改课程显示逻辑，根据showLoverCourses决定显示哪个课表
    private var displayedCourses: [Course] {
        showLoverCourses ? loverCourses : courses
    }
    // 添加保存情侣课表的方法
    private func saveLoverCourses() {
        do {
            let encoder = JSONEncoder()
            let encodedData = try encoder.encode(loverCourses)
            loverCoursesData = encodedData
        } catch {
            errorMessage = "保存情侣课表失败: \(error.localizedDescription)"
            showError = true
        }
    }
    private func updateCurrentDate() {
        let currentDate = Date()
        let calendar = Calendar.current
        // 原有逻辑：获取月份、日期
        month = calendar.component(.month, from: currentDate)
        day = calendar.component(.day, from: currentDate)
        // 新增：计算星期数字（周一=1，周二=2…周日=7）
        let systemWeekday = calendar.component(.weekday, from: currentDate)
        // 系统默认规则：1=周日，2=周一，3=周二，4=周三，5=周四，6=周五，7=周六
        if systemWeekday == 1 {
            // 特殊处理：系统“周日（1）”→ 自定义“7”
            weekdayNum = 7
        } else {
            // 其他情况：系统值-1（如2=周一→1，3=周二→2…7=周六→6）
            weekdayNum = systemWeekday - 1
        }
        updateThisweekToCurrent()
    }
    // 计算指定周的完整日期（包含月份）
    private func calculateWeekDates(for weekNumber: Int) {
        // 创建上海时区
        let shanghaiTimeZone = TimeZone(identifier: "Asia/Shanghai")!
        // 创建使用上海时区的日历
        var calendar = Calendar.current
        calendar.timeZone = shanghaiTimeZone
        // 1. 设置基准日期（第1周周一，根据实际学期调整）
        var baseDateComponents = DateComponents()
        baseDateComponents.year = firstYear
        baseDateComponents.month = firstMonth
        baseDateComponents.day = firstDay
        baseDateComponents.hour = 0
        baseDateComponents.minute = 0
        baseDateComponents.second = 0
        baseDateComponents.timeZone = shanghaiTimeZone
        
        guard let baseDate = calendar.date(from: baseDateComponents) else {
            weekdate = Array(repeating: "0/0", count: 7)
            weekFullDates = []
            return
        }
        
        // 2. 计算目标周周一日期
        let weekOffset = weekNumber - 1
        guard let targetWeekMonday = calendar.date(
            byAdding: .weekOfYear,
            value: weekOffset,
            to: baseDate
        ) else {
            weekdate = Array(repeating: "0/0", count: 7)
            weekFullDates = []
            return
        }
        print("targetWeekMonday: \(targetWeekMonday)")
        // 3. 计算周一到周日的完整日期
        var fullDates: [Date] = []
        var dateStrings: [String] = []
        let formatter = DateFormatter()
        formatter.dateFormat = "M/d"  // 格式化为"月/日"
        formatter.timeZone = shanghaiTimeZone
        
        for dayOffset in 0..<7 {
            guard let date = calendar.date(
                byAdding: .day,
                value: dayOffset,
                to: targetWeekMonday
            ) else {
                fullDates.append(Date())
                dateStrings.append("0/0")
                continue
            }
            fullDates.append(date)
            dateStrings.append(formatter.string(from: date))
        }
        weekFullDates = fullDates
        weekdate = dateStrings
    }
    // 查找指定位置的课程
    private func findCourse(for weekDay: Int, section: Int) -> Course? {
        return courses.first { course in
            // 检查课程是否在当前周显示
            let isCurrentWeek = weeknumber >= course.startWeek && weeknumber <= course.endWeek
            // 检查是否是当前星期和节次
            return isCurrentWeek && course.weekDay == weekDay && course.startSection <= section && course.endSection >= section
        }
    }
    
    // 查找指定位置的所有课程（用于多节课显示）
    private func findAllCourses(for weekDay: Int, section: Int) -> [Course] {
        return courses.filter { course in
            // 检查课程是否在当前周显示
            let isCurrentWeek = weeknumber >= course.startWeek && weeknumber <= course.endWeek
            // 检查是否是当前星期和节次
            return isCurrentWeek && course.weekDay == weekDay && course.startSection <= section && course.endSection >= section
        }
    }
}

// MARK: - 课程详情卡片
struct CourseDetailCard: View {
    let course: Course
    let index: Int
    @Environment(\.colorScheme) var colorScheme
    
    private let courseColors: [Color] = [
        Color(red: 0.89, green: 0.36, blue: 0.59), // 莓果玫红
        Color(red: 0.18, green: 0.75, blue: 0.65), // 都市青绿
        Color(red: 0.98, green: 0.61, blue: 0.38), // 蜜橙橘
        Color(red: 0.30, green: 0.72, blue: 0.88), // 冰湖蓝
        Color(red: 0.54, green: 0.48, blue: 0.94), // 潮流紫
        Color(red: 0.96, green: 0.48, blue: 0.45), // 番茄红
    ]
    
    private var courseColor: Color {
        let colorIndex = course.name.hashValue % courseColors.count
        return courseColors[abs(colorIndex)]
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // 课程标题
            Text("\(index). \(course.name)")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            // 课程信息
            VStack(alignment: .leading, spacing: 6) {
                InfoRow(icon: "person.fill", label: "教师", value: course.teacher)
                InfoRow(icon: "building.2.fill", label: "教室", value: course.classroom)
                InfoRow(icon: "graduationcap.fill", label: "班级", value: course.teachClass)
                InfoRow(icon: "calendar", label: "周次", value: "第\(course.startWeek)-\(course.endWeek)周")
                InfoRow(icon: "clock.fill", label: "时间", value: "第\(course.startSection)-\(course.endSection)节")
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(colorScheme == .dark ? Color(.systemGray6) : Color(.systemBackground))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(courseColor.opacity(0.3), lineWidth: 1)
                )
        )
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

#Preview {
    courseframe()
}
