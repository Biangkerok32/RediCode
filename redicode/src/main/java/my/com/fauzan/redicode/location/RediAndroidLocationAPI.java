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

import my.com.fauzan.redicode.RediView;
import my.com.fauzan.redicode.Util;

public class RediAndroidLocationAPI {

    private Context context;

    public RediAndroidLocationAPI(Context context){
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public void startLocation(final RediView.OnLocationListener onLocationListener) {

        if (doesUserHavePermission() && checkManifest()) {
            onLocationListener.onStart();

            final LocationManager locationManager = (LocationManager)  context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            final String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                onLocationListener.onSuccess(latitude,longitude);
            } else{
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        if (location.getLatitude() > 0.0 && location.getLongitude()> 0.0)
                            onLocationListener.onSuccess(location.getLatitude(),location.getLongitude());
                        else
                            onLocationListener.onFailure("Location not found");

                        // Stop location update
                        locationManager.removeUpdates((LocationListener) context);
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
                });
                //This is what you need:

                // locationManager.requestLocationUpdates(bestProvider, 1000, 0, context);
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
}
