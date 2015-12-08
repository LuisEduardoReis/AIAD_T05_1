package forestfire.agents.fireman;

import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import forestfire.agents.fireman.FiremanBDI.FightFire;

public class SavePeoplePlan {

	// -------- attributes --------

	@PlanCapability
	protected FiremanBDI fireman;

	@PlanReason
	protected FightFire goal;

	@PlanAPI
	protected IPlan rplan;

	/**
	 * The plan body.
	 */
	@PlanBody
	public void body() {
		// Go to house
	}

}
