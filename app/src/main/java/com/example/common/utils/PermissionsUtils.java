package com.example.common.utils;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import pub.devrel.easypermissions.EasyPermissions;

public final class PermissionsUtils {

    private PermissionsUtils() {
    }

    public static void doSomeThingWithPermission(Fragment fragment, Call call, String permission, int requestCode, int requestStrResId) {
        doSomeThingWithPermission(fragment.getActivity(), call, new String[]{permission}, requestCode,
                requestStrResId);
    }

    public static void doSomeThingWithPermission(Activity activity, Call call, String[] permission, int requestCode, int requestStrResId) {
        if (hasPermissions(permission)) {
            call.call();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    activity,
                    ContextUtils.getContext().getString(requestStrResId),
                    requestCode,
                    permission);
        }
    }

    private static boolean hasPermissions(String[] permissions) {
        return EasyPermissions.hasPermissions(ContextUtils.getContext(), permissions);
    }

    public static void doSomeThingWithPermission(Fragment fragment, Call call, String[] permission, int requestCode, int requestStrResId) {
        doSomeThingWithPermission(fragment.getActivity(), call, permission, requestCode,
                requestStrResId);
    }

    public static void doSomeThingWithPermission(Activity activity, Call call, String permission, int requestCode, int requestStrResId) {
        doSomeThingWithPermission(activity, call, new String[]{permission}, requestCode,
                requestStrResId);
    }

    public interface Call {
        void call();
    }
}
