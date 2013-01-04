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
import java.io.ObjectOutputStream;

import org.opencv.R.id;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AddNameActivity extends Activity implements OnClickListener {
	
	private String addressFile;
	private int imageNumber;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("AddNameActivity::", "OnCreate");
		super.onCreate(savedInstanceState);
		
		// Removes the title of the app
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.addname);
		
		Button saveB = (Button) findViewById(R.id.saveB);
		saveB.setOnClickListener(this);
		
		Button cancelB = (Button) findViewById(id.cancelB);
		cancelB.setOnClickListener(this);
		
		imageNumber = KeyFaceActivity.prefs.getInt("savedFaces", 0);
		/*addressFile = Environment.getExternalStorageDirectory().getPath()+"/"+KeyFaceActivity.globalappcontext.getString(R.string.app_dir)+"/Face"+imageNumber+".png";
		Mat img = Highgui.imread(addressFile, 0);
		Bitmap bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
		Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2RGBA, 4);
		Utils.matToBitmap(img, bmp);
		LinearLayout layout = (LinearLayout) findViewById(id.addNameMainLinearLayout);
		//Bitmap bmp = BitmapFactory.decodeFile(addressFile);
		
		DrawMyBitmap imgV = new DrawMyBitmap(getApplicationContext(), bmp);
		
		imgV.setClickable(false);
		imgV.setVisibility(0);
		
		layout.addView(imgV);*/
	}

	@Override
	public void onClick(View v) {
		/*switch (v.getId()) {
		case R.id.saveB:
			EditText newNameET = (EditText) findViewById(id.newNameET);
			String nm = newNameET.getText().toString();
			KeyFaceActivity.allnames.add(nm);
			try {
				FileOutputStream fout = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+KeyFaceActivity.globalappcontext.getString(R.string.app_dir)+"/names.txt");
				ObjectOutputStream objout = new ObjectOutputStream (fout);

				objout.writeObject(KeyFaceActivity.allnames);
				objout.flush();
				objout.close();
				fout.flush();
				fout.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finish();
			break;
		case R.id.cancelB:
			try{
				File img = new File(addressFile);
			
				if(!img.delete())
					throw new IOException("File in "+addressFile+" could not be deleted");
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
			}
			KeyFaceActivity.prefs.edit().putInt("savedFaces", imageNumber-1).apply();
			KeyFaceActivity.recogAlgorithm.updateData(false);
			finish();
			break;
		}*/
	}
	
	private class DrawMyBitmap extends ImageView{

        private Bitmap bmp = null;
        public DrawMyBitmap(Context context, Bitmap mybmp) {
            super(context);
            this.bmp=mybmp;
        }

        @Override
        protected void onDraw(Canvas canvas) {

             Paint paint = new Paint();

             paint.setFilterBitmap(true);
             paint.setAntiAlias(true);

             Rect bmprect = new Rect(0 ,0 , bmp.getHeight(), bmp.getWidth() );
             canvas.drawBitmap(bmp, null, bmprect, paint);
            super.onDraw(canvas);
        }
	}
}
