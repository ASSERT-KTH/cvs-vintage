/*
 * Created on 28.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.main;

/**
 * Mail/addressbook components subclass DefaultMain, which 
 * correspondes to their main entry point
 * <p>
 * @author fdietz
 */
public abstract class DefaultMain {

	// commandline arguments which can't be handled by the core
	// are passed along to other subcomponents
	public abstract void handleCommandLineParameters(String[] args);
		
	// register all configuration files mail/addressbook dependent
	public abstract void initConfiguration();
	
	// initialize all the gui stuff
	public abstract void initGui();
	
	// initialize all the plugin extension handlers
	public abstract void initPlugins();
	
	
}
