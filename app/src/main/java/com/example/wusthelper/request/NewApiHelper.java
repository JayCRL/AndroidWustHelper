package com.example.wusthelper.request;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.bean.javabean.CountDownAddData;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.bean.javabean.CountDownChangeData;
import com.example.wusthelper.bean.javabean.data.AiQaData;
import com.example.wusthelper.bean.javabean.data.AnnouncementContentData;
import com.example.wusthelper.bean.javabean.data.AnnouncementData;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.BookData;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityDetailData;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityListData;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityStatsData;
import com.example.wusthelper.bean.javabean.data.CampusMateApplicationListData;
import com.example.wusthelper.bean.javabean.data.CampusMateNotificationListData;
import com.example.wusthelper.bean.javabean.data.CampusMateUserInfoData;
import com.example.wusthelper.bean.javabean.data.CatCommentListData;
import com.example.wusthelper.bean.javabean.data.CatPostListData;
import com.example.wusthelper.bean.javabean.data.CollegeData;
import com.example.wusthelper.bean.javabean.data.Commodity;
import com.example.wusthelper.bean.javabean.data.CommodityDetailData;
import com.example.wusthelper.bean.javabean.data.CompetitionPostPageData;
import com.example.wusthelper.bean.javabean.data.CompetitionResponsePageData;
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
import com.example.wusthelper.bean.javabean.data.PageCommodity;
import com.example.wusthelper.bean.javabean.data.SchoolCalendarData;
import com.example.wusthelper.bean.javabean.data.SearchBookData;
import com.example.wusthelper.bean.javabean.data.SearchCourseData;
import com.example.wusthelper.bean.javabean.data.SearchCourseFilterData;
import com.example.wusthelper.bean.javabean.data.SecondHandPageData;
import com.example.wusthelper.bean.javabean.data.SimpleIdListData;
import com.example.wusthelper.bean.javabean.data.StudentData;
import com.example.wusthelper.bean.javabean.data.TermStartDateData;
import com.example.wusthelper.bean.javabean.data.TokenData;
import com.example.wusthelper.bean.javabean.CountDownData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.request.okhttp.request.RequestParams;
import com.example.wusthelper.ui.activity.LoginMvpActivity;
import com.example.wusthelper.utils.CountDownUtils;
import com.example.wusthelper.utils.ToastUtil;

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

    public static void handleUnauthorized(Context context, String message) {
        clearLoginState();
        String finalMessage = TextUtils.isEmpty(message) ? "登录已失效，请重新登录" : message;
        ToastUtil.show(finalMessage);
        Context targetContext = context == null ? MyApplication.getContext() : context;
        Intent intent = LoginMvpActivity.newInstance(targetContext);
        if (!(targetContext instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            targetContext.startActivity(intent);
            return;
        }
        targetContext.startActivity(intent);
        ((Activity) targetContext).finish();
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
        params.put("stuNum", username);
        params.put("jwcPwd", password);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Platform", "android");
        return RequestCenter.postRequestExecute(WustApi.LOGIN_GRADUATE_API, params, headers, TokenData.class);
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

    public static void getCampusMateActivities(String campus, String college, String type, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "campus", campus);
        putIfNotBlank(params, "college", college);
        putIfNotBlank(params, "type", type);
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_ACTIVITY_LIST, params, null, listener, CampusMateActivityListData.class);
    }

    public static void getCampusMateActivityDetail(int activityId, DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_ACTIVITY_PREFIX + activityId, null,
                buildWusterHeaders(), listener, CampusMateActivityDetailData.class);
    }

    public static void createCampusMateActivity(String title, String description, String type, String activityTime,
                                                String location, int minPeople, int maxPeople, String expireTime,
                                                String campus, String college, String tags, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "title", title);
        putIfNotBlank(params, "description", description);
        putIfNotBlank(params, "type", type);
        putIfNotBlank(params, "activityTime", activityTime);
        putIfNotBlank(params, "location", location);
        params.put("minPeople", String.valueOf(minPeople));
        params.put("maxPeople", String.valueOf(maxPeople));
        putIfNotBlank(params, "expireTime", expireTime);
        putIfNotBlank(params, "campus", campus);
        putIfNotBlank(params, "college", college);
        putIfNotBlank(params, "tags", tags);
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.CAMPUS_MATE_ACTIVITY_CREATE, params,
                buildWusterHeaders(), listener, BaseData.class);
    }

    public static void getCampusMateLikedIds(DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_LIKED_IDS, null, buildWusterHeaders(), listener, SimpleIdListData.class);
    }

    public static void getCampusMateFavoriteIds(DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_FAVORITE_IDS, null, buildWusterHeaders(), listener, SimpleIdListData.class);
    }

    public static void getCampusMateMyCreated(DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_MY_CREATED, null, buildWusterHeaders(), listener, CampusMateActivityListData.class);
    }

    public static void getCampusMateMyApplications(DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_APPLICATIONS, null, buildWusterHeaders(), listener, CampusMateApplicationListData.class);
    }

    public static void getCampusMateUserInfo(DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_USER_ME, null, buildWusterHeaders(), listener, CampusMateUserInfoData.class);
    }

    public static void getCampusMateNotifications(DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_NOTIFICATIONS, null, buildWusterHeaders(), listener, CampusMateNotificationListData.class);
    }

    public static void getCampusMateActivityStats(int activityId, DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.CAMPUS_MATE_ACTIVITY_PREFIX + activityId + "/stats", null,
                buildWusterHeaders(), listener, CampusMateActivityStatsData.class);
    }

    public static void toggleCampusMateLike(int activityId, DisposeDataListener listener) {
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.CAMPUS_MATE_ACTIVITY_PREFIX + activityId + "/like",
                new RequestParams(), buildWusterHeaders(), listener, BaseData.class);
    }

    public static void toggleCampusMateFavorite(int activityId, DisposeDataListener listener) {
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.CAMPUS_MATE_ACTIVITY_PREFIX + activityId + "/favorite",
                new RequestParams(), buildWusterHeaders(), listener, BaseData.class);
    }

    public static void applyCampusMateActivity(int activityId, String reason, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "reason", reason);
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.CAMPUS_MATE_ACTIVITY_PREFIX + activityId + "/apply",
                params, buildWusterHeaders(), listener, BaseData.class);
    }

    public static void cancelCampusMateApplication(int applicationId, DisposeDataListener listener) {
        RequestCenter.putJsonRequestWithoutLegacyToken(WustApi.CAMPUS_MATE_APPLICATION_PREFIX + applicationId + "/cancle",
                new RequestParams(), buildWusterHeaders(), listener, BaseData.class);
    }

    public static void getSecondHandDetail(int pid, DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.SECOND_HAND_DETAIL_PREFIX + pid, null,
                buildWusterHeaders(), listener, CommodityDetailData.class);
    }

    public static void addSecondHandCollection(int pid, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("pid", String.valueOf(pid));
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.SECOND_HAND_COLLECTION_ADD, params,
                buildWusterHeaders(), listener, BaseData.class);
    }

    public static void removeSecondHandCollection(int pid, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("pid", String.valueOf(pid));
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.SECOND_HAND_COLLECTION_REMOVE, params,
                buildWusterHeaders(), listener, BaseData.class);
    }

    public static void publishSecondHand(String name, double price, String contact, int status, int type,
                                         String introduce, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "name", name);
        params.put("price", String.valueOf(price));
        putIfNotBlank(params, "contact", contact);
        params.put("status", String.valueOf(status));
        params.put("type", String.valueOf(type));
        putIfNotBlank(params, "introduce", introduce);
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.SECOND_HAND_PUBLISH, params,
                buildWusterHeaders(), listener, BaseData.class);
    }

    public static void getSecondHandAll(int page, int size, DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.SECOND_HAND_LIST_ALL + "/" + page + "/" + size, null,
                buildWusterHeaders(), listener, SecondHandPageData.class);
    }

    public static void getSecondHandByTypeOrStatus(int page, int size, Integer type, Integer status, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        if (type != null && type >= 0) {
            params.put("type", String.valueOf(type));
        }
        if (status != null && status >= 0) {
            params.put("status", String.valueOf(status));
        }
        RequestCenter.getWithoutToken(WustApi.SECOND_HAND_FILTER + "/" + page + "/" + size, params,
                buildWusterHeaders(), listener, SecondHandPageData.class);
    }

    public static void searchSecondHand(String text, int page, int size, Integer category, Integer status, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "txt", text);
        int requestCategory = category == null || category < 0 ? 0 : category;
        int requestStatus = status == null || status < 0 ? 0 : status;
        RequestCenter.getWithoutToken(WustApi.SECOND_HAND_SEARCH + "/" + requestCategory + "/" + requestStatus + "/" + page + "/" + size,
                params, buildWusterHeaders(), listener, SecondHandPageData.class);
    }

    public static void getSecondHandCollectionList(DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.SECOND_HAND_COLLECTION_LIST, null, buildWusterHeaders(), listener, SecondHandPageData.class);
    }

    public static void getSecondHandMyPublish(int page, int size, DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.SECOND_HAND_MY_PUBLISH + "/" + page + "/" + size, null,
                buildWusterHeaders(), listener, SecondHandPageData.class);
    }

    public static void deleteSecondHand(int pid, DisposeDataListener listener) {
        RequestCenter.getWithoutToken(WustApi.SECOND_HAND_DELETE_PREFIX + pid, null,
                buildWusterHeaders(), listener, BaseData.class);
    }

    public static void getCompetitionPosts(String studentId, int status, String competitionName, int page, int pageSize,
                                           DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "studentId", studentId);
        params.put("status", String.valueOf(status));
        putIfNotBlank(params, "competitionName", competitionName);
        params.put("page", String.valueOf(page));
        params.put("pageSize", String.valueOf(pageSize));
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.COMPETITION_POST_PAGE, params, buildWusterHeaders(), listener,
                CompetitionPostPageData.class);
    }

    public static void createCompetitionPost(String studentId, String competitionName, String competitionIntroduction,
                                             String requirement, String contactInformation, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        putIfNotBlank(params, "studentId", studentId);
        putIfNotBlank(params, "competitionName", competitionName);
        putIfNotBlank(params, "competitionIntroduction", competitionIntroduction);
        putIfNotBlank(params, "requirement", requirement);
        putIfNotBlank(params, "contactInformation", contactInformation);
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.COMPETITION_POST_CREATE, params, buildWusterHeaders(), listener,
                BaseData.class);
    }

    public static void deleteCompetitionPost(int cid, DisposeDataListener listener) {
        RequestCenter.deleteRequestWithoutLegacyToken(WustApi.COMPETITION_POST_PREFIX + cid, buildWusterHeaders(), listener,
                BaseData.class);
    }

    public static void getCompetitionResponses(int cid, int page, int pageSize, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("cid", String.valueOf(cid));
        params.put("page", String.valueOf(page));
        params.put("pageSize", String.valueOf(pageSize));
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.COMPETITION_RESPONSE_PAGE, params, buildWusterHeaders(), listener,
                CompetitionResponsePageData.class);
    }

    public static void createCompetitionResponse(int cid, String studentId, String response, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("cid", String.valueOf(cid));
        putIfNotBlank(params, "studentId", studentId);
        putIfNotBlank(params, "response", response);
        RequestCenter.postJsonRequestWithoutLegacyToken(WustApi.COMPETITION_RESPONSE_CREATE, params, buildWusterHeaders(), listener,
                BaseData.class);
    }

    private static RequestParams buildWusterHeaders() {
        RequestParams headers = new RequestParams();
        headers.put("Platform", "android");
        headers.put("Content-Type", "application/json");
        String token = getToken();
        if (token != null && !token.trim().isEmpty()) {
            headers.put("Authorization", "Wuster " + token.trim());
        }
        return headers;
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
