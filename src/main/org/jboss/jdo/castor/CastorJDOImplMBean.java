/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jdo.castor;
    
/**
 *   Castor JDO support
 *      
 *   @author Oleg Nitz (on@ibis.odessa.ua)
 *   @version $Revision: 1.2 $
 */
public interface CastorJDOImplMBean
       extends org.jboss.util.ServiceMBean
{
    public static final String OBJECT_NAME = ":service=CastorJDO";
    
    public void setJndiName(String jndiName);

    public String getJndiName();

    public void setConfiguration(String dbConf);

    public String getConfiguration();

    public void setLockTimeout(int lockTimeout);

    public int getLockTimeout();

    public void setLoggingEnabled(boolean loggingEnabled);

    public boolean getLoggingEnabled();

    public void setCommonClassPath(boolean commonClassPath);

    public boolean getCommonClassPath();

    public void setAutoStore(boolean autoStore);

    public boolean isAutoStore();

}
