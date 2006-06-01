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

package org.columba.core.gui.config;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.columba.api.gui.frame.IContainer;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.IExtensionHandlerKeys;
import org.columba.api.plugin.PluginHandlerNotFoundException;
import org.columba.api.plugin.PluginLoadingFailedException;
import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.CheckBoxWithMnemonic;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.gui.base.SingleSideEtchedBorder;
import org.columba.core.gui.dialog.FontSelectionDialog;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.gui.plugin.ConfigurationDialog;
import org.columba.core.gui.themes.ThemeSwitcher;
import org.columba.core.gui.util.DialogHeaderPanel;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.help.HelpManager;
import org.columba.core.plugin.PluginManager;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.xml.XmlElement;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Shows a dialog for managing general options such as font settings.
 */

public class GeneralOptionsDialog extends JDialog implements ActionListener {
	private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";

	// button panel
	protected JButton okButton;

	protected JButton cancelButton;

	protected JButton helpButton;

	// look and feel
	protected JLabel lfLabel;

	protected JComboBox lfComboBox;

	protected JButton lfButton;

	private String theme = null;

	private IExtensionHandler handler;

	// fonts
	protected JCheckBox overwriteCheckBox;

	protected JLabel mainFontLabel;

	protected JLabel textFontLabel;

	protected JButton mainFontButton;

	protected JButton textFontButton;

	private Font mainFont;

	private Font textFont;

	// toolbar
	protected JLabel toolbarLabel;

	protected JComboBox toolbarComboBox;

	// language
	protected JLabel languageLabel;

	protected JComboBox languageComboBox;

	protected JFrame frame;

	protected IExtensionHandler configHandler;

	// HTTP proxy
	protected JLabel proxyLabel;

	protected JButton proxyButton;

	protected String proxyHost;

	protected int proxyPort;

	// ID of configuration plugin of this theme plugin
	protected String configID;

	private boolean restartRequired = false;

	private String oldLookAndFeelName;

	public GeneralOptionsDialog(JFrame frame) {
		super(frame, GlobalResourceLoader.getString(RESOURCE_PATH, "general",
				"dialog_title"), true);

		this.frame = frame;

		try {
			// get theme plugin-handler
			handler = PluginManager.getInstance().getExtensionHandler(
					IExtensionHandlerKeys.ORG_COLUMBA_CORE_THEME);
		} catch (PluginHandlerNotFoundException ex) {
			ex.printStackTrace();
		}

		try {
			// get config plugin-handler
			configHandler = PluginManager.getInstance().getExtensionHandler(
					IExtensionHandlerKeys.ORG_COLUMBA_CORE_CONFIG);
		} catch (PluginHandlerNotFoundException ex) {
			ex.printStackTrace();
		}

		initComponents();
		layoutComponents();
		updateComponents(true);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void updateComponents(boolean b) {

		GuiItem item = ((Config) Config.getInstance()).getOptionsConfig()
				.getGuiItem();

		if (b) {
			// look and feel
			theme = item.getString(GuiItem.THEME, GuiItem.NAME);
			oldLookAndFeelName = theme;

			lfComboBox.setSelectedItem(theme);

			// fonts
			String mainName = item.getStringWithDefault(GuiItem.FONT_MAIN,
					GuiItem.NAME, "default");
			int mainSize = item.getIntegerWithDefault(GuiItem.FONT_MAIN,
					GuiItem.SIZE_INT, 12);
			mainFont = new Font(mainName, Font.PLAIN, mainSize);

			String textName = item.getStringWithDefault(GuiItem.FONT_TEXT,
					GuiItem.NAME, "default");
			int textSize = item.getIntegerWithDefault(GuiItem.FONT_TEXT,
					GuiItem.SIZE_INT, 12);
			textFont = new Font(textName, Font.PLAIN, textSize);

			mainFontButton.setText(mainName);
			mainFontButton.setFont(mainFont);
			textFontButton.setText(textName);
			textFontButton.setFont(textFont);

			boolean overwrite = item.getBooleanWithDefault(GuiItem.FONT,
					GuiItem.OVERWRITE_BOOL, false);

			overwriteCheckBox.setSelected(overwrite);

			// enable/disable button, too
			textFontButton.setEnabled(overwrite);
			textFontLabel.setEnabled(overwrite);
			mainFontButton.setEnabled(overwrite);
			mainFontLabel.setEnabled(overwrite);

			// language
			Locale[] available = GlobalResourceLoader.getAvailableLocales();
			languageComboBox.setModel(new DefaultComboBoxModel(available));

			// select Locale in ComboBox
			for (int i = 0; i < available.length; i++) {
				if (available[i].equals(Locale.getDefault())) {
					languageComboBox.setSelectedIndex(i);
					break;
				}
			}

			boolean enableText = item.getBooleanWithDefault(GuiItem.TOOLBAR,
					GuiItem.ENABLE_TEXT_BOOL, true);

			boolean alignment = item.getBooleanWithDefault(GuiItem.TOOLBAR,
					GuiItem.TEXT_POSITION_BOOL, true);

			if (enableText) {
				toolbarComboBox.setSelectedIndex(alignment ? 1 : 2);
			}

			proxyHost = System.getProperty("http.proxyHost");
			String proxyPortString = System.getProperty("http.proxyPort", "-1");
			proxyPort = Integer.parseInt(proxyPortString);
			updateProxyButtonText();
		} else {
			// fonts
			item.setString(GuiItem.FONT_MAIN, GuiItem.NAME, getMainFont()
					.getName());
			item.setInteger(GuiItem.FONT_MAIN, GuiItem.SIZE_INT, getMainFont()
					.getSize());

			item.setString(GuiItem.FONT_TEXT, GuiItem.NAME, getTextFont()
					.getName());
			item.setInteger(GuiItem.FONT_TEXT, GuiItem.SIZE_INT, getTextFont()
					.getSize());

			item.setBoolean(GuiItem.FONT, GuiItem.OVERWRITE_BOOL,
					overwriteCheckBox.isSelected());

			// notify all listeners
			// @see org.columba.core.gui.util.FontProperties
			// @see org.columba.mail.gui.message.TextViewer
			// @see org.columba.mail.gui.composer.text.TextEditorController
			item.getElement("fonts").notifyObservers();

			// look and feel
			item.setString(GuiItem.THEME, GuiItem.NAME, (String) lfComboBox
					.getSelectedItem());

			// remember if Look And Feel has been changed
			if (!oldLookAndFeelName.equals((String) lfComboBox
					.getSelectedItem()))
				restartRequired = true;

			// get language configuration
			XmlElement locale = Config.getInstance().get("options").getElement(
					"/options/locale");

			// set language config based on selected item
			Locale l = (Locale) languageComboBox.getSelectedItem();
			locale.addAttribute("language", l.getLanguage());
			locale.addAttribute("country", l.getCountry());
			locale.addAttribute("variant", l.getVariant());

			int state = toolbarComboBox.getSelectedIndex();
			if (state == 0) {
				item.setBoolean(GuiItem.TOOLBAR, GuiItem.ENABLE_TEXT_BOOL,
						false);

			} else {
				item
						.setBoolean(GuiItem.TOOLBAR, GuiItem.ENABLE_TEXT_BOOL,
								true);

				item.setBoolean(GuiItem.TOOLBAR, GuiItem.TEXT_POSITION_BOOL,
						state == 1 ? true : false);

			}

			XmlElement options = Config.getInstance().get("options")
					.getElement("/options");

			XmlElement proxy = options.getElement("proxy");
			if (proxyHost != null && proxyPort > 0) {
				System.setProperty("http.proxyHost", proxyHost);
				System.setProperty("http.proxyPort", Integer
						.toString(proxyPort));
				if (proxy == null) {
					proxy = options.addSubElement("proxy");
				}
				proxy.addAttribute("host", proxyHost);
				proxy.addAttribute("port", Integer.toString(proxyPort));
			} else {
				Properties properties = System.getProperties();
				properties.remove("http.proxyHost");
				properties.remove("http.proxyPort");
				if (proxy != null) {
					options.removeElement(proxy);
				}
			}
		}
	}

	protected void layoutComponents() {
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		// Create a FormLayout instance.
		FormLayout layout = new FormLayout(
				"12dlu, pref, 3dlu, max(40dlu;pref), 3dlu, pref",

				// 3 columns
				"");

		// create a form builder
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		// create EmptyBorder between components and dialog-frame
		builder.setDefaultDialogBorder();

		// skip the first column
		builder.setLeadingColumnOffset(1);

		// Add components to the panel:
		builder.appendSeparator(GlobalResourceLoader.getString(RESOURCE_PATH,
				"general", "general"));
		builder.nextLine();

		builder.append(languageLabel);
		builder.append(languageComboBox, 3);
		builder.nextLine();

		builder.append(lfLabel, lfComboBox, lfButton);
		builder.nextLine();

		builder.append(toolbarLabel);
		builder.append(toolbarComboBox, 3);
		builder.nextLine();

		builder.append(proxyLabel);
		builder.append(proxyButton, 3);
		builder.nextLine();

		builder.appendSeparator(GlobalResourceLoader.getString(RESOURCE_PATH,
				"general", "fonts"));
		builder.nextLine();

		builder.append(overwriteCheckBox, 5);
		builder.nextLine();

		builder.append(mainFontLabel);
		builder.append(mainFontButton, 3);
		builder.nextLine();

		builder.append(textFontLabel);
		builder.append(textFontButton, 3);
		builder.nextLine();

		contentPane.add(builder.getPanel(), BorderLayout.CENTER);

		// init bottom panel with OK, Cancel buttons
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(helpButton);

		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this, "CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		contentPane.add(new DialogHeaderPanel(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "title_header"), GlobalResourceLoader
				.getString(RESOURCE_PATH, "general", "title_description")),
				BorderLayout.NORTH);
	}

	protected void initComponents() {
		lfLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "look_feel"));

		String[] plugins = handler.getPluginIdList();
		lfComboBox = new JComboBox(plugins);
		lfComboBox.setRenderer(new ThemeComboBoxRenderer());
		lfComboBox.setActionCommand("THEME");
		lfComboBox.addActionListener(this);
		lfLabel.setLabelFor(lfComboBox);

		lfButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "look_feel_options"));
		lfButton.setActionCommand("THEME_OPTIONS");
		lfButton.addActionListener(this);

		overwriteCheckBox = new CheckBoxWithMnemonic(GlobalResourceLoader
				.getString(RESOURCE_PATH, "general", "override_fonts"));
		overwriteCheckBox.addActionListener(this);

		mainFontLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "main_font"));
		mainFontButton = new JButton("main font");
		mainFontButton.addActionListener(this);
		mainFontLabel.setLabelFor(mainFontButton);

		textFontLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "text_font"));
		textFontButton = new JButton("text font");
		textFontButton.addActionListener(this);
		textFontLabel.setLabelFor(textFontButton);

		toolbarLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "toolbar"));
		toolbarComboBox = new JComboBox(new String[] {
				GlobalResourceLoader.getString(RESOURCE_PATH, "general",
						"toolbar_icons"),
				GlobalResourceLoader.getString(RESOURCE_PATH, "general",
						"toolbar_below"),
				GlobalResourceLoader.getString(RESOURCE_PATH, "general",
						"toolbar_beside") });
		toolbarLabel.setLabelFor(toolbarComboBox);

		languageLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "locale"));
		languageComboBox = new JComboBox();
		languageLabel.setLabelFor(languageComboBox);
		languageComboBox.setRenderer(new LocaleComboBoxRenderer());

		proxyLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "general", "proxy"));
		proxyButton = new JButton();
		proxyButton.addActionListener(this);
		proxyLabel.setLabelFor(proxyButton);

		// button panel
		okButton = new ButtonWithMnemonic(GlobalResourceLoader.getString("",
				"global", "ok"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);

		cancelButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				"", "global", "cancel"));
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);

		helpButton = new ButtonWithMnemonic(GlobalResourceLoader.getString("",
				"global", "help"));

		// associate with JavaHelp
		HelpManager.getInstance().enableHelpOnButton(helpButton,
				"configuring_columba_8");
		HelpManager.getInstance().enableHelpKey(getRootPane(),
				"configuring_columba_8");
	}

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();

		if (action.equals("OK")) {
			

			updateComponents(false);

			// TODO (@author fdietz): until we can get all the settings update
			// immediately
			// we just open a message box, telling the user to restart

			if (restartRequired) {
				JOptionPane
						.showMessageDialog(this,
								"You have to restart Columba for the changes to take effect!");

				// switch to new theme
				ThemeSwitcher.setTheme();

			}

			setVisible(false);
			
			// notify frame to update
			IContainer[] m = FrameManager.getInstance().getOpenFrames();
			for (int i = 0; i < m.length; i++) {
				JFrame frame = m[i].getFrame();
				ThemeSwitcher.updateFrame(frame);
			}

			// set fonts
			FontProperties.setFont();

			
		} else if (action.equals("CANCEL")) {
			setVisible(false);
		} else if (action.equals("THEME")) {
			// theme selection changed
			String theme = (String) lfComboBox.getSelectedItem();

			IExtension extension = handler.getExtension(theme);

			configID = extension.getMetadata().getAttribute("config");

			lfButton.setEnabled(configID != null);
		} else if (action.equals("THEME_OPTIONS")) {
			try {
				ConfigurationDialog dialog = new ConfigurationDialog(configID);
				dialog.setVisible(true);
			} catch (PluginHandlerNotFoundException phnfe) {
			} catch (PluginLoadingFailedException plfe) {
			}
		}

		Object source = event.getSource();
		if (source == mainFontButton) {
			FontSelectionDialog fontDialog = new FontSelectionDialog(this, null);

			if (fontDialog.showDialog() == FontSelectionDialog.APPROVE_OPTION) {
				mainFont = fontDialog.getSelectedFont();
				mainFontButton.setFont(mainFont);
				mainFontButton.setText(mainFont.getFontName());
			}
		} else if (source == textFontButton) {
			FontSelectionDialog fontDialog = new FontSelectionDialog(this, null);

			if (fontDialog.showDialog() == FontSelectionDialog.APPROVE_OPTION) {
				textFont = fontDialog.getSelectedFont();
				textFontButton.setFont(textFont);
				textFontButton.setText(textFont.getFontName());
			}
		} else if (source == overwriteCheckBox) {
			boolean enabled = overwriteCheckBox.isSelected();
			mainFontLabel.setEnabled(enabled);
			mainFontButton.setEnabled(enabled);
			textFontLabel.setEnabled(enabled);
			textFontButton.setEnabled(enabled);
		} else if (source == proxyButton) {
			ProxyConfigurationDialog dialog = new ProxyConfigurationDialog(this);
			dialog.setProxyHost(proxyHost);
			dialog.setProxyPort(proxyPort);
			if (dialog.showDialog() == ProxyConfigurationDialog.APPROVE_OPTION) {
				proxyHost = dialog.getProxyHost();
				proxyPort = dialog.getProxyPort();
				updateProxyButtonText();
			}
		}
	}

	protected void updateProxyButtonText() {
		if (proxyHost != null) {
			proxyButton.setText(proxyHost + ":" + Integer.toString(proxyPort));
		} else {
			proxyButton.setText(GlobalResourceLoader.getString(RESOURCE_PATH,
					"proxy", "no_proxy"));
		}
	}

	private Font getMainFont() {
		return mainFont;
	}

	private Font getTextFont() {
		return textFont;
	}
}