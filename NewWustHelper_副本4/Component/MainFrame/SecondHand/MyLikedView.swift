import SwiftUI
struct MyCollectionView: View {
    @State private var products: [Commodity] = []
    @State private var isLoading = false
    @State private var hasMoreData = true
    @State private var errorMessage: String?
    @State private var showNotice = false
    @AppStorage("authData") private var authData: String = ""
    @State private var activeIndex = 0
    @State private var showDetailShoop = false
    @State private var isRefreshing = false
    @Environment(\.presentationMode) var presentationMode
    @Binding var selectTab:Int
    @State var commodity:Commodity=Commodity()
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
                        Text("我的收藏")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                        
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
        }.navigationBarHidden(true)

    }
    
    // MARK: - 组件
    
    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Spacer()
            
            Image(systemName: "heart.fill")
                .font(.system(size: 60))
                .foregroundColor(.pink.opacity(0.6))
                .padding()
                .background(
                    Circle()
                        .fill(Color.white.opacity(0.1))
                        .frame(width: 120, height: 120)
                )
            
            Text("收藏夹空空如也")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.white)
            
            Text("发现心仪的商品，点击收藏将它们保存在这里")
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.7))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            Button(action: {
                // 这里可以添加跳转到商品列表的动作
                selectTab=0
                presentationMode.wrappedValue.dismiss()
            }) {
                Text("去逛逛")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 30)
                    .padding(.vertical, 12)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [Color.pink, Color.purple]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(25)
                    .shadow(color: Color.pink.opacity(0.5), radius: 10, x: 0, y: 5)
            }
            .padding(.top, 20)
            
            Spacer()
        }
    }
    
    private var contentListView: some View {
        List {
            ForEach(products.indices, id: \.self) { index in
                CollectionProductCard(product: products[index])
                    .onTapGesture {
                        activeIndex = index
                        commodity=products[index]
                        showDetailShoop = true
                    }
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
            }
            .onDelete(perform: deleteLikedProduct)
            
            if isLoading && products.count > 0 {
                HStack {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    Spacer()
                }
                .listRowBackground(Color.clear)
                .padding(.vertical, 20)
            }
            
            if !hasMoreData && !products.isEmpty {
                Text("没有更多收藏商品了")
                    .font(.footnote)
                    .foregroundColor(.white.opacity(0.6))
                    .frame(maxWidth: .infinity, alignment: .center)
                    .listRowBackground(Color.clear)
                    .padding(.vertical, 20)
            }
        }.padding(.bottom,1)
        .listStyle(PlainListStyle())
        .background(Color.clear)
        .sheet(isPresented: $showDetailShoop) {
            CommodityView(
                commodity: $commodity,
                onSave: { action in
                    Task {
                        do {
                            if action == 1 {
                                try await ProductService.addProductToFavorites(pid: products[activeIndex].pid)
                            } else {
                                try await ProductService.DeleteProductToFavorites(pid: products[activeIndex].pid)
                                await refreshProducts()
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
            let fetchedProducts = try await CollectedProductService.fetchMyCollectedProducts()
            await MainActor.run {
                products = fetchedProducts
                isLoading = false
            }
        } catch {
            await MainActor.run {
                errorMessage = "加载失败: \(error.localizedDescription)"
                isLoading = false
                showNotice = true
            }
        }
    }
    
    private func refreshProducts() async {
        isRefreshing = true
        await loadProducts()
        isRefreshing = false
    }
    
    private func deleteLikedProduct(at offsets: IndexSet) {
        guard let index = offsets.first else { return }
        let product = products[index]
        Task {
            do {
                try await CollectedProductService.deleteProduct(pid: product.pid, authData: authData)
                await MainActor.run {
                    products.remove(atOffsets: offsets)
                }
            } catch {
                await MainActor.run {
                    errorMessage = "取消收藏失败: \(error.localizedDescription)"
                    showNotice = true
                }
            }
        }
    }
}
struct HelpCenterView: View {
    @Environment(\.presentationMode) var presentationMode
    @Binding var selectTab: Int
    @State var commodity: Commodity = Commodity()
    @State private var suggestionText: String = "" // 用于存储用户输入的建议
    
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
                        
                        Text("帮助中心")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .padding(.leading, 100)
                        Spacer()
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    .padding(.bottom, 15)
                    .background(
                        Color.black.opacity(0.3)
                            .blur(radius: 10)
                    )
                    
                    ScrollView {
                        VStack(alignment: .leading, spacing: 20) {
                            // 说明部分
                            Text("每张图片审核后发布，图片右上角的圆球代表审核状态：")
                                .font(.subheadline)
                                .foregroundColor(.white)
                                .padding(.leading,20)

                            HStack() {
                                Circle()
                                    .fill(Color.green)
                                    .frame(width: 20, height: 20)
                                Text("绿色:通过")
                                    .foregroundColor(.white)
                                
                                Circle()
                                    .fill(Color.orange)
                                    .frame(width: 20, height: 20)
                                Text("橙色:审核")
                                    .foregroundColor(.white)
                                
                                Circle()
                                    .fill(Color.red)
                                    .frame(width: 20, height: 20)
                                Text("红色:拒绝")
                                    .foregroundColor(.white)
                            }
                            .padding(.leading,30)

                            Text("商品发布后不能修改信息，只能删除重新发送")
                                .font(.subheadline)
                                .foregroundColor(.white)
                                .padding(.leading,20)

                            Text("每人最多只能发布五个商品信息,请及时清理旧发布")
                                .font(.subheadline)
                                .foregroundColor(.white)
                                .padding(.leading,20)
                            Text("在我的发布界面长按活动卡片删除")
                                .font(.subheadline)
                                .foregroundColor(.white)
                                .padding(.leading,20)
                            
                            // 输入框和发送框
                            VStack(alignment: .leading) {
                                NavigationLink(destination: SuggestionView()){
                                    Text("我要反馈🤔")
                                        .fontWeight(.bold)
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding()
                                        .background(Color.blue)
                                        .cornerRadius(10)
                                        .padding(.top, 20)
                                }
                            }
                            .padding(.horizontal)
                        }
                        .padding(.top, 10)
                    }
                    
                    Spacer()
                }
            }
        }
        .navigationBarHidden(true)
    }
    
    // 发送建议接口
    func sendSuggestion(suggestion: String) async {
        // 1. 构造请求URL
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/feedback/suggestion") else {
            print("URL 错误")
            return
        }
        
        // 2. 构造请求体，映射字段并带上当前时间ISO格式
        let isoDate = ISO8601DateFormatter().string(from: Date())
        let requestBody = SuggestionRequest(
            suggestion: suggestion,
            date: isoDate
        )
        
        // 3. 序列化JSON
        do {
            let jsonData = try JSONEncoder().encode(requestBody)
            print(String(data: jsonData, encoding: .utf8)!)
            
            // 4. 创建请求
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            
            // 5. 添加认证header（如果需要）
            if let authToken = UserDefaults.standard.string(forKey: "authToken") {
                request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
            }
            request.httpBody = jsonData
            
            // 6. 发送请求
            let (data, response) = try await URLSession.shared.data(for: request)
            
            // 7. 检查HTTP状态码
            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                print("服务器响应失败")
                return
            }
            
            if let responseString = String(data: data, encoding: .utf8) {
                print("服务器返回: \(responseString)")
            }
            
            // 8. 解码响应，确认是否成功
            let decoded = try JSONDecoder().decode(BasicResponse.self, from: data)
            if decoded.ok {
                print("提交成功：\(decoded.message)")
            } else {
                print("提交失败：\(decoded.message)")
            }
        } catch {
            print("请求失败：\(error.localizedDescription)")
        }
    }
}

struct SuggestionRequest: Codable {
    var suggestion: String
    var date: String
}

// MARK: - 收藏商品卡片组件
struct CollectionProductCard: View {
    let product: Commodity
    @StateObject private var viewModel = ImageUploadViewModel()
    @AppStorage("ID") var StudentNumber:String="202313201025"
    var image_url: String? {
        guard let pictureDetail = viewModel.pictureDetail else {
            return nil
        }
        
        // Convert StudentNumber to Int64 to match createdId's type
        guard let studentNumber = Int64(StudentNumber) else {
            return nil // or handle invalid conversion case
        }
        
        return (pictureDetail.status == 1 || pictureDetail.createdId == studentNumber) ? pictureDetail.url : nil
    }
    var body: some View {
        HStack(spacing: 15) {
            // 商品图片
            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(Color.white.opacity(0.1))
                    .frame(width: 80, height: 80)
                if let imagePath = image_url, let url = URL(string: imagePath) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .scaledToFill()
                                .frame(width: 80, height: 80)
                                .clipShape(RoundedRectangle(cornerRadius: 14))
                            
                        default:
                            Image(systemName:"photo.on.rectangle.angled")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 80, height: 80) // 限制默认图片的大小
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                                .foregroundStyle(
                                    LinearGradient(
                                        colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                        startPoint: .top,
                                        endPoint: .bottom
                                    )
                                )
                        }
                    }
                } else {
                    Image(systemName:"photo.on.rectangle.angled")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 80, height: 80) // 限制默认图片的大小
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .foregroundStyle(
                            LinearGradient(
                                colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )
                }
            }
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(
                        LinearGradient(
                            gradient: Gradient(colors: [Color.white.opacity(0.2), Color.pink.opacity(0.3)]),
                            startPoint: .top,
                            endPoint: .bottom
                        ),
                        lineWidth: 1
                    )
            )
            // 商品信息
            VStack(alignment: .leading, spacing: 6) {
                Text(product.name)
                    .font(.headline)
                    .foregroundColor(.white)
                    .lineLimit(1)
                
                Text("¥\(product.price, specifier: "%.2f")")
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundColor(.yellow)
                
                HStack(spacing: 8) {
                    Text(product.categoryDescription)
                        .font(.caption2)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.blue.opacity(0.3))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    
                    Text(product.statusDescription)
                        .font(.caption2)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(
                            product.status == 0 ? Color.green.opacity(0.3) : Color.orange.opacity(0.3)
                        )
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            }
            
            Spacer()
            
            // 收藏图标
            Image(systemName: "heart.fill")
                .font(.system(size: 20))
                .foregroundColor(.pink)
                .padding(10)
                .background(
                    Circle()
                        .fill(Color.pink.opacity(0.2))
                )
        }
        .padding(15)
        .background(
            RoundedRectangle(cornerRadius: 18)
                .fill(Color.white.opacity(0.08))
        )
        .overlay(
            callLinearGradient(
                gradient: Gradient(colors: [Color.white.opacity(0.1), Color.pink.opacity(0.3)]),
                lineWidth: 1,
                startPoint: .top,
                endPoint: .bottom
            )
        )
        .shadow(color: .black.opacity(0.2), radius: 8, x: 0, y: 4)
        .onAppear(){
            viewModel.fetchPictureDetail(with: product.image_id ?? 2)
        }
    }
}
// 自定义函数，返回一个线性渐变的边框
func callLinearGradient(gradient: Gradient, lineWidth: CGFloat, startPoint: UnitPoint, endPoint: UnitPoint) -> some View {
    return RoundedRectangle(cornerRadius: 16)
                    .stroke(
                        LinearGradient(
                            gradient: gradient,
                            startPoint: startPoint,
                            endPoint: endPoint
                        ),
                        lineWidth: lineWidth
                    )
            }

struct CollectedProductService {
    @AppStorage("authData") private var authData: String = ""
    static let pageSize = 10
    static func deleteProduct(pid: Int,authData:String) async throws ->Bool{
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/collection/remove") else {
            throw URLError(.badURL)
        }
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)!
        //请求参数
        components.queryItems = [
            URLQueryItem(name: "pid", value: "\(pid)"),
        ]
        //发请求
        var request = URLRequest(url: components.url!)
        request.httpMethod = "DELETE"
        request.addValue("Wuster \(authData)", forHTTPHeaderField: "Authorization")
        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        // 定义 ApiResponse 和 Commodity 模型
        struct ApiResponse: Codable {
            let code: Int
            let message: String
            let data: Bool // 使用完整路径
            let ok: Bool
        }
        do {
            // 解码外层的 API 响应数据
            let decodedResponse = try JSONDecoder().decode(ApiResponse.self, from: data)
            // 直接获取 data 数组
            let ok = decodedResponse.ok
                        // 返回商品数组
            return ok
        } catch {
            print("解码失败: \(error)")
            if let jsonString = String(data: data, encoding: .utf8) {
                print("原始 JSON: \(jsonString)")  // 输出原始 JSON 数据，帮助调试
            }
            throw error
        }

    }
    // ✅ 获取当前用户收藏商品列表
    static func fetchMyCollectedProducts() async throws -> [Commodity] {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/collection/list") else {
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
                let data: [Commodity]?
                let ok: Bool
            }
            do {
                // 解码外层的 API 响应数据
                let decodedResponse = try JSONDecoder().decode(ApiResponse.self, from: data)
                // 通过 data 获取 PageCommodity
                let pageCommodity = decodedResponse.data
                // 返回 records 数组，空数组也是有效的
                return pageCommodity ?? []
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
