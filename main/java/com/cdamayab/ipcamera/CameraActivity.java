package com.cdamayab.ipcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, Camera.PictureCallback {

    CameraSurfaceView cameraSurfaceView;
    Button shutterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create a layout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout);

        //init controls
        shutterButton = new Button(this);
        shutterButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        shutterButton.setText("TAKE PICTURE");
        linearLayout.addView(shutterButton);
        shutterButton.setOnClickListener(this); // grab out shutter button so we can reference it later

        // set up our preview surface
        cameraSurfaceView = new CameraSurfaceView(this);
        linearLayout.addView(cameraSurfaceView);

    }

    @Override
    public void onClick(View v) {
        takePicture();
    }

    private void takePicture() {
        shutterButton.setEnabled(false);
        cameraSurfaceView.takePicture(this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // TODO something with the image data

        // Restart the preview and re-enable the shutter button so that we can take another picture
        camera.startPreview();
        shutterButton.setEnabled(true);
    }
}