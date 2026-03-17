import SwiftUI

// MARK: - 统一错误显示视图
struct ErrorDisplayView: View {
    let error: NetworkError
    let onRetry: (() -> Void)?
    let onDismiss: (() -> Void)?
    
    @Environment(\.colorScheme) var colorScheme
    
    init(error: NetworkError, onRetry: (() -> Void)? = nil, onDismiss: (() -> Void)? = nil) {
        self.error = error
        self.onRetry = onRetry
        self.onDismiss = onDismiss
    }
    
    var body: some View {
        VStack(spacing: 16) {
            // 错误图标
            Image(systemName: errorIcon)
                .font(.system(size: 48))
                .foregroundColor(errorColor)
            
            // 错误标题
            Text(errorTitle)
                .font(.headline)
                .foregroundColor(colorScheme == .dark ? .white : .black)
            
            // 错误描述
            Text(error.userFriendlyMessage)
                .font(.body)
                .foregroundColor(colorScheme == .dark ? .white.opacity(0.8) : .black.opacity(0.7))
                .multilineTextAlignment(.center)
                .lineLimit(3)
            
            // 操作按钮
            HStack(spacing: 12) {
                if let onRetry = onRetry, error.shouldRetry {
                    Button("重试") {
                        onRetry()
                    }
                    .buttonStyle(RetryButtonStyle())
                }
                
                if let onDismiss = onDismiss {
                    Button("关闭") {
                        onDismiss()
                    }
                    .buttonStyle(DismissButtonStyle())
                }
            }
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(colorScheme == .dark ? Color.black.opacity(0.6) : Color.white.opacity(0.9))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(errorColor.opacity(0.3), lineWidth: 1)
                )
        )
        .shadow(color: Color.black.opacity(0.1), radius: 8, x: 0, y: 4)
        .padding(.horizontal, 20)
    }
    
    // MARK: - 计算属性
    private var errorIcon: String {
        switch error {
        case .noInternetConnection:
            return "wifi.slash"
        case .timeout:
            return "clock.badge.exclamationmark"
        case .serverUnavailable, .serverError:
            return "server.rack"
        case .invalidResponse, .dataParsingError:
            return "exclamationmark.triangle"
        case .authenticationFailed:
            return "person.badge.key"
        case .unknown:
            return "exclamationmark.octagon"
        }
    }
    
    private var errorColor: Color {
        switch error {
        case .noInternetConnection, .timeout:
            return .orange
        case .serverUnavailable, .serverError:
            return .red
        case .invalidResponse, .dataParsingError:
            return .yellow
        case .authenticationFailed:
            return .purple
        case .unknown:
            return .gray
        }
    }
    
    private var errorTitle: String {
        switch error {
        case .noInternetConnection:
            return "网络连接失败"
        case .timeout:
            return "请求超时"
        case .serverUnavailable:
            return "服务不可用"
        case .serverError:
            return "服务器错误"
        case .invalidResponse:
            return "响应异常"
        case .dataParsingError:
            return "数据解析失败"
        case .authenticationFailed:
            return "登录失效"
        case .unknown:
            return "未知错误"
        }
    }
}

// MARK: - 重试按钮样式
struct RetryButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) var colorScheme
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(.white)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.blue)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}

// MARK: - 关闭按钮样式
struct DismissButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) var colorScheme
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(colorScheme == .dark ? .white : .black)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(colorScheme == .dark ? Color.gray.opacity(0.3) : Color.gray.opacity(0.2))
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}

// MARK: - 错误提示Toast
struct ErrorToast: View {
    let error: NetworkError
    let onDismiss: () -> Void
    
    @Environment(\.colorScheme) var colorScheme
    @State private var isVisible = false
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.orange)
                .font(.system(size: 20))
            
            Text(error.userFriendlyMessage)
                .font(.system(size: 15, weight: .medium))
                .foregroundColor(colorScheme == .dark ? .white : .black)
                .lineLimit(2)
            
            Spacer()
            
            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .foregroundColor(colorScheme == .dark ? .white.opacity(0.7) : .black.opacity(0.7))
                    .font(.system(size: 16))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(colorScheme == .dark ? Color.black.opacity(0.6) : Color.white.opacity(0.9))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                )
        )
        .shadow(color: Color.black.opacity(0.1), radius: 6, x: 0, y: 3)
        .padding(.horizontal, 20)
        .offset(y: isVisible ? 0 : -100)
        .onAppear {
            withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) {
                isVisible = true
            }
        }
    }
}

// MARK: - 预览
#Preview {
    VStack(spacing: 20) {
        ErrorDisplayView(
            error: .noInternetConnection,
            onRetry: { print("重试") },
            onDismiss: { print("关闭") }
        )
        
        ErrorToast(
            error: .timeout,
            onDismiss: { print("关闭Toast") }
        )
    }
    .padding()
}
