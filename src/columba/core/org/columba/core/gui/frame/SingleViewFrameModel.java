/*
 * Created on 29.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.frame;

import org.columba.core.config.ViewItem;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class SingleViewFrameModel extends DefaultFrameModel {

	/**
	 * 
	 */
	public SingleViewFrameModel(XmlElement root) {
		super();

		defaultView = root;
	}

	protected void register(String id, AbstractFrameController controller) {

		controller.setViewItem(new ViewItem(defaultView));

	}

}
