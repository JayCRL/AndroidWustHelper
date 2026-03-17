import Foundation
import SwiftUI
struct BasicResponse: Codable {
    let code: Int
    let message: String
    let data: CodableValue?
    let ok: Bool
}
// 用于泛型 data 字段处理
struct CodableValue: Codable {}
struct MyPublishView: View {
    @Binding var selectTab:Int
    @State private var products: [Commodity] = []
    @State private var isLoading = false
    @State private var hasMoreData = true
    @State private var errorMessage: String?
    @State private var showNotice = false
    @AppStorage("authData") private var authData: String = ""
    @State private var activeIndex = 0
    @State private var showDetailShoop = false
    @State private var isRefreshing = false
    @State private var currentPage = 1
    @Environment(\.presentationMode) var presentationMode
    @State var product:Commodity=Commodity()
    // 渐变色背景
    private let backgroundGradient = LinearGradient(
        gradient: Gradient(colors: [Color(#colorLiteral(red: 0.05, green: 0.1, blue: 0.2, alpha: 1)), Color(#colorLiteral(red: 0.15, green: 0.25, blue: 0.4, alpha: 1))]),
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    var body: some View {
        NavigationView {
            ZStack {
                // 深色背景
                backgroundGradient
                    .edgesIgnoringSafeArea(.top)
                VStack(spacing: 0) {
                    // 自定义导航栏
                    HStack {
                        Button(action: {
                            // 返回上一页
                            presentationMode.wrappedValue.dismiss()
                        }) {
                            Image(systemName: "arrow.left")
                                .font(.system(size: 20))
                                .foregroundColor(.white)
                                .frame(width: 40, height: 40)
                                .background(Color.white.opacity(0.15))
                                .clipShape(Circle())
                        }
                        
                        Text("我的发布")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .padding(.leading, 89)
                        
                        Spacer()
                        
                        Button(action: {
                            Task {
                                await refreshProducts()
                            }
                        }) {
                            Image(systemName: "arrow.clockwise")
                                .font(.system(size: 20))
                                .foregroundColor(.white)
                                .frame(width: 40, height: 40)
                                .background(Color.white.opacity(0.15))
                                .clipShape(Circle())
                        }
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    .padding(.bottom, 15)
                    .background(
                        Color.black.opacity(0.3)
                            .blur(radius: 10)
                    )
                    
                    // 主内容区域
                    if products.isEmpty && !isLoading {
                        emptyStateView
                    } else {
                        contentListView
                    }
                }
            }
            .onAppear {
                Task {
                    await loadProducts()
                }
            }
        }
        .navigationBarHidden(true)
    }
    
    // MARK: - 组件
    
    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "square.and.arrow.up")
                .font(.system(size: 60))
                .foregroundColor(.blue.opacity(0.6))
                .padding()
                .background(
                    Circle()
                        .fill(Color.white.opacity(0.1))
                        .frame(width: 120, height: 120)
                )
            
            Text("您还没有发布商品")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.white)
            
            Text("点击右下角+号按钮，发布您的第一件商品")
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.7))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            Button(action: {
                // 这里可以添加跳转到发布页面的动作
                presentationMode.wrappedValue.dismiss()
                selectTab=1
            }) {
                Text("发布商品")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 30)
                    .padding(.vertical, 12)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [Color.blue, Color.purple]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(25)
                    .shadow(color: Color.blue.opacity(0.5), radius: 10, x: 0, y: 5)
            }
            .padding(.top, 20)
            
            Spacer()
        }
    }
    
    private var contentListView: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                           ForEach(products.indices, id: \.self) { index in
                               ProductCardView(product: products[index],onTap: {
                                   print("点击了第\(index)个商品")
                                   activeIndex=index
                                   product=products[index]
                                   showDetailShoop=true
                               })
                                   .contextMenu {
                                       Button(role: .destructive) {
                                           deleteProduct(at: IndexSet(integer: index))
                                       } label: {
                                           Label("删除商品", systemImage: "trash")
                                       }
                                   }
                                   .onAppear {
                                       if index == products.count - 1 && hasMoreData && !isLoading {
                                           loadMoreProducts()
                                       }
                                   }
                           }
                           
                if isLoading && products.count > 0 {
                    HStack {
                        Spacer()
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        Spacer()
                    }
                    .padding(.vertical, 20)
                }
                
                if !hasMoreData && !products.isEmpty {
                    Text("没有更多商品了")
                        .font(.footnote)
                        .foregroundColor(.white.opacity(0.6))
                        .frame(maxWidth: .infinity, alignment: .center)
                        .padding(.vertical, 20)
                }
            }// 动态计算安全区域和TabBar高度
            .frame(maxWidth: .infinity,maxHeight: .infinity) // 宽度自适应父容器
            .padding(.horizontal)
            .padding(.top, 10)
            
        }            .padding(.bottom, 1)

        .refreshable {
            await refreshProducts()
        }
        .sheet(isPresented: $showDetailShoop) {
            CommodityView(
                commodity: $product
                ,
                onSave: { action in
                    Task {
                        do {
                            if action == 1 {
                                try await ProductService.addProductToFavorites(pid: products[activeIndex].pid)
                            } else {
                                try await ProductService.DeleteProductToFavorites(pid: products[activeIndex].pid)
                            }
                        } catch {
                            errorMessage = "操作失败: \(error.localizedDescription)"
                            showNotice = true
                        }
                    }
                },
                showError: $showNotice,
                ErrorMessage: $errorMessage
            )
        }
        .alert(isPresented: $showNotice) {
            Alert(
                title: Text("提示"),
                message: Text(errorMessage ?? "操作失败"),
                dismissButton: .default(Text("确定"))
            )
        }
    }
    
    // MARK: - 数据方法
    
    private func loadProducts() async {
        isLoading = true
        do {
            let fetchedProducts = try await PublishProductService.fetchMyProducts(page: currentPage)
            await MainActor.run {
                products = fetchedProducts
                isLoading = false
                hasMoreData = fetchedProducts.count == PublishProductService.pageSize
            }
        } catch {
            await MainActor.run {
                errorMessage = "加载失败: \(error.localizedDescription)"
                isLoading = false
                showNotice = true
            }
        }
    }
    
    private func loadMoreProducts() {
        guard !isLoading else { return }
        isLoading = true
        currentPage += 1
        
        Task {
            do {
                let moreProducts = try await PublishProductService.fetchMyProducts(page: currentPage)
                await MainActor.run {
                    products.append(contentsOf: moreProducts)
                    isLoading = false
                    hasMoreData = moreProducts.count == PublishProductService.pageSize
                }
            } catch {
                await MainActor.run {
                    errorMessage = "加载失败: \(error.localizedDescription)"
                    isLoading = false
                    currentPage -= 1
                }
            }
        }
    }
    
    private func refreshProducts() async {
        isRefreshing = true
        currentPage = 1
        await loadProducts()
        isRefreshing = false
    }
    
    private func deleteProduct(at offsets: IndexSet) {
        guard let index = offsets.first else { return }
        let product = products[index]
        
        Task {
            do {
                try await PublishProductService.deleteProduct(pid: product.pid, authData: authData)
                await MainActor.run {
                    products.remove(atOffsets: offsets)
                }
            } catch {
                await MainActor.run {
                    errorMessage = "删除失败: \(error.localizedDescription)"
                    showNotice = true
                }
            }
        }
    }
}

struct PublishProductService {
    @AppStorage("authData") private var authData: String = ""
    static let pageSize = 10
    // ✅ 删除商品
    static func deleteProduct(pid: Int,authData:String) async throws {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/delete/\(pid)") else {
            throw URLError(.badURL)
        }
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.addValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        let result = try JSONDecoder().decode(BasicResponse.self, from: data)
        if !result.ok {
            throw NSError(domain: "删除失败", code: result.code, userInfo: [NSLocalizedDescriptionKey: result.message])
        }
    }

    // ✅ 获取当前用户发布商品列表
    static func fetchMyProducts(page: Int) async throws -> [Commodity] {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/uid/\(page)/\(pageSize)") else {
            throw URLError(.badURL)
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        let authToken = UserDefaults.standard.string(forKey: "authToken")
        if let token = authToken {
            print("成功获取令牌：\(token.prefix(6))...") // 只打印前6位，保护隐私
            request.addValue("Wuster \(token)", forHTTPHeaderField: "Authorization")
        } else {
            print("⚠️ 未获取到authToken，可能的原因：未保存、键名错误或已被删除")
            // 这里可以添加处理逻辑，如跳转登录页
        }
        do {
            let (data, response) = try await URLSession.shared.data(for: request)

            if let httpResponse = response as? HTTPURLResponse {
                print("HTTP状态码：\(httpResponse.statusCode)")
                if let body = String(data: data, encoding: .utf8) {
                    print("响应体：\(body)")
                }
                print("码：\(httpResponse.statusCode )")
                guard httpResponse.statusCode == 200 else {
                    throw URLError(.badServerResponse)
                }
            } else {
                throw URLError(.badServerResponse)
            }
           
            // 添加一个包装层，用来解码外层的 JSON 响应结构
            struct ApiResponse: Codable {
                let code: Int
                let message: String
                let data: PageCommodity
                let ok: Bool
            }

            struct PageCommodity: Codable {
                let records: [Commodity]?
                let total: Int
                let size: Int
                let current: Int
                let pages: Int
            }
            do {
                // 解码外层的 API 响应数据
                let decodedResponse = try JSONDecoder().decode(ApiResponse.self, from: data)
                
                // 通过 data 获取 PageCommodity
                let pageCommodity = decodedResponse.data
                
                // 返回 records 数组，空数组也是有效的
                return pageCommodity.records ?? []
            } catch {
                print("解码失败: \(error)")
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("原始 JSON: \(jsonString)")  // 输出原始 JSON 数据，帮助调试
                }
                throw error
            }

        } catch {
            if let urlError = error as? URLError {
                print("⚠️ URL错误: \(urlError.localizedDescription)")
            } else {
                print("⚠️ 解析或其他错误: \(error.localizedDescription)")
            }
            throw error
        }

    }
}
