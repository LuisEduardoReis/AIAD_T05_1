package forestfire;

import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;

public class SavePeopleTask extends AbstractTask {
	/** The task name. */
	public static final String PROPERTY_TYPENAME = "save_people";

	/** The scope property. */
	public static final String PROPERTY_SCOPE = "agent";
	
	/** The house property. */
	public static final String PROPERTY_HOUSE = "house";


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
		ISpaceObject house = (ISpaceObject) getProperty(PROPERTY_HOUSE);
		
		float people = (float) house.getProperty("people");
		people = Math.max(0, people - progress*0.001f);
		house.setProperty("people", people);

		if (people == 0 || (double) obj.getProperty("health") <= 0) setFinished(space, obj, true);
	}
}
