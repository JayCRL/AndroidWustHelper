import SwiftUI

//商品详细
struct CommodityView: View {
    @Binding var commodity: Commodity
    @State var isFavorite=false  // 收藏状态
    var onSave:(Int) -> Void
    @Binding var showError:Bool
    @Binding var ErrorMessage:String?
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
    func getIsFavorite() -> Bool {
        return true
    }
    var body: some View {
        ZStack(){
            ScrollView {
                VStack(spacing: 20) {
                    // 卡片式图片
                    if let imagePath = image_url,
                       let url = URL(string: imagePath) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .empty:
                                Image(systemName:"photo.on.rectangle.angled")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 300, height: 300) // 限制默认图片的大小
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                    .foregroundStyle(
                                        LinearGradient(
                                            colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                            startPoint: .top,
                                            endPoint: .bottom
                                        )
                                    )
                            case .success(let image):
                                image
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 300, height: 300) // 限制默认图片的大小
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                    .foregroundStyle(
                                        LinearGradient(
                                            colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                            startPoint: .top,
                                            endPoint: .bottom
                                        )
                                    ).overlay{
                                        //如果是自己发布的
                                        if(commodity.uid==StudentNumber){
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
                                                .frame(width: 30, height: 30)
                                                .padding(.leading, 300)
                                                .padding(.bottom, 300)
                                            // 优化：仅当有详情数据时显示，无数据时隐藏
                                                .opacity(viewModel.pictureDetail != nil ? viewModel.pictureDetail?.status == 1 ? 0:1 : 0)
                                        }
                                    }
                            case .failure:
                                Image(systemName:"photo.on.rectangle.angled")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 300, height: 300) // 限制默认图片的大小
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                    .foregroundStyle(
                                        LinearGradient(
                                            colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                            startPoint: .top,
                                            endPoint: .bottom
                                        )
                                    )
                            @unknown default:
                                EmptyView()
                            }
                        }
                        .padding(.horizontal)
                    }else{
                        Image(systemName:"photo.on.rectangle.angled")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 300, height: 300) // 限制默认图片的大小
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .foregroundStyle(
                                LinearGradient(
                                    colors: [Color.blue.opacity(0.8), Color.blue.opacity(0.5)],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )
                    }
                    // 信息卡片
                    VStack(alignment: .leading, spacing: 16) {
                        HStack {
                            Text(commodity.name)
                                .font(.system(size: 28, weight: .bold))
                                .foregroundColor(.primary)
                            Spacer()
                            Button {
                                withAnimation(.easeInOut(duration: 0.2)) {
                                    isFavorite.toggle()
                                    if isFavorite{
                                        onSave(1)
                                    }else{
                                        onSave(0)
                                    }
                                }
                            } label: {
                                Image(systemName: isFavorite ? "heart.fill" : "heart")
                                    .font(.system(size: 28))
                                    .foregroundColor(isFavorite ? .red : .gray)
                                    .scaleEffect(isFavorite ? 1.2 : 1)
                                    .shadow(radius: isFavorite ? 4 : 0)
                            }.padding(.trailing,9)
                            .buttonStyle(PlainButtonStyle())
                        }
                        
                        HStack(spacing: 10) {
                            Text("¥\(commodity.price, specifier: "%.2f")")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.accentColor)
                            Spacer()
                            Text(commodity.statusDescription)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(Color.secondary.opacity(0.1))
                                .cornerRadius(8)
                        }
                        
                        Divider()
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("联系方式")
                                .font(.headline)
                            Text(commodity.contact)
                                .foregroundColor(.secondary)
                                .font(.body)
                        }
                        
                        Divider()
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("商品介绍")
                                .font(.headline)
                            Text(commodity.introduce)
                                .font(.body)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                    }
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .fill(Color(UIColor.systemBackground))
                            .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
                    )
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .background(Color(UIColor.systemGroupedBackground).ignoresSafeArea())
        }.onAppear {
            print("CommodityView appeared for commodity: \(commodity.name)")
            print("INDEX\(commodity.pid)")
            viewModel.fetchPictureDetail(with: commodity.image_id ?? 2)
            Task {
                do {
                    let isFav:Bool = try await CommodityView.getWhetherProductToFavorite(pid: commodity.pid)
                    // 假设你有 @State var isFavorite，更新它：
                    DispatchQueue.main.async {
                        self.isFavorite = isFav
                    }
                } catch {
                    print("获取收藏状态失败: \(error)")
                }
            }
        }
        if showError{
                Text("出错啦😫:\(ErrorMessage)")
                    .foregroundColor(.white)
                    .padding(12)
                    .background(Color.red.opacity(0.6))
                    .cornerRadius(8)
                    .transition(.opacity.combined(with: .scale)) // 淡入淡出+缩放动画
        }
    }
    static func getWhetherProductToFavorite(pid: Int) async throws -> Bool {
        // 构建请求 URL
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/select/\(pid)") else {
            throw URLError(.badURL)
        }

        // 使用 URLComponents 来构建带查询参数的 URL
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)!
        components.queryItems = [
            URLQueryItem(name: "pid", value: "\(pid)")
        ]

        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        
        // 如果有 authToken，添加到请求头中
        if let authToken = UserDefaults.standard.string(forKey: "authToken") {
            request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        }
          // 声明数据和响应变量
          var data: Data = Data() // 默认初始化一个空的 Data 对象
          var response: URLResponse? // 响应变量
        do {
            // 发起请求并等待返回
            let (data, response) = try await URLSession.shared.data(for: request)

            // 检查 HTTP 状态码是否为 200
            guard let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                throw URLError(.badServerResponse)
            }

            // 解码返回的 JSON 数据
            let decoded = try JSONDecoder().decode(BasicResponse.self, from: data)

            // 打印调试信息
            print("data: \(decoded.data)") // "商品已收藏" 或 "商品未收藏"
            
            // 检查接口是否成功
            guard decoded.ok else {
                throw NSError(domain: "收藏失败", code: decoded.code, userInfo: [NSLocalizedDescriptionKey: decoded.message])
            }

            // 判断 data 字段的值
            if decoded.data == "商品已收藏" {
                print("商品已收藏")
                return true  // 商品已收藏
            } else if decoded.data == "商品未收藏" {
                print("商品未收藏")
                return false  // 商品未收藏
            } else {
                throw NSError(domain: "未知状态", code: 0, userInfo: [NSLocalizedDescriptionKey: "未知的收藏状态"])
            }
        } catch {
            // 捕获错误并输出原始 JSON 数据
            print("解码失败: \(error)")
            if let jsonString = String(data: data, encoding: .utf8) {
                print("原始 JSON: \(jsonString)")
            }
            throw error
        }
    }

    struct BasicResponse: Codable {
        let code: Int
        let message: String
        let data: String
        let ok: Bool
    }

    static func checkFavorite(pid: Int) async -> Bool {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/userController/select/\(pid)") else {
            return false
        }
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)!
        components.queryItems = [URLQueryItem(name: "pid", value: "\(pid)")]
        
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        if let authToken = UserDefaults.standard.string(forKey: "authToken") {
            request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        }
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse,
                  httpResponse.statusCode == 200 else {
                return false
            }
            
            let decoded = try JSONDecoder().decode(BasicResponse.self, from: data)
            guard decoded.ok else {
                print("操作失败: \(decoded.message)")
                return false
            }
            print("返回信息: \(decoded.message), data: \(decoded.data)")
            // 根据接口返回的data判断是否收藏，比如
            return decoded.data == "商品已收藏"
        } catch {
            print("请求或解码失败: \(error)")
            return false
        }
    }

    static func addProductToFavorites(pid: Int) async throws {
        guard let url = URL(string: "\(BasicValue.SecondHandbaseUrl)/UserController/collection/add") else {
            throw URLError(.badURL)
        }
        
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)!
        components.queryItems = [
            URLQueryItem(name: "pid", value: "\(pid)")
        ]
        
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
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
}
#Preview {
}
