# Redicode

Currently supported API:

1. Http request using Volley Library
2. Android Geo Location 
3. Google Service Location
4. Device Info



#### Usage
**Step 1.** Add the JitPack repository to your build file

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

	
**Step 2.** Add the dependency

```
dependencies {
	implementation 'com.gitlab.MuhdFauzan:redicode:vv2.0.3'
}
```

#### Android Location

```
RediAndroidLocationAPI androidLocationAPI = new RediAndroidLocationAPI(this, new RediView.OnLocationListener() {
    @Override
    public void onStart() {
        Log.e(TAG, "onStart: Start location" );
    }

    @Override
    public void onSuccess(double latitude, double longitude) {
        Log.e(TAG, "onSuccess: im here : " + latitude + ":" + longitude);
    }

    @Override
    public void onFailure(String error) {
        Log.e(TAG, "onFailure: "+error );
    }

    @Override
    public void onPermissionFailure() {
        Log.e(TAG, "onPermissionFailure: Please enable permission");
    }
});

```
#### Google Service Location

```
RediGoogleLocationAPI rediGoogleLocationAPI = new RediGoogleLocationAPI(this, 1);
    rediGoogleLocationAPI.startLocation(new RediView.OnLocationListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onSuccess(double latitude, double longitude) {
            Log.e(TAG, "onSuccess: "+latitude);
            Log.e(TAG, "onSuccess: "+longitude);

            rediGoogleLocationAPI.stopLocationUpdates();

        }

        @Override
        public void onFailure(String error) {

        }

        @Override
        public void onPermissionFailure() {
            Log.e(TAG, "onNoUserPermission: ");
        }
});

```

#### Device Info

```
    RediDeviceInfo rediDeviceInfo = new RediDeviceInfo(this);
    Log.e(TAG, "Serial No: " + rediDeviceInfo.getSerialNo() );
    Log.e(TAG, "Mac Address: "+ rediDeviceInfo.getMacAddress() );
    Log.e(TAG, "Device Imei: "+ rediDeviceInfo.getDeviceIMEI() );
    Log.e(TAG, "Device ID: "+ rediDeviceInfo.getDeviceID() );
```

# Link
https://jitpack.io/#com.gitlab.MuhdFauzan/redicode