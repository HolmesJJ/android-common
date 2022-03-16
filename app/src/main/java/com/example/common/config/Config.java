package com.example.common.config;

import com.example.common.constants.SpUtilKeyConstants;
import com.example.common.utils.SpUtils;

public final class Config {

    public static final String SETTING_CONFIG = "SettingConfig";

    private static SpUtils sSp = SpUtils.getInstance(SETTING_CONFIG);

    private static boolean sIsLogin;
    private static int sUserId;
    private static String sUsername;

    private Config() {
    }

    public static boolean isLogin() {
        return sIsLogin;
    }

    public static void setLogin(boolean isLogin) {
        sSp.put(SpUtilKeyConstants.IS_LOGIN, isLogin);
        sIsLogin = isLogin;
    }

    public static int getUserId() {
        return sUserId;
    }

    public static void setUserId(int userId) {
        sSp.put(SpUtilKeyConstants.USER_ID, userId);
        sUserId = userId;
    }

    public static String getUsername() {
        return sUsername;
    }

    public static void setUsername(String username) {
        sSp.put(SpUtilKeyConstants.USERNAME, username);
        sUsername = username;
    }

    public static void resetConfig() {
        SpUtils.getInstance(SETTING_CONFIG).clear();
        loadConfig();
    }

    public static void loadConfig() {
        sIsLogin = sSp.getBoolean(SpUtilKeyConstants.IS_LOGIN, false);
        sUserId = sSp.getInt(SpUtilKeyConstants.USER_ID, -1);
        sUsername = sSp.getString(SpUtilKeyConstants.USERNAME, "");
    }

    static {
        loadConfig();
    }
}
