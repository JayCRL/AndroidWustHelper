package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class AiQaAnswerBean {

    @SerializedName(value = "question", alternate = {"query"})
    private String question;
    @SerializedName(value = "matchedTag", alternate = {"tag"})
    private String matchedTag;
    @SerializedName(value = "answer", alternate = {"content", "reply"})
    private String answer;
    @SerializedName(value = "hasStudentSource", alternate = {"studentSource", "fromStudentSource"})
    private boolean hasStudentSource;
    @SerializedName(value = "cacheHit", alternate = {"hitCache"})
    private boolean cacheHit;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getMatchedTag() {
        return matchedTag;
    }

    public void setMatchedTag(String matchedTag) {
        this.matchedTag = matchedTag;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isHasStudentSource() {
        return hasStudentSource;
    }

    public void setHasStudentSource(boolean hasStudentSource) {
        this.hasStudentSource = hasStudentSource;
    }

    public boolean isCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(boolean cacheHit) {
        this.cacheHit = cacheHit;
    }
}
