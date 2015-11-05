import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SimplePropertyObject;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.ISpaceProcess;
import jadex.extension.envsupport.environment.space2d.Space2D;

import java.util.Random;

/**
 * Created by Leonel Araújo on 22/11/2014.
 */
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
		public void incFire(int x, int y) { int i = getCoord(x,y); fire[i] = Math.min(fire[i]+1,100);}
		public void decFire(int x, int y) { int i = getCoord(x,y); fire[i] = Math.max(fire[i]-1,0);}
		public void decFuel(int x, int y) { int i = getCoord(x,y); fuel[i] = Math.max(fuel[i]-1,0);}
	}
	State current, next;
	ISpaceObject[] terrain; 
	
	int w,h;
	Random r = new Random();
	
    @Override
    public void start(IClockService arg0, IEnvironmentSpace arg1) {

        Space2D space = (Space2D)arg1;

        this.w = space.getAreaSize().getXAsInteger();
        this.h = space.getAreaSize().getYAsInteger();

        this.terrain = space.getSpaceObjectsByType("terrain");
        // Start fire
        terrain[r.nextInt(terrain.length)].setProperty("onFire", 100);
        
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
    	 
    	 for(int y = 0; y < h; y++){    	 
        	 for(int x = 0; x < w; x++) {
        		 int fire = current.getFire(x,y);
        		 
        		 if (current.getFuel(x,y) <= 0) next.decFire(x, y);       		 
        		 
        		 if (fire >= 50) {
        			 if (r.nextInt(100) < fire) next.decFuel(x, y);
        			 if (20000*r.nextFloat() < fire*current.getFuel(x+1, y)) next.incFire(x+1, y);
        			 if (20000*r.nextFloat() < fire*current.getFuel(x-1, y)) next.incFire(x-1, y);
        			 if (20000*r.nextFloat() < fire*current.getFuel(x, y+1)) next.incFire(x, y+1);
        			 if (20000*r.nextFloat() < fire*current.getFuel(x, y-1)) next.incFire(x, y-1);
        		 }
        		 
        	 }
    	 }

    	 for(int i = 0; i < terrain.length; i++) {
  			terrain[i].setProperty("onFire", next.fire[i]);
  			terrain[i].setProperty("fuel", next.fuel[i]);
  		}    	 
    }


}
