package com.cdamayab.hbsadelec;

import com.cdamayab.httpserver.*;
import com.cdamayab.ipcamera.*;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //INIT HTTPSERVER
        httpServer server= new httpServer();
        server.main();

        //INIT IPCAMERA
        //IPCamera ipCamera = new IPCamera();

        super.onCreate(savedInstanceState);

        //Create a layout
        LinearLayout linearLayout =null;
        LinearLayout.LayoutParams params = null;
        linearLayout = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        linearLayout.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                Toast.makeText(MainActivity.this, "Swipe Left gesture detected", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(i);
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Toast.makeText(MainActivity.this, "Swipe Right gesture detected", Toast.LENGTH_SHORT).show();
            }
        });

        setContentView(linearLayout);

        Toast.makeText(MainActivity.this, "Listening on " + server.getIpAddress() +":" + server.HttpServerPORT, Toast.LENGTH_LONG).show();
        //Toast.makeText(MainActivity.this, "IPCam on " + ipCamera.getIpAddress() +":" + ipCamera.mPort, Toast.LENGTH_LONG).show();
    }
}