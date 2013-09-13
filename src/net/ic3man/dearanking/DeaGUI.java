package net.ic3man.dearanking;

import java.io.File;
import java.text.DecimalFormat;


import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class DeaGUI {

	List list;
	Browser browser;

	Display display;
	Shell shell;

	Menu menuBar, fileMenu, helpMenu;

	MenuItem fileMenuHeader, helpMenuHeader;

	MenuItem fileExitItem, fileOpenItem, helpAboutItem;

	Label optimalLabel;

	public DeaGUI() {
		File f = new File("map.html");
		if (!f.exists()) {
			System.out.println("file not exist! " + f.getAbsolutePath());
			return;
		}
		display = new Display();
		shell = new Shell(display);
		shell.setText("Protected Areas | DEA");
		shell.setLayout(new FillLayout());

		Image imgicon = new Image(display, "icon.png");
		shell.setImage(imgicon);

		menuBar = new Menu(shell, SWT.BAR);
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");

		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open");
		fileOpenItem.addSelectionListener(new fileOpenItemListener());

		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("&Exit");
		fileExitItem.addSelectionListener(new fileExitItemListener());

		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("&Help");

		helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);

		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText("&About");

		SashForm sash = new SashForm(shell, SWT.HORIZONTAL);

		try {
			browser = new Browser(sash, SWT.NONE);
			browser.addControlListener(new ControlListener() {

				@Override
				public void controlResized(ControlEvent e) {
					browser.execute("document.getElementById('map_canvas').style.width= "
							+ (browser.getSize().x - 20) + ";");
					browser.execute("document.getElementById('map_canvas').style.height= "
							+ (browser.getSize().y - 20) + ";");
				}

				@Override
				public void controlMoved(ControlEvent e) {
				}
			});
		} catch (SWTError e) {
			System.out.println("Could not instantiate Browser: "
					+ e.getMessage());
			display.dispose();
			return;
		}

		Composite c = new Composite(sash, SWT.BORDER);
		c.setLayout(new GridLayout(1, true));

		list = new List(c, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		optimalLabel = new Label(c, SWT.BORDER);
		optimalLabel.setText("Optimal Combination\nC1:\nC2:");

		browser.setUrl(f.toURI().toString());
		sash.setWeights(new int[] { 4, 1 });

		shell.setMenuBar(menuBar);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public void addMarker(DMU d) {
		browser.evaluate("addMarker('" + d.getRank() + "', '" + d.getCode()
				+ "', '" + d.getName() + "', '" + d.getArea() + "', '"
				+ d.getC1() + "', '" + d.getC2() + "', '" + d.getTE() + "', "
				+ d.getLat() + "," + d.getLon() + ");");
	}

	class fileOpenItemListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent event) {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Open File");
			String[] filterExt = { "*.xlsx", "*.xls", "*.*" };
			fd.setFilterExtensions(filterExt);
			String selected = fd.open();
			DeaRanking.readXLSX(selected);
			// Generate Points map
			addPoints();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	class fileExitItemListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent event) {
			shell.close();
			display.dispose();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			shell.close();
			display.dispose();
		}
	}

	public void addPoints() {
		setOptimal();
		int i = 1;
		for (DMU d : DeaRanking.DMUs) {
			d.setRank(i);
			addMarker(d);
			list.add(i + " : " + d.getName());
			i++;
		}
	}

	public void readWeights() {
		InputDialog dlg = new InputDialog(shell, "",
				"Enter Variable1 weight (0.-1.)", "", new DoubleValidator());
		if (dlg.open() == Window.OK) {
			DeaRanking.weightX = Double.parseDouble(dlg.getValue());
			DeaRanking.weightY = 1 - Double.parseDouble(dlg.getValue());
		}
	}

	public void setOptimal() {
		DecimalFormat df = new DecimalFormat("#.###");
		optimalLabel.setText("Optimal Combination\nC1:"
				+ df.format(DeaRanking.optiC1) + "\nC2:"
				+ df.format(DeaRanking.optiC2));
	}

	class DoubleValidator implements IInputValidator {
		@Override
		public String isValid(String newText) {
			try {
				double d = Double.parseDouble(newText);
				return null;

			} catch (NumberFormatException nfe) {
				return "";
			}
		}
	}

}
