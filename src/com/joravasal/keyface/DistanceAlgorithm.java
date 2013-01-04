package com.joravasal.keyface;

public abstract class DistanceAlgorithm {
	
	public static double rectilinearDist(double a, double b){
		return Math.abs(a-b);
	}
	
	public static double euclideanDist(double a, double b){
		Double d = a-b;
		return d*d;
	}
}
