package loopWorld;

import info.gridworld.actor.Rock;
import info.gridworld.grid.Location;

public class LoopWorld extends StepWorld<Rock>{

	@Override
	public void run() {
		int n = 5;
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= 2*i-1; j++) {
				add(new Location(i, n-i+j), new Rock());
				pause("i=" + i + ",j="+j);
			}
		}
	}

}
