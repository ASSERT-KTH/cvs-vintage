/*
 * Created on 30.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.message;

import java.net.URL;
import java.util.Observable;

/**
 * Encapsulates an URL object.
 *
 * @author fdietz
 */
public class URLObservable extends Observable {

	URL url;
	
	/**
	 * 
	 */
	public URLObservable() {
		super();
		
	}

	/**
	 * @return
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
		
		setChanged();
		notifyObservers();
	}

}
