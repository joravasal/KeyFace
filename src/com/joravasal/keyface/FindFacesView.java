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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class FindFacesView extends CameraAccessView {

	private Mat mRgba;
	private Mat mGray;
	private CascadeClassifier cascade;
	private String tag = "FindFacesView::";
	private int savedFaces;
	

	public FindFacesView(Context context) {
		super(context);
		Log.i(tag,"Constructor-1");
		constructor(context);
	}
	
	public FindFacesView(Context context, AttributeSet attrs) {
		super(context);
		Log.i(tag,"Constructor-2");
		constructor(context);
	}
	
	public FindFacesView(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		Log.i(tag,"Constructor-3");
		constructor(context);
	}
	
	private void constructor (Context context){
		try {
			InputStream is = context.getResources().openRawResource(
					R.raw.haarcascade_frontalface_default);
			File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile = new File(cascadeDir,
					"haarcascade_frontalface_default.xml");
			FileOutputStream os = new FileOutputStream(cascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1)
				os.write(buffer, 0, bytesRead);
			is.close();
			os.close();
			
			cascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
			if (cascade.empty()) {
				cascade = null;
				Log.e(tag,"Error loading cascade");
			}
			Log.i(tag, "Cascade loaded: " + cascadeFile.getAbsolutePath());
			cascadeFile.delete();
			cascadeDir.delete();
			
			int saveSize = new Integer(KeyFaceActivity.prefs.getString("savedFaceSize", "200"));
			String appdir = Environment.getExternalStorageDirectory().getPath()+"/"+KeyFaceActivity.globalappcontext.getString(R.string.app_dir);
			
			KeyFaceActivity.recogAlgorithm = (IRecognitionAlgorithm) new PCAfaceRecog(appdir, new Size(saveSize, saveSize));
			
			savedFaces = KeyFaceActivity.prefs.getInt("savedFaces", savedFaces);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(tag,"Error in class builder: "+e.getCause());
		}
	}
	
	@Override
	protected Bitmap processFrame(VideoCapture camera) {
		//Log.i(tag,"Processing frame for our delight");
		
		Mat mRgbaAux = new Mat();
		Mat mGrayAux = new Mat();
		camera.retrieve(mRgbaAux, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
		camera.retrieve(mGrayAux, Highgui.CV_CAP_ANDROID_GREY_FRAME);
		//Correct the direction of the image
		mRgba = correctCameraImage(mRgbaAux);
		mGray = correctCameraImage(mGrayAux);
		
		AlgorithmReturnValue resExample = null;
		//We look for faces in the captured images
		if (cascade != null) {
			int faceSize = Math.round(mGray.rows() * KeyFaceActivity.minFaceSize);
			List<Rect> faces = new LinkedList<Rect>();
			try {
				cascade.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(faceSize, faceSize));
			}
			catch (CvException e) {
				System.err.println(e.getMessage());
			}
			for (Rect r : faces){ //For each face
				
				//The Rectangle commented is the area that will be used to check the face,
				//but an ellipse is shown instead, I think it looks better.
				//Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0,0,255,100), 3);

				String nombre = null;

				// We try to recognize it
				AlgorithmReturnValue res = KeyFaceActivity.recogAlgorithm.recognizeFace(mGray.submat(r));
				resExample = res;
				if(res.getResult() != -1){
					//if it worked, we find the name
					nombre = findName(res.getResult());
				}
				Point center = new Point(r.x+(r.width/2), r.y+(r.height/2));
				//If nombre is null we have no name, thus is unrecognized and draw a red circle, together with the text "Unknown"
				if (nombre == null){
					Core.ellipse(mRgba, center, new Size(r.width/2-5, r.height/2+20), 0, 0, 360, new Scalar(255,0,0,30), 3);
					Core.rectangle(mRgba, new Point(r.x+45, r.y+r.height+20), new Point(r.x+200, r.y+r.height+60), new Scalar(70,50,50,255), Core.FILLED);
					Core.putText(mRgba, "Unknown", new Point(r.x+50, r.y+r.height+50), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(200,200,200,100));
					
					//Check if the user is tryaing to save a new face
					if(KeyFaceActivity.addingFaces && faces.size()==1){
						//All is in order, we save a new image and update our account of faces. We update the recognizer data as well.
						addFaceToDB(mGray,r,savedFaces);
						
						KeyFaceActivity.toastHandler.post(new Runnable() {
							public void run() {
								KeyFaceActivity.prefs.edit().putInt("savedFaces", KeyFaceActivity.prefs.getInt("savedFaces", 0)+1).apply();
								}
							});
						
						/*KeyFaceActivity.lock.lock();
						try {
							KeyFaceActivity.faceAdded = true;
							KeyFaceActivity.addingFaces = false;
							KeyFaceActivity.condition.signalAll();
						}
						finally {
							KeyFaceActivity.lock.unlock();
						}
						*/

						if(!KeyFaceActivity.recogAlgorithm.updateData(false)){
							System.err.println("Couldn't update the recognition algorithm with the new picture.");
						}
						KeyFaceActivity.addingFaces = false;
						
						KeyFaceActivity.toastHandler.post(new Runnable() {
							public void run() {
								Toast.makeText(KeyFaceActivity.globalappcontext, "Face saved successfully!", Toast.LENGTH_SHORT).show();
								}
							});
					}
					//The user tried to save a face when there was more than one, it fails and sends a message to the user.
					else if (KeyFaceActivity.addingFaces && faces.size()>1){
						KeyFaceActivity.toastHandler.post(new Runnable() {
							public void run() {
								Toast.makeText(KeyFaceActivity.globalappcontext, "Make sure there is only one face!", Toast.LENGTH_SHORT).show();
							}
						});
						KeyFaceActivity.addingFaces = false;
					}
				}
				
				else { //We know this face!
					Core.ellipse(mRgba, center, new Size(r.width/2-5, r.height/2+20), 0, 0, 360, new Scalar(0,255,0,100), 3);
					Core.rectangle(mRgba, new Point(r.x+45, r.y+r.height+20), new Point(r.x+200, r.y+r.height+60), new Scalar(50,70,50,255), Core.FILLED);
					Core.putText(mRgba, nombre, new Point(r.x+50, r.y+r.height+50), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,255,0,100));
					if (KeyFaceActivity.addingFaces && faces.size()==1){
						//If the user tries to save a face when it is already known we don let him.
						KeyFaceActivity.toastHandler.post(new Runnable() {
							public void run() {
								Toast.makeText(KeyFaceActivity.globalappcontext, "This face is already known!", Toast.LENGTH_SHORT).show();
							}
						});
						KeyFaceActivity.addingFaces = false;
					}
				}
			}
			//If there is no face we tell the user there was a mistake
			if (KeyFaceActivity.addingFaces && faces.size()<=0){
				KeyFaceActivity.toastHandler.post(new Runnable() {
					public void run() {
						Toast.makeText(KeyFaceActivity.globalappcontext, "No face found!", Toast.LENGTH_SHORT).show();
						}
					});
				KeyFaceActivity.addingFaces = false;
			}
		}
		
		savedFaces = KeyFaceActivity.prefs.getInt("savedFaces", savedFaces);
		
		if(KeyFaceActivity.prefs.getBoolean("showData", false)){
			try{
				if(resExample != null) {
					//background rectangle for extra info on PCA
					Core.rectangle(mRgba, new Point(0, mRgba.height()-100), new Point(mRgba.width(), mRgba.height()), new Scalar(50,50,50,50), Core.FILLED);
					//Data for closest image 
					Core.putText(mRgba, "1st", new Point(5, mRgba.height()-80), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					Core.putText(mRgba, Integer.toString(resExample.getClosestImage()), new Point(5, mRgba.height()-55), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					Core.putText(mRgba, Double.toString(resExample.getDistClosestImage()/100000).substring(0, 6), new Point(5, mRgba.height()-30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					//Data for second closest image
					Core.putText(mRgba, "2nd", new Point(180, mRgba.height()-80), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					Core.putText(mRgba, Integer.toString(resExample.getSecondClosestImage()), new Point(180, mRgba.height()-55), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					Core.putText(mRgba, Double.toString(resExample.getDistSecondClosestImage()/100000).substring(0, 6), new Point(180, mRgba.height()-30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					//Data for farthest image
					Core.putText(mRgba, "Last", new Point(355, mRgba.height()-80), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					Core.putText(mRgba, Integer.toString(resExample.getFarthestImage()), new Point(355, mRgba.height()-55), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					Core.putText(mRgba, Double.toString(resExample.getDistFarthestImage()/100000).substring(0, 6), new Point(355, mRgba.height()-30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					//Num images and threshold
					Core.putText(mRgba, "Images:"+savedFaces, new Point(15, mRgba.height()-5), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
					Core.putText(mRgba, "Th:"+Double.toString(resExample.getThreshold()/100000).substring(0, Math.min(6, Double.toString(resExample.getThreshold()/100000).length())), new Point(240, mRgba.height()-5), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
				}
				else {
					Core.rectangle(mRgba, new Point(0, mRgba.height()-30), new Point(200, mRgba.height()), new Scalar(50,50,50,50), Core.FILLED);
					Core.putText(mRgba, "Images:"+savedFaces, new Point(15, mRgba.height()-5), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(250,250,250,200));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
		
		if (Utils.matToBitmap(mRgba, bmp))
			return bmp;
		
		bmp.recycle();
		return null;
	}

	private String findName(int res) {
		String nm = Integer.toString(res);
		/*try{
			nm = KeyFaceActivity.allnames.get(res);
			if(nm.isEmpty() || nm == null)
				return "> Without name ("+res+")";
		}
		catch (IndexOutOfBoundsException e) {
			System.err.println("Failed in the array of names");
			return "> Without name ("+res+")";
		}*/
		return "> "+nm;
	}

	@Override
	public void run() {
		super.run();
		Log.i(tag,"Running thread");
		synchronized (this) {
			if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();

            mRgba = null;
            mGray = null;
		}
	}
	
	@Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);
        Log.i(tag,"Surfance changed");
        synchronized (this) {
            mGray = new Mat();
            mRgba = new Mat();
        }
    }

	private void addFaceToDB(Mat mRGBA, Rect r, int numFaces){
		//Code to put the face in a bitmap and save it in the phone memory
		Mat aux = mRGBA.submat(r);
		int saveSize = new Integer(KeyFaceActivity.prefs.getString("savedFaceSize", "200"));
		//Mat aux2 = new Mat(new Size(saveSize, saveSize), aux.type());
		Imgproc.resize(aux, aux, new Size(saveSize, saveSize));
		final Bitmap bm = Bitmap.createBitmap(saveSize, saveSize, Bitmap.Config.ARGB_8888);
		Imgproc.cvtColor(aux, aux, Imgproc.COLOR_GRAY2RGBA, 4);
		Utils.matToBitmap(aux, bm);
		
		try{
			//Check if our folder exists (where all the photos are)
			File directory = new File(Environment.getExternalStorageDirectory(), KeyFaceActivity.globalappcontext.getString(R.string.app_dir));
		    if (!directory.exists() && !directory.mkdirs()) {
		    	throw new IOException("Path to app directory could not be opened or created.");
		    }
		    //save image
			String lfile = Environment.getExternalStorageDirectory().getPath()+"/"+KeyFaceActivity.globalappcontext.getString(R.string.app_dir)+"/Face"+numFaces+".png";
			OutputStream out = new FileOutputStream(lfile);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
	    }
		catch(IOException e){
			System.err.println(e.getMessage());
	    }
	}
	
}
