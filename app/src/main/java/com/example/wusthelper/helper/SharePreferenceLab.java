package com.example.wusthelper.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.wusthelper.bean.javabean.DateBean;

public class SharePreferenceLab {

    private static volatile SharePreferenceLab sharePreferenceLab;
    private SharedPreferences sharedPreferences;
    private static final String IS_LOGIN = "isLogin";
    private static final String STUDENT_ID = "studentId";
    private static final String PASSWORD = "password";
    private static final String NAME = "Course";
    private static final String DATE = "date";
    private static final String WEEK = "week";
    private static final String WEEKDAY = "weekday";
    private static final String CREDITS = "credits";
    private static final String CREDITS_LAST_TIME = "creditsLastTime";
    private static final String IS_SHOW_NOT_THIS_WEEK = "isShowNotThisWeek";
    private static final String IS_SHOW_BACKGROUND = "isShowBackground";
    private static final String IS_BACKGROUND_FULL_SCREEN = "isBackgroundFullscreen";
    private static final String IS_Set_Italic = "isSetItalic";//斜体显示
    private static final String BACKGROUND_PATH = "backgroundPath";
    private static final String BACKGROUND_PATH_BLUR = "backgroundPathBlur";//背景是否模糊
    private static final String HEAD_PATH = "headPath";
    private static final String USER_NAME = "userName";
    private static final String MAJOR = "major";
    private static final String COLLEGE = "college";
    private static final String SEMESTER = "semester";
    private static final String REAL_SEMESTER = "realSemester";
    private static final String TERM_START_DATE = "termStartDate";
    private static final String REAL_NAME = "realName";
    private static final String IS_LIBRARY_LOGIN = "isLibraryLogin";
    private static final String LIBRARY_PASSWORD = "libraryPassword";
    private static final String SELECT_STUDENT_ID = "selectStudentId";
    private static final String SELECT_SEMESTER = "selectSemester";
    private static final String IS_AT_BOTTOM = "isAtBottom";
    private static final String IS_GRADUATE = "isGraduate";
    private static final String IS_GET_QR = "isGetQr";

    private static final String TOKEN = "token";
    private static final String LOGIN_MSG = "login_msg";

    private static final String TOKEN_UPDATE_TIME = "token_update_time";
    private static final String IS_CONNECT_TO_WEIXIN = "isConnectToWeixin";

    private static final String IS_REFRESH = "isrefresh_2";
    private static final String NOTICE_VERSION = "notice_version";

    private static final String SCHOLARSHIP_SEMESTER_SELECTED = "scholarship_semester_selected";

    private static final String IS_FIRST_SHOW = "is_first_show";
    private static final String CAMPUS = "Campus";

    private static final String BACKGROUND_BLUR_RADIUS = "background_blur_radius";
    private static final String IS_UPDATE_first_TIME = "is_update_first_time";
    private static final String IS_PERMISSIONDIALOG = "is_permissionDialog";//APP初始时是否同意权限
    private static final String IS_CONFIRM_POLICY = "is_confirm_policy";


    private static final String WIDGET_TODAY_BG_ALPHA = "widget_today_bg_alpha";
    private static final String WIDGET_WEEK_BG_ALPHA = "widget_week_bg_alpha";
    private static final String WIDGET_COUNTDOWN_BG_ALPHA = "widget_countdown_alpha";


    private static final String WIDGET_TODAY_BG_PATH = "widget_today_bg_Path";
    private static final String WIDGET_WEEK_BG_PATH = "widget_week_bg_Path";
    private static final String WIDGET_COUNTDOWN_BG_PATH = "widget_countdown_bg_Path";

    private static final String WIDGET_TODAY_TEXT_COLOR = "widget_today_text_color";
    private static final String WIDGET_WEEK_TEXT_COLOR = "widget_week_text_color";
    private static final String WIDGET_COUNTDOWN_TEXT_COLOR = "widget_countdown_text_color";

    private static final String WIDGET_TODAY_BG_REFRESH_TYPE = "widget_today_bg_refresh_type";
    private static final String WIDGET_WEEK_BG_REFRESH_TYPE = "widget_week_bg_refresh_type";
    private static final String WIDGET_COUNTDOWN_BG_REFRESH_TYPE = "widget_countdown_bg_refresh_type";

    private static final String WIDGET_COUNTDOWN_WIDTH = "widget_countdown_width";
    private static final String WIDGET_COUNTDOWN_HEIGHT = "widget_countdown_height";

    private static final String WIDGET_TODAY_WIDTH = "widget_today_width";
    private static final String WIDGET_TODAY_HEIGHT = "widget_today_height";

    private static final String WIDGET_WEEK_WIDTH = "widget_week_width";
    private static final String WIDGET_WEEK_HEIGHT = "widget_week_height";

//    private static final String WIDGET_COUNTDOWN_BG_REFRESH_TYPE = "widget_countdown_bg_refresh_type";

    private static final String HOMEPAGE_SETTINGS = "homepage_settings";//首页设置
    private static final String UPDATE_IGNORE_VERSION="update_ignore_version";
    private static final String IS_INIT_MOBPUSH="is_initMObPush";//是否加载了mobpush(注册别名)
    private static final String IS_PHYSICAL_LOGIN="is_physical_login";//物理实验是否登录
    private static final String IS_PHYSICAL_SHOW="is_physical_show";//物理实验是否显示
    private static final String IS_CHOOSE_SUNDAY_FIRST="is_choose_sunday_first";//设置周日为第一天
    private static final String IS_OPEN_BY_MOBLINK="is_open_by_mobLink";//设置周日为第一天
    private static final String IS_REQUEST_COURSE="is_request_course";//设置周日为第一天
    private static final String IS_CHOOSE_WEEK="is_choose_week";//设置导入一周课程
    private static final String IS_CHOOSE_ALL_WEEK="is_choose_all_week";//设置导入所有课程
    private static final String IS_IMPORTED = "is_imported";//是否导入过日程

    private static final String BACKGROUND_ALPHA = "background_alpha";//课程表的白色背景透明度默认为0
    private static final String Font_Size = "font_size";//课程表的字体大小默认为11dp

    private static final String BAR_STATE = "bar_state";
    private static final String BAR_HIDE = "bar_hide";

    public static final int BARSTATEDEFAULT=0x1;
    public static  final int BARSTATEDELETEVOL=0x2;
    public static  final int BARSTATEDELETECON=0x3;
    public static  final int BARSTATEDELETETOW=0x4;
    public static  final int BARSTATEDELETEALL=0x5;

    public static final int HUANGJIAHU=0x1;
    public static  final int QINGSHAN=0x2;

    public static final String IS_VACATIONING = "is_vacationing";

    public static final String INFO_DEGREE = "info_degree";
    public static final String INFO_TUTOR_NAME = "info_tutor_name";
    public static final String INFO_ACADEMY = "info_academy";
    public static final String INFO_GRADE = "info_grade";

    public static final String REQUEST_TIME = "request_time"; //成绩请求时间
    public static final String REQUEST_SCHEME_TIME = "request_scheme_time"; //培养方案请求时间
    public static final String SCHEME_HTML = "SCHEME_HTML"; //缓存培养方案
//    private static final String VERSION = "version";

    //默认的测试数据。没有意义
    public static String defaultValue = "defaultValue";//默认数据添加 开始的一次
    /**
     * 获取数据
     */
    public static String getValue() {
        return SPTool.get(defaultValue, "");
    }
    /**
     * 添加数据
     */
    public static void setValue(String value) {
        SPTool.put(defaultValue, value);
    }
    /**
     * 清除某一个数据
     */
    public static void removeValue() {
        SPTool.remove(defaultValue);
    }

    /**
     * 获取登录状态
     */
    public static boolean getIsLogin() {
        return SPTool.get(IS_LOGIN, false);
    }
    public static void setIsLogin(boolean value) {
        SPTool.put(IS_LOGIN, value);
    }

    /**
     * 获取图书馆登录状态
     */
    public static boolean getIsLibraryLogin() {
        return SPTool.get(IS_LIBRARY_LOGIN, false);
    }
    public static void setIsLibraryLogin(boolean value) {
        SPTool.put(IS_LIBRARY_LOGIN, value);
    }

    /**
     * 获取Token
     */
    public static String getToken() {
        return SPTool.get(TOKEN, "");
    }
    public static void setToken(String value) {
        SPTool.put(TOKEN, value);
    }


    /**
     * 获取学号
     */
    public static String getStudentId() {
        return SPTool.get(STUDENT_ID, "");
    }
    public static void setStudentId(String value) {
        SPTool.put(STUDENT_ID, value);
    }

    /**
     * 获取教务处密码
     */
    public static String getPassword() {
        return SPTool.get(PASSWORD, "");
    }

    /**
     * 设置教务处密码（用于 WebView 自动登录等场景）。
     */
    public static void setPassword(String value) {
        SPTool.put(PASSWORD, value == null ? "" : value);
    }

    /**
     * 获取当前选择的学期
     */
    public static String getSelectSemester() {
        return SPTool.get(SELECT_SEMESTER, "");
    }
    public static void setSelectSemester(String value) {
        SPTool.put(SELECT_SEMESTER, value);
    }

    /**
     * 获取真实的学期
     */
    public static String getSemester() {
        return SPTool.get(SEMESTER, "");
    }
    public static void setSemester(String value) {
        SPTool.put(SEMESTER, value);
    }

    /**
     * 是否为研究生，默认为false
     */
    public static boolean getIsGraduate() {
        return SPTool.get(IS_GRADUATE, false);
    }
    public static void setIsGraduate(Boolean value) {
        SPTool.put(IS_GRADUATE, value);
    }

    /**
     * 是否显示情侣课表，默认为false
     */
    public static boolean getIsGetQr() {
        return SPTool.get(IS_GET_QR, false);
    }
    public static void setIsGetQr(Boolean value) {
        SPTool.put(IS_GET_QR, value);
    }

    /**
     * 获取DateBean用于课程表的日期 等等一些列的计算
     * （后续是一些列的相关的函数）
     * */
    public static DateBean getDateBean() {

        String date = getDate();
        int week = getWeek();
        int weekday = getWeekday();
        return new DateBean(date, week, weekday);
    }
    /**
     * 接下来的六个函数基本上是同时调用的
     * */
    public static String getDate() {
        return SPTool.get(DATE, "");
    }

    public static int getWeek() {
        return SPTool.get(WEEK, 0);
    }

    public static int getWeekday() {
        return SPTool.get(WEEKDAY, 0);
    }

    public static void setDate(String value) {
        SPTool.put(DATE, value);
    }
    public static void setWeek(int value) {
        SPTool.put(WEEK, value);
    }

    public static void setWeekday(int value) {
        SPTool.put(WEEKDAY, value);
    }



    /**
     * 获取全屏设置，默认设置否
     */
    public static boolean getIsBackgroundFullScreen() {
        return SPTool.get(IS_BACKGROUND_FULL_SCREEN, false);
    }
    public static void setIsBackgroundFullScreen(boolean value) {
        SPTool.put(IS_BACKGROUND_FULL_SCREEN, value);
    }

    /**
     * 获取课程表白色蒙层的透明度，默认设置 0
     */
    public static int getBackgroundAlpha() {
        return SPTool.get(BACKGROUND_ALPHA, 0);
    }
    public static void setBackgroundAlpha(int value) {
        SPTool.put(BACKGROUND_ALPHA, value);
    }

    /**
     * 获取课程表字体大小，默认设置 11dp
     */
    public static int getFontSize() {
        return SPTool.get(Font_Size, 11);
    }
    public static void setFontSize(int value) {
        SPTool.put(Font_Size, value);
    }

    /**
     * 获取斜体设置，默认设置否
     */
    public static boolean getIsItalic() {
        return SPTool.get(IS_Set_Italic, false);
    }
    public static void setIsItalic(boolean value) {
        SPTool.put(IS_Set_Italic, value);
    }

    /**
     * 设置校区，默认设置为黄家湖（1为黄家湖，2为青山校区）
     */
    public static int getCampus() {
        return SPTool.get(CAMPUS, HUANGJIAHU);
    }
    public static void setCampus(int value) {
        SPTool.put(CAMPUS, value);
    }

    /**
     * 设置"主页"为MainActivity初始显示的页面，默认设置为false（true显示为课程表，false为主页）
     */
    public static boolean getHomepageSettings() {
        return SPTool.get(HOMEPAGE_SETTINGS, true);
    }
    public static void setHomepageSettings(boolean value) {
        SPTool.put(HOMEPAGE_SETTINGS, value);
    }

    /**
     * 设置底部导航栏显示 默认设置为true（true 表示显示）
     */
    public static int getBarState() {
        return SPTool.get(BAR_STATE, BARSTATEDEFAULT);
    }
    public static void setBarState(int value) {
        SPTool.put(BAR_STATE, value);
    }

    /**
     * 设置课程表 周日为每周第一天 默认设置为false（false表示周一为每周第一天）
     */
    public static boolean getIsChooseSundayFirst() {
        return SPTool.get(IS_CHOOSE_SUNDAY_FIRST, false);
    }
    public static void setIsChooseSundayFirst(boolean value) {
        SPTool.put(IS_CHOOSE_SUNDAY_FIRST, value);
    }

    /**
     * 设置隐藏导航栏 默认设置为false
     */
    public static boolean getBarHide() {
        return SPTool.get(BAR_HIDE, false);
    }
    public static void setBarHide(boolean value) {
        SPTool.put(BAR_HIDE, value);
    }

    /**
     * 设置导入日程 导入当前周课程 默认设置为false
     */
    public static boolean getIsWeekCourseCalendar() {
        return SPTool.get(IS_CHOOSE_WEEK, false);
    }
    public static void setIsWeekCourseCalendar(boolean value) {
        SPTool.put(IS_CHOOSE_WEEK, value);
    }

    /**
     * 设置导入日程 导入所有课程 默认设置为false
     */
    public static boolean getIsAllCourseCalendar() {
        return SPTool.get(IS_CHOOSE_ALL_WEEK, false);
    }
    public static void setIsAllCourseCalendar(boolean value) {
        SPTool.put(IS_CHOOSE_ALL_WEEK, value);
    }

    /**
     * 是否导入过日程 默认设置为false
     */
    public static boolean getIsImported() {
        return SPTool.get(IS_IMPORTED, false);
    }
    public static void setIsImported(boolean value) {
        SPTool.put(IS_IMPORTED, value);
    }

    /**
     * 设置是否显示全部课程，默认设置为true
     */
    public static boolean getIsShowNotThisWeek() {
        return SPTool.get(IS_SHOW_NOT_THIS_WEEK, true);
    }
    public static void setIsShowNotThisWeek(boolean value) {
        SPTool.put(IS_SHOW_NOT_THIS_WEEK, value);
    }

    /**
     * 设置课程表 周日为每周第一天 默认设置为false（false表示周一为每周第一天）
     */
    public static String getUserName() {
        return SPTool.get(USER_NAME, "木有设置");
    }
    public static void setUserName(String value) {
        SPTool.put(USER_NAME, value);
    }

    /**
     * basic 后端返回的学期起始日（yyyy-MM-dd）。
     */
    public static String getTermStartDate() {
        return SPTool.get(TERM_START_DATE, "");
    }

    public static void setTermStartDate(String value) {
        SPTool.put(TERM_START_DATE, value == null ? "" : value);
    }

    /**
     * 兼容：写入 year/month/day（month/day 会补零）。
     */
    public static void setTermStartDate(int year, int month, int day) {
        String mm = month < 10 ? ("0" + month) : String.valueOf(month);
        String dd = day < 10 ? ("0" + day) : String.valueOf(day);
        setTermStartDate(year + "-" + mm + "-" + dd);
    }

    /**
     * 获取接口返回的真实姓名（用于默认展示，不参与用户自定义昵称的覆盖）。
     */
    public static String getRealName() {
        return SPTool.get(REAL_NAME, "");
    }

    public static void setRealName(String value) {
        SPTool.put(REAL_NAME, value == null ? "" : value);
    }

    /**
     * Mine / 设置页的显示名：优先昵称(USER_NAME)，为空或默认值则回退真实姓名。
     */
    public static String getDisplayName() {
        String nick = getUserName();
        if (nick == null) {
            nick = "";
        }
        String trimmed = nick.trim();
        if (trimmed.isEmpty() || "木有设置".equals(trimmed)) {
            String real = getRealName();
            return real == null || real.trim().isEmpty() ? "木有设置" : real;
        }
        return nick;
    }

    /**
     * 设置课程表 周日为每周第一天 默认设置为false（false表示周一为每周第一天）
     */
    public static String getHeadPath() {
        return SPTool.get(HEAD_PATH, "");
    }
    public static void setHeadPath(String value) {
        SPTool.put(HEAD_PATH, value);
    }


    /**
     * 设置倒计时小组件宽度 默认宽度304(不同手机不一样，这里是以我的手机设置的，但是不用担心，在小组件的生命周期，会重新设置这个高度)
     */
    public static int getWidgetCountdownWidth() {
        return SPTool.get(WIDGET_COUNTDOWN_WIDTH, 304);
    }
    public static void setWidgetCountdownWidth(int value) {
        SPTool.put(WIDGET_COUNTDOWN_WIDTH, value);
    }

    /**
     * 设置倒计时小组件宽度 默认宽度184
     */
    public static int getWidgetCountdownHeight() {
        return SPTool.get(WIDGET_COUNTDOWN_HEIGHT, 184);
    }
    public static void setWidgetCountdownHeight(int value) {
        SPTool.put(WIDGET_COUNTDOWN_HEIGHT, value);
    }

    /**
     * 设置每日课表小组件宽度 默认宽度304(不同手机不一样，这里是以我的手机设置的，但是不用担心，在小组件的生命周期，会重新设置这个高度)
     */
    public static int getWidgetTodayWidth() {
        return SPTool.get(WIDGET_TODAY_WIDTH, 304);
    }
    public static void setWidgetTodayWidth(int value) {
        SPTool.put(WIDGET_TODAY_WIDTH, value);
    }

    /**
     * 设置每日课表小组件宽度 默认宽度184
     */
    public static int getWidgetTodayHeight() {
        return SPTool.get(WIDGET_TODAY_HEIGHT, 184);
    }
    public static void setWidgetTodayHeight(int value) {
        SPTool.put(WIDGET_TODAY_HEIGHT, value);
    }

    /**
     * 设置每日课表小组件宽度 默认宽度304(不同手机不一样，这里是以我的手机设置的，但是不用担心，在小组件的生命周期，会重新设置这个高度)
     */
    public static int getWidgetWeekWidth() {
        return SPTool.get(WIDGET_WEEK_WIDTH, 304);
    }
    public static void setWidgetWeekWidth(int value) {
        SPTool.put(WIDGET_WEEK_WIDTH, value);
    }

    /**
     * 设置每日课表小组件宽度 默认宽度184
     */
    public static int getWidgetWeekHeight() {
        return SPTool.get(WIDGET_WEEK_HEIGHT, 384);
    }
    public static void setWidgetWeekHeight(int value) {
        SPTool.put(WIDGET_WEEK_HEIGHT, value);
    }

    /**
     * 缓存当前请求成绩的时间
     */
    public static long getRequestTime() {
        return SPTool.get(REQUEST_TIME, 0L);
    }
    public static void setRequestTime(long value) {
        SPTool.put(REQUEST_TIME, value);
    }

    /**
     * 缓存当前请求培养方案的时间
     */
    public static long getSchemeRequestTime() {
        return SPTool.get(REQUEST_SCHEME_TIME, 0L);
    }
    public static void setSchemeRequestTime(long value) {
        SPTool.put(REQUEST_SCHEME_TIME, value);
    }

    /**
     * 缓存培养方案
     */
    public static String getSchemeHtml() {
        return SPTool.get(SCHEME_HTML, "");
    }
    public static void setSchemeHtml(String value) {
        SPTool.put(SCHEME_HTML, value);
    }

    /**
     *  从3.4.2版本到后来的更新 过渡用
     *  获取是否请求过课程表（如果没有请求过，且课程表为空，提示请求课程表）
     */
    public static boolean getIsRequestCourse() {
        return SPTool.get(IS_REQUEST_COURSE, false);
    }
    public static void setIsRequestCourse(boolean value) {
        SPTool.put(IS_REQUEST_COURSE, value);
    }


    public static String getDegree() {return SPTool.get(INFO_DEGREE,"木有设置");}
    public static void setDegree(String value) {SPTool.put(INFO_DEGREE,value);}

    public static String getTutorName() {return SPTool.get(INFO_TUTOR_NAME,"木有设置");}
    public static void setTutorName(String value) {SPTool.put(INFO_TUTOR_NAME,value);}

    public static String getGrade() {return SPTool.get(INFO_GRADE,"木有设置");}
    public static void setGrade(String value) {SPTool.put(INFO_GRADE,value);}

    public static void logout() {

        SPTool.put(TOKEN, "");
        SPTool.put(IS_LOGIN, false);
        SPTool.put(STUDENT_ID, "");

        SPTool.put(PASSWORD, "");
        SPTool.put(NAME, "");
        SPTool.put(DATE, "");

        SPTool.put(IS_VACATIONING, 0);
        SPTool.put(WEEK, 0);
        SPTool.put(WEEKDAY, 0);

        SPTool.put(CREDITS, "");
        SPTool.put(CREDITS_LAST_TIME, "");
        SPTool.put(IS_SHOW_NOT_THIS_WEEK, true);

        SPTool.put(IS_SHOW_BACKGROUND, false);
        SPTool.put(IS_BACKGROUND_FULL_SCREEN, false);
        SPTool.put(BACKGROUND_PATH, "");

        SPTool.put(HEAD_PATH, "");
        SPTool.put(USER_NAME, "木有设置");
        SPTool.put(MAJOR, "木有设置");

        SPTool.put(SEMESTER, "");
        SPTool.put(IS_LIBRARY_LOGIN, false);
        SPTool.put(LIBRARY_PASSWORD, "");

        SPTool.put(SELECT_SEMESTER, "");
        SPTool.put(SELECT_STUDENT_ID, "");
        SPTool.put(IS_CONNECT_TO_WEIXIN,false);

        SPTool.put(SCHOLARSHIP_SEMESTER_SELECTED, "大一");
        SPTool.put(IS_FIRST_SHOW, true);
        SPTool.put(IS_UPDATE_first_TIME,true);

        SPTool.put(UPDATE_IGNORE_VERSION, "");
        SPTool.put(IS_PHYSICAL_LOGIN, false);
        SPTool.put(IS_PHYSICAL_SHOW,false);

        SPTool.put(IS_INIT_MOBPUSH, false);
        SPTool.put(IS_CHOOSE_SUNDAY_FIRST, false);

        SPTool.put(BAR_HIDE, false);
        SPTool.put(IS_CHOOSE_WEEK, false);
        SPTool.put(IS_CHOOSE_ALL_WEEK, false);
        SPTool.put(IS_IMPORTED, false);
        SPTool.put(BAR_HIDE, false);
        SPTool.put(IS_GRADUATE, false);
        SPTool.put(Font_Size, 11);
        SPTool.put(IS_Set_Italic, false);
        SPTool.put(BAR_STATE, BARSTATEDEFAULT);

        SPTool.put(REQUEST_SCHEME_TIME,0L);
        SPTool.put(SCHEME_HTML,"");
        SPTool.put(IS_GET_QR,false);
    }



    public static SharePreferenceLab getInstance() {
        if (sharePreferenceLab == null) {
            synchronized (SharePreferenceLab.class) {
                if (sharePreferenceLab == null) {
                    sharePreferenceLab = new SharePreferenceLab();
                }
            }
        }
        return sharePreferenceLab;
    }

    private void createSharePreference(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        }
    }

    public void setIsAtBottom(Context context, boolean isAtBottom) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_AT_BOTTOM, isAtBottom);
        editor.apply();
    }

    public boolean isAtBottom(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_AT_BOTTOM, false);

    }


    public void setIsConnectToWeixin(Context context, boolean isConnectToWeixin) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_CONNECT_TO_WEIXIN, isConnectToWeixin);
        editor.apply();

    }

    public boolean getIsConnectToWeixin(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_CONNECT_TO_WEIXIN, false);

    }

    public void setLibraryPassword(Context context, String libraryPassword) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LIBRARY_PASSWORD, libraryPassword);
        editor.apply();

    }

    public String getLibraryPassword(Context context) {

        createSharePreference(context);
        return sharedPreferences.getString(LIBRARY_PASSWORD, "");

    }

    public void setIsLibraryLogin(Context context, boolean isLibraryLogin) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LIBRARY_LOGIN, isLibraryLogin);
        editor.apply();

    }

    public boolean getIsLibraryLogin(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_LIBRARY_LOGIN, false);

    }

    public void setHeadPath(Context context, String headPath) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HEAD_PATH, headPath);
        editor.apply();

    }

    public String getHeadPath(Context context) {

        createSharePreference(context);
        return sharedPreferences.getString(HEAD_PATH, "");

    }

    public void setUsername(Context context, String userName) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME, userName);
        editor.apply();

    }

    public String getUserName(Context context) {

        createSharePreference(context);
        return sharedPreferences.getString(USER_NAME, "木有设置");

    }

    public void setMajor(Context context, String major) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MAJOR, major);
        editor.apply();
    }

    public String getMajor(Context context) {

        createSharePreference(context);
        return sharedPreferences.getString(MAJOR, "木有设置");
    }

    public void setCollege(Context context, String college) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(COLLEGE, college);
        editor.apply();
    }

    public String getCollege(Context context) {

        createSharePreference(context);
        return sharedPreferences.getString(COLLEGE, "木有设置");
    }


    public void setData(Context context, boolean isLogin, String studentId, String date, int week, int weekday, String password, String semester, boolean isConnectToWeixin) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DATE, date);
        editor.putBoolean(IS_LOGIN, isLogin);
        editor.putString(STUDENT_ID, studentId);
        editor.putInt(WEEK, week);
        editor.putInt(WEEKDAY, weekday);
        editor.putString(PASSWORD, password);
        editor.putString(SEMESTER, semester);
        editor.putBoolean(IS_CONNECT_TO_WEIXIN, isConnectToWeixin);
        editor.apply();
    }



    public void setIsShowBackground(Context context, boolean isShowBackground) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_SHOW_BACKGROUND, isShowBackground);
        editor.apply();
    }

    public boolean getIsShowBackground(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_SHOW_BACKGROUND, false);
    }
    public void setIsBackgroundFullscreen(Context context, boolean isBackgroundFullscreen) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_BACKGROUND_FULL_SCREEN, isBackgroundFullscreen);
        editor.apply();
    }

    public boolean getIsBackgroundFullscreen(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_BACKGROUND_FULL_SCREEN, false);
    }

    public void setBackgroundPath(Context context, String backgroundPath) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BACKGROUND_PATH, backgroundPath);
        editor.apply();
    }

    public String getBackgroundPath(Context context) {

        createSharePreference(context);
        String path  = sharedPreferences.getString(BACKGROUND_PATH,"");
        return path;
    }

    public void setBackgroundPathBlur(Context context,boolean isBlur){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BACKGROUND_PATH_BLUR, isBlur);
        editor.apply();
    }

    public boolean getBackgroundPathBlur(Context context){
        createSharePreference(context);
        return sharedPreferences.getBoolean(BACKGROUND_PATH_BLUR,false);
    }

    public void setIsShowNotThisWeek(Context context, boolean isShowNotThisWeek) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_SHOW_NOT_THIS_WEEK, isShowNotThisWeek);
        editor.apply();
    }

    public boolean getIsShowNotThisWeek(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_SHOW_NOT_THIS_WEEK, true);
    }

    public boolean getIsLogin(Context context) {
        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }

    public String getStudentId(Context context) {
        createSharePreference(context);
        return sharedPreferences.getString(STUDENT_ID, "");
    }

    public void setStudentId(Context context,String studentId) {
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(STUDENT_ID, studentId);
        editor.apply();
    }


    public String getDate(Context context) {
        createSharePreference(context);
        return sharedPreferences.getString(DATE, "");
    }

    public int getWeek(Context context) {
        createSharePreference(context);
        return sharedPreferences.getInt(WEEK, 0);
    }

    public void setWeek(Context context, int week) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(WEEK, week);
        editor.apply();
    }

    public void setWeekday(Context context, int weekday) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(WEEKDAY, weekday);
        editor.apply();

    }

    public void setDate(Context context, String date) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DATE, date);
        editor.apply();
        Log.e("<----->", "setDate: "+date);
    }

    public int getWeekday(Context context) {
        createSharePreference(context);
        return sharedPreferences.getInt(WEEKDAY, 0);
    }

    public void setCreditsTime(Context context, long value) {
        createSharePreference(context);
        sharedPreferences.edit().putLong(CREDITS_LAST_TIME, value).apply();
    }

    public long getCreditsTime(Context context) {
        createSharePreference(context);
        return sharedPreferences.getLong(CREDITS_LAST_TIME, 10000L);
    }

    public void setCredits(Context context, String value) {
        createSharePreference(context);
        sharedPreferences.edit().putString(CREDITS, value).apply();
    }

    public String getCredits(Context context) {
        createSharePreference(context);
        return sharedPreferences.getString(CREDITS, "");
    }

    public DateBean getDateBean(Context context) {
        createSharePreference(context);

        String date = getDate(context);
//                sharedPreferences.getString(DATE, "");
        int week = getWeek(context);
//                sharedPreferences.getInt(WEEK, 0);
        int weekday = getWeekday(context);
//                sharedPreferences.getInt(WEEKDAY ,0);
        Log.e("<------->", "getDateBean: "+date+" "+week+" "+weekday);
        return new DateBean(date, week, weekday);
    }

    public String getPassword(Context context) {

        createSharePreference(context);
        return sharedPreferences.getString(PASSWORD, "");
    }


    //
    public void setToken(Context context, String token) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN, token);
//        editor.putLong(TOKEN_UPDATE_TIME,updatetime);
        editor.apply();
    }

    public String getToken(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(TOKEN,"");
    }

    public void setMessage(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOGIN_MSG, msg);
//        editor.putLong(TOKEN_UPDATE_TIME,updatetime);
        editor.apply();
    }

    public String  getMessage(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(LOGIN_MSG,"");
    }

    public String getScholarshipSemesterSelected(Context context)
    {
        createSharePreference(context);
        return sharedPreferences.getString(SCHOLARSHIP_SEMESTER_SELECTED,"大一");
    }

    public void setScholarshipSemesterSelected(Context context,String semester_selected)
    {
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SCHOLARSHIP_SEMESTER_SELECTED, semester_selected);
//        editor.putLong(TOKEN_UPDATE_TIME,updatetime);
        editor.apply();
    }
//
//    public boolean getIsFirstShow(Context context)
//    {
//        createSharePreference(context);
//        return sharedPreferences.getBoolean(IS_FIRST_SHOW,true);
//    }
//
//    public void setIsFirstShow(Context context,boolean isFirstShow)
//    {
//        createSharePreference(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(IS_FIRST_SHOW, isFirstShow);
//        editor.apply();
//    }
//
//    public int getNoticeVersion(Context context){
//        createSharePreference(context);
//        return sharedPreferences.getInt(NOTICE_VERSION,0);
//    }
//    public void setNoticeVersion(Context context,int notice_version){
//        createSharePreference(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(NOTICE_VERSION, notice_version);
//        editor.apply();
//    }
    public int getCampus(Context context){
        return sharedPreferences.getInt(CAMPUS,HUANGJIAHU);
    }
//
//    public void setCampus(Context context,int campus){
//        createSharePreference(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(CAMPUS, campus);
//        editor.apply();
//    }

//    public String getBackgroundBluRadius(Context context){
//        return sharedPreferences.getString(BACKGROUND_BLUR_RADIUS,"0");
//    }
//
//    public void setBackgroundBluRadius(Context context,String radius){
//        createSharePreference(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(BACKGROUND_BLUR_RADIUS, radius);
//        editor.apply();
//    }

//    public boolean get_is_update_first_time(Context context)
//    {
//        createSharePreference(context);
//        return sharedPreferences.getBoolean(IS_UPDATE_first_TIME,true);
//    }
//
//    public void set_is_update_first_time(Context context,boolean is_update_first_time)
//    {
//        createSharePreference(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(IS_UPDATE_first_TIME, is_update_first_time);
//        editor.apply();
//    }
//    public boolean get_is_permissionDialog(Context context)
//    {
//        createSharePreference(context);
//        return sharedPreferences.getBoolean(IS_PERMISSIONDIALOG,true);
//    }
//
//    public void set_is_permissionDialog(Context context,boolean is_permissionDialog)
//    {
//        createSharePreference(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(IS_PERMISSIONDIALOG, is_permissionDialog);
//        editor.apply();
//    }
    public boolean get_is_confirm_policy(Context context)
    {
        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_CONFIRM_POLICY,false);
    }

    public void set_is_confirm_policy(Context context,boolean is_confirm_policy)
    {
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_CONFIRM_POLICY, is_confirm_policy);
        editor.apply();
    }

    public void setWidgetTodayBgAlpha(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_TODAY_BG_ALPHA, msg);
        editor.apply();
    }

    public String  getWidgetTodayBgAlpha(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_TODAY_BG_ALPHA,"180");
    }

    public void setWidgetWeekBgAlpha(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_WEEK_BG_ALPHA, msg);
        editor.apply();
    }

    public String  getWidgetWeekBgAlpha(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_WEEK_BG_ALPHA,"180");
    }
    public void setWidgetCountdownBgAlpha(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_COUNTDOWN_BG_ALPHA, msg);
        editor.apply();
    }

    public String  getWidgetCountdownBgAlpha(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_COUNTDOWN_BG_ALPHA,"180");
    }

    public void setHomepage_settings(Context context,Boolean homepage){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HOMEPAGE_SETTINGS, homepage);
        editor.apply();
    }

    public boolean  getHomepage_settings(Context context){
        createSharePreference(context);
        return sharedPreferences.getBoolean(HOMEPAGE_SETTINGS,true);
    }

    public void setUpdate_ignore_version(Context context,String version){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UPDATE_IGNORE_VERSION, version);
        editor.apply();
    }

    public String  getUpdate_ignore_version(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(UPDATE_IGNORE_VERSION,"");
    }



    public void setWidgetTodayBgPath(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_TODAY_BG_PATH, msg);
        editor.apply();
    }

    public String  getWidgetTodayBgPath(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_TODAY_BG_PATH,"");
    }

    public void setWidgetWeekBgPath(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_WEEK_BG_PATH, msg);
        editor.apply();
    }

    public String  getWidgetWeekBgPath(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_WEEK_BG_PATH,"");
    }

    public void setWidgetCountdownBgPath(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_COUNTDOWN_BG_PATH, msg);
        editor.apply();
    }

    public String  getWidgetCountdownBgPath(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_COUNTDOWN_BG_PATH,"");
    }

    public void setWidgetTodayTextColor(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_TODAY_TEXT_COLOR, msg);
        editor.apply();
    }

    public String getWidgetTodayTextColor(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_TODAY_TEXT_COLOR,"-16777216");
    }

    public void setWidgetWeekTextColor(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_WEEK_TEXT_COLOR, msg);
        editor.apply();
    }

    public String  getWidgetWeekTextColor(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_WEEK_TEXT_COLOR,"-13487566");
    }

    public void setWidgetCountdownTextColor(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_COUNTDOWN_TEXT_COLOR, msg);
        editor.apply();
    }

    public String  getWidgetCountdownTextColor(Context context){
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_COUNTDOWN_TEXT_COLOR,"-13487566");
    }

    public void setWidgetTodayBgRefreshType(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_TODAY_BG_REFRESH_TYPE, msg);
        editor.apply();
    }
    public void setWidgetWeekBgRefreshType(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_WEEK_BG_REFRESH_TYPE, msg);
        editor.apply();
    }
    public void setWidgetCountdownBgRefreshType(Context context,String msg){
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WIDGET_COUNTDOWN_BG_REFRESH_TYPE, msg);
        editor.apply();
    }
    public String getWidgetTodayBgRefreshType(Context context) {
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_TODAY_BG_REFRESH_TYPE,"white");
    }

    public String getWidgetWeekBgRefreshType(Context context) {
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_WEEK_BG_REFRESH_TYPE,"white");
    }

    public String getWidgetCountdownBgRefreshType(Context context) {
        createSharePreference(context);
        return sharedPreferences.getString(WIDGET_COUNTDOWN_BG_REFRESH_TYPE,"white");
    }

    public void setIsInitMobPush(Context context, boolean isInitMobPush) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_INIT_MOBPUSH, isInitMobPush);
        editor.apply();

    }

    public boolean getIsInitMobPush(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_INIT_MOBPUSH, false);
    }


    public void setIsOpenByMobLink(Context context, boolean isInitMobPush) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_OPEN_BY_MOBLINK, isInitMobPush);
        editor.apply();

    }

    public boolean getIsOpenByMobLink(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_OPEN_BY_MOBLINK, false);
    }



    public void setIsPhysicalLogin(Context context, boolean isPhysicalLogin) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_PHYSICAL_LOGIN, isPhysicalLogin);
        editor.apply();

    }

    public boolean getIsPhysicalLogin(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_PHYSICAL_LOGIN, false);

    }
    public void setIsPhysicalShow(Context context, boolean isPhysicalShow) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_PHYSICAL_SHOW, isPhysicalShow);
        editor.apply();

    }

    public boolean getIsPhysicalShow(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_PHYSICAL_SHOW, true);

    }


    public void setIsChooseSundayFirst(Context context, boolean isChooseSunday) {

        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_CHOOSE_SUNDAY_FIRST, isChooseSunday);
        editor.apply();
    }

    public boolean getIsChooseSundayFirst(Context context) {

        createSharePreference(context);
        return sharedPreferences.getBoolean(IS_CHOOSE_SUNDAY_FIRST, false);
    }




    /*    int 值代表该学期
            0 ：上课中
            1 ：假期中
            2 ：已结束
     */

    public static final int CLASSING = 0;
    public static final int VACATIONING  = 1;
    public static final int OVER = 2;

    public int get_is_vacationing(Context context){
        createSharePreference(context);
        return sharedPreferences.getInt(IS_VACATIONING,0);
    }
    public void set_is_vacationing(Context context,int is_confirm_policy)
    {
        createSharePreference(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(IS_VACATIONING, is_confirm_policy);
        editor.apply();
    }
}

