package com.example.wusthelper.bean.javabean.data;

import com.google.gson.annotations.SerializedName;

/**
 * basic 后端 /UnderGraduateStudent/getData 或 /GraduatedController/getData 返回的学期起始日。
 * 对应后端 DTO: DataInformation { year, month, day }
 */
public class TermStartDateData extends BaseData {

    @SerializedName("data")
    public Content data;

    public static class Content {
        @SerializedName("year")
        private Integer year;
        @SerializedName("month")
        private Integer month;
        @SerializedName("day")
        private Integer day;

        public Integer getYear() {
            return year;
        }

        public Integer getMonth() {
            return month;
        }

        public Integer getDay() {
            return day;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "year=" + year +
                    ", month=" + month +
                    ", day=" + day +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "TermStartDateData{" +
                "code='" + getCode() + '\'' +
                ", msg='" + getMsg() + '\'' +
                ", data=" + data +
                '}';
    }
}
