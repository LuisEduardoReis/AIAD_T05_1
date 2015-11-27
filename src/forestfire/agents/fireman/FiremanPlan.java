package forestfire.agents.fireman;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.runtime.IPlan;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.Vector2Double;

import java.util.Random;

import forestfire.movement.MovementCapability.Move;


@Plan
public class FiremanPlan {

	@PlanCapability
	FiremanBDI fireman;
	
	@PlanAPI
	protected IPlan mPlan;
	
	@PlanBody
	protected void body() {
		Random r = new Random();
		
		Space2D space = fireman.getEnvironment();

		while(true) {
			Move move = fireman.getMovement().new Move(new Vector2Double(
					r.nextDouble()*space.getAreaSize().getXAsDouble(), 
					r.nextDouble()*space.getAreaSize().getYAsDouble()));
			mPlan.dispatchSubgoal(move).get();
		}
	}
	
}
