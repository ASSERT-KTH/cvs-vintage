/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jdo.castor;

import java.io.PrintWriter;
import java.io.Serializable;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;

import javax.management.*;
import javax.naming.spi.ObjectFactory;
import javax.naming.Referenceable;
import javax.naming.Reference;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.DataObjects;
import org.exolab.castor.jdo.JDO;
import org.exolab.castor.jdo.DatabaseNotFoundException;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.persist.spi.LogInterceptor;
import org.exolab.castor.xml.Unmarshaller;

import org.jboss.logging.Log;
import org.jboss.logging.Logger;
import org.jboss.logging.LogWriter;
import org.jboss.util.ServiceMBeanSupport;

import org.jboss.proxy.Proxy;
import org.jboss.proxy.Proxies;
import org.jboss.proxy.InvocationHandler;


/**
 *   Castor JDO support
 *
 *   @author Oleg Nitz (on@ibis.odessa.ua)
 *   @version $Revision: 1.3 $
 */
public class CastorJDOImpl extends ServiceMBeanSupport 
        implements DataObjects, ObjectFactory, Referenceable, Serializable,
                   CastorJDOImplMBean, MBeanRegistration, LogInterceptor {
   
    private String _jndiName;

    private String _dbConf;

    private JDO _jdo = new JDO();

    private String _dataSourceName;

    private static HashMap _instances = new HashMap();

    private transient PrintWriter writer;

    /**
     * Do JDO classes should be loader by the global class loader
     * (the same class loader as Castor classes)?
     */
    private boolean _commonClassPath;

    /*
     * True if user prefer all reachable object to be stored automatically.
     * False (default) if user want only dependent object to be stored.
     */
    private boolean _autoStore = false;

    public CastorJDOImpl() {
    }

    public ObjectName getObjectName(MBeanServer server, ObjectName name)
            throws javax.management.MalformedObjectNameException {
        return new ObjectName(OBJECT_NAME+",name="+_jndiName);
    }
   
    public String getName() {
        return "CastorJDO";
    }

    public void startService() throws Exception {
        org.exolab.castor.jdo.conf.Database database;
        Unmarshaller unm;
        int pos;
        Method m;

        // Bind in JNDI
        bind(new InitialContext(), "java:/" + _jndiName, this);

        _jdo.setTransactionManager("java:/TransactionManager");
        _jdo.setConfiguration(_dbConf);
        unm = new Unmarshaller(org.exolab.castor.jdo.conf.Database.class);
        database = (org.exolab.castor.jdo.conf.Database) unm.unmarshal(new InputSource(_dbConf));
        _jdo.setDatabaseName(database.getName());
        if (database.getJndi() != null) {
            _dataSourceName = database.getJndi().getName();
        }
        // Castor versions older than 0.9.4 doesn't have this method,
        // we'll use reflection for backward compatibility
        //_jdo.setAutoStore(_autoStore);
        try {
            m = _jdo.getClass().getMethod("setAutoStore", new Class[] {Boolean.class});
            m.invoke(_jdo, new Object[] {new Boolean(_autoStore)});
        } catch (Exception ex) {
        }
        _instances.put(_jndiName, this);
        log.debug("DataObjects factory for " + _dataSourceName + " bound to " + _jndiName);
    }

    public void stopService() {
        // Unbind from JNDI
        try {
            new InitialContext().unbind(_jndiName);
        } catch (NamingException e) {
        }
    }


    // CastorJDOImplMBean implementation ---------------------------

    public void setJndiName(String jndiName) {
        _jndiName = jndiName;
    }

    public String getJndiName() {
        return _jndiName;
    }

    public void setConfiguration(String dbConf) {
        _dbConf = dbConf;
    }

    public String getConfiguration() {
        return _dbConf;
    }

    public void setLockTimeout(int lockTimeout) {
        _jdo.setLockTimeout(lockTimeout);
    }

    public int getLockTimeout() {
        return _jdo.getLockTimeout();
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        _jdo.setLogInterceptor(loggingEnabled ? this : null);
    }

    public boolean getLoggingEnabled() {
        return (_jdo.getLogInterceptor() != null);
    }
   
    public void setCommonClassPath(boolean commonClassPath) {
        _commonClassPath = commonClassPath;
    }

    public boolean getCommonClassPath() {
        return _commonClassPath;
    }

    /*
     * True if user prefer all reachable object to be stored automatically.
     * False if user want only dependent object to be stored.
     */
    public void setAutoStore( boolean autoStore ) {
        _autoStore = autoStore;
    }

    /*
     * Return if the next Database instance will be set to autoStore.
     */
    public boolean isAutoStore() {
        return _autoStore;
    }

    // DataObjects implementation ----------------------------------
    public Database getDatabase()
            throws DatabaseNotFoundException, PersistenceException {
        Method m;

        if (_commonClassPath) {
            _jdo.setClassLoader(null);
        } else {
            _jdo.setClassLoader(Thread.currentThread().getContextClassLoader());
        }
        return _jdo.getDatabase();
    }


    public void setDescription(String description) {
        _jdo.setDescription(description);
    }


    public String getDescription() {
        return _jdo.getDescription();
    }

    // Referenceable implementation ----------------------------------
    public Reference getReference() {
        return new Reference(getClass().getName(), getClass().getName(), null);
    }

    // ObjectFactory implementation ----------------------------------
    public Object getObjectInstance(Object obj,
                                    Name name,
                                    Context nameCtx,
                                    Hashtable environment)
            throws Exception {
        return _instances.get(name.toString());
    }
   
        // Private -------------------------------------------------------
    private void bind(Context ctx, String name, Object val)
            throws NamingException {
        // Bind val to name in ctx, and make sure that all intermediate contexts exist
      
        Name n = ctx.getNameParser("").parse(name);
        while (n.size() > 1)
        {
            String ctxName = n.get(0);
            try
            {
                ctx = (Context)ctx.lookup(ctxName);
            } catch (NameNotFoundException e)
            {
                ctx = ctx.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }

        ctx.bind(n.get(0), val);
    }

    // LogInterceptor implementation for Castor 0.8 ----------------------
    public void loading(Class objClass, Object identity) {
        log.debug( "Loading " + objClass.getName() + " (" + identity + ")" );
    }


    public void creating(Class objClass, Object identity) {
        log.debug( "Creating " + objClass.getName() + " (" + identity + ")" );
    }


    public void removing(Class objClass, Object identity) {
        log.debug( "Removing " + objClass.getName() + " (" + identity + ")" );
    }


    public void storing(Class objClass, Object identity) {
        log.debug( "Storing " + objClass.getName() + " (" + identity + ")" );
    }


    // LogInterceptor implementation for Castor 0.9 ----------------------
    public void loading(Object objClass, Object identity) {
        log.debug( "Loading " + objClass + " (" + identity + ")" );
    }


    public void creating(Object objClass, Object identity) {
        log.debug( "Creating " + objClass + " (" + identity + ")" );
    }


    public void removing(Object objClass, Object identity) {
        log.debug( "Removing " + objClass + " (" + identity + ")" );
    }


    public void storing(Object objClass, Object identity) {
        log.debug( "Storing " + objClass + " (" + identity + ")" );
    }


    // LogInterceptor implementation - the rest part --------------------

    public void storeStatement(String statement) {
        log.debug(statement);
    }


    public void queryStatement(String statement) {
        log.debug(statement);
    }


    public void message(String message) {
        log.debug(message);
    }


    public void exception(Exception except) {
        log.exception(except);
    }

    public PrintWriter getPrintWriter() {
        if (writer == null) {
            writer = new LogWriter(log);
        }
        return writer;
    }
}

