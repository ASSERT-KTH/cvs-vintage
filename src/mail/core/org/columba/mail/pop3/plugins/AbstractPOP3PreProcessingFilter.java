/*
 * Created on Apr 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.pop3.plugins;

import org.columba.core.plugin.DefaultPlugin;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AbstractPOP3PreProcessingFilter extends DefaultPlugin {

	protected XmlElement rootElement;
	
	public AbstractPOP3PreProcessingFilter( XmlElement rootElement )
	{
		this.rootElement = rootElement;
	}
	
	public abstract String modify( String rawString );

	/**
	 * @return
	 */
	public XmlElement getRootElement() {
		return rootElement;
	}

}
