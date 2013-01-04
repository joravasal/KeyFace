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

import org.opencv.R.id;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class KeyFaceActivity extends Activity implements OnClickListener {
	static public Boolean cameraRearActive; //Boolean that indicates which camera is being used
	static public SharedPreferences prefs;
	static public float minFaceSize; //The minimun face size searched (in percentage of image size)
	static public LinearLayout facesLayout; //Layout to be accessed from FindFacesView to add the faces to save
	static public boolean addingFaces = false; //Checks for what menu are we using, if the usual one or the one to add new faces
	//static public boolean faceAdded = false; //Flag to check that the face was finally added.
	static public Context globalappcontext; //App context, it is needed as a variable to be used in the View
	static public Handler toastHandler; //It allows us to execute code outside KFA as if we were here (for toasts)
	static public IRecognitionAlgorithm recogAlgorithm; //The algorithm to be used in recognition (initialised in the View)
	static public int cameraCount; //Number of cameras on device.
	
	//static public LinkedList<String> allnames; //list of all names on pictures
	
	//static public ReentrantLock lock = new ReentrantLock();
	//static public Condition condition = lock.newCondition();

	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("KeyFaceActivity::", "OnCreate");
		super.onCreate(savedInstanceState);
		
		globalappcontext = getApplicationContext();
		// Removes the title of the app
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		cameraCount = Camera.getNumberOfCameras();
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		minFaceSize = new Float(prefs.getString("faceSize", "0.3"));
		cameraRearActive = true;
		if(cameraCount > 1){
			cameraRearActive = prefs.getBoolean("defCamera", false);
		}
		setContentView(R.layout.main);
		
		Button newFaceB = (Button) findViewById(R.id.newFaceButton);
		newFaceB.setOnClickListener(this);
		
		Button changeCamB = (Button) findViewById(id.changeCameraButton);
		if(cameraCount > 1){
			changeCamB.setOnClickListener(this);
		}
		else{
			changeCamB.setEnabled(false);
		}
		
		/*
		allnames = new LinkedList<String>();
		try {
			File directory = new File(Environment.getExternalStorageDirectory(),globalappcontext.getString(R.string.app_dir));
		    if (!directory.exists() && !directory.mkdirs()) {
		    	throw new IOException("Path to app directory could not be created.");
		    }
		    File namesFile = new File(Environment.getExternalStorageDirectory(),globalappcontext.getString(R.string.app_dir)+"/names.txt");
		    if (!namesFile.exists()){
		    	FileOutputStream fout = new FileOutputStream(Environment.getExternalStorageDirectory(),globalappcontext.getString(R.string.app_dir)+"/names.txt");
		    	ObjectOutputStream objout = new ObjectOutputStream (fout);

				objout.writeObject(allnames);
				objout.flush();
				objout.close();
				fout.flush();
				fout.close();
		    }
		    else {
				FileInputStream fin = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/"+globalappcontext.getString(R.string.app_dir)+"/names.txt");
				ObjectInputStream objin = new ObjectInputStream (fin);
				
				allnames = (LinkedList<String>) objin.readObject();
				objin.close();
				fin.close();
		    }
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		*/
		toastHandler = new Handler();
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("cameraRearActive", cameraRearActive);
		outState.putFloat("minFaceSize", minFaceSize);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		cameraRearActive = savedInstanceState.getBoolean("cameraRearActive", false);
		minFaceSize = savedInstanceState.getFloat("minFaceSize", 0.3f);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("KeyFaceActivity::", "OnCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		MenuInflater mainMenu = getMenuInflater();
		mainMenu.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("KeyFaceActivity::", "Menu Item Selected - " + item.getTitle());
		switch (item.getItemId()) {
		case R.id.exit:
			finish();
			break;
		case R.id.eigenFaces:
			Intent eigenfaces = new Intent("com.joravasal.keyface.EIGENFACES");
			startActivity(eigenfaces);
			break;
		case R.id.about:
			Intent about = new Intent("com.joravasal.keyface.ABOUT");
			startActivity(about);
			break;
		case R.id.prefs:
			Intent p = new Intent("com.joravasal.keyface.PREFERENCES");
			startActivity(p);
			//minFaceSize = new Float(prefs.getString("faceSize", Float.toString(minFaceSize)));
			break;
		default:
			break;
		}
		return true;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newFaceButton:
			addingFaces = true;
			/*try {
				while(addingFaces) {
					condition.await();
				}
				if(faceAdded){
					faceAdded = false;
					prefs.edit().putInt("savedFaces", prefs.getInt("savedFaces", 0)+1).apply();
					Intent p = new Intent("com.joravasal.keyface.ADDNAME");
					startActivity(p);
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
			break;
		case R.id.changeCameraButton: //Button to change camera
			if(cameraRearActive)
				cameraRearActive = false;
			else
				cameraRearActive = true;
			break;
		default:
			break;
		}
        	
        
		
	}

}