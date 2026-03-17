import SwiftUI

// 节次时间段选项
struct ClassSection: Identifiable {
    let id = UUID()
    let text: String
    let value: Int // 代表节次范围
}

struct EditView: View {
    @Binding var isPresented: Bool
    var course: Course // 要编辑的课程
    @State var MyCourse: Course = Course(name: "", teacher: "", teachClass: "", startWeek: 1, endWeek: 1, weekDay: 1, startSection: 1, endSection: 1, classroom: "")
    // 保存回调闭包
    var onSave: (Course) -> Void
    // 新增：删除回调（可选）
    var onDelete: ((Course) -> Void)?
    // 新增：控制删除弹窗显示
    @State private var showDeleteAlert = false
    @State var showWarning:Bool=false
    // 星期选项
    let weekdays = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"]
    
    // 节次选项
    let sections: [ClassSection] = [
        ClassSection(text: "1-2节", value: 1),
        ClassSection(text: "3-4节", value: 3),
        ClassSection(text: "5-6节", value: 5),
        ClassSection(text: "7-8节", value: 7),
        ClassSection(text: "9-10节", value: 9),
        ClassSection(text: "11-12节", value: 11)
    ]
    
    // 滚动选择器的当前索引
    @State private var selectedWeekdayIndex = 0
    @State private var selectedSectionIndex = 0
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                VStack(alignment: .leading, spacing: 20) {
                    // 课程名称
                    HStack {
                        Image(systemName: "book.fill")
                            .resizable()
                            .frame(width: 30, height: 30)
                            .foregroundColor(.blue)
                            .frame(width: 35)
                        
                        Text("课程名:")
                            .foregroundColor(Color("textColor"))
                            .font(.system(size: 15))
                        Spacer()
                        TextField("课程名称", text: $MyCourse.name)
                            .font(.system(size: 15))
                            .fontWeight(.medium)
                            .multilineTextAlignment(.center)
                            .padding(.vertical, 6).padding(.leading,5)
                            .frame(minWidth: 200)
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(10)
                    
                    // 教师
                    HStack {
                        Image(systemName: "person.fill")
                            .resizable()
                            .frame(width: 30, height: 30)
                            .foregroundColor(.blue)
                            .frame(width: 35)
                        
                        Text("教师:")
                            .foregroundColor(Color("textColor"))
                            .font(.system(size: 15))
                        Spacer()
                        TextField("教师", text: $MyCourse.teacher)
                            .font(.system(size: 15))
                            .fontWeight(.medium)
                            .multilineTextAlignment(.center)
                            .padding(.vertical, 6).padding(.leading,20)
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(10)
                    
                    // 教室
                    HStack {
                        Image(systemName: "building.2.fill")
                            .resizable()
                            .frame(width: 30, height: 30)
                            .foregroundColor(.blue)
                            .frame(width: 35)
                        
                        Text("教室:")
                            .foregroundColor(Color("textColor"))                           .font(.system(size: 15))
                        
                        TextField("教室", text: $MyCourse.classroom)
                            .font(.system(size: 15))
                            .fontWeight(.medium)
                            .multilineTextAlignment(.center)
                            .padding(.vertical, 6).padding(.leading,23)
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(10)
                    
                    // 班级
                    HStack {
                        Image(systemName: "person.3.fill")
                            .resizable()
                            .frame(width: 30, height: 30)
                            .foregroundColor(.blue)
                            .frame(width: 35)
                        
                        Text("班级:")
                            .foregroundColor(Color("textColor"))                           .font(.system(size: 15))
                        
                        TextField("班级", text: $MyCourse.teachClass)
                            .font(.system(size: 15))
                            .fontWeight(.medium)
                            .multilineTextAlignment(.center)
                            .padding(.vertical, 6).padding(.leading,25)
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(10)
                    
                    // 周次范围
                    HStack {
                        Image(systemName: "calendar")
                            .resizable()
                            .frame(width: 30, height: 30)
                            .foregroundColor(.blue)
                            .frame(width: 33)
                        
                        Text("周次:")  .font(.system(size: 15)) .frame(width: 35)
                            .foregroundColor(Color("textColor"))
                        
                        TextField("\(MyCourse.startWeek)", text: Binding(
                            get: { String(MyCourse.startWeek) },
                            set: { if let value = Int($0) { MyCourse.startWeek = value } }
                        ))
                        .font(.system(size: 20))
                            .fontWeight(.medium)
                            .multilineTextAlignment(.center)
                            .padding(.vertical, 6)
                            .frame(width: 50)
                            .padding(.leading, 50)
                        
                        Text("-")
                        
                        TextField("\(MyCourse.endWeek)", text: Binding(
                            get: { String(MyCourse.endWeek) },
                            set: { if let value = Int($0) { MyCourse.endWeek = value } }
                        ))
                        .font(.system(size: 20))                            .fontWeight(.medium)
                            .multilineTextAlignment(.center)
                            .padding(.vertical, 6)
                            .frame(width: 50)
                        
                        Text("周      ")
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(10)
                    HStack {
                        Text("上课时间:")  .font(.system(size: 25))
                            .foregroundColor(.blue.opacity(0.9))
                    }.frame(height: 20).padding(.leading, 15)
                    
                    // 日期和节次选择器（同一行）
                    HStack(spacing: 5) {
                        VStack(alignment: .leading, spacing: 0) {
                            Picker("星期", selection: $selectedWeekdayIndex) {
                                ForEach(0..<weekdays.count, id: \.self) { index in
                                    Text(self.weekdays[index]).tag(index)
                                }
                            }
                            .pickerStyle(WheelPickerStyle())
                            .frame(height: 130)
                            .onChange(of: selectedWeekdayIndex) { newIndex in
                                MyCourse.weekDay = newIndex + 1 // 转换为1-7的星期值
                            }
                        }
                        VStack(alignment: .leading, spacing: 0) {
                          
                            Picker("节次", selection: $selectedSectionIndex) {
                                ForEach(0..<sections.count, id: \.self) { index in
                                    Text(self.sections[index].text).tag(index)
                                }
                            }
                            .pickerStyle(WheelPickerStyle())
                            .frame(height: 130)
                            .onChange(of: selectedSectionIndex) { newIndex in
                                MyCourse.startSection = sections[newIndex].value
                                MyCourse.endSection = sections[newIndex].value + 1
                            }
                        }
                        Spacer()
                    }.padding(.top,-30)
                    
                }
                .padding()
                
                Spacer()
            }
            .padding()
            .navigationBarItems(
                leading: Button("取消") {
                    isPresented = false
                },
                trailing: HStack {
                    // 删除按钮
                    Button(role: .destructive) {
                        // 显示删除确认弹窗
                        showDeleteAlert = true
                    } label: {
                        Image(systemName: "trash")
                    }
                    
                    // 保存按钮
                    Button("保存") {
                        if(MyCourse.endWeek<MyCourse.startWeek){
                            showWarning=true
                        }else{
                            onSave(MyCourse)
                            isPresented = false
                        }
                    }
                }
            )
        }
        // 删除确认弹窗
        .alert("确认删除", isPresented: $showDeleteAlert) {
            Button("取消", role: .cancel) {}
            Button("删除", role: .destructive) {
                // 调用删除回调（通过onSave传递特殊标记，或新增删除回调）
                onDelete?(course)
                isPresented = false
            }
        }
        .alert("警告⚠️", isPresented: $showWarning) {
            Button("取消", role: .cancel) {}
            Button("确认", role: .destructive) {
                showDeleteAlert=false
            }
        } message: {
            Text("结束周不能小于开始周")
        }
        .onAppear {
            MyCourse = course
            print("原始课程ID: \(course.id)，复制后ID: \(MyCourse.startSection)") // 确认两者一致
            // 初始化选择器索引
            selectedWeekdayIndex = max(0, course.weekDay - 1)
            if let index = sections.firstIndex(where: { $0.value == course.startSection }) {
                selectedSectionIndex = index
            }
        }
    }
}

#Preview {
    let course = Course(
        name: "计算机网络",
        teacher: "李教授",
        teachClass: "计科2001",
        startWeek: 1,
        endWeek: 16,
        weekDay: 1,
        startSection: 1,
        endSection: 4,
        classroom: "教三-301"
    )
    
    return EditView(
        isPresented: .constant(true),
        course: course
    ) { savedCourse in
        print("保存的课程: \(savedCourse.name)")
    }
}
