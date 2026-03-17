package com.example.wustchat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wust-chat.cache")
public class WustChatCacheProperties {
    private long qaTtlMinutes = 15;
    private long corpusListTtlMinutes = 5;
    private long corpusDetailTtlMinutes = 10;
    private long tagsTtlMinutes = 10;
    private String kbVersionKey = "wust:chat:kb:version";

    public long getQaTtlMinutes() {
        return qaTtlMinutes;
    }

    public void setQaTtlMinutes(long qaTtlMinutes) {
        this.qaTtlMinutes = qaTtlMinutes;
    }

    public long getCorpusListTtlMinutes() {
        return corpusListTtlMinutes;
    }

    public void setCorpusListTtlMinutes(long corpusListTtlMinutes) {
        this.corpusListTtlMinutes = corpusListTtlMinutes;
    }

    public long getCorpusDetailTtlMinutes() {
        return corpusDetailTtlMinutes;
    }

    public void setCorpusDetailTtlMinutes(long corpusDetailTtlMinutes) {
        this.corpusDetailTtlMinutes = corpusDetailTtlMinutes;
    }

    public long getTagsTtlMinutes() {
        return tagsTtlMinutes;
    }

    public void setTagsTtlMinutes(long tagsTtlMinutes) {
        this.tagsTtlMinutes = tagsTtlMinutes;
    }

    public String getKbVersionKey() {
        return kbVersionKey;
    }

    public void setKbVersionKey(String kbVersionKey) {
        this.kbVersionKey = kbVersionKey;
    }
}
