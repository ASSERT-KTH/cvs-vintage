/*
 * Created on 28.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.main;

import org.apache.commons.cli.CommandLine;
import org.columba.core.plugin.IExtensionInterface;


/**
 * Mail/addressbook components subclass DefaultMain, which
 * correspondes to their main entry point
 * <p>
 * @author fdietz
 */
public interface IComponentPlugin extends IExtensionInterface{
	
	public void init();
	
	public void postStartup();
	
	public void registerCommandLineArguments();
	
    // commandline arguments which can't be handled by the core
    // are passed along to other subcomponents
    public void handleCommandLineParameters(CommandLine commandLine);
   
}
