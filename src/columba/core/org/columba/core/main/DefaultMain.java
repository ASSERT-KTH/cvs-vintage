/*
 * Created on 28.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.main;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class DefaultMain {

	public abstract void handleCommandLineParameters(String[] args);
		
	public abstract void initConfiguration();
	
	public abstract void initGui();
	
	public abstract void initPlugins();
	
	
}
