package movement;

import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.runtime.ICapability;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.micro.annotation.Agent;

@Capability
public class MovementCapability implements EnvAccessInterface {

	@Agent
	protected ICapability capability;

	/** The environment. */
	protected Space2D env = (ContinuousSpace2D) capability.getAgent()
			.getParentAccess().getExtension("my2dspace").get();

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
