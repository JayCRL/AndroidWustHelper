package com.example.wusthelper.bean.javabean.data;


import com.google.gson.annotations.SerializedName;

/**
 * 用于解研究生信息获取接口
 * */
public class GraduateData extends BaseData {
    @SerializedName("data")
    public Content data;
    public class Content {
        private String id;
        private String studentNum;
        private String password;
        private String name;
        private String degree;
        private String tutorName;
        private String academy;
        private String specialty;
        private String grade;
        private String avatar;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStudentNum() {
            return studentNum;
        }

        public void setStudentNum(String studentNum) {
            this.studentNum = studentNum;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getTutorName() {
            return tutorName;
        }

        public void setTutorName(String tutorName) {
            this.tutorName = tutorName;
        }

        public String getAcademy() {
            return academy;
        }

        public void setAcademy(String academy) {
            this.academy = academy;
        }

        public String getSpecialty() {
            return specialty;
        }

        public void setSpecialty(String specialty) {
            this.specialty = specialty;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "id='" + id + '\'' +
                    ", studentNum='" + studentNum + '\'' +
                    ", password='" + password + '\'' +
                    ", name='" + name + '\'' +
                    ", degree='" + degree + '\'' +
                    ", tutorName='" + tutorName + '\'' +
                    ", academy='" + academy + '\'' +
                    ", specialty='" + specialty + '\'' +
                    ", grade='" + grade + '\'' +
                    ", avatar='" + avatar + '\'' +
                    '}';
        }
    }
}
