import SwiftUI

// MARK: - 网络请求使用示例
struct NetworkUsageExample: View {
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @State private var data: [String] = []
    
    var body: some View {
        NavigationView {
            ZStack {
                Color("bgcolor").edgesIgnoringSafeArea(.all)
                
                VStack {
                    if isLoading {
                        ProgressView("加载中...")
                            .padding()
                    } else if !data.isEmpty {
                        List(data, id: \.self) { item in
                            Text(item)
                        }
                    } else {
                        Text("暂无数据")
                            .foregroundColor(.gray)
                    }
                }
                
                // 错误提示
                if let errorMessage = errorMessage {
                    VStack {
                        Spacer()
                        ErrorToast(
                            error: .unknown(errorMessage),
                            onDismiss: {
                                withAnimation(.easeOut(duration: 0.5)) {
                                    self.errorMessage = nil
                                }
                            }
                        )
                        Spacer()
                    }
                }
            }
            .navigationTitle("网络请求示例")
            .onAppear {
                loadData()
            }
        }
    }
    
    // MARK: - 网络请求示例方法
    private func loadData() {
        isLoading = true
        errorMessage = nil
        
        // 示例URL - 请替换为实际的API地址
        guard let url = URL(string: "https://api.example.com/data") else {
            errorMessage = "URL无效"
            isLoading = false
            return
        }
        
        // 使用统一的网络请求处理器
        NetworkErrorHandler.get(
            url: url,
            headers: ["Authorization": "Bearer your_token"],
            responseType: DataResponse.self
        ) { result in
            isLoading = false
            
            switch result {
            case .success(let response):
                if response.code == 200 {
                    data = response.data
                } else {
                    errorMessage = response.message
                }
            case .failure(let networkError):
                errorMessage = networkError.userFriendlyMessage
            }
        }
    }
}

// MARK: - 响应数据模型示例
struct DataResponse: Codable {
    let code: Int
    let message: String
    let data: [String]
}

// MARK: - 带重试功能的网络请求示例
struct RetryableNetworkExample: View {
    @State private var isLoading = false
    @State private var networkError: NetworkError? = nil
    @State private var data: String = ""
    
    var body: some View {
        ZStack {
            Color("bgcolor").edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 20) {
                if isLoading {
                    ProgressView("加载中...")
                        .padding()
                } else if !data.isEmpty {
                    Text(data)
                        .padding()
                } else {
                    Text("点击按钮开始请求")
                        .foregroundColor(.gray)
                }
                
                Button("开始请求") {
                    performRequest()
                }
                .buttonStyle(.borderedProminent)
                .disabled(isLoading)
            }
            
            // 错误显示
            if let networkError = networkError {
                VStack {
                    Spacer()
                    ErrorDisplayView(
                        error: networkError,
                        onRetry: {
                            self.networkError = nil
                            performRequest()
                        },
                        onDismiss: {
                            self.networkError = nil
                        }
                    )
                    Spacer()
                }
            }
        }
    }
    
    private func performRequest() {
        isLoading = true
        networkError = nil
        
        guard let url = URL(string: "https://api.example.com/retryable") else {
            networkError = .unknown("URL无效")
            isLoading = false
            return
        }
        
        NetworkErrorHandler.get(
            url: url,
            responseType: SimpleResponse.self
        ) { result in
            isLoading = false
            
            switch result {
            case .success(let response):
                if response.code == 200 {
                    data = response.data
                } else {
                    networkError = .unknown(response.message)
                }
            case .failure(let error):
                networkError = error
            }
        }
    }
}

// MARK: - 简单响应模型
struct SimpleResponse: Codable {
    let code: Int
    let message: String
    let data: String
}

// MARK: - 网络状态监控示例
struct NetworkStatusExample: View {
    @StateObject private var networkMonitor = NetworkMonitor()
    @State private var data: String = ""
    
    var body: some View {
        VStack(spacing: 20) {
            HStack {
                Image(systemName: networkMonitor.isConnected ? "wifi" : "wifi.slash")
                    .foregroundColor(networkMonitor.isConnected ? .green : .red)
                
                Text(networkMonitor.isConnected ? "网络已连接" : "网络未连接")
                    .foregroundColor(networkMonitor.isConnected ? .green : .red)
            }
            
            if !networkMonitor.isConnected {
                ErrorDisplayView(
                    error: .noInternetConnection,
                    onRetry: {
                        // 重试逻辑
                    },
                    onDismiss: {
                        // 关闭逻辑
                    }
                )
            }
            
            Text(data.isEmpty ? "暂无数据" : data)
                .padding()
        }
        .padding()
    }
}

// MARK: - 预览
#Preview {
    NetworkUsageExample()
}
