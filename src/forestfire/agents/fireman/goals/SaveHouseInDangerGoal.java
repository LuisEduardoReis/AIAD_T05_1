package forestfire.agents.fireman.goals;

import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalDropCondition;
import forestfire.agents.fireman.FiremanBDI;

@Goal
public class SaveHouseInDangerGoal {
	public SaveHouseInDangerGoal() {
		//System.out.println("Goal SaveHouseInDanger");
	}
	
	@GoalCreationCondition(beliefs="houseInRange")
	protected static boolean checkCreate(FiremanBDI fireman) {
		return fireman.getHouseInRange();
	}
	
	@GoalDropCondition(beliefs="houseInRange")
	public boolean checkDrop(FiremanBDI fireman) {
		return !fireman.getHouseInRange();
	}
	
}