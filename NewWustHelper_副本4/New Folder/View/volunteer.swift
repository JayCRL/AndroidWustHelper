//
//  volunteer.swift
//  study_test
//
//  Created by wust_lh on 2025/7/8.
//

import SwiftUI

struct volunteer: View {
    private let urlString = "https://volunteer.wustlinghang.cn"
    var body: some View {
        VolunteerWebView(urlString: urlString)
            .edgesIgnoringSafeArea(.top) // 让网页充满整个屏幕，忽略安全区域
    }
}

#Preview {
    volunteer()
}
