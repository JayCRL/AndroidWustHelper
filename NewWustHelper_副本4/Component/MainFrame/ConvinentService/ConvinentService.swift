import SwiftUI

struct StudentCommunityBook: View {
    @State private var isLibrary: Bool = false
    @State private var isOneStation: Bool = false
    @State private var isXinLiHuoDong: Bool = false
    @State private var isKDJL: Bool = false
    @State private var selectedTab: Int = 0
    @State var isInto=false;
    var body: some View {
        NavigationView {
            ZStack {
                // 动态背景
                if isInto{
                    AngularGradient(
                    gradient: Gradient(colors: [Color.blue, Color("pi")]),
                    center: .bottomTrailing,
                    angle: .degrees(45.0))
                    }else{
                    AngularGradient(
                        gradient: Gradient(colors: [Color.blue, Color("pi")]),
                        center: .bottomTrailing,
                        angle: .degrees(45.0))
                }
                VStack {
                    Spacer()
                    // 顶部导航
                  //  HeaderView(selectedTab: $selectedTab)
                    // 主内容区
                    //if selectedTab == 0 {
                        ServiceGridView(isLibrary: $isLibrary, isKdjl: $isKDJL, isOneStation: $isOneStation, isXinLiHuoDongShi: $isXinLiHuoDong,isInto: $isInto)
                    //} else {
                       // FeaturedContentView()
                    //}
                    Spacer()
                    // 底部信息栏
//                    FooterInfoView()
                }
                .padding(.horizontal)
                
                // 图书馆预约视图
                if isLibrary {
                    CustomWebView(urlString: "https://auth.wust.edu.cn/lyuapServer/login?service=http://ic.lib.wust.edu.cn/loginmall.aspx")
                        .transition(.move(edge: .trailing).combined(with: .opacity))
                        .zIndex(1)
                }
            }.sheet(isPresented: $isXinLiHuoDong, content: {
                    XinLiHuoDong()
                        .zIndex(1)
            }).sheet(isPresented: $isKDJL, content: {
                KDJL()
                    .zIndex(1)
        }).transition(.move(edge: .trailing).combined(with: .opacity))
                .sheet(isPresented: $isOneStation, content: {
                    OneStationBookView()
                            .zIndex(1)
                }).transition(.move(edge: .trailing).combined(with: .opacity))
            .navigationBarHidden(true)
        }
        .accentColor(Color("primaryBlue"))
    }
}

// MARK: - 动态背景视图
struct DynamicBackground: View {
    @State private var animateGradient = false
    var body: some View {
        LinearGradient(
            gradient: Gradient(colors: [
                Color("primaryBlue"),
                Color("secondaryBlue"),
                Color("lightTeal")
            ]),
            startPoint: animateGradient ? .topLeading : .bottomLeading,
            endPoint: animateGradient ? .bottomTrailing : .topTrailing
        )
        .edgesIgnoringSafeArea(.all)
        .overlay(
            VisualEffectBlur(blurStyle: .systemUltraThinMaterialDark)
                .edgesIgnoringSafeArea(.all)
        )
        .onAppear {
            withAnimation(Animation.easeInOut(duration: 8).repeatForever(autoreverses: true)) {
                animateGradient.toggle()
            }
        }
    }
}

// MARK: - 顶部导航视图
struct HeaderView: View {
    @Binding var selectedTab: Int
    let tabs = ["服务", "推荐"]
    
    var body: some View {
        VStack(spacing: 15) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("校园服务")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white).padding(.leading,20)
                    
                    Text("便捷预约，高效服务")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.8)).padding(.leading,20)
                }
                
                Spacer()
                // 用户头像
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .frame(width: 42, height: 42)
                    .foregroundColor(.white)
                    .background(Color("lightTeal"))
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color.white, lineWidth: 2))
            }
            .padding(.top, 10)
            
            // 选项卡
            HStack(spacing: 0) {
                ForEach(0..<tabs.count, id: \.self) { index in
                    Button(action: {
                        withAnimation(.spring()) {
                            selectedTab = index
                        }
                    }) {
                        Text(tabs[index])
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(selectedTab == index ? .white : .white.opacity(0.6))
                            .padding(.vertical, 10)
                            .frame(maxWidth: .infinity)
                            .background(
                                selectedTab == index ? Color.white.opacity(0.2) : Color.clear
                            )
                            .cornerRadius(12)
                    }
                }
            }
            .background(Color.black.opacity(0.1))
            .cornerRadius(12)
            .padding(.bottom, 5)
        }
    }
}

// MARK: - 服务网格视图
struct ServiceGridView: View {
    @Binding var isLibrary: Bool
    @Binding var isKdjl: Bool
    @Binding var isOneStation: Bool
    @Binding var isXinLiHuoDongShi: Bool
    @Binding var isInto:Bool
    let services = [
        ("books.vertical.fill", "电子图书馆", "海量电子资源"),
        ("chair.lounge.fill", "图书馆预约", "座位与空间预约"),
        ("sofa.fill", "一站式预约", "综合服务预约"),
        ("house.fill", "心理活动室", "心理咨询与活动")
    ]
    
    var body: some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 20), count: 2), spacing: 20) {
            ForEach(services, id: \.1) { service in
                Button(action: {
                    if service.1 == "电子图书馆" {
                        withAnimation(.spring()) {
                            isKdjl = true
                        }
                    }
                    if service.1 == "图书馆预约" {
                        withAnimation(.spring()) {
                            isLibrary = true
                        }
                    }
                    if service.1 == "一站式预约" {
                        withAnimation(.spring()) {
                            isOneStation = true
                        }
                    }
                    if service.1 == "心理活动室" {
                        withAnimation(.spring()) {
                            isXinLiHuoDongShi = true
                        }
                    }
                    isInto=true
                }) {
                    ServiceCardView(icon: service.0, title: service.1, subtitle: service.2)
                }
                .buttonStyle(ScaleButtonStyle())
            }
        }
        .padding(.top, 10)
    }
}

// MARK: - 服务卡片视图
struct ServiceCardView: View {
    let icon: String
    let title: String
    let subtitle: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 15) {
            ZStack {
                Circle()
                    .fill(LinearGradient(
                        gradient: Gradient(colors: [Color("lightTeal"), Color("primaryBlue")]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ))
                    .frame(width: 60, height: 60)
                
                Image(systemName: icon)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 30, height: 30)
                    .foregroundColor(.white)
            }
            
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            
            Text(subtitle)
                .font(.system(size: 14))
                .foregroundColor(.white.opacity(0.8))
                .lineLimit(2)
            
            Spacer()
        }
        .padding(20)
        .frame(height: 200)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.white.opacity(0.12))
                .background(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.white.opacity(0.2), lineWidth: 1)
                )
        )
        .compositingGroup()
        .shadow(color: Color.black.opacity(0.2), radius: 12, x: 0, y: 6)
    }
}

//// MARK: - 推荐内容视图
//struct FeaturedContentView: View {
//    var body: some View {
//        ScrollView(.vertical, showsIndicators: false) {
//            VStack(spacing: 20) {
//                Text("本周推荐")
//                    .font(.title2)
//                    .bold()
//                    .foregroundColor(.white)
//                    .frame(maxWidth: .infinity, alignment: .leading)
//                    .padding(.top, 10)
//                
//                // 推荐卡片1
//                FeaturedCard(
//                    title: "新书速递",
//                    subtitle: "探索最新馆藏资源",
//                    icon: "book.fill",
//                    color: Color("lightTeal"),
//                    content: "本月新增图书500余册，涵盖人工智能、量子计算、人文社科等多个领域。"
//                )
//                
//                // 推荐卡片2
//                FeaturedCard(
//                    title: "心理讲座",
//                    subtitle: "压力管理与自我调节",
//                    icon: "brain.head.profile",
//                    color: Color("lightPurple"),
//                    content: "本周五下午3点，知名心理学家张教授将带来「压力管理与自我调节」专题讲座。"
//                )
//                
//                // 推荐卡片3
//                FeaturedCard(
//                    title: "学习空间",
//                    subtitle: "新增小组讨论室",
//                    icon: "person.3.fill",
//                    color: Color("lightOrange"),
//                    content: "图书馆3层新增4间小组讨论室，配备智能白板和视频会议设备。"
//                )
//            }
//        }
//    }
//}
//
//// MARK: - 推荐卡片视图
//struct FeaturedCard: View {
//    let title: String
//    let subtitle: String
//    let icon: String
//    let color: Color
//    let content: String
//    
//    var body: some View {
//        VStack(alignment: .leading, spacing: 15) {
//            HStack {
//                ZStack {
//                    Circle()
//                        .fill(color.opacity(0.3))
//                        .frame(width: 44, height: 44)
//                    
//                    Image(systemName: icon)
//                        .foregroundColor(color)
//                        .font(.system(size: 20))
//                }
//                
//                VStack(alignment: .leading) {
//                    Text(title)
//                        .font(.system(size: 18, weight: .bold))
//                        .foregroundColor(.white)
//                    
//                    Text(subtitle)
//                        .font(.system(size: 14))
//                        .foregroundColor(.white.opacity(0.8))
//                }
//                
//                Spacer()
//            }
//            
//            Text(content)
//                .font(.system(size: 15))
//                .foregroundColor(.white.opacity(0.9))
//                .lineLimit(3)
//                .fixedSize(horizontal: false, vertical: true)
//            
//            Button(action: {}) {
//                Text("查看详情")
//                    .font(.system(size: 14, weight: .medium))
//                    .foregroundColor(color)
//                    .padding(.horizontal, 20)
//                    .padding(.vertical, 8)
//                    .background(color.opacity(0.2))
//                    .cornerRadius(12)
//            }
//        }
//        .padding(20)
//        .background(
//            RoundedRectangle(cornerRadius: 20)
//                .fill(Color.white.opacity(0.08))
//                .background(
//                    RoundedRectangle(cornerRadius: 20)
//                        .stroke(Color.white.opacity(0.15), lineWidth: 1)
//                )
//        )
//    }
//}

//// MARK: - 底部信息视图
//struct FooterInfoView: View {
//    var body: some View {
//        VStack(spacing: 8) {
//            Text("当前服务状态：正常")
//                .font(.caption)
//                .foregroundColor(.white.opacity(0.7))
//        }
//        .padding(.vertical, 15)
//    }
//}

// MARK: - 视觉模糊效果
struct VisualEffectBlur: UIViewRepresentable {
    var blurStyle: UIBlurEffect.Style
    
    func makeUIView(context: Context) -> UIVisualEffectView {
        return UIVisualEffectView(effect: UIBlurEffect(style: blurStyle))
    }
    
    func updateUIView(_ uiView: UIVisualEffectView, context: Context) {
        uiView.effect = UIBlurEffect(style: blurStyle)
    }
}

// MARK: - 按钮动画效果
struct ScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Self.Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.spring(), value: configuration.isPressed)
    }
}

// MARK: - 预览
#Preview {
    StudentCommunityBook()
        .preferredColorScheme(.dark)
}

// MARK: - 颜色扩展
extension Color {
    static let primaryBlue = Color("primaryBlue")
    static let secondaryBlue = Color("secondaryBlue")
    static let lightTeal = Color("lightTeal")
    static let lightPurple = Color("lightPurple")
    static let lightOrange = Color("lightOrange")
    static let cardBackground = Color("cardBackground")
}
struct OneStationBookView:View {
    var body: some View {
        Image("YIZHANSHI").resizable().frame(width: 300,height: 300)
        Text("微信扫码打开小程序").foregroundColor(Color.gray)
    }
}

struct XinLiHuoDong:View {
    var body: some View {
        Image("psychologychol").resizable().frame(width: 300,height: 300)

    }
}
struct KDJL: View {
    var body: some View {
        VStack {
            Image("kdjl")
                .resizable()
                .frame(width: 300, height: 350)
                .padding(.top,100)
            Text("微信小程序搜索🔍")
            HStack {
                Text("武汉科技大学图书精灵")
                    .foregroundColor(.blue)

                Button(action: {
                    UIPasteboard.general.string = "武汉科技大学图书精灵"
                }) {
                    Label("复制", systemImage: "doc.on.doc") // 带图标的复制按钮
                        .labelStyle(.iconOnly)              // 只显示图标
                }
            }

            Spacer()
        }
    }
}
