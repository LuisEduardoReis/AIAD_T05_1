package forestfire.agents.fireman;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import jadex.extension.envsupport.math.Vector2Double;

import java.util.Random;

import forestfire.agents.fireman.FiremanBDI.LookForFire;

@Plan
public class LookForFirePlan {

	//-------- attributes --------

	@PlanCapability
	protected FiremanBDI fireman;
	
	@PlanAPI
	protected IPlan plan;
	
	@PlanReason
	protected LookForFire goal;
	
	/**
	 *  The plan body.
	 */
	@PlanBody
	public void body()
	{
		Random r = new Random();
		
		while(true) {
			Vector2Double dest = new Vector2Double(
					r.nextDouble() * fireman.terrain_width, 
					r.nextDouble() * fireman.terrain_height);
			plan.dispatchSubgoal(fireman.getMovement().new Move(dest)).get();
		}
	}
	
	
}
