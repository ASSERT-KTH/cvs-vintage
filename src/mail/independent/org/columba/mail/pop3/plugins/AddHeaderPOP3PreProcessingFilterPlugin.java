/*
 * Created on Apr 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.pop3.plugins;

import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddHeaderPOP3PreProcessingFilterPlugin
	extends AbstractPOP3PreProcessingFilter {

	public AddHeaderPOP3PreProcessingFilterPlugin( XmlElement rootElement)
	{
		super(rootElement);
		
	}
	
	/* (non-Javadoc)
	 * @see org.columba.mail.pop3.plugins.AbstractPOP3PreProcessingFilter#modify(java.lang.String)
	 */
	public String modify(String rawString) {
		
		String version = rootElement.getAttribute("version");
		
		return "X-Columba-version: "+version+"\n"+rawString;
	}

}
