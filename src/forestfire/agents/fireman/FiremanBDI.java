package forestfire.agents.fireman;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalDropCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.annotation.RawEvent;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.service.annotation.Service;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.Vector2Double;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;

import java.util.Random;

import forestfire.Util;
import forestfire.movement.EnvAccessInterface;
import forestfire.movement.MoveToLocationPlan;
import forestfire.movement.MovementCapability;
import forestfire.movement.MovementCapability.Move;

@Agent
@Service
@Plans({
	@Plan(body = @Body(FiremanPlan.class)),
	@Plan(trigger = @Trigger(goals = { FiremanBDI.LookForFire.class }), body = @Body(LookForFirePlan.class)), 
	@Plan(trigger = @Trigger(goals = { FiremanBDI.RunFromFire.class }), body = @Body(MoveToLocationPlan.class))
})
public class FiremanBDI implements EnvAccessInterface{
	@Agent
	protected BDIAgent agent;

	@Capability
	MovementCapability movement = new MovementCapability();

	public MovementCapability getMovement() {
		return movement;
	}

	@Belief
	protected ContinuousSpace2D space = (ContinuousSpace2D) agent
			.getParentAccess().getExtension("2dspace").get();

	@Belief
	protected ISpaceObject myself = space.getAvatar(
			agent.getComponentDescription(), agent.getModel().getFullName());

	// Health
	@Belief
	public double getHealth() {
		return (double) myself.getProperty("health");
	}

	@Belief
	public void setHealth(double health) {
		myself.setProperty("health", health);
	}

	/*@Plan(trigger = @Trigger(factchangeds = "health"))
	protected void printHealthPlan(ChangeEvent e) {
		System.out.println("Ouch! Health at: " + ((double) e.getValue()));
	}*/

	// View of space
	@Belief
	public final int viewRange = (int) myself.getProperty("viewRange");

	protected ISpaceObject[] terrain = space.getSpaceObjectsByType("terrain");
	public final int terrain_width = space.getAreaSize().getXAsInteger();
	public final int terrain_height = space.getAreaSize().getYAsInteger();

	@Belief(updaterate = 200)
	ISpaceObject[] terrain_view = getTerrainView();
	public int terrain_view_pos_x, terrain_view_pos_y;

	public ISpaceObject getTerrainView(int x, int y) {
		return terrain_view[(y + viewRange) * (2 * viewRange + 1)
				+ (x + viewRange)];
	}

	protected ISpaceObject[] terrain_view_aux = null;

	protected ISpaceObject[] getTerrainView() {
		if (terrain_view_aux == null)
			terrain_view_aux = new ISpaceObject[(2 * viewRange + 1)
					* (2 * viewRange + 1)];

		Vector2Double position = (Vector2Double) myself.getProperty("position");
		int fx = position.getXAsInteger(), fy = position.getYAsInteger();
		terrain_view_pos_x = fx;
		terrain_view_pos_y = fy;

		for (int i = -viewRange; i <= viewRange; i++) {
			for (int j = -viewRange; j <= viewRange; j++) {
				int x = (fx + j + terrain_width) % terrain_width, y = (fy + i + terrain_height)
						% terrain_height;
				terrain_view_aux[(i + viewRange) * (2 * viewRange + 1)
						+ (j + viewRange)] = terrain[y * terrain_width + x];
			}
		}

		return terrain_view_aux;
	}

	// In Danger belief
	@Belief(updaterate = 1000)
	protected boolean inDanger = isInDanger();

	protected boolean isInDanger() {
		Vector2Double position = (Vector2Double) myself.getProperty("position");
		double x = position.getXAsDouble(), y = position.getYAsDouble();

		for (int i = -viewRange; i <= viewRange; i++) {
			for (int j = -viewRange; j <= viewRange; j++) {
				ISpaceObject terrain = getTerrainView(j, i);
				if ((float) terrain.getProperty("fire") >= 50f
						&& Util.pointDistance(x, y, terrain_view_pos_x + j,
								terrain_view_pos_y + i) < 2)
					return true;
			}
		}
		return false;
	}

	// Fire in Range belief
	@Belief(updaterate = 1000)
	protected boolean fireInRange = isFireInRange();

	protected boolean isFireInRange() {		
		for (int i = -viewRange; i <= viewRange; i++) {
			for (int j = -viewRange; j <= viewRange; j++) {
				ISpaceObject terrain = getTerrainView(j, i);
				if ((float) terrain.getProperty("fire") >= 50f)
					return true;
			}
		}
		return false;
	}
	
	@AgentBody
	public void body() {
		Random r = new Random();
		myself.setProperty("position", new Vector2Double(
				r.nextDouble() * space.getAreaSize().getXAsDouble(), 
				r.nextDouble() * space.getAreaSize().getYAsDouble()
		));

		System.out.println("Fireman " + myself.getId() + " running.");
		agent.adoptPlan(new FiremanPlan());
	}

	@Plan(trigger = @Trigger(factchangeds = "inDanger"))
	public void checkInDanger() {
		if (inDanger) System.out.println("Fireman " + myself.getId() + " is in danger!");
	}
	
	@Plan(trigger = @Trigger(factchangeds = "fireInRange"))
	public void checkFireInRange() {
		if (fireInRange) System.out.println("Fireman " + myself.getId() + " has fire in range");
	}
	
	@Goal
	public class LookForFire {
		
		@GoalDropCondition(rawevents = { @RawEvent(ChangeEvent.FACTCHANGED) })
		public boolean checkDrop() {
			return fireInRange;
		}
	}
	
	@Goal
	public class RunFromFire extends Move {
		
		public RunFromFire() {
			movement.super(null);
		}
	}

	/*
	 * @Goal(recur = true) public class Explore {
	 * 
	 * @GoalCreationCondition(beliefs = "finalPos") public Explore() { }
	 * 
	 * @GoalRecurCondition(beliefs = "currentTime") public boolean
	 * checkRecur(ChangeEvent event) { System.out.println("Keeping goal at " +
	 * (long) event.getValue()); return true; } }
	 * 
	 * 
	 * @Goal(recur = true) public class MoveTo {
	 * 
	 * @GoalCreationCondition(beliefs = "finalPos") public MoveTo() { }
	 * 
	 * @GoalRecurCondition(beliefs = "currentTime") public boolean
	 * checkRecur(ChangeEvent event) { System.out.println("Keeping goal at " +
	 * (long) event.getValue()); return true; } }
	 */

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
	public Object getAgent() {
		return this;
	}
	
	@Override
	public Space2D getEnvironment() {
		return space;
	}
	
	@Override
	public ISpaceObject getMyself() {
		return myself;
	}

}
