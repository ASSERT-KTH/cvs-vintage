package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
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
 * individuals on behalf of Collab.Net.
 */ 

import java.util.Enumeration;

// Turbine
import org.apache.turbine.tool.TemplateLink;
import org.apache.turbine.RunData;
import org.apache.turbine.DynamicURI;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.Turbine;
import org.apache.fulcrum.util.parser.ValueParser;
import org.apache.fulcrum.pool.InitableRecyclable;
import org.apache.torque.om.NumberKey;

// Scarab
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.Log;

/**
    This class adds a ModuleManager.CURRENT_PROJECT to every link. This class is added
    into the context to replace the $link that Turbine adds.
    
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @author <a href="mailto:jmcnally@collab.net">John McNally</a>
    @author <a href="mailto:maartenc@tigris.org">Maarten Coene</a>
    @version $Id: ScarabLink.java,v 1.49 2002/07/20 06:45:17 jmcnally Exp $
*/
public class ScarabLink extends TemplateLink
                        implements InitableRecyclable
{
    private static final String TEMPLATE_KEY = "template";
    private RunData data;
    private String label;
    private String attributeText;
    private String alternateText;
    private String currentModuleId;
    private Module currentModule;
    private ScarabRequestTool scarabR;

    /**
     * Constructor.
     *
     * @param data A Turbine RunData object.
     */
    public ScarabLink()
    {
    }

    /**
     * This will initialise a TemplateLink object that was
     * constructed with the default constructor (ApplicationTool
     * method).
     *
     * @param data assumed to be a RunData object
     */
    public void init(Object data)
    {
        // we just blithely cast to RunData as if another object
        // or null is passed in we'll throw an appropriate runtime
        // exception.
        super.init(data);
        this.data = (RunData)data;
        setAbsolute(false);
    }

    public void refresh()
    {
        super.refresh();
        setAbsolute(false);
        label = null;
        attributeText = null;
        alternateText = null;
        currentModuleId = null;
        currentModule = null;
        scarabR = null;
        super.setPage(null);
        super.removePathInfo(TEMPLATE_KEY);
    }

    /**
     * Sets the template variable used by the Template Service.
     *
     * @param t A String with the template name.
     * @return A TemplateLink.
     */
    public TemplateLink setPage(String t)
    {
        String moduleid = data.getParameters().getString(ScarabConstants.CURRENT_MODULE);
        return setPage(t, moduleid);
    }
     
    /**
     * Sets the template variable used by the Template Service. The
     * module id of the new selected module is given.
     *
     * @param t A String with the template name.
     * @param moduleid The id of the new selected module.
     * @return A TemplateLink.
     */
    protected TemplateLink setPage(String t, String moduleid)
    {
        currentModuleId = moduleid;
        if (isSet(moduleid))
        {
            addPathInfo(ScarabConstants.CURRENT_MODULE, moduleid);
        }
        String issuetypeid = data.getParameters()
            .getString(ScarabConstants.CURRENT_ISSUE_TYPE);
        if (isSet(issuetypeid))
        {
            addPathInfo(ScarabConstants.CURRENT_ISSUE_TYPE, issuetypeid);
        }
        String issueKey = data.getParameters()
            .getString(ScarabConstants.REPORTING_ISSUE);
        if (isSet(issueKey))
        {
            addPathInfo(ScarabConstants.REPORTING_ISSUE, issueKey);
        }
        Object threadKey = ((ScarabUser)data.getUser()).getThreadKey();
        if (threadKey != null)
        {
            addPathInfo(ScarabConstants.THREAD_QUERY_KEY, threadKey);
        }
        String reportKey = data.getParameters()
            .getString(ScarabConstants.CURRENT_REPORT);
        if (isSet(reportKey))
        {
            if (t.startsWith("report")) 
            {
                addPathInfo(ScarabConstants.CURRENT_REPORT, reportKey);
            }
            else if (!t.startsWith("help"))
            {
                addPathInfo(ScarabConstants.REMOVE_CURRENT_REPORT, reportKey);
            }            
        }
        // if a screen is to be passed along, add it
        String historyScreen = data.getParameters()
            .getString(ScarabConstants.HISTORY_SCREEN);
        if (isSet(historyScreen))
        {
            addPathInfo(ScarabConstants.HISTORY_SCREEN, historyScreen);
        }
        // if a admin menu is to be passed along, add it
        String adminMenu = data.getParameters()
            .getString(ScarabConstants.CURRENT_ADMIN_MENU);
        if (isSet(adminMenu))
        {
            addPathInfo(ScarabConstants.CURRENT_ADMIN_MENU, adminMenu);
        }
        // if a debug is set, add it
        String debug = data.getParameters()
            .getString(ScarabConstants.DEBUG);
        if (isSet(debug))
        {
            addPathInfo(ScarabConstants.DEBUG, debug);
        }
        
        super.setPage(t);
        return this;
    }

    private boolean isSet(String s)
    {
        return s != null && s.length() > 0;
    }

    /**
     * Returns the name of the template that is being being processed
     */
    public String getCurrentView()
    {
        String temp = data.getParameters().getString(TEMPLATE_KEY,null);
        if ( temp != null )
        {
            temp = temp.replace(',', '/');
        }
        return temp;
    }

    public ScarabLink setPathInfo(String key, String value)
    {
        removePathInfo(key);
        addPathInfo(key, value);
        return this;
    }

    // where is this method being used, i do not understand its purpose - jdm
    public ScarabLink addPathInfo(String key, ParameterParser pp)
    {
        addPathInfo(key, pp);
        return this;
    }

    /**
     * Adds a name=value pair to the path_info string.  This method is missing
     * in DynamicURI, but should be there.
     *
     * @param name A String with the name to add.
     * @param value A double with the value to add.
     */
    public DynamicURI addPathInfo(String name, boolean value)
    {
        addPathInfo(name, (value ? "true" : "false"));
        return this;
    }

    /**
     * Adds all the parameters in a ValueParser to the pathinfo except
     * the action, screen, or template keys as defined by Turbine
     */
    public ScarabLink addPathInfo(ValueParser pp)
    {
        // would be nice if DynamicURI included this method but it requires
        // a specific implementation of ParameterParser
        Enumeration e = pp.keys();
        while ( e.hasMoreElements() )
        {
            String key = (String)e.nextElement();
            if ( !key.equalsIgnoreCase(Turbine.ACTION) &&
                 !key.equalsIgnoreCase(Turbine.SCREEN) &&
                 !key.equalsIgnoreCase(Turbine.TEMPLATE) )
            {
                String[] values = pp.getStrings(key);
                for ( int i=0; i<values.length; i++ )
                {
                    addPathInfo(key, values[i]);
                }
            }
        }
        return this;
    }

    /**
     * Setting the label will cause the link tool to print out the
     * the text for the anchor tag.  This is useful in that if the link
     * should not be active for security reasons it can be completely
     * eliminated.
     *
     * @param label a <code>String</code> value
     * @return a <code>ScarabLink</code> value
     */
    public ScarabLink setLabel(String label)
    {
        this.label = label;
        return this;
    }
    
    /**
     * Allows for setting attributes such as class on an anchor tag
     * <a class="xxx" href="yyy">label</a>.  Note the complete anchor
     * tag is only returned from toString, if the lable has been set
     * so this setter will have no effect unless setLabel is called.
     *
     * @param attributeText a <code>String</code> value
     * @return a <code>ScarabLink</code> value
     */
    public ScarabLink setAttributeText(String attributeText)
    {
        this.attributeText = attributeText;
        return this;
    }

    /**
     * Text that will be returned from toString if the user did not have
     * permission to see the link.  The default is the empty string
     *
     * @param attributeText a <code>String</code> value
     * @return a <code>ScarabLink</code> value
     */
    public ScarabLink setAlternateText(String alternateText)
    {
        this.alternateText = alternateText;
        return this;
    }

    /**
     * Prints out the url and resets the relative flag to true.
     *
     * @return a <code>String</code> url
     */
    public String toString()
    {
        String tostring = null;
        String alternateText = this.alternateText;
        // will reset link if false
        if (isAllowed())
        {
            tostring = getLink();
        }
        else
        {
            tostring = (alternateText == null) ? "" : alternateText;
        }
        refresh();
        return tostring;
    }

    /**
     * Returns a short link for viewing a single issue
     *
     * @param issue an <code>Issue</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    public ScarabLink getIssueIdLink(Issue issue)
        throws Exception
    {
        ScarabLink link = this;
        link.addPathInfo("id", issue.getUniqueId());
        return link; 
    }

    /**
     * Returns a short link for viewing a single issue that will not
     * include session info and will be absolute.  It is meant to
     * be suitable for embedding in an email that points to the issue.
     *
     * @param issue an <code>Issue</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    public ScarabLink getIssueIdAbsoluteLink(Issue issue)
        throws Exception
    {
        ScarabLink link = getIssueIdLink(issue);
        link.setRelative(false).setEncodeUrl(false);
        return link;
    }

    /**
     * Check if the user has the permission to see the link. If the user
     * has the permission(s), <code>true</code> is returned.  if the
     * user does NOT have the proper permissions, this method has the
     * side effect of reseting the link, so that it is ready for use
     * in building the next link.
     */
    public boolean isAllowed()
    {
        boolean allowed = isAllowed(getPage());

        if ( !allowed ) 
        {
            // reset link
            super.toString();
            refresh();
        }
        
        return allowed;
    }

    /**
     * Check if the user has the permission to see the template t. If the user
     * has the permission(s), <code>true</code> is returned. If template t is
     * null, this method returns false.
     */
    public boolean isAllowed(String t)
    {
        if (t == null) 
        {
            //check pathinfo for "id"
            final int count = pathInfo.size();
            for (int i = 0; i < count; i++)
            {
                Object[] pair = (Object[]) pathInfo.get(i);
                if ("id".equals(pair[0])) 
                {
                    t = "ViewIssue.vm";
                    break;
                }
            }
        }
        if (t == null) 
        {
            //check querydata for "id"
            final int count = queryData.size();
            for (int i = 0; i < count; i++)
            {
                Object[] pair = (Object[]) queryData.get(i);
                if ("id".equals(pair[0])) 
                {
                    t = "ViewIssue.vm";
                    break;
                }
            }
        }
        if (t == null)
        {
            return false;
        }
        boolean allowed = false;
        try
        {
            String perm = ScarabSecurity.getScreenPermission(t);
            if (perm != null)
            {
                if (scarabR == null)
                {
                    scarabR = 
                        (ScarabRequestTool)
                        org.apache.turbine.modules.Module.getTemplateContext(data)
                        .get(ScarabConstants.SCARAB_REQUEST_TOOL);
                    currentModule = scarabR.getCurrentModule();
                }
                
                if ( currentModuleId != null )
                {
                    if (currentModule == null ||
                        !currentModule.getModuleId().equals(currentModuleId) ) 
                    {
                        currentModule = ModuleManager
                            .getInstance(new NumberKey(currentModuleId));
                    }
                }
                ScarabUser user = (ScarabUser)data.getUser();
                if (user.hasLoggedIn() 
                    && user.hasPermission(perm, currentModule))
                {
                    allowed = true;
                }
            }
            else 
            {
                allowed = true;
            }
        }
        catch (Exception e)
        {
            allowed = false;
            Log.get().info("Could not check permission due to: ", e);
        }
        return allowed;
    }

    private String getLink()
    {
        String s = null;
        if ( label != null && label.length() > 0 ) 
        {
            StringBuffer sbuf = new StringBuffer(50);
            sbuf.append("<a ");
            if ( attributeText != null && attributeText.length() > 0 ) 
            {
                sbuf.append(attributeText);
                sbuf.append(' ');
            }
            sbuf.append("href=\"")
                .append(super.toString())
                .append("\">")
                .append(label)
                .append("</a>");
            s = sbuf.toString();
        }
        else 
        {
            s = super.toString();
        }
        return s;
    }

    /**
     * Give subclasses access to the RunData, so they do not have to 
     * reimplement the pooling code, just to get at it.
     */
    protected RunData getRunData()
    {
        return data;
    }

    // ****************************************************************
    // ****************************************************************
    // Implementation of Recyclable
    // ****************************************************************
    // ****************************************************************

    private boolean disposed = false;

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
        data = null;
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
}    
