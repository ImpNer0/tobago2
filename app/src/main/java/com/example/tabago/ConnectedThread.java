package com.example.tabago;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;
import android.location.Location;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLOutput;
import java.util.List;


public class ConnectedThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handler;
    private List<Marker> lstMarkers; // 마커 리스트

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.handler = handler;
        this.lstMarkers = lstMarkers; // 마커 리스트 전달


        // 임시 객체를 사용하여 입력 및 출력 스트림 가져오기. 멤버 스트림은 최종적이기 때문에 임시 객체를 사용합니다.
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];  // 스트림을 위한 버퍼 저장소
        int bytes; // 읽어들인 바이트 수
        // 예외가 발생할 때까지 입력 스트림을 계속 듣기
        while (true) {
            try {
                bytes = mmInStream.available();

                // 읽어온 데이터를 문자열로 변환
                byte[] receivedData = new byte[bytes];
                if (bytes != 0) {
                    mmInStream.read(receivedData, 0, bytes);
                    String receivedStr = new String(receivedData, 0, bytes);
                    System.out.println(receivedStr);
                    handleMessageFromConnectedThread(receivedStr);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessageFromConnectedThread(String receivedData) {
        if (receivedData.contains("count")) {
            Message msg = handler.obtainMessage(MainActivity.INCREASE_COUNT_AND_GRAPH);
            handler.sendMessage(msg);
        }
    }
}



