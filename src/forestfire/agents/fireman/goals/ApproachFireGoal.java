package forestfire.agents.fireman.goals;

import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalDropCondition;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.movement.MoveGoal;

// Approach fire
@Goal
public class ApproachFireGoal extends MoveGoal {

	public ApproachFireGoal(double actionRange) {
		super(null);
		this.approach_dist = actionRange - 1;
		//System.out.println("Goal Approach Fire");
	}
	
	// Approach fire when it enters view 
	@GoalCreationCondition(beliefs="fireInView")
	public static ApproachFireGoal checkCreate(FiremanBDI fireman) {
		if (fireman.getFireInView())
			return new ApproachFireGoal(fireman.actionRange);
		else
			return null;
	}
	
	// Stop when fire is no longer in view
	@GoalDropCondition(beliefs="fireInView")
	public boolean checkDrop(FiremanBDI fireman) {
		return !fireman.getFireInView();
	}
	
}