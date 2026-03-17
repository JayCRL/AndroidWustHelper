//
//  SchoolCalender.swift
//  study_test
//
//  Created by wust_lh on 2025/7/29.
//

import SwiftUI
struct SchoolCalender: View {
    var body: some View {
        ScrollView{
            VStack(){
                Image("Calender1").resizable().frame(width: 400,height: 700)
                Image("Calender2").resizable().frame(width: 400,height: 700)
            }
        }
    }
}
#Preview {
    SchoolCalender()
}
