/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import org.jboss.util.ServiceMBean;

public interface JDBCDataSourceLoaderMBean extends ServiceMBean {
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=JDBCDataSource";

   // Public --------------------------------------------------------
    public void setPoolName(String name);
    public String getPoolName();
    public void setURL(String url);
    public String getURL();
    public void setJDBCUser(String userName);
    public String getJDBCUser();
    public void setPassword(String password);
    public String getPassword();
    public void setProperties(String properties);
    public String getProperties();
    public void setLoggingEnabled(boolean enabled);
    public boolean isLoggingEnabled();
    public void setMinSize(int minSize);
    public int getMinSize();
    public void setMaxSize(int maxSize);
    public int getMaxSize();
    public void setBlocking(boolean blocking);
    public boolean isBlocking();
    public void setGCEnabled(boolean gcEnabled);
    public boolean isGCEnabled();
    public void setGCInterval(long interval);
    public long getGCInterval();
    public void setGCMinIdleTime(long idleMillis);
    public long getGCMinIdleTime();
    public void setIdleTimeoutEnabled(boolean enabled);
    public boolean isIdleTimeoutEnabled();
    public void setIdleTimeout(long idleMillis);
    public long getIdleTimeout();
    public void setMaxIdleTimeoutPercent(float percent);
    public float getMaxIdleTimeoutPercent();
    public void setInvalidateOnError(boolean invalidate);
    public boolean isInvalidateOnError();
    public void setTimestampUsed(boolean timestamp);
    public boolean isTimestampUsed();
}
