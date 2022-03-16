package com.example.common.config;

import com.example.common.utils.SpUtils;

public final class Config {

    public static final String SETTING_CONFIG = "SettingConfig";

    private final static SpUtils SP = SpUtils.getInstance(SETTING_CONFIG);

    private Config() {
    }

    public static void resetConfig() {
        SpUtils.getInstance(SETTING_CONFIG).clear();
        loadConfig();
    }

    public static void loadConfig() {
    }

    static {
        loadConfig();
    }
}
