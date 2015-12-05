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
	@Plan(trigger = @Trigger(goals = { FiremanBDI.Move.class, FiremanBDI.RunFromFire.class, FiremanBDI.ApproachFire.class }), body = @Body(MoveToLocationPlan.class))
})
@RequiredServices(@RequiredService(name="clockser", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)))
public class FiremanBDI {
	public static final double SAFETY_RANGE = 2;

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

	// Distance to fire
	@Belief
	protected double distanceToFire = Double.MAX_VALUE; 
	
	// In Danger belief
	@Belief(dynamic=true)
	protected boolean inDanger = distanceToFire < SAFETY_RANGE;
	
	// Fire in Range belief
	@Belief(dynamic=true)
	protected boolean fireInRange = distanceToFire < actionRange;
	
	// Fire in View belief
	@Belief(dynamic=true)
	protected boolean fireInView = distanceToFire != Double.MAX_VALUE;
	

	// View of space
	protected TerrainView terrain_view_aux = new TerrainView(space, myself, viewRange);
	
	@Belief(updaterate = 200)
	protected TerrainView terrain_view = updateTerrainView();

	protected TerrainView updateTerrainView() {
		terrain_view_aux.updateView();
		
		// Update Beliefs based on view
		health = (double) myself.getProperty("health");
		distanceToFire = terrain_view_aux.distanceToNearestFire();
		
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
	public static class LookForFire {
		// By default look for fire
	}
	
	// Approach fire
	@Goal(deliberation=@Deliberation(inhibits={LookForFire.class}, cardinalityone=true))
	public static class ApproachFire extends Move {

		public ApproachFire(double actionDistance) {
			super(null);
			this.approach_dist = actionDistance;
			//System.out.println("Goal Approach Fire");
		}
		
		// Approach fire when it enters view 
		@GoalCreationCondition(beliefs="fireInView")
		public static ApproachFire checkCreate(FiremanBDI fireman) {
			if (fireman.fireInView)
				return new ApproachFire(fireman.actionRange);
			else
				return null;
		}
		
		// Stop when fire is no longer in view
		@GoalDropCondition(beliefs="fireInView")
		public static boolean checkDrop(FiremanBDI fireman) {
			return !fireman.fireInView;
		}
		
	}
	
	// Fight fire, triggered when there is fire in range
	@Goal(deliberation=@Deliberation(inhibits={LookForFire.class, ApproachFire.class}, cardinalityone=true))
	public static class FightFire {
		public FightFire() {
			// System.out.println("Goal Fight Fire");
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
	
	// Run from fire, triggered when fireman is in danger
	@Goal(excludemode=ExcludeMode.Never, deliberation=@Deliberation(inhibits={FightFire.class, LookForFire.class, ApproachFire.class}, cardinalityone=true))
	public static class RunFromFire extends Move {		
		public RunFromFire() { 
			super(null);
			// System.out.println("Goal Fight Fire");
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
		protected double retreat_dist, approach_dist;
	
		public Move(IVector2 destination) {
			this.destination = destination;
			this.retreat_dist = FiremanBDI.SAFETY_RANGE;
			this.approach_dist = -1;
		}

		public IVector2 getDestination() {
			return destination;
		}

		public double getRetreatDist() {
			return retreat_dist;
		}
		
		public double getApproachDist() {
			return approach_dist;
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

	public double getDistanceToFire() {
		return distanceToFire;
	}

}
