/*
 * AP(r) Computer Science GridWorld Case Study:
 * Copyright(c) 2002-2006 College Entrance Examination Board
 * (http://www.collegeboard.com).
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * @author Julie Zelenski
 * @author Cay Horstmann
 */

package info.gridworld.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import info.gridworld.world.World;

/**
 * The GUIController controls the behavior in a WorldFrame. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI
 * implementation details that are not intended to be understood by AP CS
 * students.
 */

public class GUIController<T> {
	public static final int INDEFINITE = 0, FIXED_STEPS = 1, PROMPT_STEPS = 2;

	private static final int MIN_DELAY_MSECS = 10, MAX_DELAY_MSECS = 1000;
	private static final int INITIAL_DELAY = MIN_DELAY_MSECS + (MAX_DELAY_MSECS - MIN_DELAY_MSECS) / 2;

	private Timer timer;
	private JButton stepButton, runButton, stopButton;
	private JComponent controlPanel;
	private GridPanel display;
	private WorldFrame<T> parentFrame;
	private int numStepsToRun, numStepsSoFar;
	private ResourceBundle resources;
	private DisplayMap displayMap;
	private boolean running;
	private Set<Class<?>> occupantClasses;

	/**
	 * Creates a new controller tied to the specified display and gui frame.
	 *
	 * @param parent     the frame for the world window
	 * @param disp       the panel that displays the grid
	 * @param displayMap the map for occupant displays
	 * @param res        the resource bundle for message display
	 */
	public GUIController(WorldFrame<T> parent, GridPanel disp, DisplayMap displayMap, ResourceBundle res) {
		resources = res;
		display = disp;
		parentFrame = parent;
		this.displayMap = displayMap;
		makeControls();

		occupantClasses = new TreeSet<>((a, b) -> a.getName().compareTo(b.getName()));

		final World<T> world = parentFrame.getWorld();
		final Grid<T> gr = world.getGrid();
		for (final Location loc : gr.getOccupiedLocations()) {
			addOccupant(gr.get(loc));
		}
		for (final String name : world.getOccupantClasses()) {
			try {
				occupantClasses.add(Class.forName(name));
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		timer = new Timer(INITIAL_DELAY, evt -> step());

		display.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				final Grid<T> gr = parentFrame.getWorld().getGrid();
				final Location loc = display.locationForPoint(evt.getPoint());
				if (loc != null && gr.isValid(loc) && !isRunning()) {
					display.setCurrentLocation(loc);
					locationClicked();
				}
			}
		});
		stop();
	}

	/**
	 * Advances the world one step.
	 */
	public void step() {
		parentFrame.getWorld().step();
		parentFrame.repaint();
		if (++numStepsSoFar == numStepsToRun) {
			stop();
		}
		final Grid<T> gr = parentFrame.getWorld().getGrid();

		for (final Location loc : gr.getOccupiedLocations()) {
			addOccupant(gr.get(loc));
		}
	}

	private void addOccupant(T occupant) {
		Class<?> cl = occupant.getClass();
		do {
			if ((cl.getModifiers() & Modifier.ABSTRACT) == 0) {
				occupantClasses.add(cl);
			}
			cl = cl.getSuperclass();
		} while (cl != Object.class);
	}

	/**
	 * Starts a timer to repeatedly carry out steps at the speed currently indicated
	 * by the speed slider up Depending on the run option, it will either carry out
	 * steps for some fixed number or indefinitely until stopped.
	 */
	public void run() {
		display.setToolTipsEnabled(false); // hide tool tips while running
		parentFrame.setRunMenuItemsEnabled(false);
		stopButton.setEnabled(true);
		stepButton.setEnabled(false);
		runButton.setEnabled(false);
		numStepsSoFar = 0;
		timer.start();
		running = true;
	}

	/**
	 * Stops any existing timer currently carrying out steps.
	 */
	public void stop() {
		display.setToolTipsEnabled(true);
		parentFrame.setRunMenuItemsEnabled(true);
		timer.stop();
		stopButton.setEnabled(false);
		runButton.setEnabled(true);
		stepButton.setEnabled(true);
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * Builds the panel with the various controls (buttons and slider).
	 */
	private void makeControls() {
		controlPanel = new JPanel();
		stepButton = new JButton(resources.getString("button.gui.step"));
		runButton = new JButton(resources.getString("button.gui.run"));
		stopButton = new JButton(resources.getString("button.gui.stop"));

		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		controlPanel.setBorder(BorderFactory.createEtchedBorder());

		final Dimension spacer = new Dimension(5, stepButton.getPreferredSize().height + 10);

		controlPanel.add(Box.createRigidArea(spacer));

		controlPanel.add(stepButton);
		controlPanel.add(Box.createRigidArea(spacer));
		controlPanel.add(runButton);
		controlPanel.add(Box.createRigidArea(spacer));
		controlPanel.add(stopButton);
		runButton.setEnabled(false);
		stepButton.setEnabled(false);
		stopButton.setEnabled(false);

		controlPanel.add(Box.createRigidArea(spacer));
		controlPanel.add(new JLabel(resources.getString("slider.gui.slow")));
		final JSlider speedSlider = new JSlider(MIN_DELAY_MSECS, MAX_DELAY_MSECS, INITIAL_DELAY);
		speedSlider.setInverted(true);
		speedSlider.setPreferredSize(new Dimension(100, speedSlider.getPreferredSize().height));
		speedSlider.setMaximumSize(speedSlider.getPreferredSize());

		// remove control PAGE_UP, PAGE_DOWN from slider--they should be used
		// for zoom
		InputMap map = speedSlider.getInputMap();
		while (map != null) {
			map.remove(KeyStroke.getKeyStroke("control PAGE_UP"));
			map.remove(KeyStroke.getKeyStroke("control PAGE_DOWN"));
			map = map.getParent();
		}

		controlPanel.add(speedSlider);
		controlPanel.add(new JLabel(resources.getString("slider.gui.fast")));
		controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));

		stepButton.addActionListener(e -> step());
		runButton.addActionListener(e -> run());
		stopButton.addActionListener(e -> stop());
		speedSlider.addChangeListener(evt -> timer.setDelay(((JSlider) evt.getSource()).getValue()));
	}

	/**
	 * Returns the panel containing the controls.
	 *
	 * @return the control panel
	 */
	public JComponent controlPanel() {
		return controlPanel;
	}

	/**
	 * Callback on mousePressed when editing a grid.
	 */
	private void locationClicked() {
		final World<T> world = parentFrame.getWorld();
		final Location loc = display.getCurrentLocation();
		if (loc != null && !world.locationClicked(loc)) {
			editLocation();
		}
		parentFrame.repaint();
	}

	/**
	 * Edits the contents of the current location, by displaying the constructor or
	 * method menu.
	 */
	public void editLocation() {
		final World<T> world = parentFrame.getWorld();

		final Location loc = display.getCurrentLocation();
		if (loc != null) {
			final T occupant = world.getGrid().get(loc);
			if (occupant == null) {
				final MenuMaker<T> maker = new MenuMaker<>(parentFrame, resources, displayMap);
				final JPopupMenu popup = maker.makeConstructorMenu(occupantClasses, loc);
				final Point p = display.pointForLocation(loc);
				popup.show(display, p.x, p.y);
			} else {
				final MenuMaker<T> maker = new MenuMaker<>(parentFrame, resources, displayMap);
				final JPopupMenu popup = maker.makeMethodMenu(occupant, loc);
				final Point p = display.pointForLocation(loc);
				popup.show(display, p.x, p.y);
			}
		}
		parentFrame.repaint();
	}

	/**
	 * Edits the contents of the current location, by displaying the constructor or
	 * method menu.
	 */
	public void deleteLocation() {
		final World<T> world = parentFrame.getWorld();
		final Location loc = display.getCurrentLocation();
		if (loc != null) {
			world.remove(loc);
			parentFrame.repaint();
		}
	}
}
