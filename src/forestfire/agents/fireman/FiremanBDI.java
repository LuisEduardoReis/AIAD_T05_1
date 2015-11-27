package forestfire.agents.fireman;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalRecurCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.service.annotation.Service;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.Vector2Int;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import forestfire.movement.EnvAccessInterface;
import forestfire.movement.MovementCapability;

@Agent
@Service
//@Plans({@Plan(trigger = @Trigger(goals = FiremanBDI.Explore.class), body = @Body(MoveToLocationPlan.class))})
@Plans(@Plan(body=@Body(FiremanPlan.class)))
public class FiremanBDI implements EnvAccessInterface {
	@Agent
	protected BDIAgent agent;

	@Capability
	MovementCapability movement = new MovementCapability();

	public MovementCapability getMovement() {
		return movement;
	}

	
	@Belief
	protected ContinuousSpace2D space = (ContinuousSpace2D) agent.getParentAccess()
			.getExtension("2dspace").get();

	@Belief
	protected ISpaceObject myself = space.getAvatar(
			agent.getComponentDescription(), agent.getModel().getFullName());

	protected int spaceHeight = space.getAreaSize().getXAsInteger();
	protected int spaceWidth = space.getAreaSize().getYAsInteger();

	@Belief
	protected int currentX = 10;
	@Belief
	protected int currentY = 9;

	@Belief
	protected Vector2Int finalPos;

	@Belief(updaterate = 200)
	protected long currentTime = System.currentTimeMillis();

	public long getCurrentTime() {
		return currentTime;
	}

	ISpaceObject[] terrain;

	@AgentBody
	public void body() {

		/*
		 * myself.setProperty("position", new Vector2Int(currentX, currentY));
		 * this.terrain = space.getSpaceObjectsByType("terrain");
		 * 
		 * finalPos = new Vector2Int(currentX + 5, currentY);
		 */
		
		agent.adoptPlan(new FiremanPlan());

		System.out.println("Finished");

	}

	
	
	@Goal(recur = true)
	public class Explore {

		@GoalCreationCondition(beliefs = "finalPos")
		public Explore() {
		}

		@GoalRecurCondition(beliefs = "currentTime")
		public boolean checkRecur(ChangeEvent event) {
			System.out.println("Keeping goal at " + (long) event.getValue());
			return true;
		}
	}
	
	
	@Goal(recur = true)
	public class MoveTo {

		@GoalCreationCondition(beliefs = "finalPos")
		public MoveTo() {
		}

		@GoalRecurCondition(beliefs = "currentTime")
		public boolean checkRecur(ChangeEvent event) {
			System.out.println("Keeping goal at " + (long) event.getValue());
			return true;
		}
	}
	
	
	
	
	
	
	

	/*
	 * @Plan(trigger = @Trigger(goals = MoveTo.class)) public class
	 * MovingToPositionPlan {
	 * 
	 * @PlanBody protected void failingPlan() {
	 * 
	 * 
	 * System.out.println("Attempt at " + getCurrentTime()); if (agent.currentX
	 * != agent.finalPos.getXAsInteger() || agent.currentY !=
	 * agent.finalPos.getYAsInteger()) {
	 * 
	 * System.out.println("moving to x=" + finalPos.getXAsInteger() + ", y=" +
	 * finalPos.getYAsInteger());
	 * 
	 * if (finalPos.getXAsInteger() > currentX) { myself.setProperty("position",
	 * new Vector2Int(currentX + 1, currentY)); currentX += 1; }
	 * 
	 * if (finalPos.getXAsInteger() < currentX) { myself.setProperty("position",
	 * new Vector2Int(currentX - 1, currentY)); currentX -= 1; }
	 * 
	 * if (finalPos.getYAsInteger() > currentY) { myself.setProperty("position",
	 * new Vector2Int(currentX, currentY + 1)); currentY += 1; }
	 * 
	 * if (finalPos.getYAsInteger() < currentY) { myself.setProperty("position",
	 * new Vector2Int(currentX, currentY - 1)); currentY -= 1; }
	 * 
	 * throw new PlanFailureException();
	 * 
	 * } }
	 * 
	 * @PlanPassed public void passed() {
	 * System.out.println("Plan finished successfully at " + getCurrentTime());
	 * 
	 * System.out.println("changing plan");
	 * 
	 * Random r = new Random(); finalPos = new Vector2Int(r.nextInt(spaceWidth),
	 * r.nextInt(spaceHeight));
	 * 
	 * }
	 * 
	 * 
	 * 
	 * }
	 */

	@Override
	public Space2D getEnvironment() {
		return space;
	}

	@Override
	public ISpaceObject getMyself() {
		return myself;
	}

}
