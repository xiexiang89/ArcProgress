package com.igo.customview;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    DialProgressBar dialProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialProgressBar = findViewById(R.id.dial_progressbar);
//        dialProgressBar.setLevel(2);
    }

    public void onClick(View view) {
        dialProgressBar.setLevel(6,true);
    }
}
