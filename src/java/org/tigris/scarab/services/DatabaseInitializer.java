package org.tigris.scarab.services;

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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Iterator;

import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.localization.Localization;
import org.apache.turbine.Turbine;

import org.apache.torque.Torque;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.GlobalParameterPeer;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;

/**
 * Transforms localization keys stored in the database into their
 * respective localized values upon initial startup of Fulcrum.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: DatabaseInitializer.java,v 1.17 2004/05/01 19:04:27 dabbous Exp $
 */
public class DatabaseInitializer
    extends BaseService
{
    private static final String PRE_L10N = "pre-l10n";
    private static final String STARTED_L10N = "started";
    private static final String POST_L10N = "post-l10n";
    private static final String DB_L10N_STATE = "db-l10n-state";

    // some old parameter keys for the http parameters
    private static final String MODULE_DOMAIN = "module-domain";
    private static final String MODULE_PORT = "module-port";
    private static final String MODULE_SCHEME = "module-scheme";
    private static final String MODULE_SCRIPT_NAME = "module-script-name";


    /**
     * The values returned by {@link #getInputData()}.
     */
    private static final String[][] BEAN_METHODS =
    {
        {"InitDbScarabBundle", "MITList", "Name"},
        {"InitDbScarabBundle", "Attribute", "Name", "Description"},
        {"InitDbScarabBundle", "AttributeOption", "Name"},
        {"InitDbScarabBundle", "IssueType", "Name", "Description"},
        {"InitDbScarabBundle", "AttributeGroup", "Name", "Description"},
        {"InitDbScarabBundle", "RModuleAttribute", "DisplayValue"},
        {"InitDbScarabBundle", "Scope", "Name"}
    };

    /**
     * Initializes the service by setting up Torque.
     */
    public void init()
        throws InitializationException
    {
        try
        {
            String dbState =
                GlobalParameterManager.getString(DB_L10N_STATE);
            if (PRE_L10N.equals(dbState) || STARTED_L10N.equals(dbState))
            {
                long start = System.currentTimeMillis();
                Log.get().info("New scarab database; localizing strings for '" +
                               ScarabConstants.DEFAULT_LOCALE.getDisplayName() + "'...");
                GlobalParameterManager.setString(DB_L10N_STATE, STARTED_L10N);
                initdb(ScarabConstants.DEFAULT_LOCALE);     
                GlobalParameterManager.setString(DB_L10N_STATE, POST_L10N);
                Log.get().info("Done localizing.  Time elapsed = " + 
                    (System.currentTimeMillis()-start)/1000.0 + " s");
            }

            checkNewHttpParameters();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new InitializationException(
                "Failed to localize default data!", e); //EXCEPTION
        }

        // indicate that the service initialized correctly
        setInit(true);
    }

    protected String[][] getInputData()
    {
        return BEAN_METHODS;
    }

    private void initdb(Locale defaultLocale)
        throws Exception
    {
        String[][] methodNames = getInputData();        
        Class[] stringSig = {String.class};
        Class[] critSig = {Criteria.class};

        for (int m=0; m<methodNames.length; m++) 
        {
            String[] row = methodNames[m];
            String omClassName = "org.tigris.scarab.om." + row[1];
            Class peerClass = Class.forName(omClassName + "Peer");
            Object peerObject = peerClass.newInstance();

            Method doSelect = peerClass.getMethod("doSelect", critSig);
            Object[] critArg = {new Criteria()};
            List omlist = (List)doSelect.invoke(peerObject, critArg);
            if (!omlist.isEmpty()) 
            {
                Class omClass = Class.forName(omClassName);
                int nbrBeanMethods = row.length - 2;
                Method[] getters = new Method[nbrBeanMethods];
                Method[] setters = new Method[nbrBeanMethods];
                for (int n=2; n<row.length; n++) 
                {
                    getters[n-2] = omClass.getMethod("get"+row[n], null);
                    setters[n-2] = omClass.getMethod("set"+row[n], stringSig);
                }
                Method save = omClass.getMethod("save", null);

                Iterator i = omlist.iterator();
                while (i.hasNext()) 
                {
                    Object om = i.next();
                    for (int n=0; n<getters.length; n++) 
                    {
                        Log.get().debug("Converting " + row[1] + '.' + 
                                        getters[n].getName());
                        String key = (String)getters[n].invoke(om, null);
                        String value = null;

                        // Oracle returns null on empty field.
                        if (key != null)
                        {
                            try 
                            {
                                value = Localization.getString(row[0], 
                                                               defaultLocale,
                                                               key);
                            }
                            catch (MissingResourceException e)
                            {
                                Log.get().debug("Missing database initialization "
                                                + "resource: " + e.getMessage());
                            } 
                        }
                        if (value != null) 
                        {
                            Object[] arg = {value};
                            setters[n].invoke(om, arg);
                        }
                    }
                    save.invoke(om, null);
                } 
            }
        }
    }

    private void checkNewHttpParameters()
        throws Exception
    {
        String oldDomain = GlobalParameterManager
            .getString(ScarabConstants.HTTP_DOMAIN);
        String oldScheme = GlobalParameterManager
            .getString(ScarabConstants.HTTP_SCHEME);
        String oldScriptName = GlobalParameterManager
            .getString(ScarabConstants.HTTP_SCRIPT_NAME);
        String oldPort = GlobalParameterManager
            .getString(ScarabConstants.HTTP_PORT);

        if (oldDomain == null)
        {
            // installations with post-b15 but pre-b16 may have module
            // specific values.  
            Criteria crit = new Criteria();
            crit.add(GlobalParameterPeer.NAME, MODULE_DOMAIN);
            List parameters = GlobalParameterPeer.doSelect(crit);
            if (!parameters.isEmpty()) 
            {
                oldDomain = ((GlobalParameter)parameters.get(0)).getValue();
            }

            crit = new Criteria();
            crit.add(GlobalParameterPeer.NAME, MODULE_SCHEME);
            parameters = GlobalParameterPeer.doSelect(crit);
            if (!parameters.isEmpty()) 
            {
                oldScheme = ((GlobalParameter)parameters.get(0)).getValue();
            }

            crit = new Criteria();
            crit.add(GlobalParameterPeer.NAME, MODULE_SCRIPT_NAME);
            parameters = GlobalParameterPeer.doSelect(crit);
            if (!parameters.isEmpty()) 
            {
                oldScriptName = 
                    ((GlobalParameter)parameters.get(0)).getValue();
            }

            crit = new Criteria();
            crit.add(GlobalParameterPeer.NAME, MODULE_PORT);
            parameters = GlobalParameterPeer.doSelect(crit);
            if (!parameters.isEmpty()) 
            {
                oldPort = ((GlobalParameter)parameters.get(0)).getValue();
            }
        }

        String newValue = Turbine.getConfiguration()
            .getString(ScarabConstants.HTTP_DOMAIN);
        if (newValue != null && newValue.trim().length() != 0
            && !newValue.equals(oldDomain)) 
        {
            GlobalParameterManager
                .setString(ScarabConstants.HTTP_DOMAIN, newValue);
            fixIssueIdCounters(oldDomain, newValue);
        }

        newValue = Turbine.getConfiguration()
            .getString(ScarabConstants.HTTP_SCHEME);
        if (newValue != null && newValue.trim().length() != 0
            && !newValue.equals(oldScheme)) 
        {
            GlobalParameterManager
                .setString(ScarabConstants.HTTP_SCHEME, newValue);
        }

        newValue = Turbine.getConfiguration()
            .getString(ScarabConstants.HTTP_SCRIPT_NAME);
        if (newValue != null && newValue.trim().length() != 0
            && !newValue.equals(oldScriptName)) 
        {
            GlobalParameterManager
                .setString(ScarabConstants.HTTP_SCRIPT_NAME, newValue);
        }

        newValue = Turbine.getConfiguration()
            .getString(ScarabConstants.HTTP_PORT);
        if (newValue != null && newValue.trim().length() != 0
            && !newValue.equals(oldPort)) 
        {
            GlobalParameterManager
                .setString(ScarabConstants.HTTP_PORT, newValue);
        }
    }

    private void fixIssueIdCounters(String oldDomain, String newDomain)
        throws Exception
    {
        String sql = 
            "select NEXT_ID from ID_TABLE where TABLE_NAME='ID_TABLE'";
        Connection con = null;
        try 
        {
            con = Torque.getConnection();
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery(sql);
            int maxId = 0;
            if (rs.next()) 
            {
                maxId = rs.getInt(1);
            }
            s.close();

            for (int id = 1000; id <= maxId; id++) 
            {
                sql = "select TABLE_NAME from ID_TABLE where ID_TABLE_ID=" 
                    + id;
                s = con.createStatement();
                rs = s.executeQuery(sql);
                if (rs.next()) 
                {
                    String oldKey = rs.getString(1);
                    s.close();
                    int hyphenPos = oldKey.indexOf('-');
                    String newKey = null;
                    if ( (oldDomain == null || oldDomain.length() == 0)
                         && hyphenPos <= 0) 
                    {
                        newKey = newDomain + '-' + oldKey;
                    }
                    else 
                    {
                        String prefix = oldKey.substring(0, hyphenPos);
                        String code = oldKey.substring(hyphenPos+1);
                        if (prefix.equals(oldDomain)) 
                        {
                            newKey = newDomain + '-' + code;
                        }
                    }
                    
                    if (newKey != null) 
                    {
                        sql = "update ID_TABLE set TABLE_NAME='" + newKey +
                            "' where ID_TABLE_ID=" + id;
                        s = con.createStatement();
                        s.executeUpdate(sql);
                        s.close();
                    }
                }
            }
        }
        finally
        {
            if (con != null) 
            {
                con.close();
            }
        }
    }
}
