package forestfire.agents.fireman.plans;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import jadex.extension.envsupport.math.Vector2Double;

import java.util.Random;

import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.goals.LookForFireGoal;
import forestfire.movement.MoveGoal;

@Plan
public class LookForFirePlan {

	//-------- attributes --------

	@PlanCapability
	protected FiremanBDI fireman;
	
	@PlanAPI
	protected IPlan plan;
	
	@PlanReason
	protected LookForFireGoal goal;
	
	/**
	 *  The plan body.
	 */
	@PlanBody
	public void body()
	{
		Random r = new Random();
		
		while(fireman.getHealth() > 0) {
			Vector2Double dest = new Vector2Double(
					r.nextDouble() * fireman.getTerrainView().terrain_width, 
					r.nextDouble() * fireman.getTerrainView().terrain_height);
			plan.dispatchSubgoal(new MoveGoal(dest)).get();
		}
	}
	
	
}
