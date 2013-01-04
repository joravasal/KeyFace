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
import java.io.OutputStream;
import java.util.LinkedList;
import org.opencv.R.id;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EigenFacesActivity extends Activity implements OnClickListener {
	
	private Bitmap average;
	private LinkedList<Bitmap> eigenfacesList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("eigenFacesActivity::", "OnCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.eigenfaces);
		setTitle("Eigenfaces");
		Mat aver = ((PCAfaceRecog) KeyFaceActivity.recogAlgorithm).getAverage();
		Mat faces = ((PCAfaceRecog) KeyFaceActivity.recogAlgorithm).getEigenFaces();
		
		int size = new Integer(KeyFaceActivity.prefs.getString("savedFaceSize", "200"));
		Mat aux = new Mat();
		
		aver = aver.reshape(1,size);
		//aver.convertTo(aux, );
		aver = toGrayscale(aver);
		average = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Imgproc.cvtColor(aver, aux, Imgproc.COLOR_GRAY2RGBA, 4);
		Utils.matToBitmap(aux, average);
		LinearLayout layout = (LinearLayout) findViewById(id.eigenFacesHorizontalLayout);
		
		TextView avrgImgTV = new TextView(getApplicationContext());
		avrgImgTV.setText("Average image:");
		avrgImgTV.setPadding(5, 10, 10, 20);
		avrgImgTV.setGravity(Gravity.CENTER);
		
		TextView eigenfacesImgsTV = new TextView(getApplicationContext());
		eigenfacesImgsTV.setText("Eigenfaces:");
		eigenfacesImgsTV.setPadding(5, 10, 10, 20);
		eigenfacesImgsTV.setGravity(Gravity.CENTER);
		
		ImageView imgV = new ImageView(getApplicationContext());
		
		imgV.setClickable(false);
		imgV.setVisibility(0);
		imgV.setPadding(0, 10, 10, 20);
		imgV.setImageBitmap(average);
		
		layout.addView(avrgImgTV);
		layout.addView(imgV);
		layout.addView(eigenfacesImgsTV);
		
		LinkedList<ImageView> variables = new LinkedList<ImageView>();
		eigenfacesList = new LinkedList<Bitmap>();
		for(int i=0; i<faces.rows(); i++){
			variables.add(new ImageView(getApplicationContext()));
			eigenfacesList.add(Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888));
			
			aux = new Mat();
			aux = faces.row(i).reshape(1, size);
			aux = toGrayscale(aux);
			Mat auxGreyC4 = new Mat();
			Imgproc.cvtColor(aux, auxGreyC4, Imgproc.COLOR_GRAY2RGBA, 4);
			Utils.matToBitmap(auxGreyC4, eigenfacesList.get(i));
			
			variables.get(i).setClickable(false);
			variables.get(i).setVisibility(0);
			variables.get(i).setPadding(0, 10, 10, 20);
			variables.get(i).setImageBitmap(eigenfacesList.get(i));
			layout.addView(variables.get(i));
		}
		
		Button save = (Button) findViewById(id.saveEigenfacesB);
		save.setOnClickListener(this);
	}
	
	/**
	 * Converts a matrix with any values into a matrix with correct values (between 0 and 255, both included) to be shown as an image.
	 * @param mat: The matrix to convert
	 * @return A matrix that can be used as an image
	 */
	private Mat toGrayscale(Mat mat) {
	    Mat res = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC1);
	    double min, max;
	    MinMaxLocResult minmax = Core.minMaxLoc(mat);
	    min = minmax.minVal;
	    max = minmax.maxVal;
	    for(int row = 0; row < mat.rows(); row++) {
	        for(int col = 0; col < mat.cols(); col++) {
	            res.put(row, col, 255 * ((mat.get(row, col)[0] - min) / (max - min)));
	        }
	    }
	    return res;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.saveEigenfacesB:
			eigenfacesToDB();
			KeyFaceActivity.toastHandler.post(new Runnable() {
				public void run() {
					Toast.makeText(KeyFaceActivity.globalappcontext, "Eigenfaces saved!", Toast.LENGTH_SHORT).show();
				}
			});
			break;
		default:
			break;
		}
	}

	/**
	 * Saves all the eigenfaces and the average face into a folder in the app directory.
	 */
	private void eigenfacesToDB() {
		try{
			//Check if our folder exists (where all the faces are)
			File directory = new File(Environment.getExternalStorageDirectory(), KeyFaceActivity.globalappcontext.getString(R.string.app_dir)+"/Eigenfaces");
		    if (!directory.exists() && !directory.mkdirs()) {
		    	throw new IOException("Path to app directory could not be opened or created.");
		    }
		    //save images
		    String lfile = Environment.getExternalStorageDirectory().getPath()+"/"+KeyFaceActivity.globalappcontext.getString(R.string.app_dir)+"/Eigenfaces/averageFace.png";
			OutputStream out = new FileOutputStream(lfile);
			average.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		    for(int i=0; i<eigenfacesList.size(); i++){
				lfile = Environment.getExternalStorageDirectory().getPath()+"/"+KeyFaceActivity.globalappcontext.getString(R.string.app_dir)+"/Eigenfaces/eigenface"+i+".png";
				out = new FileOutputStream(lfile);
				eigenfacesList.get(i).compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
		    }
			
	    }
		catch(IOException e){
			System.err.println(e.getMessage());
	    }
	}
}
