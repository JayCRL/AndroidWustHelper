//
//  courseGrid.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/11/3.
//

import SwiftUI
struct CourseGrid: View {
    var courses: [Course]
    @Binding var weeknumber: Int
    @Binding var showBackGround:Bool
    var activeNumber: Int
    var onCellTap: (Int) -> Void
    var onCourseTap: (Course) -> Void
    var onCourseLongPress: (Course) -> Void
    var onMultipleCoursesTap: ([Course], Int) -> Void
    var columns: [GridItem]
    var body: some View {
        LazyVGrid(columns: columns) {
            ForEach(0..<42) { index in
                CourseCell(
                    index: index,
                    courses: courses, showBackGround: $showBackGround,
                    weeknumber: $weeknumber,
                    activeNumber: activeNumber,
                    onTap: onCellTap,
                    onCourseTap: onCourseTap,
                    onCourseLongPress: onCourseLongPress,
                    onMultipleCoursesTap: onMultipleCoursesTap
                )
            }
        }
        .frame(maxWidth: .infinity)
    }
}
