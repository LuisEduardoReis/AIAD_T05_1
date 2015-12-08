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
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import forestfire.agents.fireman.TerrainView;

@Agent
@Service
@RequiredServices({
	@RequiredService(name="clockser", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="reportviewservice", type=IReportTerrainViewService.class, multiple=true, binding=@Binding(dynamic=true, scope=Binding.SCOPE_PLATFORM)),
	@RequiredService(name="giveorderservice", type=IGiveOrderService.class, multiple=true, binding=@Binding(dynamic=true, scope=Binding.SCOPE_PLATFORM))
})
public class CommanderBDI {
	
	@Agent
	protected BDIAgent agent;
	
	@Belief
	protected ContinuousSpace2D space = (ContinuousSpace2D) agent
			.getParentAccess().getExtension("2dspace").get();

	@Belief
	protected int terrain_width = space.getAreaSize().getXAsInteger();
	@Belief
	protected int terrain_height = space.getAreaSize().getYAsInteger();

	@Belief(updaterate=10000)
	protected float fire_status[] = updateFireStatus();
	
	@Belief(updaterate=10000)
	protected float people_danger[] = updatePeopleInDanger();
	
	protected float fire_status_aux[];
	protected float[] updateFireStatus() {
		if (fire_status_aux == null)
			fire_status_aux = new float[terrain_width * terrain_height];
		
		for(int i = 0; i < fire_status_aux.length; i++) fire_status_aux[i] = 0;
		
		System.out.println("\nCommander asks for report");
		for (Object service : agent.getRequiredServices("reportviewservice").get()) {
			TerrainView tv = ((IReportTerrainViewService) service).reportTerrainView();
			if (tv == null) continue;
			
			int vr = tv.viewRange,
				pos_x = tv.getPosX(),
				pos_y = tv.getPosY();

			for (int i = -vr; i <= vr; i++) {
				for (int j = -vr; j <= vr; j++) {
					float fire = (float) tv.get(i, j).getProperty("fire");
					setFireStatus(pos_x + j + terrain_width, pos_y + i + terrain_height, fire);
					/*if (fire > 50f) 
						System.out.print("x ");
					else
						System.out.print("  ");*/
				}
				//System.out.println();
			}
			//System.out.println();
		}
		
		System.out.println("\nCommander sends orders");
		for (Object service : agent.getRequiredServices("giveorderservice").get()) {
			// TODO
			((IGiveOrderService) service).giveDestinationOrder(20, 20);
		}
		
		return fire_status_aux;
	}
	
	protected float getFireStatus(int x, int y) {
		return fire_status_aux[(y % terrain_height) * terrain_width + (x % terrain_width)];
	}
	protected void setFireStatus(int x, int y, float v) {
		fire_status_aux[(y % terrain_height) * terrain_width + (x % terrain_width)] = v;
	}

	
	
	
	protected float[] updatePeopleInDanger() {
		
		return null;
		
		
	}
	
	@AgentBody
	public void body() {
		
	}

}
