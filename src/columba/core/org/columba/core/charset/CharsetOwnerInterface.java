/*
 * Created on Jun 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.charset;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface CharsetOwnerInterface {

	public abstract CharsetManager getCharsetManager();		
	public abstract void setCharsetManager(CharsetManager manager);
}
