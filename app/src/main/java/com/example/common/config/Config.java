package com.example.common.config;

import com.example.common.constants.SpUtilKeyConstants;
import com.example.common.constants.SpUtilValueConstants;
import com.example.common.utils.SpUtils;

public final class Config {

    public static final String SETTING_CONFIG = "SettingConfig";

    private final static SpUtils SP = SpUtils.getInstance(SETTING_CONFIG);

    private static String sPublicKey;
    private static String sDeviceUUID;
    private static boolean sIsLogin;
    private static int sUserId;
    private static String sUsername;
    private static String sToken;
    private static String sRefreshToken;
    private static String sSpeechData;

    private Config() {
    }

    public static String getPublicKey() {
        return sPublicKey;
    }

    public static void setPublicKey(String publicKey) {
        SP.put(SpUtilKeyConstants.PUBLIC_KEY, publicKey);
        sPublicKey = publicKey;
    }

    public static String getDeviceUUID() {
        return sDeviceUUID;
    }

    public static void setDeviceUUID(String deviceUUID) {
        SP.put(SpUtilKeyConstants.DEVICE_UUID, deviceUUID);
        sDeviceUUID = deviceUUID;
    }

    public static boolean isLogin() {
        return sIsLogin;
    }

    public static void setLogin(boolean isLogin) {
        SP.put(SpUtilKeyConstants.IS_LOGIN, isLogin);
        sIsLogin = isLogin;
    }

    public static int getUserId() {
        return sUserId;
    }

    public static void setUserId(int userId) {
        SP.put(SpUtilKeyConstants.USER_ID, userId);
        sUserId = userId;
    }

    public static String getUsername() {
        return sUsername;
    }

    public static void setUsername(String username) {
        SP.put(SpUtilKeyConstants.USERNAME, username);
        sUsername = username;
    }

    public static String getToken() {
        return sToken;
    }

    public static void setToken(String token) {
        SP.put(SpUtilKeyConstants.TOKEN, token);
        sToken = token;
    }

    public static String getRefreshToken() {
        return sRefreshToken;
    }

    public static void setRefreshToken(String refreshToken) {
        SP.put(SpUtilKeyConstants.REFRESH_TOKEN, refreshToken);
        sRefreshToken = refreshToken;
    }

    public static String getSpeechData() {
        return sSpeechData;
    }

    public static void setSpeechData(String speechData) {
        SP.put(SpUtilKeyConstants.SPEECH_DATA, speechData);
        sSpeechData = speechData;
    }

    public static void resetConfig() {
        SpUtils.getInstance(SETTING_CONFIG).clear();
        loadConfig();
    }

    public static void loadConfig() {
        sPublicKey = SP.getString(SpUtilKeyConstants.PUBLIC_KEY, "");
        sDeviceUUID = SP.getString(SpUtilKeyConstants.DEVICE_UUID, SpUtilValueConstants.VALUE_DEVICE_UUID);
        sIsLogin = SP.getBoolean(SpUtilKeyConstants.IS_LOGIN, false);
        sUserId = SP.getInt(SpUtilKeyConstants.USER_ID, -1);
        sUsername = SP.getString(SpUtilKeyConstants.USERNAME, "");
        sToken = SP.getString(SpUtilKeyConstants.TOKEN, "");
        sRefreshToken = SP.getString(SpUtilKeyConstants.REFRESH_TOKEN, "");
        sSpeechData = SP.getString(SpUtilKeyConstants.SPEECH_DATA, "");
    }

    static {
        loadConfig();
    }
}
