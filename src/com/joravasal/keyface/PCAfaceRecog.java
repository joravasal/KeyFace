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
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class PCAfaceRecog implements IRecognitionAlgorithm {
	
	private Mat sum; //All images appended together in a matrix where each row is an image
	private Mat projectedTraining; //The training images projections to compare when recognizing
	private Mat average; //Average matrix from all images
	private Mat eigenfaces; //result of applying PCA, the eigenvectors.
	private int numImages; //Number of images saved, it should be equal to the global variable savedFaces in the preferences
	private String imagesDir; //the address where faces are saved
	private Size imageSize; //The size of face images
	private int imgLength; //all pixels of an image, the length of each row in the matrix "sum"
	
	//We assume the size of each image is right, if it isn't, it will bring problems
	public PCAfaceRecog(String address, Size size) {
		imagesDir = address;
		imageSize = size;
		
		imgLength = (int)(size.width*size.height);
		
		average = new Mat();
		eigenfaces = new Mat();
		
		updateData(false);
	}
	
	/**
	 * Given a Mat object (data structure from OpenCV) with a face on it, 
	 * it will try to find if the face is recognized from the data saved.
	 * It applies a change in size to match the one needed.
	 * 
	 * @return An integer that specifies which vector is recognized with the given Mat
	 * */
	public AlgorithmReturnValue recognizeFace(Mat face){
		if(numImages<2){
			return new AlgorithmReturnValue();
		}
		Imgproc.resize(face, face, imageSize); //Size must be equal to the size of the saved faces 
		
		Mat analyze = new Mat(1, imgLength, CvType.CV_32FC1);
		Mat X = analyze.row(0);
		try{
			face.reshape(1,1).convertTo(X, CvType.CV_32FC1);
		}
		catch (CvException e) {
			return new AlgorithmReturnValue();
		}
		Mat res = new Mat();
		Core.PCAProject(analyze, average, eigenfaces, res);
		return findClosest(res);
	}
	
	/**
	 * It has no input, it will add the last image (when numerically ordered)
	 * to the array of images and calculate the new PCA subspace.
	 * 
	 * PCA won't work properly if newimage is true.
	 * 
	 * @return A boolean that specifies if everything went fine.
	 * */
	public boolean updateData(boolean newimage){
		if(newimage) { //There is some error with this code, if newimage is true.
			//Probably it is the matrix.create() function. Later when PCA is done, the projection will be wrong.
			//So this code is never used at the moment, and newimage should be used as false always.
			//It uses more instructions, but until a solution is found it must stay as it is.
			numImages++;
			try {
				File directory = new File(imagesDir);
			    if (!directory.exists()) {
			    	throw new IOException("Path to file could not be opened.");
			    }
			    String lfile = imagesDir+"/Face"+(numImages-1)+".png";
				Mat img = Highgui.imread(lfile, 0);
				if (img.empty())
					throw new IOException("Opening image number "+(numImages-1)+" failed.");
				//we adapt the old matrices to new sizes
				sum.create(numImages, imgLength, CvType.CV_32FC1);
				projectedTraining.create(numImages, numImages, CvType.CV_32FC1);
				
				//and add the new image to the array of images
				img.reshape(1,1).convertTo(sum.row(numImages-1), CvType.CV_32FC1);
				
			}
			catch(IOException e){
				System.err.println(e.getMessage());
				return false;
		    }
		}
		else {
			numImages = KeyFaceActivity.prefs.getInt("savedFaces", numImages);
			sum = new Mat(numImages, imgLength, CvType.CV_32FC1);
			projectedTraining = new Mat(numImages, numImages, CvType.CV_32FC1);
			
			for(int i = 0; i < numImages; i++){ //opens each image and appends it as a column in the matrix Sum
				String lfile = imagesDir+"/Face"+i+".png";
				try {
					Mat img = Highgui.imread(lfile, 0);
					//Other way of loading image data
					//Mat img = Utils.bitmapToMat(BitmapFactory.decodeFile(lfile));
					if (img.empty())
						throw new IOException("Opening image number "+i+" failed.");
		            //We add the image to the correspondent row in the matrix of images (sum)
		            img.reshape(1,1).convertTo(sum.row(i), CvType.CV_32FC1);
				} catch (IOException e) {
					System.err.println(e.getMessage());
					return false;
				}
			}
		}
		
		
		if (numImages>1){
			average = new Mat();
			eigenfaces = new Mat();
			Core.PCACompute(sum, average, eigenfaces);
			for(int i=0; i<numImages; i++){
				Core.PCAProject(sum.row(i), average, eigenfaces, projectedTraining.row(i));
			}
		}
		
		return true;
	}

	/**
	 * It gets information on the closest image, the distance, the second closest, the distance from this one,
	 * and the furthest image with its distance as well.
	 * 
	 * The difference from result and closest image is that the second one might not be close enough depending 
	 * on the threshold, so result will be -1, but closest image will still be an image. If not, their values are equal.
	 */
	private AlgorithmReturnValue findClosest(Mat toCompare){
		
		AlgorithmReturnValue result = new AlgorithmReturnValue();
		
		for(int i=0; i<numImages; i++){
			double dist = 0;
			for(int j=0; j<numImages; j++){
				if(Integer.parseInt(KeyFaceActivity.prefs.getString("distAlgPref", "1")) == 1){ //Case is 1 -> Euclidean distance
					dist += DistanceAlgorithm.euclideanDist(toCompare.get(0, j)[0], projectedTraining.get(i, j)[0]);
				}
				else { //Case is 0 -> Rectilinear Distance
					dist += DistanceAlgorithm.rectilinearDist(toCompare.get(0, j)[0], projectedTraining.get(i, j)[0]);
				}
			}
			if(dist < result.getDistClosestImage()){
				result.setDistSecondClosestImage(result.getDistClosestImage());
				result.setDistClosestImage(dist);
				result.setSecondClosestImage(result.getClosestImage());
				result.setClosestImage(i);
			}
			else if(dist < result.getDistSecondClosestImage()){
				result.setDistSecondClosestImage(dist);
				result.setSecondClosestImage(i);
			}
			if(dist > result.getDistFarthestImage()){
				result.setDistFarthestImage(dist);
				result.setFarthestImage(i);
			}
		}
		
		//We define the threshold depending on the preferences value multiplied by 10 exp 5
		result.setThreshold(new Double(KeyFaceActivity.prefs.getString("threshold", "50"))*100000.0);
		
		if(result.getDistClosestImage() < result.getThreshold())
			result.setResult(result.getClosestImage());
		return result;
	}
	
	public Mat getEigenFaces(){
		return eigenfaces.clone();
	}
	public Mat getAverage(){
		return average.clone();
	}
}