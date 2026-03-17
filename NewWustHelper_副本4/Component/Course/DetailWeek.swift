//
//  DetailWeek.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/11/3.
//

import SwiftUI

struct DetailWeek:View {
    @Binding var weeknumber:Int //当前周数
    @State var month:Int
    @State var day:Int
    @State var activeNumber:Int
    @State var ischoose:Bool
    @State var courses: [Course]
    public var allWeeksCourseMap: [Int: [Int: [Int]]] {
       var result: [Int: [Int: [Int]]] = [:]
       
       // 初始化所有周的哈希表
       for weekNumber in 1...25 {
           result[weekNumber] = [:]
       }
       // 遍历所有课程，填充哈希表
       for course in courses {
           // 计算课程覆盖的周数范围
           let startWeek = max(1, course.startWeek)
           let endWeek = min(25, course.endWeek)
           for weekNumber in startWeek...endWeek {
               // 确保该周的哈希表已初始化
               if result[weekNumber] == nil {
                   result[weekNumber] = [:]
               }
               // 确保该星期的数组已初始化
               if result[weekNumber]![course.weekDay] == nil {
                   result[weekNumber]![course.weekDay] = []
               }
               let section = course.endSection/2
               // 添加该课程的所有节次
                   if !result[weekNumber]![course.weekDay]!.contains(section) {
                       result[weekNumber]![course.weekDay]!.append(section)
                   }
               
           }
       }
       return result
   }
    var body: some View {
        Rectangle()
            .fill(Color.gray.opacity(0.3))
            .frame(height: ischoose ? 90 : 0)
            .overlay {
                ScrollView(.horizontal, showsIndicators: false, content: {
                    HStack(spacing: 3) {
                        ForEach(0..<25, content: { index in
                            WeekCell(
                                index: index,
                                isCurrentWeek: (index + 1) == weeknumber,
                                onTap: { selectedWeek in
                                    withAnimation {
                                        weeknumber = selectedWeek
                                    }
                                    print("周数已选择: \(selectedWeek)")
                                },
                                allWeeksCourseMap: allWeeksCourseMap
                            )
                        })
                    }
                })
            }
    }
}
