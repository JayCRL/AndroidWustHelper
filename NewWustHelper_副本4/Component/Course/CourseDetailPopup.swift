//
//  CourseDetailPopup.swift
//  study_test
//
//  Created by wust_lh on 2025/7/24.
//
import SwiftUI
struct CourseDetailPopup: View {
    @Binding var isPresented: Bool
    let course: Course
    var body: some View {
        ZStack {
            // 半透明背景，点击可关闭
            Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
                .onTapGesture {
                    withAnimation {
                        isPresented = false
                    }
                }
                .contentShape(Rectangle()) // 确保整个区域都能响应点击
            // 居中显示的课程详情卡片
            VStack(spacing: 0) {
                // 标题和关闭按钮
                    Text(course.name)
                    .foregroundColor(Color("courseTitleColor"))
                    .font(.system(size: 20)).bold().padding(.all,5).padding(.top,10)
                // 课程信息
                    VStack(alignment: .leading, spacing: 5) {
                        InfoRow(icon: "person.fill", label: "教师", value: course.teacher)
                        InfoRow(icon: "building.2.fill", label: "教室", value: course.classroom)
                        InfoRow(icon: "magazine.fill", label: "星期", value: weekdayString(for: course.weekDay))
                        InfoRow(icon: "clock.fill", label: "节次", value: "\(course.startSection)-\(course.endSection)节")
                        InfoRow(icon: "calendar", label: "周次", value: "第\(course.startWeek)-\(course.endWeek)周")
                        InfoRow(icon: "graduationcap.fill", label: "班级", value: course.teachClass)
                    }
                    .padding()
                Spacer()
            }
            .frame(width: 250, height: 320)
            .background(Color(.systemBackground))
            .cornerRadius(20)
            .padding(.horizontal, 30)
            .shadow(radius: 10)
            .transition(.scale(scale: 0.9).combined(with: .opacity))
        }
        .animation(.spring(), value: isPresented)
    }
    
    // 将数字星期转换为中文
    private func weekdayString(for weekday: Int) -> String {
        let weekdays = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"]
        return weekdays[min(weekday - 1, weekdays.count - 1)]
    }
}
// 信息行组件
struct InfoRow: View {
    let icon: String
    let label: String
    let value: String
    
    var body: some View {
        HStack() {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .font(.system(size: 13))
                .frame(width: 25)
            Text(label)
                .foregroundColor(.gray)
                .font(.system(size: 13))
            Text(value)
                .fontWeight(.medium)
                .foregroundColor(Color("courseTitleColor"))
                .font(.system(size: 11))
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 8)
        .frame(width: 200, alignment: .leading) // 固定宽度 200
        .background(Color.gray.opacity(0.1))
        .cornerRadius(10)
    }

}
