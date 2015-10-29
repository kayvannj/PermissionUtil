package com.kayvan.tools.permissionutil;

/**
 * Created by kayvan on 10/27/15.
 */
public class SinglePermission {

    private String mPermissionName;
    private boolean isGranted = false;
    private String mReason;

    public SinglePermission(String permissionName) {
        mPermissionName = permissionName;
    }

    public SinglePermission(String permissionName, String reason) {
        mPermissionName = permissionName;
        mReason = reason;
    }

    public String getReason() {
        return mReason == null ? "" : mReason;
    }

    public void setReason(String reason) {
        mReason = reason;
    }

    public String getPermissionName() {
        return mPermissionName;
    }

    public void setPermissionName(String permissionName) {
        mPermissionName = permissionName;
    }

    public void setIsGranted(boolean isGranted) {
        this.isGranted = isGranted;
    }

    public boolean isGranted() {
        return isGranted;
    }
}
