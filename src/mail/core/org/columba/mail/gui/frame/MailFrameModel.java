package org.columba.mail.gui.frame;

import java.util.Enumeration;
import java.util.Hashtable;

import org.columba.core.gui.FrameController;
import org.columba.core.gui.FrameModel;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.table.TableChangedEvent;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailFrameModel extends FrameModel {

	

	public MailFrameModel(XmlElement viewList) {
		super(viewList);

	}

	public FrameController createInstance(String id) {
		return new MailFrameController(id);
	}

	protected XmlElement ensureViewConfigurationExists(String key) {
		XmlElement child = super.ensureViewConfigurationExists(key);

		XmlElement toolbars = new XmlElement("toolbars");
		toolbars.addAttribute("show_main", "true");
		toolbars.addAttribute("show_filter", "true");
		toolbars.addAttribute("show_folderinfo", "true");
		child.addElement(toolbars);
		XmlElement splitpanes = new XmlElement("splitpanes");
		splitpanes.addAttribute("main", "200");
		splitpanes.addAttribute("header", "200");
		splitpanes.addAttribute("attachment", "100");
		child.addElement(splitpanes);

		return child;
	}

	/*
	public void saveAll() {
		
	}
	*/
	/*
	protected void saveAndExit() {
		try {
			Config.save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		MainInterface.popServerCollection.saveAll();

		saveAllFolders();

		System.exit(0);
	}
	*/

	

	public void tableChanged(TableChangedEvent ev) throws Exception {
		for (Enumeration e = controllers.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();

			MailFrameController frame =
				(MailFrameController) controllers.get(key);
			frame.tableController.tableChanged(ev);
		}

	}

	public void updatePop3Menu() {
		for (Enumeration e = controllers.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();

			MailFrameController frame =
				(MailFrameController) controllers.get(key);
			frame.getMenu().updatePopServerMenu();
		}
	}

}
