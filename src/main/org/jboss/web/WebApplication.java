/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web;

import java.net.URL;
import java.util.Iterator;

import org.w3c.dom.Element;

/** A WebApplication represents the information for a war deployment.

@see AbstractWebContainer

@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public class WebApplication
{
    /** Class loader of this application */
    ClassLoader classLoader = null;
    /** name of this application */
    String name = "";
    /** URL where this application was deployed from */
    URL url;
    /** The root element of thw web-app.xml descriptor. */
    Element webApp;
    /** The root element of thw jboss-web.xml descriptor. */
    Element jbossWeb;
    /** Arbitary data object for storing application specific data */
    Object data;

    /** Create an empty WebApplication instance
     */
    public WebApplication()
    {
    }
    /** Create a WebApplication instance with with given name,
        url and class loader.
    @param name, name of this application
    @param url, url where this application was deployed from
    @param classLoader, Class loader of this application
     */
    public WebApplication(String name, URL url, ClassLoader classLoader)
    {
        this.name = name;
        this.url = url;
        this.classLoader = classLoader;
    }

    /** Get the class loader of this WebApplication.
     * @return The ClassLoader instance of the web application
     */
    public ClassLoader getClassLoader()
    {
      return classLoader;
    }
    /** Set the class loader of this WebApplication.
     * @param classLoader, The ClassLoader instance for the web application
     */
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

	/** Get the name of this WebApplication. 
	 * @return String name of the web application
	 */
    public String getName()
    {
        String n = name;
        if( n == null )
            n = url.getFile();
        return n;
    }
	/** Set the name of this WebApplication. 
	 * @param String name of the web application
	 */
    public void setName(String name)
    {
        this.name = name;
    }


	/** Get the URL from which this WebApplication was deployed
	 * @return URL where this application was deployed from
	 */
    public URL getURL()
    {
        return url;
    }
	/** Set the URL from which this WebApplication was deployed
	 * @param url, URL where this application was deployed from
	 */
    public void setURL(URL url)
    {
        if (url == null)
            throw new IllegalArgumentException("Null URL");
        this.url = url;
    }

    /** Getter for property webApp.
     * @return Value of property webApp.
     */
    public Element getWebApp()
    {
        return webApp;
    }
    /** Setter for property webApp.
     * @param webApp New value of property webApp.
     */
    public void setWebApp(Element webApp)
    {
        this.webApp = webApp;
    }
    
    /** Getter for property jbossWeb.
     * @return Value of property jbossWeb.
     */
    public Element getJbossWeb()
    {
        return jbossWeb;
    }
    /** Setter for property jbossWeb.
     * @param jbossWeb New value of property jbossWeb.
     */
    public void setJbossWeb(Element jbossWeb)
    {
        this.jbossWeb = jbossWeb;
    }

    public Object getAppData()
    {
        return data;
    }
    public void setAppData(Object data)
    {
        this.data = data;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer("{WebApplication: ");
        buffer.append(getName());
        buffer.append(", URL: ");
        buffer.append(url);
        buffer.append(", classLoader: ");
        buffer.append(classLoader);
        buffer.append('}');
        return buffer.toString();
    }
}
