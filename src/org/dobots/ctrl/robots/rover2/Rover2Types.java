package org.dobots.ctrl.robots.rover2;

public class Rover2Types {
	
	public static final String ADDRESS 	= "192.168.1.100";
//	public static final String ADDRESS 	= "10.10.1.182";
	public static final int PORT		= 80;
//	public static final int PORT		= 5000;
	
	public static final String ID		= "AC13";
	public static final String PWD		= "AC13";
	
	public static final String SSID_FILTER = "";

	public static final int MIN_SPEED		= 0;
	public static final int MAX_SPEED 		= 10;
	public static final int MIN_RADIUS 		= 1;
	public static final int MAX_RADIUS 		= 1000;
	public static final double AXLE_WIDTH  	= 230.0; // mm

	public enum VideoResolution {
		res_unknown,
		res_320x240,
		res_640x480
	}

}
