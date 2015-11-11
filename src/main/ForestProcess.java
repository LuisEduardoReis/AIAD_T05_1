package main;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SimplePropertyObject;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceProcess;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.Vector2Int;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ForestProcess extends SimplePropertyObject implements ISpaceProcess {

	@Override
    public void start(IClockService arg0, IEnvironmentSpace arg1) {

        Space2D space = (Space2D)arg1;

        int spaceHeight = space.getAreaSize().getXAsInteger();
        int spaceWidth = space.getAreaSize().getYAsInteger();

        Random r = new Random();

        // Add terrain items to grid
        for(int i = 0; i < spaceHeight; i++) {
        	for(int j = 0; j < spaceWidth; j++) {
        		Map<String, Object> properties = new HashMap<String, Object>();
        		properties.put("position", new Vector2Int(j, i));
        		int type = r.nextFloat() < 0.15 ? r.nextInt(3)+1 : 0;
        		properties.put("type", (int) type);
        		properties.put("fire", (float) 0);
        		properties.put("fuel", (float) (100 + type*50));
        		
        		space.createSpaceObject("terrain", properties, null);
        	}
        }

    }

    @Override
    public void shutdown(IEnvironmentSpace iEnvironmentSpace) {

    }

    @Override
    public void execute(IClockService iClockService, IEnvironmentSpace iEnvironmentSpace) {

    }



}
