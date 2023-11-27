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
 * @author Chris Nevison
 * @author Cay Horstmann
 */

package info.gridworld.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import info.gridworld.world.World;

/**
 * The WorldFrame displays a World and allows manipulation of its occupants.
 * <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI
 * implementation details that are not intended to be understood by AP CS
 * students.
 */
public class WorldFrame<T> extends JFrame {
	private static final long serialVersionUID = 1L;
	private GUIController<T> control;
	private GridPanel display;
	private JTextArea messageArea;
	private ArrayList<JMenuItem> menuItemsDisabledDuringRun;
	private World<T> world;
	private ResourceBundle resources;
	private DisplayMap displayMap;

	private Set<Class<?>> gridClasses;
	private JMenu newGridMenu;

	private static int count = 0;

	/**
	 * Constructs a WorldFrame that displays the occupants of a world
	 *
	 * @param world the world to display
	 */
	public WorldFrame(World<T> world) {
		this.world = world;
		count++;
		resources = ResourceBundle.getBundle(getClass().getName() + "Resources");
		try {
			System.setProperty("sun.awt.exception.handler", GUIExceptionHandler.class.getName());
		} catch (final SecurityException ex) {
			// will fail in an applet
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				count--;
				if (count == 0) {
					System.exit(0);
				}
			}
		});

		displayMap = new DisplayMap();
		String title = null;
		try // won't work in applets
		{
			System.getProperty("info.gridworld.gui.frametitle");
		} catch (final SecurityException ex) {
		}
		if (title == null) {
			title = resources.getString("frame.title");
		}
		setTitle(title);
		setLocation(25, 15);

		final URL appIconUrl = getClass().getResource("GridWorld.gif");
		if (appIconUrl != null) {
			final ImageIcon appIcon = new ImageIcon(appIconUrl);
			setIconImage(appIcon.getImage());
		}

		makeMenus();

		final JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		content.setLayout(new BorderLayout());
		setContentPane(content);

		display = new GridPanel(displayMap, resources);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
			if (getFocusOwner() == null) {
				return false;
			}
			String text = KeyStroke.getKeyStrokeForEvent(event).toString();
			final String PRESSED = "pressed ";
			final int n = text.indexOf(PRESSED);
			// filter out modifier keys; they are neither characters or actions
			if ((n < 0) || (event.getKeyChar() == KeyEvent.CHAR_UNDEFINED && !event.isActionKey())) {
				return false;
			}
			text = text.substring(0, n) + text.substring(n + PRESSED.length());
			final boolean consumed = getWorld().keyPressed(text, display.getCurrentLocation());
			if (consumed) {
				repaint();
			}
			return consumed;
		});

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewport(new PseudoInfiniteViewport(scrollPane));
		scrollPane.setViewportView(display);
		content.add(scrollPane, BorderLayout.CENTER);

		gridClasses = new TreeSet<>((a, b) -> a.getName().compareTo(b.getName()));
		for (final String name : world.getGridClasses()) {
			try {
				gridClasses.add(Class.forName(name));
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		final Grid<T> gr = world.getGrid();
		gridClasses.add(gr.getClass());

		makeNewGridMenu();

		control = new GUIController<>(this, display, displayMap, resources);
		content.add(control.controlPanel(), BorderLayout.SOUTH);

		messageArea = new JTextArea(2, 35);
		messageArea.setEditable(false);
		messageArea.setFocusable(false);
		messageArea.setBackground(new Color(0xFAFAD2));
		content.add(new JScrollPane(messageArea), BorderLayout.NORTH);

		pack();
		repaint(); // to show message
		display.setGrid(gr);
	}

	@Override
	public void repaint() {
		String message = getWorld().getMessage();
		if (message == null) {
			message = resources.getString("message.default");
		}
		messageArea.setText(message);
		messageArea.repaint();
		display.repaint(); // for applet
		super.repaint();
	}

	/**
	 * Gets the world that this frame displays
	 *
	 * @return the world
	 */
	public World<T> getWorld() {
		return world;
	}

	/**
	 * Sets a new grid for this world. Occupants are transferred from the old world
	 * to the new.
	 *
	 * @param newGrid the new grid
	 */
	public void setGrid(Grid<T> newGrid) {
		final Grid<T> oldGrid = world.getGrid();
		final Map<Location, T> occupants = new HashMap<>();
		for (final Location loc : oldGrid.getOccupiedLocations()) {
			occupants.put(loc, world.remove(loc));
		}

		world.setGrid(newGrid);
		for (final Location loc : occupants.keySet()) {
			if (newGrid.isValid(loc)) {
				world.add(loc, occupants.get(loc));
			}
		}

		display.setGrid(newGrid);
		repaint();
	}

	/**
	 * Displays an error message
	 *
	 * @param t        the throwable that describes the error
	 * @param resource the resource whose .text/.title strings should be used in the
	 *                 dialog
	 */
	public void showError(Throwable t, String resource) {
		String text;
		try {
			text = resources.getString(resource + ".text");
		} catch (final MissingResourceException e) {
			text = resources.getString("error.text");
		}

		String title;
		try {
			title = resources.getString(resource + ".title");
		} catch (final MissingResourceException e) {
			title = resources.getString("error.title");
		}

		final String reason = resources.getString("error.reason");
		final String message = text + "\n" + MessageFormat.format(reason, new Object[] { t });

		JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
	}

	// Creates the drop-down menus on the frame.

	private JMenu makeMenu(String resource) {
		final JMenu menu = new JMenu();
		configureAbstractButton(menu, resource);
		return menu;
	}

	private JMenuItem makeMenuItem(String resource, ActionListener listener) {
		final JMenuItem item = new JMenuItem();
		configureMenuItem(item, resource, listener);
		return item;
	}

	private void configureMenuItem(JMenuItem item, String resource, ActionListener listener) {
		configureAbstractButton(item, resource);
		item.addActionListener(listener);
		try {
			final String accel = resources.getString(resource + ".accel");
			final String metaPrefix = "@";
			if (accel.startsWith(metaPrefix)) {
				// int menuMask = getToolkit().getMenuShortcutKeyMask();
				final int menuMask = InputEvent.CTRL_DOWN_MASK;
				final KeyStroke key = KeyStroke.getKeyStroke(
						KeyStroke.getKeyStroke(accel.substring(metaPrefix.length())).getKeyCode(), menuMask);
				item.setAccelerator(key);
			} else {
				item.setAccelerator(KeyStroke.getKeyStroke(accel));
			}
		} catch (final MissingResourceException ex) {
			// no accelerator
		}
	}

	private void configureAbstractButton(AbstractButton button, String resource) {
		String title = resources.getString(resource);
		final int i = title.indexOf('&');
		int mnemonic = 0;
		if (i >= 0) {
			mnemonic = title.charAt(i + 1);
			title = title.substring(0, i) + title.substring(i + 1);
			button.setText(title);
			button.setMnemonic(Character.toUpperCase(mnemonic));
			button.setDisplayedMnemonicIndex(i);
		} else {
			button.setText(title);
		}
	}

	private void makeMenus() {
		final JMenuBar mbar = new JMenuBar();
		JMenu menu;

		menuItemsDisabledDuringRun = new ArrayList<>();

		mbar.add(menu = makeMenu("menu.file"));

		newGridMenu = makeMenu("menu.file.new");
		menu.add(newGridMenu);
		menuItemsDisabledDuringRun.add(newGridMenu);

		menu.add(makeMenuItem("menu.file.quit", e -> System.exit(0)));

		mbar.add(menu = makeMenu("menu.view"));

		menu.add(makeMenuItem("menu.view.up", e -> display.moveLocation(-1, 0)));
		menu.add(makeMenuItem("menu.view.down", e -> display.moveLocation(1, 0)));
		menu.add(makeMenuItem("menu.view.left", e -> display.moveLocation(0, -1)));
		menu.add(makeMenuItem("menu.view.right", e -> display.moveLocation(0, 1)));

		JMenuItem viewEditMenu;
		menu.add(viewEditMenu = makeMenuItem("menu.view.edit", e -> control.editLocation()));
		menuItemsDisabledDuringRun.add(viewEditMenu);

		JMenuItem viewDeleteMenu;
		menu.add(viewDeleteMenu = makeMenuItem("menu.view.delete", e -> control.deleteLocation()));
		menuItemsDisabledDuringRun.add(viewDeleteMenu);

		menu.add(makeMenuItem("menu.view.zoomin", e -> display.zoomIn()));

		menu.add(makeMenuItem("menu.view.zoomout", e -> display.zoomOut()));

		mbar.add(menu = makeMenu("menu.help"));
		menu.add(makeMenuItem("menu.help.about", e -> showAboutPanel()));
		menu.add(makeMenuItem("menu.help.help", e -> showHelp()));
		menu.add(makeMenuItem("menu.help.license", e -> showLicense()));

		setRunMenuItemsEnabled(true);
		setJMenuBar(mbar);
	}

	private void makeNewGridMenu() {
		newGridMenu.removeAll();
		final MenuMaker<T> maker = new MenuMaker<>(this, resources, displayMap);
		maker.addConstructors(newGridMenu, gridClasses);
	}

	/**
	 * Sets the enabled status of those menu items that are disabled when running.
	 *
	 * @param enable true to enable the menus
	 */
	public void setRunMenuItemsEnabled(boolean enable) {
		for (final JMenuItem item : menuItemsDisabledDuringRun) {
			item.setEnabled(enable);
		}
	}

	/**
	 * Brings up a simple dialog with some general information.
	 */
	private void showAboutPanel() {
		String html = MessageFormat.format(resources.getString("dialog.about.text"),
				new Object[] { resources.getString("version.id") });
		final String[] props = { "java.version", "java.vendor", "java.home", "os.name", "os.arch", "os.version",
				"user.name", "user.home", "user.dir" };
		html += "<table border='1'>";
		for (final String prop : props) {
			try {
				final String value = System.getProperty(prop);
				html += "<tr><td>" + prop + "</td><td>" + value + "</td></tr>";
			} catch (final SecurityException ex) {
				// oh well...
			}
		}
		html += "</table>";
		html = "<html>" + html + "</html>";
		JOptionPane.showMessageDialog(this, new JLabel(html), resources.getString("dialog.about.title"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Brings up a window with a scrolling text pane that display the help
	 * information.
	 */
	private void showHelp() {
		final JDialog dialog = new JDialog(this, resources.getString("dialog.help.title"));
		final JEditorPane helpText = new JEditorPane();
		try {
			final URL url = getClass().getResource("GridWorldHelp.html");

			helpText.setPage(url);
		} catch (final Exception e) {
			helpText.setText(resources.getString("dialog.help.error"));
		}
		helpText.setEditable(false);
		helpText.addHyperlinkListener(ev -> {
			if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					helpText.setPage(ev.getURL());
				} catch (final Exception ex) {
				}
			}
		});
		final JScrollPane sp = new JScrollPane(helpText);
		sp.setPreferredSize(new Dimension(650, 500));
		dialog.getContentPane().add(sp);
		dialog.setLocation(getX() + getWidth() - 200, getY() + 50);
		dialog.pack();
		dialog.setVisible(true);
	}

	/**
	 * Brings up a dialog that displays the license.
	 */
	private void showLicense() {
		final JDialog dialog = new JDialog(this, resources.getString("dialog.license.title"));
		final JEditorPane text = new JEditorPane();
		try {
			final URL url = getClass().getResource("GNULicense.txt");

			text.setPage(url);
		} catch (final Exception e) {
			text.setText(resources.getString("dialog.license.error"));
		}
		text.setEditable(false);
		final JScrollPane sp = new JScrollPane(text);
		sp.setPreferredSize(new Dimension(650, 500));
		dialog.getContentPane().add(sp);
		dialog.setLocation(getX() + getWidth() - 200, getY() + 50);
		dialog.pack();
		dialog.setVisible(true);
	}

	/**
	 * Nested class that is registered as the handler for exceptions on the Swing
	 * event thread. The handler will put up an alert panel and dump the stack trace
	 * to the console.
	 */

	public class GUIExceptionHandler {
		public void handle(Throwable e) {
			e.printStackTrace();

			final JTextArea area = new JTextArea(10, 40);
			final StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			area.setText(writer.toString());
			area.setCaretPosition(0);
			final String copyOption = resources.getString("dialog.error.copy");
			final JOptionPane pane = new JOptionPane(new JScrollPane(area), JOptionPane.ERROR_MESSAGE,
					JOptionPane.YES_NO_OPTION, null, new String[] { copyOption, resources.getString("cancel") });
			pane.createDialog(WorldFrame.this, e.toString()).setVisible(true);
			if (copyOption.equals(pane.getValue())) {
				area.setSelectionStart(0);
				area.setSelectionEnd(area.getText().length());
				area.copy(); // copy to clipboard
			}
		}
	}
}
