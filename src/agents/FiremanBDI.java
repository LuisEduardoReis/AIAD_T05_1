package agents;

import jadex.bdi.runtime.PlanFailureException;
import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goal.ExcludeMode;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalRecurCondition;
import jadex.bdiv3.annotation.GoalResult;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanPassed;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Grid2D;
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

import main.Util;

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
	protected int currentX = 10;
	@Belief
	protected int currentY = 9;

	@Belief
	protected Vector2Int finalPos;

	public long getStartingTime() {
		return startingTime;
	}

	@Belief(updaterate = 200)
	protected long currentTime = System.currentTimeMillis();

	public long getCurrentTime() {
		return currentTime;
	}

	ISpaceObject[] terrain;

	@AgentBody
	public void body() {

		myself.setProperty("position", new Vector2Int(currentX, currentY));
		this.terrain = space.getSpaceObjectsByType("terrain");

		// agent.dispatchTopLevelGoal(new Move());

		finalPos = new Vector2Int(currentX + 5, currentY);

		System.out.println("Finished");

	}

	@Plan
	// (trigger = @Trigger(factchangeds = "currentTime"))
	protected void moveForward(ChangeEvent event) {
		Random r = new Random();
		// myself.setProperty("position", new Vector2Int(r.nextInt(spaceWidth),
		// r.nextInt(spaceHeight)));
		myself.setProperty("position", new Vector2Int(currentX + 1,
				currentY + 1));
		currentX += 1;
		currentY += 1;

	}

	int n = 0;

	@Goal(excludemode = ExcludeMode.Never)
	public class Move {

		@GoalResult
		protected Vector2Int final_pos;

		// objetivo a manter
		@GoalMaintainCondition(beliefs = "currentX")
		protected boolean maintain() {
			return !checkNextToFire();
		}

		// executa até esta condição qnd o maintain goal é violado
		@GoalTargetCondition(beliefs = "currentX")
		protected boolean target() {
			return n < 3;
		}

	}

	@Plan(trigger = @Trigger(goals = Move.class))
	protected void runAway() {
		myself.setProperty("position", new Vector2Int(currentX - 3,
				currentY - 3));
		currentX -= 3;
		currentY -= 3;
		n++;

	}

	@Goal(recur=true)
	public class MoveTo {

		@GoalCreationCondition(beliefs = "finalPos")
		public MoveTo() {
		}
		
		@GoalRecurCondition(beliefs="currentTime")
		public boolean checkRecur(ChangeEvent event) {
			System.out.println("Keeping goal at " + (long) event.getValue());
			
			return true;
		}
	}
	
	@Plan(trigger=@Trigger(goals=MoveTo.class))
	public class MovingToPositionPlan {
 
		@PlanBody
		protected void failingPlan() {
			System.out.println("Attempt at " + getCurrentTime());
			if(currentX != finalPos.getXAsInteger() || currentY != finalPos.getYAsInteger()) {
				
				System.out.println("moving to x=" + finalPos.getXAsInteger() + ", y="
						+ finalPos.getYAsInteger());
				
				if (finalPos.getXAsInteger() > currentX) {
					myself.setProperty("position", new Vector2Int(currentX + 1,
							currentY));
					currentX += 1;
				}

				if (finalPos.getXAsInteger() < currentX) {
					myself.setProperty("position", new Vector2Int(currentX - 1,
							currentY));
					currentX -= 1;
				}

				if (finalPos.getYAsInteger() > currentY) {
					myself.setProperty("position", new Vector2Int(currentX,
							currentY + 1));
					currentY += 1;
				}

				if (finalPos.getYAsInteger() < currentY) {
					myself.setProperty("position", new Vector2Int(currentX,
							currentY - 1));
					currentY -= 1;
				}

				
				 throw new PlanFailureException();
				
			}
		}
 
		@PlanPassed
		public void passed() {
			System.out.println("Plan finished successfully at " + getCurrentTime());
			
			System.out.println("changing plan");
			
			Random r = new Random();			
			finalPos = new Vector2Int(r.nextInt(spaceWidth), r.nextInt(spaceHeight));
			
		}
 
	}
 


	boolean checkNextToFire() {
		Vector2Int currentPos = (Vector2Int) myself.getProperty("position");
		int x = currentX;
		int y = currentY;

		if ((float) terrain[Util.getCoord(x + 1, y, spaceWidth, spaceHeight)]
				.getProperty("fire") > 50f
				|| (float) terrain[Util.getCoord(x - 1, y, spaceWidth,
						spaceHeight)].getProperty("fire") > 50f
				|| (float) terrain[Util.getCoord(x, y + 1, spaceWidth,
						spaceHeight)].getProperty("fire") > 50f
				|| (float) terrain[Util.getCoord(x, y - 1, spaceWidth,
						spaceHeight)].getProperty("fire") > 50f) {

			n = 0;

			return true;
		}

		return false;
	}

}
