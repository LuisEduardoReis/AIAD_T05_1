package forestfire.agents.fireman.goals;

import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalDropCondition;
import jadex.bdiv3.annotation.RawEvent;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bdiv3.runtime.IGoal;
import jadex.extension.envsupport.math.IVector2;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.movement.MoveGoal;

@Goal
public class ApproachHouseInDangerGoal extends MoveGoal {
	 
	public ApproachHouseInDangerGoal(IVector2 destination) {
		super(destination);
		//System.out.println("Goal Approach House in Danger");
	}

	@GoalCreationCondition(beliefs="houseBeingSaved")
	public static ApproachHouseInDangerGoal checkCreate(FiremanBDI fireman) {
		if (fireman.getHouseBeingSaved() != null)
			return new ApproachHouseInDangerGoal(fireman.getHouseBeingSaved());
		else
			return null;
	}
	
	@GoalDropCondition(rawevents={@RawEvent(ChangeEvent.GOALADOPTED)}, beliefs="houseBeingSaved")
	public boolean checkDrop(FiremanBDI fireman, IGoal goal) {
		return fireman.getHouseBeingSaved() != destination;
	}
	
}