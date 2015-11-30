package forestfire.movement;

import java.util.HashMap;
import java.util.Map;

import forestfire.MoveTask;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import jadex.bdiv3.runtime.PlanFinishedTaskCondition;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.math.IVector2;

@Plan
public class MoveToLocationPlan {

	//-------- attributes --------

	@PlanCapability
	protected EnvAccessInterface capa;
	
	@PlanAPI
	protected IPlan rplan;
	
	@PlanReason
	protected DestinationInterface goal;
	
	/**
	 *  The plan body.
	 */
	@PlanBody
	public void body()
	{
		ISpaceObject myself	= capa.getMyself();
		IVector2 dest = goal.getDestination();
		
//		System.out.println("move plan: "+goal.getDestination());
		
//		if(!((String)myself.getProperty("state")).equals("moving_to_hospital") && dest.equals(home))
//			myself.setProperty("state", "moving_home");
		
		// Create a move task
		Map<Object, Object> props = new HashMap<Object, Object>();
		props.put(MoveTask.PROPERTY_DESTINATION, dest);
//		props.put(MoveTask.PROPERTY_SCOPE, capa.getCapability().getAgent().getExternalAccess());
		props.put(AbstractTask.PROPERTY_CONDITION, new PlanFinishedTaskCondition(rplan));
		IEnvironmentSpace space = capa.getEnvironment();
		
		Future<Void> fut = new Future<Void>();
		DelegationResultListener<Void> lis = new DelegationResultListener<Void>(fut, true);
		Object mtaskid = space.createObjectTask(MoveTask.PROPERTY_TYPENAME, props, myself.getId());
		space.addTaskListener(mtaskid, myself.getId(), lis);
		fut.get();
//		System.out.println("move after second task: "+rplan);
		
//		System.out.println("Moved to location: "+capa.getMyself());
	}
	
	
}