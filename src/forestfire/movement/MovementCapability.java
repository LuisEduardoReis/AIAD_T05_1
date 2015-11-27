package forestfire.movement;

import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.runtime.ICapability;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

@Capability
@Plans(@Plan(trigger=@Trigger(goals={MovementCapability.Move.class}), body=@Body(forestfire.movement.MoveToLocationPlan.class)))
@RequiredServices(@RequiredService(name="clockser", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)))
public class MovementCapability implements EnvAccessInterface {

	@Agent
	protected ICapability capability;

	/** The environment. */
	protected Space2D env = (ContinuousSpace2D) capability.getAgent()
			.getParentAccess().getExtension("2dspace").get();
	
	/** The environment. */
	protected ISpaceObject myself = env.getAvatar(capability.getAgent()
			.getComponentDescription(), capability.getAgent().getModel()
			.getFullName());

	@Override
	public Space2D getEnvironment() {
		return env;
	}

	@Override
	public ISpaceObject getMyself() {
		return myself;
	}
	
	public IVector2 getPosition()
	{
		return (IVector2)getMyself().getProperty("position");
	}
	
	public ICapability getCapability()
	{
		return capability;
	}

	@Goal
	public class Move implements DestinationInterface {
		/** The destination. */
		protected IVector2 destination;

		/**
		 * Create a new Move.
		 */
		public Move(IVector2 destination) {
			this.destination = destination;
		}

		/**
		 * Get the destination.
		 * 
		 * @return The destination.
		 */
		public IVector2 getDestination() {
			return destination;
		}
	}

}
