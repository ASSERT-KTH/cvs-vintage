package org.jboss.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

/** Encapsulation of the web.xml security-constraints
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class WebSecurityMetaData
{
   private static HashSet ALL_HTTP_METHODS = new HashSet();

   static
   {
      ALL_HTTP_METHODS.add("GET");
      ALL_HTTP_METHODS.add("POST");
      ALL_HTTP_METHODS.add("PUT");
      ALL_HTTP_METHODS.add("DELETE");
      ALL_HTTP_METHODS.add("HEAD");
      ALL_HTTP_METHODS.add("OPTIONS");
      ALL_HTTP_METHODS.add("TRACE");
   }

   /** The HashMap<String, WebResourceCollection> for the
    * security-constraint/web-resource-collection elements
    */ 
   private HashMap webResources = new HashMap();
   /** Set<String> of the allowed role names defined by the
    * security-constraint/auth-constraint elements
    */
   private Set roles = new HashSet();

   /** The optional security-constraint/user-data-constraint/transport-guarantee */
   private String transportGuarantee;
   /** The unchecked flag is set when there is no security-constraint/auth-constraint
    */
   private boolean unchecked = false;
   /** The excluded flag is set when there is an empty
    security-constraint/auth-constraint element
    */
   private boolean excluded = false;

   public static String[] getMissingHttpMethods(HashSet httpMethods)
   {
      String[] methods = {};
      if( httpMethods.size() > 0 && httpMethods.containsAll(ALL_HTTP_METHODS) == false )
      {
         HashSet missingMethods = new HashSet(ALL_HTTP_METHODS);
         missingMethods.removeAll(httpMethods);
         methods = new String[missingMethods.size()];
         missingMethods.toArray(methods);
      }
      return methods;         
   }

   public WebResourceCollection addWebResource(String name)
   {
      WebResourceCollection webrc = new WebResourceCollection(name);
      webResources.put(name, webrc);
      return webrc;
   }
   public HashMap getWebResources()
   {
      return webResources;
   }

   public void addRole(String name)
   {
      roles.add(name);
   }
   /** Get the security-constraint/auth-constraint values. An empty role
    * set must be qualified by the isUnchecked and isExcluded methods.
    * 
    * @return Set<String> for the role names
    */ 
   public Set getRoles()
   {
      return roles;
   }
   
   /** Get the security-constraint/transport-guarantee setting
    @return null == no guarantees
      INTEGRAL == an integretity guarantee
      CONFIDENTIAL == protected for confidentiality
    */ 
   public String getTransportGuarantee()
   {
      return transportGuarantee;
   }
   public void setTransportGuarantee(String transportGuarantee)
   {
      this.transportGuarantee = transportGuarantee;
   }

   public boolean isUnchecked()
   {
      return unchecked;
   }
   public void setUnchecked(boolean flag)
   {
      this.unchecked = flag;
   }

   public boolean isExcluded()
   {
      return excluded;
   }
   public void setExcluded(boolean flag)
   {
      this.excluded = flag;
   }

   /** The security-constraint/web-resource-collection child element container
    * 
    */
   public static class WebResourceCollection
   {
      /** The required web-resource-name element */
      private String name;
      /** The required url-pattern element(s) */
      private HashSet urlPatterns = new HashSet();
      /** The optional http-method element(s) */
      private ArrayList httpMethods = new ArrayList();

      public WebResourceCollection(String name)
      {
         this.name = name;
      }

      public String getName()
      {
         return name;
      }
      public void addPattern(String pattern)
      {
         urlPatterns.add(pattern);
      }
      /** Get the url-patterns specified in the resource collection. 
       * @return
       */ 
      public String[] getUrlPatterns()
      {
         String[] patterns = {};
         patterns = new String[urlPatterns.size()];
         urlPatterns.toArray(patterns);
         return patterns;
      }

      public void addHttpMethod(String method)
      {
         httpMethods.add(method);
      }
      /** The optional security-constraint/web-resource-collection/http-method
       @return empty for all methods, a subset of GET, POST, PUT, DELETE,
               HEAD, OPTIONS, TRACE otherwise
       */ 
      public String[] getHttpMethods()
      {
         String[] methods = {};
         if( httpMethods.containsAll(ALL_HTTP_METHODS) == false )
         {
            methods = new String[httpMethods.size()];
            httpMethods.toArray(methods);
         }
         return methods;
      }
      /** Return the http methods that were not specified in the collection.
       If there were a subset of the ALL_HTTP_METHODS given, then this
       method returns the ALL_HTTP_METHODS - the subset. If no or all
       ALL_HTTP_METHODS were specified this return an empty array.
       @return empty for all methods, a subset of GET, POST, PUT, DELETE,
               HEAD, OPTIONS, TRACE otherwise
       */ 
      public String[] getMissingHttpMethods()
      {
         String[] methods = {};
         if( httpMethods.size() > 0 && httpMethods.containsAll(ALL_HTTP_METHODS) == false )
         {
            HashSet missingMethods = new HashSet(ALL_HTTP_METHODS);
            missingMethods.removeAll(httpMethods);
            methods = new String[missingMethods.size()];
            missingMethods.toArray(methods);
         }
         return methods;         
      }
   }
}
