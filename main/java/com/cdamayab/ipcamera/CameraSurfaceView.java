package com.cdamayab.ipcamera;

import java.io.IOException;
import java.util.LinkedList;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	Camera camera;

	private LinkedList<byte[]> mQueue = new LinkedList<byte[]>();
	private static final int MAX_BUFFER = 15;
	private byte[] mLastFrame = null;


	CameraSurfaceView(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		//SERVER FUNC
		camera.setPreviewCallback(mPreviewCallback);

		// The default orientation is landscape, so for a portrait app like this
		// one we need to rotate the view 90 degrees.
		camera.setDisplayOrientation(90);
		
		// IMPORTANT: We must call startPreview() on the camera before we take
		// any pictures
		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// Open the Camera in preview mode
			this.camera = Camera.open();
			this.camera.setPreviewDisplay(holder);
		} catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when replaced with a new screen
		// Always make sure to release the Camera instance
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
	}

	public void takePicture(PictureCallback imageCallback) {
		camera.takePicture(null, null, imageCallback);
	}

	////FUNCTION TO USE WITH SERVER

	private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			synchronized (mQueue) {
				if (mQueue.size() == MAX_BUFFER) {
					mQueue.poll();
				}
				mQueue.add(data);
			}
		}
	};

	public byte[] getImageBuffer() {
		synchronized (mQueue) {
			if (mQueue.size() > 0) {
				mLastFrame = mQueue.poll();
			}
		}

		return mLastFrame;
	}

	private void resetBuff() {

		synchronized (mQueue) {
			mQueue.clear();
			mLastFrame = null;
		}
	}

}