/*
 * Created on 26.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.addressbook.gui.table;

import org.columba.addressbook.gui.frame.AddressbookFrameController;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TableController {

	TableView view;
	
	AddressbookFrameController frameController;
	/**
	 * 
	 */
	public TableController(AddressbookFrameController frameController) {
		super();
		
		this.frameController = frameController;
		
		view = new TableView(frameController);
	}

	/**
	 * @return AddressbookFrameController
	 */
	public AddressbookFrameController getFrameController() {
		return frameController;
	}

	/**
	 * @return TableView
	 */
	public TableView getView() {
		return view;
	}

}
