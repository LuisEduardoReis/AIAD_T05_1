import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SimplePropertyObject;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.ISpaceProcess;
import jadex.extension.envsupport.environment.space2d.Space2D;

import java.util.Random;


public class FireProcess extends SimplePropertyObject implements ISpaceProcess {

	public class State {
		public int[] fire, fuel;
		public int w,h;
		
		public State(int w, int h) { 
			this.w = w;
			this.h = h;
			fire = new int[w*h];
			fuel = new int[w*h];
		}		
		private int getCoord(int x, int y) { return ((y+h)%h)*w + ((x+w)%w);}
		public int getFire(int x, int y) { return fire[getCoord(x,y)];}
		public int getFuel(int x, int y) { return fuel[getCoord(x,y)];}
		public void setFire(int x, int y, int v) { fire[getCoord(x,y)] = v;}
		public void setFuel(int x, int y, int v) { fuel[getCoord(x,y)] = v;}
		public void incFire(int x, int y, int v) { int i = getCoord(x,y); fire[i] = Math.min(fire[i]+v,100);}
		public void decFire(int x, int y, int v) { int i = getCoord(x,y); fire[i] = Math.max(fire[i]-v,0);}
		public void decFuel(int x, int y, int v) { int i = getCoord(x,y); fuel[i] = Math.max(fuel[i]-v,0);}
	}
	
	Space2D space;
	State current, next;
	ISpaceObject[] terrain; 
	
	int w,h;
	Random r = new Random();
	
    @Override
    public void start(IClockService arg0, IEnvironmentSpace arg1) {

        space = (Space2D)arg1;

        this.w = space.getAreaSize().getXAsInteger();
        this.h = space.getAreaSize().getYAsInteger();

        this.terrain = space.getSpaceObjectsByType("terrain");
        // Start fire
        terrain[Math.round(h/2)*w + Math.round(w/2)].setProperty("onFire", 100);
        
        this.current = new State(w,h);
        this.next = new State(w,h);
    }

    @Override
    public void shutdown(IEnvironmentSpace iEnvironmentSpace) {

    }
    
    @Override
    public void execute(IClockService iClockService, IEnvironmentSpace iEnvironmentSpace) {
    	 for(int i = 0; i < terrain.length; i++) {
 			current.fire[i] = next.fire[i] = (int) terrain[i].getProperty("onFire");
 			current.fuel[i] = next.fuel[i] =(int) terrain[i].getProperty("fuel");
 		 }
    	 
    	 float windDir = ((float) space.getProperty("wind_direction"))*Util.degToRad;
    	 float windVel = ((float) space.getProperty("wind_velocity"));
    	 float wx = Util.fcos(windDir), wy = -Util.fsin(windDir);
    	 
    	 for(int y = 0; y < h; y++){    	 
        	 for(int x = 0; x < w; x++) {
        		 int fire = current.getFire(x,y);
        		 
        		 if (current.getFuel(x,y) <= 0) next.decFire(x, y, 1);       		 
        		 
        		 if (fire >= 50) {
        			 next.decFuel(x, y, 3);
        			 
        			 for (int i = 0; i < 3; i++) {
	        			 float spreadDirection = Util.TwoPI*r.nextFloat();
	        			 float 	sx = Util.fcos(spreadDirection), 
	        					sy = -Util.fsin(spreadDirection);
	        			 int 	tx = Math.round(x + sx) % w,
	        					ty = Math.round(y + sy) % h;
	        			 float dot = wx*sx + wy*sy; // Dot product between wind vector and spread vector
	        			 dot = (dot+1)/2; // Normalize to [0,1]
	        			 dot = (float) Math.pow(dot, 1+windVel); // dot = dot^(1+windVel)
	        			 dot = (1+windVel*dot)/(1+windVel); //Fire spread bias depends on wind speed
	        			 
	        			 next.incFire(tx, ty, Math.round((1+windVel)*dot*current.getFuel(tx, ty)/100));
        			 }
        			 
        			/* if (20000*r.nextFloat() < fire*current.getFuel(x+1, y)) next.incFire(x+1, y);
        			 if (20000*r.nextFloat() < fire*current.getFuel(x-1, y)) next.incFire(x-1, y);
        			 if (20000*r.nextFloat() < fire*current.getFuel(x, y+1)) next.incFire(x, y+1);
        			 if (20000*r.nextFloat() < fire*current.getFuel(x, y-1)) next.incFire(x, y-1);*/
        		 }
        		 
        	 }
    	 }

    	 for(int i = 0; i < terrain.length; i++) {
  			terrain[i].setProperty("onFire", next.fire[i]);
  			terrain[i].setProperty("fuel", next.fuel[i]);
  		}    	 
    }


}
