package org.columba.mail.gui.header;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class HeaderController {

	HeaderView view;
	/**
	 * Constructor for HeaderController.
	 */
	public HeaderController() {
		
		
		String[] keys = new String[4];
		keys[0] = "Subject";
		keys[1] = "Date";
		keys[3] = "To";
		keys[2] = "From";
		
		view = new HeaderView(keys,2);
	}
	
	public HeaderView getView()
	{
		return view;
	}

}
