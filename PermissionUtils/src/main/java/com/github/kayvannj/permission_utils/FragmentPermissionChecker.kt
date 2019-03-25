package com.github.kayvannj.permission_utils

import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

class FragmentPermissionChecker(val fragment: Fragment) :
		AbstractAndroidPermissionChecker() {

	override fun permissionIsGranted(permission: SinglePermission): Boolean {
		return ContextCompat.checkSelfPermission(fragment.context!!,
				permission.permissionName) != PackageManager.PERMISSION_GRANTED
	}

	override fun shouldShowRequestPermissionRationale(permission: SinglePermission): Boolean {
		return fragment.shouldShowRequestPermissionRationale(permission.permissionName)
	}

	override fun requestPermissions(permissions: Array<out String>, reqCode: Int) {
		fragment.requestPermissions(permissions, reqCode)
	}
}