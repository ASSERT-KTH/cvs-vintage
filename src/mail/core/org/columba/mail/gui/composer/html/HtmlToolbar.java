/*
 * Created on 08.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.composer.html;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.html.HTML;

import org.columba.core.gui.toolbar.ToggleToolbarButton;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.ActionPluginHandler;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.html.action.FontSizeMenu;
import org.columba.mail.gui.composer.html.action.ParagraphMenu;
import org.columba.mail.gui.composer.html.util.FormatInfo;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 *
 * JPanel with useful HTML related actions.
 * 
 * @author fdietz
 * 
 */
public class HtmlToolbar implements ActionListener, Observer {

	ComposerController controller;

	JComboBox paragraphComboBox;
	JComboBox sizeComboBox;
	
	/** 
	 * Flag indicating whether we are programatically changing the 
	 * paragraph combobox, and therefore shall do nothing
	 * in actionPerformed. 
	 */
	private boolean ignoreFormatAction = false;
	
	/**
	 * 
	 */
	public HtmlToolbar(ComposerController controller, PanelBuilder builder) {
		super();
		this.controller = controller;

		try {
			initComponents(builder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// register for text selection changes
		controller.getEditorController().addObserver(this);

		// register for changes to editor type (text / html)
		XmlElement optionsElement =
			MailConfig.get("composer_options").getElement("/options");
		XmlElement htmlElement = optionsElement.getElement("html");
		if (htmlElement == null)
			htmlElement = optionsElement.addSubElement("html");
		String enableHtml = htmlElement.getAttribute("enable", "false");
		htmlElement.addObserver(this);
		
	}

	protected void initComponents(PanelBuilder builder) throws Exception {
		
		CellConstraints cc = new CellConstraints();

		// we generate most buttons using the actions already instanciated
		ActionPluginHandler handler = null;
		try {
			handler =
				(ActionPluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.core.action");
		} catch (PluginHandlerNotFoundException e) {
			e.printStackTrace();
		}

		// init components

		JLabel paraLabel = new JLabel(
				MailResourceLoader.getString(
						"dialog", "composer", "style"));
		paragraphComboBox = new JComboBox(ParagraphMenu.STYLES);
		paragraphComboBox.setActionCommand("PARA");
		paragraphComboBox.addActionListener(this);
		paragraphComboBox.setFocusable(false);
		
		JLabel sizeLabel = new JLabel(
				MailResourceLoader.getString(
						"dialog", "composer", "size"));
		sizeComboBox = new JComboBox(FontSizeMenu.SIZES);
		sizeComboBox.setActionCommand("SIZE");
		sizeComboBox.addActionListener(this);
		sizeComboBox.setSelectedIndex(2);
		sizeComboBox.setFocusable(false);

		// set initial enabled state of combo boxes
		XmlElement optionsElement =
			MailConfig.get("composer_options").getElement("/options");
		XmlElement htmlElement = optionsElement.getElement("html");
		String enableHtml = htmlElement.getAttribute("enable", "false");
		paragraphComboBox.setEnabled(
				(new Boolean(enableHtml)).booleanValue());

		// TODO: sizeComboBox can be enabled as paragraphComboBox when implemented
		sizeComboBox.setEnabled(false);
		
		ToggleToolbarButton boldFormatButton =
			new ToggleToolbarButton(
				handler.getAction("BoldFormatAction", getFrameController()));
		ToggleToolbarButton italicFormatButton =
			new ToggleToolbarButton(
				handler.getAction("ItalicFormatAction", getFrameController()));
		ToggleToolbarButton underlineFormatButton =
			new ToggleToolbarButton(
				handler.getAction(
					"UnderlineFormatAction",
					getFrameController()));
		ToggleToolbarButton strikeoutFormatButton =
			new ToggleToolbarButton(
				handler.getAction(
					"StrikeoutFormatAction",
					getFrameController()));

		ToggleToolbarButton leftJustifyButton =
			new ToggleToolbarButton(
				handler.getAction("LeftJustifyAction", getFrameController()));
		ToggleToolbarButton centerJustifyButton =
			new ToggleToolbarButton(
				handler.getAction("CenterJustifyAction", getFrameController()));
		ToggleToolbarButton rightJustifyButton =
			new ToggleToolbarButton(
				handler.getAction("RightJustifyAction", getFrameController()));

		builder.add(paraLabel, cc.xy(1, 7));

		// nested panel
		JPanel panel = new JPanel();
		FormLayout layout =
			new FormLayout(
				"default, 3dlu,default, 3dlu,default, 3dlu,default, 3dlu, default, 3dlu, default, 3dlu, default, 6dlu, default, 3dlu, default, 3dlu, default, 3dlu,",
				"fill:default");
		PanelBuilder b = new PanelBuilder(panel, layout);

		CellConstraints c = new CellConstraints();

		b.add(paragraphComboBox, cc.xy(1, 1));
		b.add(sizeLabel, cc.xy(3, 1));
		b.add(sizeComboBox, cc.xy(5, 1));
		b.add(boldFormatButton, cc.xy(7, 1));
		b.add(italicFormatButton, cc.xy(9, 1));
		b.add(underlineFormatButton, cc.xy(11, 1));
		b.add(strikeoutFormatButton, cc.xy(13, 1));
		b.add(leftJustifyButton, cc.xy(15, 1));
		b.add(centerJustifyButton, cc.xy(17, 1));
		b.add(rightJustifyButton, cc.xy(19, 1));

		builder.add(panel, cc.xy(3, 7));
	}

	/**
	 * @return
	 */
	public ComposerController getFrameController() {
		return controller;
	}

	/**
	 * Method is called when text selection has changed.
	 * <p>
	 * Set state of togglebutton / -menu to pressed / not pressed
	 * when selections change. 
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1) {
		
		if (arg0 instanceof HtmlEditorController) {

			// Handling of paragraph combo box
	
			// select the item in the combo box corresponding to present format
			FormatInfo info = (FormatInfo) arg1;
			if        (info.isHeading1()) {
				selectInParagraphComboBox(HTML.Tag.H1);
			} else if (info.isHeading2()) {
				selectInParagraphComboBox(HTML.Tag.H2);
			} else if (info.isHeading3()) {
				selectInParagraphComboBox(HTML.Tag.H3);
			} else if (info.isPreformattet()) {
				selectInParagraphComboBox(HTML.Tag.PRE);
			} else if (info.isAddress()) {
				selectInParagraphComboBox(HTML.Tag.ADDRESS);
			} else {
				// select the "Normal" entry as default
				selectInParagraphComboBox(HTML.Tag.P);
			}

			// Font size combo box
		
			// TODO: Add handling for font size combo box

		} else if (arg0 instanceof XmlElement) {
			// possibly change btw. html and text
			XmlElement e = (XmlElement) arg0;

			if (e.getName().equals("html")) {
				String enableHtml = e.getAttribute("enable", "false");
				boolean html = (new Boolean(enableHtml)).booleanValue();
	
				// paragraphComboBox should only be enabled in html mode			
				paragraphComboBox.setEnabled(html);

				// TODO: Add handling for font size combo box
				
			}
		}
					
				
	}
	
	/**
	 * Private utility to select an item in the paragraph combo box, 
	 * given the corresponding html tag.
	 * If such a sub menu does not exist - nothing happens
	 */
	private void selectInParagraphComboBox(HTML.Tag tag) {
		
		for (int i=0; i<ParagraphMenu.STYLE_TAGS.length; i++) {
			if (tag.equals(ParagraphMenu.STYLE_TAGS[i])) {
				// found
				
				if (paragraphComboBox.getSelectedIndex() != i) {
					// need to change selection
					
					// Set ignore flag
					ignoreFormatAction = true;

					paragraphComboBox.setSelectedIndex(i);

					// clear ignore flag
					ignoreFormatAction = false;
				}
				return;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		if (action.equals("PARA")) {
			// selection in the paragraph combo box

			if (!ignoreFormatAction) {
				// only do something if ignore flag is not set
				
				HtmlEditorController ctrl =
					(HtmlEditorController) getFrameController()
						.getEditorController();
	
				// set paragraph formatting according to the selection
				int selectedIndex = paragraphComboBox.getSelectedIndex();
				HTML.Tag tag = ParagraphMenu.STYLE_TAGS[selectedIndex];
				
				ColumbaLogger.log.info(
						"Setting paragraph format to: " + 
						tag.toString());
				
				ctrl.setParagraphFormat(tag);
			}
			
		} else if (action.equals("SIZE")) {
			int selectedIndex = sizeComboBox.getSelectedIndex();

			// TODO: implement action for font size combo box!
		}

	}

}
