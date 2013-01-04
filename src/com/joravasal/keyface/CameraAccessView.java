/**************************************
 * 
 * KeyFace - A program for android that recognizes faces in real time
 *  using OpenCV libraries.
 *  Copyright (C) 2012  Jorge Avalos-Salguero
 *  To contact the author: joravasal@gmail.com
 *  or search for my profile in LinkedIn.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **************************************/

package com.joravasal.keyface;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class CameraAccessView extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {
	private String tag = "CameraAccessView::";

	private SurfaceHolder holder;
	private VideoCapture camera;
	private FpsMeter fps;
	private SharedPreferences prefs;
	private boolean cameraRearActive; //It is used to compare with the variable in KeyFaceActivity, if they are different, a change in camera is requested.

	public void Constructor (Context con){
		holder = getHolder();
		holder.addCallback(this);
		fps = new FpsMeter();
		// We load the preferences, where we can check f.e. the default camera
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
	}
	
	public CameraAccessView(Context context) {
		super(context);
		Log.i(tag, "Constructor-1");
		Constructor(context);
	}
	
	public CameraAccessView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(tag, "Constructor-2");
		Constructor(context);
	}
	
	public CameraAccessView(Context context, AttributeSet attrs, int defStyle) { 
		super(context, attrs, defStyle);
		Log.i(tag, "Constructor-3");
		Constructor(context);
	}

	protected abstract Bitmap processFrame(VideoCapture capture);

	@Override
	public void run() {
		Log.i(tag, "Running thread");
		if (prefs.getBoolean("fpsMeter", true))
			fps.init();

		while (true) {
			Bitmap bmp = null;
			

			synchronized (this) {
				//Check camera variable is OK
				if (camera == null)
					break;
				if (cameraRearActive != KeyFaceActivity.cameraRearActive)
					changeCamera();
				if (!camera.grab()) {
					Log.e(tag,
							"Running thread failed - could not grab from camera");
					break;
				}
			}
			//Whatever process we make for each capture from camera
			//  The class that inherits will implement it.
			bmp = processFrame(camera);
			//Using FPS meter
			if (prefs.getBoolean("fpsMeter", true))
				fps.measure();
			

			//Apply all changes in canvas
			if (bmp != null) {
				Canvas canvas = holder.lockCanvas();
				if (canvas != null) {
					// Draw all our things
					//Log.i(tag, "Drawing in canvas");
					//Black background or cleaning
					//canvas.drawARGB(255, 0, 0, 0);
					canvas.drawColor(0, Mode.CLEAR);
					
					//Camera picture
					canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
					
					//Showing the FPS meter if preference is set to
					if (prefs.getBoolean("fpsMeter", true))
						fps.draw(canvas, (canvas.getWidth() - bmp.getWidth()) / 2, 0);
					holder.unlockCanvasAndPost(canvas);
				}
				bmp.recycle();
			}
		}
		Log.i(tag, "Finished with thread");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(tag, "Surface Changed");
		changeCamera();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(tag, "Surface Created");
		cameraRearActive = KeyFaceActivity.cameraRearActive;
		if (cameraRearActive)
			camera = new VideoCapture(Highgui.CV_CAP_ANDROID);
		else
			camera = new VideoCapture(Highgui.CV_CAP_ANDROID + 1);
		if (camera.isOpened())
			(new Thread(this)).start();
		else {
			camera.release();
			camera = null;
			Log.e(tag, "Surface Created failed - camera not opened");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(tag, "Surface Destroyed");
		if (camera != null) {
			synchronized (this) {
				camera.release();
				camera = null;
			}
		}
	}

	public void changeCamera (){
		synchronized (this) {
			camera.release();
			cameraRearActive = KeyFaceActivity.cameraRearActive;
			if(cameraRearActive)
				camera = new VideoCapture(Highgui.CV_CAP_ANDROID);
			else
				camera = new VideoCapture(Highgui.CV_CAP_ANDROID+1);
			findBetterPreviewSize();
		}
	}
	
	// Function to turn the picture from camera in the right direction
	// It also applies the mirror effect in the front camera
	public Mat correctCameraImage(Mat image) {
		//Log.i(tag, "Correcting image rotation");
		//Check rotation of device
		int rotation = ((KeyFaceActivity) this.getContext()).getWindowManager()
				.getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			int degrees = 90;
			//Mirror (y axis) if front camera and rotation in any case
			Mat imageResult = new Mat();
			//For some reason to rotate the image properly, we have to set the center like this
			Point center = new Point(image.width() / 2, image.width() / 2);
			Mat transform = Imgproc.getRotationMatrix2D(center, degrees, 1.0);
			try{
				Imgproc.warpAffine(image, imageResult, transform,
						new Size(image.height(), image.width()));
			}
			catch (CvException e) {
				System.err.println(e.getMessage());
			}
			if (KeyFaceActivity.cameraRearActive)
				Core.flip(imageResult, imageResult, -1);
			else
				Core.flip(imageResult, imageResult, 1);
			return imageResult;
		case Surface.ROTATION_90:
			//Mirror on y axis if front camera
			if (!KeyFaceActivity.cameraRearActive)
				Core.flip(image, image, 1);
			break;
		case Surface.ROTATION_180:
			//Never gets here but just in case:
			break;
		case Surface.ROTATION_270:
			//Mirror on the x axis if rear camera, both axis if front camera
			if (KeyFaceActivity.cameraRearActive) 
				Core.flip(image, image, -1);
			else
				Core.flip(image, image, 0);
			break;
		default:
			break;
		}
		
		return image;
	}
	
	private void findBetterPreviewSize() {
		synchronized (this) {
			if (camera != null && camera.isOpened()) {
				List<Size> sizes = camera.getSupportedPreviewSizes();
				// The best sizes that surpasses by less the size of the screen
				int betterHeight = Integer.MAX_VALUE;
				int betterWidth = Integer.MAX_VALUE;
				// maximum sizes for image in case no one surpasses the size of
				// the screen
				int maxHeight = 0;
				int maxWidth = 0;
				for (Size size : sizes) {
					if ((size.height >= getHeight() || size.width >= getWidth())
							&& size.height < betterHeight) {
						betterHeight = (int) size.height;
						betterWidth = (int) size.width;
					}
					if (size.height > maxHeight) {
						maxHeight = (int) size.height;
						maxWidth = (int) size.width;
					}
				}
				// Images will have the minimum size that exceeds the screen size,
				// unless the maximum size possible is smaller than the screen
				// size
				if (maxHeight < betterHeight) {
					camera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, maxHeight);
					camera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, maxWidth);
				} else {
					camera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, betterHeight);
					camera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, betterWidth);
				}		
			}
		}
	}

}
