package com.stech.android.app.bluetooth.rgb_controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by chyh1 on 2018-05-11.
 */

public class BluetoothModule {
    private static final String TAG = "BluetoothModule";

    private static final int REQUEST_ENABLE_BT = 2;

    private Activity mActivity;
    private BluetoothAdapter mBluetoothAdapter;

    private Set<BluetoothDevice> mPairedDevices;

    public BluetoothModule(Activity mActivity) {
        this.mActivity = mActivity;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean getDeviceState() {
        Log.d(TAG, "check available");
        if(mBluetoothAdapter == null) {
            Log.d(TAG, "bt not available");
            return false;
        }
        else {
            Log.d(TAG, "bt available");
            return true;
        }
    }

    public boolean isBlutoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public void enableBluetooth() {
        Log.d(TAG, "check enabled");
        if(mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enable now");
        }
        else {
            Log.d(TAG, "enable request");

            Intent getPermission = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(getPermission, REQUEST_ENABLE_BT);
        }
    }

    public void disableBluetooth() {
        if(mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    public void selectDevice() {
        final int mPairedDeviceCount;

        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mPairedDevices.size();

        if(mPairedDeviceCount == 0) {
            Toast.makeText(mActivity, "No paired devices...", Toast.LENGTH_SHORT).show();
        }

        AlertDialog.Builder mAlertDialogBuilder = new AlertDialog.Builder(mActivity);
        mAlertDialogBuilder.setTitle("블루투스 장치 선택");

        List<String> mListDevice = new ArrayList<String>();

        for(BluetoothDevice mBluetoothdevices : mPairedDevices) {
            mListDevice.add(mBluetoothdevices.getName()+ "\n" + mBluetoothdevices.getAddress());
        }
        mListDevice.add("취소");

        final CharSequence[] items = mListDevice.toArray(new CharSequence[mListDevice.size()]);

        mAlertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == mPairedDeviceCount) {
                    Toast.makeText(mActivity, "Canceled by user...", Toast.LENGTH_SHORT).show();
                }
                else {
                    connectToSelectedDevice(items[which].toString().substring(0, items[which].length() - 18));
                }
            }
        });

        mAlertDialogBuilder.setCancelable(false);
        AlertDialog mAlertDialog = mAlertDialogBuilder.create();
        mAlertDialog.show();
    }

    BluetoothDevice getDeviceFromBondedList(String name) {
        mPairedDevices = mBluetoothAdapter.getBondedDevices();

        BluetoothDevice mSelectedDevice = null;

        for(BluetoothDevice mBluetoothDevice : mPairedDevices) {
            if(name.equals(mBluetoothDevice.getName())) {
                mSelectedDevice = mBluetoothDevice;
                break;
            }
        }
        return mSelectedDevice;
    }

    private OutputStream mOutputStream;

    void connectToSelectedDevice(String selectedDeviceName) {
        BluetoothDevice mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        Log.i("Selected Device", selectedDeviceName + " : " + mRemoteDevice);

        try {
            // 소켓 생성
            BluetoothSocket mBluetoothSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            // RFCOMM 채널을 통한 연결
            mBluetoothSocket.connect();
            Toast.makeText(mActivity, "Connected with" + selectedDeviceName + "!", Toast.LENGTH_SHORT).show();
            // 데이터 송수신을 위한 스트림 열기
            mOutputStream = mBluetoothSocket.getOutputStream();
           // mInputStream = mSocket.getInputStream();

            // 데이터 수신 준비
            //beginListenForData();
        }catch(Exception e) {
            // 블루투스 연결 중 오류 발생
           Toast.makeText(mActivity, "Connection error occurred!", Toast.LENGTH_SHORT).show();
        }
    }
    void sendData(String message) {
        String mSendMsg = "<";
        mSendMsg += message + ">";

        try {
            mOutputStream.write(mSendMsg.getBytes());
        }
        catch (Exception e) {
            Toast.makeText(mActivity, "Error occurred while sending message!", Toast.LENGTH_SHORT).show();
        }
    }
}

