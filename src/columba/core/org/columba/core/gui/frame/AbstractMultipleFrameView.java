/*
 * Created on 05.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.frame;

import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AbstractMultipleFrameView extends AbstractFrameView {

	/**
	 * @param frameController
	 */
	public AbstractMultipleFrameView(AbstractFrameController frameController) {
		super(frameController);
		
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameView#createMenu(org.columba.core.gui.frame.AbstractFrameController)
	 */
	protected abstract Menu createMenu(AbstractFrameController controller) ;

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameView#createToolbar(org.columba.core.gui.frame.AbstractFrameController)
	 */
	protected abstract ToolBar createToolbar(AbstractFrameController controller) ;

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameView#createDefaultConfiguration(java.lang.String)
	 */
	protected XmlElement createDefaultConfiguration(String id) {
		
		XmlElement child; // = getChild(new Integer(key).toString());

		// create new node
		child = new XmlElement("view");
		child.addAttribute("id", id);
		XmlElement window = new XmlElement("window");
		window.addAttribute("x", "0");
		window.addAttribute("y", "0");
		window.addAttribute("width", "900");
		window.addAttribute("height", "700");
		window.addAttribute("maximized", "true");
		child.addElement(window);

		return child;
	
	}

}
