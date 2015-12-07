package forestfire;

import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.TerrainView;
import jadex.bdiv3.runtime.PlanFinishedTaskCondition;
import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;

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
		TerrainView terrain_view = fireman.getTerrain_view();

		int vr = fireman.viewRange;
		for (int y = -vr; y <= vr; y++) {
			for (int x = -vr; x <= vr; x++) {
				ISpaceObject terrain = terrain_view.get(x, y);
				if ((float) terrain.getProperty("fire") >= 50f) {
					float new_fire_state = (float) terrain.getProperty("fire")-(progress/100);	
					System.out.println("Extinguishing fire pos x="+x+", y="+y+" "+terrain.getProperty("fire")+" new state "+new_fire_state);
					terrain.setProperty("fire", new_fire_state);
					fireman.getMyself().setProperty("fighting_fire", true);
				}
				else{
					fireman.getMyself().setProperty("fighting_fire", false);
					setFinished(space, obj, true);
				}
			}
			
		}
		

	}
}