package com.github.kayvannj.permission_utils

abstract class AbstractAndroidPermissionChecker {
	abstract fun permissionIsGranted(permission: SinglePermission): Boolean
	abstract fun shouldShowRequestPermissionRationale(permission: SinglePermission): Boolean
	abstract fun requestPermissions(permissions: Array<out String>, reqCode: Int)
}