package forestfire;

import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SimplePropertyObject;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.ISpaceProcess;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.Vector2Double;

import java.util.Random;

public class FireProcess extends SimplePropertyObject implements ISpaceProcess {

	public class State {
		public float[] fire, fuel;
		public int w, h;

		public State(int w, int h) {
			this.w = w;
			this.h = h;
			fire = new float[w * h];
			fuel = new float[w * h];
		}

		private int getCoord(int x, int y) {
			return ((y + h) % h) * w + ((x + w) % w);
		}

		public float getFire(int x, int y) {
			return fire[getCoord(x, y)];
		}

		public float getFuel(int x, int y) {
			return fuel[getCoord(x, y)];
		}

		public void setFire(int x, int y, float v) {
			fire[getCoord(x, y)] = v;
		}

		public void setFuel(int x, int y, float v) {
			fuel[getCoord(x, y)] = v;
		}

		public void incFire(int x, int y, float v) {
			int i = getCoord(x, y);
			fire[i] = Math.min(fire[i] + v, 100);
		}

		public void decFire(int x, int y, float v) {
			int i = getCoord(x, y);
			fire[i] = Math.max(fire[i] - v, 0);
		}

		public void decFuel(int x, int y, float v) {
			int i = getCoord(x, y);
			fuel[i] = Math.max(fuel[i] - v, 0);
		}
	}

	Space2D space;
	State current, next;
	ISpaceObject[] terrain;

	int w, h;
	Random r = new Random();

	@Override
	public void start(IClockService arg0, IEnvironmentSpace arg1) {

		space = (Space2D) arg1;

		this.w = space.getAreaSize().getXAsInteger();
		this.h = space.getAreaSize().getYAsInteger();

		this.terrain = space.getSpaceObjectsByType("terrain");

		// Start fire
		//terrain[Math.round(h / 2) * w + Math.round(w / 2)].setProperty("fire",100.0f);
		for(int i = 0; i < 5; i++)
			terrain[r.nextInt(h) * w + r.nextInt(w)].setProperty("fire",100.0f);
		
		// Set wind
		setRandomWind();

		this.current = new State(w, h);
		this.next = new State(w, h);
	}

	@Override
	public void shutdown(IEnvironmentSpace iEnvironmentSpace) {

	}

	@Override
	public void execute(IClockService iClockService,
			IEnvironmentSpace iEnvironmentSpace) {

		int firemen_dead = 0, people_dead = 0, area_burned = 0, houses_burned = 0;
		
		// Debug fire
		/*
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				System.out.format(" %02.0f", current.getFire(j, i));
			}
			System.out.println();
		}*/
		
		// Propagate fire
		for (int i = 0; i < terrain.length; i++) {
			current.fire[i] = next.fire[i] = (float) terrain[i].getProperty("fire");
			current.fuel[i] = next.fuel[i] = (float) terrain[i].getProperty("fuel");
			
			if (current.fuel[i] == 0) {
				area_burned++;
				if ((int) terrain[i].getProperty("type") == Util.HOUSE) {
					houses_burned++;
					if ((float) terrain[i].getProperty("people") > 0)
						people_dead++;
				}
			}			
		}

		float windDir = ((float) space.getProperty("wind_direction"))* Util.degToRad;
		float windVel = ((float) space.getProperty("wind_velocity"));

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float fire = current.getFire(x, y);

				if (current.getFuel(x, y) <= 0)
					next.decFire(x, y, 1);

				if (fire >= 50) {
					if (current.getFuel(x, y) > 0) next.incFire(x, y, 1);
					next.decFuel(x, y, 2);

					for (int i = 0; i < 3; i++) {
						float t = r.nextFloat();
						float deltaDir = (float) Math.pow(t, 1 + windVel);
						if (r.nextBoolean())
							deltaDir = -deltaDir;
						deltaDir *= Util.PI;

						float spreadDirection = windDir + deltaDir;

						int tx = Math.round(x + Util.fcos(spreadDirection)) % w, ty = Math
								.round(y - Util.fsin(spreadDirection)) % h;

						if (next.getFire(tx, ty) < 50f) next.incFire(tx, ty, current.getFuel(tx, ty) / 100);
					}
				}

			}
		}

		for (int i = 0; i < terrain.length; i++) {
			terrain[i].setProperty("fire", next.fire[i]);
			terrain[i].setProperty("fuel", next.fuel[i]);
		}

		// Hurt fireman
		for (ISpaceObject fireman : space.getSpaceObjectsByType("fireman")) {
			Vector2Double pos = (Vector2Double) fireman.getProperty("position");
			float fire = next.getFire(pos.getXAsInteger(), pos.getYAsInteger());
			double current_health = (double) fireman.getProperty("health");
			if (fire > 50)
				fireman.setProperty("health", Math.max(0.0, current_health - 10));
			
			if (current_health == 0) firemen_dead++;			
		}

		// Change wind
		if (r.nextInt(250) == 0)
			setRandomWind();
		
		// Update statistics
		space.setProperty("area_burned", area_burned);
		space.setProperty("houses_burned", houses_burned);
		space.setProperty("people_dead", people_dead);
		space.setProperty("firemen_dead", firemen_dead);
	}

	private void setRandomWind() {
		space.setProperty("wind_direction", (float) 360 * r.nextFloat());
		space.setProperty("wind_velocity", (float) 1 + r.nextFloat());
	}

}
