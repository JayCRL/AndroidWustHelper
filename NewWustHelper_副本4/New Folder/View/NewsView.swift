//
//  NewsView.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/9/4.
//
import SwiftUI
@preconcurrency import WebKit
struct NewsView: View {
    private let urlString = "https://news.wustlinghang.cn"
    var body: some View {
        CustomWebView(urlString: urlString)
            .edgesIgnoringSafeArea(.all) // 让网页充满整个屏幕，忽略安全区域
    }
}

#Preview {
    NewsView()
}
