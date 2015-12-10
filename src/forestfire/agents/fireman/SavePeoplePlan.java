package forestfire.agents.fireman;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.math.IVector2;
import forestfire.agents.fireman.FiremanBDI.FightFire;
import forestfire.agents.fireman.FiremanBDI.SaveHouseInDanger;

@Plan
public class SavePeoplePlan {

	// -------- attributes --------

	@PlanCapability
	protected FiremanBDI fireman;

	@PlanReason
	protected SaveHouseInDanger goal;

	@PlanAPI
	protected IPlan rplan;

	/**
	 * The plan body.
	 */
	@PlanBody
	public void body() {
		rplan.waitFor(3000).get();
		TerrainView view = fireman.terrain_view;
		IVector2 v = fireman.houseBeingSaved;
		if(v == null) System.out.println("ups");
		ISpaceObject house = view.getGlobal(fireman.houseBeingSaved.getXAsInteger(),fireman.houseBeingSaved.getYAsInteger());
		house.setProperty("house_people", false);
		fireman.houseBeingSaved = null;
		System.out.println("People saved!");
	}

}
