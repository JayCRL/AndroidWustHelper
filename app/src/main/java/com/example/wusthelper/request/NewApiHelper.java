package com.example.wusthelper.request;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.bean.javabean.CountDownAddData;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.bean.javabean.CountDownChangeData;
import com.example.wusthelper.bean.javabean.data.AiQaData;
import com.example.wusthelper.bean.javabean.data.AnnouncementContentData;
import com.example.wusthelper.bean.javabean.data.AnnouncementData;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.BookData;
import com.example.wusthelper.bean.javabean.data.CollegeData;
import com.example.wusthelper.bean.javabean.data.ConfigData;
import com.example.wusthelper.bean.javabean.data.CourseData;
import com.example.wusthelper.bean.javabean.data.CourseNameData;
import com.example.wusthelper.bean.javabean.data.CreditsData;
import com.example.wusthelper.bean.javabean.data.CycleImageData;
import com.example.wusthelper.bean.javabean.data.EmptyClassroomData;
import com.example.wusthelper.bean.javabean.data.EmptyClassroomSimpleData;
import com.example.wusthelper.bean.javabean.data.GatewayCarouselData;
import com.example.wusthelper.bean.javabean.data.GatewayNoticeData;
import com.example.wusthelper.bean.javabean.data.GradeData;
import com.example.wusthelper.bean.javabean.data.GraduateData;
import com.example.wusthelper.bean.javabean.data.GraduateGradeData;
import com.example.wusthelper.bean.javabean.data.LibCollectData;
import com.example.wusthelper.bean.javabean.data.LibraryHistoryData;
import com.example.wusthelper.bean.javabean.data.LostData;
import com.example.wusthelper.bean.javabean.data.SchoolCalendarData;
import com.example.wusthelper.bean.javabean.data.SearchBookData;
import com.example.wusthelper.bean.javabean.data.SearchCourseData;
import com.example.wusthelper.bean.javabean.data.SearchCourseFilterData;
import com.example.wusthelper.bean.javabean.data.StudentData;
import com.example.wusthelper.bean.javabean.data.TermStartDateData;
import com.example.wusthelper.bean.javabean.data.TokenData;
import com.example.wusthelper.bean.javabean.CountDownData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.request.okhttp.request.RequestParams;
import com.example.wusthelper.utils.CountDownUtils;

import java.io.IOException;

import okhttp3.Response;

/**
 * 请求中心，所以的api调用都在这里进行
 * 这里面所有的请求，都会默认带上token
 */
public class NewApiHelper {

    private static final String TAG = "NewApiHelper";

    public static boolean isLogin() {
        String t = getToken();
        return t != null && !t.isEmpty();
    }

    public static String getToken() {
        String t = RequestCenter.getToken();
        if (t == null || t.trim().isEmpty()) {
            t = SharePreferenceLab.getToken();
            if (t != null && !t.trim().isEmpty()) {
                RequestCenter.setToken(t.trim());
            }
        }
        return t == null ? "" : t;
    }

    public static void setToken(String token) {
        RequestCenter.setToken(token);
    }

    public static void setMessage(String message) {
        RequestCenter.setMessage(message);
    }

    public static void clearLoginState() {
        RequestCenter.setToken("");
        RequestCenter.setMessage("");
        SharePreferenceLab.setToken("");
        SharePreferenceLab.setIsLogin(false);
        SharePreferenceLab.getInstance().setMessage(MyApplication.getContext(), "");
    }

    /**
     * 用户登陆请求
     */
    public static void login(String studentId, String password, DisposeDataListener listener) {
        loginUndergraduateV2(studentId, password, listener);
    }

    public static void loginUndergraduateV2(String studentId, String password, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("username", studentId);
        params.put("password", password);

        RequestParams headers = new RequestParams();
        headers.put("Platform", "android");
        headers.put("Content-Type", "application/json");
        // 直连 basic 服务时需要显式带 Host，确保后端按域名路由/识别
        headers.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);

        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.UG_LOGIN_V2, params, headers, listener, TokenData.class);
    }

    /**
     * 用户登陆请求,研究生
     */
    public static void loginGraduate(String studentId, String password, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        RequestParams headers = new RequestParams();
        params.put("stuNum", studentId);
        params.put("jwcPwd", password);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Platform", "android");
        RequestCenter.postRequest(WustApi.LOGIN_GRADUATE_API, params, headers, listener, TokenData.class);
    }

    public static Response login(String username, String password) throws IOException {
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);

        RequestParams headers = new RequestParams();
        headers.put("Platform", "android");
        headers.put("Content-Type", "application/json");
        headers.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);

        return RequestCenter.postJsonRequestExecuteWithoutLegacyToken(WustApi.UG_LOGIN_V2, params, headers, TokenData.class);
    }

    public static Response loginGraduate(String username, String password) throws IOException {
        RequestParams params = new RequestParams();
        RequestParams headers = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        headers.put("Content-Type", "application/json");
        headers.put("Platform", "android");
        return RequestCenter.postJsonRequestExecute(WustApi.LOGIN_GRADUATE_API, params,headers, TokenData.class);
    }

    public static void getUserInfo(DisposeDataListener listener) {
        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.UG_STUDENT_INFO_V2, null, extraHeaders, listener, StudentData.class);
    }

    public static void getGraduateInfo(DisposeDataListener listener) {
        RequestCenter.get(WustApi.GRADUATE_INFO_API, null, listener, GraduateData.class);
    }

    public static void getCourse(String semester, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        // wust-mywust-basic: /UnderGraduateStudent/getCourses?term=...
        putIfNotBlank(params, "term", semester);

        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.UG_COURSE_TABLE_V2, params, extraHeaders, listener, CourseData.class);
    }

    public static void getQrCourse(String token,String semester, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("schoolTerm", semester);
        RequestCenter.getQr(token,WustApi.CURRICULUM_API, params, listener, CourseData.class);
    }

    public static void getGraduateCourse( DisposeDataListener listener) {
        RequestCenter.get(WustApi.GRADUATE_CURRICULUM_API, null, listener, CourseData.class);
    }

    public static void getCheckToken(DisposeDataListener listener) {
        RequestCenter.get(WustApi.CHECK_TOKEN, null, listener, BaseData.class);
    }

    public static void getConfig(DisposeDataListener listener) {
        RequestParams headers = new RequestParams();
        headers.put("Platform", "android");
        headers.put("Token", getToken());
        RequestCenter.getRequest(WustApi.GET_CONFIG, null, headers, listener, ConfigData.class);
    }

    public static void getNotice(DisposeDataListener listener) {
        // 公告统一切到新服务端接口（首页与弹窗公告一致）
        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.ANDROID_NOTICE_V2, null, extraHeaders, listener, GatewayNoticeData.class);
    }

    public static void getAndroidNotices(DisposeDataListener listener) {
        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.ANDROID_NOTICE_V2, null, extraHeaders, listener, GatewayNoticeData.class);
    }

    public static void getGrade(DisposeDataListener listener) {
        // 本科生成绩走新 basic 服务
        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.UG_SCORE_V2, null, extraHeaders, listener, GradeData.class);
    }

    public static void getGraduateGrade(DisposeDataListener listener) {
        RequestCenter.get(WustApi.GRADUATE_GRADE_API, null, listener, GraduateGradeData.class);
    }

    public static void getShareCountdown(String onlyId, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("uuid", onlyId);
        RequestCenter.get(WustApi.ADD_SHARE_COUNTDOWN_URL, params, listener, BaseData.class);
    }

    public static void getCountDownFormNet(DisposeDataListener listener) {
        RequestCenter.get(WustApi.GET_COUNT_DOWN_URL, null, listener, CountDownData.class);
    }

    public static void deleteCountDownFromNet(DisposeDataListener listener, String onlyId) {
        RequestParams params = new RequestParams();
        params.put("uuid", onlyId);
        RequestCenter.get(WustApi.DELETE_COUNTDOWN_URL, params, listener, BaseData.class);
    }

    public static void uploadCountDownFromNet(DisposeDataListener listener, CountDownBean countDownBean) {
        RequestParams params = new RequestParams();
        params.put("name",countDownBean.getName());
        params.put("time",CountDownUtils.getShowTime(countDownBean.getTargetTime()));
        params.put("comment",countDownBean.getNote());
        RequestCenter.postJsonRequest(WustApi.ADD_COUNT_DOWN_URL, params, listener, CountDownAddData.class);
    }

    public static void changeCountDown(CountDownBean countDownBean,DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("uuid",countDownBean.getOnlyId());
        params.put("name",countDownBean.getName());
        params.put("time",CountDownUtils.getShowTime(countDownBean.getTargetTime()));
        params.put("comment",countDownBean.getNote());
        RequestCenter.postJsonRequest(WustApi.CHANGE_COUNTDOWN_URL, params, listener, CountDownChangeData.class);
    }

    public static void getCredit(DisposeDataListener listener){
        if(SharePreferenceLab.getIsGraduate()) {
            RequestCenter.get(WustApi.GRADUATE_CREDIT_API,null,listener, CreditsData.class);
        }else {
            RequestCenter.get(WustApi.CREDIT_API,null,listener, CreditsData.class);
        }
    }

    public static void getScheme(DisposeDataListener listener){
        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        if(SharePreferenceLab.getIsGraduate()) {
            RequestCenter.getWithBearer(WustApi.GRAD_TRAINING_PLAN_V2, null, extraHeaders, listener, CreditsData.class);
        } else {
            RequestCenter.getWithBearer(WustApi.UG_TRAINING_PLAN_V2, null, extraHeaders, listener, CreditsData.class);
        }
    }

    public static void getCycleImage(DisposeDataListener listener){
        RequestCenter.get(WustApi.GET_CYCLE_IMAGE,null,listener, CycleImageData.class);
    }

    public static void getCarousels(DisposeDataListener listener) {
        RequestParams headers = new RequestParams();
        headers.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        // /admin/common/getCarousels 在不同部署下可能需要 Bearer。
        // 这里不走 legacy Token（避免旧体系干扰），但如果本地已有 token，则显式带上 Authorization。
        String token = getToken();
        if (token != null && !token.trim().isEmpty()) {
            headers.put("Authorization", "Bearer " + token.trim());
        }
        RequestCenter.getWithoutToken(WustApi.CAROUSELS_V2, null, headers, listener, GatewayCarouselData.class);
    }

    public static void getSchoolCalendar(DisposeDataListener listener) {
        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.SCHOOL_CALENDAR_V2, null, extraHeaders, listener, SchoolCalendarData.class);
    }

    /**
     * basic 后端获取“本学期起始日”（year/month/day）。
     * 注意：该接口当前不带 term 参数，后端返回的是配置的当前学期起始日。
     */
    public static void getTermStartDate(DisposeDataListener listener) {
        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        if (SharePreferenceLab.getIsGraduate()) {
            // 研究生接口仍走旧体系，但 basic 也提供了毕业生 startDate，这里优先尝试 basic
            RequestCenter.getWithBearer(WustApi.GRAD_TERM_STARTDATE_V2, null, extraHeaders, listener, TermStartDateData.class);
        } else {
            RequestCenter.getWithBearer(WustApi.UG_TERM_STARTDATE_V2, null, extraHeaders, listener, TermStartDateData.class);
        }
    }

    public static void chatWithAi(String question, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("question", question);

        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.CHAT_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.AI_CHAT_V2, params, extraHeaders, listener, AiQaData.class);
    }

    public static void submitAiCorpus(String text, String source, String tag, String expireAt, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "text", text);
        putIfNotBlank(params, "source", source);
        putIfNotBlank(params, "tag", tag);
        putIfNotBlank(params, "expireAt", expireAt);

        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.CHAT_SERVER_HOST_HEADER);
        RequestCenter.postJsonWithBearer(WustApi.AI_SUBMIT_V2, params, extraHeaders, listener, com.example.wusthelper.bean.javabean.data.AiSubmitData.class);
    }

    public static void searchCourses(String courseName, String teacherName, String classroom,
                                     String weekDay, String section, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "name", courseName);
        putIfNotBlank(params, "teacher", teacherName);
        putIfNotBlank(params, "classroom", classroom);
        putIfNotBlank(params, "weekDay", weekDay);
        putIfNotBlank(params, "section", section);

        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.SEARCH_COURSES_V2, params, extraHeaders, listener, SearchCourseFilterData.class);
    }

    public static void getEmptyClassrooms(String week, String weekDay, String section, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "week", week);
        putIfNotBlank(params, "weekDay", weekDay);
        putIfNotBlank(params, "section", section);

        RequestParams extraHeaders = new RequestParams();
        extraHeaders.put("Host", WustApi.BASIC_SERVER_HOST_HEADER);
        RequestCenter.getWithBearer(WustApi.EMPTY_CLASSROOMS_V2, params, extraHeaders, listener, EmptyClassroomSimpleData.class);
    }

    private static void putIfNotBlank(RequestParams params, String key, String value) {
        if (value == null) {
            return;
        }
        String v = value.trim();
        if (!v.isEmpty()) {
            params.put(key, v);
        }
    }

    public static void postLoginPhysical(String password, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        RequestParams headers = new RequestParams();
        params.put("wlsyPwd", password);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Token", getToken());
        headers.put("Platform", "android");
        RequestCenter.postRequest(WustApi.WLSYLOGIN, params, headers, listener, BaseData.class);
    }

    public static void getPhysicalCourse(DisposeDataListener listener){
        RequestCenter.get(WustApi.WLSYGETCOURSES,null,listener, CourseData.class);
    }

    public static void postLoginLibrary(String password, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        RequestParams headers = new RequestParams();
        params.put("libPwd", password);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Token", getToken());
        headers.put("Platform", "android");
        RequestCenter.postRequest(WustApi.LibLogin, params, headers, listener, BaseData.class);
    }

    public static void getHistoryBook(DisposeDataListener listener) {
        RequestCenter.get(WustApi.LIB_HISTORY, null,listener, LibraryHistoryData.class);
    }

    public static void getRentInfo(DisposeDataListener listener) {
        RequestCenter.get(WustApi.LIB_RENT_INFO, null,listener, LibraryHistoryData.class);
    }

    public static void getLibMakeAddCollection(String title,String isbn,String author,String publisher,String bookDetailUrl, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("title", title);
        params.put("isbn", isbn);
        params.put("author", author);
        params.put("publisher", publisher);
        params.put("detailUrl", bookDetailUrl);
        RequestCenter.postJsonRequest(WustApi.LIB_ADD_COLLECTION, params, listener, BaseData.class);
    }

    public static void getDelCollection(String isbn,DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("isbn", isbn);
        RequestCenter.get(WustApi.LIB_DEL_COLLECTION, params, listener, BaseData.class);
    }

    public static void getBookDetail(String url, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        RequestParams headers = new RequestParams();
        params.put("url", url);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Token", getToken());
        headers.put("Platform", "android");
        RequestCenter.postRequest(WustApi.LIB_BOOK_INFO, params, headers, listener, BookData.class);
    }

    public static void getLibListCollection(String pageNum,DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("pageNum", pageNum);
        RequestCenter.get(WustApi.LIB_LIST_COLLECTION, params, listener, LibCollectData.class);
    }

    public static void getLibraryAnnouncement(String pageNum,DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("pageNum",pageNum);
        RequestCenter.get(WustApi.LIB_ANNOUNCEMENTLIST,params,listener, AnnouncementData.class);
    }

    public static void getLibraryAnnouncementDetail(String announcementId, DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("announcementId",announcementId);
        RequestCenter.get(WustApi.LIB_ANNOUNCEMENTCONTENT,params,listener, AnnouncementContentData.class);
    }

    public static void searchLibraryBook(String pageNum,String keyWord,DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        RequestParams headers = new RequestParams();
        params.put("pageNum", pageNum);
        params.put("keyWord", keyWord);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Token", getToken());
        headers.put("Platform", "android");
        RequestCenter.postRequest(WustApi.LIB_BOOK_SEARCH, params, headers, listener, SearchBookData.class);
    }

    public static void findEmptyClassroom(String buildingName,String areaNum,String campusName,String week,String weekDay,String section,DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("buildingName",buildingName);
        params.put("areaNum",areaNum);
        params.put("campusName",campusName);
        params.put("week",week);
        params.put("weekDay",weekDay);
        params.put("section",section);
        RequestCenter.get(WustApi.CLASSROOM_EMPTY_FIND,params,listener, EmptyClassroomData.class);
    }

    public static void getCollegeList(DisposeDataListener listener){
        RequestCenter.get(WustApi.CLASSROOM_COLLEGE_LIST,null,listener, CollegeData.class);
    }

    public static void getCourseNameList(String collegeId,String pageNum,DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("collegeId",collegeId);
        params.put("pageNum",pageNum);
        RequestCenter.get(WustApi.CLASSROOM_COURSE_LIST,params,listener, CourseNameData.class);
    }

    public static void getCourseInfo(String collegeId,String courseName,String pageNum,DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("collegeId",collegeId);
        params.put("courseName",courseName);
        params.put("pageNum",pageNum);
        RequestCenter.get(WustApi.CLASSROOM_COURSE_INFO,params,listener, SearchCourseData.class);
    }

    public static void searchInCollege(String collegeId,String key,String pageNum,DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("collegeId",collegeId);
        params.put("key",key);
        params.put("pageNum",pageNum);
        RequestCenter.get(WustApi.CLASSROOM_SEARCH_COLLEGE,params,listener, SearchCourseData.class);
    }

    public static void searchALL(String key,String pageNum,DisposeDataListener listener){
        RequestParams params = new RequestParams();
        params.put("key",key);
        params.put("pageNum",pageNum);
        RequestCenter.get(WustApi.CLASSROOM_SEARCH,params,listener, SearchCourseData.class);
    }

    public static void getLostUnread(DisposeDataListener listener) {
        RequestCenter.get(WustApi.LOST_NOTICE_UNREAD, null, listener, LostData.class);
    }

    public static void getLostMark(DisposeDataListener listener) {
        RequestCenter.get(WustApi.LOST_NOTICE_MARK, null, listener, LostData.class);
    }
}
