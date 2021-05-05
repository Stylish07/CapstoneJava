package com.example.capstonejava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.capstonejava.databinding.ActivityLiveDataBinding;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class LiveDataActivity extends AppCompatActivity {

    private ActivityLiveDataBinding binding;

    // callback url + service key
    String callBackUrl = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire?";
    String serviceKey = "serviceKey=4NaBv4lhRmPGISwgpcWKZND8uajFXfEoUExAjER97oWKmchADrfyEjVYZ3EPdkrAnDl1BkTmqskPKNMydZcFIQ%3D%3D";
    String selectedStage1;
    // String selectedStage2;

    String data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveDataBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.stage1, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);

        /*ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.stage2Seoul, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter2);*/

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStage1 = binding.spinner.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /*Log.d("에러", "여기까지 실행1");

        switch (selectedStage1) {
            case "서울":
                // 초기설정임.
                Log.d("에러", "여기까지 실행1.5");
                break;
            case "부산":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Busan, android.R.layout.simple_spinner_item);
                break;
            case "대구":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Daegu, android.R.layout.simple_spinner_item);
                break;
            case "인천":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Incheon, android.R.layout.simple_spinner_item);
                break;
            case "광주":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Kwangju, android.R.layout.simple_spinner_item);
                break;
            case "대전":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Daejeon, android.R.layout.simple_spinner_item);
                break;
            case "울산":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Ulsan, android.R.layout.simple_spinner_item);
                break;
            case "세종":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Sejong, android.R.layout.simple_spinner_item);
                break;
            case "경기":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Gyeonggi, android.R.layout.simple_spinner_item);
                break;
            case "강원":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Gangwon, android.R.layout.simple_spinner_item);
                break;
            case "충북":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Chungbuk, android.R.layout.simple_spinner_item);
                break;
            case "충남":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2chungnam, android.R.layout.simple_spinner_item);
                break;
            case "전북":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Jeonbuk, android.R.layout.simple_spinner_item);
                break;
            case "전남":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Jeonnam, android.R.layout.simple_spinner_item);
                break;
            case "경북":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Kyeongbuk, android.R.layout.simple_spinner_item);
                break;
            case "경남":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Kyeongnam, android.R.layout.simple_spinner_item);
                break;
            case "제주":
                adapter2 = ArrayAdapter.createFromResource(this,
                        R.array.stage2Jeju, android.R.layout.simple_spinner_item);
                break;
            default:
                Log.d("에러", "디폴트값으로 들어감");
        }

        binding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStage2 = binding.spinner2.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/

        binding.btnParsing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        data= getXmlData();//아래 메소드를 호출하여 XML data를 파싱해서 String 객체로 얻어오기

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                binding.textInfo.setText(data); //TextView에 문자열  data 출력
                            }
                        });
                    }
                }).start();
            }
        });
    }

    String getXmlData() {
        StringBuffer buffer=new StringBuffer();

        String queryUrl = "http://apis.data.go.kr/B552657/ErmctInfoInqireService/getEmrrmRltmUsefulSckbdInfoInqire?serviceKey=4NaBv4lhRmPGISwgpcWKZND8uajFXfEoUExAjER97oWKmchADrfyEjVYZ3EPdkrAnDl1BkTmqskPKNMydZcFIQ%3D%3D&STAGE1=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C&STAGE2=%EA%B0%95%EB%82%A8%EA%B5%AC&pageNo=1&numOfRows=100";

        try {
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.

            InputStream is= url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();

                        switch (tag) {
                            case "dutyName":
                                buffer.append("병원 이름: ");
                                xpp.next();
                                buffer.append(xpp.getText());
                                buffer.append("\n");
                                break;
                            case "dutyTel3":
                                buffer.append("병원 전화번호: ");
                                xpp.next();
                                buffer.append(xpp.getText());
                                buffer.append("\n");
                                break;
                            case "hvec":
                                buffer.append("응급실 현황: ");
                                xpp.next();
                                buffer.append(xpp.getText());
                                buffer.append("\n");
                                break;
                            case "hvidate":
                                buffer.append("입력 일시: ");
                                xpp.next();
                                buffer.append(xpp.getText());
                                buffer.append("\n\n\n");
                                break;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = xpp.next();
            }

        }catch (Exception e) {
            buffer.append("오류가 발생했습니다. 인터넷 또는 실행 환경을 점검해 주세요.");
        }

        return buffer.toString();//StringBuffer 문자열 객체 반환
    }
}