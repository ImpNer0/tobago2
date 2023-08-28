package com.example.tabago;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;

import java.util.List;

public class LocationUtility {

    private static List<LatLng> smokingCoordinates;

    // 확인하고자 하는 위치(사용자의 현위치)의 좌표를 넣으면, 마커들 안에 있는지 확인함.
    public static boolean isSmokingZone(LatLng latLng) {
        List<LatLng> smokingCoordinates = LocationUtility.getSmokingCoordinates();
        for (LatLng marker : smokingCoordinates) {
            double distance = calculateDistance(latLng, marker);
            // 일정 거리 이내에 마커가 존재하면 SmokingZone으로 판단
            if (distance < 100) { // 예시로 100 미터 이내로 설정
                return true;
            }
        }
        return false;
    }

    public static double calculateDistance(LatLng point1, LatLng point2) {
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
    }

    // 현 위치를 LatLng 객체로 반환한다.
    public static LatLng getCurrentLocation(Context context) {
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

        return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }


    public static List<LatLng> getSmokingCoordinates(){
        if(smokingCoordinates != null)
            return smokingCoordinates;

        smokingCoordinates = List.of(
                new LatLng(37.210279, 126.975302),
                new LatLng(37.211645, 127.980015),
                new LatLng(37.212269, 127.970126),
                new LatLng(37.390935, 126.956417),
                new LatLng(37.391317, 126.957767),
                new LatLng(37.390935, 126.956417),
                new LatLng(37.398283, 126.952497),
                new LatLng(37.397808, 126.953833),
                new LatLng(37.411986, 126.951515),
                new LatLng(37.551689,127.134950)

        );

        return smokingCoordinates;
    }
}
