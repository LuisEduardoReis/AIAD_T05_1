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

	// #### BELIEFS ####
	
	@Belief
	protected ContinuousSpace2D space = (ContinuousSpace2D) agent
			.getParentAccess().getExtension("2dspace").get();

	@Belief
	protected ISpaceObject myself = space.getAvatar(
			agent.getComponentDescription(), agent.getModel().getFullName());
	
	// View Range
	@Belief
	public final int viewRange = (int) myself.getProperty("viewRange");

	// Action Range
	@Belief
	public final double actionRange = (double) myself.getProperty("actionRange");
	
	// Health
	@Belief
	protected double health;

	// In Danger belief
	@Belief
	protected boolean inDanger;

	// Fire in View belief
	@Belief
	protected boolean fireInView;
	
	// Fire in Range belief
	@Belief
	protected boolean fireInRange;
	

	// View of space
	protected TerrainView terrain_view_aux = new TerrainView(space, myself, viewRange);
	
	@Belief(updaterate = 200)
	protected TerrainView terrain_view = updateTerrainView();

	protected TerrainView updateTerrainView() {
		terrain_view_aux.updateView();
		double dist = terrain_view_aux.distanceToNearestFire();
		
		// Update Beliefs based on view
		health = (double) myself.getProperty("health");
		
		inDanger = dist < 2;
		fireInRange = dist < actionRange;
		fireInView = dist != Double.MAX_VALUE;		
		
		return terrain_view_aux;
	}
	
	// #### AGENT BODY ####

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

	// #### INLINE PLANS ####
	
	@Plan(trigger = @Trigger(factchangeds = "inDanger"))
	public void checkInDanger() {
		if (inDanger) System.out.println("Fireman " + myself.getId() + " is in danger!");
	}
	
	@Plan(trigger = @Trigger(factchangeds = "fireInRange"))
	public void checkFireInRange() {
		if (fireInRange) System.out.println("Fireman " + myself.getId() + " has fire in range");
	}
	
	@Plan(trigger = @Trigger(factchangeds = "fireInView"))
	public void checkFireInView() {
		if (fireInView) System.out.println("Fireman " + myself.getId() + " has fire in view");
	}
	
	// #### GOALS ####
	
	// Look for fire, default goal
	@Goal
	public static class LookForFire {}
	
	// Fight fire, triggered when there is fire in range
	@Goal(deliberation=@Deliberation(inhibits=LookForFire.class, cardinalityone=true))
	public static class FightFire {
		
		public FightFire() {
			System.out.println("Created FightFire");
		}
		
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
			return true;
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

	public TerrainView getTerrain_view() {
		return terrain_view;
	}

}
