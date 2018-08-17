package com.stech.android.app.bluetooth.rgb_controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import yuku.ambilwarna.AmbilWarnaDialog;

public class Main extends Activity implements SeekBar.OnSeekBarChangeListener, TextWatcher, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int REQUEST_ENABLE_BT = 2;

    public int mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE;

    private FrameLayout mColorView;
    private AmbilWarnaDialog mColorPicker;

    private Button mBtnSendColorCode, mBtnGetBTPermission, mBtnSelectDevice;
    private SeekBar mController_RED, mController_GREEN, mController_BLUE;
    private EditText mColorCodeEditor;
    private TextView mTextValue_RED, mTextValue_GREEN, mTextValue_BLUE;
    private Switch mMasterSwitch;

    private boolean isSeekbarTouched, isBlutoothEnabled, isBluetoothFirstStart;;

    private ColorCodeConversion mColorCodeConversion;
    private BluetoothModule mBluetoothModule = null;

    private Timer mTimer = null;
    private TimerTask mTask = null;
    private  Handler mHandler = null;

    void init() {
        isSeekbarTouched = false;
        isBlutoothEnabled = false;
        isBluetoothFirstStart = true;

        mColorCodeConversion = new ColorCodeConversion();

        if (mBluetoothModule == null)
            mBluetoothModule = new BluetoothModule(this);

        mColorView = (FrameLayout) findViewById(R.id.colorViewer);
        mController_RED = (SeekBar) findViewById(R.id.ctr_R);
        mController_GREEN = (SeekBar) findViewById(R.id.ctr_G);
        mController_BLUE = (SeekBar) findViewById(R.id.ctr_B);
        mColorCodeEditor = (EditText) findViewById(R.id.inputColorCode);
        mTextValue_RED = (TextView) findViewById(R.id.valueOfRed);
        mTextValue_GREEN = (TextView) findViewById(R.id.valueOfGreen);
        mTextValue_BLUE = (TextView) findViewById(R.id.valueOfBlue);

        mBtnSendColorCode = (Button) findViewById(R.id.btnSend);
        mBtnGetBTPermission = (Button) findViewById(R.id.btnBTPermission);
        mBtnSelectDevice = (Button) findViewById(R.id.btnSelectDevice);
        mMasterSwitch = (Switch) findViewById(R.id.masterSW);
        ////////////////////////////////////////////////////////////////////////////////////////////
        mTextValue_RED.setText("0");
        mTextValue_GREEN.setText("0");
        mTextValue_BLUE.setText("0");

        mController_RED.setOnSeekBarChangeListener(this);
        mController_GREEN.setOnSeekBarChangeListener(this);
        mController_BLUE.setOnSeekBarChangeListener(this);

        mColorCodeEditor.addTextChangedListener(this);
        mColorCodeEditor.setText(mColorCodeConversion.RGBToHexCode(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE));
        mColorCodeEditor.setFilters(new InputFilter[]{filterColorCode});
        mBtnSendColorCode.setOnClickListener(this);
        mBtnGetBTPermission.setOnClickListener(this);
        mBtnSelectDevice.setOnClickListener(this);
        mMasterSwitch.setOnCheckedChangeListener(this);

        mColorView.setBackgroundColor(Color.rgb(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE));
        mColorView.setOnClickListener(this);
    }

    public InputFilter filterColorCode = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[abcdef0-9]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();
    }

    @Override
    protected void onStart() {
        UpdateUIList();
        startUIUpdate();
        super.onStart();
    }

    @Override
    protected  void onStop() {
        super.onStop();
        stopUIUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothModule.disableBluetooth();

        mColorView = null;
        mColorPicker = null;;
        mBtnSendColorCode = null;
        mBtnGetBTPermission = null;
        mController_RED = null;
        mController_GREEN = null;
        mController_BLUE = null;
        mColorCodeEditor = null;
        mTextValue_RED = null;
        mTextValue_GREEN = null;
        mTextValue_BLUE = null;
        mColorCodeConversion = null;
        mBluetoothModule = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mController_RED) {
            mTextValue_RED.setText(mController_RED.getProgress() + "");
            mIntValue_RED = mController_RED.getProgress();
        } else if (seekBar == mController_GREEN) {
            mTextValue_GREEN.setText(mController_GREEN.getProgress() + "");
            mIntValue_GREEN = mController_GREEN.getProgress();
        } else if (seekBar == mController_BLUE) {
            mTextValue_BLUE.setText(mController_BLUE.getProgress() + "");
            mIntValue_BLUE = mController_BLUE.getProgress();
        }
        mColorView.setBackgroundColor(Color.rgb(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE));
        if(isSeekbarTouched) {
            mColorCodeEditor.setText(mColorCodeConversion.RGBToHexCode(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE));
            mBluetoothModule.sendData(mColorCodeConversion.RGBToHexCode(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE, "/"));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {isSeekbarTouched = true;}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {isSeekbarTouched = false;}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    private boolean formatting;
    private int mStart;
    private int mEnd;

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.mStart = -1;
        if (before == 0) {
            mStart = start + count;
            mEnd = Math.min(mStart + count, s.length());
        }

        if (!isSeekbarTouched) {
            mController_RED.setProgress(mColorCodeConversion.HexCodeToRGB(mColorCodeEditor.getText().toString(), mColorCodeEditor.length(), 0));
            mController_GREEN.setProgress(mColorCodeConversion.HexCodeToRGB(mColorCodeEditor.getText().toString(), mColorCodeEditor.length(), 1));
            mController_BLUE.setProgress(mColorCodeConversion.HexCodeToRGB(mColorCodeEditor.getText().toString(), mColorCodeEditor.length(), 2));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(mColorCodeEditor.length() > 6) {
            s.delete(6, mColorCodeEditor.length());
        }
        if (!formatting) {
            if (mStart >= 0) {
                formatting = true;
                s.replace(mStart, mEnd, "");
                formatting = false;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnSendColorCode) {
            Toast.makeText(this, mColorCodeConversion.RGBToHexCode(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE), Toast.LENGTH_SHORT).show();
            mBluetoothModule.sendData(mColorCodeConversion.RGBToHexCode(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE, "/"));
        }
        else if (v == mColorView) {
            mColorPicker = new AmbilWarnaDialog(this, Color.parseColor("#" + mColorCodeConversion.getCompleteColorCode(mColorCodeEditor.getText().toString(), mColorCodeEditor.length())), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    // color is the color selected by the user.
                    String ColorCode = String.format("%08x", color);
                    mColorCodeEditor.setText(ColorCode.substring(2, 8));

                    mBluetoothModule.sendData(mColorCodeConversion.RGBToHexCode(mIntValue_RED, mIntValue_GREEN, mIntValue_BLUE, "/"));
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // cancel was selected by the user
                }
            });
            mColorPicker.show();
        }
        else if (v == mBtnGetBTPermission) {
            if(mBluetoothModule.getDeviceState()) {
                mBluetoothModule.enableBluetooth();
            }
        }
        else if (v == mBtnSelectDevice) {
            mBluetoothModule.selectDevice();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(mMasterSwitch.isChecked())
            mBluetoothModule.sendData("master_on");
        else
            mBluetoothModule.sendData("master_off");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if(requestCode == Activity.RESULT_OK) {
                }
                else {
                    //Toast.makeText(this, "사용자에 의해 취소됨", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    public void UpdateUIList() {
        if(mHandler == null) {
            mHandler = new Handler() {
                public void handleMessage(Message message) {
                    //Toast.makeText(Main.this, "", Toast.LENGTH_SHORT).show();
                    if(mBluetoothModule.isBlutoothEnabled()) {
                        if(isBluetoothFirstStart) {
                            mBluetoothModule.selectDevice();
                            isBluetoothFirstStart = false;
                        }
                        mBtnGetBTPermission.setEnabled(false);
                        mBtnSelectDevice.setEnabled(true);
                    }
                    else {
                        mBtnGetBTPermission.setEnabled(true);
                        mBtnSelectDevice.setEnabled(false);
                        isBluetoothFirstStart = true;
                    }
                }
            };
        }

        if(mTask == null) {
           mTask = new TimerTask() {
               @Override
               public void run() {
                   mHandler.obtainMessage(1).sendToTarget();
                   Log.e("UI Updater", "Updating...");
               }
           };
        }
    }

    public void startUIUpdate() {
        if(mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(mTask, 0, 1000);
        }
    }

    public void stopUIUpdate() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if(mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        if(mHandler != null) {
            mHandler = null;
        }
        Log.e("UI Updater", "Updating stopped.");
    }
}


