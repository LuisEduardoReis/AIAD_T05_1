package forestfire.agents.commander;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.types.clock.IClockService;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.TerrainView;

@Agent
@Service
@ProvidedServices({
	@ProvidedService(type=IReportTerrainViewService.class, implementation=@Implementation(expression="$pojoagent"))
})
@RequiredServices({
	@RequiredService(name="clockser", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
public class CommanderBDI implements IReportTerrainViewService {
	
	@Agent
	protected BDIAgent agent;
	
	@Belief
	protected ContinuousSpace2D space = (ContinuousSpace2D) agent
			.getParentAccess().getExtension("2dspace").get();

	
	@AgentBody
	public void body() {
		
	}

	@Override
	public void reportTerrainView(FiremanBDI fireman, TerrainView terrain_view) {
		//System.out.println(terrain_view);
	}

}
