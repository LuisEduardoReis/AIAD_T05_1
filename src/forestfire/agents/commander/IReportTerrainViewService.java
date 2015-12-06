package forestfire.agents.commander;

import jadex.bridge.service.annotation.Reference;
import forestfire.agents.fireman.TerrainView;


public interface IReportTerrainViewService {

	public @Reference TerrainView reportTerrainView();
	
}
