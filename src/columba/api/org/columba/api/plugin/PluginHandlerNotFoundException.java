/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.api.plugin;

import org.columba.api.exception.BaseException;

/**
 * {@link IPluginManager}throws this exception if it can't find the requested
 * plugin handler.
 * 
 * @author fdietz
 */
public class PluginHandlerNotFoundException extends BaseException {
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

}