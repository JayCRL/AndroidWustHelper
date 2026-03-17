import SwiftUI

struct PhoneNumber: View {
    // 模拟数据
    let studentCommonContacts = [
        Contact(name: "保卫处1", phone: "027-68893272"),
        Contact(name: "保卫处2", phone: "027-68893392"),
        Contact(name: "黄家湖校区综合办公室", phone: "027-68893276"),
        Contact(name: "洪山校区综合办公室", phone: "027-51012586"),
        Contact(name: "教务处", phone: "027-68862468"),
        Contact(name: "后勤集团", phone: "027-68862221"),
        Contact(name: "校医院", phone: "027-68893271")
    ]
    let partyAndGovernmentContacts = [
        Contact(name: "学校办公室", phone: "027-68862478"),
        Contact(name: "黄家湖校区综合办公室", phone: "027-68893276"),
        Contact(name: "洪山校区综合办公室", phone: "027-51012586"),
        Contact(name: "纪委（监察处）", phone: "027-68862473"),
        Contact(name: "党委组织部（机关党委）", phone: "027-68862793"),
        Contact(name: "党委统战部", phone: "027-68862589"),
        Contact(name: "党委学生会工作部（武装部、学生...", phone: "027-68862673"),
        Contact(name: "工会（妇女委员会、教代会）", phone: "027-68863508"),
        Contact(name: "团委", phone: "027-68862339")
    ]
    let administrativeContacts = [
        Contact(name: "研究生学位与学科建设科", phone: "027-68862026"),
        Contact(name: "研究生培养教育处", phone: "027-68862116"),
        Contact(name: "研究生招生就业处", phone: "027-68862830"),
        Contact(name: "人事处", phone: "027-68862406"),
        Contact(name: "教务处", phone: "027-68862468"),
        Contact(name: "教学质量监控与评估处", phone: "027-68862055"),
        Contact(name: "发展规划处（高等教育研究所）", phone: "027-68862410"),
        Contact(name: "财务处", phone: "027-68862458"),
        Contact(name: "审计处", phone: "027-68862466"),
        Contact(name: "国有资产与实验室管理处", phone: "027-68862205"),
        Contact(name: "基建与后勤管理处", phone: "027-68862819"),
        Contact(name: "国际交流合作处", phone: "027-68862606")
    ]
    let directlyAffiliatedUnitsContacts = [
        Contact(name: "工程训练中心", phone: "027-68893669"),
        Contact(name: "现代教育信息中心", phone: "027-68862211"),
        Contact(name: "图书馆", phone: "027-68862220"),
        Contact(name: "档案馆", phone: "027-68862017"),
        Contact(name: "学报编辑部", phone: "027-68862317"),
        Contact(name: "后勤集团", phone: "027-68862221"),
        Contact(name: "资产经营有限公司（科技园有限公...", phone: "027-68863373"),
        Contact(name: "校医院", phone: "027-68893271"),
        Contact(name: "耐火材料与冶金省部共建国家重点...", phone: "027-68862085"),
        Contact(name: "国际钢铁研究院", phone: "027-68862772"),
        Contact(name: "绿色制造与节能减排中心", phone: "027-68862815"),
        Contact(name: "继续教育学院（职业技术学院）", phone: "027-51012585"),
        Contact(name: "附属天佑医院", phone: "027-87896186")
    ]
    
    var body: some View {
            List {
                // 可展开的分组，模拟其他部门
                Section() {
                    DisclosureGroup(content: {
                        ForEach(studentCommonContacts, id: \.phone) { contact in
                            ContactRow(contact: contact)
                        }
                    }, label: {
                        HStack(){
                            ZStack(){
                                Image("student").resizable().frame(width: 30,height: 30)
                                Circle().stroke(Color.blue.opacity(0.3)).frame(width: 40,height: 40)
                                Circle().stroke(Color.blue.opacity(0.5)).frame(width: 50,height: 50)
                            }
                            Text("学生常用").font(.caption)
                        }
                    })
                    DisclosureGroup(content: {
                        ForEach(partyAndGovernmentContacts, id: \.phone) { contact in
                            ContactRow(contact: contact)
                        }
                    }, label: {
                        HStack(){
                            ZStack(){
                                Image("government").resizable().frame(width: 30,height: 30)
                                Circle().stroke(Color.red.opacity(0.3)).frame(width: 40,height: 40)
                                Circle().stroke(Color.red.opacity(0.5)).frame(width: 50,height: 50)
                            }
                            Text("党政部门").font(.caption)
                        }
                    })
                    DisclosureGroup(content: {
                        ForEach(administrativeContacts, id: \.phone) { contact in
                            ContactRow(contact: contact)
                        }
                    }, label: {
                        HStack(){
                            ZStack(){
                                Image("administration").resizable().frame(width: 30,height: 30)
                                Circle().stroke(Color.green.opacity(0.3)).frame(width: 40,height: 40)
                                Circle().stroke(Color.green.opacity(0.5)).frame(width: 50,height: 50)
                            }
                            Text("行政部门").font(.caption)
                        }
                    })
                    DisclosureGroup(content: {
                        ForEach(directlyAffiliatedUnitsContacts, id: \.phone) { contact in
                            ContactRow(contact: contact)
                        }
                    }, label: {
                        HStack(){
                            ZStack(){
                                Image("department").resizable().frame(width: 30,height: 30)
                                Circle().stroke(Color.yellow.opacity(0.3)).frame(width: 40,height: 40)
                                Circle().stroke(Color.yellow.opacity(0.5)).frame(width: 50,height: 50)
                            }
                            Text("直属单位").font(.caption)
                        }
                    })
                }
            }      .padding(.bottom, 1)
            .listStyle(GroupedListStyle())
    }
}
struct Contact: Identifiable {
    var id = UUID()
    var name: String
    var phone: String
}
struct ContactRow: View {
    var contact: Contact
    
    var body: some View {
        HStack {
            Image(systemName: "phone.circle.fill").resizable().frame(width: 35,height: 35)
                .foregroundColor(.green)
            Text(contact.name)
                .fontWeight(.bold)
            Spacer()
            Text(contact.phone)
                .foregroundColor(.gray)
            Spacer()
        }
        .padding()
        .onTapGesture {
            // 按钮点击事件，拨打电话
            if let url = URL(string: "tel://\(contact.phone)") {
                UIApplication.shared.open(url)
            }
        }
    }
}

struct PhoneNumber_Previews: PreviewProvider {
    static var previews: some View {
        PhoneNumber()
    }
}
