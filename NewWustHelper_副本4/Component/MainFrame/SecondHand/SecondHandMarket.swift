import SwiftUI

// MARK: - 数据模型
struct Commodity: Identifiable, Codable {
    let pid: Int
    let uid: String
    var name: String
    var price: Double
    var date: String
    var contact: String
    var status: Int
    var type: Int
    var introduce: String
    var image_id: Int?

    // 计算属性
    var id: Int { pid }

    var statusDescription: String {
        switch status {
        case 0: return "出售"
        case 1: return "求购"
        default: return "其他"
        }
    }

    var categoryDescription: String {
        switch type {
        case 0: return "电子商品"
        case 1: return "生活用品"
        case 2: return "虚拟商品"
        case 3: return "学习用品"
        case 4: return "跑腿服务"
        default: return "其他"
        }
    }

    // 静态方法可供 Picker 使用
    static func sampleStatusDescription(for value: Int) -> String {
        switch value {
        case 0: return "出售"
        case 1: return "求购"
        default: return "其他"
        }
    }

    static func sampleCategoryDescription(for value: Int) -> String {
        switch value {
        case 0: return "电子商品"
        case 1: return "生活用品"
        case 2: return "虚拟商品"
        case 3: return "学习用品"
        case 4: return "跑腿服务(其它)"
        default: return "其他"
        }
    }

    // 默认构造函数
    init(pid: Int = 0, uid: String = "", name: String = "", price: Double = 0.0, date: String = "", contact: String = "", status: Int = 0, type: Int = 0, introduce: String = "", image_id: Int = 0) {
        self.pid = pid
        self.uid = uid
        self.name = name
        self.price = price
        self.date = date
        self.contact = contact
        self.status = status
        self.type = type
        self.introduce = introduce
        self.image_id = image_id
    }
}

struct PageCommodity: Codable {
    let records: [Commodity]
    let total: Int
    let size: Int
    let current: Int
    let pages: Int
}

struct GlobalResult<T: Codable>: Codable {
    let code: Int
    let message: String
    let data: T?
    let ok: Bool
}
struct PageResponse: Codable {
    let code: Int
    let message: String
    let data: PageCommodity
    let ok: Bool
}


// MARK: - 网络服务
class ProductService {
    //聚合查询
    struct BasicResponse: Codable {
        let code: Int
        let message: String
        let data:Bool
        let ok: Bool
    }
    
    static func DeleteProductToFavorites(pid: Int) async throws {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/collection/remove") else {
            throw URLError(.badURL)
        }
        
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)!
        components.queryItems = [
            URLQueryItem(name: "pid", value: "\(pid)")
        ]
        
        var request = URLRequest(url: components.url!)
        request.httpMethod = "DELETE"
        if let authToken = UserDefaults.standard.string(forKey: "authToken") {
            request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        }
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        
        do {
            let decoded = try JSONDecoder().decode(BasicResponse.self, from: data)
            guard decoded.ok else {
                throw NSError(domain: "取消收藏失败", code: decoded.code, userInfo: [NSLocalizedDescriptionKey: decoded.message])
            }
            print("收藏成功: \(decoded.message)")
        } catch {
            print("解码失败: \(error)")
            if let jsonString = String(data: data, encoding: .utf8) {
                print("原始 JSON: \(jsonString)")
            }
            throw error
        }
    }

    static func addProductToFavorites(pid:Int) async throws {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/collection/add") else {
            throw URLError(.badURL)
        }
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)!
        components.queryItems = [
            URLQueryItem(name: "pid", value: "\(pid)")
        ]
        var request = URLRequest(url: components.url!)
        request.httpMethod = "POST"
        if let authToken = UserDefaults.standard.string(forKey: "authToken") {
            request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        }
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        do {
            let decoded = try JSONDecoder().decode(BasicResponse.self, from: data)
            print("data: \(decoded.data)") // true/false
            guard decoded.ok else {
                throw NSError(domain: "收藏失败", code: decoded.code, userInfo: [NSLocalizedDescriptionKey: decoded.message])
            }
            print("收藏成功: \(decoded.message)")
        } catch {
            print("解码失败: \(error)")
            if let jsonString = String(data: data, encoding: .utf8) {
                print("原始 JSON: \(jsonString)")
            }
            throw error
        }
    }
    
    enum NetworkError: Error {
        case invalidURL
        // 可以根据需要添加其他错误类型，如请求失败、解析错误等
    }
    static func getProductsByAllAndSearch(text:String,status:Int,category: Int, page: Int, size: Int) async throws -> PageCommodity {
        guard let url1 = URL(string: "\(BasicValue.SecondHandbaseUrl)/selectController/byTypeOrStatus/\(page)/\(size)")else{
            throw NetworkError.invalidURL

        }
        guard let url2 = URL(string: "\(BasicValue.SecondHandbaseUrl)/selectController/byNameAndIntroduce/\(category)/\(status)/\(page)/\(size)")else{
            throw NetworkError.invalidURL

        }
        var chooseurl:URL
        if text==""{
            //不带搜索文本
            chooseurl=url1
            var components = URLComponents(url: chooseurl, resolvingAgainstBaseURL: false)!
            print(chooseurl)

            //请求参数
            components.queryItems = [
                URLQueryItem(name: "type", value: "\(category)"),
                URLQueryItem(name: "status", value: "\(status)")
            ]
            //发请求
            var request = URLRequest(url: components.url!)
            request.httpMethod = "GET"
            // 添加认证头（根据实际需要）
            //添加header
            if let authToken = UserDefaults.standard.string(forKey: "authToken") {
                request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
            }
            //发请求
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                throw URLError(.badServerResponse)
            }
            print(httpResponse.statusCode)
            do {
                let decoded = try JSONDecoder().decode(PageResponse.self, from: data)
                return decoded.data  // ✅ 返回 PageCommodity
            } catch {
                print("解码失败: \(error)")
                if let jsonString = String(data: data, encoding: .utf8) {
                    throw URLError(URLError.Code(rawValue: 201))
                    print("原始 JSON: \(jsonString)")
                }
                throw error
            }
        }else{
            //带搜索文本
            chooseurl=url2
            var components = URLComponents(url: chooseurl, resolvingAgainstBaseURL: false)!
            //请求参数
            components.queryItems = [
                URLQueryItem(name: "txt", value: text)
            ]

            var request = URLRequest(url: components.url!)
            request.httpMethod = "GET"
            // 添加认证头（根据实际需要）
            if let authToken = UserDefaults.standard.string(forKey: "authToken") {
                request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
            }
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                throw URLError(.badServerResponse)
            }
            do {
                let decoded = try JSONDecoder().decode(PageResponse.self, from: data)
                return decoded.data  // ✅ 返回 PageCommodity
            } catch {
                print("解码失败: \(error)")
                if let jsonString = String(data: data, encoding: .utf8) {
                    throw URLError(URLError.Code(rawValue: 201))
                    print("原始 JSON: \(jsonString)")
                }
                throw error
            }
        }
       
    }


    // 获取所有商品
    static func getAllProducts(page: Int, size: Int) async throws -> PageCommodity {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/showController/all/\(page)/\(size)")else{
            throw NetworkError.invalidURL
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        // 添加认证头
        if let authToken = UserDefaults.standard.string(forKey: "authToken") {
            request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        }
        
        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        do {
            let pageCommodity = try JSONDecoder().decode(PageCommodity.self, from: data)
            return pageCommodity
        } catch {
            print("解码失败: \(error)")
            if let jsonString = String(data: data, encoding: .utf8) {
                print("原始 JSON: \(jsonString)")
            }
            throw error
        }
    }
}
// MARK: - 主视图（全面优化）
struct SearchProductView: View {
    @Environment(\.dismiss) private var dismiss
    @AppStorage("authData") private var authData: String = ""
    @State private var selectedTab: Int = 0
    @State private var searchText = ""
    @State private var hasMoreData = true
    
    // 筛选状态
    @State private var selectedStatus: String = "全部状态"
    @State private var selectedCategory: String = "全部分类"
    @State private var showNotice = false
    
    // 商品数据
    @State private var products: [Commodity] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var Message: String?
    @State private var currentPage = 1
    private let pageSize = 10
    @State private var showDetailShoop = false
    @State private var chooseIndex = 0
    @State private var isRefreshing = false
    @State private var chooseProduct:Commodity=Commodity()
    // 分类映射
    private let categories: [String: Int] = [
        "全部分类": -1,
        "电子商品": 0,
        "生活用品": 1,
        "虚拟商品": 2,
        "学习用品": 3,
        "跑腿服务": 4
    ]
    
    // 状态映射
    private let statuses: [String: Int] = [
        "全部状态": -1,
        "出售": 0,
        "求购": 1,
        "其他": 2
    ]
    
    // 渐变色背景
    private let backgroundGradient = LinearGradient(
        gradient: Gradient(colors: [Color(#colorLiteral(red: 0.05, green: 0.1, blue: 0.2, alpha: 1)), Color(#colorLiteral(red: 0.15, green: 0.25, blue: 0.4, alpha: 1))]),
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                // 深色背景
                backgroundGradient
                    .edgesIgnoringSafeArea(.top)
                VStack(spacing: 0) {
         
                    // 顶部导航栏
                    HStack {
                        Button(action: { dismiss() }) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 40, height: 40)
                                .background(Color.white.opacity(0.15))
                                .clipShape(Circle())
                        }
                        
                        Spacer()
                        
                        Text(navigationTitle)
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.white).padding(.leading,10)
                        
                        Spacer()
                        
                        HStack(spacing: 20) {
                            tabButton(icon: "cart", tab: 0)
                            tabButton(icon: "person.circle", tab: 2)
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
                    Group {
                        switch selectedTab {
                        case 0: homeView
                        case 1: publishView
                        case 2: personalCenterView
                        default: homeView
                        }
                    }
                }
                
                // 右下角悬浮按钮
                if selectedTab != 1 {
                    FloatingActionButton()
                }
            }
            .navigationBarHidden(true)
            .onAppear {
                loadProducts()
                UserDefaults.standard.set(authData, forKey: "authToken")
            }
        }            .navigationBarHidden(true)
    }
    
    // MARK: - 组件
    private func tabButton(icon: String, tab: Int) -> some View {
        Button(action: { selectedTab = tab }) {
            if icon == "cart" {
                Image(systemName: icon).padding(.trailing,2)
                    .font(.system(size: 20))
                    .foregroundColor(selectedTab == tab ? .blue : .white)
                    .frame(width: 36, height: 36) // 图标的宽度小于圆圈，留有偏移空间
                    .background(selectedTab == tab ? Color.white : Color.clear)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(selectedTab == tab ? Color.blue : Color.white, lineWidth: 1.5)
                    )

            }else{
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(selectedTab == tab ? .blue : .white)
                    .frame(width: 36, height: 36)
                    .background(selectedTab == tab ? Color.white : Color.clear)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(selectedTab == tab ? Color.blue : Color.white, lineWidth: 1.5)
                    )
            }
        }
    }
    
    private func FloatingActionButton() -> some View {
        Button {
            selectedTab = 1
        } label: {
            Image(systemName: "plus")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
                .frame(width: 60, height: 60)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [Color.blue, Color.purple]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .clipShape(Circle())
                .shadow(color: Color.blue.opacity(0.7), radius: 10, x: 0, y: 5)
                .overlay(
                    Circle()
                        .stroke(Color.white, lineWidth: 2)
                )
        }
        .padding(25)
        .transition(.scale)
    }
    
    private var navigationTitle: String {
        switch selectedTab {
        case 0: return "校园集市"
        case 1: return "发布商品"
        case 2: return "个人中心"
        default: return ""
        }
    }
    
    // MARK: - 首页视图
    private var homeView: some View {
            VStack(spacing: 20) {
                // 搜索栏
                searchSection
                // 分类和状态选择
                VStack(spacing: 15) {
                    categorySection
                    statusSection
                }
                .padding(.horizontal)
                .padding(.vertical, 10)
                .background(Color.white.opacity(0.08))
                .cornerRadius(16)
                .padding(.horizontal, 10)
                
                // 商品列表
                productListSection
            }
            .padding(.top, 10)
       
    }
    
    private var searchSection: some View {
        HStack(spacing: 12) {
            TextField("搜索商品名称...", text: $searchText)
                .padding(.horizontal, 13)
                .padding(.vertical, 10)
                .background(Color.white.opacity(0.12))
                .foregroundColor(.white)
                .cornerRadius(19)
                .submitLabel(.search)
                .onSubmit(loadProducts)
//                .overlay(
//                    HStack {
//                        Image(systemName: "magnifyingglass")
//                            .foregroundColor(.white.opacity(0.7))
//                            .padding(.leading, 10)
//                        Spacer()
//                    }
//                )
//            
            Button(action: loadProducts) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 20))
                    .foregroundColor(.white)
                    .frame(width: 50, height: 50)
                    .background(Color.blue.opacity(0.3))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal)
    }
    
    private var categorySection: some View {
        VStack(alignment: .leading, spacing: 12) {
//            Text("商品分类")
//                .font(.caption2)
//                .foregroundColor(.white.opacity(0.8))
//                .padding(.leading, 5)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(["全部分类", "电子商品", "生活用品", "虚拟商品", "学习用品", "跑腿服务"], id: \.self) { category in
                        CategoryButton(
                            title: category,
                            isSelected: selectedCategory == category,
                            action: {
                                currentPage = 1
                                selectedCategory = category
                                loadProducts()
                            }
                        )
                    }
                }
                .padding(.horizontal, 5)
            }
        }
    }
    
    private var statusSection: some View {
        VStack(alignment: .leading, spacing: 12) {
//            Text("交易状态")
//                .font(.headline)
//                .foregroundColor(.white.opacity(0.8))
//                .padding(.leading, 5)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(["全部状态", "出售", "求购","其它"], id: \.self) { status in
                        StatusButton(
                            title: status,
                            isSelected: selectedStatus == status,
                            action: {
                                currentPage = 1
                                selectedStatus = status
                                loadProducts()
                            }
                        )
                    }
                }
                .padding(.horizontal, 5)
            }
        }
    }
    
    private var productListSection: some View {
        ScrollView(){
            Group {
                if isLoading && products.isEmpty {
                    VStack {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(1.5)
                        Text("加载中...")
                            .foregroundColor(.white)
                            .padding(.top, 10)
                    }
                    .frame(maxWidth: .infinity, minHeight: 300)
                } else if let errorMessage = errorMessage, products.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 40))
                            .foregroundColor(.yellow)
                        Text("加载失败")
                            .font(.title2)
                            .foregroundColor(.white)
                        Text(errorMessage)
                            .foregroundColor(.white.opacity(0.8))
                            .multilineTextAlignment(.center)
                        
                        Button(action: loadProducts) {
                            Text("重试")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(.horizontal, 30)
                                .padding(.vertical, 12)
                                .background(Color.blue)
                                .cornerRadius(25)
                        }
                        .padding(.top, 10)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, minHeight: 300)
                } else if products.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "bag.fill")
                            .font(.system(size: 50))
                            .foregroundColor(.white.opacity(0.3))
                        Text("没有找到商品")
                            .font(.title2)
                            .foregroundColor(.white)
                        Text("尝试修改搜索条件或发布新商品")
                            .foregroundColor(.white.opacity(0.7))
                    }
                    .padding()
                    .frame(maxWidth: .infinity, minHeight: 300)
                } else {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 20) {
                        ForEach(products.indices, id: \.self) { index in
                            ProductCardView(product: products[index], onTap: {
                                print("点击了第\(index)个商品")
                                chooseIndex=index
                                chooseProduct = products[index]
                                showDetailShoop=true
                            })
                            .onAppear {
                                if index == products.count - 1 && !isLoading && hasMoreData {
                                    loadMoreProducts()
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    .sheet(isPresented: $showDetailShoop) {
                        CommodityView(commodity: $chooseProduct, onSave: { action in
                            Task {
                                do {
                                    if action == 1 {
                                        try await ProductService.addProductToFavorites(pid: products[chooseIndex].pid)
                                    } else {
                                        try await ProductService.DeleteProductToFavorites(pid: products[chooseIndex].pid)
                                    }
                                } catch {
                                    errorMessage = "操作失败: \(error.localizedDescription)"
                                    showNotice = true
                                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                                        showNotice = false
                                    }
                                }
                            }
                        }, showError: $showNotice, ErrorMessage: $errorMessage)
                    }
                    
                    if isLoading && !products.isEmpty {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .padding(.vertical, 20)
                    } else if !hasMoreData && !products.isEmpty {
                        Text("没有更多商品了")
                            .font(.footnote)
                            .foregroundColor(.white.opacity(0.6))
                            .padding(.vertical, 20)
                    }
                }
            }
        } .refreshable {
            await refreshProducts()
        }
    }
    
    // MARK: - 发布视图
    @State private var newProduct = Commodity(
        pid: 0, uid: "", name: "", price: 0.0, date: "", contact: "",
        status: 0, type: 0, introduce: "", image_id: 0
    )
    
    enum ContactType: String, CaseIterable {
        case qq, email, phone, wechat
        var description: String {
            switch self {
            case .qq: return "QQ"
            case .email: return "邮箱"
            case .phone: return "手机"
            case .wechat: return "微信"
            }
        }
        
        var icon: String {
            switch self {
            case .qq: return "message.fill"
            case .email: return "envelope.fill"
            case .phone: return "phone.fill"
            case .wechat: return "bubble.left.fill"
            }
        }
    }
    
    @State private var selectedContactType: ContactType = .qq
    @State private var isContactValid = true
    @State private var uploadedPictureId:Int?=0
    private var publishView: some View {
        ZStack(){
            // 深色背景
            backgroundGradient
                .edgesIgnoringSafeArea(.top)
            ScrollView {
                VStack(spacing: 20) {
                    // 表单
                    VStack(alignment: .leading,spacing: 25) {
                        ImageUploadView(pictureId: $uploadedPictureId).padding(.leading,70)
                        // 分类选择
                        VStack(alignment: .leading, spacing: 12) {
                            Text("商品分类")
                                .font(.headline)
                                .foregroundColor(.white.opacity(0.8))
                            // 关键：用 ScrollView(.horizontal) 包裹 HStack，支持横向滚动
                            ScrollView(.horizontal, showsIndicators: false) { // showsIndicators: false 隐藏滚动条，更美观
                                HStack(spacing: 10) { // 调整间距为 10（原默认间距可能过大）
                                    ForEach(0..<5) { i in
                                        CategoryPill(
                                            title: Commodity.sampleCategoryDescription(for: i),
                                            isSelected: newProduct.type == i,
                                            action: { newProduct.type = i }
                                        )
                                        .fixedSize() // 关键：禁止 Pill 被压缩或拉伸，保持原有尺寸
                                    }
                                }
                            }
                        }
                        // 状态选择
                        VStack(alignment: .leading, spacing: 12) {
                            Text("交易状态")
                                .font(.headline)
                                .foregroundColor(.white.opacity(0.8))
                            HStack (spacing: 10){
                                ForEach(0..<3) { i in
                                    StatusPill(
                                        title: Commodity.sampleStatusDescription(for: i),
                                        isSelected: newProduct.status == i,
                                        action: { newProduct.status = i }
                                    )
                                }
                            }.padding(.leading,5)
                        }
                        
                        // 联系方式
                        VStack(alignment: .leading, spacing: 12) {
                            Text("联系方式")
                                .font(.headline)
                                .foregroundColor(.white.opacity(0.8))
                            
                            HStack {
                                ForEach(ContactType.allCases, id: \.self) { type in
                                    ContactTypeButton(
                                        icon: type.icon,
                                        title: type.description,
                                        isSelected: selectedContactType == type,
                                        action: { selectedContactType = type }
                                    )
                                }
                            }
                        }
                        
                        // 商品信息
                        VStack(alignment: .leading, spacing: 12) {
                            Text("商品信息")
                                .font(.headline)
                                .foregroundColor(.white.opacity(0.8))
                            
                            FloatingTextField(title: "商品名称", text: $newProduct.name)
                            FloatingTextField(title: "价格 (¥)", value: $newProduct.price, formatter: numberFormatter)
                            FloatingTextField(title: "联系方式", text: $newProduct.contact)
                        }
                        
                        // 商品描述
                        VStack(alignment: .leading, spacing: 12) {
                            Text("商品描述")
                                .font(.headline)
                                .foregroundColor(.white.opacity(0.8))
                            
                            CustomTextEditor(text: $newProduct.introduce)
                                .frame(minHeight: 120)
                                .padding(10)
                                .background(Color.white.opacity(0.1)) // 添加背景
                                .cornerRadius(12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color.white.opacity(0.3), lineWidth: 1)
                                )
                        }

                        // 发布按钮
                        Button(action: {
                            validateContact(contact: newProduct.contact)
                            if isContactValid {
                                Task {
                                    do {
                                        let success = try await publishProduct()
                                        if success {
                                            Message = "商品发布成功！"
                                            resetForm()
                                        }
                                    } catch {
                                        errorMessage = "发布失败: \(error.localizedDescription)"
                                    }
                                }
                            }
                        }) {
                            Text("发布商品")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding()
                                .frame(maxWidth: .infinity)
                                .background(LinearGradient(
                                    gradient: Gradient(colors: [Color.blue, Color.purple]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                ))
                                .cornerRadius(15)
                                .shadow(color: Color.blue.opacity(0.5), radius: 10, x: 0, y: 5)
                        }
                        .padding(.top, 10)
                    }
                    .padding()
                    .background(Color.white.opacity(0.08))
                    .cornerRadius(20)
                    .padding(.horizontal)
                    .padding(.bottom, 30)
                }
                
            }
            .alert(isPresented: .constant(errorMessage != nil || Message != nil)) {
                if let error = errorMessage {
                    return Alert(
                        title: Text("错误"),
                        message: Text(error),
                        dismissButton: .default(Text("确定")))
                } else {
                    return Alert(
                        title: Text("成功"),
                        message: Text(Message ?? "操作成功"),
                        dismissButton: .default(Text("确定")))
                }
            }
        }

    }
    
    // MARK: - 个人中心视图
    private var personalCenterView: some View {
        ScrollView {
            VStack(spacing: 25) {
//                // 用户信息卡片
//                VStack {
//                    Image(systemName: "person.circle.fill")
//                        .font(.system(size: 80))
//                        .foregroundColor(.white)
//                        .padding(.top, 30)
//                    
//                    Text(" ")
//                        .font(.title)
//                        .fontWeight(.bold)
//                        .foregroundColor(.white)
//                        .padding(.top, 10)
//                    
////                    Text("UID: \(authData.prefix(8))")
////                        .font(.subheadline)
////                        .foregroundColor(.white.opacity(0.7))
////                        .padding(.bottom, 30)
//                }
//                .frame(maxWidth: .infinity)
//                .background(
//                    LinearGradient(
//                        gradient: Gradient(colors: [Color.blue.opacity(0.4), Color.purple.opacity(0.4)]),
//                        startPoint: .top,
//                        endPoint: .bottom
//                    )
//                )
//                .cornerRadius(20)
//                .padding(.horizontal)
//                .shadow(color: Color.blue.opacity(0.3), radius: 10, x: 0, y: 5)
//                
                // 功能卡片
                VStack(spacing: 15) {
                    NavigationLink(destination: MyPublishView(selectTab: $selectedTab)) {
                        SettingRow(icon: "doc.text.fill", title: "我的发布", color: .blue)
                    }
                    
                    NavigationLink(destination: MyCollectionView(selectTab: $selectedTab)) {
                        SettingRow(icon: "heart.fill", title: "我的喜欢", color: .pink)
                    }
                    
                    Button(action: {}) {
                        SettingRow(icon: "gearshape.fill", title: "设置", color: .gray)
                    }
                    NavigationLink(destination: HelpCenterView(selectTab: $selectedTab)) {
                        SettingRow(icon: "questionmark.circle.fill", title: "帮助中心", color: .green)
                    }
                }
                .padding(.horizontal)
                .padding(.top,30)
                Spacer()
            }
            .padding(.top, 20)
        }
    }
    
    // MARK: - 辅助方法和组件
    private func CategoryButton(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .padding(.horizontal, 18)
                .padding(.vertical, 10)
                .background(
                    isSelected ? AnyView(
                        LinearGradient(
                            gradient: Gradient(colors: [Color.blue, Color.purple]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    ) : AnyView(Color.white.opacity(0.15))
                )
                .foregroundColor(isSelected ? .white : .white.opacity(0.9))
                .cornerRadius(15)
                .overlay(
                    RoundedRectangle(cornerRadius: 15)
                        .stroke(isSelected ? Color.blue : Color.white.opacity(0.3), lineWidth: 1)
                )
        }
    }
    
    private func StatusButton(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(
                    ZStack {
                        if isSelected {
                            LinearGradient(
                                gradient: Gradient(colors: [Color.green, Color.blue]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        } else {
                            Color.white.opacity(0.15)
                        }
                    }
                )
                .foregroundColor(isSelected ? .white : .white.opacity(0.9))
                .cornerRadius(15)
                .overlay(
                    RoundedRectangle(cornerRadius: 15)
                        .stroke(isSelected ? Color.green : Color.white.opacity(0.3), lineWidth: 1)
                )
        }
    }
    
    private func CategoryPill(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .padding(.horizontal, 15)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color.white.opacity(0.1))
                .foregroundColor(isSelected ? .white : .white.opacity(0.9))
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(isSelected ? Color.blue : Color.white.opacity(0.3), lineWidth: 1)
                )
        }
    }
    
    private func StatusPill(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
                .background(isSelected ? Color.green : Color.white.opacity(0.1))
                .foregroundColor(isSelected ? .white : .white.opacity(0.9))
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(isSelected ? Color.green : Color.white.opacity(0.3), lineWidth: 1)
                )
        }
    }
    
    private func ContactTypeButton(icon: String, title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(isSelected ? .white : .white.opacity(0.8))
                Text(title)
                    .font(.caption)
                    .foregroundColor(isSelected ? .white : .white.opacity(0.8))
            }
            .padding(10)
            .frame(maxWidth: .infinity)
            .background(isSelected ? Color.blue.opacity(0.5) : Color.clear)
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? Color.blue : Color.white.opacity(0.3), lineWidth: 1)
            )
        }
    }
    
    private func SettingRow(icon: String, title: String, color: Color) -> some View {
        HStack {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(color)
                .frame(width: 30)
            
            Text(title)
                .font(.headline)
                .foregroundColor(.white)
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.white.opacity(0.5))
        }
        .padding()
        .background(Color.white.opacity(0.1))
        .cornerRadius(15)
    }
    
    private func FloatingTextField(title: String, text: Binding<String>) -> some View {
        ZStack(alignment: .leading) {
            Text(title)
                .foregroundColor(Color.white.opacity(0.6))
                .offset(y: text.wrappedValue.isEmpty ? 0 : -25)
                .scaleEffect(text.wrappedValue.isEmpty ? 1 : 0.8, anchor: .leading)
                .animation(.spring(response: 0.3), value: text.wrappedValue)
            
            TextField("", text: text)
                .foregroundColor(.white)
        }
        .padding(.top, 15)
        .padding()
        .background(Color.white.opacity(0.1))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.white.opacity(0.3), lineWidth: 1)
        )
    }
    
    private func FloatingTextField(title: String, value: Binding<Double>, formatter: NumberFormatter) -> some View {
        let stringBinding = Binding<String>(
            get: { formatter.string(from: NSNumber(value: value.wrappedValue)) ?? "" },
            set: {
                if let number = formatter.number(from: $0) {
                    value.wrappedValue = number.doubleValue
                }
            }
        )
        
        return FloatingTextField(title: title, text: stringBinding)
            .keyboardType(.decimalPad)
    }
    
    private var numberFormatter: NumberFormatter {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = 2
        return formatter
    }
    
    // MARK: - 数据加载方法
    static func getStatusnumber(for value: String) -> Int {
        switch value {
        case "全部状态": return -1
        case "出售": return 0
        case "求购": return 1
        default: return 2
        }
    }
    
    private func loadProducts() {
           isLoading = true
           errorMessage = nil
           Task {
               do {
                   let result: PageCommodity
                   //如果内容为空 按分类查询
                   if !searchText.isEmpty,let categoryValue = categories[selectedCategory] {
                       currentPage=1
                       result = try await ProductService.getProductsByAllAndSearch(text: searchText, status:SearchProductView.getStatusnumber(for: selectedStatus), category: categoryValue, page: currentPage, size: pageSize)
                   //按内容+按分类查询
                   } else if selectedCategory != "全部分类",
                             let categoryValue = categories[selectedCategory] {
                       currentPage=1
                       result = try await ProductService.getProductsByAllAndSearch(text: searchText, status: SearchProductView.getStatusnumber(for: selectedStatus),
                           category: categoryValue,
                           page: currentPage,
                           size: pageSize
                       )
                   } else {
                       //查所有的
                       result = try await ProductService.getAllProducts(
                           page: currentPage,
                           size: pageSize
                       )
                   }

                   var filtered = result.records
                   if selectedStatus != "全部状态",
                      let statusValue = statuses[selectedStatus] {
                       filtered = filtered.filter { $0.status == statusValue }
                   }

                   await MainActor.run {
                       products = filtered
                       isLoading = false
                       errorMessage = nil
                   hasMoreData = filtered.count == pageSize
                       

                   }
               } catch {
                   await MainActor.run {
                       errorMessage = error.localizedDescription
                       isLoading = false
                   }
               }
           }
       }
    
    private func loadMoreProducts() {
           guard !isLoading else { return }
           currentPage += 1
           isLoading = true
           print(currentPage)
           Task {
               do {
                   let result: PageCommodity
                   
                   if !searchText.isEmpty,let categoryValue = categories[selectedCategory] {
                       result = try await ProductService.getProductsByAllAndSearch(text: searchText, status:SearchProductView.getStatusnumber(for: selectedStatus), category: categoryValue, page: currentPage, size: pageSize)
                   //按内容+按分类查询
                   } else if selectedCategory != "全部分类",
                             let categoryValue = categories[selectedCategory] {
                       result = try await ProductService.getProductsByAllAndSearch(text: searchText, status: SearchProductView.getStatusnumber(for: selectedStatus),
                           category: categoryValue,
                           page: currentPage,
                           size: pageSize
                       )
                   } else {
                       result = try await ProductService.getAllProducts(
                           page: currentPage,
                           size: pageSize
                       )
                   }

                   var filtered = result.records
                   if selectedStatus != "全部状态",
                      let statusValue = statuses[selectedStatus] {
                       filtered = filtered.filter { $0.status == statusValue }
                   }

                   await MainActor.run {
                       if filtered.isEmpty {
                           currentPage -= 1
                           hasMoreData = false
                       } else {
                           products.append(contentsOf: filtered)
                           hasMoreData = filtered.count == pageSize
                       }
                       isLoading = false
                   }

               } catch {
                   await MainActor.run {
                       errorMessage = "加载更多失败: \(error.localizedDescription)"
                       print(error.localizedDescription)
                       isLoading = false
                       currentPage -= 1
                   }
               }
           }
       }
    private func refreshProducts() async {
         isLoading = true
         errorMessage = nil
         currentPage=1
         do {
             let result: PageCommodity
             if !searchText.isEmpty,let categoryValue = categories[selectedCategory] {
                 result = try await ProductService.getProductsByAllAndSearch(text: searchText, status:SearchProductView.getStatusnumber(for: selectedStatus), category: categoryValue, page: currentPage, size: pageSize)
             //按内容+按分类查询
             } else if selectedCategory != "全部分类",
                       let categoryValue = categories[selectedCategory] {
                 result = try await ProductService.getProductsByAllAndSearch(text: searchText, status: SearchProductView.getStatusnumber(for: selectedStatus),
                     category: categoryValue,
                     page: currentPage,
                     size: pageSize
                 )
             }else {
                 result = try await ProductService.getAllProducts(
                     page: currentPage,
                     size: pageSize
                 )
             }

             var filtered = result.records
             if selectedStatus != "全部状态",
                let statusValue = statuses[selectedStatus] {
                 filtered = filtered.filter { $0.status == statusValue }
             }

             await MainActor.run {
                 products = filtered
                 isLoading = false
                 errorMessage = nil
             }
         } catch {
             await MainActor.run {
                 errorMessage = "刷新失败: \(error.localizedDescription)"
                 isLoading = false
             }
         }
     }
    struct PublishProduct: Codable {
        var name: String
        var price: Double
        var date: String
        var contact: String
        var status: Int
        var type: Int
        var introduce: String
        var imageId: Int?
    }

    struct BasicResponse: Codable {
        let code: Int
        let message: String
        let data: String?
        let ok: Bool
    }
    // MARK: - 发布相关方法
     func publishProduct() async throws -> Bool {
        // 1. 构造请求URL
         guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/publishController/publish") else {
            throw URLError(.badURL)
        }
        // 2. 构造请求体，映射字段并带上当前时间ISO格式
        let isoDate = ISO8601DateFormatter().string(from: Date())
        let requestBody = PublishProduct(
            name: newProduct.name,
            price: newProduct.price,
            date: isoDate,
            contact: newProduct.contact,
            status: newProduct.status,
            
            type: newProduct.type,
            introduce: newProduct.introduce,
            imageId: uploadedPictureId
        )
         // 3. 序列化JSON
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
            throw URLError(.badServerResponse)
        }
      
        if let responseString = String(data: data, encoding: .utf8) {
            print("服务器返回: \(responseString)")
        }
        // 8. 解码响应，确认是否成功
        let decoded = try JSONDecoder().decode(BasicResponse.self, from: data)
      

        if decoded.ok {
            print("发布成功：\(decoded.message)")
            return true
        } else {
            throw NSError(domain: "发布失败", code: decoded.code, userInfo: [NSLocalizedDescriptionKey: decoded.message])
        }
    }
    
  func validateContact(contact: String) {
        switch selectedContactType {
        case .qq:
            isContactValid = isValidQQ(contact)
        case .email:
            isContactValid = isValidEmail(contact)
        case .phone:
            isContactValid = isValidPhone(contact)
        case .wechat:
            isContactValid = isValidWechat(contact)
        }
        
        if !isContactValid {
            errorMessage = "请输入有效的\(selectedContactType.description)"
        } else {
            errorMessage = nil
        }
    }
        // 验证 QQ 格式
    func isValidQQ(_ qq: String) -> Bool {
        let qqRegex = "^[1-9][0-9]{4,10}$"
        let qqPredicate = NSPredicate(format: "SELF MATCHES %@", qqRegex)
        return qqPredicate.evaluate(with: qq)
    }

    // 验证邮箱格式
    func isValidEmail(_ email: String) -> Bool {
        let emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: email)
    }

    // 验证手机号格式
    func isValidPhone(_ phone: String) -> Bool {
        let phoneRegex = "^1[3-9]\\d{9}$"  // 中国的手机号规则
        let phonePredicate = NSPredicate(format: "SELF MATCHES %@", phoneRegex)
        return phonePredicate.evaluate(with: phone)
    }

    // 验证微信号格式
    func isValidWechat(_ wechat: String) -> Bool {
        let wechatRegex = "^[a-zA-Z][a-zA-Z0-9_-]{5,19}$"
        let wechatPredicate = NSPredicate(format: "SELF MATCHES %@", wechatRegex)
        return wechatPredicate.evaluate(with: wechat)
    }
    
    private func resetForm() {
        newProduct = Commodity(
            pid: 0, uid: "", name: "", price: 0.0, date: "", contact: "",
            status: 0, type: 0, introduce: "", image_id: 0
        )
    }
}
struct CustomTextEditor: UIViewRepresentable {
    @Binding var text: String
    
    // 用于处理UITextView的代理回调
    class Coordinator: NSObject, UITextViewDelegate {
        var parent: CustomTextEditor
        
        init(parent: CustomTextEditor) {
            self.parent = parent
        }
        
        // 监听文本变化，同步到@Binding变量
        func textViewDidChange(_ textView: UITextView) {
            parent.text = textView.text
        }
    }
    
    // 创建Coordinator
    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }
    
    func makeUIView(context: Context) -> UITextView {
        let textView = UITextView()
        textView.backgroundColor = .clear
        textView.textColor = .white
        textView.font = UIFont.systemFont(ofSize: 17)
        // 设置代理为Coordinator，监听文本变化
        textView.delegate = context.coordinator
        // 确保可编辑（默认是true，但显式设置更安全）
        textView.isEditable = true
        // 确保用户交互开启
        textView.isUserInteractionEnabled = true
        return textView
    }
    
    func updateUIView(_ uiView: UITextView, context: Context) {
        // 避免不必要的刷新（当text未变化时不更新）
        if uiView.text != text {
            uiView.text = text
        }
    }
}



// MARK: - 商品卡片视图（优化）
struct ProductCardView: View {
    @AppStorage("ID") var StudentNumber:String="202313201025"

    let product: Commodity
    var onTap: (() -> Void)?  // 点击回调
    @StateObject private var viewModel = ImageUploadViewModel()
    // 计算属性
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
        Button(action: {
            onTap?()
        }) {
            VStack(alignment: .leading, spacing: 12) {
                // 商品图片
                ZStack {
                    if let imagePath = image_url, let url = URL(string: "\(imagePath)") {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .scaledToFill() // 保持比例缩放，避免变形
                                    .frame(width: 150, height: 150) // 限制宽高，确保自适应
                                    .clipShape(RoundedRectangle(cornerRadius: 14)).overlay{
                                        //如果是自己发布的
                                        if(product.uid==StudentNumber){
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
                                                .padding(.leading, 150)
                                                .padding(.bottom, 150)
                                            // 优化：仅当有详情数据时显示，无数据时隐藏
                                                .opacity(viewModel.pictureDetail != nil ? viewModel.pictureDetail?.status == 1 ? 0:1 : 0)
                                        }
                                    }
                            default:
                                Image(systemName:"photo.on.rectangle.angled")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 150, height: 150) // 限制默认图片的大小
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
                            .frame(width: 150, height: 150) // 限制默认图片的大小
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
                    callLinearGradient(
                        gradient: Gradient(colors: [Color.white.opacity(0.2), Color.blue.opacity(0.3)]),
                        lineWidth: 1,
                        startPoint: .top,
                        endPoint: .bottom
                    )
                   
                )
                .shadow(color: .black.opacity(0.2), radius: 8, x: 0, y: 4)

                // 商品信息
                VStack(alignment: .leading, spacing: 8) {
                    Text(product.name)
                        .font(.headline)
                        .foregroundColor(.white)
                        .lineLimit(1)

                    Text("¥\(product.price, specifier: "%.2f")")
                        .font(.title3)
                        .fontWeight(.bold)
                        .foregroundColor(.yellow)

                    HStack {
                        Text(product.categoryDescription)
                            .font(.caption)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color.blue.opacity(0.3))
                            .foregroundColor(.white)
                            .cornerRadius(8)

                        Spacer()

                        Text(product.statusDescription)
                            .font(.caption)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(product.status == 0 ? Color.green.opacity(0.3) : Color.orange.opacity(0.3))
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                }
                .padding(.bottom, 5)
            }
            .onAppear {
                viewModel.fetchPictureDetail(with: product.image_id ?? 2)
            }
            .padding(10)
            .background(Color.white.opacity(0.08))
            .overlay(
                callLinearGradient(
                    gradient: Gradient(colors: [Color.white.opacity(0.2), Color.blue.opacity(0.3)]),
                    lineWidth: 1,
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .shadow(color: .black.opacity(0.2), radius: 8, x: 0, y: 4)
        }

    }
}
struct SuggestionView: View {
    private let urlString = "https://txc.qq.com/products/776354"
    // 用于控制弹窗显示的状态变量，初始值为true表示初始化时就显示
    @State private var showAlert = true
    
    var body: some View {
        CustomWebView(urlString: urlString)
            .alert("提示", isPresented: $showAlert) {
                // 弹窗中的确认按钮
                Button("知道了", role: .cancel) {
                    // 点击后关闭弹窗
                    showAlert = false
                }
            } message: {
                // 弹窗提示内容
                Text("提交反馈请选择游客登录")
            }
    }
}



// MARK: - 预览
struct SearchProductView_Previews: PreviewProvider {
    static var previews: some View {
        SearchProductView()
    }
}
