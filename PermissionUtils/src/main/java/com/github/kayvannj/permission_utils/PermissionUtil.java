package com.github.kayvannj.permission_utils;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Created by kayvan on 10/26/15.
 */
public class PermissionUtil {

    public static PermissionObject with(AppCompatActivity activity) {
        return new PermissionObject(activity);
    }

    public static PermissionObject with(Fragment fragment) {
        return new PermissionObject(fragment);
    }

    public static class PermissionObject {

        private AppCompatActivity mActivity;
        private Fragment mFragment;

        PermissionObject(AppCompatActivity activity) {
            mActivity = activity;
        }

        PermissionObject(Fragment fragment) {
            mFragment = fragment;
        }

        public boolean has(String permissionName) {
            int permissionCheck;
            if (mActivity != null) {
                permissionCheck = ContextCompat.checkSelfPermission(mActivity, permissionName);
            } else {
                permissionCheck = ContextCompat.checkSelfPermission(mFragment.getContext(), permissionName);
            }

            return permissionCheck == PackageManager.PERMISSION_GRANTED;
        }

        public PermissionRequestObject request(String permissionName) {
            if (mActivity != null) {
                return new PermissionRequestObject(mActivity, new String[]{permissionName});
            } else {
                return new PermissionRequestObject(mFragment, new String[]{permissionName});
            }
        }

        public PermissionRequestObject request(String... permissionNames) {
            return new PermissionRequestObject(mActivity, permissionNames);
        }
    }

    static public class PermissionRequestObject {

        private static final String TAG = PermissionObject.class.getSimpleName();
        private AppCompatActivity mActivity;
        private Func mDenyFunc;
        private Fragment mFragment;
        private Func mGrantFunc;
        private String[] mPermissionNames;
        private ArrayList<SinglePermission> mPermissionsWeDontHave;
        private Func3 mRationalFunc;
        private int mRequestCode;
        private Func2 mResultFunc;

        public PermissionRequestObject(AppCompatActivity activity, String[] permissionNames) {
            mActivity = activity;
            mPermissionNames = permissionNames;
        }

        public PermissionRequestObject(Fragment fragment, String[] permissionNames) {
            mFragment = fragment;
            mPermissionNames = permissionNames;
        }

        /**
         * Execute the permission request with the given Request Code
         *
         * @param reqCode a unique request code in your activity
         */
        public PermissionRequestObject ask(int reqCode) {
            mRequestCode = reqCode;
            int length = mPermissionNames.length;
            mPermissionsWeDontHave = new ArrayList<>(length);
            for (String mPermissionName : mPermissionNames) {
                mPermissionsWeDontHave.add(new SinglePermission(mPermissionName));
            }

            if (needToAsk()) {
                Log.i(TAG, "Asking for permission");
                if (mActivity != null) {
                    ActivityCompat.requestPermissions(mActivity, mPermissionNames, reqCode);
                } else {
                    mFragment.requestPermissions(mPermissionNames, reqCode);
                }
            } else {
                Log.i(TAG, "No need to ask for permission");
                if (mGrantFunc != null) {
                    mGrantFunc.call();
                }
            }
            return this;
        }

        private boolean needToAsk() {
            ArrayList<SinglePermission> neededPermissions = new ArrayList<>(mPermissionsWeDontHave);
            for (int i = 0; i < mPermissionsWeDontHave.size(); i++) {
                SinglePermission perm = mPermissionsWeDontHave.get(i);
                int checkRes;
                if (mActivity != null) {
                    checkRes = ContextCompat.checkSelfPermission(mActivity, perm.getPermissionName());
                } else {
                    checkRes = ContextCompat.checkSelfPermission(mFragment.getContext(), perm.getPermissionName());
                }
                if (checkRes == PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.remove(perm);
                } else {
                    boolean shouldShowRequestPermissionRationale;
                    if (mActivity != null) {
                        shouldShowRequestPermissionRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(mActivity, perm.getPermissionName());
                    } else {
                        shouldShowRequestPermissionRationale = mFragment.shouldShowRequestPermissionRationale(perm.getPermissionName());
                    }
                    if (shouldShowRequestPermissionRationale) {
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

        /**
         * Called for the first denied permission if there is need to show the rational
         */
        public PermissionRequestObject onRational(Func3 rationalFunc) {
            mRationalFunc = rationalFunc;
            return this;
        }

        /**
         * Called if all the permissions were granted
         */
        public PermissionRequestObject onAllGranted(Func grantFunc) {
            mGrantFunc = grantFunc;
            return this;
        }

        /**
         * Called if there is at least one denied permission
         */
        public PermissionRequestObject onAnyDenied(Func denyFunc) {
            mDenyFunc = denyFunc;
            return this;
        }

        /**
         * Called with the original operands from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} for any result
         */
        public PermissionRequestObject onResult(Func2 resultFunc) {
            mResultFunc = resultFunc;
            return this;
        }

        /**
         * This Method should be called from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} with all the same incoming operands
         * <pre>
         * {@code
         *
         * public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
         *      if (mStoragePermissionRequest != null)
         *          mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions,grantResults);
         * }
         * }
         * </pre>
         */
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            if (mRequestCode == requestCode) {
                Log.i(TAG, String.format("ReqCode: %d, ResCode: %d, PermissionName: %s", requestCode, grantResults[0], permissions[0]));
                if (mResultFunc != null) {
                    Log.i(TAG, "Calling Results Func");
                    mResultFunc.call(requestCode, permissions, grantResults);
                    return;
                }

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (mPermissionsWeDontHave.get(i).isRationalNeeded() && mRationalFunc != null) {
                            Log.i(TAG, "Calling Rational Func");
                            mRationalFunc.call(mPermissionsWeDontHave.get(i).getPermissionName());
                        } else if (mDenyFunc != null) {
                            Log.i(TAG, "Calling Deny Func");
                            mDenyFunc.call();
                        } else {
                            Log.e(TAG, "NUll DENY FUNCTIONS");
                        }

                        // terminate if there is at least one deny
                        return;
                    }
                }

                // there has not been any deny
                if (mGrantFunc != null) {
                    Log.i(TAG, "Calling Grant Func");
                    mGrantFunc.call();
                } else {
                    Log.e(TAG, "NUll GRANT FUNCTIONS");
                }
            }
        }
    }
}
