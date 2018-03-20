package com.zagsoft.polls.util;

public enum AppConstants {
    DEFAULT_PAGE_NUMBER(0), DEFAULT_PAGE_SIZE(30), MAX_PAGE_SIZE(50);

    private int value;
    AppConstants(int value) { this.value = value; }
    public int get() { return this.value; }
}
