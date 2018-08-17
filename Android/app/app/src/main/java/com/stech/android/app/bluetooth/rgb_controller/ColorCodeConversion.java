package com.stech.android.app.bluetooth.rgb_controller;

import android.util.Log;

/**
 * Created by chyh1 on 2018-05-10.
 */

public class ColorCodeConversion {

    public ColorCodeConversion() {/*Default Constructor*/}

    public String getCompleteColorCode(String ColorCode, int EditCodeLength) {
        if (EditCodeLength < 6) {
            for (int i = 0; i < 6 - EditCodeLength; i++)
                ColorCode += "0";
        }
        return ColorCode;
    }

    public String RGBToHexCode(int mRED, int mGREEN, int mBLUE) {
        String mPart_RED, mPart_GREEN, mPart_BLUE;

        mPart_RED = Integer.toString(mRED, 16);
        mPart_GREEN = Integer.toString(mGREEN, 16);
        mPart_BLUE = Integer.toString(mBLUE, 16);

        if(mRED == 0)   mPart_RED = "00";
        if(mGREEN == 0)   mPart_GREEN = "00";
        if(mBLUE == 0)   mPart_BLUE = "00";

        return String.format("%02x", mRED) + String.format("%02x", mGREEN) +  String.format("%02x", mBLUE);
    }

    public String RGBToHexCode(int mRED, int mGREEN, int mBLUE, String mSeparator) {
        String mPart_RED, mPart_GREEN, mPart_BLUE;

        mPart_RED = Integer.toString(mRED, 16);
        mPart_GREEN = Integer.toString(mGREEN, 16);
        mPart_BLUE = Integer.toString(mBLUE, 16);

        if(mRED == 0)   mPart_RED = "00";
        if(mGREEN == 0)   mPart_GREEN = "00";
        if(mBLUE == 0)   mPart_BLUE = "00";

        return String.format("%02x", mRED) + mSeparator + String.format("%02x", mGREEN) + mSeparator + String.format("%02x", mBLUE);
    }

    public int HexCodeToRGB(String ColorCode, int EditCodeLength, int arg) {
        int mInt_RED, mInt_GREEN, mInt_BLUE;
        String mPart_RED, mPart_GREEN, mPart_BLUE;

        ColorCode = getCompleteColorCode(ColorCode, EditCodeLength);

        if (arg == 0) {
            mPart_RED = ColorCode.substring(0, 2);
            mInt_RED = Integer.parseInt(mPart_RED, 16);
            return mInt_RED;
        }
        else if (arg == 1) {
            mPart_GREEN = ColorCode.substring(2, 4);
            mInt_GREEN = Integer.parseInt(mPart_GREEN, 16);
            return mInt_GREEN;
        }
        else if (arg == 2) {
            mPart_BLUE = ColorCode.substring(4, 6);
            mInt_BLUE = Integer.parseInt(mPart_BLUE, 16);
            return mInt_BLUE;
        }

        return 0;
    }
}
