/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.config.ConfigPath;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.io.ZipFileIO;
import org.columba.core.main.MainInterface;
import org.columba.core.util.GlobalResourceLoader;
import org.columba.mail.gui.util.URLController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author fdietz
 *
 * This dialog lets you view all installed plugins in a
 * categorized tree view.
 * 
 * There are buttons which let you:
 * - install new plugins
 * - remove plugins
 * - enable/disable plugins
 * - view plugin info (readme.txt in plugin folder)
 * 
 * TODO: remove plugins
 * 
 * TODO: enable/disable plugins doesn't work right know
 *       - have to find a good way of using renderer/editor (JCheckBox)
 *       - EabledRenderer/Editor aren't working correctly
 *         (and are disabled right now)
 * 
 */
public class PluginManagerDialog
	extends JDialog
	implements ActionListener, TreeSelectionListener {

	private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";

	JButton installButton;
	JButton removeButton;
	JButton optionsButton;
	JButton infoButton;

	JButton helpButton;
	JButton closeButton;

	PluginTree table;

	protected PluginNode selectedNode;

	public PluginManagerDialog() {
		// modal JDialog
		super(new JFrame(), true);

		initComponents();

		pack();
		setLocationRelativeTo(null);

		setVisible(true);
	}

	protected void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		getContentPane().add(mainPanel);

		installButton =
			new JButton(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"pluginmanager",
					"install"));
		installButton.setMnemonic(
			GlobalResourceLoader.getMnemonic(
				RESOURCE_PATH,
				"pluginmanager",
				"install"));
		installButton.setActionCommand("INSTALL");
		installButton.addActionListener(this);

		removeButton =
			new JButton(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"pluginmanager",
					"remove"));
		removeButton.setMnemonic(
			GlobalResourceLoader.getMnemonic(
				RESOURCE_PATH,
				"pluginmanager",
				"remove"));
		removeButton.setActionCommand("REMOVE");
		removeButton.setEnabled(false);
		removeButton.addActionListener(this);

		optionsButton =
			new JButton(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"pluginmanager",
					"options"));
		optionsButton.setMnemonic(
			GlobalResourceLoader.getMnemonic(
				RESOURCE_PATH,
				"pluginmanager",
				"options"));
		optionsButton.setActionCommand("OPTIONS");
		optionsButton.setEnabled(false);
		optionsButton.addActionListener(this);

		infoButton =
			new JButton(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"pluginmanager",
					"info"));
		infoButton.setMnemonic(
			GlobalResourceLoader.getMnemonic(
				RESOURCE_PATH,
				"pluginmanager",
				"info"));
		infoButton.setActionCommand("INFO");
		infoButton.setEnabled(false);
		infoButton.addActionListener(this);

		// top panel

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		//topPanel.setLayout( );

		JPanel topBorderPanel = new JPanel();
		topBorderPanel.setLayout(new BorderLayout());
		//topBorderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		topBorderPanel.add(topPanel);
		//mainPanel.add( topBorderPanel, BorderLayout.NORTH );

		JLabel nameLabel = new JLabel("name");
		nameLabel.setEnabled(false);
		topPanel.add(nameLabel);

		topPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		topPanel.add(Box.createHorizontalGlue());

		Component glue = Box.createVerticalGlue();
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		//c.fill = GridBagConstraints.HORIZONTAL;
		gridBagLayout.setConstraints(glue, c);

		gridBagLayout = new GridBagLayout();
		c = new GridBagConstraints();
		JPanel eastPanel = new JPanel(gridBagLayout);
		mainPanel.add(eastPanel, BorderLayout.EAST);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints(installButton, c);
		eastPanel.add(installButton);

		Component strut1 = Box.createRigidArea(new Dimension(30, 5));
		gridBagLayout.setConstraints(strut1, c);
		eastPanel.add(strut1);

		gridBagLayout.setConstraints(removeButton, c);
		eastPanel.add(removeButton);

		Component strut = Box.createRigidArea(new Dimension(30, 5));
		gridBagLayout.setConstraints(strut, c);
		eastPanel.add(strut);

		gridBagLayout.setConstraints(optionsButton, c);
		eastPanel.add(optionsButton);

		Component strut3 = Box.createRigidArea(new Dimension(30, 5));
		gridBagLayout.setConstraints(strut3, c);
		eastPanel.add(strut3);

		gridBagLayout.setConstraints(infoButton, c);
		eastPanel.add(infoButton);

		strut = Box.createRigidArea(new Dimension(30, 20));
		gridBagLayout.setConstraints(strut, c);
		eastPanel.add(strut);

		glue = Box.createVerticalGlue();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		gridBagLayout.setConstraints(glue, c);
		eastPanel.add(glue);

		// centerpanel

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		/*
		listView = new FilterListTable(filterList, this);
		listView.getSelectionModel().addListSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setPreferredSize(new Dimension(300, 250));
		scrollPane.getViewport().setBackground(Color.white);
		centerPanel.add(scrollPane);
		*/
		table = new PluginTree();
		table.getTree().addTreeSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(450, 300));
		scrollPane.getViewport().setBackground(Color.white);
		centerPanel.add(scrollPane);

		mainPanel.add(centerPanel);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 12, 11, 11));
		JButton closeButton =
			new JButton(MailResourceLoader.getString("global", "close"));
		closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		JButton helpButton =
			new JButton(MailResourceLoader.getString("global", "help"));
		helpButton.setActionCommand("HELP");
		helpButton.addActionListener(this);
		buttonPanel.add(helpButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(closeButton);
		getRootPane().registerKeyboardAction(
			this,
			"CLOSE",
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(
			this,
			"HELP",
			KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);

	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CLOSE")) {

			setVisible(false);
		} else if (action.equals("HELP")) {
			URLController c = new URLController();
			try {
				c.open(new URL("help.html"));
			} catch (MalformedURLException mue) {
			}
		} else if (action.equals("INFO")) {
			String id = selectedNode.getId();

			URL url = MainInterface.pluginManager.getInfoURL(id);
			if (url == null) {
				NotifyDialog d = new NotifyDialog();
				d.showDialog("No readme file found.");
			} else
				new InfoViewerDialog(url);

		} else if (action.equals("OPTIONS")) {
		} else if (action.equals("REMOVE")) {
		} else if (action.equals("INSTALL")) {
			JFileChooser chooser = new JFileChooser();

			int result = chooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();

				installPlugin(file);
			}

		}

	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent arg0) {
		selectedNode = (PluginNode) arg0.getPath().getLastPathComponent();

		if (selectedNode == null)
			return;

		boolean isCategoryFolder = selectedNode.isCategory();
		if (isCategoryFolder) {
			// this is just a folder
			// ->disable all actions

			removeButton.setEnabled(false);
			optionsButton.setEnabled(false);
			infoButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
			optionsButton.setEnabled(true);
			infoButton.setEnabled(true);
		}
	}

	/**
	 * @return
	 */
	public PluginNode getSelectedNode() {
		return selectedNode;
	}

	protected void installPlugin(File file) {

		// use user's config folder in his/her home-folder
		File destination = new File(ConfigPath.configDirectory, "plugins");

		// extract plugin
		ZipFileIO.extract(file, destination);

		
		JOptionPane.showMessageDialog(
			this,
			"Plugin will be started on next startup of Columba");
		
		/*
		 * TODO: extract the plugin-folder name
		 * 
		 * if this works you don't have to restart Columba,
		 * the plugin will be loaded automatically 
		 * 
		 
		   File pluginFolder=null;
		   MainInterface.pluginManager.addPlugin(pluginFolder);
		*/
	}

}
