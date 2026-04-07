package com.example.wusthelper.bean.javabean;

public class WidgetCourseBean {

    private String className;

    private String classRoom;

    private String teacher;

    private boolean isInClassTime;

    private int color;

    private int num;

    public WidgetCourseBean() {
        num = 0;
    }

    public WidgetCourseBean(String className, String classRoom, String teacher, boolean isInClassTime, int color, int num) {
        this.className = className;
        this.classRoom = classRoom;
        this.teacher = teacher;
        this.isInClassTime = isInClassTime;
        this.color = color;
        this.num = num;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public boolean isInClassTime() {
        return isInClassTime;
    }

    public void setInClassTime(boolean inClassTime) {
        isInClassTime = inClassTime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "WidgetCourseBean{" +
                "className='" + className + '\'' +
                ", classRoom='" + classRoom + '\'' +
                ", teacher='" + teacher + '\'' +
                ", isInClassTime=" + isInClassTime +
                ", color=" + color +
                ", num=" + num +
                '}';
    }

    public void setCourseData(CourseBean courseBean) {
        this.className = courseBean.getCourseName();
        classRoom = courseBean.getClassRoom();
        teacher = courseBean.getTeacherName();
        color = courseBean.getColor();
        num = 1;
    }
}
