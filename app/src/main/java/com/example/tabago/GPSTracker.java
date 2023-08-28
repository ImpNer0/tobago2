package com.example.tabago;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

public class GPSTracker {

    private final Context mContext;
    private LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        this.locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public void requestLocationUpdates(LocationListener locationListener) {
        try {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopUsingGPS(LocationListener locationListener) {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getLastKnownLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
