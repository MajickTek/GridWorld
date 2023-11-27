/*
 * AP(r) Computer Science GridWorld Case Study:
 * Copyright(c) 2005-2006 Cay S. Horstmann (http://horstmann.com)
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
 * @author Cay Horstmann
 */

package info.gridworld.gui;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import info.gridworld.grid.Location;

/**
 * A property editor for the Location type. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI
 * implementation details that are not intended to be understood by AP CS
 * students.
 */
public class LocationEditor extends PropertyEditorSupport {
	private final JFormattedTextField rowField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField colField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JPanel panel = new JPanel();

	public LocationEditor() {
		rowField.setColumns(5);
		colField.setColumns(5);

		panel.add(rowField);
		panel.add(colField);
	}

	@Override
	public Object getValue() {
		final int row = ((Number) rowField.getValue()).intValue();
		final int col = ((Number) colField.getValue()).intValue();
		return new Location(row, col);
	}

	@Override
	public void setValue(Object newValue) {
		final Location loc = (Location) newValue;
		rowField.setValue(Integer.valueOf(loc.getRow()));
		colField.setValue(Integer.valueOf(loc.getCol()));
	}

	@Override
	public boolean supportsCustomEditor() {
		return true;
	}

	@Override
	public Component getCustomEditor() {
		return panel;
	}
}
