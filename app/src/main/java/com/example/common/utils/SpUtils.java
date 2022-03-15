package com.example.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SpUtils {

    private final static Map<String, SpUtils> SP_MAP = new ConcurrentHashMap<>();
    private final SharedPreferences SP;

    private SpUtils(Context context, String spName) {
        this.SP = new SecuritySharedPreference(context, spName, 0);
    }

    /** @deprecated */
    @Deprecated
    public static SpUtils getInstance() {
        return getInstance("");
    }

    /** @deprecated */
    @Deprecated
    public static SpUtils getInstance(String spName) {
        if (isSpace(spName)) {
            spName = "spUtils";
        }
        SpUtils sp = (SpUtils) SP_MAP.get(spName);
        if (sp == null) {
            Class<SpUtils> var2 = SpUtils.class;
            synchronized (SpUtils.class) {
                sp = new SpUtils(ContextUtils.getContext(), spName);
                SP_MAP.put(spName, sp);
            }
        }
        return sp;
    }

    public static SpUtils getInstance(Context context, String spName) {
        if (isSpace(spName)) {
            spName = "spUtils";
        }
        SpUtils sp = (SpUtils) SP_MAP.get(spName);
        if (sp == null) {
            Class<SpUtils> var3 = SpUtils.class;
            synchronized (SpUtils.class) {
                sp = new SpUtils(context, spName);
                SP_MAP.put(spName, sp);
            }
        }
        return sp;
    }

    private static boolean isSpace(String s) {
        if (s == null) {
            return true;
        } else {
            int i = 0;

            for (int len = s.length(); i < len; ++i) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public void put(@NonNull String key, @NonNull String value) {
        this.SP.edit().putString(key, value).apply();
    }

    public String getString(@NonNull String key) {
        return this.getString(key, "");
    }

    public String getString(@NonNull String key, @NonNull String defaultValue) {
        return this.SP.getString(key, defaultValue);
    }

    public void put(@NonNull String key, int value) {
        this.SP.edit().putInt(key, value).apply();
    }

    public int getInt(@NonNull String key) {
        return this.getInt(key, -1);
    }

    public int getInt(@NonNull String key, int defaultValue) {
        return this.SP.getInt(key, defaultValue);
    }

    public void put(@NonNull String key, long value) {
        this.SP.edit().putLong(key, value).apply();
    }

    public long getLong(@NonNull String key) {
        return this.getLong(key, -1L);
    }

    public long getLong(@NonNull String key, long defaultValue) {
        return this.SP.getLong(key, defaultValue);
    }

    public void put(@NonNull String key, float value) {
        this.SP.edit().putFloat(key, value).apply();
    }

    public float getFloat(@NonNull String key) {
        return this.getFloat(key, -1.0F);
    }

    public float getFloat(@NonNull String key, float defaultValue) {
        return this.SP.getFloat(key, defaultValue);
    }

    public void put(@NonNull String key, boolean value) {
        this.SP.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(@NonNull String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return this.SP.getBoolean(key, defaultValue);
    }

    public void put(@NonNull String key, @NonNull Set<String> values) {
        this.SP.edit().putStringSet(key, values).apply();
    }

    public Set<String> getStringSet(@NonNull String key) {
        return this.getStringSet(key, Collections.<String>emptySet());
    }

    public Set<String> getStringSet(@NonNull String key, @NonNull Set<String> defaultValue) {
        return this.SP.getStringSet(key, defaultValue);
    }

    public void put(@NonNull String key, Serializable obj) {
        if (obj != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                String temp = new String(Base64.encode(baos.toByteArray(), 0));
                this.SP.edit().putString(key, temp).apply();
            } catch (IOException var6) {
                var6.printStackTrace();
            }
        }
    }

    public Serializable getSerializable(@NonNull String key) {
        String temp = this.SP.getString(key, "");
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(temp.getBytes(), 0));
        Serializable obj = null;

        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            obj = (Serializable) ois.readObject();
        } catch (IOException | ClassNotFoundException var6) {
            var6.printStackTrace();
        }

        return obj;
    }

    public Map<String, ?> getAll() {
        return this.SP.getAll();
    }

    public boolean contains(@NonNull String key) {
        return this.SP.contains(key);
    }

    public void remove(@NonNull String key) {
        this.SP.edit().remove(key).apply();
    }

    public void clear() {
        this.SP.edit().clear().apply();
    }
}
