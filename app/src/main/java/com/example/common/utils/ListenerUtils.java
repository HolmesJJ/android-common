package com.example.common.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.databinding.BaseObservable;
import androidx.databinding.Observable;
import androidx.databinding.Observable.OnPropertyChangedCallback;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.example.common.listener.OnMultiClickListener;

import java.util.HashMap;
import java.util.Map;

public final class ListenerUtils {

    private static final Map<Integer, OnPropertyChangedCallback> CALLBACK_MAP = new HashMap<>();

    private ListenerUtils() {
    }

    public static void addSignalOnPropertyChangeCallback(final BaseObservable baseObservable, final Callback callback) {
        if (baseObservable == null) {
            return;
        }
        OnPropertyChangedCallback onPropertyChangedCallback = CALLBACK_MAP.get(baseObservable.hashCode());
        if (onPropertyChangedCallback != null) {
            baseObservable.removeOnPropertyChangedCallback(onPropertyChangedCallback);
        }
        OnPropertyChangedCallback propertyChangedCallback = new OnPropertyChangedCallback() {

            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (observable == null) {
                    return;
                }
                if (callback != null) {
                    callback.callback(observable, i);
                }
            }
        };
        baseObservable.addOnPropertyChangedCallback(propertyChangedCallback);
        CALLBACK_MAP.put(baseObservable.hashCode(), propertyChangedCallback);
    }

    public static void addSignalOnPropertyChangeCallback(ObservableBoolean observableBoolean, final BooleanCallback callback) {
        if (observableBoolean == null) {
            return;
        }
        OnPropertyChangedCallback onPropertyChangedCallback = CALLBACK_MAP.get(observableBoolean.hashCode());

        if (onPropertyChangedCallback != null) {
            observableBoolean.removeOnPropertyChangedCallback(onPropertyChangedCallback);
        }

        OnPropertyChangedCallback propertyChangedCallback = new OnPropertyChangedCallback() {

            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (!(observable instanceof ObservableBoolean)) {
                    return;
                }
                if (callback != null) {
                    callback.callback(observable, i, ((ObservableBoolean) observable).get());
                }
            }
        };
        observableBoolean.addOnPropertyChangedCallback(propertyChangedCallback);
        CALLBACK_MAP.put(observableBoolean.hashCode(), propertyChangedCallback);
    }

    public static void addSignalOnPropertyChangeCallback(ObservableField<String> observable, final StringCallback callback) {
        if (observable == null) {
            return;
        }
        OnPropertyChangedCallback onPropertyChangedCallback = CALLBACK_MAP.get(observable.hashCode());

        if (onPropertyChangedCallback != null) {
            observable.removeOnPropertyChangedCallback(onPropertyChangedCallback);
        }
        OnPropertyChangedCallback propertyChangedCallback = new OnPropertyChangedCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (!(observable instanceof ObservableField)) {
                    return;
                }
                if (callback != null) {
                    callback.callback(observable, i, ((ObservableField<String>) observable).get());
                }
            }
        };
        observable.addOnPropertyChangedCallback(propertyChangedCallback);
        CALLBACK_MAP.put(observable.hashCode(), propertyChangedCallback);
    }

    public static void addOnPropertyChangeCallback(final BaseObservable baseObservable, final Callback callback) {

        if (baseObservable == null) {
            return;
        }
        OnPropertyChangedCallback propertyChangedCallback = new OnPropertyChangedCallback() {

            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (callback != null) {
                    callback.callback(observable, i);
                }
            }
        };
        baseObservable.addOnPropertyChangedCallback(propertyChangedCallback);
    }

    public static void addTextChangeListener(EditText editText, final TextChange textChange) {
        if (editText == null) {
            return;
        }
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && textChange != null) {
                    textChange.textChange(s.toString());
                }
            }
        });
    }

    public static void setOnClickListener(View view, OnMultiClickListener listener) {
        if (view == null) {
            return;
        }
        view.setOnClickListener(listener);
    }

    public static void setOnClickListener(View view, View.OnClickListener listener) {
        if (view == null) {
            return;
        }
        view.setOnClickListener(listener);
    }

    public static void setOnTouchListener(View view, View.OnTouchListener listener) {
        if (view == null) {
            return;
        }
        view.setOnTouchListener(listener);
    }

    public static void setCheckChangeListener(CompoundButton view, CompoundButton.OnCheckedChangeListener listener) {
        if (view == null) {
            return;
        }
        view.setOnCheckedChangeListener(listener);
    }

    public static void setFocusChangeListener(View view, View.OnFocusChangeListener listener) {
        if (view == null) {
            return;
        }
        view.setOnFocusChangeListener(listener);
    }

    public static void remove(ObservableBoolean observableBoolean) {
        CALLBACK_MAP.remove(observableBoolean.hashCode());
    }

    public interface TextChange {
        void textChange(String s);
    }

    public interface Callback {
        void callback(Observable observable, int i);
    }

    public interface BooleanCallback {
        void callback(Observable observable, int i, boolean value);
    }

    public interface StringCallback {
        void callback(Observable observable, int i, String value);
    }
}
