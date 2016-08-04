# PermissionUtil
[![Join the chat at https://gitter.im/kayvannj/PermissionUtil](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/kayvannj/PermissionUtil?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-PermissionUtil-green.svg?style=true)](https://android-arsenal.com/details/1/2750)
###What is this?
A simple wrapper around Android 6.0 runtime permission api
###Why?
Adding runtime permissions is not hard but having to seperate your code and move the methods around to capture callbacks is a little pain. This library provides a chained api to do all you need to do for supporting runtime permissions.

###How?
Anywhere in your ```AppCompatActivity``` that you want to ask for user's permisssion
```java
mRequestObject = PermissionUtil.with(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).onAllGranted(
                new Func() {
                    @Override protected void call() {
                        //Happy Path
                    }
                }).onAnyDenied(
                new Func() {
                    @Override protected void call() {
                        //Sad Path
                    }
                }).ask(REQUEST_CODE_STORAGE);
```
And add this to ```onRequestPermissionsResult```
```java
mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
```
Add the requested permission to your ```AndroidManifest.xml``` as well
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

###Dependency?
```groovy
compile 'com.android.support:appcompat-v7:23.1.0'
```
###Download
```groovy
compile 'com.github.kayvannj:PermissionUtils:1.0.2@aar'
```


License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


