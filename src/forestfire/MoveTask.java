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
		
		IVector2 direction = destination.copy().subtract(loc).normalize();
		double dx = direction.getXAsDouble(), dy = direction.getYAsDouble();
		
		int vr = fireman.viewRange, tvx = fireman.terrain_view_pos_x, tvy = fireman.terrain_view_pos_y;
		double fx = loc.getXAsDouble(), fy = loc.getYAsDouble();
		
		for(int y = -vr; y<=vr; y++) {
		for(int x = -vr; x<=vr; x++) {
			ISpaceObject terrain = fireman.getTerrainView(x, y);
			double vx = (tvx + x) - fx, vy = tvy + y - fy, d = Math.sqrt(vx*vx + vy*vy);
			if (((float) terrain.getProperty("fire")) > 50f && d <= 2) {
				dx -= vx; dy -= vy;
			}
		}}
		direction = new Vector2Double(dx, dy);//.normalize(); 
		double dist = ((Space2D) space).getDistance(loc, destination).getAsDouble();
		
		
		double maxdist = progress * speed *0.001;
		IVector2 newloc = dist <= maxdist ? destination : direction.multiply(
				maxdist).add(loc);
		((Space2D) space).setPosition(obj.getId(), newloc);
		
		if (newloc == destination || ((double) obj.getProperty("health")) == 0)
			setFinished(space, obj, true);
	}

}
