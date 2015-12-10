package forestfire.agents.fireman.goals;

import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalDropCondition;
import forestfire.agents.fireman.FiremanBDI;

// Fight fire, triggered when there is fire in range
@Goal
public class FightFireGoal {
	public FightFireGoal() {
		//System.out.println("Goal Fight Fire");				
	}
	
	@GoalCreationCondition(beliefs = "fireInRange")
	public static boolean checkCreate(FiremanBDI fireman) {
		return fireman.getFireInRange();
	}

	@GoalDropCondition(beliefs = "fireInRange")
	public boolean checkDrop(FiremanBDI fireman) {
		return !fireman.getFireInRange();
	}
}