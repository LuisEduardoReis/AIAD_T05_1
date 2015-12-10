package forestfire;


import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Double;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.TerrainView;

public class MoveTask extends AbstractTask {
	// -------- constants --------

	/** The task name. */
	public static final String PROPERTY_TYPENAME = "move";

	/** The destination property. */
	public static final String PROPERTY_DESTINATION = "destination";

	/** The speed property of the moving object (units per second). */
	public static final String PROPERTY_SPEED = "speed";
	
	/** The scope property. */
	public static final String PROPERTY_SCOPE = "agent";
	
	/** Range for distance to fire **/
	public static final String PROPERTY_RETREAT_DIST = "min_dist";
	public static final String PROPERTY_APPROACH_DIST = "max_dist";
	
	/** Drop conditions **/
	//public static final String PROPERTY_DROP_ON_MIN_DIST = "drop_on_min_dist";
	//public static final String PROPERTY_DROP_ON_MAX_DIST = "drop_on_max_dist";

	// -------- IObjectTask methods --------

	/**
	 * Executes the task.
	 * 
	 * @param space
	 *            The environment in which the task is executing.
	 * @param obj
	 *            The object that is executing the task.
	 * @param progress
	 *            The time that has passed according to the environment
	 *            executor.
	 */
	public void execute(IEnvironmentSpace space, ISpaceObject obj,
			long progress, IClockService clock) {
		
		// Properties
		IVector2 destination = (IVector2) getProperty(PROPERTY_DESTINATION);
		IVector2 loc = (IVector2) obj.getProperty(Space2D.PROPERTY_POSITION);		
		double speed = ((Number) obj.getProperty(PROPERTY_SPEED)).doubleValue();
		double retreat_dist = (double) getProperty(PROPERTY_RETREAT_DIST);
		double approach_dist = (double) getProperty(PROPERTY_APPROACH_DIST);
		
		FiremanBDI fireman = (FiremanBDI) getProperty(PROPERTY_SCOPE);
		TerrainView terrain_view = fireman.getTerrainView();
		
		double loc_x = loc.getXAsDouble(), loc_y = loc.getYAsDouble();
		int width = terrain_view.terrain_width, 
			height = terrain_view.terrain_height;
		double dist_to_dest = Double.MAX_VALUE, dest_x = 0, dest_y = 0;
		
		double maxdelta = progress * speed * 0.001;		
		IVector2 newloc;
		
		// Get fastest way to get to destination
		if (destination != null) {			
			double cart_dest_x = destination.getXAsDouble(), cart_dest_y = destination.getYAsDouble();
			dest_x = cart_dest_x; dest_y = cart_dest_y;
			if (Math.abs(dest_x - loc_x) > Math.abs(cart_dest_x + width - loc_x)) dest_x = cart_dest_x + width;
			if (Math.abs(dest_x - loc_x) > Math.abs(cart_dest_x - width - loc_x)) dest_x = cart_dest_x - width;
			
			if (Math.abs(dest_y - loc_y) > Math.abs(cart_dest_y + height - loc_y)) dest_y = cart_dest_y + height;
			if (Math.abs(dest_y - loc_y) > Math.abs(cart_dest_y - height - loc_y)) dest_y = cart_dest_y - height;
			
			dist_to_dest = Util.pointDistance(loc_x, loc_y, dest_x, dest_y);		
		}
		
		if (dist_to_dest <= maxdelta) 
			// Got to destination (if it has one)
			newloc = destination;
		else { 
			// Calculate where to go
			double dir_x = 0, dir_y = 0;
			int vr = fireman.viewRange, tv_x = terrain_view.getPosX(), tv_y = terrain_view.getPosY();
			double dist_to_fire = fireman.getDistanceToFire();
			boolean retreat, approach;
			
			// Add force towards destination (if it has one)
			if (destination != null) {
				dir_x = (dest_x - loc_x)/dist_to_dest; 
				dir_y = (dest_y - loc_y)/dist_to_dest;
			}	
			
			// Move away or towards fire (each fire in view applies a force on the fireman)
			retreat = retreat_dist > 0 && dist_to_fire < retreat_dist;
			approach = approach_dist > 0 && dist_to_fire > approach_dist;
			
			if (retreat || approach) {			
				for(int y = -vr; y<=vr; y++) {
					for(int x = -vr; x<=vr; x++) {
						ISpaceObject terrain = terrain_view.get(x, y);
						if (((float) terrain.getProperty("fire")) < 50f) continue;
						
						double vx = tv_x + x - loc_x, 
							   vy = tv_y + y - loc_y;
						double d = Util.vectorLength(vx, vy);
	
						if (retreat) {
							if (d < retreat_dist) {
								dir_x -= vx / (d*d); 
								dir_y -= vy / (d*d);
							}
						} else {
							dir_x += vx / d; 
							dir_y += vy / d;
						}
							
					}
				}	
			}	
			
			double d = Util.vectorLength(dir_x, dir_y);
			if (d > 0) {
				newloc = new Vector2Double(
					(loc_x + maxdelta*(dir_x/d)) % width,
					(loc_y + maxdelta*(dir_y/d)) % height);
			} else
				newloc = loc;
		}
		((Space2D) space).setPosition(obj.getId(), newloc);
		
		// Finish task if: Reached destination, Didn't move or Dead.
		if (newloc == destination || (newloc == loc && approach_dist < 0) || ((double) obj.getProperty("health")) == 0)
			setFinished(space, obj, true);
			
	}

}
