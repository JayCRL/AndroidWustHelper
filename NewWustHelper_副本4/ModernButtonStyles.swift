import SwiftUI

// MARK: - 现代化按钮样式
struct ModernRetryButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) var colorScheme
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(.white)
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [Color.blue, Color.blue.opacity(0.8)]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
            .shadow(color: Color.blue.opacity(0.3), radius: 4, x: 0, y: 2)
    }
}

struct ModernDismissButtonStyle: ButtonStyle {
    @Environment(\.colorScheme) var colorScheme
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(colorScheme == .dark ? .white : .black)
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(
                        colorScheme == .dark ? 
                        Color.gray.opacity(0.3) : 
                        Color.gray.opacity(0.2)
                    )
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(
                        colorScheme == .dark ? 
                        Color.gray.opacity(0.5) : 
                        Color.gray.opacity(0.3), 
                        lineWidth: 1
                    )
            )
    }
}

// MARK: - 预览
#Preview {
    VStack(spacing: 20) {
        Button("重试") { }
            .buttonStyle(ModernRetryButtonStyle())
        
        Button("关闭") { }
            .buttonStyle(ModernDismissButtonStyle())
    }
    .padding()
}
