package com.example.wusthelper.bean.javabean;

import org.litepal.crud.LitePalSupport;

/**
 * name:                课程名称(xxx)                  #name
 * credit               课程学分                       #credit
 * point:               成绩(87)                      #point
 * term:                选修学期                       #courseCredit
 *
 * e.g.
 * */
public class GraduateGradeBean extends LitePalSupport {
    private String name;
    private String credit;
    private String point;
    private String term;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return "GraduateGradeBean{" +
                "studentId='" + name + '\'' +
                ", credit='" + credit + '\'' +
                ", point='" + point + '\'' +
                ", grade='" + term + '\'' +
                '}';
    }
}
