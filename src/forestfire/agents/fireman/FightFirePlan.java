package forestfire.agents.fireman;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import forestfire.agents.fireman.FiremanBDI.FightFire;

@Plan
public class FightFirePlan {

	//-------- attributes --------

	@PlanCapability
	protected FiremanBDI fireman;
	
	@PlanAPI
	protected IPlan plan;
	
	@PlanReason
	protected FightFire goal;
	
	/**
	 *  The plan body.
	 */
	@PlanBody
	public void body()
	{		
		while(fireman.health > 0) {
			// TODO Dispatch fight fire task or fight fire subgoal
			plan.waitFor(1000).get();
		}
	}
	
	
}