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

import org.columba.core.gui.FrameController;
import org.columba.core.gui.FrameView;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class AddressbookController extends FrameController {

	/**
	 * Constructor for AddressbookController.
	 */
	public AddressbookController( String id ) {
		super( id, null );
	}
	
	public void close() {}
	
	public FrameView getView()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#createView()
	 */
	protected FrameView createView() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#init()
	 */
	protected void init() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#initInternActions()
	 */
	protected void initInternActions() {
		// TODO Auto-generated method stub

	}

}
