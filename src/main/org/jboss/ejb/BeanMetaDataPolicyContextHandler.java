package org.jboss.ejb;

import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;

/** A PolicyContextHandler for the active EnterpriseBean metadata.
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class BeanMetaDataPolicyContextHandler implements PolicyContextHandler
{
   public static final String METADATA_CONTEXT_KEY = "org.jboss.ejb.BeanMetaData";
   private static ThreadLocal metaDataContext = new ThreadLocal();

   static void setMetaData(Object metadata)
   {
      metaDataContext.set(metadata);
   }

   /** Access the current EJB context metadata.
    * @param key - "org.jboss.ejb.BeanMetaData"
    * @param data currently unused
    * @return The active org.jboss.metadata.BeanMetaData subclass
    * @throws javax.security.jacc.PolicyContextException
    */ 
   public Object getContext(String key, Object data)
      throws PolicyContextException
   {
      Object context = null;
      if( key.equalsIgnoreCase(METADATA_CONTEXT_KEY) == true )
         context = metaDataContext.get();
      return context;
   }

   public String[] getKeys()
      throws PolicyContextException
   {
      String[] keys = {METADATA_CONTEXT_KEY};
      return keys;
   }

   public boolean supports(String key)
      throws PolicyContextException
   {
      return key.equalsIgnoreCase(METADATA_CONTEXT_KEY);
   }
}
