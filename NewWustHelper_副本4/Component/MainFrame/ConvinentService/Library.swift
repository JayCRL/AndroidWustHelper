import SwiftUI
@preconcurrency import WebKit
import SwiftUI
import WebKit
import Foundation


struct VolunteerWebView: UIViewRepresentable {
    let urlString: String
    @AppStorage("username") var savedUsername: String = ""
    @AppStorage("password") var savedPassword: String = ""
    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.preferences.javaScriptEnabled = true
        configuration.setValue(true, forKey: "allowUniversalAccessFromFileURLs")
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148"
        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        guard let url = URL(string: urlString) else { return }
        uiView.load(URLRequest(url: url))
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, WKNavigationDelegate {
        var parent: VolunteerWebView
        
        init(_ parent: VolunteerWebView) {
            self.parent = parent
        }
        
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            guard !parent.savedUsername.isEmpty, !parent.savedPassword.isEmpty else {
                print("❌ 未保存账号密码")
                return
            }
            // 修复：增加null安全检查，避免contenteditable元素不存在时出错
            let jsCode = """
            let checkCount = 0;
            const maxCheck = 15; // 最多检测15次（7.5秒）
            const interval = setInterval(() => {
                checkCount++;
                console.log(`第${checkCount}次检测`);
                
                // 1. 学号输入框（精确匹配）
                const usernameInput = document.querySelector(
                    'input[type="text"][placeholder="请输入学号"].van-field__control'
                );
                
                // 2. 密码输入框
                const passwordInput = document.querySelector(
                    'input[type="password"][placeholder="请输入密码"].van-field__control'
                );
                
                // 3. 确认按钮
                const confirmBtn = document.querySelector('.van-dialog__confirm');
                
                // 调试输出元素状态
                console.log("学号框:", usernameInput ? "找到" : "未找到");
                console.log("密码框:", passwordInput ? "找到" : "未找到");
                console.log("确认按钮:", confirmBtn ? "找到" : "未找到");
                
                if (usernameInput && passwordInput) {
                    clearInterval(interval);
                    console.log("✅ 找到所有元素，开始填充");
                    
                    // 填充学号（带安全检查）
                    usernameInput.value = '\(parent.savedUsername)';
                    triggerEvents(usernameInput);
                    
                    // 填充密码
                    passwordInput.value = '\(parent.savedPassword)';
                    triggerEvents(passwordInput);
                    
                    // 延迟点击确认按钮
                    setTimeout(() => {
                        if (confirmBtn) {
                            confirmBtn.click();
                            console.log("✅ 已点击确认按钮");
                        } else {
                            console.log("❌ 未找到确认按钮，尝试提交表单");
                            submitForm(usernameInput);
                        }
                    }, 1000);
                }
                
                // 达到最大检测次数
                if (checkCount >= maxCheck) {
                    clearInterval(interval);
                    console.log("❌ 超时未找到元素");
                }
            }, 500);
            
            // 触发事件函数（增加null安全检查）
            function triggerEvents(element) {
                // 基础输入事件（确保元素存在）
                if (!element) {
                    console.log("❌ 元素不存在，跳过事件触发");
                    return;
                }
                
                // 触发标准事件
                const events = ['focus', 'input', 'change', 'blur'];
                events.forEach(event => {
                    element.dispatchEvent(new Event(event, { bubbles: true }));
                });
                
                // 修复：仅在contenteditable元素存在时才执行同步
                // 尝试多种方式查找关联的contenteditable元素
                let editableEl = element.nextElementSibling; // 下一个兄弟元素
                if (!editableEl || !editableEl.isContentEditable) {
                    // 备选：父元素内查找
                    editableEl = element.parentNode.querySelector('[contenteditable="plaintext-only"]');
                }
                
                // 只有找到可编辑元素时才同步
                if (editableEl && editableEl.isContentEditable) {
                    editableEl.textContent = element.value;
                    editableEl.dispatchEvent(new Event('input', { bubbles: true }));
                    editableEl.dispatchEvent(new Event('change', { bubbles: true }));
                    console.log("✅ 已同步contenteditable元素内容");
                } else {
                    console.log("ℹ️ 未找到关联的contenteditable元素，跳过同步");
                }
                
                // 模拟键盘事件
                element.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
                element.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter' }));
            }
            
            // 提交表单（备选方案）
            function submitForm(inputElement) {
                if (!inputElement) return;
                let form = inputElement.closest('form');
                if (form) {
                    form.submit();
                    console.log("✅ 已提交表单");
                } else {
                    console.log("❌ 未找到表单元素");
                }
            }
            """
            webView.evaluateJavaScript(jsCode) { _, error in
                if let error = error {
                    print("❌ JS执行错误：\(error)")
                }
            }
        }
        
        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            decisionHandler(.allow)
        }
    }
}
// 保存账号密码的工具方法（建议在登录成功后调用）
func saveVolunteerAccount(username: String, password: String) {
    UserDefaults.standard.set(username, forKey: "volunteer_username")
    UserDefaults.standard.set(password, forKey: "volunteer_password")
}
    
struct CustomWebView: UIViewRepresentable {
    let urlString: String
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.navigationDelegate = context.coordinator
        // 启用 JavaScript
        webView.configuration.preferences.javaScriptEnabled = true
        // 设置手机端 User-Agent
        webView.customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_6_1 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/537.36"
        // 获取 Cookies
        let cookieStore = webView.configuration.websiteDataStore.httpCookieStore
        cookieStore.getAllCookies { cookies in
            for cookie in cookies {
                print("Cookie: \(cookie)")
            }
        }

        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        guard let url = URL(string: urlString) else { return }
        let request = URLRequest(url: url)
        uiView.load(request)
    }
    
    func makeCoordinator() -> Coordinator {
        return Coordinator(self)
    }
    
    class Coordinator: NSObject, WKNavigationDelegate {
        var parent: CustomWebView
        
        init(_ parent: CustomWebView) {
            self.parent = parent
        }
        
        // 捕捉重定向并处理
        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            if let url = navigationAction.request.url {
                print("Navigating to: \(url.absoluteString)")
            }
            decisionHandler(.allow)
        }
        
        // 捕捉页面加载错误
        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            print("Error loading page: \(error.localizedDescription)")
        }
    }
}

struct Library: View {
    private let urlString = "https://auth.wust.edu.cn/lyuapServer/login?service=http://ic.lib.wust.edu.cn/loginmall.aspx"
    
    var body: some View {
        CustomWebView(urlString: urlString)
            .edgesIgnoringSafeArea(.all) // 让网页充满整个屏幕，忽略安全区域
    }
}

#Preview {
    Library()
}
