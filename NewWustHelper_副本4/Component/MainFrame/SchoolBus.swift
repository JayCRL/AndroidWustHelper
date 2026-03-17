//
//  SchoolBus.swift
//  study_test
//
//  Created by wust_lh on 2025/7/29.
//

import SwiftUI

struct SchoolBus: View {
    var body: some View {
        ScrollView{
            Image("SchoolBusPicture").resizable().frame(width: 400,height: 1000).padding(.trailing,10)
        }
    }
}

#Preview {
    SchoolBus()
}
