package com.example.tabago;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.List;

public class LocationUtility {
    public static boolean isSmokingZone(Activity activity, SubActivity3 subActivity3) {
        List<Marker> lstMarkers = subActivity3.getLstMarkers(); // lstMarkers 변수에 접근
        FusedLocationSource fusedLocationSource = new FusedLocationSource(activity, SubActivity3.LOCATION_PERMISSION_REQUEST_CODE);
        LatLng currentLocation = new LatLng(fusedLocationSource.getLastLocation().getLatitude(),
                fusedLocationSource.getLastLocation().getLongitude());

        for (Marker marker : lstMarkers) {
            LatLng markerLocation = marker.getPosition();
            double distance = calculateDistance(currentLocation, markerLocation);

            // 일정 거리 이내에 마커가 존재하면 SmokingZone으로 판단
            if (distance < 100) { // 예시로 100 미터 이내로 설정
                return true;
            }
        }

        return false;
    }

    static double calculateDistance(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;

        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515 * 1.609344 * 1000; // 거리를 미터로 변환

        return dist;
    } public Location getCurrentLocation(Context context) {
        GPSTracker gpsTracker = new GPSTracker(context);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 위치가 변경될 때 호출됩니다.
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
        };
        gpsTracker.requestLocationUpdates(locationListener);

        // GPS 위치 정보 가져오기
        Location currentLocation = gpsTracker.getLastKnownLocation();

        // GPS 위치 추적 중지
        gpsTracker.stopUsingGPS(locationListener);

        return currentLocation;
    }

}
