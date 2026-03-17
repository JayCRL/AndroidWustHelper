import SwiftUI
import CoreImage.CIFilterBuiltins
import CoreImage

struct QRCodeView: View {
    var content: String
    var size: CGFloat = 200
    var correctionLevel: CorrectionLevel = .L // 默认高容错级别
    
    @State private var qrCodeImage: UIImage?
    @State private var errorMessage: String?
    
    // 二维码纠错级别枚举（含容量限制和描述）
    enum CorrectionLevel: String, CaseIterable {
        case L, M, Q, H
        // 不同级别对应的最大UTF-8字节容量（符合QR Code标准）
        var maxBytes: Int {
            switch self {
            case .L: return 2953  // 7%容错率，约1800个汉字
            case .M: return 2331  // 15%容错率，约1400个汉字
            case .Q: return 1663  // 25%容错率，约1000个汉字
            case .H: return 1273  // 30%容错率，约780个汉字
            }
        }
        
        // 容错率描述文本（用于UI展示）
        var faultTolerance: String {
            switch self {
            case .L: return "7%"
            case .M: return "15%"
            case .Q: return "25%"
            case .H: return "30%"
            }
        }
    }
    
    var body: some View {
        VStack(spacing: 12) {
            // 错误状态展示
            if let errorMessage = errorMessage {
                VStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundColor(.red)
                    Text("生成失败")
                        .font(.headline)
                        .foregroundColor(.primary)
                    Text(errorMessage)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding()
                .background(Color(UIColor.systemBackground))
                .cornerRadius(12)
                .shadow(radius: 2)
            }
            // 二维码成功生成展示
            else if let qrCodeImage = qrCodeImage {
                Image(uiImage: qrCodeImage)
                    .resizable()
                    .interpolation(.none) // 禁用插值，保证二维码像素清晰
                    .scaledToFit()
                    .frame(width: size, height: size)
                    .background(Color.white) // 白色背景提升扫码识别率
                    .cornerRadius(8)
                    .shadow(radius: 3)
//                
//                // 内容信息提示（字符数+字节数+纠错级别）
//                VStack(spacing: 4) {
//                    Text("内容：\(content.count)字符（\(content.utf8.count)字节）")
//                        .font(.caption)
//                        .foregroundColor(.secondary)
//                    Text("纠错级别：\(correctionLevel.rawValue)（容错率\(correctionLevel.faultTolerance)）")
//                        .font(.caption2)
//                        .foregroundColor(.secondary)
//                }
//                
//                // 复制内容按钮
//                Button(action: {
//                    UIPasteboard.general.string = content
//                }) {
//                    Text("复制内容")
//                        .font(.subheadline)
//                        .padding(.horizontal, 16)
//                        .padding(.vertical, 8)
//                }
//                .buttonStyle(.borderedProminent)
//                .tint(.blue)
            }
            // 加载中状态
            else {
                ProgressView("生成二维码中...")
                    .frame(width: size, height: size)
                    .background(Color(UIColor.systemBackground))
                    .cornerRadius(12)
                    .shadow(radius: 2)
            }
        }
        .padding()
        .task {
            generateQRCode() // 视图出现时触发生成逻辑
        }
    }
    
    // MARK: - 主二维码生成逻辑（后台线程执行，避免阻塞UI）
    private func generateQRCode() {
        // 重置状态
        errorMessage = nil
        qrCodeImage = nil
        // 1. 空内容检查
        guard !content.isEmpty else {
            errorMessage = "请输入二维码内容（不能为空）"
            return
        }
        // 2. 容量检查（按UTF-8字节数，比字符数更准确）
        let contentBytes = content.utf8.count
        guard contentBytes <= correctionLevel.maxBytes else {
            // 自动推荐可容纳的纠错级别
            let suggestedLevel = CorrectionLevel.allCases.first { $0.maxBytes >= contentBytes }
            var errorText = "内容超出容量限制：\n当前\(correctionLevel.rawValue)级最大支持\(correctionLevel.maxBytes)字节，\n您的内容为\(contentBytes)字节"
            
            if let suggested = suggestedLevel {
                errorText += "\n建议改用\(suggested.rawValue)级（支持\(suggested.maxBytes)字节，容错率\(suggested.faultTolerance)）"
            } else {
                errorText += "\n课表内容太多了,暂时不能分享哦😯"
            }
            errorMessage = errorText
            return
        }
        // 3. 后台线程处理图像生成（用户级优先级，兼顾性能和UI响应）
        DispatchQueue.global(qos: .userInitiated).async {
            do {
                // 转换内容为UTF-8数据（二维码需基于二进制数据生成）
                guard let contentData = content.data(using: .utf8) else {
                    throw NSError(domain: "QRCodeError", code: 2, userInfo: [
                        NSLocalizedDescriptionKey: "无法将内容转换为UTF-8数据"
                    ])
                }
                
                // 创建具体的QRCode滤镜（类型安全，支持直接设置属性）
                let qrFilter = CIFilter.qrCodeGenerator()
                qrFilter.message = contentData // 直接设置数据（类型专属属性）
                qrFilter.correctionLevel = correctionLevel.rawValue // 直接设置纠错级别
                
                // 获取滤镜输出的CIImage（未缩放的原始图像）
                guard let outputCIImage = qrFilter.outputImage else {
                    throw NSError(domain: "QRCodeError", code: 3, userInfo: [
                        NSLocalizedDescriptionKey: "滤镜未生成二维码图像",
                        NSLocalizedFailureReasonErrorKey: "将尝试备选生成方案"
                    ])
                }
                
                // 缩放原始图像到目标尺寸（保持高质量，无模糊）
                guard let scaledUIImage = createHighQualityUIImage(from: outputCIImage, targetSize: CGSize(width: size, height: size)) else {
                    throw NSError(domain: "QRCodeError", code: 4)
                }
                
                // 主线程更新UI（UI操作必须在主线程执行）
                DispatchQueue.main.async {
                    self.qrCodeImage = scaledUIImage
                }
            } catch {
                // 主方案失败时，尝试备选方案
                if (error as NSError).code == 3 {
                    if let backupImage = generateQRCodeWithBackupPlan() {
                        DispatchQueue.main.async {
                            self.qrCodeImage = backupImage
                        }
                        return
                    }
                }
                // 所有方案失败，显示错误信息
                DispatchQueue.main.async {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }
    // MARK: - 备选二维码生成方案（兼容旧版逻辑，用字符串键设置参数）
    private func generateQRCodeWithBackupPlan() -> UIImage? {
        // 1. 转换内容为UTF-8数据
        guard let contentData = content.data(using: .utf8) else {
            return nil
        }
        
        // 2. 创建通用滤镜（用名称初始化，返回CIFilter基类）
        guard let qrFilter = CIFilter(name: "CIQRCodeGenerator") else {
            return nil
        }
        
        // 关键修复：用字符串键设置参数（苹果官方定义的键名，无作用域问题）
        qrFilter.setValue(contentData, forKey: "inputMessage") // 对应数据参数
        qrFilter.setValue(correctionLevel.rawValue, forKey: "inputCorrectionLevel") // 对应纠错级别参数
        
        // 3. 获取原始输出图像
        guard let outputCIImage = qrFilter.outputImage else {
            return nil
        }
        
        // 4. 简化的图像转换逻辑（兼容低版本系统）
        let ciContext = CIContext(options: [
            .useSoftwareRenderer: false, // 优先使用GPU渲染，提升速度
            .highQualityDownsample: true // 保证缩放质量
        ])
        
        // 转换CIImage为CGImage（中间步骤，便于后续缩放）
        guard let cgImage = ciContext.createCGImage(outputCIImage, from: outputCIImage.extent) else {
            return nil
        }
        
        // 缩放CGImage到目标尺寸（用UIGraphicsImageRenderer保证清晰）
        let targetSize = CGSize(width: size, height: size)
        let renderer = UIGraphicsImageRenderer(size: targetSize)
        return renderer.image { context in
            // 禁用插值，避免二维码像素模糊
            context.cgContext.interpolationQuality = .none
            UIImage(cgImage: cgImage).draw(in: CGRect(origin: .zero, size: targetSize))
        }
    }
    
    // MARK: - 高质量二维码图像生成（无插值，保持像素清晰）
    private func createHighQualityUIImage(from ciImage: CIImage, targetSize: CGSize) -> UIImage? {
        // 修正图像范围为整数像素（避免浮点像素导致的模糊）
        let integralExtent = ciImage.extent.integral
        
        // 计算等比例缩放系数（保证二维码不拉伸）
        let scaleFactor = min(
            targetSize.width / integralExtent.width,
            targetSize.height / integralExtent.height
        )
        
        // 计算最终缩放后的尺寸（整数像素，避免抗锯齿）
        let scaledWidth = Int(integralExtent.width * scaleFactor)
        let scaledHeight = Int(integralExtent.height * scaleFactor)
        let scaledSize = CGSize(width: scaledWidth, height: scaledHeight)
        
        // 创建灰度位图上下文（二维码为黑白，减少内存占用，提升渲染速度）
        guard let bitmapContext = CGContext(
            data: nil,
            width: scaledWidth,
            height: scaledHeight,
            bitsPerComponent: 8, // 8位灰度（0=黑，255=白）
            bytesPerRow: scaledWidth, // 每行字节数=宽度（无Alpha通道）
            space: CGColorSpaceCreateDeviceGray(),
            bitmapInfo: CGImageAlphaInfo.none.rawValue // 无Alpha通道
        ) else {
            return nil
        }
        
        // 渲染CIImage到位图上下文
        let ciContext = CIContext(options: [.useSoftwareRenderer: false])
        guard let cgImage = ciContext.createCGImage(ciImage, from: integralExtent) else {
            return nil
        }
        
        // 禁用插值，保证二维码像素锐利
        bitmapContext.interpolationQuality = .none
        // 缩放上下文（对应之前计算的缩放系数）
        bitmapContext.scaleBy(x: scaleFactor, y: scaleFactor)
        // 绘制图像到上下文
        bitmapContext.draw(cgImage, in: integralExtent)
        
        // 从上下文获取最终CGImage，并转换为UIImage
        guard let scaledCGImage = bitmapContext.makeImage() else {
            return nil
        }
        
        return UIImage(cgImage: scaledCGImage)
    }
}
// MARK: - 预览视图（支持多场景测试）
struct QRCodeView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            // 普通文本测试
            QRCodeView(content: "Hello, QR Code!")
                .preferredColorScheme(.light)
            
            // 链接测试（指定尺寸和纠错级别）
            QRCodeView(content: "https://example.com", size: 300, correctionLevel: .M)
                .preferredColorScheme(.dark)
            
            // 长文本测试（接近容量限制）
            QRCodeView(content: String(repeating: "测试", count: 700), size: 250, correctionLevel: .L)
        }
    }
}
