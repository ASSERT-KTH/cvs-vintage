/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import java.lang.reflect.*;
import java.util.*;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.naming.*;
import javax.sql.DataSource;
import org.jboss.logging.LogWriter;
import org.jboss.minerva.datasource.JDBCPoolDataSource;
import org.jboss.util.*;
import org.jboss.logging.Logger;


/**
 * Service that loads a JDBC 1 connection pool.  The constructors are called by
 * the JMX engine based on your MLET tags.
 * @version $Revision: 1.2 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class JDBCDataSourceLoader extends ServiceMBeanSupport implements JDBCDataSourceLoaderMBean {
    private JDBCPoolDataSource source;

    public JDBCDataSourceLoader() {
    }
    public JDBCDataSourceLoader(String poolName, String url, String username, String password, Integer minSize, Integer maxSize) {
        this(poolName, url, username, password, minSize, maxSize, "");
    }
    public JDBCDataSourceLoader(String poolName, String url, String username, String password, Integer minSize, Integer maxSize, String poolParameters) {
        source = new JDBCPoolDataSource();
        source.setPoolName(poolName);
        source.setJDBCURL(url);
        source.setJDBCUser(username);
        source.setJDBCPassword(password);
        source.setMinSize(minSize.intValue());
        source.setMaxSize(maxSize.intValue());
        setAdditionalProperties(parseProperties(poolParameters));
        try {
            source.setLogWriter(new LogWriter(log));
        }catch(java.sql.SQLException e) {
            Logger.exception(e);
        }
        source.initialize();
    }

    public ObjectName getObjectName(MBeanServer parm1, ObjectName parm2) throws javax.management.MalformedObjectNameException {
        return new ObjectName(OBJECT_NAME+",name="+source.getJNDIName());
    }

    public String getName() {
        return "JDBCDataSource";
    }

    public void startService() throws Exception {
        // Bind in JNDI
        bind(new InitialContext(), "jdbc."+source.getPoolName(), source);

        log.log("JDBC Connection pool "+source.getPoolName()+" bound to jdbc."+source.getPoolName());

        // Test database
        source.getConnection().close();
    }

    public void stopService() {
        // Unbind from JNDI
        try {
            new InitialContext().unbind("jdbc."+source.getPoolName());
            log.log("JDBC Connection pool "+source.getPoolName()+" removed from JNDI");
        } catch (NamingException e) {
            // Ignore
        }
    }

	// Private -------------------------------------------------------

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

    private void setAdditionalProperties(Properties props) {
        Iterator it = props.keySet().iterator();
        while(it.hasNext()) {
            String property = (String)it.next();
            String value = props.getProperty(property);
            try {
                Class cls = source.getClass();
                Method list[] = cls.getMethods();
                Method setter = null;
                for(int i=0; i<list.length; i++)
                    if(list[i].getName().equals("set"+property) && list[i].getParameterTypes().length == 1) {
                        setter = list[i];
                        break;
                    }
                if(setter == null) throw new NoSuchMethodException("Unable to find 1-arg setter for property '"+property+"'");
                Class argClass = setter.getParameterTypes()[0];
                if(argClass.isPrimitive())
                    argClass = getPrimitiveClass(argClass);
                Constructor con = argClass.getDeclaredConstructor(new Class[]{String.class});
                Object arg = con.newInstance(new Object[]{value});
                setter.invoke(source, new Object[]{arg});
            } catch(Exception e) {
                log.error("Unable to set pool property '"+property+"' to '"+value+"': "+e);
                Logger.exception(e);
            }
        }
    }

    private static Class getPrimitiveClass(Class source) {
        if(source.equals(Boolean.TYPE)) return Boolean.class;
        if(source.equals(Integer.TYPE)) return Integer.class;
        if(source.equals(Float.TYPE)) return Float.class;
        if(source.equals(Long.TYPE)) return Long.class;
        if(source.equals(Double.TYPE)) return Double.class;
        if(source.equals(Character.TYPE)) return Character.class;
        if(source.equals(Short.TYPE)) return Short.class;
        if(source.equals(Byte.TYPE)) return Byte.class;
        return null;
    }
}