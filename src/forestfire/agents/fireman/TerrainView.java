package forestfire.agents.fireman;

import forestfire.Util;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.math.Vector2Double;

public class TerrainView {
	
	public final int viewRange;
	public final int terrain_width, terrain_height;
	
	protected ISpaceObject[] terrain;
	protected ISpaceObject[] terrain_view;
	protected ContinuousSpace2D space;
	protected ISpaceObject myself;
	
	protected int pos_x, pos_y;

	public TerrainView(ContinuousSpace2D space, ISpaceObject myself, int viewRange) {
		this.space = space;
		this.myself = myself;
		this.terrain = space.getSpaceObjectsByType("terrain");
		this.terrain_view = new ISpaceObject[(2 * viewRange + 1) * (2 * viewRange + 1)];
		
		this.viewRange = viewRange;
		this.terrain_width = space.getAreaSize().getXAsInteger();
		this.terrain_height = space.getAreaSize().getYAsInteger();
	}

	public void updateView() {
		Vector2Double position = (Vector2Double) myself.getProperty("position");
		pos_x = position.getXAsInteger();
		pos_y = position.getYAsInteger();

		for (int i = -viewRange; i <= viewRange; i++) {
			for (int j = -viewRange; j <= viewRange; j++) {
				int x = (pos_x + j + terrain_width) % terrain_width, 
					y = (pos_y + i + terrain_height)% terrain_height;
				/*float fire = (float) terrain[y * terrain_width + x].getProperty("fire");
				if (fire > 50)
					System.out.print("x ");
				else
					System.out.print("  ");*/
				set(j,i, terrain[y * terrain_width + x]);
			}
			//System.out.println();
		}
		//System.out.println();
	}
	
	public ISpaceObject get(int x, int y) {
		return terrain_view[(y + viewRange) * (2 * viewRange + 1) + (x + viewRange)];
	}
	public void set(int x, int y, ISpaceObject obj) {
		terrain_view[(y + viewRange) * (2 * viewRange + 1) + (x + viewRange)] = obj;
	}
	
	public double distanceToNearestFire() {
		Vector2Double position = (Vector2Double) myself.getProperty("position");
		double x = position.getXAsDouble(), y = position.getYAsDouble();

		double min = Double.MAX_VALUE;
		
		for (int i = -viewRange; i <= viewRange; i++) {
			for (int j = -viewRange; j <= viewRange; j++) {
				ISpaceObject terrain = get(j, i);
				if ((float) terrain.getProperty("fire") >= 50f)
					min = Math.min(min, Util.pointDistance(x, y, pos_x + j, pos_y + i));
			}
		}
		return min;
	}
	
	public int getPosX() { return pos_x; }
	public int getPosY() { return pos_y; }
}
