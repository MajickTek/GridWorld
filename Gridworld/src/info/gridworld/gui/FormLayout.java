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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * A layout manager that lays out components along a central axis <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI
 * implementation details that are not intended to be understood by AP CS
 * students.
 */
public class FormLayout implements LayoutManager {
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		final Component[] components = parent.getComponents();
		left = 0;
		right = 0;
		height = 0;
		for (int i = 0; i < components.length; i += 2) {
			final Component cleft = components[i];
			final Component cright = components[i + 1];

			final Dimension dleft = cleft.getPreferredSize();
			final Dimension dright = cright.getPreferredSize();
			left = Math.max(left, dleft.width);
			right = Math.max(right, dright.width);
			height = height + Math.max(dleft.height, dright.height);
		}
		return new Dimension(left + GAP + right, height);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		preferredLayoutSize(parent); // sets left, right

		final Component[] components = parent.getComponents();

		final Insets insets = parent.getInsets();
		final int xcenter = insets.left + left;
		int y = insets.top;

		for (int i = 0; i < components.length; i += 2) {
			final Component cleft = components[i];
			final Component cright = components[i + 1];

			final Dimension dleft = cleft.getPreferredSize();
			final Dimension dright = cright.getPreferredSize();

			final int height = Math.max(dleft.height, dright.height);

			cleft.setBounds(xcenter - dleft.width, y + (height - dleft.height) / 2, dleft.width, dleft.height);

			cright.setBounds(xcenter + GAP, y + (height - dright.height) / 2, dright.width, dright.height);
			y += height;
		}
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	private int left;
	private int right;
	private int height;
	private static final int GAP = 6;
}
