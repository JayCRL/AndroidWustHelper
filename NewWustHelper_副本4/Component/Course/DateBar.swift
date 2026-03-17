//
//  DateBar.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/11/3.
//

import SwiftUI

struct DataBar:View {
    @Binding var currentMonth:String
    @Binding var weekNumber:Int
    @Binding var thisWeek:Int
    @Binding var showBackGround:Bool
    @State var day:Int
    @Binding var whichWeekDay:Int
    @Binding var weekdate: [String]  // 仍保留“月/日”显示数组
    func istoday(index:Int)->Bool{
        if(index+1==whichWeekDay && weekNumber==thisWeek){
            return true
        }else{
            return false
        }
    }
    func changetostring(index:Int)->String{
        switch index {
        case 1:
            return "周一"
        case 2:
            return "周二"
        case 3:
            return "周三"
        case 4:
            return "周四"
        case 5:
            return "周五"
        case 6:
            return "周六"
        case 7:
            return "周日"
        default:
            return "未知"
        }
    }
    var body: some View {
        HStack{
            RoundedRectangle(cornerRadius: 5)
                .fill(Color("bgcolor").opacity(showBackGround ? 0:1))
                .frame(width: 34, height: 40)
                .overlay {
                    HStack(){
                        
                        VStack(){
                            Text(currentMonth)
                                .foregroundColor(Color.gray)
                                .font(.system(size: 14)) // 使用固定字体大小
                                .minimumScaleFactor(0.7) // 添加最小缩放因子
                                .lineLimit(1)
                            Text("月")
                                .foregroundColor(Color.gray)
                                .font(.system(size: 12))
                                .minimumScaleFactor(0.7)
                                .lineLimit(1)
                        }.padding(.leading,8)
                        Spacer()
                    }
                }
            
            // 动态生成日期
            ForEach(0..<7) { index in
                RoundedRectangle(cornerRadius: 10)
                    .fill(showBackGround ? Color.orange.opacity(0) : (istoday(index: index) ? Color.gray.opacity(0.2) : Color("bgcolor")))
                    .frame(width: 42, height: 45)
                    .overlay {
                        VStack {
                            Text(changetostring(index: index + 1))
                                .foregroundColor(istoday(index: index) ? Color.blue.opacity(0.6) : Color.gray)
                                .font(.system(size: 12)) // 使用固定字体大小
                                                                .minimumScaleFactor(0.7) // 添加最小缩放因子
                                                                .lineLimit(1)
                                                                .multilineTextAlignment(.center)
                            Text("\(weekdate[index])")
                                .foregroundColor(istoday(index: index) ? Color.blue.opacity(0.6) : Color.gray)
                                .font(.system(size: 12))
                                                                .minimumScaleFactor(0.7)
                                                                .lineLimit(1)
                                                                .multilineTextAlignment(.center)
                        }
                    }.padding(.trailing,1)
            }
        }
    }
}
