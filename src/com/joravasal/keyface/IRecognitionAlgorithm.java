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

import org.opencv.core.Mat;

public interface IRecognitionAlgorithm {
	/**Given a Mat object with a face on it, it will try to find if the face is recognized.
	 * The distance used is defined by the user with the list preference "distAlgPref", with
	 * integers parsed as strings (0 is rectilinear, 1 is euclidean, ...; you can find the values on the arrays.xml file in values).
	 * 
	 * @param An OpenCV Mat object with a face (the size is the original one) to be recognized.
	 * 
	 * @return An object of the class AlgorithmReturnValue with info on the closest
	 * image (which is and the distance), same info on the second closest and same info on the farthest.
	 * It returns as well a value result and threshold, result will be -1 if the closest image is beyond the threshold.
	 * */
	public AlgorithmReturnValue recognizeFace(Mat face);
	
	/**This function updates the training data and redoes whichever process needed for the algorithm.
	 * 
	 * @param (If the boolean is true it only updates the last image added (saving computing power).
	 * If it were false, it would update the whole data.
	 * 
	 * @return A boolean that specifies if everything went fine.
	 * */
	public boolean updateData(boolean newimage);
}
