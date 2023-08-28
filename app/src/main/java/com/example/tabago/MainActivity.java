package com.example.tabago;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //TODO : 나중에 그 코드 하나 만들어서 셧다운 시켜달라고 하기.

    public static String SHUTDOWN_CODE = "limit";
    private LinearLayout move2LinearLayout;
    private LinearLayout move3LinearLayout;

    private Button increaseButton;
    private int currentCount = 0;
    private int totalCount = 10; // 총 횟수 설정
    private List<Float> weeklyData;

    private BarChart dailyChart;
    private List<BarEntry> dailyEntries;
    private List<String> dailyLabels;

    //여기서부터 2023년 8월 17일
    TextView textStatus;
    Button btnParied, btnSend;
    ListView listView;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread;

    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    public static final int INCREASE_COUNT_AND_GRAPH = 1; // 예시 상수 값
    public static final int STOP_SMOKING = 2;


    private Handler increaseHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == INCREASE_COUNT_AND_GRAPH) {
                increaseCountAndGraph();
                return true;
            }
            return false;
        }
    });


    private Handler noSmokingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == STOP_SMOKING){
                LatLng curLatLng = LocationUtility.getCurrentLocation(getApplicationContext());
                boolean isSmokingZone = LocationUtility.isSmokingZone(curLatLng);
                if ( !isSmokingZone && connectedThread!=null){
                    connectedThread.write(SHUTDOWN_CODE);
                }
                return true;
            }
            return false;
        }
    }

    );

    private class myOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

            textStatus.setText("try...");

            final String name = btArrayAdapter.getItem(position); // get name
            final String address = deviceAddressArray.get(position); // get address
            boolean flag = true;

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try {
                btSocket = createBluetoothSocket(device);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
                    return;
                }
                btSocket.connect();
            } catch (IOException e) {
                flag = false;
                textStatus.setText("connection failed!");
                e.printStackTrace();
            }

            if (flag) {
                textStatus.setText("connected to " + name);
                connectedThread = new ConnectedThread(btSocket, increaseHandler);
                connectedThread.start();
            }
        }
    }//이 부분까지 추가된 블루투스 코드

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        move2LinearLayout = findViewById(R.id.move2);
        move3LinearLayout = findViewById(R.id.move3);
        dailyChart = findViewById(R.id.daily_chart);

        weeklyData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weeklyData.add(0f);
        }

        // 일간 그래프 데이터 초기화
        dailyEntries = new ArrayList<>();
        dailyLabels = getDailyLabels();

        // UI 업데이트
        setupDailyChart();

        // Get permission(여기부터 뭐하는 코드인지 모르겠음-블루투스 코드)
        String[] permission_list = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // variables
        textStatus = (TextView) findViewById(R.id.text_status);
        btnParied = (Button) findViewById(R.id.btn_paired);
        btnSend = (Button) findViewById(R.id.btn_send);
        listView = (ListView) findViewById(R.id.listview);

        // Show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        listView.setOnItemClickListener(new myOnItemClickListener());
//여기까지 뭐하는 코드인지 모르겠음
        move2LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SubActivity2.class);

                // 주간 그래프 데이터를 문자열 배열로 변환하여 넘김
                String[] weeklyDataArray = floatListToStringArray(weeklyData);
                String[] weeklyLabelsArray = getWeeklyLabels().toArray(new String[0]);
                intent.putExtra("weeklyData", weeklyDataArray);
                intent.putExtra("weeklyLabels", weeklyLabelsArray);

                // 일간 그래프 데이터를 문자열 배열로 변환하여 넘김
                String[] dailyEntriesData = barEntryListToStringArray(dailyEntries);
                String[] dailyLabelsData = dailyLabels.toArray(new String[0]);
                intent.putExtra("dailyEntries", dailyEntriesData);
                intent.putExtra("dailyLabels", dailyLabelsData);

                startActivity(intent);
            }
        });
        Button btnMove3 = findViewById(R.id.btn_move3);
        btnMove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // activity_sub3.xml로 이동
                startActivity(new Intent(MainActivity.this, SubActivity3.class));
            }
        });

        // 2PAGE 버튼 클릭 이벤트 처리
        Button page2Button = findViewById(R.id.btn_move2);
        page2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity2.class);

                // 주간 그래프 데이터를 문자열 배열로 변환하여 넘김
                String[] weeklyDataArray = floatListToStringArray(weeklyData);
                String[] weeklyLabelsArray = getWeeklyLabels().toArray(new String[0]);
                intent.putExtra("weeklyData", weeklyDataArray);
                intent.putExtra("weeklyLabels", weeklyLabelsArray);

                // 일간 그래프 데이터를 문자열 배열로 변환하여 넘김
                String[] dailyEntriesData = barEntryListToStringArray(dailyEntries);
                String[] dailyLabelsData = dailyLabels.toArray(new String[0]);
                intent.putExtra("dailyEntries", dailyEntriesData);
                intent.putExtra("dailyLabels", dailyLabelsData);

                startActivity(intent);
            }
        });

        Button setCountButton = findViewById(R.id.setCount);
        EditText userSetCountEditText = findViewById(R.id.userSetCountEditText);
        Button userSetCountButton = findViewById(R.id.userSetCountButton);

        setCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userSetCountEditText.getVisibility() == View.GONE) {
                    userSetCountEditText.setVisibility(View.VISIBLE); // 보이기
                    userSetCountButton.setVisibility(View.VISIBLE); // 보이기
                } else {
                    userSetCountEditText.setVisibility(View.GONE); // 숨기기
                    userSetCountButton.setVisibility(View.GONE); // 숨기기
                }
            }
        });

        userSetCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 횟수 설정 버튼이 클릭되었을 때의 동작 추가
                String userSetCountStr = userSetCountEditText.getText().toString();
                if (!userSetCountStr.isEmpty()) {
                    int userSetCount = Integer.parseInt(userSetCountStr);
                    // SharedPreferences를 사용하여 설정값 저장
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("UserSetCount", userSetCount);
                    editor.apply();

                    // 수정된 설정값을 화면에 반영
                    totalCount = userSetCount;
                    TextView countTextView = findViewById(R.id.countTextView);
                    countTextView.setText("현재횟수:0 / " + userSetCount);

                }
            }
        });
    }

    public void increaseCountAndGraph() {
        // SharedPreferences에서 사용자 설정 값을 가져옴
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userSetCount = sharedPreferences.getInt("UserSetCount", 0);

        if (currentCount >= userSetCount) {
            if (connectedThread != null) {
                connectedThread.write(SHUTDOWN_CODE); // "limit" 문자열 전송
            }
            return; // 설정된 값 이상이면 더 이상 증가하지 않음
        }
        // 현재 시간 및 요일을 가져옴
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        // 주간 그래프 데이터 업데이트
        int weeklyIndex = currentDayOfWeek - 1;
        float weeklyValue = weeklyData.get(weeklyIndex) + 1;
        weeklyData.set(weeklyIndex, weeklyValue);

        // 일간 그래프 데이터 업데이트
        dailyEntries.get(currentHour).setY(dailyEntries.get(currentHour).getY() + 1);

        // UI 업데이트
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentCount++; // 총 횟수 증가

                TextView countTextView = findViewById(R.id.countTextView);
                countTextView.setText("현재 횟수: " + currentCount + "/ " + userSetCount);
                // 일일 그래프 업데이트
                updateDailyChart();
            }
        });
    }


    public void onClickButtonPaired(View view) {
        btArrayAdapter.clear();
        if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
            deviceAddressArray.clear();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            return;
        }
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
    }//아마 페어링 시키는 코드일듯

    public void onClickButtonSearch(View view) {
        // Check if the device is already discovering
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 4);
            return;
        }
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        } else {
            if (btAdapter.isEnabled()) {
                btAdapter.startDiscovery();
                btArrayAdapter.clear();
                if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                    deviceAddressArray.clear();
                }
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Send string "limit"
    public void onClickButtonSend(View view) {
        if (connectedThread != null) {
            connectedThread.write("limit");
        }
    }



    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
             if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
                btArrayAdapter.notifyDataSetChanged();

                // 수정: MainActivity.this를 이용하여 권한 체크
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
    private String[] barEntryListToStringArray(List<BarEntry> data) {
        String[] dataArray = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            BarEntry entry = data.get(i);
            dataArray[i] = entry.getX() + "," + entry.getY();
        }
        return dataArray;
    }

    private List<String> getWeeklyLabels() {
        List<String> weeklyLabels = new ArrayList<>();
        weeklyLabels.add("일");
        weeklyLabels.add("월");
        weeklyLabels.add("화");
        weeklyLabels.add("수");
        weeklyLabels.add("목");
        weeklyLabels.add("금");
        weeklyLabels.add("토");
        return weeklyLabels;
    }

    private List<String> getDailyLabels() {
        List<String> dailyLabels = new ArrayList<>();
        dailyLabels.add("00시");
        dailyLabels.add("01시");
        dailyLabels.add("02시");
        dailyLabels.add("03시");
        dailyLabels.add("04시");
        dailyLabels.add("05시");
        dailyLabels.add("06시");
        dailyLabels.add("07시");
        dailyLabels.add("08시");
        dailyLabels.add("09시");
        dailyLabels.add("10시");
        dailyLabels.add("11시");
        dailyLabels.add("12시");
        dailyLabels.add("13시");
        dailyLabels.add("14시");
        dailyLabels.add("15시");
        dailyLabels.add("16시");
        dailyLabels.add("17시");
        dailyLabels.add("18시");
        dailyLabels.add("19시");
        dailyLabels.add("20시");
        dailyLabels.add("21시");
        dailyLabels.add("22시");
        dailyLabels.add("23시");
        return dailyLabels;
    }

    private String[] floatListToStringArray(List<Float> data) {
        String[] dataArray = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dataArray[i] = String.valueOf(data.get(i));
        }
        return dataArray;
    }

    private void setupDailyChart() {
        XAxis xAxis = dailyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dailyLabels));

        YAxis leftAxis = dailyChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(10f); // 최대값을 10으로 설정

        dailyChart.getAxisRight().setEnabled(false);
        dailyChart.getDescription().setEnabled(false);

        dailyEntries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            dailyEntries.add(new BarEntry(i, 0f));
        }

        BarDataSet barDataSet = new BarDataSet(dailyEntries, "일일 담배 횟수");
        barDataSet.setColor(Color.rgb(102, 204, 255));
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);

        dailyChart.setData(barData);
        dailyChart.invalidate();
    }

    private void updateDailyChart() {
        XAxis xAxis = dailyChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dailyLabels));

        float maxDailyValue = 0f; // Initialize the maximum value as 0
        for (BarEntry entry : dailyEntries) {
            float yValue = entry.getY();
            if (yValue > maxDailyValue) {
                maxDailyValue = yValue; // Find the maximum value in the daily entries
            }
        }

        YAxis leftAxis = dailyChart.getAxisLeft();
        leftAxis.setAxisMaximum(maxDailyValue + 1); // Set the maximum value for the Y-axis

        BarDataSet barDataSet = new BarDataSet(dailyEntries, "일일 담배 횟수");
        barDataSet.setColor(Color.rgb(102, 204, 255));
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);

        dailyChart.setData(barData);
        dailyChart.notifyDataSetChanged();
        dailyChart.invalidate();
    }
}
