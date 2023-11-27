package loopWorld;

import java.util.concurrent.Semaphore;

import info.gridworld.grid.Grid;
import info.gridworld.world.World;

public abstract class StepWorld<T> extends World<T> {
	private Semaphore s = new Semaphore(0);
	
	public StepWorld() {
		init();
	}
	
	public StepWorld(Grid<T> grid) {
		super(grid);
		init();
	}
	
	public abstract void run();
	
	public void pause(String message) {
		setMessage(message);
		pause();
	}
	
	public void pause() {
		try {
			s.acquire();
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public void step() {
		s.release();
		try {
			Thread.sleep(100);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void init() {
		setMessage("Click the Step button.");
		Thread t = new Thread(() -> {
			try {
				s.acquire();
				StepWorld.this.run();
				setMessage("Done.");
			} catch(InterruptedException e) {
				
			}
		});
		t.start();
	}
}
