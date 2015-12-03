package forestfire.agents.fireman;

import forestfire.movement.EnvAccessInterface;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.runtime.IPlan;


@Plan
public class FiremanPlan {

	@PlanCapability
	EnvAccessInterface capa;
	
	@PlanAPI
	protected IPlan mPlan;
	
	@PlanBody
	protected void body() {

		FiremanBDI fireman = (FiremanBDI) capa.getAgent();
		
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
				mPlan.waitFor(1000).get();
				//mPlan.dispatchSubgoal(fireman.new LookForFire()).get();
			}
		}
		System.out.println("Fireman " + fireman.getMyself().getId() + " finished.");
	}
	
}
