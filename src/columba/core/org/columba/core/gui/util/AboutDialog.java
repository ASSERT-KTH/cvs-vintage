//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.gui.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.columba.core.util.GlobalResourceLoader;
import org.columba.mail.gui.util.AddressLabel;
import org.columba.mail.gui.util.URLLabel;

public class AboutDialog implements ActionListener {

	public static final String CMD_CLOSE = "CLOSE";
	private static final String RESOURCE_BUNDLE_PATH = "org.columba.core.i18n.dialog";

	private JDialog dialog;
	private JTabbedPane tabbedPane;
	private boolean bool = false;

	public AboutDialog() {
		showDialog();
	}

	public void showDialog() {
		//LOCALIZE
		dialog =
			DialogStore.getDialog(
				GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "title")
					+ org.columba.core.main.MainInterface.version);

		dialog.setModal(false);
		tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		JPanel contentPane = (JPanel)dialog.getContentPane();
		contentPane.setLayout(new BorderLayout(0, 0));
		JPanel imagePanel = new JPanel();
		imagePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		imagePanel.add(new JLabel(ImageLoader.getImageIcon("startup.png")));
		contentPane.add(imagePanel, BorderLayout.NORTH);

		JPanel contactPanel = new JPanel(new GridBagLayout());
		contactPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 12, 0, 11),
				BorderFactory.createTitledBorder(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "contact"))));

		GridBagConstraints c = new GridBagConstraints();
		JLabel authorLabel = new JLabel(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "authors"));
		//Font font = MainInterface.columbaTheme.getControlTextFont();
		Font font = UIManager.getFont("Label.font");
		if (font != null) {
			font = font.deriveFont(Font.BOLD);
			authorLabel.setFont(font);
		}
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		contactPanel.add(authorLabel, c);

		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		Component box = Box.createRigidArea(new Dimension(10, 10));
		contactPanel.add(box, c);

		AddressLabel a1 = new AddressLabel("Frederik Dietz <fdietz@users.sourceforge.net>");
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		contactPanel.add(a1, c);

		AddressLabel a2 = new AddressLabel("Timo Stich <tstich@users.sourceforge.net>");
		c.gridx = 2;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		contactPanel.add(a2, c);

		JLabel websiteLabel = new JLabel(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "website"));
		if (font != null)
			websiteLabel.setFont(font);
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		contactPanel.add(websiteLabel, c);

		URLLabel websiteUrl = null;
		try {
			websiteUrl = new URLLabel(new URL("http://columba.sourceforge.net"));
		}
		catch (MalformedURLException mue) {
		} //does not occur
		c.gridx = 2;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		contactPanel.add(websiteUrl, c);

		//		contentPane.add(contactPanel, BorderLayout.CENTER);
		tabbedPane.addTab("Authors", contactPanel);
		tabbedPane.addTab("Memory", new MemoryPanel());
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new BorderLayout(0, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		ButtonWithMnemonic closeButton = new ButtonWithMnemonic(GlobalResourceLoader.getString("global", "global", "close"));
		closeButton.setActionCommand(CMD_CLOSE);
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton, BorderLayout.EAST);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		dialog.getRootPane().setDefaultButton(closeButton);
		dialog.getRootPane().registerKeyboardAction(
			this,
			CMD_CLOSE,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (CMD_CLOSE.equals(e.getActionCommand())) {
			bool = true;
			dialog.dispose();
		}
	}

	public boolean success() {
		return bool;
	}

	private class MemoryPanel extends JPanel {
		private JPanel centerPanel;
		private JLabel currentMemoryKBLabel;
		private JLabel currentMemoryLabel;
		private JFormattedTextField currentMemoryTextField;
		private JPanel fillerPanel;
		private JButton gcButton;
		private JLabel maxMemoryKBLabel;
		private JLabel maxMemoryLabel;
		private JFormattedTextField maxMemoryTextField;
		private JProgressBar progressBar;
		private JPanel southPanel;
		private JLabel totalMemoryKBLabel;
		private JLabel totalMemoryLabel;
		private JFormattedTextField totalMemoryTextField;
		private BoundedRangeModel model;
		private MemoryMonitorThread memoryMonitorThread;

		public MemoryPanel() {
			initPanel();
			initComponents();

			memoryMonitorThread = new MemoryMonitorThread();
			memoryMonitorThread.start();
		}

		public int getCurrentMemory() {
			return Integer.parseInt(currentMemoryTextField.getText());
		}

		public void setCurrentMemory(int mem) {
			currentMemoryTextField.setValue(new Integer(mem));
			progressBar.setValue(mem);
		}

		public int getTotalMemory() {
			return Integer.parseInt(totalMemoryTextField.getText());
		}

		public void setTotalMemory(int mem) {
			totalMemoryTextField.setValue(new Integer(mem));
			progressBar.setMaximum(mem);
		}

		private void initPanel() {
			setLayout(new GridBagLayout());
		}

		private void initComponents() {
			GridBagConstraints gridBagConstraints;

			southPanel = new JPanel();
			progressBar = new JProgressBar();
			gcButton = new JButton(ImageLoader.getImageIcon("stock_delete-16.png"));
			centerPanel = new JPanel();
			currentMemoryLabel = new JLabel();
			currentMemoryTextField = new JFormattedTextField(NumberFormat.getInstance());
			currentMemoryKBLabel = new JLabel();
			totalMemoryLabel = new JLabel();
			totalMemoryTextField = new JFormattedTextField(NumberFormat.getInstance());
			totalMemoryKBLabel = new JLabel();
			maxMemoryLabel = new JLabel();
			maxMemoryTextField = new JFormattedTextField();
			maxMemoryKBLabel = new JLabel();
			fillerPanel = new JPanel();

			setLayout(new java.awt.BorderLayout());

			setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
			southPanel.setLayout(new java.awt.GridBagLayout());

			progressBar.setPreferredSize(gcButton.getPreferredSize());
			progressBar.setStringPainted(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.weightx = 1.0;
			southPanel.add(progressBar, gridBagConstraints);

			gcButton.setContentAreaFilled(false);
			gcButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					System.gc();
				}
			});
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 6, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			southPanel.add(gcButton, gridBagConstraints);

			add(southPanel, java.awt.BorderLayout.SOUTH);

			centerPanel.setLayout(new java.awt.GridBagLayout());

			currentMemoryLabel.setText("Used:");
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(currentMemoryLabel, gridBagConstraints);

			currentMemoryTextField.setColumns(5);
			currentMemoryTextField.setEditable(false);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 4, 0, 4);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(currentMemoryTextField, gridBagConstraints);

			currentMemoryKBLabel.setText("KB");
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(currentMemoryKBLabel, gridBagConstraints);

			totalMemoryLabel.setText("Total:");
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 3;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(totalMemoryLabel, gridBagConstraints);

			totalMemoryTextField.setColumns(5);
			totalMemoryTextField.setEditable(false);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 4;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.insets = new Insets(6, 4, 0, 4);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(totalMemoryTextField, gridBagConstraints);

			totalMemoryKBLabel.setText("KB");
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 5;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(totalMemoryKBLabel, gridBagConstraints);

			maxMemoryLabel.setText("VM Max:");
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.insets = new Insets(6, 0, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(maxMemoryLabel, gridBagConstraints);

			maxMemoryTextField.setColumns(5);
			maxMemoryTextField.setEditable(false);
			maxMemoryTextField.setValue(new Integer((int) (Runtime.getRuntime().maxMemory() / 1000)));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.insets = new Insets(4, 4, 0, 4);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(maxMemoryTextField, gridBagConstraints);

			maxMemoryKBLabel.setText("KB");
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 2;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			centerPanel.add(maxMemoryKBLabel, gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 2;
			gridBagConstraints.gridwidth = 6;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			centerPanel.add(fillerPanel, gridBagConstraints);

			add(centerPanel, java.awt.BorderLayout.CENTER);
		}

		private class MemoryMonitorThread extends Thread {
			private boolean isRunning = true;

			public MemoryMonitorThread() {
				setPriority(Thread.MIN_PRIORITY);
			}

			public boolean isRunning() {
				return isRunning;
			}

			public void setRunning(boolean b) {
				isRunning = b;
			}

			public void run() {
				while (isRunning) {
					Runtime runtime = Runtime.getRuntime();

					int totalMem = (int) (runtime.totalMemory() / 1000);
					int currMem = (int) (totalMem - (runtime.freeMemory() / 1000));
					setTotalMemory(totalMem);
					setCurrentMemory(currMem);

					try {
						Thread.sleep(750);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}
}