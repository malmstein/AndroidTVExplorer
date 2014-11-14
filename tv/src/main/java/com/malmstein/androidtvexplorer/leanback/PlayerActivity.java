package com.malmstein.androidtvexplorer.leanback;

import android.app.Activity;
import android.os.Bundle;

import com.malmstein.androidtvexplorer.R;

public class PlayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_player);
    }

}
