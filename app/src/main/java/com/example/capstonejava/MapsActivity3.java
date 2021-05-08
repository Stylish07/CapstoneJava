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
    MarkerOptions hospitalMarker;

    String callBackUri = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytLcinfoInqire";
    String serviceKey = "4NaBv4lhRmPGISwgpcWKZND8uajFXfEoUExAjER97oWKmchADrfyEjVYZ3EPdkrAnDl1BkTmqskPKNMydZcFIQ%3D%3D";

    double latitudeCur;
    double longitudeCur;

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

                // xml로부터 정보 가져오기
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        HospitalInfo[] hospitalInfos = new HospitalInfo[100];


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                // showHospitalsMarker(hospitalInfos);
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
            latitudeCur = location.getLatitude();
            longitudeCur = location.getLongitude();

            String message = "내 위치 -> Latitude : "+ latitudeCur + "\nLongitude:"+ longitudeCur;
            Log.d("Map", message);

            showCurrentLocation(latitudeCur, longitudeCur);
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

    private void showHospitalsMarker(HospitalInfo[] hospitalInfos) {
        int i = 0;
        while (hospitalInfos[i].getDutyName().equals("")) {
            hospitalMarker = new MarkerOptions();
            LatLng hosPosition = new LatLng(hospitalInfos[i].getLatitude(), hospitalInfos[i].getLongitude());
            hospitalMarker.position(hosPosition);
            hospitalMarker.title(hospitalInfos[i].getDutyName());
            hospitalMarker.snippet(hospitalInfos[i].getDutyAddr() + hospitalInfos[i].getDutyTel1());
            map.addMarker(hospitalMarker);
            i++;
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

    HospitalInfo[] getXmlData(HospitalInfo[] hospitalInfos) {
        StringBuffer buffer = new StringBuffer();

        // String queryUrl = callBackUri + "?serviceKey=" + serviceKey + "&WGS84_LON=" + longitudeCur + "&WGS84_LAT=" + latitudeCur + "&pageNo=1&numOfRows=100";

        String queryUrl = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytLcinfoInqire?serviceKey=4NaBv4lhRmPGISwgpcWKZND8uajFXfEoUExAjER97oWKmchADrfyEjVYZ3EPdkrAnDl1BkTmqskPKNMydZcFIQ%3D%3D&WGS84_LON=127.08515659273706&WGS84_LAT=37.488132562487905&pageNo=1&numOfRows=100";

        // HospitalInfo[] hospitalInfos = new HospitalInfo[100]; 함수 바꿔서 주석(삭제)처리

        Log.d("에러", "클래스배열 100칸 생성되었습니다.");

        try {
            URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.

            InputStream is = url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            int i = 0;

            Log.d("에러", "try문 진입완료. 와일문 시작합니다.");

            // 스타트도큐0, 엔드도큐1, 스타트태그2, 엔드태그3, 텍스트4

            while (eventType != XmlPullParser.END_DOCUMENT) {

                Log.d("에러", "와일문 시작 현재 이벤트타입 = " + eventType + "   i값 = " + i);
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();

                        Log.d("에러", "현재 태그는 = " + tag);
                        switch (tag) {
                            case "distance":
                                xpp.next();
                                buffer.append(xpp.getText());
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setDistance(xpp.getText());

                                Log.d("에러", hospitalInfos[i].getDistance());
                                break;
                            case "dutyAddr":
                                xpp.next();
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setDutyAddr(xpp.getText());

                                Log.d("에러", hospitalInfos[i].getDutyAddr());
                                break;
                            case "dutyName":
                                xpp.next();
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setDutyName(xpp.getText());

                                Log.d("에러", hospitalInfos[i].getDutyName());
                                break;
                            case "dutyTel1":
                                xpp.next();
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setDutyTel1(xpp.getText());

                                Log.d("에러", hospitalInfos[i].getDutyTel1());
                                break;
                            case "endTime":
                                xpp.next();
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setEndTime(xpp.getText());

                                Log.d("에러", hospitalInfos[i].getEndTime());
                                break;
                            case "latitude":
                                xpp.next();
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setLatitude(Double.parseDouble(xpp.getText()));

                                break;
                            case "longitude":
                                xpp.next();
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setLongitude(Double.parseDouble(xpp.getText()));

                                break;
                            case "startTime":
                                xpp.next();
                                hospitalInfos[i] = new HospitalInfo();
                                hospitalInfos[i].setStartTime(xpp.getText());

                                Log.d("에러", hospitalInfos[i].getStartTime());
                                break;
                            default:
                                Log.d("에러", "찾는 태그가 아닙니다. 현재 태그 = " + tag);
                                break;
                        }

                        Log.d("에러", "스타트태그 탈출합니다.");
                        break;

                    case XmlPullParser.TEXT:
                        Log.d("에러", "텍스트 부분 브레이크.");
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();
                        if (tag.equals("item")) {
                            i++;
                        }
                        Log.d("에러", "엔드태그. i++합니다. i값 = " + i + "태그이름: /" + tag);
                        break;
                }
                eventType = xpp.next();
            }

            Log.d("에러", "와일문 끝난 직후입니다.");
        }
        catch (Exception e) {
            buffer.append("오류가 발생했습니다. 인터넷 또는 실행 환경을 점검해 주세요.");
        }
        Log.d("에러", "리턴 직전입니다.");

        return hospitalInfos;
    }

    class HospitalInfo {
        String dutyName = "";
        String startTime = "";
        String endTime = "";
        String dutyAddr = "";
        String distance = "";
        String dutyTel1 = "";
        double latitude = 0.0;
        double longitude = 0.0;

        public String getDutyName() {
            return dutyName;
        }

        public void setDutyName(String dutyName) {
            this.dutyName = dutyName;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getDutyAddr() {
            return dutyAddr;
        }

        public void setDutyAddr(String dutyAddr) {
            this.dutyAddr = dutyAddr;
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