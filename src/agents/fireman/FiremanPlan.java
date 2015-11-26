package agents.fireman;

import movement.MovementCapability.Move;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.runtime.IPlan;
import jadex.extension.envsupport.math.Vector2Int;


@Plan
public class FiremanPlan {

	@PlanCapability
	FiremanBDI fireman;
	
	@PlanAPI
	protected IPlan mPlan;
	
	@PlanBody
	protected void body() {
		
		Move move = fireman.getMovement().new Move(new Vector2Int(5, 5));
		mPlan.dispatchSubgoal(move).get();
		
	}
	
}
