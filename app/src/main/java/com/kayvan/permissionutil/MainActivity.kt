package com.kayvan.permissionutil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.kayvannj.permission_utils.PermissionUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
	private var mBothPermissionRequest: PermissionUtil.PermissionObject? = null
	private var mContactsPermissionRequest: PermissionUtil.PermissionObject? = null
	private var mStoragePermissionRequest: PermissionUtil.PermissionObject? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		storage_check.setOnClickListener {
			val hasStoragePermission = PermissionUtil.with(this).has(WRITE_EXTERNAL_STORAGE)
			updateStatus(if (hasStoragePermission) "Has Storage permission" else "Doesn't have Storage permission")
		}

		contacts_check.setOnClickListener {
			val hasContactsPermission = PermissionUtil.with(this).has(WRITE_CONTACTS)
			updateStatus(if (hasContactsPermission) "Has Contacts permission" else "Doesn't have Contacts permission")
		}

		storage.setOnClickListener {
			mStoragePermissionRequest =
					PermissionUtil.with(this).request(WRITE_EXTERNAL_STORAGE)
							.onAllGranted {
								doOnPermissionGranted("Storage")
							}
							.onAnyDenied {
								doOnPermissionDenied("Storage")
							}
							.onRationalNeeded {
								updateStatus("Storage Permission Needs Rational")
							}
							.ask(REQUEST_CODE_STORAGE)
		}

		contacts.setOnClickListener {
			mContactsPermissionRequest =
					PermissionUtil.with(this).request(WRITE_CONTACTS)
							.onAllGranted {
								doOnPermissionGranted("Contacts")
							}
							.onAnyDenied {
								doOnPermissionDenied("Contacts")
							}
							.onRationalNeeded {
								updateStatus("Contacts Permission Needs Rational")
							}
							.ask(REQUEST_CODE_CONTACTS)
		}

		both.setOnClickListener {
			mBothPermissionRequest = PermissionUtil.with(this).request(WRITE_EXTERNAL_STORAGE,
					WRITE_CONTACTS)
					.onResult { requestCode: Int,
								permissions: Array<String>,
								grantResults: IntArray ->
						for (i in permissions.indices) {
							if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
								doOnPermissionGranted(permissions[i])
							} else {
								doOnPermissionDenied(permissions[i])
							}
						}
					}
					.ask(REQUEST_CODE_BOTH)
		}

	}

	private fun updateStatus(s: String) {
		status.text = "> $s\n${status.text}"
	}


	private fun doOnPermissionDenied(permission: String) {
		updateStatus("$permission Permission Denied or is on \"Do Not SHow Again\"")
	}

	private fun doOnPermissionGranted(permission: String) {
		updateStatus("$permission Permission Granted")
	}

	override fun onRequestPermissionsResult(requestCode: Int,
											permissions: Array<String>,
											grantResults: IntArray) {
		if (mStoragePermissionRequest != null) {
			mStoragePermissionRequest!!.onRequestPermissionsResult(requestCode,
					permissions,
					grantResults)
		}

		if (mContactsPermissionRequest != null) {
			mContactsPermissionRequest!!.onRequestPermissionsResult(requestCode,
					permissions,
					grantResults)
		}

		if (mBothPermissionRequest != null) {
			mBothPermissionRequest!!.onRequestPermissionsResult(requestCode,
					permissions,
					grantResults)
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}

	companion object {

		val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
		val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
		private val REQUEST_CODE_CONTACTS = 1
		private val REQUEST_CODE_STORAGE = 2
		private val REQUEST_CODE_BOTH = 3
	}
}
