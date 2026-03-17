//
//  ImageLoadView.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/9/5.
//
import SwiftUI
import UIKit
import Combine

// MARK: - 1. 新增：图片详情响应模型（匹配接口返回结构）
/// 图片详情数据模型
struct PictureDetailData: Codable {
    let pid: Int          // 接口定义为integer($int64)，用Int64适配
    let url: String         // 真实图片访问URL
    let status: Int         // 图片状态（如0=待审核/1=审核通过）
    let ifdelete: Int       // 是否删除（0=未删/1=已删）
    let createdId: Int64    // 上传者ID
    let uploadTime: String  // 上传时间（ISO格式）
    let statusUpdatedTime: String? // 状态更新时间
}

/// 图片详情接口整体响应模型
struct PictureDetailResponse: Codable {
    let code: Int
    let message: String
    let data: PictureDetailData? // 详情数据（成功时返回）
    let timestamp: TimeInterval
}
// MARK: - 2. 图片上传视图（含详情展示）
struct ImageUploadHeightView: View {
    @Binding var pictureId:Int?
    @StateObject private var viewModel = ImageUploadViewModel()
    @State private var showingImagePicker = false
    @State private var isUploadAreaTapped = false // 上传区域点击动画
    var body: some View {
        ZStack(){
            ZStack {
                // 已选择/上传的图片预览
                if let image = viewModel.displayImage {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 330, height: 200)
                        .cornerRadius(10)
                        .shadow(radius: 3)
                        .clipped() // 防止图片拉伸变形
                } else {
                    // 未上传时的现代风格占位区
                    ZStack {
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(UIColor.systemBackground))
                            .frame(width: 330, height: 200)
                            .shadow(color: .black.opacity(0.08), radius: 6, x: 0, y: 2)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.blue.opacity(0.2), lineWidth: 1.5)
                            )
                        
                        VStack(spacing: 12) {
                            Image(systemName: "photo.on.rectangle.angled")
                                .font(.system(size: 36, weight: .light))
                                .foregroundStyle(
                                    LinearGradient(
                                        colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                        startPoint: .top,
                                        endPoint: .bottom
                                    )
                                )
                            // 加载状态提示（上传/查询详情）
                            if viewModel.isLoading || viewModel.isLoadingDetail {
                                ProgressView(viewModel.isLoading ? "上传中..." : "查询详情中...")
                                    .font(.caption)
                                    .foregroundColor(Color(UIColor.secondaryLabel))
                            } else {
                                Text("点击上传图片")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(Color(UIColor.secondaryLabel))
                                    .multilineTextAlignment(.center)
                            }
                        }
                    }
                }
                // 错误信息展示（上传/查询通用）
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(8)
                        .frame(maxWidth: .infinity)
                }
            }.overlay{
                // 圆形状态指示器（直接在 View 中写判断逻辑）
                Circle()
                    .fill(
                        // 1. 先处理 pictureDetail 为 nil 的情况（默认透明，避免崩溃）
                        viewModel.pictureDetail?.status == nil ? Color.clear :
                            // 2. 按 status 匹配对应颜色
                        viewModel.pictureDetail?.status == 0 ? Color.orange :
                            viewModel.pictureDetail?.status == 1 ? Color.green :
                            viewModel.pictureDetail?.status == 2 ? Color.red :
                            viewModel.pictureDetail?.status == 3 ? Color.gray :
                            // 3. 兜底颜色（status 为其他值时用浅灰）
                        Color.gray.opacity(0.5)
                    )
                    .frame(width: 20, height: 20)
                    .padding(.leading, 320)
                    .padding(.bottom, 200)
                // 优化：仅当有详情数据时显示，无数据时隐藏
                    .opacity(viewModel.pictureDetail != nil ? 1 : 0)
            }
            
            .onTapGesture {
                showingImagePicker = true
            }
            
            
            .simultaneousGesture(
                TapGesture()
                    .onEnded {
                        withAnimation(.easeInOut(duration: 0.15)) {
                            isUploadAreaTapped = true
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                                isUploadAreaTapped = false
                            }
                        }
                    }
            )
            
            .scaleEffect(isUploadAreaTapped ? 0.98 : 1.0)
            .sheet(isPresented: $showingImagePicker) {
                MyImagePicker(image: $viewModel.selectedImage)
            }
            
        }        .onReceive(viewModel.$uploadUrlId) { newValue in
            // 将 viewModel 中的值传递给父视图
            pictureId = newValue
        }
    }
    
    // MARK: - 辅助方法：状态文字转换（0/1转中文）
    private func statusText(_ status: Int) -> String {
        switch status {
        case 0: return "待审核"
        case 1: return "审核通过"
        case 2: return "审核驳回"
        default: return "未知状态"
        }
    }
    
    // MARK: - 辅助方法：状态颜色（通过=绿/待审=黄/驳回=红）
    private func statusColor(_ status: Int) -> Color {
        switch status {
        case 0: return .orange.opacity(0.8)
        case 1: return .green.opacity(0.8)
        case 2: return .red.opacity(0.8)
        default: return .gray
        }
    }
    
    // MARK: - 辅助方法：时间格式转换（ISO格式转本地时间）
    private func formatTime(_ isoTime: String) -> String {
        let formatter = ISO8601DateFormatter()
        if let date = formatter.date(from: isoTime) {
            let localFormatter = DateFormatter()
            localFormatter.dateFormat = "yyyy-MM-dd HH:mm"
            localFormatter.timeZone = TimeZone.current
            return localFormatter.string(from: date)
        }
        return isoTime // 转换失败则显示原始值
    }
}

// MARK: - 2. 图片上传视图（含详情展示）
struct ImageUploadView: View {
    @Binding var pictureId:Int?
    @StateObject private var viewModel = ImageUploadViewModel()
    @State private var showingImagePicker = false
    @State private var isUploadAreaTapped = false // 上传区域点击动画
    var body: some View {
        ZStack(){
            ZStack {
                // 已选择/上传的图片预览
                if let image = viewModel.displayImage {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 200, height: 200)
                        .cornerRadius(10)
                        .shadow(radius: 3)
                        .clipped() // 防止图片拉伸变形
                } else {
                    // 未上传时的现代风格占位区
                    ZStack {
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(UIColor.systemBackground))
                            .frame(width: 200, height: 200)
                            .shadow(color: .black.opacity(0.08), radius: 6, x: 0, y: 2)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.blue.opacity(0.2), lineWidth: 1.5)
                            )
                        
                        VStack(spacing: 12) {
                            Image(systemName: "photo.on.rectangle.angled")
                                .font(.system(size: 36, weight: .light))
                                .foregroundStyle(
                                    LinearGradient(
                                        colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                        startPoint: .top,
                                        endPoint: .bottom
                                    )
                                )
                            // 加载状态提示（上传/查询详情）
                            if viewModel.isLoading || viewModel.isLoadingDetail {
                                ProgressView(viewModel.isLoading ? "上传中..." : "查询详情中...")
                                    .font(.caption)
                                    .foregroundColor(Color(UIColor.secondaryLabel))
                            } else {
                                Text("点击上传图片")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(Color(UIColor.secondaryLabel))
                                    .multilineTextAlignment(.center)
                            }
                        }
                    }
                }
                // 错误信息展示（上传/查询通用）
                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(8)
                        .frame(maxWidth: .infinity)
                }
            }.overlay{
                // 圆形状态指示器（直接在 View 中写判断逻辑）
                Circle()
                    .fill(
                        // 1. 先处理 pictureDetail 为 nil 的情况（默认透明，避免崩溃）
                        viewModel.pictureDetail?.status == nil ? Color.clear :
                            // 2. 按 status 匹配对应颜色
                        viewModel.pictureDetail?.status == 0 ? Color.orange :
                            viewModel.pictureDetail?.status == 1 ? Color.green :
                            viewModel.pictureDetail?.status == 2 ? Color.red :
                            viewModel.pictureDetail?.status == 3 ? Color.gray :
                            // 3. 兜底颜色（status 为其他值时用浅灰）
                        Color.gray.opacity(0.5)
                    )
                    .frame(width: 20, height: 20)
                    .padding(.leading, 200)
                    .padding(.bottom, 200)
                // 优化：仅当有详情数据时显示，无数据时隐藏
                    .opacity(viewModel.pictureDetail != nil ? 1 : 0)
            }
            
            .onTapGesture {
                showingImagePicker = true
            }
            
            
            .simultaneousGesture(
                TapGesture()
                    .onEnded {
                        withAnimation(.easeInOut(duration: 0.15)) {
                            isUploadAreaTapped = true
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                                isUploadAreaTapped = false
                            }
                        }
                    }
            )
            
            .scaleEffect(isUploadAreaTapped ? 0.98 : 1.0)
            .sheet(isPresented: $showingImagePicker) {
                MyImagePicker(image: $viewModel.selectedImage)
            }
            
        }        .onReceive(viewModel.$uploadUrlId) { newValue in
            // 将 viewModel 中的值传递给父视图
            pictureId = newValue
        }
    }
    
    // MARK: - 辅助方法：状态文字转换（0/1转中文）
    private func statusText(_ status: Int) -> String {
        switch status {
        case 0: return "待审核"
        case 1: return "审核通过"
        case 2: return "审核驳回"
        default: return "未知状态"
        }
    }
    
    // MARK: - 辅助方法：状态颜色（通过=绿/待审=黄/驳回=红）
    private func statusColor(_ status: Int) -> Color {
        switch status {
        case 0: return .orange.opacity(0.8)
        case 1: return .green.opacity(0.8)
        case 2: return .red.opacity(0.8)
        default: return .gray
        }
    }
    
    // MARK: - 辅助方法：时间格式转换（ISO格式转本地时间）
    private func formatTime(_ isoTime: String) -> String {
        let formatter = ISO8601DateFormatter()
        if let date = formatter.date(from: isoTime) {
            let localFormatter = DateFormatter()
            localFormatter.dateFormat = "yyyy-MM-dd HH:mm"
            localFormatter.timeZone = TimeZone.current
            return localFormatter.string(from: date)
        }
        return isoTime // 转换失败则显示原始值
    }
}

// MARK: - 3. 图片选择器（不变，保持原有逻辑）
struct MyImagePicker: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    @Environment(\.presentationMode) var presentationMode
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = .photoLibrary
        picker.allowsEditing = false // 如需裁剪可设为true
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: MyImagePicker
        
        init(_ parent: MyImagePicker) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let uiImage = info[.originalImage] as? UIImage {
                parent.image = uiImage
            }
            parent.presentationMode.wrappedValue.dismiss()
        }
        
        // 取消选择时关闭picker
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.presentationMode.wrappedValue.dismiss()
        }
    }
}

// MARK: - 4. 视图模型（新增查询图片详情逻辑）
class ImageUploadViewModel: ObservableObject {
    // 基础状态
    @Published var selectedImage: UIImage?
    @Published var displayImage: UIImage?
    @Published var isLoading: Bool = false // 上传加载
    @Published var isLoadingDetail: Bool = false // 详情查询加载
    @Published var errorMessage: String?
    @Published var uploadUrlId: Int? // 上传返回的pid（适配接口Int64）
    @Published var pictureDetail: PictureDetailData? // 图片详情数据
    @Published var uploadSuccess: Bool = false
    // 缓存数据
    @AppStorage("authData") private var authData: String = ""
    @AppStorage("ID") private var studentNumber: String = ""
    // Combine订阅管理
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        // 选择图片后自动上传
        $selectedImage
            .sink { [weak self] image in
                guard let self = self, let newImage = image else { return }
                self.displayImage = newImage
                self.resetState() // 重置之前的状态
                self.uploadImageAndSaveToDatabase(with: newImage)
            }
            .store(in: &cancellables)
    }
    @State var postPictureUrl:String=""
    // MARK: - 合并版：图片上传 + 存入数据库（一键完成，自动获取pid）
    func uploadImageAndSaveToDatabase(with image: UIImage, completion: ((Bool) -> Void)? = nil) {
        isLoading = true // 整个流程开始，显示加载
        errorMessage = nil
        uploadSuccess = false // 重置成功状态
        print("配置请求头时 authData：\(authData)")

        // --------------------------
        // 步骤1：图片压缩（前置处理）
        // --------------------------
        guard let imageData = compressImage(image) else {
            isLoading = false
            errorMessage = "图片处理失败，请选择其他图片"
            completion?(false)
            return
        }
        
        // --------------------------
        // 步骤2：第一个接口：上传图片获取URL
        // --------------------------
        // 构建第一个请求（上传图片）
        guard let uploadUrl = URL(string: "\(BasicValue.baseGetUrl)/admin/common/upload") else {
            isLoading = false
            errorMessage = "上传接口地址配置错误"
            completion?(false)
            return
        }
        
        var uploadRequest = URLRequest(url: uploadUrl)
        uploadRequest.httpMethod = "POST"
        uploadRequest.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        uploadRequest.timeoutInterval = 30
        
        // 构建 multipart/form-data 请求体（上传文件专用格式）
        let boundary = "Boundary-\(UUID().uuidString)"
        uploadRequest.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        var uploadBody = Data()
        let fileName = "\(studentNumber)_\(UUID().uuidString).jpg" // 自定义文件名（避免重复）
        
        // 添加文件参数到请求体
        uploadBody.append("--\(boundary)\r\n".data(using: .utf8)!)
        uploadBody.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
        uploadBody.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        uploadBody.append(imageData)
        uploadBody.append("\r\n".data(using: .utf8)!)
        uploadBody.append("--\(boundary)--\r\n".data(using: .utf8)!)
        uploadRequest.httpBody = uploadBody
        
        // 发送第一个请求（上传图片）
        URLSession.shared.dataTask(with: uploadRequest) { [weak self] uploadData, uploadResp, uploadError in
            DispatchQueue.main.async {
                guard let self = self else { return }
                
                // 第一个请求网络错误
                if let error = uploadError {
                    self.isLoading = false
                    self.errorMessage = "图片上传失败：\(error.localizedDescription)"
                    completion?(false)
                    return
                }
                
                // 第一个请求状态码校验
                guard let httpResp = uploadResp as? HTTPURLResponse, 200...299 ~= httpResp.statusCode else {
                    self.isLoading = false
                    self.errorMessage = "图片上传接口响应错误（状态码异常）"
                    completion?(false)
                    return
                }
                
                // 第一个请求无响应数据
                guard let data = uploadData else {
                    self.isLoading = false
                    self.errorMessage = "图片上传接口未返回数据"
                    completion?(false)
                    return
                }
                
                // 解析第一个请求响应（获取图片URL）
                do {
                    let uploadResponse = try JSONDecoder().decode(ImageUploadResponseOne.self, from: data)
                    // 第一个请求业务成功（code=0或200）
                    guard uploadResponse.code == 0 || uploadResponse.code == 200 else {
                        self.isLoading = false
                        self.errorMessage = "图片上传失败：\(uploadResponse.message)"
                        completion?(false)
                        return
                    }
                    
                    // 拿到图片URL（第一个请求核心结果）
                    let pictureUrl = uploadResponse.data
                    guard !pictureUrl.isEmpty else {
                        self.isLoading = false
                        self.errorMessage = "图片上传成功，但未返回有效URL"
                        completion?(false)
                        return
                    }
                    self.postPictureUrl = pictureUrl // 保存URL到状态变量
                    // --------------------------
                    // 步骤3：第二个接口：URL存入数据库获取pid
                    // --------------------------
                    // 关键：URL参数编码（避免URL中的特殊字符导致请求失败）
                    guard let saveUrl = URL(string: "\(BasicValue.baseParseUrl)/mywustBasic/picture/addPicture?url=\(pictureUrl)&uid=\(self.studentNumber)") else{
                        self.errorMessage = "数据库服务器地址失效"
                        return
                    }
                    // 构建第二个请求（存入数据库）
                    var saveRequest = URLRequest(url: saveUrl)
                    saveRequest.httpMethod = "POST"
                    saveRequest.setValue("Wuster \(self.authData)", forHTTPHeaderField: "Authorization")
                    saveRequest.timeoutInterval = 30
                    
                    // 发送第二个请求（存入数据库）
                    URLSession.shared.dataTask(with: saveRequest) { [weak self] saveData, saveResp, saveError in
                        DispatchQueue.main.async {
                            guard let self = self else { return }
                            self.isLoading = false // 整个流程结束，隐藏加载
                            
                            // 第二个请求网络错误
                            if let error = saveError {
                                self.errorMessage = "URL存入数据库失败：\(error.localizedDescription)"
                                completion?(false)
                                return
                            }
                            
                            // 第二个请求状态码校验
                            guard let httpSaveResp = saveResp as? HTTPURLResponse, 200...299 ~= httpSaveResp.statusCode else {
                                self.errorMessage = "数据库存储接口响应错误（状态码异常）"
                                completion?(false)
                                return
                            }
                            
                            // 第二个请求无响应数据
                            guard let saveData = saveData else {
                                self.errorMessage = "数据库存储接口未返回数据"
                                completion?(false)
                                return
                            }
                            
                            // 解析第二个请求响应（获取pid）
                            do {
                                let saveResponse = try JSONDecoder().decode(ImageUploadResponseTwo.self, from: saveData)
                                // 第二个请求业务成功
                                guard saveResponse.code == 0 || saveResponse.code == 200 else {
                                    self.errorMessage = "URL存入数据库失败：\(saveResponse.message)"
                                    completion?(false)
                                    return
                                }
                                
                                // 拿到pid（最终核心结果）
                                self.uploadUrlId = saveResponse.data
                                self.uploadSuccess = true // 标记整个流程成功
                                completion?(true)
                                // 自动查询图片详情（原逻辑保留）
                                if let pid = self.uploadUrlId {
                                    self.fetchPictureDetail(with: pid)
                                }
                                
                            } catch {
                                // 第二个请求解析失败
                                let rawResp = String(data: saveData, encoding: .utf8) ?? "未知响应"
                                print("数据库存储响应解析失败：\(error)，原始响应：\(rawResp)")
                                self.errorMessage = "URL存入数据库响应解析失败：\(error.localizedDescription)"
                                completion?(false)
                            }
                        }
                    }.resume()
                    
                } catch {
                    // 第一个请求解析失败
                    let rawResp = String(data: data, encoding: .utf8) ?? "未知响应"
                    print("图片上传响应解析失败：\(error)，原始响应：\(rawResp)")
                    self.isLoading = false
                    self.errorMessage = "图片上传响应解析失败：\(error.localizedDescription)"
                    completion?(false)
                }
            }
        }.resume()
    }

    // MARK: - 核心2：新增查询图片详情方法
    func fetchPictureDetail(with pid: Int) {
        print("～～～～～～～～获取头像url \(pid)")
        isLoadingDetail = true
        errorMessage = nil
        pictureDetail = nil
        // 构建查询URL（带pid参数）
        guard let baseUrl = URL(string: "\(BasicValue.baseParseUrl)/mywustBasic/admin/common/getPictureDetail") else {
            isLoadingDetail = false
            errorMessage = "图片详情接口地址错误"
            return
        }
        
        // 拼接pid查询参数（GET请求参数放在URL中）
        var components = URLComponents(url: baseUrl, resolvingAgainstBaseURL: true)
        components?.queryItems = [URLQueryItem(name: "pid", value: "\(pid)")]
        guard let requestUrl = components?.url else {
            isLoadingDetail = false
            errorMessage = "详情请求URL拼接失败"
            return
        }
        
        // 配置请求
        var request = URLRequest(url: requestUrl)
        request.httpMethod = "POST"
        request.setValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15
        // 发送请求
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                self?.isLoadingDetail = false
                
                // 网络错误
                if let error = error {
                    self?.errorMessage = "查询详情失败：\(error.localizedDescription)"
                    return
                }
                print("\(response)")
                // HTTP状态码校验
                guard let httpResp = response as? HTTPURLResponse, 200...299 ~= httpResp.statusCode else {
                    self?.errorMessage = "详情接口响应错误（状态码：\((response as? HTTPURLResponse)?.statusCode ?? -1)）"
                    return
                }
                
                // 解析详情响应
                guard let data = data else {
                    self?.errorMessage = "详情接口未返回数据"
                    return
                }
                
                do {
                    let detailResp = try JSONDecoder().decode(PictureDetailResponse.self, from: data)
                    if detailResp.code == 200 {
                        // 解析成功，赋值详情数据
                        self?.pictureDetail = detailResp.data
                        // （可选）若需用接口返回的url重新加载图片，可在此处实现
                        // self?.loadImageFromUrl(detailResp.data?.url ?? "")
                    } else {
                        self?.errorMessage = "查询详情失败：\(detailResp.message)"
                    }
                } catch {
                    let rawResp = String(data: data, encoding: .utf8) ?? "未知响应"
                    self?.errorMessage = "详情响应解析失败：\(error.localizedDescription)\n原始响应：\(rawResp)"
                    print(rawResp)

                }
            }
        }.resume()
    }
    
    // MARK: - 辅助1：图片压缩（原有逻辑不变）
    private func compressImage(_ image: UIImage) -> Data? {
        let maxDimension: CGFloat = 1024
        var scaledSize = image.size
        // 按尺寸缩放
        if scaledSize.width > maxDimension || scaledSize.height > maxDimension {
            let scaleRatio = maxDimension / max(scaledSize.width, scaledSize.height)
            scaledSize = CGSize(width: scaledSize.width * scaleRatio, height: scaledSize.height * scaleRatio)
        }
        // 绘制缩放图
        UIGraphicsBeginImageContextWithOptions(scaledSize, false, 1.0)
        defer { UIGraphicsEndImageContext() }
        image.draw(in: CGRect(origin: .zero, size: scaledSize))
        
        guard let scaledImage = UIGraphicsGetImageFromCurrentImageContext() else {
            return nil
        }
        
        // 按质量压缩（0.8兼顾质量和大小）
        return scaledImage.jpegData(compressionQuality: 0.8)
    }
    
    // MARK: - 辅助2：状态重置（选择新图片时清空之前数据）
    private func resetState() {
        uploadUrlId = nil
        pictureDetail = nil
        uploadSuccess = false
        errorMessage = nil
    }
    
    // 辅助3：从接口返回的URL加载图片（如需替换本地预览图）
    private func loadImageFromUrl(_ urlString: String) {
        guard let url = URL(string: urlString) else { return }
        URLSession.shared.dataTask(with: url) { [weak self] data, _, _ in
            if let data = data, let image = UIImage(data: data) {
                DispatchQueue.main.async {
                    self?.displayImage = image
                }
            }
        }.resume()
    }
}
//将pid传给服务器
struct ImageUploadResponseTwo: Codable {
    let code: Int
    let message: String
    let data: Int // 上传返回的pid（接口定义为Int64，原Int改为Int64）
    let timestamp: TimeInterval
}
//传阿里云拿到url
struct ImageUploadResponseOne: Codable {
    let code: Int
    let message: String
    let data: String // 上传返回的pid（接口定义为Int64，原Int改为Int64）
    let timestamp: TimeInterval
}
