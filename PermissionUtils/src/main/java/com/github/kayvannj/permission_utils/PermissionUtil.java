package com.github.kayvannj.permission_utils;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 *
 * <p/>
 * Created by kayvan on 10/26/15.
 */
public class PermissionUtil {

    public static final int REQ_CODE_ALL_GRANTED = -1;
    private static final String TAG = PermissionUtil.class.getSimpleName();
    static private AppCompatActivity mAppCompatActivity;


    public static PermissionObject with(AppCompatActivity activity) {
        mAppCompatActivity = activity;
        return new PermissionObject();
    }

    /**
     * Gets all the needed permission names and creates a PermissionRequestObject accordingly
     */
    public static class PermissionObject {

        public PermissionRequestObject request(String permissionName) {
            return new PermissionRequestObject(new String[]{permissionName});
        }

        public PermissionRequestObject request(String... permissionNames) {
            return new PermissionRequestObject(permissionNames);
        }
    }

    static public class PermissionRequestObject {

        private static final String TAG = PermissionObject.class.getSimpleName();

        private ArrayList<SinglePermission> mPermissionsWeDontHave;
        private int mRequestCode;
        private Func mRationalFunc;
        private Func mGrantFunc;
        private Func mDenyFunc;
        private Func2 mResultFunc;
        private String[] mPermissionNames;

        public PermissionRequestObject(String[] permissionNames) {
            mPermissionNames = permissionNames;
        }

        public PermissionRequestObject ask(int reqCode) {
            mRequestCode = reqCode;
            int length = mPermissionNames.length;
            mPermissionsWeDontHave = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                mPermissionsWeDontHave.add(new SinglePermission(mPermissionNames[i]));
            }

            if (needToAsk()) {
                Log.i(TAG, "Asking for permission");
                ActivityCompat.requestPermissions(mAppCompatActivity, mPermissionNames, reqCode);
            } else {
                Log.i(TAG, "No need to ask for permission");
                if(mGrantFunc!=null)
                    mGrantFunc.call();
            }
            return this;
        }

        private boolean needToAsk() {
            ArrayList<SinglePermission> neededPermissions = new ArrayList<>(mPermissionsWeDontHave);
            for (int i = 0; i < mPermissionsWeDontHave.size(); i++) {
                SinglePermission perm = mPermissionsWeDontHave.get(i);
                int checkRes = ContextCompat.checkSelfPermission(mAppCompatActivity, perm.getPermissionName());
                if (checkRes == PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.remove(perm);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mAppCompatActivity, perm.getPermissionName())) {
                        perm.setRationalNeeded(true);
                    }
                }
            }
            mPermissionsWeDontHave = neededPermissions;
            mPermissionNames = new String[mPermissionsWeDontHave.size()];
            for (int i = 0; i < mPermissionsWeDontHave.size(); i++) {
                mPermissionNames[i] = mPermissionsWeDontHave.get(i).getPermissionName();
            }
            return mPermissionsWeDontHave.size() != 0;
        }

        @NonNull public PermissionRequestObject onRational(@NonNull Func rationalFunc) {
            mRationalFunc = rationalFunc;
            return this;
        }

        @NonNull public PermissionRequestObject onAllGranted(@NonNull Func grantFunc) {
            mGrantFunc = grantFunc;
            return this;
        }

        @NonNull public PermissionRequestObject onAllDenied(@NonNull Func denyFunc) {
            mDenyFunc = denyFunc;
            return this;
        }
        @NonNull public PermissionRequestObject onResult(@NonNull Func2 resultFunc) {
            mResultFunc = resultFunc;
            return this;
        }

        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            Log.i(TAG, String.format("ReqCode: %d, ResCode: %d, PermissionName: %s", requestCode, grantResults[0], permissions[0]));

            if (mRequestCode == requestCode) {
                if (mResultFunc != null) {
                    Log.i(TAG, "Calling Results Func");
                    mResultFunc.call(requestCode, permissions, grantResults);
                    return;
                }

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (mPermissionsWeDontHave.get(i).isRationalNeeded()) {
                            if (mRationalFunc != null) {
                                Log.i(TAG, "Calling Rational Func");
                                mRationalFunc.call();
                            }
                        }
                        if (mDenyFunc != null) {
                            Log.i(TAG, "Calling Deny Func");
                            mDenyFunc.call();
                        } else Log.e(TAG, "NUll DENY FUNCTIONS");
                        // terminate if there is at least one deny
                        return;
                    }
                }

                // there has not been any deny
                if (mGrantFunc != null) {
                    Log.i(TAG, "Calling Grant Func");
                    mGrantFunc.call();
                } else Log.e(TAG, "NUll GRANT FUNCTIONS");
            }
        }
    }
}
