/*이 코드는 지도에 주변 응급실 띄우는거 하는건데
* 하다가 막혀서 일단 내비두고 AED 띄우는 거부터 하는중
* 원리는 똑같으니 해결되면 같이 해결될 것 같음*/

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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity3 extends AppCompatActivity implements AutoPermissionsListener {

    SupportMapFragment mapFragment;
    GoogleMap map;
    MarkerOptions myLocationMarker;

    String callBackUri = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytLcinfoInqire";
    String serviceKey = "4NaBv4lhRmPGISwgpcWKZND8uajFXfEoUExAjER97oWKmchADrfyEjVYZ3EPdkrAnDl1BkTmqskPKNMydZcFIQ%3D%3D";

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps3);

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
                        List<HospitalInfo> hospitalInfoList = getXmlData();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showHospitalsMarker(hospitalInfoList);
                            }
                        });
                    }
                }).start();
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
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
            myLocationMarker = new MarkerOptions()
                    .position(curPoint)
                    .title("내 위치\n")
                    .snippet(" GPS로 확인한 위치 ")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            map.addMarker(myLocationMarker);
        }
        else {
            myLocationMarker.position(curPoint);
        }
    }

    private void showHospitalsMarker(List<HospitalInfo> hospitalInfoList) {
        for (int i = 0; i < hospitalInfoList.size(); i++) {
            MarkerOptions aedMakers = new MarkerOptions();
            LatLng latLng = new LatLng(hospitalInfoList.get(i).getLatitude(), hospitalInfoList.get(i).getLongitude());
            aedMakers.position(latLng);
            aedMakers.title(hospitalInfoList.get(i).getDutyName());
            aedMakers.snippet(hospitalInfoList.get(i).getDutyTel1() + "거리: " + hospitalInfoList.get(i).getDistance());
            map.addMarker(aedMakers);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, String[] permissions) {
        Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, String[] permissions) {
        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    List<HospitalInfo> getXmlData() {
        StringBuffer buffer = new StringBuffer();
        String queryUrl = callBackUri + "?serviceKey=" + serviceKey + "&WGS84_LON=" + longitude + "&WGS84_LAT=" + latitude + "&pageNo=1&numOfRows=20";

        List<HospitalInfo> hospitalInfoList = new ArrayList<>();

        try {
            URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is = url.openStream(); //url위치로 입력스트림 연결
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            xpp.next();
            int eventType = xpp.getEventType();
            String tag = xpp.getName();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && tag.equals("item")) {
                    HospitalInfo hospitalInfo = new HospitalInfo();
                    eventType = xpp.next();
                    tag = xpp.getName();

                    while (!(eventType == XmlPullParser.END_TAG && tag.equals("item"))) {
                        switch (tag) {
                            case "dutyName":
                                xpp.next();
                                hospitalInfo.setDutyName(xpp.getText());
                                break;

                            case "distance":
                                xpp.next();
                                hospitalInfo.setDistance(xpp.getText());
                                break;

                            case "dutyTel1":
                                xpp.next();
                                hospitalInfo.setDutyTel1(xpp.getText());
                                break;

                            case "latitude":
                                xpp.next();
                                hospitalInfo.setLatitude(Double.parseDouble(xpp.getText()));
                                break;

                            case "longitude":
                                xpp.next();
                                hospitalInfo.setLongitude(Double.parseDouble(xpp.getText()));
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
                    hospitalInfoList.add(hospitalInfo);
                }
                eventType = xpp.next();
                tag = xpp.getName();
            }
        }
        catch (Exception e) {
            buffer.append("오류가 발생했습니다. 인터넷 또는 실행 환경을 점검해 주세요.");
        }
        return hospitalInfoList;
    }

    class HospitalInfo {
        String dutyName;
        String distance;
        String dutyTel1;
        double latitude;
        double longitude;

        public String getDutyName() {
            return dutyName;
        }

        public void setDutyName(String dutyName) {
            this.dutyName = dutyName;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public void setDutyTel1(String dutyTel1) {
            this.dutyTel1 = dutyTel1;
        }

        public String getDutyTel1() {
            return dutyTel1;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}