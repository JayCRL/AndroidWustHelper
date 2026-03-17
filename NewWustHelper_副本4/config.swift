//
//  config.swift
//  NewWustHelper
//
//  Created by JSPV on 2025/8/31.
//
struct BasicValue{
    //获取教务系统数据 连接的阿里云服务器
    static var baseGetUrl="https://www.lyzyy.love"
    //解析数据 本地服务器解析
    static var baseParseUrl="https://www.lyzyy.love:8081"
    
    static var CampusMatebaseUrl="https://www.lyzyy.love:8081/CampusMate"
    static var CompetitionbaseUrl="https://www.lyzyy.love:8081/CompetitionGroup"
    static var SecondHandbaseUrl="https://www.lyzyy.love:8081/SecondHand"
    
    // 研究生管理系统API配置
    // 个人信息接口使用8009端口
    static let graduateSystemBaseUrl = "https://www.lyzyy.love:8009"
    // 课程获取接口使用8007端口
    static let graduateCourseBaseUrl = "https://www.lyzyy.love:8007"
    // 培养方案接口使用8007端口，域名无www
    static let graduateCultivationPlanBaseUrl = "https://www.lyzyy.love:8007"
}
//本科生还是研究生
struct Identify{
    static var chooseIdentify="/Graduate/Support"
    static var chooseParseIdentify="/mywustBasic/UnderGraduateStudent"

    //获取教务数据
    static let UnderGraduate="/UnderGraduate/Support"
    static let Graduate="/Graduate/Support"
    
    //解析数据接口
    static let ParseUnderGraduate="/mywustBasic/UnderGraduateStudent"
    static let ParseGraduate="/mywustBasic/GraduatedController"

}
struct Method{
    //登录细化
    static let GraduateLogin="/personal-info"
    static let UnderGraduateloginGetCookie="/loginGetCookie"
    //同名操作
    static let getTrainingPlanPage="/getTrainingPlanPage"
    static let getStudentInfoPage="/getStudentInfoPage"
    static let getCoursePage="/getCoursesPage"
   
    //获取图片接口
    static let Pictre="/admin/common/upload"
    
    //解析接口
    static let ParseLogin="/login"
    static let getTrainingPlan="/postTrainingPlan"
    static let getStudentInfo="/postStudentInfo"
    static let getData="/getData"
    static let getCourses="/getCourses"

    
    //只有本科生有
    static let GetScore="/UnderGraduatepostScore" //解析
    static let GetScorePage="/GetScorePage" //获取
    static let GetExamPage="/GetExamPage" //获取
    static let GetRequireParse="/postGraduateRequireParse"
    static let GetCreditStatus="/postCreditStatus"
}
struct ParseMethod{
    //登录细化
    static let GraduateLogin="/personal-info"
    static let UnderGraduateloginGetCookie="/loginGetCookie"
    //同名操作
    static let getTrainingPlanPage="/getTrainingPlanPage"
    static let getStudentInfo="/getStudentInfo"
    static let getCoursePage="/getCoursePage"
   
    //获取图片接口
    static let Pictre="/admin/common/upload"
}
