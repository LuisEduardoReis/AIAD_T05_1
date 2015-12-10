package forestfire.agents.fireman.goals;

import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goal.ExcludeMode;
import jadex.bdiv3.annotation.GoalDropCondition;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.movement.MoveGoal;

//Run from fire, triggered when fireman is in danger
@Goal(excludemode = ExcludeMode.Never)
public class RunFromFireGoal extends MoveGoal {
	public RunFromFireGoal() {
		super(null);
		// System.out.println("Goal Run From Fire");
	}

	@GoalMaintainCondition(beliefs = "inDanger")
	protected boolean maintain(FiremanBDI fireman) {
		return !fireman.getInDanger();
	}

	@GoalTargetCondition(beliefs = "inDanger")
	protected boolean target(FiremanBDI fireman) {
		return true;
	}

	// when fireman is dead he does not need to care about security anymore
	@GoalDropCondition(beliefs = "health")
	public boolean checkDrop(FiremanBDI fireman) {
		return fireman.getHealth() <= 0;
	}
}