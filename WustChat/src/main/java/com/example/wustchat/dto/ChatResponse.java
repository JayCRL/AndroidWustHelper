package com.example.wustchat.dto;

public class ChatResponse {
    private String question;
    private String matchedTag;
    private String answer;
    private boolean hasStudentSource;
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
