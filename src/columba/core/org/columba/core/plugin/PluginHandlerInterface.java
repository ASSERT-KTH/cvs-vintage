/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.plugin;

import java.util.ListIterator;

import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface PluginHandlerInterface {

	public String getId();
	public XmlElement getParent();
	public ListIterator getExternalPlugins();
	
}
