/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jdbc;

import org.jboss.util.ServiceMBean;

public interface XADataSourceLoaderMBean extends ServiceMBean {
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=XADataSource";

   // Public --------------------------------------------------------
   public void setPoolName(String name);
   public String getPoolName();
   public void setDataSourceClass(String clazz);
   public String getDataSourceClass();
   public void setURL(String url);
   public String getURL();
   public void setJDBCUser(String userName);
   public String getJDBCUser();
   public void setPassword(String password);
   public String getPassword();
   public void setProperties(String properties);
   public String getProperties();
   public void setLoggingEnabled(boolean enabled);
   public boolean getLoggingEnabled();
   public void setMinSize(int minSize);
   public int getMinSize();
   public void setMaxSize(int maxSize);
   public int getMaxSize();
   public void setBlocking(boolean blocking);
   public boolean getBlocking();
   public void setBlockingTimeout(int blockingTimeout);
   public int getBlockingTimeout();
   public void setGCEnabled(boolean gcEnabled);
   public boolean getGCEnabled();
   public void setGCInterval(long interval);
   public long getGCInterval();
   public void setGCMinIdleTime(long idleMillis);
   public long getGCMinIdleTime();
   public void setIdleTimeoutEnabled(boolean enabled);
   public boolean getIdleTimeoutEnabled();
   public void setIdleTimeout(long idleMillis);
   public long getIdleTimeout();
   public void setMaxIdleTimeoutPercent(float percent);
   public float getMaxIdleTimeoutPercent();
   public void setInvalidateOnError(boolean invalidate);
   public boolean getInvalidateOnError();
   public void setTimestampUsed(boolean timestamp);
   public boolean getTimestampUsed();
   public String getTransactionIsolation();
   public void setTransactionIsolation(String iso);
   public int getPSCacheSize();
   public void setPSCacheSize(int size);
    
}
