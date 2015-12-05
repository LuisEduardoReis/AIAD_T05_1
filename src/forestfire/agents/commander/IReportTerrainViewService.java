package forestfire.agents.commander;

import jadex.bridge.service.annotation.Reference;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.TerrainView;


public interface IReportTerrainViewService {

	public void reportTerrainView(@Reference FiremanBDI fireman, @Reference TerrainView terrain_view);
	
}
