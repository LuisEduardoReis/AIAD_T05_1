package forestfire;


import forestfire.agents.fireman.FiremanBDI;
import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Double;

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
		
		IVector2 destination = (IVector2) getProperty(PROPERTY_DESTINATION);
		IVector2 loc = (IVector2) obj.getProperty(Space2D.PROPERTY_POSITION);		
		double speed = ((Number) obj.getProperty(PROPERTY_SPEED)).doubleValue();
		FiremanBDI fireman = (FiremanBDI) getProperty(PROPERTY_SCOPE);	
		
		// Get fastest way to get to destination
		int width = fireman.terrain_width, height = fireman.terrain_height;
		double loc_x = loc.getXAsDouble(), loc_y = loc.getYAsDouble();
		double cart_dest_x = destination.getXAsDouble(), cart_dest_y = destination.getYAsDouble();
		double dest_x = cart_dest_x, dest_y = cart_dest_y;
		if (Math.abs(dest_x - loc_x) > Math.abs(cart_dest_x + width - loc_x)) dest_x = cart_dest_x + width;
		if (Math.abs(dest_x - loc_x) > Math.abs(cart_dest_x - width - loc_x)) dest_x = cart_dest_x - width;
		
		if (Math.abs(dest_y - loc_y) > Math.abs(cart_dest_y + height - loc_y)) dest_y = cart_dest_y + height;
		if (Math.abs(dest_y - loc_y) > Math.abs(cart_dest_y - height - loc_y)) dest_y = cart_dest_y - height;		
		
		
		double dist = Util.pointDistance(loc_x, loc_y, dest_x, dest_y);
		double maxdist = progress * speed * 0.001;
		
		IVector2 newloc;
		
		if (dist <= maxdist) 
			// Got to destination
			newloc = destination;
		else { 
			// Calculate where to go
			double 	dir_x = (dest_x - loc_x)/dist, 
					dir_y = (dest_y - loc_y)/dist;
			
			int vr = fireman.viewRange, tv_x = fireman.terrain_view_pos_x, tv_y = fireman.terrain_view_pos_y;
			
			// Avoid fire (each fire in range applies a force on the fireman)
			for(int y = -vr; y<=vr; y++) {
				for(int x = -vr; x<=vr; x++) {
					ISpaceObject terrain = fireman.getTerrainView(x, y);
					if (((float) terrain.getProperty("fire")) < 50f) continue;
					
					double vx = tv_x + x - loc_x, 
						   vy = tv_y + y - loc_y,
						   d = Util.vectorLength(vx, vy);
					if (d < 2) {
						dir_x -= vx/d; 
						dir_y -= vy/d;
					}
				}
			}		
			
			double d = Util.vectorLength(dir_x, dir_y);
			newloc = new Vector2Double(
					(loc_x + maxdist*(dir_x/d)) % fireman.terrain_width,
					(loc_y + maxdist*(dir_y/d)) % fireman.terrain_height);
		}
		((Space2D) space).setPosition(obj.getId(), newloc);
		
		if (newloc == destination || ((double) obj.getProperty("health")) == 0)
			setFinished(space, obj, true);
	}

}
