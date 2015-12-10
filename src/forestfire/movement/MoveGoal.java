package forestfire.movement;

import jadex.bdiv3.annotation.Goal;
import jadex.extension.envsupport.math.IVector2;
import forestfire.agents.fireman.FiremanBDI;

@Goal
public class MoveGoal {
	protected IVector2 destination;
	protected double retreat_dist, approach_dist;

	public MoveGoal(IVector2 destination) {
		this.destination = destination;
		this.retreat_dist = FiremanBDI.SAFETY_RANGE;
		this.approach_dist = -1;
	}

	public IVector2 getDestination() { return destination; }

	public double getRetreatDist() { return retreat_dist; }
	
	public double getApproachDist() { return approach_dist; }
}