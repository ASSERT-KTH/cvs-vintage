/*
 * Created on 30.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.gui.message;

import java.net.URL;

import java.util.Observable;

import org.columba.mail.gui.message.util.ColumbaURL;


/**
 * Encapsulates an URL object.
 *
 * @author fdietz
 */
public class URLObservable extends Observable {
		private ColumbaURL url;
		
    /**
		 *
		 */
    public URLObservable() {
        super();
    }

    /**
		 * @param url
		 */
    public void setUrl(ColumbaURL url) {
        this.url = url;

        setChanged();
        notifyObservers();
    }
    
    public ColumbaURL getUrl()
    {
      return url;
    }
    
}
