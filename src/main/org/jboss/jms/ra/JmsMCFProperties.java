/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jms.ra;

import javax.resource.ResourceException;

/**
 * <p>The MCF default properties, settable in ra.xml or in deployer.
 *
 *
 * Created: Thu Sep 27 10:01:25 2001
 *
 * @author Peter Antman
 * @version $Revision: 1.1 $
 */

public class JmsMCFProperties  {
   final static String QUEUE_TYPE = "javax.jms.Queue";
   final static String TOPIC_TYPE = "javax.jms.Topic";

   String userName = null;
   String password = null;
   String providerJNDI = "java:DefaultJMSProvider";
   boolean isTopic = true;
   
   public JmsMCFProperties() {
      
   }
   
   /**
    * Set userName, null by default.
    */
   public void setUserName(String userName) {
      this.userName = userName;
   }

   /**
    * Get userName, may be null.
    */ 
   public String getUserName() {
      return userName;
   }
   
   /**
    * Set password, null by default.
    */
   public void setPassword(String password) {
      this.password = password;
   }
   /**
    * Get password, may be null.
    */
   public String getPassword() {
      return password;
   }

   /**
    * <p>Set providerJNDI, the JMS provider adapter to use.
    *
    * <p>Defaults to java:DefaultJMSProvider.
    */
   public void setProviderJNDI(String providerJNDI) {
      this.providerJNDI  = providerJNDI;
   }

   /**
    * Get providerJNDI. May not be null.
    */
   public String getProviderJNDI() {
      return providerJNDI;
   }

   /**
    * Type of the JMS Session, defaults to true.
    */
   public boolean isTopic() {
      return isTopic;
   }

   /**
    * Set the default session type.
    */
   public void setIsTopic(boolean isTopic) {
      this.isTopic = isTopic;
   }

   /**
    * Helper method to set the default session type.
    *
    * @param type either javax.jms.Topic or javax.jms.Queue
    * @exception ResourceException if type was not a valid type.
    */
   public void setSessionDefaultType(String type) throws ResourceException {
      if (type.equals(QUEUE_TYPE)) {
	 isTopic = false;
      }else if(type.equals(TOPIC_TYPE)) {
	 isTopic = true;
      } else {
	 throw new  ResourceException(type + " is not a recogniced JMS session type");
      }
      
   }

   public String getSessionDefaultType() {
      return (isTopic ? TOPIC_TYPE : QUEUE_TYPE);
   }

   /**
    * Test for equality of all attributes.
    */
   public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof JmsMCFProperties) {
            JmsMCFProperties you = (JmsMCFProperties) obj;
            return (StringUtil.compare(userName, you.getUserName()) &&
		    StringUtil.compare(password, you.getPassword()) &&
		    StringUtil.compare(providerJNDI, you.getProviderJNDI()) &&
		    this.isTopic == you.isTopic()
		    );
        } else {
            return false;
        }
    }
 
   /**
    * Simple hashCode of all attributes. 
    */
    public int hashCode() {
        String result = "" + userName + password + providerJNDI + isTopic;
        return result.hashCode();
    }
   
} // JmsMCFProperties
