/*
 * Created on 08.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.composer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.toolbar.ToggleToolbarButton;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.ActionPluginHandler;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.mail.gui.composer.html.HtmlEditorController;
import org.columba.mail.gui.composer.html.action.FontSizeMenu;
import org.columba.mail.gui.composer.html.action.ParagraphMenu;

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
public class HtmlToolbar implements ActionListener {

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

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		if (action.equals("PARA")) {
			int selectedIndex = paragraphComboBox.getSelectedIndex();

			HtmlEditorController c =
				(HtmlEditorController) getFrameController()
					.getEditorController();

			switch (selectedIndex) {
				case 0 :
					{
						// normal <p>
					}
				case 1 :
					{
						// preformatted <pre>
					}
				case 2 :
					{
						// <H1>
						c.setFormatHeading(1);
					}
				case 3 :
					{
						// <H2>
						c.setFormatHeading(2);
					}
				case 4 :
					{
						// <H3>
						c.setFormatHeading(3);
					}
				case 5 :
					{
						// address
					}

			}

		} else if (action.equals("SIZE")) {
			int selectedIndex = sizeComboBox.getSelectedIndex();

			// TODO: implement action!
		}

	}

}
