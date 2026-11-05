package com.oasischeck;

import android.app.Application;

import com.oasischeck.util.ThemeHelper;

public class OasisCheckApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.aplicar(this);
    }
}
