package com.github.kayvannj.util.permission;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * PermissionUtil.with(context).request(permission).onAllGranted(function).onAllDenied(function).reason("").ask();
 * Created by kayvan on 10/26/15.
 */
public class PermissionUtil {

    public static final int REQ_CODE_ALL_GRANTED = -1;
    private static final String TAG = PermissionUtil.class.getSimpleName();
    static private AppCompatActivity mActivity;

    public static PermissionObject with(AppCompatActivity activity) {
        mActivity = activity;
        return new PermissionObject();
    }


    public static class PermissionObject {


        private ArrayList<SinglePermission> mPermissions;
        private String[] mPermissionNames;

        public PermissionRequestObject request(int reqCode, String... permissionNames) {
            mPermissionNames = permissionNames;//new ArrayList<String>(Arrays.asList(permissionNames));
            int length = permissionNames.length;
            mPermissions = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                mPermissions.add(new SinglePermission(permissionNames[i]));
            }

            if (needToAsk()) {
                Log.i(TAG, "Asking for permission");
                ActivityCompat.requestPermissions(mActivity,mPermissionNames , reqCode);
            } else {
                Log.i(TAG, "No need to ask for permission");
                return new PermissionRequestObject(REQ_CODE_ALL_GRANTED);
            }
            return new PermissionRequestObject(reqCode, mPermissions);
        }

        private boolean needToAsk() {
            ArrayList<SinglePermission> neededPermissions = new ArrayList<>(mPermissions);
            for (int i = 0; i < mPermissions.size(); i++) {
                SinglePermission perm = mPermissions.get(i);
                int checkRes = ContextCompat.checkSelfPermission(mActivity, perm.getPermissionName());
                if (checkRes == PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.remove(perm);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, perm.getPermissionName())) {
                        perm.setRationalNeeded(true);
                    }
                }
            }
            mPermissions = neededPermissions;
            mPermissionNames = new String[mPermissions.size()];
            for (int i = 0; i < mPermissions.size(); i++) {
                mPermissionNames[i] = mPermissions.get(i).getPermissionName();
            }
            return mPermissions.size() == 0 ? false : true;
        }


    }

    static public class PermissionRequestObject {

        private static final String TAG = PermissionObject.class.getSimpleName();
        private ArrayList<SinglePermission> mPermissions;
        private int mRequestCode;
        private Func mRationalFunc;
        private Func mGrantFunc;
        private Func mDenyFunc;

        public PermissionRequestObject(int requestCode, ArrayList<SinglePermission> permissions) {
            mRequestCode = requestCode;
            mPermissions = permissions;
        }

        public PermissionRequestObject(int reqCode) {
            if (reqCode == REQ_CODE_ALL_GRANTED && mGrantFunc != null) mGrantFunc.call();
        }

        public PermissionRequestObject onRational(Func rationalFunc) {
            mRationalFunc = rationalFunc;
            return this;
        }

        public PermissionRequestObject onAllGranted(Func grantFunc) {
            mGrantFunc = grantFunc;
            return this;
        }

        public PermissionRequestObject onAllDenied(Func denyFunc) {
            mDenyFunc = denyFunc;
            return this;
        }

        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            Log.i(TAG, String.format("ReqCode: %d, ResCode: %d, PermissionName: %s", requestCode, grantResults[0], permissions[0]));

            if (mRequestCode == requestCode) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (mPermissions.get(i).isRationalNeeded()) {
                            if (mRationalFunc != null) mRationalFunc.call();
                            else defaultRational();
                        } else {
                            if (mDenyFunc != null) {
                                Log.i(TAG, "Calling Deny Func");
                                mDenyFunc.call();
                            } else Log.e(TAG, "NUll DENY FUNCTIONS");
                        }
                    } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (mGrantFunc != null) {
                            Log.i(TAG, "Calling Grant Func");
                            mGrantFunc.call();
                        } else Log.e(TAG, "NUll GRANT FUNCTIONS");
                    }

                }
            }
        }

        private void defaultRational() {

        }
    }
}
