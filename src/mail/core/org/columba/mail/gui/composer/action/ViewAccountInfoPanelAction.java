/*
 * Created on 22.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.mail.gui.composer.ComposerView;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ViewAccountInfoPanelAction extends CheckBoxAction {

	public ViewAccountInfoPanelAction(AbstractFrameController frameController) {
		super(frameController, "AccountInfoPanel");
		setActionCommand("SHOW_ACCOUNTINFOPANEL");
	}

	public void actionPerformed(ActionEvent evt) {

		((ComposerView) frameMediator.getView()).showAccountInfoPanel();
	}

	protected boolean getInitState() {

		return frameMediator.isToolbarEnabled(ComposerView.ACCOUNTINFOPANEL);
	}

	public void setCheckBoxMenuItem(
		JCheckBoxMenuItem checkBoxMenuItem,
		AbstractFrameView frameView) {

		super.setCheckBoxMenuItem(checkBoxMenuItem);

		getCheckBoxMenuItem().setSelected(getInitState());
	}

}
