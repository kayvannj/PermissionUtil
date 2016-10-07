package com.kayvan.permissionutil;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.Func2;
import com.github.kayvannj.permission_utils.PermissionUtil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CONTACTS = 1;
    private static final int REQUEST_CODE_STORAGE = 2;
    private static final int REQUEST_CODE_BOTH = 3;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
    @Bind(R.id.status) TextView mStatus;
    private PermissionUtil.PermissionRequestObject mStoragePermissionRequest;
    private PermissionUtil.PermissionRequestObject mContactsPermissionRequest;
    private PermissionUtil.PermissionRequestObject mBothPermissionRequest;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.storage_check) public void onCheckStoragePermissionClick(){
        boolean hasStoragePermission = PermissionUtil.with(this).has(WRITE_EXTERNAL_STORAGE);
        updateStatus(hasStoragePermission?"Has Storage permission":"Doesn't have Storage permission");
    }
    @OnClick(R.id.contacts_check) public void onCheckContactsPermissionClick(){
        boolean hasContactsPermission = PermissionUtil.with(this).has(WRITE_CONTACTS);
        updateStatus(hasContactsPermission?"Has Contacts permission":"Doesn't have Contacts permission");
    }

    @OnClick(R.id.storage) public void onAskForStoragePermissionClick() {
        mStoragePermissionRequest = PermissionUtil.with(this).request(WRITE_EXTERNAL_STORAGE).onAllGranted(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionGranted("Storage");
                    }
                }).onAnyDenied(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionDenied("Storage");
                    }
                }).ask(REQUEST_CODE_STORAGE);

    }

    @OnClick(R.id.both) public void onAskBothPermissionsClick() {
        mBothPermissionRequest =
                PermissionUtil.with(this).request(WRITE_EXTERNAL_STORAGE, WRITE_CONTACTS).onResult(
                        new Func2() {
                            @Override protected void call(int requestCode, String[] permissions, int[] grantResults) {
                                for (int i = 0; i < permissions.length; i++) {
                                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) doOnPermissionGranted(permissions[i]);
                                    else doOnPermissionDenied(permissions[i]);
                                }
                            }
                        }).ask(REQUEST_CODE_BOTH);

    }

    @OnClick(R.id.contacts) public void onAskForContactsPermissionClick() {
        mContactsPermissionRequest = PermissionUtil.with(this).request(WRITE_CONTACTS);
        mContactsPermissionRequest.onAllGranted(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionGranted("Contacts");
                    }
                }).onAnyDenied(
                new Func() {
                    @Override protected void call() {
                        doOnPermissionDenied("Contacts");
                    }
                }).ask(REQUEST_CODE_CONTACTS);
    }

    private void doOnPermissionDenied(String permission) {
        updateStatus(permission + " Permission Denied or is on \"Do Not SHow Again\"");
    }

    private void doOnPermissionGranted(String permission) {
        updateStatus(permission + " Permission Granted");
    }

    private void updateStatus(String s) {mStatus.setText(String.format("> %s\n", s) + mStatus.getText().toString());}

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mStoragePermissionRequest != null) mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (mContactsPermissionRequest != null)
            mContactsPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (mBothPermissionRequest != null) mBothPermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
