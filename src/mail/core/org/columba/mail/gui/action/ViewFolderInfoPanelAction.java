/*
 * Created on 22.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.mail.gui.frame.MailFrameView;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ViewFolderInfoPanelAction extends CheckBoxAction {

	/**
	 * @param frameMediator
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public ViewFolderInfoPanelAction(FrameMediator frameController) {
		super(
				frameController,
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_view_folderinfopanel"));
		
		setActionCommand("SHOW_FOLDERINFOPANEL");

	}

	public void actionPerformed(ActionEvent evt) {

		((MailFrameView)frameMediator.getView()).showFolderInfoPanel();
	}

	protected boolean getInitState() {

		return frameMediator.isToolbarEnabled(MailFrameView.FOLDERINFOPANEL);
	}

	public void setCheckBoxMenuItem(
		JCheckBoxMenuItem checkBoxMenuItem,
		AbstractFrameView frameView) {

		super.setCheckBoxMenuItem(checkBoxMenuItem);

		getCheckBoxMenuItem().setSelected(getInitState());
	}

}
