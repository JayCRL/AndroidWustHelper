//
//  MainBar.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/11/3.
//

import SwiftUI
struct mainBar: View {
    @Binding var isQingShan: Bool
    @Binding var weeknumber: Int
    @Binding var courses: [Course]
    @Binding var activeNumber: Int
    @Binding var showCourseDetail: Bool
    @Binding var showEditView: Bool
    @Binding var selectedCourse: Course?
    @Binding var showMultipleCoursesDetail: Bool
    @Binding var selectedCourses: [Course]
    @Binding var selectedCellIndex: Int

    @Binding var showBackGround: Bool
    
    @Binding var scrollToTopTrigger: Bool
    // 校区时间配置
    @State var coursetimeHJH: [CourseTime] = [
        CourseTime(start:"08:20", end:"10:00"),
        CourseTime(start:"10:20", end:"12:00"),
        CourseTime(start:"14:00", end:"15:40"),
        CourseTime(start:"16:00", end:"17:40"),
        CourseTime(start:"18:40", end:"20:20"),
        CourseTime(start:"20:30", end:"22:10")
    ]
    @State var coursetimeQS: [CourseTime] = [
        CourseTime(start:"08:00", end:"09:40"),
        CourseTime(start:"10:10", end:"11:50"),
        CourseTime(start:"14:00", end:"15:40"),
        CourseTime(start:"16:00", end:"17:40"),
        CourseTime(start:"18:40", end:"20:20"),
        CourseTime(start:"20:30", end:"22:10")
    ]
    @State var coursetime: [CourseTime] = []

    let coloums : [GridItem] = Array(repeating: GridItem(.flexible()), count: 7)

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                HStack {
                    TimeSidebar(showBackGroud: $showBackGround, courseTimes: $coursetime)
                    CourseGrid(
                        courses: courses,
                        weeknumber: $weeknumber,
                        showBackGround: $showBackGround,
                        activeNumber: activeNumber,
                        onCellTap: { selectedIndex in
                            activeNumber = selectedIndex
                        },
                        onCourseTap: { course in
                            selectedCourse = course
                            showCourseDetail.toggle()
                        },
                        onCourseLongPress: { course in
                            selectedCourse = course
                            showEditView = true
                        },
                        onMultipleCoursesTap: { courses, cellIndex in
                            selectedCourses = courses
                            selectedCellIndex = cellIndex
                            showMultipleCoursesDetail = true
                        },
                        columns: coloums
                    )
                    Spacer()
                }
                .id("Top") // 给 ScrollView 顶部加 ID
            }.scrollIndicators(.hidden) // 隐藏滚动指示器
            .onChange(of: isQingShan) { newValue in coursetime = newValue ? coursetimeQS : coursetimeHJH
            }
            .onChange(of: scrollToTopTrigger) { newValue in
                withAnimation {
                    proxy.scrollTo("Top", anchor: .top)
                }
            }
            .onAppear {
                coursetime = isQingShan ? coursetimeQS : coursetimeHJH
            }
        }
    }
}
