/*
 * Created on 08.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.composer.html;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
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
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.html.action.FontSizeMenu;
import org.columba.mail.gui.composer.html.action.ParagraphMenu;
import org.columba.mail.gui.composer.html.util.FormatInfo;

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

		// TODO: localize
		JLabel paraLabel = new JLabel("Style:");
		paragraphComboBox = new JComboBox(ParagraphMenu.STYLES);
		paragraphComboBox.setActionCommand("PARA");
		paragraphComboBox.addActionListener(this);
		paragraphComboBox.setFocusable(false);
		
		// TODO: localize
		JLabel sizeLabel = new JLabel("Size:");
		sizeComboBox = new JComboBox(FontSizeMenu.SIZES);
		sizeComboBox.setActionCommand("SIZE");
		sizeComboBox.addActionListener(this);
		sizeComboBox.setSelectedIndex(2);
		sizeComboBox.setFocusable(false);
		
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
		
		if (arg1 == null) {
			return; 
		}
	
		// Handling of paragraph combo box
	
		// check if text is selected - if not the combo box is disabled
		FormatInfo info = (FormatInfo) arg1;
		paragraphComboBox.setEnabled(info.isTextSelected());
				
		// select the item in the combo box corresponding to present format
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
					
					// TODO: Find solution for selection without firing action events
					//       being fired. The present solution is a quick hack!
					//       (which does not handle ItemListeners)

					// remove action listeners - to avoid actionPerformed being called
					ColumbaLogger.log.debug("Temporarily removing action listeners");
					ActionListener[] al = paragraphComboBox.getActionListeners();
					for (int j=0; j<al.length; j++) {
						paragraphComboBox.removeActionListener(al[j]);
					}
					// set new selected item
					paragraphComboBox.setSelectedIndex(i);
					// re-add action listeners
					for (int j=0; j<al.length; j++) {
						paragraphComboBox.addActionListener(al[j]);
					}
					ColumbaLogger.log.debug("Action listeners readded");
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
			int selectedIndex = paragraphComboBox.getSelectedIndex();

			HtmlEditorController ctrl =
				(HtmlEditorController) getFrameController()
					.getEditorController();

			switch (selectedIndex) {
				case 0 :
					// normal <p>
					ctrl.setFormatNormal();
					break;

				case 1 :
					// preformatted <pre>
					break;

					// TODO: Implement <pre>

				case 2 :
					// <h1>
					ctrl.setFormatHeading(1);
					break;
				case 3 :
					// <h2>
					ctrl.setFormatHeading(2);
					break;
					
				case 4 :
					// <h3>
					ctrl.setFormatHeading(3);
					break;

				case 5 :
					// address
					break;

					// TODO: Implement <address>

				default:
					ColumbaLogger.log.error("Unsupported format");
					break;
			}

		} else if (action.equals("SIZE")) {
			int selectedIndex = sizeComboBox.getSelectedIndex();

			// TODO: implement action!
		}

	}

}
