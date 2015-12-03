package forestfire.movement;

import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;

public interface EnvAccessInterface {

	public Object getAgent();
	
	public Space2D getEnvironment();
	
	public ISpaceObject getMyself();
	
}
