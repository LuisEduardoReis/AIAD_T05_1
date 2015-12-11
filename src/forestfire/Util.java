package forestfire;

public class Util {

	public static final float PI = (float) (Math.PI);
	public static final float radToDeg = 180/PI;
	public static final float degToRad = PI/180;
	
	public static final int HOUSE = 4;

	public static float fcos(float v) { return (float) Math.cos(v);	}
	public static float fsin(float v) { return (float) Math.sin(v);	}
	
	public static int getCoord(int x, int y, int w, int h) { return ((y+h)%h)*w + ((x+w)%w);}
	
	public static double vectorLength(double x, double y) { return Math.sqrt(x*x + y*y);}
	public static double pointDistance(double x1, double y1, double x2, double y2) { return vectorLength(x1-x2, y1-y2); }

	public static double pointDistanceToroidalWorld(double x1, double y1, double x2, double y2, double w, double h) {
		double dx = Math.abs(x2-x1), dy = Math.abs(y2-y1);
		dx = Math.min(dx, Math.abs(x2+w-x1));
		dx = Math.min(dx, Math.abs(x2-w-x1));
		
		dy = Math.min(dy, Math.abs(y2+h-y1));
		dy = Math.min(dy, Math.abs(y2-h-y1));
		
		return vectorLength(dx, dy);
	}
}
