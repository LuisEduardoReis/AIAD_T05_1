package forestfire.agents.fireman;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.runtime.IPlan;


@Plan
public class FiremanPlan {

	@PlanCapability
	FiremanBDI fireman;
	
	@PlanAPI
	protected IPlan mPlan;
	
	@PlanBody
	protected void body() {
		
		while(fireman.getHealth() > 0) {
			if (fireman.inDanger) {
				// Go to safety
				mPlan.dispatchSubgoal(fireman.new RunFromFire()).get();
			} else if (fireman.fireInRange) {
				// Fight fire
				// TODO
				mPlan.waitFor(1000).get();
			} else {
				// Look for fire
				mPlan.dispatchSubgoal(fireman.new LookForFire()).get();
				
			}
		}
		System.out.println("Fireman " + fireman.getMyself().getId() + " finished.");
	}
	
}
