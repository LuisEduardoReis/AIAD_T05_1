package forestfire.agents.fireman;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Deliberation;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goals;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.runtime.IGoal.GoalProcessingState;
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
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.ArrayList;
import java.util.Random;

import forestfire.Util;
import forestfire.agents.commander.IFiremanServices;
import forestfire.agents.fireman.goals.ApproachFireGoal;
import forestfire.agents.fireman.goals.ApproachHouseInDangerGoal;
import forestfire.agents.fireman.goals.FightFireGoal;
import forestfire.agents.fireman.goals.FollowDestinationOrderGoal;
import forestfire.agents.fireman.goals.LookForFireGoal;
import forestfire.agents.fireman.goals.RunFromFireGoal;
import forestfire.agents.fireman.goals.SaveHouseInDangerGoal;
import forestfire.movement.MoveGoal;
import forestfire.movement.MoveToLocationPlan;

@Agent
@Service
@Goals({
	@Goal(clazz=LookForFireGoal.class),
	@Goal(clazz=FollowDestinationOrderGoal.class, 	deliberation=@Deliberation(inhibits={LookForFireGoal.class  },cardinalityone=true)),
	@Goal(clazz=ApproachFireGoal.class, 			deliberation=@Deliberation(inhibits={LookForFireGoal.class, FollowDestinationOrderGoal.class  }, cardinalityone=true)),	
	@Goal(clazz=ApproachHouseInDangerGoal.class, 	deliberation=@Deliberation(inhibits={LookForFireGoal.class, FollowDestinationOrderGoal.class, ApproachFireGoal.class  },cardinalityone=true)),
	@Goal(clazz=RunFromFireGoal.class, 				deliberation=@Deliberation(inhibits={LookForFireGoal.class, FollowDestinationOrderGoal.class, ApproachFireGoal.class, ApproachHouseInDangerGoal.class  },cardinalityone = true)),
	
	@Goal(clazz=FightFireGoal.class, 				deliberation=@Deliberation(inhibits={LookForFireGoal.class, FollowDestinationOrderGoal.class}, cardinalityone = true)),
	@Goal(clazz=SaveHouseInDangerGoal.class, 		deliberation=@Deliberation(inhibits={LookForFireGoal.class, FollowDestinationOrderGoal.class, ApproachFireGoal.class, ApproachHouseInDangerGoal.class, FightFireGoal.class}, cardinalityone = true))
})
@Plans({
	@Plan(trigger = @Trigger(goals = { LookForFireGoal.class }), body = @Body(LookForFirePlan.class)),
	@Plan(trigger = @Trigger(goals = { FightFireGoal.class }), body = @Body(FightFirePlan.class)),
	@Plan(trigger = @Trigger(goals = { MoveGoal.class, RunFromFireGoal.class, ApproachFireGoal.class, FollowDestinationOrderGoal.class, ApproachHouseInDangerGoal.class }), body = @Body(MoveToLocationPlan.class)),
	@Plan(trigger = @Trigger(goals = { SaveHouseInDangerGoal.class }), body = @Body(SavePeoplePlan.class))
})
@ProvidedServices({
	@ProvidedService(type=IFiremanServices.class, implementation=@Implementation(expression="$pojoagent"))
})
@RequiredServices({
	@RequiredService(name="clockser", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
public class FiremanBDI implements IFiremanServices {
	public static final double SAFETY_RANGE = 2.0;
	public static final double SAVE_HOUSE_RANGE = 1.0;
	public static final double HOUSE_IN_DANGER_THRESHOLD = 7.5;

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
	
	// Distance to house
	@Belief
	protected double distanceToHouse = Double.MAX_VALUE; 
	
	// In Danger belief
	@Belief(dynamic=true)
	protected boolean inDanger = distanceToFire < SAFETY_RANGE;
	
	// Fire in Range belief
	@Belief(dynamic=true)
	protected boolean fireInRange = distanceToFire < actionRange;
	
	// Fire in View belief
	@Belief(dynamic=true)
	protected boolean fireInView = distanceToFire != Double.MAX_VALUE;
	
	// House being Saved belief
	@Belief
	protected IVector2 houseBeingSaved = null;
	
	// House in Range belief
	@Belief(dynamic=true)
	protected boolean houseInRange = distanceToHouse < SAVE_HOUSE_RANGE;
	
	// Destination Order
	@Belief
	protected Vector2Double destination_order;

	// View of space
	protected TerrainView terrain_view_aux = new TerrainView(space, myself);
	
	@Belief(updaterate = 200)
	protected TerrainView terrain_view = updateTerrainView();
	
	// Update view loop
	protected TerrainView updateTerrainView() {
		health = (double) myself.getProperty("health");
		
		if (health > 0) {
			terrain_view_aux.updateView();
			
			// Update view related beliefs
			
				// Calculate distance to fire
				distanceToFire = terrain_view_aux.distanceToNearestFire((IVector2) myself.getProperty("position"));
				
				// If not saving a house, find house to save 
				if (houseBeingSaved == null) 
					houseBeingSaved = terrain_view_aux.nearestHouseInDanger();
				// Else, check if house still needs saving
				else {
					ISpaceObject house = terrain_view_aux.getGlobal(houseBeingSaved.getXAsInteger(), houseBeingSaved.getYAsInteger());
					boolean has_people = (float) house.getProperty("people") > 0;
					boolean burned_down = ((float)house.getProperty("fuel") == 0);
					
					if (!has_people || burned_down) houseBeingSaved = null;
				}
				
				// Calculate distance to house
				distanceToHouse = getDistanceToHouse();
				//if (distanceToHouse < Double.MAX_VALUE) System.out.println(distanceToHouse);
			
				
			// Update visual properties
				
				// Fighting fire
				ArrayList<FightFireGoal> ffgc = (ArrayList<FightFireGoal>) agent.getGoals(FightFireGoal.class);
				myself.setProperty("fighting_fire", ffgc.size()>0 && agent.getGoal(ffgc.get(0)).getProcessingState() == GoalProcessingState.INPROCESS);
		}
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
		
		agent.dispatchTopLevelGoal(new LookForFireGoal());
		agent.dispatchTopLevelGoal(new RunFromFireGoal());		
	}

	// #### INLINE PLANS ####
	
	@Plan(trigger = @Trigger(factchangeds = "health"))
	public void checkAlive() {
		if (health <= 0) {
			//System.out.println("Fireman " + myself.getId() + " died!");
			agent.killAgent();
		}
	}
	
	@Plan(trigger = @Trigger(factchangeds = "inDanger"))
	public void checkInDanger() {
		//if (inDanger) System.out.println("Fireman " + myself.getId() + " is in danger!");
	}
	
	@Plan(trigger = @Trigger(factchangeds = "fireInRange"))
	public void checkFireInRange() {
		//if (fireInRange) System.out.println("Fireman " + myself.getId() + " has fire in range");
	}
	
	@Plan(trigger = @Trigger(factchangeds = "fireInView"))
	public void checkFireInView() {
		//if (fireInView) System.out.println("Fireman " + myself.getId() + " has fire in view");
	}
	
	@Plan(trigger = @Trigger(factchangeds = "houseBeingSaved"))
	public void checkPeopleInDanger() {
		if (houseBeingSaved != null) System.out.println("Fireman " + myself.getId() + " is going to save house at pos "+ houseBeingSaved.toString());
	}
		
	@Plan(trigger = @Trigger(factchangeds = "houseInRange"))
	public void checkPeopleInRange() {
		if (houseInRange) System.out.println("Fireman " + myself.getId() + " is saving house at pos "+ houseBeingSaved.toString());
	}
	
	
	
	// #### GETTERS ####

	
	public Space2D getEnvironment() { return space; }
	public ISpaceObject getMyself() { return myself; }
	public TerrainView getTerrainView() { return terrain_view; }
		
	public double getHealth() { return health; }
	public double getDistanceToFire() { return distanceToFire; }
	public boolean getInDanger() { return inDanger; }
	public boolean getFireInRange() { return fireInRange; }
	public boolean getFireInView() { return fireInView; }
	public IVector2 getDestinationOrder() { return destination_order; }
	public IVector2 getHouseBeingSaved() { return houseBeingSaved; }
	public boolean getHouseInRange() { return houseInRange; }	
	
	
	public double getDistanceToHouse(){
		if(houseBeingSaved == null) return Double.MAX_VALUE;
		IVector2 pos = (IVector2) myself.getProperty("position");
		return Util.pointDistance(pos.getXAsDouble(), pos.getYAsDouble(), houseBeingSaved.getXAsDouble(), houseBeingSaved.getYAsDouble());
	}

	// #### SERVICE IMPLEMENTATION ####
	
	@Override
	public TerrainView reportTerrainView() {
		if (health > 0) return terrain_view;
		else return null;
	}

	@Override
	public void giveDestinationOrder(double x, double y) {
		destination_order = new Vector2Double(x, y);
		agent.dispatchTopLevelGoal(new FollowDestinationOrderGoal(destination_order));
	}


}
