package forestfire;

import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.TerrainView;

public class FightFireTask extends AbstractTask {
	/** The task name. */
	public static final String PROPERTY_TYPENAME = "fight_fire";

	/** The scope property. */
	public static final String PROPERTY_SCOPE = "agent";

	long time;

	public FightFireTask() {
		time = 0;
	}

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
		TerrainView terrain_view = fireman.getTerrainView();

		int vr = fireman.viewRange;
		double actionRange = fireman.actionRange;
		boolean hasFire = false;
		for (int y = -vr; y <= vr; y++) {
			for (int x = -vr; x <= vr; x++) {
				ISpaceObject terrain = terrain_view.get(x, y);
				if ((float) terrain.getProperty("fire") >= 15f && Util.vectorLength(x, y) <= actionRange) {
					float new_fire_state = (float) ((float) terrain.getProperty("fire")-((float)fireman.getMyself().getProperty("figh_fire_rate"))*progress*0.001);	
					//System.out.println("Extinguishing fire pos x="+x+", y="+y+" "+terrain.getProperty("fire")+" new state "+new_fire_state);
					terrain.setProperty("fire", new_fire_state);
					hasFire = true;
				}
			}
			
		}
		
		if(!hasFire)
			setFinished(space, obj, true);		

	}
}
