/*
 * Created on 26.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.addressbook.gui.tree;

import org.columba.addressbook.gui.frame.AddressbookFrameController;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TreeController {

	TreeView view;
	
	AddressbookFrameController frameController;
	/**
	 * 
	 */
	public TreeController( AddressbookFrameController frameController ) {
		super();
		this.frameController = frameController;
		
		
		view = new TreeView(frameController);
		
	}

	/**
	 * @return AddressbookTreeView
	 */
	public TreeView getView() {
		return view;
	}

	/**
	 * @return AddressbookFrameController
	 */
	public AddressbookFrameController getFrameController() {
		return frameController;
	}

}
