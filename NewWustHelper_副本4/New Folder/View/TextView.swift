import SwiftUI
import UIKit
struct QQGroup: View {
    @State private var showAlert = false
    @State private var alertMessage = ""
    var body: some View {
        VStack(spacing: 30) {
            Spacer()
            Text("跳转失败请复制qq群号在qq中查看：913012243").font(.title).foregroundColor(Color.gray)
            Spacer()
        }.onAppear(){
            openQQChat()
        }
        .alert(isPresented: $showAlert) {
            Alert(
                title: Text("提示"),
                message: Text(alertMessage),
                dismissButton: .default(Text("确定"))
            )
        }
        .padding()
    }
    // 打开QQ应用
    func openQQ() {
        let qqURL = URL(string: "mqq://")!
        if UIApplication.shared.canOpenURL(qqURL) {
            UIApplication.shared.open(qqURL, options: [:]) { success in
                if !success {
                    alertMessage = "打开QQ失败"
                    showAlert = true
                }
            }
        } else {
            alertMessage = "未安装QQ"
            showAlert = true
        }
    }
    
    func openQQChat() {
        let groupNumber = "913012243" // 「武科大助手用户群」的实际群号
        let groupSecret = "ABCDE1234567890" // 群密钥（电脑版QQ群二维码中提取）
        
        // 传统群跳转URL格式
        let urlString = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=\(groupNumber)&card_type=group&source=qrcode&key=\(groupSecret)"
        
        guard let qqURL = URL(string: urlString) else {
            alertMessage = "群信息配置错误"
            showAlert = true
            return
        }
        
        // 后续跳转逻辑与上述一致...
        if UIApplication.shared.canOpenURL(qqURL) {
            UIApplication.shared.open(qqURL, options: [:]) { success in
                if !success {
                    alertMessage = "打开群聊失败，请检查群信息"
                    showAlert = true
                }
            }
        } else {
            alertMessage = "未安装QQ"
            showAlert = true
        }
    }
}
