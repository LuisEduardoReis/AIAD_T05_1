package forestfire.agents.commander;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.IFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Int;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.ArrayList;
import java.util.Collection;

import forestfire.Util;
import forestfire.agents.fireman.FiremanBDI;
import forestfire.agents.fireman.TerrainView;

@Agent
@Service
@RequiredServices({
	@RequiredService(name="clockser", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="firemanservices", type=IFiremanServices.class, multiple=true, binding=@Binding(dynamic=true, scope=Binding.SCOPE_PLATFORM))
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
	
	
	protected float fire_status_aux[];
	protected float[] updateFireStatus() {
		if (fire_status_aux == null)
			fire_status_aux = new float[terrain_width * terrain_height];
		
		for(int i = 0; i < fire_status_aux.length; i++) fire_status_aux[i] = 0;
		
		boolean commander_can_see_fire = false;
		ArrayList<Integer> firemen_who_need_orders = new ArrayList<Integer>();
		ArrayList<IVector2> firemen_who_need_orders_pos = new ArrayList<IVector2>();
		
		if (firemen != null) {
			System.out.println("\nCommander asks for report");
			for (int i = 0; i < firemen.size(); i++) {
				IFiremanServices service = firemen.get(i);
				TerrainView tv = ((IFiremanServices) service).reportTerrainView();
				if (tv == null) continue;
				
				int vr = tv.viewRange,
					pos_x = tv.getPosX(),
					pos_y = tv.getPosY();
				
				boolean fireman_can_see_fire = false;
				for (int y = -vr; y <= vr; y++) {
					for (int x = -vr; x <= vr; x++) {
						float fire = (float) tv.get(x, y).getProperty("fire");
						if (fire > 50) fireman_can_see_fire = true;
						setFireStatus(pos_x + x + terrain_width, pos_y + y + terrain_height, fire);
					}
				}
				
				if (fireman_can_see_fire) commander_can_see_fire = true;
				else {
					firemen_who_need_orders.add(i);
					firemen_who_need_orders_pos.add(new Vector2Int(tv.getPosX(), tv.getPosY()));
				}
			}
		}
		
		if (commander_can_see_fire) {
			System.out.println("\nCommander sends orders");
			for (int i = 0; i < firemen_who_need_orders.size(); i++) {
				int ind = firemen_who_need_orders.get(i);
				IVector2 pos = firemen_who_need_orders_pos.get(i);
				
				IFiremanServices service = firemen.get(ind);
				
				giveOrderToNearestFire(service, pos);
			}
		}
		
		return fire_status_aux;
	}
	
	protected void giveOrderToNearestFire(IFiremanServices service, IVector2 pos) {
		double mindist = Double.MAX_VALUE;
		int fx = pos.getXAsInteger(), fy= pos.getYAsInteger(), tx = 0, ty = 0;
		
		for(int y = 0; y<terrain_width; y++){
			for(int x = 0; x<terrain_height; x++){
				if (getFireStatus(x, y)>50) {
					double dist = Util.pointDistanceToroidalWorld(fx,fy, x, y, terrain_width, terrain_height);
					if (dist < mindist) {
						mindist = dist;
						tx = x; ty = y;
					}
				}
			}
		}
		
		if (mindist < Double.MAX_VALUE) {
			System.out.println("Sent fireman to " + tx + ", " + ty);
			service.giveDestinationOrder(tx, ty);
		}
	}

	protected float getFireStatus(int x, int y) {
		return fire_status_aux[(y % terrain_height) * terrain_width + (x % terrain_width)];
	}
	protected void setFireStatus(int x, int y, float v) {
		fire_status_aux[(y % terrain_height) * terrain_width + (x % terrain_width)] = v;
	}

	protected ArrayList<IFiremanServices> firemen;
	
	@AgentBody
	public void body() {
		firemen = new ArrayList<IFiremanServices>();
		IFuture<Collection<IFiremanServices>> firemenservices = agent.getServiceContainer().getRequiredServices("firemanservices");
		firemenservices.addResultListener(new IntermediateDefaultResultListener<IFiremanServices>() {
			public void intermediateResultAvailable(IFiremanServices cs) {
				firemen.add(cs);
			}
		});
	}

}
