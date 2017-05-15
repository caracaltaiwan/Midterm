package com.example.midterm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String
            urlString = "http://opendataap2.e-land.gov.tw/resource/files/2017-04-13/e868085a0c9a6955a34b7cc9953e138d.json",
            respose;
    HttpURLConnection connection = null;
    String responseString;
    JSONObject jsonObject = null;
    TextView v;
    Button btn;
    Spinner spn;
    String[] location, address, type, phone;
    double Latitude;
    double Longitude;
    double[]
            latitude, longitude;
    ArrayAdapter<String> locationList;
    int potion;
    boolean first = true, second = true;
    HashMap<Marker, Integer> position = new HashMap<>();

    SQLiteDatabase db, test;//資料庫名稱
    SQLiteOH sqLiteOH;//資料庫所在位置
    SQLite sql;//資料庫功能所在位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        v = (TextView) findViewById(R.id.textView);
        btn = (Button) findViewById(R.id.button);
        spn = (Spinner) findViewById(R.id.spinner);

        //SQLite建構子
        sqLiteOH = new SQLiteOH(getApplicationContext(), "tablespace", null, 1);
        sql = new SQLite(getApplicationContext(), sqLiteOH);
        //sql.createTable(1, "name_location");

        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
                    case 888:
                        // 重繪UI
                        findmap();
                        break;
                    case 111:
                        Toast.makeText(MapsActivity.this, "請稍等，正在加載詳細位置。", Toast.LENGTH_LONG).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        System.out.printf("!!!");
        System.out.print(System.currentTimeMillis());
        //region 第一執行序
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(1500);
            connection.setConnectTimeout(1500);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
            connection.setInstanceFollowRedirects(true);

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        respose = connection.getResponseMessage();//檢查有沒有收到資料
                        if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                            // 讀取網頁內容
                            InputStream inputStream = connection.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                            String tempStr;
                            StringBuffer stringBuffer = new StringBuffer();

                            while ((tempStr = bufferedReader.readLine()) != null) {
                                stringBuffer.append(tempStr);
                            }

                            bufferedReader.close();
                            inputStream.close();

                            // 取得網頁內容類型
                            String mime = connection.getContentType();
                            boolean isMediaStream = false;

                            // 判斷是否為串流檔案
                            if (mime.indexOf("audio") == 0 || mime.indexOf("video") == 0) {
                                isMediaStream = true;
                            }

                            // 網頁內容字串
                            responseString = stringBuffer.toString();
                            //run的結束訊號
                            Message m = new Message();// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
                            m.what = 888;
                            handler.sendMessage(m);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        connection.disconnect();
                    }

                }
            });
            th.start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            second = false;
        }
        //endregion
        //region 第二執行序註解中
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < location.length; i++) {
                    Geocoder geoCoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    List<Address> addressLocation = null;
                    try {
                        addressLocation = geoCoder.getFromLocationName(location[i], 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    latitude[i] = addressLocation.get(0).getLatitude();
                    longitude[i] = addressLocation.get(0).getLongitude();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude, Longitude), 16));
                }
            }
        });
        if (!second) {
            //th.start();
        }
        //endregion
        //region 按鍵監聽
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!first) {
                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:24.746, 121.746?z=9&q=" + Uri.encode(jsonObject.getString("點位名稱"))));//z(zoom) 調大小有動畫 無意義的功能/*記得*/
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:24.746, 121.746?z=9&q=" + Uri.encode(location[potion])));
                    startActivity(intent);
                } else {
                    Message mm = new Message();// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
                    mm.what = 111;
                    handler.sendMessage(mm);
                    btn.setText("走吧!");//Toast.makeText(MapsActivity.this, "請稍等，正在加載詳細位置。", Toast.LENGTH_LONG).show();
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(MapsActivity.this, "你選的是" + location[position], Toast.LENGTH_SHORT).show();
                            Geocoder geoCoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                            List<Address> addressLocation = null;
                            try {
                                addressLocation = geoCoder.getFromLocationName(location[position], 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(!addressLocation.isEmpty()){
                                Latitude = addressLocation.get(0).getLatitude();
                                Longitude = addressLocation.get(0).getLongitude();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude,Longitude),16));
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    for(int i = 0; i < location.length; i++){
                        Geocoder geoCoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                        List<Address> addressLocation = null;
                        try {
                            addressLocation = geoCoder.getFromLocationName(location[i], 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!addressLocation.isEmpty()){
                            Latitude = addressLocation.get(0).getLatitude();
                            Longitude = addressLocation.get(0).getLongitude();
                            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(addressLocation.get(0).getLatitude(),addressLocation.get(0).getLongitude())).
                                    title(location[i]).
                                    snippet(type[i]+"\n"+address[i]));
                            position.put(m ,i);
                        }
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(24.745332, 121.745053),14));
                }

                first = false;
            }
        });
        //endregion



    }

    public void findmap(){
        StringBuffer sf = new StringBuffer();
        try {
            JSONArray jsonArray = new JSONArray(responseString);
            location = new String[jsonArray.length()];
            address = new String[jsonArray.length()];
            type = new String[jsonArray.length()];
            phone = new String [jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                sf.append(jsonObject.getString("點位名稱"));
                location[i] = jsonObject.getString("點位名稱");
                address[i] = jsonObject.getString("地址");
                type[i] = jsonObject.getString("類型");
                phone[i] = jsonObject.getString("服務台電話1");
            }
            locationList = new ArrayAdapter<>(MapsActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    location);
            spn.setAdapter(locationList);
            StringBuffer sff = new StringBuffer();
            for (int i = 0; i < location.length; i++) {
                sff.append(location[i]);
                //sql.insertTable(0,location[i]);
            }
            v.setText(sff.toString());//背景

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            //v.setText(responseString);
            Geocoder geoCoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            latitude = new double[location.length];
            longitude = new double[location.length];
            try {
                for(int i=0;i<location.length;i++) {
                    List<Address> addressLocation = geoCoder.getFromLocationName(String.valueOf(jsonObject.get("點位名稱")), 1);
                    latitude[i] = addressLocation.get(0).getLatitude();
                    longitude[i] = addressLocation.get(0).getLongitude();
                    //mMap.addMarker(new MarkerOptions().position(new LatLng(addressLocation.get(0).getLatitude(),addressLocation.get(0).getLongitude())).snippet("你準備好了嗎?我聽不到回答!"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(24.745332, 121.745053);
        mMap.addMarker(new MarkerOptions().position(sydney).title("你今天靠北工程師了嗎?"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.setAlpha(0.7f);
                if(position.get(marker)!=null){
                    potion = position.get(marker);
                }
                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // Retrieve the data from the marker.
                Integer clickCount = (Integer) marker.getTag();

                // Check if a click count was set, then display the click count.
                if (clickCount == null) {
                    clickCount = 1;
                    marker.setTag(clickCount);
                    if (position.get(marker)!=null) {
                        marker.setSnippet("地址 : " + address[position.get(marker)]);
                    }
                } else if (clickCount == 1) {
                    clickCount = clickCount + 1;
                    marker.setTag(clickCount);
                    if (position.get(marker)!=null) {
                        marker.setSnippet("服務電話 : " + phone[position.get(marker)]);
                    }
                } else if (clickCount == 2) {
                    clickCount = 0;
                    clickCount = clickCount + 1;
                    marker.setTag(clickCount);
                    if (position.get(marker)!=null) {
                        marker.setSnippet("地址 : " + address[position.get(marker)]);
                    }
                }

                marker.showInfoWindow();
            }
        });

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }
        });
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                this,R.raw.style_json);
        mMap.setMapStyle(mapStyleOptions);
    }
}
