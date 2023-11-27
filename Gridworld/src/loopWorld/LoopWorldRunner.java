package loopWorld;

import info.gridworld.actor.Rock;
import info.gridworld.grid.UnboundedGrid;
import info.gridworld.world.World;

public class LoopWorldRunner {
	public static void main(String[] args) {
		World<Rock> world = new LoopWorld();
		world.setGrid(new UnboundedGrid<Rock>());
		world.show();
	}
}
