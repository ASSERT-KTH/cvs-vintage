/*
 * Created on 12.03.2003
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
package org.columba.core.gui.menu;

import java.util.List;
import java.util.ListIterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.columba.core.action.BasicAction;
import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.ActionPluginHandler;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class PopupMenuGenerator extends AbstractMenuGenerator {

	/**
	 * @param frameMediator
	 * @param path
	 */
	public PopupMenuGenerator(
		FrameMediator frameController,
		String path) {
		super(frameController, path);

	}

	public void createPopupMenu(JPopupMenu menu) {
		menu.removeAll();
		createPopupMenu(getMenuRoot(), menu);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.AbstractMenuGenerator#getMenuRoot()
	 */
	public XmlElement getMenuRoot() {

		return xmlFile.getRoot().getElement("menu");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.AbstractMenuGenerator#getRootElementName()
	 */
	public String getRootElementName() {
		return "menu";
	}

	protected JPopupMenu createPopupMenu(
		XmlElement menuElement,
		JPopupMenu menu) {
		List childs = menuElement.getElements();
		ListIterator it = childs.listIterator();

		while (it.hasNext()) {
			XmlElement next = (XmlElement) it.next();
			String name = next.getName();
			if (name.equals("menuitem")) {

				if (next.getAttribute("action") != null) {
					try {
						BasicAction action =
							(
								(
									ActionPluginHandler) MainInterface
										.pluginManager
										.getHandler(
									"org.columba.core.action")).getAction(
								next.getAttribute("action"),
								frameController);
						if (action != null) {
							//							use our custom CMenuItem here
							// -> in order to support JavaHelp support
							// -> @see CMenuItem for more details
							CMenuItem tmp = new CMenuItem(action);
							// display tooltip in statusbar
							tmp.addMouseListener(
								frameController.getMouseTooltipHandler());
							menu.add(tmp);
							menu.add(tmp);
						}
					} catch (Exception e) {
						NotifyDialog dialog = new NotifyDialog();
						dialog.showDialog(
							"Error while loading plugin "
								+ next.getAttribute("action")
								+ ". This probably means that the class wasn't found. Compile the plugin to create it.");

						if (MainInterface.DEBUG) {
							ColumbaLogger.log.error(
								e + ": " + next.getAttribute("action"));
							e.printStackTrace();
						}
					}
				} else if (next.getAttribute("checkboxaction") != null) {
					try {
						CheckBoxAction action =
							(CheckBoxAction)
								(
									(
										ActionPluginHandler) MainInterface
											.pluginManager
											.getHandler(
										"org.columba.core.action")).getAction(
								next.getAttribute("checkboxaction"),
								frameController);
						JCheckBoxMenuItem menuitem =
							new JCheckBoxMenuItem(action);
						// display tooltip in statusbar
						menuitem.addMouseListener(
							frameController.getMouseTooltipHandler());
						menu.add(menuitem);
						action.setCheckBoxMenuItem(menuitem);
					} catch (Exception e) {
						e.printStackTrace();
						ColumbaLogger.log.error(e);
					}
				} else if (next.getAttribute("imenu") != null) {
					try {
						menu.add(
							(
								(
									ActionPluginHandler) MainInterface
										.pluginManager
										.getHandler(
									"org.columba.core.action")).getIMenu(
								next.getAttribute("imenu"),
								frameController));
					} catch (Exception e) {
						e.printStackTrace();
						ColumbaLogger.log.error(e);
					}
				}

			} else if (name.equals("separator")) {

				menu.addSeparator();

			} else if (name.equals("menu")) {
				menu.add(createSubMenu(next));
			}
		}

		return menu;
	}

}
