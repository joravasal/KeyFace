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

public class AlgorithmReturnValue {
	private int result;
	private double threshold; 
	
	private int closestImage;
	private double distClosestImage;
	
	private int secondClosestImage;
	private double distSecondClosestImage;
	
	private int farthestImage;
	private double distFarthestImage;
	
	/**
	 * Create an object with all interesting information from an algorithm to recognize faces.
	 * It sets all the values to -1, except distance to closest image, set to Double.MAX_VALUE.
	 */
	public AlgorithmReturnValue(){
		this.result = -1;
		this.threshold = -1;
		this.closestImage = -1;
		this.distClosestImage = Double.MAX_VALUE;
		this.secondClosestImage = -1;
		this.distSecondClosestImage = -1;
		this.farthestImage = -1;
		this.distFarthestImage = -1;
	}
	
	/**
	 * Create an object with all interesting information from an algorithm to recognize faces.
	 */
	public AlgorithmReturnValue(int result, double threshold, int closestImage,
			double distClosestImage, int secondClosestImage,
			double distSecondClosestImage, int farthestImage,
			double distFarthestImage) {
		this.result = result;
		this.threshold = threshold;
		this.closestImage = closestImage;
		this.distClosestImage = distClosestImage;
		this.secondClosestImage = secondClosestImage;
		this.distSecondClosestImage = distSecondClosestImage;
		this.farthestImage = farthestImage;
		this.distFarthestImage = distFarthestImage;
	}
	
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public int getClosestImage() {
		return closestImage;
	}
	public void setClosestImage(int closestImage) {
		this.closestImage = closestImage;
	}
	public double getDistClosestImage() {
		return distClosestImage;
	}
	public void setDistClosestImage(double distClosestImage) {
		this.distClosestImage = distClosestImage;
	}
	public int getSecondClosestImage() {
		return secondClosestImage;
	}
	public void setSecondClosestImage(int secondClosestImage) {
		this.secondClosestImage = secondClosestImage;
	}
	public double getDistSecondClosestImage() {
		return distSecondClosestImage;
	}
	public void setDistSecondClosestImage(double distSecondClosestImage) {
		this.distSecondClosestImage = distSecondClosestImage;
	}
	public int getFarthestImage() {
		return farthestImage;
	}
	public void setFarthestImage(int furthestImage) {
		this.farthestImage = furthestImage;
	}
	public double getDistFarthestImage() {
		return distFarthestImage;
	}
	public void setDistFarthestImage(double distFurthestClosestImage) {
		this.distFarthestImage = distFurthestClosestImage;
	}
}
