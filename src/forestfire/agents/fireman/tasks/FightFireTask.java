package forestfire.agents.fireman.tasks;

import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.math.IVector2;
import forestfire.Util;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.TerrainView;

public class FightFireTask extends AbstractTask {
	/** The task name. */
	public static final String PROPERTY_TYPENAME = "fight_fire";

	/** The scope property. */
	public static final String PROPERTY_SCOPE = "agent";

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
		FiremanBDI fireman = (FiremanBDI) getProperty(PROPERTY_SCOPE);
		IVector2 fireman_pos = (IVector2) obj.getProperty("position");
		TerrainView terrain_view = fireman.getTerrainView();

		int vr = fireman.viewRange;
		double actionRange = fireman.actionRange;
		float ammount = ((float) fireman.getMyself().getProperty("fire_fight_rate"))*progress*0.001f;
		double fx = fireman_pos.getXAsDouble(), fy = fireman_pos.getYAsDouble();
		int ft_x = terrain_view.getPosX(), ft_y = terrain_view.getPosY();
		
		while(ammount > 0) {
			double minDist = actionRange;
			ISpaceObject target = null; float target_fire = 0.0f;
			
			// Find closest fire
			for (int y = -vr; y <= vr; y++) {
				for (int x = -vr; x <= vr; x++) {
					ISpaceObject terrain = terrain_view.get(x, y);
					float fire = (float) terrain.getProperty("fire");
					if (fire < 15f) continue;
					
					double dist = Util.pointDistance(fx,fy, ft_x+x,ft_y+y);					
					if (dist < minDist) {
						minDist = dist;
						target = terrain;
						target_fire = fire;
					}
				}
			}
			
			if (target == null) break;
		
			// Fight closest fire
			if (ammount > target_fire) {
				target.setProperty("fire", 0.0f);
				ammount -= target_fire;
			} else {
				target.setProperty("fire", (target_fire - ammount));
				ammount = 0;
			}
		}
		
		if(ammount>0) setFinished(space, obj, true);		

	}
}
