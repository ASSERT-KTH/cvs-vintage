/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import org.jboss.util.ServiceMBean;

public interface XADataSourceLoaderMBean extends ServiceMBean {
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=XADataSource";

   // Public --------------------------------------------------------
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
    public void setShrinkingEnabled(boolean enabled);
    public boolean isShrinkingEnabled();
    public void setShrinkMinIdleTime(long idleMillis);
    public long getShrinkMinIdleTime();
    public void setShrinkPercent(float percent);
    public float getShrinkPercent();
    public void setInvalidateOnError(boolean invalidate);
    public boolean isInvalidateOnError();
    public void setTimestampUsed(boolean timestamp);
    public boolean isTimestampUsed();
}
