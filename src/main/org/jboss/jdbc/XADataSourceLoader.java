/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.XADataSource;
import org.jboss.logging.LogWriter;
import org.jboss.minerva.datasource.XAPoolDataSource;
import org.jboss.util.ServiceMBeanSupport;
import org.jboss.logging.Logger;

/**
 * Service that loads a JDBC 2 std. extension-compliant connection pool.  This
 * pool generates connections that are registered with the current Transaction
 * and support two-phase commit.  The constructors are called by the JMX engine
 * based on your MLET tags.
 * @version $Revision: 1.11 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XADataSourceLoader extends ServiceMBeanSupport
        implements XADataSourceLoaderMBean {
    private XAPoolDataSource source;
    private String url;

    public XADataSourceLoader() {}

    public XADataSourceLoader(String poolName, String xaDataSourceClass) {
        source = new XAPoolDataSource();
        source.setPoolName(poolName);
        XADataSource vendorSource = null;
        try {
            Class cls = Class.forName(xaDataSourceClass);
            vendorSource = (XADataSource)cls.newInstance();
        } catch(Exception e) {
            Logger.exception(e);
            throw new RuntimeException("Unable to initialize XA database pool '"+poolName+"': "+e);
        }
        source.setDataSource(vendorSource);
    }

    public void setURL(String url) {
        this.url = url == null ? "" : url;  // Save URL, so it doesn't disappear from the JCML file
        XADataSource vendorSource = (XADataSource)source.getDataSource();
        try {
            Class cls = vendorSource.getClass();
            if(url != null && url.length() > 0) {
                Method setURL = cls.getMethod("setURL", new Class[]{String.class});
                setURL.invoke(vendorSource, new Object[]{url});
            }
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to set url to '"+url+"' for pool "+source.getPoolName()+": "+e);
        }
    }

    public String getURL() {
        String result = "";
        XADataSource vendorSource = (XADataSource)source.getDataSource();
        try {
            Class cls = vendorSource.getClass();
            Method getURL = cls.getMethod("getURL", new Class[0]);
            result =  (String) getURL.invoke(vendorSource, new Object[0]);
        } catch(Exception e) {
            log.error("There seems to be a problem with the JDBC URL: "+e);
        }
        if(result == null || result.length() == 0)
            result = url;
        return result;
    }

    public void setProperties(String properties) {
        XADataSource vendorSource = (XADataSource)source.getDataSource();
        try {
            Class cls = vendorSource.getClass();
            if(properties != null && properties.length() > 0) {
                Properties props = parseProperties(properties);
                Method setProperties = cls.getMethod("setProperties", new Class[]{Properties.class});
                setProperties.invoke(vendorSource, new Object[]{props});
            }
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to set proprties to '"+properties+"' for pool "+source.getPoolName()+": "+e);
        }
    }

    public String getProperties() {
        XADataSource vendorSource = (XADataSource)source.getDataSource();
        try {
            Class cls = vendorSource.getClass();
            Method getProperties = cls.getMethod("getProperties", new Class[0]);
            Properties result = (Properties) getProperties.invoke(vendorSource, new Object[0]);
            if(result == null)
                return "";
            else
                return buildProperties(result);
        } catch(Exception e) {
            return "";
        }
    }

    public void setJDBCUser(String userName) {
        if(userName != null && userName.length() > 0)
            source.setJDBCUser(userName);
    }

    public String getJDBCUser() {
        String user = source.getJDBCUser();
        return user;
    }

    public void setPassword(String password) {
        if(password != null && password.length() > 0)
            source.setJDBCPassword(password);
    }

    public String getPassword() {
        return source.getJDBCPassword();
    }

    public void setLoggingEnabled(boolean enabled) {
        PrintWriter writer = enabled ? new LogWriter(log) : null;
        try {
            source.setLogWriter(writer);
            source.getDataSource().setLogWriter(writer);
        } catch(Exception e) {
            System.out.println("Unable to set logger for Minerva XA Pool!");
        }
    }

    public boolean isLoggingEnabled() {
        try {
            return source.getLogWriter() != null;
        } catch(Exception e) {
            return false;
        }
    }

    public void setMinSize(int minSize) {
        source.setMinSize(minSize);
    }

    public int getMinSize() {
        return source.getMinSize();
    }

    public void setMaxSize(int maxSize) {
        source.setMaxSize(maxSize);
    }

    public int getMaxSize() {
        return source.getMaxSize();
    }

    public void setBlocking(boolean blocking) {
        source.setBlocking(blocking);
    }

    public boolean isBlocking() {
        return source.isBlocking();
    }

    public void setGCEnabled(boolean gcEnabled) {
        source.setGCEnabled(gcEnabled);
    }

    public boolean isGCEnabled() {
        return source.isGCEnabled();
    }

    public void setGCInterval(long interval) {
        source.setGCInterval(interval);
    }

    public long getGCInterval() {
        return source.getGCInterval();
    }

    public void setGCMinIdleTime(long idleMillis) {
        source.setGCMinIdleTime(idleMillis);
    }

    public long getGCMinIdleTime() {
        return source.getGCMinIdleTime();
    }

    public void setIdleTimeoutEnabled(boolean enabled) {
        source.setIdleTimeoutEnabled(enabled);
    }

    public boolean isIdleTimeoutEnabled() {
        return source.isIdleTimeoutEnabled();
    }

    public void setIdleTimeout(long idleMillis) {
        source.setIdleTimeout(idleMillis);
    }

    public long getIdleTimeout() {
        return source.getIdleTimeout();
    }

    public void setMaxIdleTimeoutPercent(float percent) {
        source.setMaxIdleTimeoutPercent(percent);
    }

    public float getMaxIdleTimeoutPercent() {
        return source.getMaxIdleTimeoutPercent();
    }

    public void setInvalidateOnError(boolean invalidate) {
        source.setInvalidateOnError(invalidate);
    }

    public boolean isInvalidateOnError() {
        return source.isInvalidateOnError();
    }

    public void setTimestampUsed(boolean timestamp) {
        source.setTimestampUsed(timestamp);
    }

    public boolean isTimestampUsed() {
        return source.isTimestampUsed();
    }

    public ObjectName getObjectName(MBeanServer parm1, ObjectName parm2) throws javax.management.MalformedObjectNameException {
        return new ObjectName(OBJECT_NAME+",name="+source.getPoolName());
    }

    public String getName() {
        return "XADataSource";
    }

    public void startService() throws Exception {
        initializePool();
    }

    public void stopService() {
        // Unbind from JNDI
        try {
            String name = source.getPoolName();
            new InitialContext().unbind(name);
            log.log("XA Connection pool "+name+" removed from JNDI");
            source.close();
            log.log("XA Connection pool "+name+" shut down");
        } catch (NamingException e) {
            // Ignore
        }
    }

	// Private -------------------------------------------------------

    private void initializePool() throws NamingException, SQLException {
        Context ctx = null;
        Object mgr = null;
        source.setTransactionManagerJNDIName("TransactionManager");
        try {
            ctx = new InitialContext();
            mgr = ctx.lookup("TransactionManager");
        } catch(NamingException e) {
            throw new IllegalStateException("Cannot start XA Connection Pool; there is no TransactionManager in JNDI!");
        }
        source.initialize();

        // Bind in JNDI
        bind(new InitialContext(), source.getPoolName(), source);

        log.log("XA Connection pool "+source.getPoolName()+" bound to "+source.getPoolName());

        // Test database
        source.getConnection().close();
    }

    private void bind(Context ctx, String name, Object val) throws NamingException {
        // Bind val to name in ctx, and make sure that all intermediate contexts exist
        Name n = ctx.getNameParser("").parse(name);
        while (n.size() > 1) {
            String ctxName = n.get(0);
            try {
                ctx = (Context)ctx.lookup(ctxName);
            } catch (NameNotFoundException e) {
                ctx = ctx.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }

        ctx.bind(n.get(0), val);
    }

    private static Properties parseProperties(String string) {
        Properties props = new Properties();
        if(string == null || string.length() == 0) return props;
        int lastPos = -1;
        int pos = string.indexOf(";");
        while(pos > -1) {
            addProperty(props, string.substring(lastPos+1, pos));
            lastPos = pos;
            pos = string.indexOf(";", lastPos+1);
        }
        addProperty(props, string.substring(lastPos+1));
        return props;
    }

    private static void addProperty(Properties props, String property) {
        int pos = property.indexOf("=");
        if(pos < 0) {
            System.err.println("Unable to parse property '"+property+"' - please use 'name=value'");
            return;
        }
        props.setProperty(property.substring(0, pos), property.substring(pos+1));
    }

    private static String buildProperties(Properties props) {
        StringBuffer buf = new StringBuffer();
        Iterator it = props.keySet().iterator();
        Object key;
        while(it.hasNext()) {
            key = it.next();
            if(buf.length() > 0)
                buf.append(';');
            buf.append(key).append('=').append(props.get(key));
        }
        return buf.toString();
    }
}
