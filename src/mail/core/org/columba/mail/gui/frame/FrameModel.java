package org.columba.mail.gui.frame;

import java.util.Vector;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FrameModel {
	
	private Vector views;
	
	/**
	 * Registers the View
	 * @param view
	 */
	public void register( FrameView view ) {
		views.add( view );	
	}
	
	/**
	 * Unregister the View from the Model
	 * @param view
	 * @return boolean true if there are no more views for the model
	 */
	public boolean unregister( FrameView view ) {
		views.remove(view);
		return views.size() == 0;
	}

}
