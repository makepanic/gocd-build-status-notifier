package com.tw.go.plugin.provider.bitbucket;

public enum BitbucketCommitState {
    INPROGRESS("INPROGRESS"),
    SUCCESSFUL("SUCCESSFUL"),
    FAILED("FAILED"),
    STOPPED("STOPPED");

    private String status;

    BitbucketCommitState(String status) {
        this.status = status;
    }

    public String status() {
        return this.status;
    }
}
