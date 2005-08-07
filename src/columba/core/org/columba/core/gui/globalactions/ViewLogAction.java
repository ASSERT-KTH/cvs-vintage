package org.columba.core.gui.globalactions;

import java.awt.event.ActionEvent;
import java.io.File;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.config.Config;
import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.GlobalResourceLoader;

public class ViewLogAction extends AbstractColumbaAction {

	public ViewLogAction(IFrameMediator frameMediator) {
		super(frameMediator, GlobalResourceLoader.getString(null, null, "menu_utilities_showerrorlog"));
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		File file = Config.getInstance().getConfigDirectory();
		File logDirectory = new File(file, "log");
		File logFile = new File(logDirectory, "columba.log");
		
		ColumbaDesktop.getInstance().open(logFile);
	}

}
