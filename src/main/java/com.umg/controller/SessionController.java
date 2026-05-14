package com.dataquery.sqlcompiler.controller;

public class SessionController {
    private boolean isActive;

    public SessionController() {
        this.isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }

    public void closeSession() {
        this.isActive = false;
    }

    public void resetSession() {
        this.isActive = true;
    }
}
