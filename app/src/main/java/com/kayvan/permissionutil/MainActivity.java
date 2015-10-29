package com.kayvan.permissionutil;

import com.kayvan.tools.permissionutil.Func;
import com.kayvan.tools.permissionutil.PermissionRequestObject;
import com.kayvan.tools.permissionutil.PermissionUtil;
import com.kayvan.tools.permissionutil.SinglePermission;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CONTACTS = 1;
    private static final int REQUEST_CODE_STORAGE = 2;
    @Bind(R.id.status) TextView mStatus;
    private PermissionRequestObject mStoragePermissionRequest;
    private PermissionRequestObject mContactsPermissionRequest;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.storage) public void onAskForStoragePermissionClick() {
        mStoragePermissionRequest = PermissionUtil.with(this).request(
                new SinglePermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, "For some reason that don't wanna discuss")).onGrant(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionGranted();
                    }
                }).onDeny(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionDenied();
                    }
                }).ask(REQUEST_CODE_STORAGE);
    }

    private void doOnPermissionDenied() {
        updateStatus("Permission Denied or is on \"Do Not SHow Again\"");
    }

    private void doOnPermissionGranted() {
        updateStatus("Permission Granted");
    }

    private void updateStatus(String s) {mStatus.setText(String.format("> %s\n", s) + mStatus.getText().toString());}

    @OnClick(R.id.contacts) public void onAskForContactsPermissionClick() {
        mContactsPermissionRequest = PermissionUtil.with(this).request(
                new SinglePermission(
                        Manifest.permission.WRITE_CONTACTS, "For some reason other reason")).onGrant(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionGranted();
                    }
                }).onDeny(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionDenied();
                    }
                }).ask(REQUEST_CODE_CONTACTS);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mStoragePermissionRequest != null) mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (mContactsPermissionRequest != null)
            mContactsPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
