package forestfire.agents.fireman;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalDropCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.annotation.RawEvent;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.annotation.Goal.ExcludeMode;
import jadex.bdiv3.runtime.ChangeEvent;
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
	@Plan(body = @Body(FiremanPlan.class)),
	@Plan(trigger = @Trigger(goals = { FiremanBDI.LookForFire.class }), body = @Body(LookForFirePlan.class)), 
	@Plan(trigger = @Trigger(goals = { FiremanBDI.Move.class }), body = @Body(MoveToLocationPlan.class))
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
	public class LookForFire {}
	
	@Goal
	public class RunFromFire extends Move {
		public RunFromFire() { super(null); }
	}
	
	@Goal
	public class Move {
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
