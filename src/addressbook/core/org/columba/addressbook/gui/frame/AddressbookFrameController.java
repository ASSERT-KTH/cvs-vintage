//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.addressbook.gui.frame;

import org.columba.addressbook.gui.table.TableController;
import org.columba.addressbook.gui.tree.TreeController;
import org.columba.core.gui.frame.FrameController;
import org.columba.core.gui.frame.FrameView;
import org.columba.core.gui.frame.MultiViewFrameModel;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class AddressbookFrameController extends FrameController {
	
	protected TreeController tree;
	protected TableController table;

	/**
	 * Constructor for AddressbookController.
	 */
	public AddressbookFrameController( String id, MultiViewFrameModel model ) {
		super( id, model );
		
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#createView()
	 */
	protected FrameView createView() {
		AddressbookFrameView view = new AddressbookFrameView(this);
		view.init(tree.getView(),table.getView());
		
		view.pack();
		
		return view;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#init()
	 */
	protected void init() {
		tree = new TreeController( this );
		table = new TableController(this);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#initInternActions()
	 */
	protected void initInternActions() {
		

	}

	

	/**
	 * @return AddressbookTableController
	 */
	public TableController getTable() {
		return table;
	}

	/**
	 * @return AddressbookTreeController
	 */
	public TreeController getTree() {
		return tree;
	}

}
