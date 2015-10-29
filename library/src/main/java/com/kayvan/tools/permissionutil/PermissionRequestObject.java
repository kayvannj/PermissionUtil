package com.kayvan.tools.permissionutil;

import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by kayvan on 10/27/15.
 */
public class PermissionRequestObject {

    private static final String TAG = PermissionObject.class.getSimpleName();
    private int mRequestCode;
    private Func mGrantFunc;
    private Func mDenyFunc;

    public PermissionRequestObject(int requestCode, Func grantFunc, Func denyFunc) {
        mRequestCode = requestCode;
        mGrantFunc = grantFunc;
        mDenyFunc = denyFunc;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.i(TAG, String.format("ReqCode: %d, ResCode: %d, PermissionName: %s", requestCode, grantResults[0], permissions[0]));
        if (mRequestCode == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (mDenyFunc != null) {
                    Log.i(TAG, "Calling Deny Func");
                    mDenyFunc.call();
                } else Log.e(TAG, "NUll DENY FUNCTIONS");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mGrantFunc != null) {
                    Log.i(TAG, "Calling Grant Func");
                    mGrantFunc.call();
                } else Log.e(TAG, "NUll GRANT FUNCTIONS");
            }
        }
    }
}
