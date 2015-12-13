package forestfire.agents.fireman.plans;

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

import java.util.HashMap;
import java.util.Map;

import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.goals.FightFireGoal;
import forestfire.agents.fireman.tasks.FightFireTask;
import forestfire.movement.MoveTask;

@Plan
public class FightFirePlan {

	//-------- attributes --------

	@PlanCapability
	protected FiremanBDI fireman;
	
	@PlanReason
	protected FightFireGoal goal;
	
	@PlanAPI
	protected IPlan rplan;
	
	/**
	 *  The plan body.
	 */
	@PlanBody
	public void body()
	{	
		ISpaceObject myself	= fireman.getMyself();
		
		while(fireman.getHealth() > 0) {
			
			Map<Object, Object> props = new HashMap<Object, Object>();
			props.put(MoveTask.PROPERTY_SCOPE, fireman);
			props.put(AbstractTask.PROPERTY_CONDITION, new PlanFinishedTaskCondition(rplan));
			
			IEnvironmentSpace space = fireman.getEnvironment();
			
			Future<Void> fut = new Future<Void>();
			DelegationResultListener<Void> lis = new DelegationResultListener<Void>(fut, true);
			Object mtaskid = space.createObjectTask(FightFireTask.PROPERTY_TYPENAME, props, myself.getId());
			space.addTaskListener(mtaskid, myself.getId(), lis);
			fut.get();
			
		}
	}
	
	
}