/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;
import java.io.PrintWriter;
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
import javax.sql.DataSource;
import org.jboss.pool.jdbc.JDBCPoolDataSource;
import org.jboss.logging.LogWriter;
import org.jboss.util.ServiceMBeanSupport;
import org.jboss.logging.Logger;

/**
 * Service that loads a JDBC 1 connection pool.  The constructors are called by
 * the JMX engine based on your MLET tags.
 * @version $Revision: 1.12 $
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 */
public class JDBCDataSourceLoader extends ServiceMBeanSupport implements JDBCDataSourceLoaderMBean {
    private JDBCPoolDataSource source;

    public JDBCDataSourceLoader() {
	source = new JDBCPoolDataSource();
    }
    public JDBCDataSourceLoader(String poolName) {
        source = new JDBCPoolDataSource();
        source.setPoolName(poolName);
    }

    public void setPoolName(String name)
    {
	source.setPoolName(name);
    }

    public String getPoolName()
    {
	return source.getPoolName();
    }

    public void setURL(String jdbcURL) {
        source.setJDBCURL(jdbcURL);
    }

    public String getURL() {
        return source.getJDBCURL();
    }

    public void setProperties(String properties) {
        Properties props = parseProperties(properties);
        source.setJDBCProperties(props);
    }

    public String getProperties() {
	Properties props = source.getJDBCProperties();
        return (props==null) ? null : buildProperties(props);
    }

    public void setJDBCUser(String userName) {
        if(userName != null && userName.length() > 0)
            source.setJDBCUser(userName);
    }

    public String getJDBCUser() {
        return source.getJDBCUser();
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
        } catch(Exception e) {
            System.out.println("Unable to set logger for Minerva JDBC Pool!");
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
        return new ObjectName(OBJECT_NAME+",name="+source.getJNDIName());
    }

    public String getName() {
        return "JDBCDataSource";
    }

    public void startService() throws Exception {
        initializePool();
    }

    public void stopService() {
        // Unbind from JNDI
        try {
            String name = source.getPoolName();
            new InitialContext().unbind("java:/"+name);
            log.log("JDBC Connection pool "+name+" removed from JNDI");
            source.close();
            log.log("JDBC Connection pool "+name+" shut down");
        } catch (NamingException e) {
            // Ignore
        }
    }

    // Private -------------------------------------------------------

    private void initializePool() throws NamingException, SQLException {
        source.initialize();

        // Bind in JNDI
        bind(new InitialContext(), "java:/" + source.getPoolName(), source);

        log.log("JDBC Connection pool "+source.getPoolName()+" bound to java:/"+source.getPoolName());

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
