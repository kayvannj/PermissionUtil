package com.github.kayvannj.permission_utils

import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Created by kayvan on 10/26/15.
 */
object PermissionUtil {

	fun with(activity: AppCompatActivity) = ActivityPermissionManager(activity)
	fun with(fragment: Fragment) = FragmentPermissionManager(fragment)

	interface PermissionManager {
		fun has(permissionName: String): Boolean
		fun request(vararg permissionNames: String): PermissionObject
	}

	class FragmentPermissionManager(private val fragment: Fragment) : PermissionManager {
		val permissionChecker = FragmentPermissionChecker(fragment)

		override fun request(vararg permissionNames: String) = PermissionObject(
				permissionChecker,
				permissionNames)

		override fun has(permissionName: String) = ContextCompat.checkSelfPermission(fragment.context!!,
				permissionName) == PackageManager.PERMISSION_GRANTED
	}

	class ActivityPermissionManager(private val activity: AppCompatActivity) : PermissionManager {
		val permissionChecker = ActivityPermissionChecker(activity)
		override fun has(permissionName: String) = ContextCompat.checkSelfPermission(activity,
				permissionName) == PackageManager.PERMISSION_GRANTED

		override fun request(vararg permissionNames: String) = PermissionObject(
				permissionChecker,
				permissionNames)

	}

	class PermissionObject(
			val permissionChecker: AbstractAndroidPermissionChecker,
			var mPermissionNames: Array<out String>,
			var mPermissionsWeDontHave: List<SinglePermission> = listOf(),
			var mRequestCode: Int = 0,
			/**
			 * Called for the first denied permission if there is need to show the rational
			 */
			private var mDenyFunc: () -> Unit = {},
			/**
			 * Called if all the permissions were granted
			 */
			private var mGrantFunc: () -> Unit = {},
			/**
			 * Called if there is at least one denied permission
			 */
			private var mRationalFunc: (permissionName: String) -> Unit = {},
			var mResultFunc: ((requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit)? = null
	) {

		fun onAllGranted(value: () -> Unit): PermissionObject {
			mGrantFunc = value
			return this
		}

		fun onAnyDenied(value: () -> Unit): PermissionObject {
			mDenyFunc = value
			return this
		}

		fun onRationalNeeded(value: (permissionName: String) -> Unit): PermissionObject {
			mRationalFunc = value
			return this
		}

		fun onResult(value: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit): PermissionObject {
			mResultFunc = value
			return this
		}

		/**
		 * Execute the permission request with the given Request Code
		 *
		 * @param reqCode a unique request code in your activity
		 */
		fun ask(reqCode: Int): PermissionObject {
			mRequestCode = reqCode
			val needToAsk = mPermissionNames.asSequence().map { SinglePermission(it) }
					.filter {
						permissionChecker.permissionIsGranted(it)
					}.map {
						it.isRationalNeeded = permissionChecker.shouldShowRequestPermissionRationale(
								it)
						it
					}.toList().also {
						mPermissionsWeDontHave = it
					}.toList().isNotEmpty()

			if (needToAsk) {
				Log.i(TAG, "Asking for permission")
				permissionChecker.requestPermissions(mPermissionNames, reqCode)
			} else {
				Log.i(TAG, "No need to ask for permission")
				mGrantFunc()
			}
			return this
		}

		/**
		 * This Method should be called from [ onRequestPermissionsResult][AppCompatActivity.onRequestPermissionsResult] with all the same incoming operands
		 * <pre>
		 * `public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		 * if (mStoragePermissionRequest != null)
		 * mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions,grantResults);
		 * }
		` *
		</pre> *
		 */
		fun onRequestPermissionsResult(requestCode: Int,
									   permissions: Array<String>,
									   grantResults: IntArray) {
			if (mRequestCode == requestCode) {
				if (mResultFunc != null) {
					Log.i(TAG, "Calling Results Func")
					mResultFunc?.invoke(requestCode, permissions, grantResults)
					return
				}

				for (i in permissions.indices) {
					if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
						if (mPermissionsWeDontHave[i].isRationalNeeded) {
							Log.i(TAG, "Calling Rational Func")
							mRationalFunc(mPermissionsWeDontHave[i].permissionName)
						} else {
							Log.i(TAG, "Calling Deny Func")
							mDenyFunc()
						}

						// terminate if there is at least one deny
						return
					}
				}

				// there has not been any deny
				Log.i(TAG, "Calling Grant Func")
				mGrantFunc()
			}
		}

		val TAG = this::class.java.simpleName
	}
}
