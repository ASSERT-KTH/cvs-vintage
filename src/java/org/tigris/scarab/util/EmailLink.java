package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */

import java.util.ArrayList;
import java.util.List;

// Turbine
import org.apache.fulcrum.pool.InitableRecyclable;

// Scarab
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.SkipFiltering;

/**
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: EmailLink.java,v 1.2 2003/05/03 22:37:24 jon Exp $
 */
public class EmailLink
    implements InitableRecyclable, SkipFiltering
{
    private String label;
    private String attributeText;
    private String alternateText;
    private Integer currentModuleId;
    private Module currentModule;
    private boolean isOmitModule;
    private boolean isOmitIssueType;
    private boolean overrideSecurity;

    private boolean disposed = false;

    /** An ArrayList that contains all the path info if any. */
    private List pathInfo = new ArrayList();

    /** HTTP protocol. */
    public static final String HTTP = "http";

    /** HTTPS protocol. */
    public static final String HTTPS = "https";

    /**
     * Constructor.
     */
    public EmailLink(Module currentModule)
    {
        setCurrentModule(currentModule);
    }

    public void init(Object obj)
    {
        if (obj instanceof Module)
        {
            setCurrentModule((Module)obj);
        }
    }

    public Module getCurrentModule()
    {
        return this.currentModule;
    }

    public void setCurrentModule(Module cM)
    {
        currentModuleId = cM.getModuleId();
        this.currentModule = cM;
    }

    public void refresh()
    {
        label = null;
        attributeText = null;
        alternateText = null;
        currentModuleId = null;
        currentModule = null;
        isOmitModule = false;
        isOmitIssueType = false;
        this.pathInfo.clear();
    }

    private String convertAndTrim(String value)
    {
        String tmp = null;
        if (value != null)
        {
            tmp = value.trim();
            tmp = tmp.toLowerCase();
        }
        return tmp;
    }        

    /**
     * Add a key value pair (in the form of a 2 object array) to the provided
     * list
     *
     * @param list List to add to.
     * @param name A String with the name to add.
     * @param value A String with the value to add.
     */
    protected void addPair(List list,
                           String name,
                           String value)
    {
        Object[] tmp = new Object[2];

        tmp[0] = convertAndTrim(name);
        tmp[1] = value;

        list.add(tmp);
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value A String with the value to add.
     */
    public EmailLink addPathInfo(String name, String value)
    {
        addPair(pathInfo, name, value);
        return this;
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value An Object with the value to add.
     */
    public EmailLink addPathInfo(String name, Object value)
    {
        addPathInfo(name, value.toString());
        return this;
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value A double with the value to add.
     */
    public EmailLink addPathInfo(String name, double value)
    {
        addPathInfo(name, Double.toString(value));
        return this;
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value An int with the value to add.
     */
    public EmailLink addPathInfo(String name, int value)
    {
        addPathInfo(name, new Integer(value).toString());
        return this;
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value A long with the value to add.
     */
    public EmailLink addPathInfo(String name, long value)
    {
        addPathInfo(name, new Long(value).toString());
        return this;
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value A double with the value to add.
     */
    public EmailLink addPathInfo(String name, boolean value)
    {
        addPathInfo(name, (value ? "true" : "false"));
        return this;
    }

    public EmailLink setPathInfo(String key, String value)
    {
        removePathInfo(key);
        addPathInfo(key, value);
        return this;
    }

    /**
     * Helper method to remove one or more pairs by its name (ie key).
     * It is intended to be used with <tt>queryData</tt> and <tt>pathInfo</tt>.
     * @param pairs the list of pairs to look over for removal.
     * @param name the name of the pair(s) to remove.
     */
    protected void removePairByName(List pairs, String name)
    {
        name = convertAndTrim(name);
        // CAUTION: the dynamic evaluation of the size is on purpose because
        // elements may be removed on the fly.
        for (int i = 0; i < pairs.size(); i++)
        {
            Object[] pair = (Object[])pairs.get(i);
            if ( name.equals( (String)pair[0] ) )
            {
                pairs.remove(i);
            }
        }
    }

    /**
     * Removes all the path info elements.
     */
    public void removePathInfo()
    {
        this.pathInfo.clear();
    }

    /**
     * Removes a name=value pair from the path info.
     *
     * @param name A String with the name to be removed.
     */
    public void removePathInfo(String name)
    {
        removePairByName( pathInfo, name );;
    }

    /**
     * Gets the server name.
     *
     * @return A String with the server name.
     */
    public String getServerName()
    {
        String result = null;
        if (currentModule != null)
        {
            result = currentModule.getDomain();
        }
        return result;
    }

    /**
     * Gets the server port.
     *
     * @return A String with the server port.
     */
    public int getServerPort()
    {
        int result = -1;
        try
        {
            if (currentModule != null)
            {
                result = Integer.parseInt(currentModule.getPort());
            }
        }
        catch (Exception e)
        {
            Log.get().debug(e);
        }
        return result;
    }

    /**
     * Gets the server scheme (HTTP or HTTPS).
     *
     * @return A String with the server scheme.
     */
    public String getServerScheme()
    {
        String result = null;
        try
        {
            if (currentModule != null)
            {
                result = currentModule.getScheme();
            }
        }
        catch (Exception e)
        {
            Log.get().debug(e);
        }
        return result;
    }

    /**
     * Gets the server scriptName (/scarab/issues).
     *
     * @return A String with the server scriptName.
     */
    public String getScriptName()
    {
        String result = null;
        try
        {
            if (currentModule != null)
            {
                result = currentModule.getScriptName();
            }
        }
        catch (Exception e)
        {
            Log.get().debug(e);
        }
        return result;
    }

    /**
     * Does this URI have path info.
     */
    public boolean hasPathInfo()
    {
        return ! pathInfo.isEmpty();
    }

    /**
     * This method takes a Vector of key/value arrays and writes it to the
     * supplied StringBuffer as encoded path info.
     *
     * @param pairs A Vector of key/value arrays.
     * @param out Buffer to which encoded path info is written
     */
    protected void renderPathInfo(List pairs, StringBuffer out)
    {
        renderPairs( pairs, out, '/', '/' );
    }

    /**
     * This method takes a List of key/value arrays and converts it
     * into a URL encoded key/value pair format with the appropriate
     * separator.
     *
     * @param out the buffer to write the pairs to.
     * @param pairs A List of key/value arrays.
     * @param pairSep the character to use as a separator between pairs.
     * For example for a query-like rendering it would be '&'.
     * @param keyValSep the character to use as a separator between
     * key and value. For example for a query-like rendering, it would be '='.
     */
    protected void renderPairs(List pairs, StringBuffer out,
                               char pairSep, char keyValSep)
    {
        boolean first = true;

        final int count = pairs.size();
        for (int i = 0; i < count; i++)
        {
            Object[] pair = (Object[]) pairs.get(i);

            if ( first )
            {
                first = false;
            }
            else
            {
                out.append(pairSep);
            }

            writeFastEncoded((String) pair[0], out);
            out.append(keyValSep);
            writeFastEncoded((String) pair[1], out);
        }
    }

    /**
     * Builds the URL with all of the data URL-encoded as well as
     * encoded using HttpServletResponse.encodeUrl().
     *
     * <p>
     * <code><pre>
     * DynamicURI dui = new DynamicURI (data, "UserScreen" );
     * dui.addPathInfo("user","jon");
     * dui.toString();
     * </pre></code>
     *
     *  The above call to toString() would return the String:
     *
     * <p>
     * http://www.server.com/servlets/Turbine/screen/UserScreen/user/jon
     *
     * @return A String with the built URL.
     */
    public String toString()
    {
        StringBuffer output = new StringBuffer();
        output.append(getServerScheme());
        output.append("://");
        output.append(getServerName());
        if ((getServerScheme().equals(HTTP) && getServerPort() != 80)
            || (getServerScheme().equals(HTTPS) && getServerPort() != 443))
        {
            output.append(':');
            output.append(getServerPort());
        }

        output.append(getScriptName());

        if (this.hasPathInfo())
        {
            output.append('/');
            renderPathInfo(this.pathInfo, output);
        }
        return output.toString();
    }

    /**
     * Causes the link to not include the module id.  Useful for templates
     * where a module is not required or desired.
     *
     * @return a <code>EmailLink</code> value
     */
    public EmailLink omitModule()
    {
        isOmitModule = true;
        return this;
    }

    /**
     * Sets the template variable used by the Template Service. The
     * module id of the new selected module is given.
     *
     * @param t A String with the template name.
     * @return A EmailLink.
     */
    public EmailLink setPage(String t)
    {
        addPathInfo(ScarabConstants.TEMPLATE, t);

        if (!isOmitModule)
        {
            addPathInfo(ScarabConstants.CURRENT_MODULE, currentModuleId);
        }
        return this;
    }

    /**
     * Sets the action= value for this URL.
     *
     * <p>By default it adds the information to the path_info instead
     * of the query data.
     *
     * @param action A String with the action value.
     * @return A EmailLink (self).
     */
    public EmailLink setAction(String action)
    {
        addPathInfo(ScarabConstants.ACTION, action);
        return this;
    }

    /**
     * Returns a short link for viewing a single issue
     *
     * @param issue an <code>Issue</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    public EmailLink getIssueIdLink(Issue issue)
        throws Exception
    {
        this.addPathInfo(ScarabConstants.ID, issue.getUniqueId());
        return this;
    }

    // ****************************************************************
    // ****************************************************************
    // Implementation of Recyclable
    // ****************************************************************
    // ****************************************************************

    /**
     * Recycles the object by removing its disposed flag.
     */
    public void recycle()
    {
        disposed = false;
    }

    /**
     * Disposes the object by setting its disposed flag.
     */
    public void dispose()
    {
        refresh();
        disposed = true;
    }

    /**
     * Checks whether the object is disposed.
     *
     * @return true, if the object is disposed.
     */
    public boolean isDisposed()
    {
        return disposed;
    }    

    // ------------------------------------- private constants for url encoding

    /**
     * URL encodes <code>in</code> and writes it to <code>out</code>. If the
     * string is null, 'null' will be written.  
     * This method is faster if
     * the string does not contain any characters needing encoding.  It
     * adds some penalty for strings which actually need to be encoded.
     * for short strings ~20 characters the upside is a 75% decrease.  while
     * the penalty is a 10% increase.  As many query parameters do not need
     * encoding even in i18n applications it should be much better to
     * delay the byte conversion.
     *
     * @param in String to write.
     * @param out Buffer to write to.
     */
    protected static final void writeFastEncoded(String in, StringBuffer out)
    {
        if (in == null || in.length() == 0)
        {
            out.append("null");
            return;
        }

        char[] chars = in.toCharArray();

        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];

            if ( c < 128 && safe[ c ] )
            {
                out.append(c);
            }
            else if (c == ' ')
            {
                out.append('+');
            }
            else
            {
                // since we need to encode we will give up on 
                // doing it the fast way and convert to bytes.
                writeEncoded(new String(chars, i, chars.length-i), out);
                break;
            }
        }
    }

    /**
     * URL encodes <code>in</code> and writes it to <code>out</code>. If the
     * string is null, 'null' will be written.
     *
     * @param in String to write.
     * @param out Buffer to write to.
     */
    protected static final void writeEncoded(String in, StringBuffer out)
    {
        if (in == null || in.length() == 0)
        {
            out.append("null");
            return;
        }

        // This is the most expensive operation:

        byte[] bytes = in.getBytes();

        for (int i = 0; i < bytes.length; i++)
        {
            char c = (char) bytes[i];

            if ( c < 128 && safe[ c ] )
            {
                out.append(c);
            }
            else if (c == ' ')
            {
                out.append('+');
            }
            else
            {
                byte toEscape = bytes[i];
                out.append('%');
                int low = (int) (toEscape & 0x0f);
                int high = (int) ((toEscape & 0xf0) >> 4);
                out.append(HEXADECIMAL[high]);
                out.append(HEXADECIMAL[low]);
            }
        }
    }

    /**
     * Array mapping HEXADECIMAL values to the corresponding ASCII characters.
     */
    private static final char[] HEXADECIMAL =
        {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
        };

    /**
     * Characters that need not be encoded. This is much faster than using a
     * BitSet, and for such a small array the space cost seems justified.
     */
    private static boolean[] safe = new boolean[ 128 ];

    /** Static initializer for {@link #safe} */
    static
    {
        for (int i = 'a'; i <= 'z'; i++)
        {
            safe[ i ] = true;
        }
        for (int i = 'A'; i <= 'Z'; i++)
        {
            safe[ i ] = true;
        }
        for (int i = '0'; i <= '9'; i++)
        {
            safe[ i ] = true;
        }

        safe['-'] = true;
        safe['_'] = true;
        safe['.'] = true;
        safe['!'] = true;
        safe['~'] = true;
        safe['*'] = true;
        safe['\''] = true;
        safe['('] = true;
        safe[')'] = true;
    }
}    
