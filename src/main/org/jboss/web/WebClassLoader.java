package org.jboss.web;

import java.net.URL;
import java.net.URLClassLoader;

/** A simple subclass of URLClassLoader that overrides the getURLs()
method to return a different set of URLs for remote loading than what is used
for local loading. This class is used in conjunction with the WebService
mbean to allow dynamic loading of resources and classes from deployed ears,
ejb jars and wars.

@see #getUrls()
@see #setWebURLs(URL[])

@author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@author Sacha Labourey <sacha.labourey@cogito-info.ch>
@author Vladimir Blagojevic <vladimir@xisnext.2y.net>
@version $Revision: 1.2 $
*/
public class WebClassLoader extends URLClassLoader
{
    /** The URLs returned by the getURLs() method override */
    private URL[] webURLs;

    /** Creates new WebClassLoader */
    public WebClassLoader(URL[] urls)
    {
        super(urls);
    }
    public WebClassLoader(URL[] urls, ClassLoader parent)
    {
        super(urls, parent);
    }
    public WebClassLoader(URL[] urls, ClassLoader parent, java.net.URLStreamHandlerFactory factory)
    {
        super(urls, parent, factory);
    }

    /** Get the list of URLs that should be used as the RMI annotated codebase.
     This is the URLs previously set via setWebURLs. If setWebURLs has not
     been invoked or was passed in a null value, the super class value of
     getURLs() is used.
    @return the local web URLs if not null, else the value of super.getURLs()
     */
    public URL[] getURLs()
    {
        URL[] urls = webURLs;
        if( urls == null )
            urls = super.getURLs();
        return urls;
    }
    /** Access the URLClassLoader.getURLs() value.
     @return the URLs used for local class and resource loading
     */
    public URL[] getLocalURLs()
    {
        return super.getURLs();
    }

    /** Set the URLs that should be returned from this classes getURLs() override.
     @param webURLs, the set of URL codebases to be used for remote class loading.
     */
    public void setWebURLs(URL[] webURLs)
    {
        this.webURLs = webURLs;
    }
}
