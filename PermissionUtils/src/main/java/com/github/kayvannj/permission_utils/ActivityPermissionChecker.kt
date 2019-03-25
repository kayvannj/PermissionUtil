package com.github.kayvannj.permission_utils

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

class ActivityPermissionChecker(val activity: AppCompatActivity) :
		AbstractAndroidPermissionChecker() {

	override fun permissionIsGranted(permission: SinglePermission): Boolean {
		return ContextCompat.checkSelfPermission(activity,
				permission.permissionName) != PackageManager.PERMISSION_GRANTED
	}

	override fun shouldShowRequestPermissionRationale(permission: SinglePermission): Boolean {
		return ActivityCompat.shouldShowRequestPermissionRationale(activity,
				permission.permissionName)
	}

	override fun requestPermissions(permissions: Array<out String>, reqCode: Int) {
		ActivityCompat.requestPermissions(activity, permissions, reqCode)
	}
}