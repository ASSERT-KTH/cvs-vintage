/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.plugin;

/**
 * {@link PluginManager} throws this exception if it can't 
 * find the requested plugin handler.
 *
 * @author fdietz
 */
public class PluginHandlerNotFoundException extends Exception {

	/**
	 * 
	 */
	public PluginHandlerNotFoundException() {
		super();
		
	}

	/**
	 * @param arg0
	 */
	public PluginHandlerNotFoundException(String arg0) {
		super(arg0);
		
	}

	
	// FIXME
	// 
	// this doesn't work with jdk1.3
	// getCause() was introduced since jdk1.4 !!!
	//
	/*
	public PluginHandlerNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

	
	public PluginHandlerNotFoundException(Throwable arg0) {
		super(arg0);
		
	}
	*/
	
}
