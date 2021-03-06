package com.example.capstonejava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AedMapsActivity extends AppCompatActivity implements AutoPermissionsListener {

    SupportMapFragment mapFragment;
    GoogleMap map;
    private final String aedCallBackUrl = "http://apis.data.go.kr/B552657/AEDInfoInqireService/getAedLcinfoInqire";
    private final String serviceKey = "4NaBv4lhRmPGISwgpcWKZND8uajFXfEoUExAjER97oWKmchADrfyEjVYZ3EPdkrAnDl1BkTmqskPKNMydZcFIQ%3D%3D";
    double longitude = 127.085156592737;
    double latitude = 37.4881325624879;

    MarkerOptions myLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aed_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
            }
        });

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<AEDInfo> aedInfoList = getXmlAed();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAedMarker(aedInfoList);
                            }
                        });
                    }
                }).start();
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    List<AEDInfo> getXmlAed() {
        StringBuffer buffer = new StringBuffer();
        String queryUrl = aedCallBackUrl + "?serviceKey=" + serviceKey + "&WGS84_LON=" + longitude + "&WGS84_LAT=" + latitude + "&pageNo=1&numOfRows=20";

        List<AEDInfo> aedInfoList = new ArrayList<>();

        try {
            URL url= new URL(queryUrl);//???????????? ??? ?????? url??? URL ????????? ??????.
            InputStream is= url.openStream(); //url????????? ??????????????? ??????
            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream ???????????? xml ????????????

            xpp.next();
            int eventType = xpp.getEventType();
            String tag = xpp.getName();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG && tag.equals("item")) {
                    AEDInfo aedInfo = new AEDInfo();
                    eventType = xpp.next();
                    tag = xpp.getName();

                    while (!(eventType == XmlPullParser.END_TAG && tag.equals("item"))) {

                        switch (tag) {
                            // xml ???????????? ???????????? ????????? ???????????? ??????
                            case "buildPlace":
                                xpp.next();
                                aedInfo.setBuildPlace(xpp.getText());
                                break;

                            case "clerkTel":
                                xpp.next();
                                aedInfo.setClerkTel(xpp.getText());
                                break;

                            case "distance":
                                xpp.next();
                                aedInfo.setDistance(xpp.getText());
                                break;

                            case "org":
                                xpp.next();
                                aedInfo.setOrg(xpp.getText());
                                break;

                            case "wgs84Lat":
                                xpp.next();
                                aedInfo.setWgs84Lat(Double.parseDouble(xpp.getText()));
                                break;

                            case "wgs84Lon":
                                xpp.next();
                                aedInfo.setWgs84Lon(Double.parseDouble(xpp.getText()));
                                break;

                            default:
                                break;
                        }
                        eventType = xpp.next();
                        tag = xpp.getName();

                        if (eventType == XmlPullParser.TEXT) {
                            eventType = xpp.next();
                            tag = xpp.getName();
                        }

                        if (eventType == XmlPullParser.END_TAG) {
                            eventType = xpp.next();
                            tag = xpp.getName();
                        }
                    }
                    aedInfoList.add(aedInfo);
                }
                eventType = xpp.next();
                tag = xpp.getName();
            }

        }catch (Exception e) {
            buffer.append("????????? ??????????????????. ????????? ?????? ?????? ????????? ????????? ?????????.");
        }
        return aedInfoList;
    }

    public class AEDInfo {
        String buildPlace;
        String clerkTel;
        String distance;
        String org;
        double wgs84Lat;
        double wgs84Lon;

        public String getBuildPlace() {
            return buildPlace;
        }

        public void setBuildPlace(String buildPlace) {
            this.buildPlace = buildPlace;
        }

        public String getClerkTel() {
            return clerkTel;
        }

        public void setClerkTel(String clerkTel) {
            this.clerkTel = clerkTel;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public double getWgs84Lat() {
            return wgs84Lat;
        }

        public void setWgs84Lat(double wgs84Lat) {
            this.wgs84Lat = wgs84Lat;
        }

        public double getWgs84Lon() {
            return wgs84Lon;
        }

        public void setWgs84Lon(double wgs84Lon) {
            this.wgs84Lon = wgs84Lon;
        }
    }

    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                String message = "?????? ?????? -> Latitude : " + latitude + "\nLongitude:" + longitude;
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, gpsListener);

            Toast.makeText(getApplicationContext(), "??? ???????????? ?????????",
                    Toast.LENGTH_SHORT).show();

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            String message = "??? ?????? -> Latitude : "+ latitude + "\nLongitude:"+ longitude;

            showCurrentLocation(latitude, longitude);
        }

        public void onProviderDisabled(String provider) { }

        public void onProviderEnabled(String provider) { }

        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        showMyLocationMarker(curPoint);
    }

    private void showMyLocationMarker(LatLng curPoint) {
        if (myLocationMarker == null) {
            myLocationMarker = new MarkerOptions()
                    .position(curPoint)
                    .title("??? ??????\n")
                    .snippet(" GPS??? ????????? ?????? ")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            map.addMarker(myLocationMarker);
        }
        else {
            myLocationMarker.position(curPoint);
        }
    }

    private void showAedMarker(List<AEDInfo> aedInfoList) {
        for (int i = 0; i < aedInfoList.size(); i++) {
            MarkerOptions aedMakers = new MarkerOptions();
            LatLng latLng = new LatLng(aedInfoList.get(i).getWgs84Lat(), aedInfoList.get(i).getWgs84Lon());
            aedMakers.position(latLng);
            aedMakers.title(aedInfoList.get(i).getOrg());
            aedMakers.snippet(aedInfoList.get(i).getBuildPlace() + ", " + aedInfoList.get(i).getClerkTel() + ".  ????????????: " + aedInfoList.get(i).getDistance() + "km");
            map.addMarker(aedMakers);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, @NotNull String[] permissions) {
        Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, String[] permissions) {
        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }
}