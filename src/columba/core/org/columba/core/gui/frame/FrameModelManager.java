/*
 * Created on 29.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.frame;

import java.util.Vector;

import org.columba.core.main.MainInterface;
import java.util.List;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FrameModelManager {

	List list;
	
	/**
	 * 
	 */
	public FrameModelManager() {
		super();
	
		list = new Vector();	
	}
	
	public void init()
	{
		// if no frame is visible 
		//  -> open mail-component
		/*
		if ( list.size() == 0 )
		{
			MainInterface.frameModel.openView();
		}
		*/
	}
	
	public void register( AbstractFrameController view )
	{
		list.add(view);
	}
	
	public void unregister(AbstractFrameController view)
	{
		list.remove(view);
		
		if ( list.size() == 0 )
		{
			// close Columba
			MainInterface.shutdownManager.shutdown();
		}
	}
	

}
