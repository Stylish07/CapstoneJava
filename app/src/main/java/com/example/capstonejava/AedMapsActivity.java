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

public class AedMapsActivity extends AppCompatActivity implements AutoPermissionsListener {

    SupportMapFragment mapFragment;
    GoogleMap map;
    private final String aedCallBackUrl = "http://apis.data.go.kr/B552657/AEDInfoInqireService/getAedLcinfoInqire";
    private final String serviceKey = "4NaBv4lhRmPGISwgpcWKZND8uajFXfEoUExAjER97oWKmchADrfyEjVYZ3EPdkrAnDl1BkTmqskPKNMydZcFIQ%3D%3D";
    double longitude = 127.085156592737;
    double latitude = 37.4881325624879;

    MarkerOptions myLocationMarker;
    MarkerOptions aedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aed_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d("Map", "지도 준비됨.");
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
                        // 함수로 파싱해오기
                        AEDInfo[] aedInfos = getXmlAed();
                        // Log.d("에러", aedInfos[0].getBuildAddress());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAedMarker(aedInfos);
                            }
                        });
                    }
                }).start();
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    AEDInfo[] getXmlAed() {
        StringBuffer buffer = new StringBuffer();

        String queryUrl = aedCallBackUrl + "?serviceKey=" + serviceKey + "&WGS84_LON=" + longitude + "&WGS84_LAT=" + latitude + "&pageNo=1&numOfRows=10";

        AEDInfo[] aedInfos = new AEDInfo[10];

        try {
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.

            InputStream is= url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;
            int i = 0;

            xpp.next();
            int eventType= xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                aedInfos[i] = new AEDInfo();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();

                        switch (tag) {
                            case "buildAddress":
                                xpp.next();
                                aedInfos[i].setBuildAddress(xpp.getText());
                                break;

                            case "buildPlace":
                                xpp.next();
                                aedInfos[i].setBuildPlace(xpp.getText());
                                break;

                            case "clerkTel":
                                xpp.next();
                                aedInfos[i].setClerkTel(xpp.getText());
                                break;

                            case "distance":
                                xpp.next();
                                aedInfos[i].setDistance(xpp.getText());
                                break;

                            case "org":
                                xpp.next();
                                aedInfos[i].setOrg(xpp.getText());
                                break;

                            case "wgs84Lat":
                                xpp.next();
                                aedInfos[i].setWgs84Lat(Double.parseDouble(xpp.getText()));
                                break;

                            case "wgs84Lon":
                                xpp.next();
                                aedInfos[i].setWgs84Lon(Double.parseDouble(xpp.getText()));
                                break;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        if (xpp.getName().equals("item")) {
                            i++;
                        }
                        break;
                }

                eventType = xpp.next();
            }

        }catch (Exception e) {
            buffer.append("오류가 발생했습니다. 인터넷 또는 실행 환경을 점검해 주세요.");
        }

        return aedInfos;
    }

    public class AEDInfo {
        String buildAddress;
        String buildPlace;
        String clerkTel;
        String distance;
        String org;
        double wgs84Lat;
        double wgs84Lon;

        public String getBuildAddress() {
            return buildAddress;
        }

        public void setBuildAddress(String buildAddress) {
            this.buildAddress = buildAddress;
        }

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
                String message = "최근 위치 -> Latitude : " + latitude + "\nLongitude:" + longitude;

                Log.d("Map", message);
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, gpsListener);

            Toast.makeText(getApplicationContext(), "내 위치확인 요청함",
                    Toast.LENGTH_SHORT).show();

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            String message = "내 위치 -> Latitude : "+ latitude + "\nLongitude:"+ longitude;
            Log.d("Map", message);

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
            myLocationMarker = new MarkerOptions();
            myLocationMarker.position(curPoint);
            myLocationMarker.title("내 위치\n");
            myLocationMarker.snippet(" GPS로 확인한 위치 ");
            map.addMarker(myLocationMarker);
        }
        else {
            myLocationMarker.position(curPoint);
        }
    }

    private void showAedMarker(AEDInfo[] aedInfos) {
        aedMarker = new MarkerOptions();
        LatLng aedPoint = new LatLng(aedInfos[0].getWgs84Lat(), aedInfos[0].getWgs84Lon());
        aedMarker.position(aedPoint);
        aedMarker.title(aedInfos[0].getOrg());
        aedMarker.snippet(aedInfos[0].getBuildAddress() + "  " + aedInfos[0].getBuildPlace());
        map.addMarker(aedMarker);
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