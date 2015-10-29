package com.kayvan.tools.permissionutil;

import android.support.v7.app.AppCompatActivity;

/**
 * PermissionUtil.with(context).request(permission).onGrant(function).onDeny(function).reason("").ask();
 * Created by kayvan on 10/26/15.
 */
public class PermissionUtil {
    public static PermissionObject with(AppCompatActivity activity) {
        return new PermissionObject(activity);
    }

}
