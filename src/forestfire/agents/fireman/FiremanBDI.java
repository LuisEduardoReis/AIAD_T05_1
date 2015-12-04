package forestfire.agents.fireman;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Deliberation;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goal.ExcludeMode;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalDropCondition;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.annotation.Trigger;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Double;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.Random;

import forestfire.Util;
import forestfire.movement.MoveToLocationPlan;

@Agent
@Service
@Plans({
	@Plan(trigger = @Trigger(goals = { FiremanBDI.LookForFire.class }), body = @Body(LookForFirePlan.class)),
	@Plan(trigger = @Trigger(goals = { FiremanBDI.FightFire.class }), body = @Body(FightFirePlan.class)),
	@Plan(trigger = @Trigger(goals = { FiremanBDI.Move.class, FiremanBDI.RunFromFire.class }), body = @Body(MoveToLocationPlan.class))
})
@RequiredServices(@RequiredService(name="clockser", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)))
public class FiremanBDI {
	@Agent
	protected BDIAgent agent;

	@Belief
	protected ContinuousSpace2D space = (ContinuousSpace2D) agent
			.getParentAccess().getExtension("2dspace").get();

	@Belief
	protected ISpaceObject myself = space.getAvatar(
			agent.getComponentDescription(), agent.getModel().getFullName());

	// Health
	@Belief(updaterate=200)
	protected double health = (double) myself.getProperty("health");


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
	@Belief(updaterate = 200)
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
	@Belief(updaterate = 200)
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
		
		agent.dispatchTopLevelGoal(new LookForFire());
		agent.dispatchTopLevelGoal(new RunFromFire());
	}

	@Plan(trigger = @Trigger(factchangeds = "inDanger"))
	public void checkInDanger() {
		if (inDanger) System.out.println("Fireman " + myself.getId() + " is in danger!");
	}
	
	@Plan(trigger = @Trigger(factchangeds = "fireInRange"))
	public void checkFireInRange() {
		if (fireInRange) System.out.println("Fireman " + myself.getId() + " has fire in range");
	}
	
	@Goal(succeedonpassed = false, excludemode=ExcludeMode.Never)
	public static class LookForFire {}
	
	@Goal(deliberation=@Deliberation(inhibits=LookForFire.class, cardinalityone=true))
	public static class FightFire {
		
		@GoalCreationCondition(beliefs="fireInRange")
		public static boolean checkCreate(FiremanBDI fireman) {
			return fireman.fireInRange;
		}
		
		@GoalDropCondition(beliefs="fireInRange")
		public static boolean checkDrop(FiremanBDI fireman) {
			return !fireman.fireInRange;
		}
	}
	
	@Goal(excludemode=ExcludeMode.Never, deliberation=@Deliberation(inhibits={FightFire.class, LookForFire.class}, cardinalityone=true))
	public static class RunFromFire extends Move {		
		public RunFromFire() { 
			super(null);
		}

		@GoalMaintainCondition(beliefs = "inDanger")
		protected boolean maintain(FiremanBDI fireman) {
			return !fireman.inDanger;
		}

		@GoalTargetCondition(beliefs = "inDanger")
		protected boolean target(FiremanBDI fireman) {
			return !fireman.inDanger;
		}
		
		@GoalDropCondition(beliefs="health")
		public static boolean checkDrop(FiremanBDI fireman) {
			return fireman.health <= 0;
		}
	}
	
	@Goal
	public static class Move {
		protected IVector2 destination;
	
		public Move(IVector2 destination) {
			this.destination = destination;
		}

		public IVector2 getDestination() {
			return destination;
		}
	}

	
	public Space2D getEnvironment() {
		return space;
	}

	public ISpaceObject getMyself() {
		return myself;
	}

}
