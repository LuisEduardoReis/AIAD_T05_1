package forestfire.agents.fireman;

import java.util.ArrayList;

import forestfire.Util;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Double;
import jadex.extension.envsupport.math.Vector2Int;

public class TerrainView {

	public final int viewRange;
	public final int terrain_width, terrain_height;

	protected ISpaceObject[] terrain;
	protected ISpaceObject[] terrain_view;
	protected ContinuousSpace2D space;
	protected ISpaceObject myself;

	protected int pos_x, pos_y;

	protected ArrayList<IVector2> houses;
	
	

	public TerrainView(ContinuousSpace2D space, ISpaceObject myself) {
		this.space = space;
		this.myself = myself;
		this.terrain = space.getSpaceObjectsByType("terrain");
		this.viewRange = (int) myself.getProperty("viewRange");
		this.terrain_view = new ISpaceObject[(2 * viewRange + 1)
				* (2 * viewRange + 1)];

		
		this.terrain_width = space.getAreaSize().getXAsInteger();
		this.terrain_height = space.getAreaSize().getYAsInteger();		
		

		houses = new ArrayList<>();
		for (int i = 0; i < terrain_height; i++) {
			for (int j = 0; j < terrain_width; j++) {
				if((int)terrain[i*terrain_width+j].getProperty("type") == Util.HOUSE)
					houses.add(new Vector2Int(j,i));
			}
		}

	}

	public void updateView() {
		Vector2Double position = (Vector2Double) myself.getProperty("position");
		pos_x = (int) Math.floor(position.getXAsDouble());
		pos_y = (int) Math.floor(position.getYAsDouble());

		for (int i = -viewRange; i <= viewRange; i++) {
			for (int j = -viewRange; j <= viewRange; j++) {
				int x = (pos_x + j + terrain_width) % terrain_width, y = (pos_y
						+ i + terrain_height)
						% terrain_height;
				/*
				 * float fire = (float) terrain[y * terrain_width +
				 * x].getProperty("fire"); if (fire > 50)
				 * System.out.print("x "); else System.out.print("  ");
				 */
				set(j, i, terrain[y * terrain_width + x]);
			}
			// System.out.println();
		}
		// System.out.println();
	}

	public ISpaceObject get(int x, int y) {
		return terrain_view[(y + viewRange) * (2 * viewRange + 1)
				+ (x + viewRange)];
	}

	public void set(int x, int y, ISpaceObject obj) {
		terrain_view[(y + viewRange) * (2 * viewRange + 1) + (x + viewRange)] = obj;
	}

	public double distanceToNearestFire(IVector2 position) {
		double x = position.getXAsDouble(), y = position.getYAsDouble();

		double min = Double.MAX_VALUE;

		for (int i = -viewRange; i <= viewRange; i++) {
			for (int j = -viewRange; j <= viewRange; j++) {
				ISpaceObject terrain = get(j, i);
				if ((float) terrain.getProperty("fire") >= 50f)
					min = Math.min(min,
							Util.pointDistance(x, y, pos_x + j, pos_y + i));
			}
		}
		return min;
	}

	public IVector2 nearestHouseInDanger() {
		
		IVector2 nearest = null;
		double min_dist = FiremanBDI.HOUSE_IN_DANGER_THRESHOLD;
		for(int i = 0; i < houses.size(); i++){
			double house_dist = distanceToNearestFire(houses.get(i));
			if (house_dist < min_dist) {
				ISpaceObject house = getGlobal(houses.get(i).getXAsInteger(), houses.get(i).getYAsInteger());
				boolean has_people = (float) house.getProperty("people") > 0;
				boolean burned_down = ((float)house.getProperty("fuel") == 0);
				
				if (has_people && !burned_down) {
					min_dist = house_dist;
					nearest = houses.get(i);
				}
			}
			
		}
		
		return nearest;
		
	}
	

	

	public int getPosX() {
		return pos_x;
	}

	public int getPosY() {
		return pos_y;
	}
	
	
	public ISpaceObject getGlobal(int x, int y) {
		return terrain[(y % terrain_height) * terrain_width + (x % terrain_width)];
	}
	
}
