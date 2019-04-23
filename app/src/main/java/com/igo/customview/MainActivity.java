package com.igo.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    ArcProgressBar arcProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arcProgressBar = findViewById(R.id.dial_progressbar);
//        arcProgressBar.setMaxArcNum(30);
    }

    public void onClick(View view) {
        arcProgressBar.setProgress(70,true);
    }
}
