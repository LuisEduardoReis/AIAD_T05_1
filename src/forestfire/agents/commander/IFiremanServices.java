package forestfire.agents.commander;

import jadex.bridge.service.annotation.Reference;
import forestfire.agents.fireman.TerrainView;

public interface IFiremanServices {

	public @Reference TerrainView reportTerrainView();
	
	public void giveDestinationOrder(double x, double y);
	
}
