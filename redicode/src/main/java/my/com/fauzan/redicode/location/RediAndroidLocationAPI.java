package my.com.fauzan.redicode.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import my.com.fauzan.redicode.RediView;
import my.com.fauzan.redicode.Util;

public class RediAndroidLocationAPI implements LocationListener{

    private Context context;
    private Handler handler;
    private Runnable runnable;
    private long timeout = 30000;
    private RediView.OnLocationListener onLocationListener;
    private LocationManager locationManager;

    public RediAndroidLocationAPI(Context context, RediView.OnLocationListener mOnLocationListener){
        this.context = context;
        this.onLocationListener = mOnLocationListener;

        startLocation();

    }

    public RediAndroidLocationAPI(Context context, long timeout, RediView.OnLocationListener mOnLocationListener){
        this.context = context;
        this.onLocationListener = mOnLocationListener;
        this.timeout = timeout;

        startLocation();
    }

    @SuppressLint("MissingPermission")
    private void startLocation() {

        if (doesUserHavePermission() && checkManifest()) {
            onLocationListener.onStart();

            locationManager = (LocationManager)  context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            final String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                onLocationListener.onSuccess(latitude,longitude);
            } else {
                handler = new Handler();    // android.os.Handler
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        locationManager.removeUpdates(RediAndroidLocationAPI.this);
                        onLocationListener.onFailure("Location not found");
                    }
                };
                handler.postDelayed(runnable, timeout);
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        } else {
            onLocationListener.onPermissionFailure();
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

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        if (location.getLatitude() > 0.0 && location.getLongitude()> 0.0)
            onLocationListener.onSuccess(location.getLatitude(),location.getLongitude());
        else
            onLocationListener.onFailure("Location not found");

        // Stop location update
        locationManager.removeUpdates((LocationListener) context);
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
