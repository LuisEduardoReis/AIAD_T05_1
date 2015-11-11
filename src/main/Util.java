package main;

public class Util {

	public static final float PI = (float) (Math.PI);
	public static final float radToDeg = 180/PI;
	public static final float degToRad = PI/180;
	
	
	

	public static float fcos(float v) { return (float) Math.cos(v);	}
	public static float fsin(float v) { return (float) Math.sin(v);	}
	
	public static int getCoord(int x, int y, int w, int h) { return ((y+h)%h)*w + ((x+w)%w);}

}
