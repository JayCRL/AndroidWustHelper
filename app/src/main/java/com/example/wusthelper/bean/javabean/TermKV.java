package com.example.wusthelper.bean.javabean;

public class TermKV {
    private String trueTerm;
    private String showTerm;

    public TermKV(String trueTerm, String showTerm) {
        this.trueTerm = trueTerm;
        this.showTerm = showTerm;
    }

    public String getTrueTerm() {
        return trueTerm;
    }

    public void setTrueTerm(String trueTerm) {
        this.trueTerm = trueTerm;
    }

    public String getShowTerm() {
        return showTerm;
    }

    public void setShowTerm(String showTerm) {
        this.showTerm = showTerm;
    }

    @Override
    public String toString() {
        return "TermKV{" +
                "trueTerm='" + trueTerm + '\'' +
                ", showTerm='" + showTerm + '\'' +
                '}';
    }
}
