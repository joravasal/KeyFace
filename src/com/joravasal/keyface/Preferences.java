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
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class Preferences extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		
		Preference defCamera = (Preference) findPreference("defCamera");
		if(KeyFaceActivity.cameraCount <= 1){
			defCamera.setEnabled(false);
		}
		
		setTitle("Preferences");
		
		Preference myPref = (Preference) findPreference("deleteData");
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					KeyFaceActivity.toastHandler.post(new Runnable() {
						public void run() {
							Toast.makeText(KeyFaceActivity.globalappcontext, "Deleting data...", Toast.LENGTH_LONG).show();
							}
						});
					KeyFaceActivity.toastHandler.post(new Runnable() {
						public void run() {
							KeyFaceActivity.prefs.edit().putInt("savedFaces", 0).apply();
							}
						});
					
					File directory = new File(Environment.getExternalStorageDirectory(),"KeyFace");
				    if (!directory.exists()) {
				    	KeyFaceActivity.toastHandler.post(new Runnable() {
							public void run() {
								Toast.makeText(KeyFaceActivity.globalappcontext, "There was no data to delete!", Toast.LENGTH_SHORT).show();
								}
							});
				    	return false;
				    }
				    for(File file : directory.listFiles()){
				    	file.delete();
				    }
				    try {
				    	if(!directory.delete()){
				    		throw new IOException("The directory couldn't be deleted!");
				    	}
				    }
				    catch (IOException e) {
						System.err.println(e.getMessage());
				    }
				    
				    KeyFaceActivity.recogAlgorithm.updateData(false);
				    
				    KeyFaceActivity.toastHandler.post(new Runnable() {
						public void run() {
							Toast.makeText(KeyFaceActivity.globalappcontext, "Data deleted!", Toast.LENGTH_SHORT).show();
							}
						});
					return true;
		            }
		        });

	}
	
}
