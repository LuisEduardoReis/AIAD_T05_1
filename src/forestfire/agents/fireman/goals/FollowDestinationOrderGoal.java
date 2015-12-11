package forestfire.agents.fireman.goals;

import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalDropCondition;
import jadex.bdiv3.annotation.RawEvent;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.extension.envsupport.math.IVector2;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.movement.MoveGoal;

@Goal
public class FollowDestinationOrderGoal extends MoveGoal {

	public FollowDestinationOrderGoal(IVector2 destination) {
		super(destination);
		// System.out.println("Goal Follow Festination Order");
	}

	@GoalDropCondition(rawevents={@RawEvent(ChangeEvent.GOALADOPTED)}, beliefs="fireInView")
	public boolean checkDrop(FiremanBDI fireman) {
		return fireman.getFireInView() || destination != fireman.getDestinationOrder();
	}
}