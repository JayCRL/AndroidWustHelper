package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.LitePalSupport;

public class GradeBean extends LitePalSupport {

    /**
     * courseNum            课程编号                          # courseNum
     * courseName:          课程名称(xxx)                     # courseName
     * grade:               成绩(87或 B)                      # grade
     * courseCredit:        学分(3.0)                        # courseCredit
     * courseHours:         学时(56)                         # courseHours
     * gradePoint:          绩点(3.7)                        # gradePoint
     * evaluationMode:      课程类别名称(必修)                 # evaluationMode
     * examNature:          课时性质名称(正常考试)              # examNature
     * courseNature:        课程性质 (实践教学模块)             # courseNature
     * schoolTerm:          开课学期(2018-2019-1)             # schoolTerm
     * reExam:              补考标识 0--正常考试 1--补考一
     * rebuildTag:          重修标识 0--正常  1--重修
     * missingTag:          缺考缓考的标志 0--代表正常 1--代表缺考 2--缓考
     *
     * e.g.
     * */

    private String studentId;

    @SerializedName(value = "courseNum", alternate = {"id", "courseId"})
    private String courseNum;

    @SerializedName(value = "courseName", alternate = {"name", "className"})
    private String courseName;

    //分数100或者A
    @SerializedName(value = "grade", alternate = {"score", "result", "finalScore"})
    private String grade;

    @SerializedName(value = "courseCredit", alternate = {"credit"})
    private String courseCredit;

    @SerializedName(value = "courseHours", alternate = {"hours"})
    private String courseHours;

    //绩点3.7
    @SerializedName(value = "gradePoint", alternate = {"point", "gpa"})
    private String gradePoint;

    @SerializedName(value = "evaluationMode", alternate = {"type", "courseType"})
    private String evaluationMode;

    @SerializedName(value = "examNature", alternate = {"examType"})
    private String examNature;

    @SerializedName(value = "courseNature", alternate = {"nature"})
    private String courseNature;

    @SerializedName(value = "schoolTerm", alternate = {"term", "semester"})
    private String schoolTerm;

    @SerializedName("reExamTag")
    private int reExam;                //补考标识 0--正常考试 1--补考一
    @SerializedName("rebuildTag")
    private int rebuildTag;//新增数据： //重修标识 0--未重修  1--重修
    @SerializedName("missExamTag")
    private int missingTag;//新增数据： //缺考缓考的标志 0--代表正常 1--代表缺考 2--缓考

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseNum() {
        return courseNum;
    }

    public void setCourseNum(String courseNum) {
        this.courseNum = courseNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getGrade() {
        if (grade == null || grade.equals("")) {
            return "0";
        } else {
            return grade;
        }

    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCourseCredit() {
        if (courseCredit == null || courseCredit.equals("")) {
            return "0";
        } else {
            return courseCredit;
        }
    }

    public void setCourseCredit(String courseCredit) {
        this.courseCredit = courseCredit;
    }

    public String getCourseHours() {
        return courseHours;
    }

    public void setCourseHours(String courseHours) {
        this.courseHours = courseHours;
    }

    public String getGradePoint() {
        if (gradePoint == null || gradePoint.equals("")) {
            return "0";
        } else {
            return gradePoint;
        }
    }

    public void setGradePoint(String gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getEvaluationMode() {
        return evaluationMode;
    }

    public void setEvaluationMode(String evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    public String getExamNature() {
        return examNature;
    }

    public void setExamNature(String examNature) {
        this.examNature = examNature;
    }

    public String getCourseNature() {
        return courseNature;
    }

    public void setCourseNature(String courseNature) {
        this.courseNature = courseNature;
    }

    public String getSchoolTerm() {
        return schoolTerm;
    }

    public void setSchoolTerm(String schoolTerm) {
        this.schoolTerm = schoolTerm;
    }

    public int getReExam() {
        return reExam;
    }

    public void setReExam(int reExam) {
        this.reExam = reExam;
    }

    public int getRebuildTag() {
        return rebuildTag;
    }

    public void setRebuildTag(int rebuildTag) {
        this.rebuildTag = rebuildTag;
    }

    public int getMissingTag() {
        return missingTag;
    }

    public void setMissingTag(int missingTag) {
        this.missingTag = missingTag;
    }

    @Override
    public String toString() {
        return "GradeBean{" +
                "studentId='" + studentId + '\'' +
                ", courseNum='" + courseNum + '\'' +
                ", courseName='" + courseName + '\'' +
                ", grade='" + grade + '\'' +
                ", courseCredit=" + courseCredit +
                ", courseHours=" + courseHours +
                ", gradePoint=" + gradePoint +
                ", evaluationMode='" + evaluationMode + '\'' +
                ", examNature='" + examNature + '\'' +
                ", courseNature='" + courseNature + '\'' +
                ", schoolTerm='" + schoolTerm + '\'' +
                ", reExam=" + reExam +
                ", rebuildTag=" + rebuildTag +
                ", missingTag=" + missingTag +
                '}';
    }
}
