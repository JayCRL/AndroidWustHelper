import SwiftUI

// 闪光效果视图
// 增强版闪光效果 - 适配浅色模式
struct FlashEffectEnhanced: View {
    @State private var flash = false
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        RoundedRectangle(cornerRadius: 20)
            .fill(
                LinearGradient(
                    gradient: Gradient(colors: [
                        .white.opacity(0),
                        .white.opacity(colorScheme == .dark ? 0.4 : 0.7),  // 浅色模式下更亮
                        .white.opacity(0)
                    ]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .scaleEffect(x: 1, y: 1.5)
            .rotationEffect(.degrees(-30))
            .offset(x: flash ? 200 : -200, y: 0)
            .animation(
                Animation.linear(duration: 1.5)
                    .delay(1)
                    .repeatForever(autoreverses: false),
                value: flash
            )
            .onAppear {
                flash = true
            }
            .blendMode(colorScheme == .dark ? .overlay : .plusLighter)  // 浅色模式使用更亮的混合模式
            .mask(
                RoundedRectangle(cornerRadius: 20)
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [.black, .clear, .black]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
            )
            .opacity(colorScheme == .dark ? 0.6 : 0.8)  // 浅色模式下更明显
    }
}

// 颜色方案扩展，用于检测当前模式
extension ColorScheme {
    static var current: ColorScheme {
        #if os(iOS)
        return UIScreen.main.traitCollection.userInterfaceStyle == .dark ? .dark : .light
        #else
        return .light // 默认值
        #endif
    }
}

struct mainframecamp: View {
    @State var mycolor: Color = Color("mytestcolor")
    let userData = UserData()
    let icons: [String] = ["banner7", "bannerhello"]
    // 新增：获取当前颜色模式（统一用环境变量，更符合SwiftUI规范）
    @Environment(\.colorScheme) var colorScheme
    
    // 屏幕适配相关计算属性
    private var screenWidth: CGFloat {
        UIScreen.main.bounds.width
    }
    
    private var horizontalPadding: CGFloat {
        screenWidth * 0.04 // 4% 的屏幕宽度作为左右边距
    }
    
    private var cardSpacing: CGFloat {
        screenWidth * 0.02 // 2% 的屏幕宽度作为卡片间距
    }
    
    private var carouselWidth: CGFloat {
        screenWidth * 0.9 // 90% 的屏幕宽度作为轮播图宽度，与成绩查询卡片保持一致
    }
    
    private var carouselHeight: CGFloat {
        carouselWidth * 0.56 // 保持16:9比例
    }
    
    private var largeCardWidth: CGFloat {
        screenWidth * 0.9 // 大卡片占90%屏幕宽度
    }
    
    private var smallCardWidth: CGFloat {
        (screenWidth - horizontalPadding * 2 - cardSpacing) / 2 // 小卡片平分剩余空间
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                ZStack {
                    Color("bgcolor")
                    VStack(alignment: .leading) {
                        // 轮播图（保持原有）
                        // 轮播图（新增玻璃效果，替换原有轮播图代码）
                        // 轮播图整体容器：外层固定玻璃+内层滑动图片
                        ZStack {
                            // 1. 固定玻璃容器（不动，带闪光动效）
                            RoundedRectangle(cornerRadius: 18)
                                .fill(.ultraThinMaterial) // 核心玻璃质感
                                .background(
                                    // 深浅色适配：深色用系统背景色，浅色用白色（增强不透明度避免过透）
                                    RoundedRectangle(cornerRadius: 18)
                                        .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.85) : Color.white.opacity(0.9))
                                )
                                .blur(radius: 6) // 玻璃模糊效果
                            // 2. 玻璃光泽效果（提升精致度）
                                .overlay(
                                    RoundedRectangle(cornerRadius: 18)
                                        .fill(
                                            LinearGradient(
                                                gradient: Gradient(colors: [
                                                    .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                    .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                    .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                ]),
                                                startPoint: .topLeading,
                                                endPoint: .bottomTrailing
                                            )
                                        )
                                        .rotationEffect(.degrees(30))
                                        .blur(radius: 2)
                                        .mask(
                                            RoundedRectangle(cornerRadius: 18)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [.black, .clear]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                        )
                                )
                            // 3. 玻璃渐变边框（与卡片风格统一）
                                .overlay(
                                    RoundedRectangle(cornerRadius: 18)
                                        .stroke(
                                            LinearGradient(
                                                gradient: Gradient(colors: [
                                                    .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                    .clear,
                                                    .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                ]),
                                                startPoint: .init(x: 0, y: 0),
                                                endPoint: .init(x: 1, y: 1)
                                            ),
                                            lineWidth: 1.2
                                        )
                                )
                            // 4. 玻璃阴影（固定不动，避免跟随图片滑动）
                                .shadow(
                                    color: colorScheme == .dark ? Color.black.opacity(0.25) : Color.gray.opacity(0.18),
                                    radius: 10, x: 5, y: 6 // 右移阴影避免左侧超出
                                )
                                .shadow(
                                    color: colorScheme == .dark ? .clear : Color.white.opacity(0.35),
                                    radius: 3, x: 2, y: 2
                                )
                            // 5. 玻璃闪光动效（固定在玻璃上，不跟随图片动）
                                .overlay(FlashEffectEnhanced()
                                    .mask(RoundedRectangle(cornerRadius: 18)) // 闪光裁剪为玻璃形状
                                    .opacity(colorScheme == .dark ? 0.5 : 0.7) // 调整闪光透明度
                                )
                                .frame(width: carouselWidth, height: carouselHeight) // 玻璃容器响应式尺寸
                            
                            // 6. 内部滑动轮播图（仅图片动，玻璃不动）
                            TabView {
                                ForEach(icons, id: \.self) { icon in
                                    Image(icon)
                                        .resizable()
                                        .scaledToFill()
                                        .frame(width: carouselWidth * 0.94, height: carouselHeight * 0.89) // 图片尺寸小于玻璃容器，形成内边距
                                        .clipped()
                                        .cornerRadius(14) // 图片圆角小于玻璃，增强层次感
                                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 4) // 图片内阴影，突出主体
                                }
                            }
                            .frame(width: carouselWidth * 0.94, height: carouselHeight * 0.89) // 轮播图尺寸与图片一致
                            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .always))
                            .indexViewStyle(PageIndexViewStyle(backgroundDisplayMode: .interactive))
                            // 轮播图指示器样式调整（适配玻璃效果）
                            .overlay(
                                VStack(spacing: 0) {
                                    Spacer()
                                    // 指示器背景半透明化，与玻璃融合
                                    Color.black.opacity(0.1)
                                        .frame(height: 30)
                                        .mask(
                                            LinearGradient(
                                                gradient: Gradient(colors: [.clear, .black, .clear]),
                                                startPoint: .leading,
                                                endPoint: .trailing
                                            )
                                        )
                                }
                            )
                        }
                        // 外层整体padding，避免玻璃容器左侧超出
                        .padding(.horizontal, horizontalPadding)
                        // 热门功能标题（保持原有）
                        Text("热门功能")
                            .multilineTextAlignment(.leading)
                            .padding(.leading, horizontalPadding * 0.5)
                            .font(.system(size: min(20, screenWidth * 0.05)))
                            .fontWeight(.light)
                            .minimumScaleFactor(0.8)
                            .lineLimit(1)
                        
                        // 1. 成绩查询卡片（参考基准，保持不变）
                        NavigationLink(destination: CheckGrades()) {
                            ZStack {
                                // 玻璃效果背景
                                RoundedRectangle(cornerRadius: 20)
                                    .fill(.ultraThinMaterial)
                                    .background(
                                        RoundedRectangle(cornerRadius: 20)
                                            .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8): Color.white.opacity(0.7))
                                    )
                                    .blur(radius: 5)
                                // 光泽效果
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 20)
                                            .fill(
                                                LinearGradient(
                                                    gradient: Gradient(colors: [
                                                        .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                        .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                        .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                    ]),
                                                    startPoint: .topLeading,
                                                    endPoint: .bottomTrailing
                                                )
                                            )
                                            .rotationEffect(.degrees(30))
                                            .blur(radius: 2)
                                            .mask(
                                                RoundedRectangle(cornerRadius: 20)
                                                    .fill(
                                                        LinearGradient(
                                                            gradient: Gradient(colors: [.black, .clear]),
                                                            startPoint: .topLeading,
                                                            endPoint: .bottomTrailing
                                                        )
                                                    )
                                            )
                                    )
                                // 边框光泽
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 20)
                                            .stroke(
                                                LinearGradient(
                                                    gradient: Gradient(colors: [
                                                        .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                        .clear,
                                                        .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                    ]),
                                                    startPoint: .init(x: 0, y: 0),
                                                    endPoint: .init(x: 1, y: 1)
                                                ),
                                                lineWidth: 1
                                            )
                                    )
                                // 阴影适配
                                    .shadow(
                                        color: colorScheme == .dark ? Color.blue.opacity(0.3) : Color.blue.opacity(0.15),
                                        radius: 10, x: 0, y: 5
                                    )
                                    .shadow(
                                        color: colorScheme == .dark ? .clear : Color.white.opacity(0.4),
                                        radius: 3, x: 0, y: 2
                                    )
                                
                                // 内容（文字颜色适配）
                                HStack {
                                    VStack(alignment: .leading) {
                                        Text("成绩查询")
                                            .foregroundColor(colorScheme == .dark ? .white : Color.green)
                                            .fontWeight(.bold)
                                            .font(.title)
                                            .padding(.leading, 5)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.5),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        Text("成绩查询/绩点计算/均分查看")
                                            .foregroundColor(colorScheme == .dark ? .white : Color.green.opacity(0.8))
                                            .font(.system(size: 11))
                                            .padding(.leading, 5)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 1, x: 0, y: 1
                                            )
                                    }
                                    
                                    Image("grade")
                                        .padding(.leading, 15)
                                        .shadow(
                                            color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                            radius: 2, x: 0, y: 1
                                        )
                                }
                            }
                            .frame(width: largeCardWidth, height: largeCardWidth * 0.45)
                            .overlay(FlashEffectEnhanced()) // 动态闪光
                        }.padding(.leading, horizontalPadding)
                        
                        // 2. 考试提醒 + 培养方案/竞赛组队 横向布局
                        HStack {
                            // 2.1 考试提醒卡片（新增完整效果）
                            NavigationLink(destination: ClockNotice()) {
                                ZStack {
                                    // 玻璃效果背景
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(colorScheme == .dark ?  Color(.systemBackground).opacity(0.8) : Color("bgcolor2").opacity(0.8))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配
                                        
                                    
                                    // 内容（文字颜色适配）
                                    VStack {
                                        Image("alarmClock")
                                            .resizable()
                                            .frame(width: 80, height: 80)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        Text("考试提醒")
                                            .foregroundColor(colorScheme == .dark ? .white : Color("alarmcolor"))
                                            .fontWeight(.medium)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 1, x: 0, y: 1
                                            )
                                        
                                        Text("倒计时/消息通知")
                                            .foregroundColor(colorScheme == .dark ? .white : Color("alarmcolor").opacity(0.8))
                                            .font(.system(size: 13))
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 1, x: 0, y: 1
                                            )
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 1.1)
                                .overlay(FlashEffectEnhanced()) // 动态闪光
                            }
                            
                            // 右侧两个卡片（培养方案 + 竞赛组队）
                            VStack(spacing: cardSpacing) {
                                // 2.2 培养方案卡片（新增完整效果）
                                NavigationLink(destination: TrainingPlanView()) {
                                    ZStack {
                                        // 玻璃效果背景
                                        RoundedRectangle(cornerRadius: 20)
                                            .fill(.ultraThinMaterial)
                                            .background(
                                                RoundedRectangle(cornerRadius: 20)
                                                    .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color("bgcolor4").opacity(0.8))
                                            )
                                            .blur(radius: 5)
                                        // 光泽效果
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 20)
                                                    .fill(
                                                        LinearGradient(
                                                            gradient: Gradient(colors: [
                                                                .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                                .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                                .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                            ]),
                                                            startPoint: .topLeading,
                                                            endPoint: .bottomTrailing
                                                        )
                                                    )
                                                    .rotationEffect(.degrees(30))
                                                    .blur(radius: 2)
                                                    .mask(
                                                        RoundedRectangle(cornerRadius: 20)
                                                            .fill(
                                                                LinearGradient(
                                                                    gradient: Gradient(colors: [.black, .clear]),
                                                                    startPoint: .topLeading,
                                                                    endPoint: .bottomTrailing
                                                                )
                                                            )
                                                    )
                                            )
                                        // 边框光泽
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 20)
                                                    .stroke(
                                                        LinearGradient(
                                                            gradient: Gradient(colors: [
                                                                .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                                .clear,
                                                                .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                            ]),
                                                            startPoint: .init(x: 0, y: 0),
                                                            endPoint: .init(x: 1, y: 1)
                                                        ),
                                                        lineWidth: 1
                                                    )
                                            )
                                        // 阴影适配
                                            .shadow(
                                                color: colorScheme == .dark ?Color(.systemBackground).opacity(0.8): Color("bgcolor4").opacity(0.15),
                                                radius: 8, x: 0, y: 4
                                            )
                                            .shadow(
                                                color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        // 内容（文字颜色适配）
                                        HStack {
                                            VStack(alignment: .leading) {
                                                Text("培养方案")
                                                    .foregroundColor(colorScheme == .dark ? .white : Color.orange)
                                                    .fontWeight(.bold)
                                                    .font(.headline)
                                                    .shadow(
                                                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                        radius: 1, x: 0, y: 1
                                                    )
                                                
                                                Text("掌握修读情况")
                                                    .foregroundColor(colorScheme == .dark ? .white : Color.orange.opacity(0.8))
                                                    .font(.system(size: 13))
                                                    .shadow(
                                                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                        radius: 1, x: 0, y: 1
                                                    )
                                            }
                                            
                                            Image("credit")
                                                .resizable()
                                                .frame(width: 50, height: 50)
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 2, x: 0, y: 1
                                                )
                                        }
                                    }
                                    .frame(width: smallCardWidth, height: smallCardWidth * 0.55)
                                    .overlay(FlashEffectEnhanced()) // 动态闪光
                                }
                                
                                // 2.3 竞赛组队卡片（新增完整效果）
                                NavigationLink(destination: GroupCompetition()) {
                                    ZStack {
                                        // 玻璃效果背景
                                        RoundedRectangle(cornerRadius: 20)
                                            .fill(.ultraThinMaterial)
                                            .background(
                                                RoundedRectangle(cornerRadius: 20)
                                                    .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color("bgcolor3").opacity(0.8))
                                            )
                                            .blur(radius: 5)
                                        // 光泽效果
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 20)
                                                    .fill(
                                                        LinearGradient(
                                                            gradient: Gradient(colors: [
                                                                .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                                .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                                .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                            ]),
                                                            startPoint: .topLeading,
                                                            endPoint: .bottomTrailing
                                                        )
                                                    )
                                                    .rotationEffect(.degrees(30))
                                                    .blur(radius: 2)
                                                    .mask(
                                                        RoundedRectangle(cornerRadius: 20)
                                                            .fill(
                                                                LinearGradient(
                                                                    gradient: Gradient(colors: [.black, .clear]),
                                                                    startPoint: .topLeading,
                                                                    endPoint: .bottomTrailing
                                                                )
                                                            )
                                                    )
                                            )
                                        // 边框光泽
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 20)
                                                    .stroke(
                                                        LinearGradient(
                                                            gradient: Gradient(colors: [
                                                                .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                                .clear,
                                                                .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                            ]),
                                                            startPoint: .init(x: 0, y: 0),
                                                            endPoint: .init(x: 1, y: 1)
                                                        ),
                                                        lineWidth: 1
                                                    )
                                            )
                                        // 阴影适配
                                            .shadow(
                                                color: colorScheme == .dark ? Color(.systemBackground).opacity(0.8): Color("bgcolor3").opacity(0.15),
                                                radius: 8, x: 0, y: 4
                                            )
                                            .shadow(
                                                color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        // 内容（文字颜色适配）
                                        HStack {
                                            VStack(alignment: .leading) {
                                                Text("竞赛组队")
                                                    .foregroundColor(colorScheme == .dark ? .white : Color.blue)
                                                    .fontWeight(.medium)
                                                    .shadow(
                                                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                        radius: 1, x: 0, y: 1
                                                    )
                                                
                                                Text("志同道合")
                                                    .foregroundColor(colorScheme == .dark ? .white : Color.blue.opacity(0.8))
                                                    .font(.caption)
                                                    .shadow(
                                                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                        radius: 1, x: 0, y: 1
                                                    )
                                            }
                                            .padding(.leading, 3)
                                            
                                            Image(systemName: "person.2.fill")
                                                .resizable()
                                                .foregroundColor(colorScheme == .dark ?  Color.blue : Color.blue)
                                                .frame(width: 50, height: 40)
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 2, x: 0, y: 1
                                                )
                                        }
                                    }
                                    .frame(width: smallCardWidth, height: smallCardWidth * 0.55)
                                    .overlay(FlashEffectEnhanced()) // 动态闪光
                                }
                            }
                        }
                        .padding(.horizontal, horizontalPadding)
                        .padding(.top, cardSpacing)
                        
                        // 3. 二手平台 + 校园搭子 横向布局
                        HStack {
                            // 3.1 二手平台卡片（新增完整效果）
                            NavigationLink(destination: SearchProductView()) {
                                ZStack {
                                    // 玻璃效果背景
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8): Color("librarycolor").opacity(0.8))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配
                                        .shadow(
                                            color: colorScheme == .dark ? Color("librarycolor").opacity(0.3) : Color("librarycolor").opacity(0.15),
                                            radius: 8, x: 0, y: 4
                                        )
                                        .shadow(
                                            color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                                            radius: 2, x: 0, y: 1
                                        )
                                    
                                    // 内容（文字颜色适配）
                                    HStack {
                                        Image("library")
                                            .resizable()
                                            .frame(width: 50, height: 50)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        VStack(alignment: .leading) {
                                            Text("二手平台")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.red)
                                                .fontWeight(.bold)
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                            
                                            Text("物品置换")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.red.opacity(0.8))
                                                .font(.system(size: 13))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                        }
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 0.55)
                                .overlay(FlashEffectEnhanced()) // 动态闪光
                            }
                            
                            // 3.2 校园搭子卡片（新增完整效果）
                            NavigationLink(destination: Group1MainView().environmentObject(userData)) {
                                ZStack {
                                    // 玻璃效果背景
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color.bgcolor4.opacity(0.8))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配
                                        .shadow(
                                            color: colorScheme == .dark ? Color.bgcolor4.opacity(0.3) : Color.bgcolor4.opacity(0.15),
                                            radius: 8, x: 0, y: 4
                                        )
                                        .shadow(
                                            color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                                            radius: 2, x: 0, y: 1
                                        )
                                    
                                    // 内容（文字颜色适配）
                                    HStack {
                                        VStack(alignment: .leading) {
                                            Text("校园搭子")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.brown)
                                                .fontWeight(.bold)
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                            
                                            Text("多彩生活")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.brown.opacity(0.8))
                                                .font(.system(size: 13))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                        }
                                        
                                        Image(systemName: "figure.badminton")
                                            .resizable()
                                            .foregroundColor(colorScheme == .dark ?  Color.brown: Color.brown)
                                            .frame(width: 50, height: 50)
                                            .padding(.top, 15)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 0.55)
                                .overlay(FlashEffectEnhanced()) // 动态闪光
                                .badge("1")
                            }
                        }
                        .padding(.horizontal, horizontalPadding)
                        .padding(.top, cardSpacing)
                        
                        // 4. 学习互助 + AI问答 横向布局 (New Row)
                        HStack {
                            // 4.1 学习互助卡片
                            NavigationLink(destination: StudyCommunityView()) {
                                ZStack {
                                    // 玻璃效果背景
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color.purple.opacity(0.1))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配
                                        .shadow(
                                            color: colorScheme == .dark ? Color.purple.opacity(0.3) : Color.purple.opacity(0.15),
                                            radius: 8, x: 0, y: 4
                                        )
                                        .shadow(
                                            color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                                            radius: 2, x: 0, y: 1
                                        )
                                    
                                    // 内容
                                    HStack {
                                        Image(systemName: "book.fill")
                                            .resizable()
                                            .aspectRatio(contentMode: .fit)
                                            .foregroundColor(colorScheme == .dark ? .white : Color.purple)
                                            .frame(width: 40, height: 40)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        VStack(alignment: .leading) {
                                            Text("学习互助")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.purple)
                                                .fontWeight(.bold)
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                            
                                            Text("答疑/经验")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.purple.opacity(0.8))
                                                .font(.system(size: 13))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                        }
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 0.55)
                                .overlay(FlashEffectEnhanced())
                            }
                            
                            // 4.2 AI问答卡片
                            NavigationLink(destination: AIAssistantView()) {
                                ZStack {
                                    // 玻璃效果背景
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color.cyan.opacity(0.1))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配
                                        .shadow(
                                            color: colorScheme == .dark ? Color.cyan.opacity(0.3) : Color.cyan.opacity(0.15),
                                            radius: 8, x: 0, y: 4
                                        )
                                        .shadow(
                                            color: colorScheme == .dark ? .clear : Color.white.opacity(0.3),
                                            radius: 2, x: 0, y: 1
                                        )
                                    
                                    // 内容
                                    HStack {
                                        VStack(alignment: .leading) {
                                            Text("AI 问答")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.cyan)
                                                .fontWeight(.bold)
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                            
                                            Text("智能助手")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.cyan.opacity(0.8))
                                                .font(.system(size: 13))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                        }
                                        
                                        Image(systemName: "sparkles")
                                            .resizable()
                                            .aspectRatio(contentMode: .fit)
                                            .foregroundColor(colorScheme == .dark ? .white : Color.cyan)
                                            .frame(width: 40, height: 40)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 0.55)
                                .overlay(FlashEffectEnhanced())
                            }
                        }
                        .padding(.horizontal, horizontalPadding)
                        .padding(.top, cardSpacing)
                        
                        // 小工具标题（保持原有）
                        HStack {
                            Text("小工具")
                                .font(.system(size: min(18, screenWidth * 0.045)))
                                .multilineTextAlignment(.leading)
                                .padding(.leading, horizontalPadding * 0.5)
                                .padding(.top, 15)
                            NavigationLink(destination: CampusCatView()) {
                                Image("cat")
                                    .resizable()
                                    .frame(width: min(70, screenWidth * 0.18), height: min(70, screenWidth * 0.18))
                                    .padding(.leading, screenWidth * 0.5)
                            }
                        }
                        
                        // 4. 小工具第一行（校车时刻 + 学校日历）
                        HStack {
                            // 4.1 校车时刻卡片（新增完整效果）
                            NavigationLink(destination: SchoolBus()) {
                                ZStack {
                                    // 玻璃效果背景（浅色白色/深色深色背景）
                                    RoundedRectangle(cornerRadius: 10)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color.white.opacity(0.9))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 10)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配（浅色保留原阴影，深色弱化）
                                        .shadow(
                                            color: colorScheme == .dark ? .black.opacity(0.1) : Color.gray.opacity(0.1),
                                            radius: 1, x: 0, y: 10
                                        )
                                    
                                    // 内容（文字颜色适配）
                                    HStack {
                                        Image("schoolBus")
                                            .resizable()
                                            .frame(width: 50, height: 50)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        VStack {
                                            Text("校车时刻")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.black)
                                                .font(.system(size: 16))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                            
                                            Text("发车时刻查询")
                                                .foregroundColor(colorScheme == .dark ? .white.opacity(0.9) : Color.gray)
                                                .font(.system(size: 11))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                        }
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                .overlay(
                                            FlashEffectEnhanced()
                                                .mask( // 添加蒙版限制闪光范围
                                                    RoundedRectangle(cornerRadius: 10)
                                                        .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                                )
                                                .offset(x: 4, y: 0) // 重置偏移
                                        )
                            }
                            
                            // 4.2 学校日历卡片（新增完整效果）
                            NavigationLink(destination: SchoolCalender()) {
                                ZStack {
                                    // 玻璃效果背景
                                    RoundedRectangle(cornerRadius: 10)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color.white.opacity(0.9))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 10)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配
                                        .shadow(
                                            color: colorScheme == .dark ? .black.opacity(0.1) : Color.gray.opacity(0.1),
                                            radius: 2, x: 0, y: 10
                                        )
                                    
                                    // 内容（文字颜色适配）
                                    HStack {
                                        Image("schoolCalendar")
                                            .resizable()
                                            .frame(width: 50, height: 50)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        VStack {
                                            Text("学校日历")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.black)
                                                .font(.system(size: 16))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                            
                                            Text("了解学校安排")
                                                .foregroundColor(colorScheme == .dark ? .white.opacity(0.9) : Color.gray)
                                                .font(.system(size: 11))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                        }
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                .overlay(
                                    FlashEffectEnhanced()
                                        .mask( // 添加蒙版限制闪光范围
                                            RoundedRectangle(cornerRadius: 10)
                                                .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                             )
                                        .offset(x: 12, y: 0) // 重置偏移
                                )
                            }
                        }
                        .padding(.horizontal, horizontalPadding)
                        .padding(.top, cardSpacing)
                        
                        // 5. 小工具第二行（校园黄页 + 校园服务）
                        HStack {
                            // 5.1 校园黄页卡片（新增完整效果）
                            NavigationLink(destination: PhoneNumber()) {
                                ZStack {
                                    // 玻璃效果背景
                                    RoundedRectangle(cornerRadius: 10)
                                        .fill(.ultraThinMaterial)
                                        .background(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color.white.opacity(0.9))
                                        )
                                        .blur(radius: 5)
                                    // 光泽效果
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                            .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                            .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                        ]),
                                                        startPoint: .topLeading,
                                                        endPoint: .bottomTrailing
                                                    )
                                                )
                                                .rotationEffect(.degrees(30))
                                                .blur(radius: 2)
                                                .mask(
                                                    RoundedRectangle(cornerRadius: 10)
                                                        .fill(
                                                            LinearGradient(
                                                                gradient: Gradient(colors: [.black, .clear]),
                                                                startPoint: .topLeading,
                                                                endPoint: .bottomTrailing
                                                            )
                                                        )
                                                )
                                        )
                                    // 边框光泽
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(
                                                    LinearGradient(
                                                        gradient: Gradient(colors: [
                                                            .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                            .clear,
                                                            .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                        ]),
                                                        startPoint: .init(x: 0, y: 0),
                                                        endPoint: .init(x: 1, y: 1)
                                                    ),
                                                    lineWidth: 1
                                                )
                                        )
                                    // 阴影适配
                                        .shadow(
                                            color: colorScheme == .dark ? .black.opacity(0.1) : Color.gray.opacity(0.1),
                                            radius: 2, x: 0, y: 10
                                        )
                                    
                                    // 内容（文字颜色适配）
                                    HStack {
                                        Image("yellowPages")
                                            .resizable()
                                            .frame(width: 50, height: 50)
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                radius: 2, x: 0, y: 1
                                            )
                                        
                                        VStack {
                                            Text("校园黄页")
                                                .foregroundColor(colorScheme == .dark ? .white : Color.black)
                                                .font(.system(size: 16))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                            
                                            Text("号码通讯录")
                                                .foregroundColor(colorScheme == .dark ? .white.opacity(0.9) : Color.gray)
                                                .font(.system(size: 11))
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 1, x: 0, y: 1
                                                )
                                        }
                                    }
                                }
                                .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                .overlay(
                                    FlashEffectEnhanced()
                                        .mask( // 添加蒙版限制闪光范围
                                            RoundedRectangle(cornerRadius: 10)
                                                .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                             )
                                        .offset(x: 2, y: 0) // 重置偏移
                                )
                            }
                            
                            // 5.2 校园服务卡片（新增完整效果）
                            NavigationLink(destination: StudentCommunityBook()) {
                                    ZStack {
                                        // 玻璃效果背景
                                        RoundedRectangle(cornerRadius: 10)
                                            .fill(.ultraThinMaterial)
                                            .background(
                                                RoundedRectangle(cornerRadius: 10)
                                                    .fill(colorScheme == .dark ? Color(.systemBackground).opacity(0.8) : Color.white.opacity(0.9))
                                            )
                                            .blur(radius: 5)
                                        // 光泽效果
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 10)
                                                    .fill(
                                                        LinearGradient(
                                                            gradient: Gradient(colors: [
                                                                .white.opacity(colorScheme == .dark ? 0.3 : 0.6),
                                                                .white.opacity(colorScheme == .dark ? 0.05 : 0.2),
                                                                .white.opacity(colorScheme == .dark ? 0.3 : 0.6)
                                                            ]),
                                                            startPoint: .topLeading,
                                                            endPoint: .bottomTrailing
                                                        )
                                                    )
                                                    .rotationEffect(.degrees(30))
                                                    .blur(radius: 2)
                                                    .mask(
                                                        RoundedRectangle(cornerRadius: 10)
                                                            .fill(
                                                                LinearGradient(
                                                                    gradient: Gradient(colors: [.black, .clear]),
                                                                    startPoint: .topLeading,
                                                                    endPoint: .bottomTrailing
                                                                )
                                                            )
                                                    )
                                            )
                                        // 边框光泽
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 10)
                                                    .stroke(
                                                        LinearGradient(
                                                            gradient: Gradient(colors: [
                                                                .white.opacity(colorScheme == .dark ? 0.5 : 0.8),
                                                                .clear,
                                                                .white.opacity(colorScheme == .dark ? 0.2 : 0.5)
                                                            ]),
                                                            startPoint: .init(x: 0, y: 0),
                                                            endPoint: .init(x: 1, y: 1)
                                                        ),
                                                        lineWidth: 1
                                                    )
                                            )
                                        // 阴影适配
                                            .shadow(
                                                color: colorScheme == .dark ? .black.opacity(0.1) : Color.gray.opacity(0.1),
                                                radius: 2, x: 0, y: 10
                                            )
                                        
                                        // 内容（文字颜色适配）
                                        HStack {
                                            Image("eduNews")
                                                .resizable()
                                                .frame(width: 50, height: 50)
                                                .shadow(
                                                    color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                    radius: 2, x: 0, y: 1
                                                )
                                            
                                            VStack {
                                                Text("校园服务")
                                                    .foregroundColor(colorScheme == .dark ? .white : Color.black)
                                                    .font(.system(size: 16))
                                                    .shadow(
                                                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                        radius: 1, x: 0, y: 1
                                                    )
                                                
                                                Text("图书馆/约教室")
                                                    .foregroundColor(colorScheme == .dark ? .white.opacity(0.9) : Color.gray)
                                                    .font(.system(size: 11))
                                                    .shadow(
                                                        color: colorScheme == .dark ? .black.opacity(0.2) : .white.opacity(0.4),
                                                        radius: 1, x: 0, y: 1
                                                    )
                                            }
                                        }
                                    }
                                    .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                    .overlay(
                                        FlashEffectEnhanced()
                                            .mask( // 添加蒙版限制闪光范围
                                                RoundedRectangle(cornerRadius: 10)
                                                    .frame(width: smallCardWidth, height: smallCardWidth * 0.46)
                                                 )
                                            .offset(x: 12, y: 0) // 重置偏移
                                    )
                                }
                            }
                            .padding(.horizontal, horizontalPadding)
                            .padding(.top, cardSpacing)
                            
                            // 底部占位（保持原有）
                            Text("123123123123123")
                                .frame(height: 10)
                                .opacity(0)
                        }
                    }
                }
                .scrollIndicators(.hidden)
                // 导航栏（保持原有）
                .navigationBarItems(
                    leading:
                        HStack {
                            Text("武科大助手")
                                .font(.system(size: min(28, screenWidth * 0.07)))
                                .fontWeight(.bold)
                            Text("WUST HELPER")
                                .font(.system(size: min(12, screenWidth * 0.03)))
                                .padding(.top, 5)
                        }
                )
            }
        }
    }
#Preview {
    mainframecamp()
}
