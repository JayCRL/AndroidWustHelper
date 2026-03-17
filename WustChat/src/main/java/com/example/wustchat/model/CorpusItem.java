package com.example.wustchat.model;

public class CorpusItem {
    private Long id;
    private String text;
    private String source;
    private String tag;
    private String expireAt;
    private String uploaderType;
    private boolean studentSubmitted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }

    public String getUploaderType() {
        return uploaderType;
    }

    public void setUploaderType(String uploaderType) {
        this.uploaderType = uploaderType;
    }

    public boolean isStudentSubmitted() {
        return studentSubmitted;
    }

    public void setStudentSubmitted(boolean studentSubmitted) {
        this.studentSubmitted = studentSubmitted;
    }
}
