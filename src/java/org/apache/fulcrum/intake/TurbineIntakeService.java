package org.apache.fulcrum.intake;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPool;

import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.transform.XmlToAppData;
import org.apache.fulcrum.intake.xmlmodel.AppData;
import org.apache.fulcrum.intake.xmlmodel.XmlGroup;

/**
 * This service provides access to input processing objects based
 * on an XML specification.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: TurbineIntakeService.java,v 1.1 2004/10/24 22:12:31 dep4b Exp $
 */
public class TurbineIntakeService
    extends BaseService
    implements IntakeService
{
    /** Array of group names. */
    private String[] groupNames;

    /** The cache of group names. */
    private Map groupNameMap;

    /** The cache of group keys. */
    private Map groupKeyMap;

    /** The cache of property getters. */
    private Map getterMap;

    /** The cache of property setters. */
    private Map setterMap;

    /** Keep a OMTool to be able to retrieve objects */
    //private OMTool omTool;

    /** The top element of the object tree */
    private AppData appData;

    /** pools Group objects */
    KeyedObjectPool keyedPool;

    // a couple integers for a switch statement
    private static final int GETTER = 0;
    private static final int SETTER = 1;

    /**
     * Constructor.
     */
    public TurbineIntakeService()
    {
    }

    /**
     * Called the first time the Service is used.
     *
     * @param config A ServletConfig.
     */
    public void init()
        throws InitializationException
    {
        String xmlPath = getConfiguration()
            .getString(XML_PATH, XML_PATH_DEFAULT);
        String appDataPath = getConfiguration()
            .getString(SERIAL_XML, SERIAL_XML_DEFAULT);
        
        String SERIALIZED_ERROR_MSG = 
            "Intake initialization could not be serialized " +
            "because writing to " + appDataPath + " was not " +
            "allowed.  This will require that the xml file be " +
            "parsed when restarting the application.";

        if ( xmlPath == null )
        {
            String pathError =
                "Path to intake.xml was not specified.  Check that the" +
                " property exists in TR.props and was loaded.";
            getCategory().error(pathError);
            throw new InitializationException(pathError);
        }

        File serialAppData = null;
        File xmlFile = null;
        xmlFile = new File(xmlPath);
        if ( !xmlFile.canRead() ) 
        {
            // If possible, transform paths to be webapp root relative.
            xmlPath = getRealPath(xmlPath);
            xmlFile = new File(xmlPath);
            if ( !xmlFile.canRead() ) 
            {
                String pathError =
                    "Could not read input file.  Even tried relative to"
                    + " webapp root.";
                getCategory().error(pathError);
                throw new InitializationException(pathError);
            }
        }

        serialAppData = new File(appDataPath);
        try
        {
            serialAppData.createNewFile();
            serialAppData.delete();
        }
        catch (Exception e)
        {
            // If possible, transform paths to be webapp root relative.
            appDataPath = getRealPath(appDataPath);
            serialAppData = new File(appDataPath);
            try
            {
                serialAppData.createNewFile();
                serialAppData.delete();
            }
            catch (Exception ee)
            {
                getCategory().info(SERIALIZED_ERROR_MSG);
            }
        }

        try
        {
            if ( serialAppData.exists()
                 && serialAppData.lastModified() > xmlFile.lastModified() )
            {
                InputStream in = null;
                try
                {
                    in = new FileInputStream(serialAppData);
                    ObjectInputStream p = new ObjectInputStream(in);
                    appData = (AppData)p.readObject();
                }
                catch (Exception e)
                {
                    // We got a corrupt file for some reason
                    writeAppData(xmlPath, appDataPath, serialAppData);
                }
                finally
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
            }
            else
            {
                writeAppData(xmlPath, appDataPath, serialAppData);
            }

            groupNames = new String[appData.getGroups().size()];
            groupKeyMap = new HashMap();
            groupNameMap = new HashMap();
            getterMap = new HashMap();
            setterMap = new HashMap();
            // omTool = new OMTool();
            String pkg = appData.getBasePackage();

            int maxPooledGroups = 0;
            List glist = appData.getGroups();
            for ( int i=glist.size()-1; i>=0; i-- )
            {
                XmlGroup g = (XmlGroup)glist.get(i);
                String groupName = g.getName();
                groupNames[i] = groupName;
                groupKeyMap.put(groupName, g.getKey());
                groupNameMap.put(g.getKey(), groupName);
                maxPooledGroups = 
                    Math.max(maxPooledGroups, 
                             Integer.parseInt(g.getPoolCapacity()));
                List classNames = g.getMapToObjects();
                Iterator iter2 = classNames.iterator();
                while (iter2.hasNext())
                {
                    String className = (String)iter2.next();
                    if ( !getterMap.containsKey(className) )
                    {
                        getterMap.put(className, new HashMap());
                        setterMap.put(className, new HashMap());
                    }
                }
            }

            KeyedPoolableObjectFactory factory = 
                new Group.GroupFactory(appData);
            keyedPool = new StackKeyedObjectPool(factory, maxPooledGroups);

            setInit(true);
        }
        catch (Exception e)
        {
            throw new InitializationException(
                "TurbineIntakeService failed to initialize", e);
        }
    }

    /**
     * This method writes the appData file into Objects and stores
     * the information into this classes appData property
     */
    private void writeAppData(String xmlPath, String appDataPath, File serialAppData)
        throws Exception
    {
        XmlToAppData xmlApp = new XmlToAppData();
        appData = xmlApp.parseFile(xmlPath);
        OutputStream out = null;
        InputStream in = null;
        try
        {
            // write the appData file out
            out = new FileOutputStream(serialAppData);
            ObjectOutputStream p = new ObjectOutputStream(out);
            p.writeObject(appData);
            p.flush();

            // read the file back in. for some reason on OSX 10.1
            // this is necessary.
            in = new FileInputStream(serialAppData);
            ObjectInputStream pin = new ObjectInputStream(in);
            appData = (AppData)pin.readObject();
        }
        catch (Exception e)
        {
            getCategory().info(
                "Intake initialization could not be serialized " +
                "because writing to " + appDataPath + " was not " +
                "allowed.  This will require that the xml file be " +
                "parsed when restarting the application.");
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
            if (in != null)
            {
                in.close();
            }
        }
    }

    /**
     * Gets an instance of a named group either from the pool
     * or by calling the Factory Service if the pool is empty.
     *
     * @param groupName the name of the group.
     * @return a Group instance.
     * @throws ServiceException if recycling fails.
     */
    public Group getGroup(String groupName)
            throws ServiceException
    {
        Group group = null;
        if (groupName == null)
        {
            throw new ServiceException (
                "Intake TurbineIntakeService.getGroup(groupName) is null");
        }
        try
        {
            group = (Group)keyedPool.borrowObject(groupName);
        }
        catch (Exception e)
        {
            new ServiceException(e);
        }
        return group;
    }


    /**
     * Puts a Group back to the pool.
     *
     * @param instance the object instance to recycle.
     * @return true if the instance was accepted.
     */
    public void releaseGroup(Group instance)
    {
        if (instance != null)
        {
            String name = instance.getIntakeGroupName();
            try
            {
                keyedPool.returnObject(name, instance);
            }
            catch (Exception e)
            {
                new ServiceException(e);
            }
            //return true;
        }
        else
        {
            //return false;
        }
    }

    /**
     * Gets the current size of the pool for a group.
     *
     * @param name the name of the group.
     */
    public int getSize(String name)
    {
        return keyedPool.getNumActive(name) + keyedPool.getNumIdle(name);
    }

    /**
     * Names of all the defined groups.
     *
     * @return array of names.
     */
    public String[] getGroupNames()
    {
        return groupNames;
    }

    /**
     * Gets the key (usually a short identifier) for a group.
     *
     * @param groupName the name of the group.
     * @return the the key.
     */
    public String getGroupKey(String groupName)
    {
        return (String)groupKeyMap.get(groupName);
    }

    /**
     * Gets the group name given its key.
     *
     * @param the the key.
     * @return groupName the name of the group.
     */
    public String getGroupName(String groupKey)
    {
        return (String)groupNameMap.get(groupKey);
    }

    /**
     * Gets the Method that can be used to set a property.
     *
     * @param className the name of the object.
     * @param propName the name of the property.
     * @return the setter.
     */
    public Method getFieldSetter(String className, String propName)
    {
        Map settersForClassName = (Map)setterMap.get(className);
        Method setter = (Method)settersForClassName.get(propName);

        if ( setter == null )
        {
            PropertyDescriptor pd = null; 
            synchronized(setterMap)
            {
                try
                {
                    // !FIXME! will throw an exception if the getter is not
                    // available.  Need to make this more robust
                    pd = new PropertyDescriptor(propName, 
                                                Class.forName(className));
                    setter = pd.getWriteMethod();
                    ((Map)setterMap.get(className)).put(propName, setter);
                    if ( setter == null ) 
                    {
                        getCategory().error("Intake: setter for '" + propName
                                            + "' in class '" + className
                                            + "' could not be found.");
                    }
                }
                catch (Exception e)
                {
                    getCategory().error(e);
                }
            }
            // we have already completed the reflection on the getter, so
            // save it so we do not have to repeat
            synchronized(getterMap)
            {
                try
                {
                    Method getter = pd.getReadMethod();
                    ((Map)getterMap.get(className)).put(propName, getter);
                }
                catch (Exception e)
                {
                    // ignore, the getter may not be needed
                }
            }
        }
        return setter;
    }

    /**
     * Gets the Method that can be used to get a property value.
     *
     * @param className the name of the object.
     * @param propName the name of the property.
     * @return the getter.
     */
    public Method getFieldGetter(String className, String propName)
    {
        Map gettersForClassName = (Map)getterMap.get(className);
        Method getter = (Method)gettersForClassName.get(propName);

        if ( getter == null )
        {
            PropertyDescriptor pd = null;
            synchronized(getterMap)
            {
                try
                {
                    // !FIXME! will throw an exception if the setter is not
                    // available.  Need to make this more robust
                    pd = new PropertyDescriptor(propName, 
                                                Class.forName(className));
                    getter = pd.getReadMethod();
                    ((Map)getterMap.get(className)).put(propName, getter);
                    if ( getter == null ) 
                    {
                        getCategory().error("Intake: getter for '" + propName
                                            + "' in class '" + className
                                            + "' could not be found.");
                    }
                }
                catch (Exception e)
                {
                    getCategory().error(e);
                }
            }
            // we have already completed the reflection on the setter, so
            // save it so we do not have to repeat
            synchronized(setterMap)
            {
                try
                {
                    Method setter = pd.getWriteMethod();
                    ((Map)setterMap.get(className)).put(propName, setter);
                }
                catch (Exception e)
                {
                    // ignore, the setter may not be needed
                }
            }
        }
        return getter;
    }

}
