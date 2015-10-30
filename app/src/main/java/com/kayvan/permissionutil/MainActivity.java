package com.kayvan.permissionutil;

import com.github.kayvannj.util.permission.Func;
import com.github.kayvannj.util.permission.PermissionUtil;

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
    private PermissionUtil.PermissionRequestObject mStoragePermissionRequest;
    private PermissionUtil.PermissionRequestObject mContactsPermissionRequest;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.storage) public void onAskForStoragePermissionClick() {
        mStoragePermissionRequest = PermissionUtil.with(this).request(
                REQUEST_CODE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_CONTACTS);
        mStoragePermissionRequest.onAllGranted(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionGranted();
                    }
                }).onAllDenied(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionDenied();
                    }
                });

    }

    private void doOnPermissionDenied() {
        updateStatus("Permission Denied or is on \"Do Not SHow Again\"");
    }

    private void doOnPermissionGranted() {
        updateStatus("Permission Granted");
    }

    private void updateStatus(String s) {mStatus.setText(String.format("> %s\n", s) + mStatus.getText().toString());}

    @OnClick(R.id.contacts) public void onAskForContactsPermissionClick() {
//        mContactsPermissionRequest = PermissionUtil.with(this).request(
//                new SinglePermission(
//                        Manifest.permission.WRITE_CONTACTS, "For some reason other reason")).onAllGranted(
//                new Func() {
//                    @Override protected void call(String permissionName) {
//                        doOnPermissionGranted();
//                    }
//                }).onAllDenied(
//                new Func() {
//                    @Override protected void call(String permissionName) {
//                        doOnPermissionDenied();
//                    }
//                }).ask(REQUEST_CODE_CONTACTS);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mStoragePermissionRequest != null) mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (mContactsPermissionRequest != null)
            mContactsPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
