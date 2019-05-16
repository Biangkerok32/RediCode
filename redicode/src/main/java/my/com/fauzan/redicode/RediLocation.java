package my.com.fauzan.redicode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Timer;
import java.util.TimerTask;

public class RediLocation {

    private final String TAG = RediLocation.class.getSimpleName();

    private Context context;
    private String latitude;
    private String longitude;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private long timeout = 0;

    // location updates interval - 10sec
    private long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    public RediLocation(Context context){
        this.context = context;
    }

    public RediLocation(Context context, long setUpdateInterval, long setFastestUpdateInterval){
        this.context = context;
        this.UPDATE_INTERVAL_IN_MILLISECONDS = setUpdateInterval;
        this.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = setFastestUpdateInterval;
    }

    public RediLocation(Context context, long timeout){
        this.context = context;
        this.timeout = timeout;
    }

    public void startLocation(final RediView.OnLocationListener onLocationListener){

        if (doesUserHavePermission() && checkManifest()) {
            this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            this.mSettingsClient = LocationServices.getSettingsClient(context);

            this.mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // location is received
                    RediLocation.this.mCurrentLocation = locationResult.getLastLocation();
                    onLocationListener.onSuccess(RediLocation.this.mCurrentLocation.getLatitude(), RediLocation.this.mCurrentLocation.getLongitude());
                }
            };

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();


            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.d(TAG, "All location settings are satisfied.");
                            Log.d(TAG, "Started location updates!");
                            //noinspection MissingPermission
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());

                            onLocationListener.onStart();

                            if (timeout > 0) {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        stopLocationUpdates();
                                    }
                                }, timeout);
                            }

                        }
                    })
                    .addOnFailureListener((Activity) context, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.d(TAG, "onFailure: Location settings are not satisfied. Attempting to upgrade location settings");
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult((Activity) context, 2222);
                                    } catch (IntentSender.SendIntentException sie) {
                                        onLocationListener.onFailure(sie.getMessage());
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.";
                                    onLocationListener.onFailure(errorMessage);
                            }

                        }
                    });
        } else
            onLocationListener.onPermissionFailure();

    }

    private void startLocationUpdates() {


    }

    public void stopLocationUpdates() {
        // Removing location updates
        if (mFusedLocationClient != null) {
            mFusedLocationClient
                    .removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.e(TAG, "Location update stopped!");
                        }
                    });
        }
    }

    private boolean doesUserHavePermission() {
        int courseLocation = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineLocation = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        return courseLocation == PackageManager.PERMISSION_GRANTED && fineLocation == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkManifest(){
        return  Util.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) && Util.hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

}
