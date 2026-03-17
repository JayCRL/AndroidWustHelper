package com.example.wusthelper.request;
/**
 * 武科大助手的一些常用接口
 * @date 2019年8月11日
 */
public class WustApi {
    //    129.211.66.191  测试用域名
//    118.89.45.172 正常使用的域名
//     wustlinghang.cn:8443
//      https://qyyzxty.xyz/wust_helper 新测试用域名
//    public static final String BASE_API = "http://129.211.66.191:1234";
//public static final String BASE_API = "https://wusthelper.wustlinghang.cn/mobileapi";正式版
//    public static final String BASE_API = "https://wusthelper.wustlinghang.cn/testapi";

    public static final String BASE_API = "https://wusthelper.wustlinghang.cn/mobileapi";

    // 新服务直连（当网关不可达/超时时可用）
    // 说明：这仍然是“新体系”接口，只是绕过 8088 网关直连各业务服务。
    public static final String BASIC_SERVER_HOST_HEADER = "lyzyy.love:8082";
    public static final String BASIC_SERVER_API = "http://8.162.1.176:8082";

    public static final String CHAT_SERVER_HOST_HEADER = "lyzyy.love:8096";
    public static final String CHAT_SERVER_API = "http://8.162.1.176:8096";

    // 新网关入口（聚合路由用；当前网络环境下可能不可达）
    public static final String GATEWAY_API = "http://lyzyy.love:8088";
    public static final String WUST_BASIC_API = GATEWAY_API + "/wust-basic";
    public static final String WUST_CHAT_API = GATEWAY_API + "/wust-chat";
   //测试
//    public static final String BASE_API = "https://www.violetsnow.link";
public static final String LOGIN_API = BASE_API+ "/v2/jwc/login";
    public static final String COMBINE_LOGIN_API = BASE_API+ "/v2/jwc/combine-login";
    public static final String INFO_API = BASE_API+ "/v2/jwc/get-student-info";
    public static final String GRADE_API = BASE_API+ "/v2/jwc/get-grade";
    public static final String ADMINISTER_URL = BASE_API+ "/api/AndroidApi/";

    public static final String LOGIN_GRADUATE_API = BASE_API+ "/v2/yjs/login";
    public static final String GRADUATE_INFO_API = BASE_API+ "/v2/yjs/get-student-info";
    public static final String GRADUATE_GRADE_API = BASE_API+ "/v2/yjs/get-grade";
    public static final String GRADUATE_CURRICULUM_API = BASE_API+ "/v2/yjs/get-course";
    public static final String GRADUATE_CREDIT_API = BASE_API+ "/v2/yjs/get-scheme";


    public static final String CHECK_TOKEN = BASE_API+ "/v2/lh/check-token";
    public static final String CREDIT_API = BASE_API+ "/v2/jwc/get-credit";
    public static final String SCHEME_API = BASE_API+ "/v2/jwc/get-scheme";
    public static final String CURRICULUM_API = BASE_API+ "/v2/jwc/get-curriculum";
    public static final String ANNOUNCEMENT_API = BASE_API+ "/v2/jwc/list-announcement";
    public static final String ANNOUNCEMENT_CONTENT_API = BASE_API+ "/jwc/getannouncementcontent";
    public static final String LIB_LOGIN = BASE_API+ "/v2/lib/login";
    public static final String VERIFICATION_CODE = BASE_API+ "/lib/pic";
//    public static final String LIB_RENT_INFO = BASE_API+ "/v2/lib/get-current-rent";
//    public static final String LIB_HISTORY = BASE_API+ "/v2/lib/get-rent-history";
//    public static final String LIB_BOOK_INFO = BASE_API+ "/v2/lib/get-book-detail";
//    public static final String LIB_ANNOUNCEMENTLIST = BASE_API+ "/v2/lib/list-anno";
//    public static final String LIB_ANNOUNCEMENTCONTENT = BASE_API+ "/v2/lib/get-anno-content";
    public  static final String VOLUNTEER_TIME= BASE_API+ "/volunteer/getInfo";
    public static final String UPDATE_API = "https://wusthelper.wustlinghang.cn/android/wusthelper_android.json";
    public static final String SHOOL_CALENDAR = "https://wusthelper.wustlinghang.cn/page/calendar";

    public static final String EMPTYCLASSROOM_URL = "https://wusthelper.wustlinghang.cn/class/emptyroom";
    public static final String OFFICIALWEB_URL = "https://wustlinghang.cn";

    public static final String ADD_COUNT_DOWN_URL = BASE_API+ "/v2/lh/add-countdown";

    public static final String GET_COUNT_DOWN_URL = BASE_API+ "/v2/lh/list-countdown";

    public static final String DELETE_COUNTDOWN_URL = BASE_API+ "/v2/lh/del-countdown";

    public static final String CHANGE_COUNTDOWN_URL = BASE_API+ "/v2/lh/modify-countdown";

    public static final String ADD_SHARE_COUNTDOWN_URL = BASE_API+ "/v2/lh/add-shared-countdown";


    public static final String CONSULT_URL = "https://news.wustlinghang.cn";
    public static final String LOSTCARD_URL = "https://lost.wustlinghang.cn";
    public static final String GET_LOSTCARD_MSG = "/v2/msg/get-android-msg";

    public static final String VOLUNTEER_URL = "https://volunteer.wustlinghang.cn";
    //public static final String VOLUNTEER_URL = "http://81.69.252.38/volunteermobile/";
    public static final String PRIVACY_URL = "https://wusthelper.wustlinghang.cn/page/android_privacy.html";
    public static final String NOTICE_URL = "https://wusthelper.wustlinghang.cn/wusthelperadminapi/v1/wusthelper/notice";
    public static final String GET_HELP_LOGIN_URL = "https://support.qq.com/product/275699/faqs-more";
    public static final String GET_CYCLE_IMAGE = "https://wusthelper.wustlinghang.cn/wusthelperadminapi/v1/wusthelper/act";
    public static final String GET_CONFIG = "https://wusthelper.wustlinghang.cn/wusthelperadminapi/v1/wusthelper/config";


//    public static final String LIB_DEL_COLLECTION= BASE_API+ "/v2/lib/del-collection";
//    public static final String LIB_ADD_COLLECTION= BASE_API+ "/v2/lib/add-collection";
//    public static final String LIB_LIST_COLLECTION= BASE_API+ "/v2/lib/list-collection";
    public static final String LIB_PIC= BASE_API+ "/v2/lib/pic";
    public static final String WEBSOCKET = BASE_API+ "wss://wusthelper.wustlinghang.cn/receive/android/";//websocket测试地址
    public static final String WLSYLOGIN = BASE_API+ "/v2/wlsy/login";
    public static final String WLSYGETCOURSES = BASE_API+ "/v2/wlsy/get-courses";

    //测试
//    public static final String BASE_TEST = "http://192.168.1.151:9596";
    public static final String LibLogin = BASE_API+"/v2/lib/login";
    public static final String LIB_RENT_INFO = BASE_API+"/v2/lib/get-current-rent";
    public static final String LIB_HISTORY = BASE_API+"/v2/lib/get-rent-history";
    public static final String LIB_BOOK_SEARCH = BASE_API+"/v2/lib/search";
    public static final String LIB_BOOK_INFO = BASE_API+"/v2/lib/get-book-detail";
    public static final String LIB_ANNOUNCEMENTLIST = BASE_API+"/v2/lib/list-anno";
    public static final String LIB_ANNOUNCEMENTCONTENT = BASE_API+"/v2/lib/get-anno-content";
    public static final String LIB_DEL_COLLECTION= BASE_API+"/v2/lib/del-collection";
    public static final String LIB_ADD_COLLECTION= BASE_API+"/v2/lib/add-collection";
    public static final String LIB_LIST_COLLECTION= BASE_API+"/v2/lib/list-collection";

    //空教室查询
    public static final String CLASSROOM_EMPTY_FIND = BASE_API + "/v2/clsroom/find-empty-classroom";
    public static final String CLASSROOM_COLLEGE_LIST = BASE_API + "/v2/clsroom/get-college-list";
    public static final String CLASSROOM_COURSE_LIST = BASE_API + "/v2/clsroom/get-course-name-list";
    public static final String CLASSROOM_COURSE_INFO = BASE_API + "/v2/clsroom/get-course-info";
    public static final String CLASSROOM_SEARCH_COLLEGE = BASE_API + "/v2/clsroom/search-in-college";
    public static final String CLASSROOM_SEARCH = BASE_API + "/v2/clsroom/search";

    public static final String UG_LOGIN_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/login";
    public static final String UG_STUDENT_INFO_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/getStudentInfo";
    // 以 wust-mywust-basic 后端代码为准：课表接口为 /getCourses
    public static final String UG_COURSE_TABLE_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/getCourses";
    // basic: 获取本学期起始日（后端配置 StartDay.*）
    public static final String UG_TERM_STARTDATE_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/getData";
    public static final String GRAD_TERM_STARTDATE_V2 = BASIC_SERVER_API + "/GraduatedController/getData";
    // 以 wust-mywust-basic 后端代码为准：成绩接口为 /getScores
    public static final String UG_SCORE_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/getScores";
    public static final String UG_TRAINING_PLAN_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/getTrainingPlan";
    public static final String GRAD_TRAINING_PLAN_V2 = BASIC_SERVER_API + "/GraduatedController/getTrainingPlan";

    public static final String SEARCH_COURSES_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/searchCourses";
    public static final String EMPTY_CLASSROOMS_V2 = BASIC_SERVER_API + "/UnderGraduateStudent/getEmptyClassrooms";
    public static final String ANDROID_NOTICE_V2 = BASIC_SERVER_API + "/operationLog/list/publishedButAndroid";
    public static final String CAROUSELS_V2 = BASIC_SERVER_API + "/admin/common/getCarousels";
    public static final String SCHOOL_CALENDAR_V2 = BASIC_SERVER_API + "/admin/common/getCalendar";
    public static final String AI_CHAT_V2 = CHAT_SERVER_API + "/api/rag/chat";
    public static final String AI_SUBMIT_V2 = CHAT_SERVER_API + "/api/rag/student/submit";

    public static final String LOST_NOTICE_UNREAD = "https://neolaf.lensfrex.net/api/v1/message/unread";
    public static final String LOST_NOTICE_MARK = "https://neolaf.lensfrex.net/api/v1/message/mark";
}

