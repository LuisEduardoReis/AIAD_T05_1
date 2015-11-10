package agents;

import jadex.bdi.runtime.PlanFailureException;
import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goal.ExcludeMode;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalRecurCondition;
import jadex.bdiv3.annotation.GoalResult;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanPassed;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Grid2D;
import jadex.extension.envsupport.math.Vector2Double;
import jadex.extension.envsupport.math.Vector2Int;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;

import java.util.ArrayList;
import java.util.Random;

@Agent
public class FiremanBDI {
	@Agent
	protected BDIAgent agent;

	@Belief
	protected Grid2D space = (Grid2D) agent.getParentAccess()
			.getExtension("2dspace").get();

	@Belief
	protected ISpaceObject myself = space.getAvatar(
			agent.getComponentDescription(), agent.getModel().getFullName());


	protected int spaceHeight = space.getAreaSize().getXAsInteger();
	protected int spaceWidth = space.getAreaSize().getYAsInteger();

	
	@Belief
	protected long startingTime = System.currentTimeMillis();
	
	@Belief
	protected int n = 0;

	public long getStartingTime() {
		return startingTime;
	}

	@Belief(updaterate = 1000)
	protected long currentTime = System.currentTimeMillis();

	public long getCurrentTime() {
		return currentTime;
	}

	@AgentBody
	public void body() {

		myself.setProperty("position", new Vector2Int(0, 0));
		
		agent.dispatchTopLevelGoal(new Move());

		System.out.println("Finished");

	}

	@Plan(trigger = @Trigger(factchangeds = "currentTime"))
	protected void move(ChangeEvent event) {
		Random r = new Random();
		 //myself.setProperty("position", new Vector2Int(r.nextInt(spaceWidth), r.nextInt(spaceHeight)));
		Vector2Int currentPos = (Vector2Int) myself.getProperty("position");
		myself.setProperty("position", new Vector2Int(currentPos.getXAsInteger()+1, currentPos.getYAsInteger()+1));
		

	}

	
	@Goal(excludemode=ExcludeMode.Never)
	public class Move {

		@GoalResult
		protected Vector2Int final_pos;

		@GoalMaintainCondition(beliefs="n")
		protected boolean maintain() {
			return n<=4;
		}
 
		@GoalTargetCondition(beliefs="n")
		protected boolean target() {
			return n<3;
		}

	}

	
	boolean checkNextToFire(){
		Vector2Int currentPos = (Vector2Int) myself.getProperty("position");
		int x = currentPos.getXAsInteger();
		int y = currentPos.getYAsInteger();
		
		//if(space.getr)
		
		
		return false;
		
	}

}
