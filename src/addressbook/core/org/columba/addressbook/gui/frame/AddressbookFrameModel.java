/*
 * Created on 26.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.addressbook.gui.frame;

import org.columba.core.gui.FrameController;
import org.columba.core.gui.MultiViewFrameModel;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddressbookFrameModel extends MultiViewFrameModel {

	/**
	 * @param viewList
	 */
	public AddressbookFrameModel(XmlElement viewList) {
		super(viewList);
		
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.DefaultFrameModel#createInstance(java.lang.String)
	 */
	public FrameController createInstance(String id) {
		return new AddressbookController(id, this);
	}

}
