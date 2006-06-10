// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.gui.base.SingleSideEtchedBorder;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.frapuccino.swing.ActiveWindowTracker;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Dialog showing an error message and the exception's stack trace on request.
 * <p>
 * TODO (@author fdietz): I've currently replaced MultiLineLabel with JLabel,
 * because it totally destroys the layout. Somehow the MultiLineLabel doesn't
 * respect the JDialog size.
 * 
 * @author fdietz
 */

public class ErrorDialog extends JDialog implements ActionListener {
	private static final String URL_FORUM = "http://columba.sourceforge.net/phpBB2/viewforum.php?f=15";

	public static final String CMD_CLOSE = "CLOSE";

	public static final String CMD_REPORT_BUG = "REPORT_BUG";

	private static final String RESOURCE_BUNDLE_PATH = "org.columba.core.i18n.dialog";

	private boolean bool = false;

	private JLabel imageLabel;

	private JTextArea stacktraceTextArea;

	private ButtonWithMnemonic closeButton;

	private ButtonWithMnemonic reportBugButton;

	private MultiLineLabel messageMultiLineLabel;

	private JLabel label;

	private String message;

	private JToggleButton detailsButton;

	private Throwable ex;

	public static ErrorDialog createDialog(String message, Throwable ex) {
		Window w = ActiveWindowTracker.findActiveWindow();
		if (w instanceof Frame)
			return new ErrorDialog((Frame) w, message, ex);
		else if (w instanceof Dialog)
			return new ErrorDialog((Dialog) w, message, ex);
		else
			return new ErrorDialog(message, ex);
	}

	private ErrorDialog(String message, Throwable ex) {
		super();

		init(message, ex);
	}

	private ErrorDialog(Dialog frame, String message, Throwable ex) {
		super(frame, true);

		init(message, ex);
	}

	private ErrorDialog(Frame frame, String message, Throwable ex) {
		super(frame, true);

		init(message, ex);
	}

	private void init(String message, Throwable ex) {
		this.message = message;
		this.ex = ex;

		setTitle(GlobalResourceLoader.getString("org.columba.core.i18n.dialog",
				"error", "error_title"));

		initComponents();
		layoutComponents();
		pack();

		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel createCenterPanel(boolean showDetails) {
		FormLayout layout = new FormLayout(
				"default, 3dlu, fill:default:grow, 3dlu",
				"default, 8dlu, default 2dlu, fill:default:grow");

		JPanel centerPanel = new JPanel(layout);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		CellConstraints cc = new CellConstraints();
		centerPanel.add(imageLabel, cc.xy(1, 1));

		centerPanel.add(messageMultiLineLabel, cc.xywh(3, 1, 1, 1));
		// centerPanel.add(label, cc.xywh(3, 1, 1, 1));

		centerPanel.add(detailsButton, cc.xywh(1, 3, 1, 1));
		if (showDetails) {
			centerPanel.add(new JScrollPane(stacktraceTextArea), cc.xywh(1, 5,
					3, 1));

		}

		return centerPanel;
	}

	private void initComponents() {
		imageLabel = new JLabel(UIManager.getIcon("OptionPane.errorIcon"),
				SwingConstants.LEFT);

		messageMultiLineLabel = new MultiLineLabel(message);

		messageMultiLineLabel.setFont(messageMultiLineLabel.getFont()
				.deriveFont(Font.BOLD));
		label = new JLabel(message);
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		stacktraceTextArea = new JTextArea();

		if ( ex != null ) {
			StringWriter stringWriter = new StringWriter();
			ex.printStackTrace(new PrintWriter(stringWriter));
	
			stacktraceTextArea.append(stringWriter.toString());
			stacktraceTextArea.setEditable(false);
		}
		
		// TODO (@author fdietz): i18n
		detailsButton = new JToggleButton("Details >>");
		detailsButton.setSelected(false);
		detailsButton.setActionCommand("DETAILS");
		detailsButton.addActionListener(this);
		
		if ( ex == null ) {
			detailsButton.setEnabled(false);
		}
		
		closeButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				"global", "global", "close"));
		closeButton.setActionCommand(CMD_CLOSE);
		closeButton.addActionListener(this);

		reportBugButton = new ButtonWithMnemonic(GlobalResourceLoader
				.getString(RESOURCE_BUNDLE_PATH, "exception", "report_bug"));
		reportBugButton.setActionCommand(CMD_REPORT_BUG);
		reportBugButton.addActionListener(this);
	}

	private void layoutComponents() {
		if (getContentPane().getComponentCount() > 0) {
			getContentPane().removeAll();
		}

		getRootPane().setDefaultButton(closeButton);

		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		// buttonPanel.add(reportBugButton);
		buttonPanel.add(closeButton);

		bottomPanel.add(buttonPanel, BorderLayout.EAST);

		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		c.add(createCenterPanel(detailsButton.isSelected()),
				BorderLayout.CENTER);
		c.add(bottomPanel, BorderLayout.SOUTH);

		/*
		 * FormLayout layout = new FormLayout("default", "default, default");
		 * IContainer c = getContentPane(); c.setLayout(layout);
		 * 
		 * CellConstraints cc = new CellConstraints();
		 * c.add(createCenterPanel(detailsButton.isSelected()), cc.xy(1, 1));
		 * c.add(bottomPanel, cc.xy(1, 2));
		 */

	}

	public boolean success() {
		return bool;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (CMD_CLOSE.equals(command)) {
			bool = true;
			dispose();
		} else if (CMD_REPORT_BUG.equals(command)) {
			bool = false;

			try {
				ColumbaDesktop.getInstance().browse(new URL(URL_FORUM));
			} catch (MalformedURLException e1) {
			}

		} else if (command.equals("DETAILS")) {
			layoutComponents();
			pack();
			setLocationRelativeTo(null);
		}
	}
}