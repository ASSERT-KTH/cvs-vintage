/*
 * Created on 07.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.mail.gui.config.export.ExportDialog;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExportDialogAction extends FrameAction {

	public ExportDialogAction(AbstractFrameController frameController) {

		super(
				frameController,
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_utilities_exportmail"));

	}

	public void actionPerformed(ActionEvent evt) {
		new ExportDialog();
	}
}
