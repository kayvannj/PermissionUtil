package com.kayvan.tools.permissionutil;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by kayvan on 10/27/15.
 */
public class PermissionObject {

    private static final String TAG = PermissionObject.class.getSimpleName();
    private AppCompatActivity mActivity;
    private SinglePermission mPermission;
    private Func mGrantFunc;
    private Func mDenyFunc;
    private Func mRationalFunc;

    public PermissionObject(AppCompatActivity activity) {
        mActivity = activity;
    }

    public PermissionObject request(SinglePermission permission) {
        mPermission = permission;
        return this;
    }

    public PermissionObject request(String permissionName) {
        mPermission = new SinglePermission(permissionName);
        return this;
    }

    public PermissionObject onGrant(Func grantFunc) {
        mGrantFunc = grantFunc;
        return this;
    }

    public PermissionObject onDeny(Func denyFunc) {
        mDenyFunc = denyFunc;
        return this;
    }
    public PermissionObject onShowRational(Func rationalFunc){
        mRationalFunc = rationalFunc;
        return this;
    }

    public PermissionRequestObject ask(int requestCode) {
        if (needToAsk()) {
            Log.i(TAG, "Asking for permission");
            ActivityCompat.requestPermissions(mActivity, new String[]{mPermission.getPermissionName()}, requestCode);
        } else {
            Log.i(TAG, "No need to ask for permission");
            mGrantFunc.call();
        }
        return new PermissionRequestObject(requestCode, mGrantFunc, mDenyFunc);
    }

    private boolean needToAsk() {
        SinglePermission perm = mPermission;
        int checkRes = ContextCompat.checkSelfPermission(mActivity, perm.getPermissionName());
        if (checkRes == PackageManager.PERMISSION_GRANTED) {
            perm.setIsGranted(true);
            return false;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, perm.getPermissionName())) {
                if (mRationalFunc!=null){
                    mRationalFunc.call();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(perm.getReason()).setPositiveButton(
                            "OK", new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create().show();
                }
            }
        }
        return true;
    }

}
