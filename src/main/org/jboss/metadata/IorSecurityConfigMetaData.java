/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.jboss.deployment.DeploymentException;

/**
 * Describes the security configuration information for the IOR.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version <tt>$Revision: 1.7 $</tt>
 */
public class IorSecurityConfigMetaData
   implements Serializable
{
   /**
    * The root element for security between the end points.
    * Optional element.
    */
   private TransportConfig transportConfig;

   /**
    * as-context (CSIv2 authentication service) is the element describing the authentication
    * mechanism that will be used to authenticate the client. If specified it will be the
    * username-password mechanism.
    * Optional element.
    */
   private AsContext asContext;

   /**
    * sas-context (related to CSIv2 security attribute service) element describes the sas-context fields.
    */
   private SasContext sasContext;

   /** Create a default security configuration.
    * TransportConfig[integrity=supported, confidentiality=supported,
    * establish-trust-in-target=supported,establish-trust-in-client=supported,
    * detect-misordering=supported, detect-replay=supported]
    * AsContext[auth-method=USERNAME_PASSWORD, realm=default, required=false]
    * SasContext[caller-propagation=NONE]
    */
   public IorSecurityConfigMetaData()
   {
      transportConfig = new TransportConfig();
      asContext = new AsContext();
      sasContext = new SasContext();
   }

   /**
    * @param element ior-security-config element.
    */
   public IorSecurityConfigMetaData(Element element) throws DeploymentException
   {
      Element child = MetaData.getOptionalChild(element, "transport-config");
      if(child != null)
      {
         transportConfig = new TransportConfig(child);
      }

      child = MetaData.getOptionalChild(element, "as-context");
      if(child != null)
      {
         asContext = new AsContext(child);
      }

      child = MetaData.getOptionalChild(element, "sas-context");
      if(child != null)
      {
         sasContext = new SasContext(child);
      }
   }

   public TransportConfig getTransportConfig()
   {
      return transportConfig;
   }
   public void setTransportConfig(TransportConfig config)
   {
      this.transportConfig = config;
   }

   public AsContext getAsContext()
   {
      return asContext;
   }
   public void setAsContext(AsContext context)
   {
      this.asContext = context;
   }

   public SasContext getSasContext()
   {
      return sasContext;
   }
   public void setSasContext(SasContext context)
   {
      this.sasContext = context;
   }

   public String toString()
   {
      return
         "[transport-config=" + transportConfig +
         ", as-context=" + asContext +
         ", sas-context=" + sasContext + "]";
   }

   // Inner

   /**
    * The root element for security between the end points
    */
   public class TransportConfig
   {
      public static final String INTEGRITY_NONE = "NONE";
      public static final String INTEGRITY_SUPPORTED = "SUPPORTED";
      public static final String INTEGRITY_REQUIRED = "REQUIRED";

      public static final String CONFIDENTIALITY_NONE = "NONE";
      public static final String CONFIDENTIALITY_SUPPORTED = "SUPPORTED";
      public static final String CONFIDENTIALITY_REQUIRED = "REQUIRED";

      public static final String DETECT_MISORDERING_NONE = "NONE";
      public static final String DETECT_MISORDERING_SUPPORTED = "SUPPORTED";
      public static final String DETECT_MISORDERING_REQUIRED = "REQUIRED";

      public static final String DETECT_REPLAY_NONE = "NONE";
      public static final String DETECT_REPLAY_SUPPORTED = "SUPPORTED";
      public static final String DETECT_REPLAY_REQUIRED = "REQUIRED";

      public static final String ESTABLISH_TRUST_IN_TARGET_NONE = "NONE";
      public static final String ESTABLISH_TRUST_IN_TARGET_SUPPORTED = "SUPPORTED";

      public static final String ESTABLISH_TRUST_IN_CLIENT_NONE = "NONE";
      public static final String ESTABLISH_TRUST_IN_CLIENT_SUPPORTED = "SUPPORTED";
      public static final String ESTABLISH_TRUST_IN_CLIENT_REQUIRED = "REQUIRED";

      /**
       * integrity element indicates if the server (target) supports integrity protected messages.
       * The valid values are NONE, SUPPORTED or REQUIRED.
       * Required element.
       */
      private final String integrity;

      /**
       * confidentiality element indicates if the server (target) supports privacy protected
       * messages. The values are NONE, SUPPORTED or REQUIRED.
       * Required element.
       */
      private final String confidentiality;

      /**
       * detect-misordering indicates if the server (target) supports detection
       * of message sequence errors. The values are NONE, SUPPORTED or REQUIRED.
       * Optional element.
       */
      private final String detectMisordering;

      /**
       * detect-replay indicates if the server (target) supports detection
       * of message replay attempts. The values are NONE, SUPPORTED or REQUIRED.
       * Optional element.
       */
      private final String detectReplay;

      /**
       * establish-trust-in-target element indicates if the target is capable of authenticating to a client.
       * The values are NONE or SUPPORTED.
       * Required element.
       */
      private final String establishTrustInTarget;

      /**
       * establish-trust-in-client element indicates if the target is capable of authenticating a client. The
       * values are NONE, SUPPORTED or REQUIRED.
       * Required element.
       */
      private final String establishTrustInClient;

      private TransportConfig()
      {
         integrity = INTEGRITY_SUPPORTED;
         confidentiality = CONFIDENTIALITY_SUPPORTED;
         establishTrustInTarget = ESTABLISH_TRUST_IN_TARGET_SUPPORTED;
         establishTrustInClient = ESTABLISH_TRUST_IN_CLIENT_SUPPORTED;
         this.detectMisordering = DETECT_MISORDERING_SUPPORTED;
         this.detectReplay = DETECT_REPLAY_SUPPORTED;
      }

      /**
       * @param element  transport-config element.
       */
      private TransportConfig(Element element) throws DeploymentException
      {
         String value = MetaData.getUniqueChildContent(element, "integrity");
         if(INTEGRITY_NONE.equalsIgnoreCase(value))
         {
            integrity = INTEGRITY_NONE;
         }
         else if(INTEGRITY_SUPPORTED.equalsIgnoreCase(value))
         {
            integrity = INTEGRITY_SUPPORTED;
         }
         else if(INTEGRITY_REQUIRED.equalsIgnoreCase(value))
         {
            integrity = INTEGRITY_REQUIRED;
         }
         else
         {
            throw new DeploymentException("Allowed values for integrity element are " +
               INTEGRITY_NONE + ", " + INTEGRITY_REQUIRED + " and " + INTEGRITY_SUPPORTED +
               " but got " + value);
         }

         value = MetaData.getUniqueChildContent(element, "confidentiality");
         if(CONFIDENTIALITY_NONE.equalsIgnoreCase(value))
         {
            confidentiality = CONFIDENTIALITY_NONE;
         }
         else if(CONFIDENTIALITY_SUPPORTED.equalsIgnoreCase(value))
         {
            confidentiality = CONFIDENTIALITY_SUPPORTED;
         }
         else if(CONFIDENTIALITY_REQUIRED.equalsIgnoreCase(value))
         {
            confidentiality = CONFIDENTIALITY_REQUIRED;
         }
         else
         {
            throw new DeploymentException("Allowed values for confidentiality are " +
               CONFIDENTIALITY_NONE + ", " + CONFIDENTIALITY_SUPPORTED + " and " + CONFIDENTIALITY_REQUIRED +
               " but got " + value);
         }

         value = MetaData.getUniqueChildContent(element, "establish-trust-in-target");
         if(ESTABLISH_TRUST_IN_TARGET_NONE.equalsIgnoreCase(value))
         {
            establishTrustInTarget = ESTABLISH_TRUST_IN_TARGET_NONE;
         }
         else if(ESTABLISH_TRUST_IN_TARGET_SUPPORTED.equalsIgnoreCase(value))
         {
            establishTrustInTarget = ESTABLISH_TRUST_IN_TARGET_SUPPORTED;
         }
         else
         {
            throw new DeploymentException("Allowed values for establish-trust-in-target are " +
               ESTABLISH_TRUST_IN_TARGET_NONE + " and " + ESTABLISH_TRUST_IN_TARGET_SUPPORTED +
               " but got " + value);
         }

         value = MetaData.getUniqueChildContent(element, "establish-trust-in-client");
         if(ESTABLISH_TRUST_IN_CLIENT_NONE.equalsIgnoreCase(value))
         {
            establishTrustInClient = ESTABLISH_TRUST_IN_CLIENT_NONE;
         }
         else if(ESTABLISH_TRUST_IN_CLIENT_SUPPORTED.equalsIgnoreCase(value))
         {
            establishTrustInClient = ESTABLISH_TRUST_IN_CLIENT_SUPPORTED;
         }
         else if(ESTABLISH_TRUST_IN_CLIENT_REQUIRED.equalsIgnoreCase(value))
         {
            establishTrustInClient = ESTABLISH_TRUST_IN_CLIENT_REQUIRED;
         }
         else
         {
            throw new DeploymentException("Allowed values for establish-trust-in-client are " +
               ESTABLISH_TRUST_IN_CLIENT_NONE + ", " + ESTABLISH_TRUST_IN_CLIENT_SUPPORTED + " and " +
               ESTABLISH_TRUST_IN_CLIENT_REQUIRED + " but got " + value);
         }

         value = MetaData.getOptionalChildContent(element, "detect-misordering");
         if( DETECT_MISORDERING_NONE.equalsIgnoreCase(value) )
         {
            this.detectMisordering = DETECT_MISORDERING_NONE;
         }
         else if( DETECT_MISORDERING_REQUIRED.equalsIgnoreCase(value) )
         {
            this.detectMisordering = DETECT_MISORDERING_REQUIRED;
         }
         else if( DETECT_MISORDERING_SUPPORTED.equalsIgnoreCase(value) )
         {
            this.detectMisordering = DETECT_MISORDERING_SUPPORTED;
         }
         else
         {
            this.detectMisordering = DETECT_MISORDERING_NONE;
         }

         value = MetaData.getOptionalChildContent(element, "detect-replay");
         if( DETECT_REPLAY_NONE.equalsIgnoreCase(value) )
         {
            this.detectReplay = DETECT_REPLAY_NONE;
         }
         else if( DETECT_REPLAY_REQUIRED.equalsIgnoreCase(value) )
         {
            this.detectReplay = DETECT_REPLAY_REQUIRED;
         }
         else if( DETECT_REPLAY_SUPPORTED.equalsIgnoreCase(value) )
         {
            this.detectReplay = DETECT_REPLAY_SUPPORTED;
         }
         else
         {
            this.detectReplay = DETECT_REPLAY_NONE;
         }
      }

      public String getIntegrity()
      {
         return integrity;
      }

      public String getConfidentiality()
      {
         return confidentiality;
      }
      public String getDetectMisordering()
      {
         return detectMisordering;
      }

      public String getDetectReplay()
      {
         return detectReplay;
      }

      public String getEstablishTrustInTarget()
      {
         return establishTrustInTarget;
      }

      public boolean isEstablishTrustInTargetSupported()
      {
         return ESTABLISH_TRUST_IN_TARGET_SUPPORTED.equalsIgnoreCase(establishTrustInTarget);
      }

      public String getEstablishTrustInClient()
      {
         return establishTrustInClient;
      }

      public String toString()
      {
         return
            "[integrity=" + integrity +
            ", confidentiality=" + confidentiality +
            ", establish-trust-in-target=" + establishTrustInTarget +
            ", establish-trust-in-client=" + establishTrustInClient + 
			", detect-misordering=" + detectMisordering +
			", detect-replay=" + detectReplay + "]";
      }
   }

   /**
    * as-context (CSIv2 authentication service) is the element describing the authentication
    * mechanism that will be used to authenticate the client. It can be either
    * the username-password mechanism, or none (default).
    */
   public class AsContext
   {
      public static final String AUTH_METHOD_USERNAME_PASSWORD = "USERNAME_PASSWORD";
      public static final String AUTH_METHOD_NONE = "NONE";

      /**
       * auth-method element describes the authentication method. The only supported values
       * are USERNAME_PASSWORD and NONE.
       * Required element.
       */
      private final String authMethod;

      /**
       * realm element describes the realm in which the user is authenticated. Must be
       * a valid realm that is registered in server configuration.
       * Required element.
       */
      private final String realm;

      /**
       * required element specifies if the authentication method specified is required
       * to be used for client authentication. If so the EstablishTrustInClient bit
       * will be set in the target_requires field of the AS_Context. The element value
       * is either true or false.
       * Required element.
       */
      private final boolean required;

      private AsContext()
      {
         authMethod = AUTH_METHOD_USERNAME_PASSWORD;
         realm = "default";
         required = false;
      }

      private AsContext(Element element) throws DeploymentException
      {
         String value = MetaData.getUniqueChildContent(element, "auth-method");
         if(AUTH_METHOD_USERNAME_PASSWORD.equalsIgnoreCase(value))
         {
            authMethod = AUTH_METHOD_USERNAME_PASSWORD;
         }
         else if (AUTH_METHOD_NONE.equalsIgnoreCase(value))
         {
            authMethod = AUTH_METHOD_NONE;
         }
         else
         {
            throw new DeploymentException("The only allowed values for auth-method are "
            + AUTH_METHOD_USERNAME_PASSWORD + ", " + AUTH_METHOD_NONE +
               " but got " + value);
         }

         realm = MetaData.getUniqueChildContent(element, "realm");
         if(realm == null || realm.trim().length() == 0)
         {
            throw new DeploymentException("realm is not set for ior-security-config/as-context.");
         }

         value = MetaData.getUniqueChildContent(element, "required");
         if("true".equalsIgnoreCase(value))
         {
            required = true;
         }
         else if("false".equalsIgnoreCase(value))
         {
            required = false;
         }
         else
         {
            throw new DeploymentException("Allowed values for required in ior-security-config/as-context are " +
               "true and false but got " + value);
         }
      }

      public String getAuthMethod()
      {
         return authMethod;
      }

      public String getRealm()
      {
         return realm;
      }

      public boolean isRequired()
      {
         return required;
      }

      public String toString()
      {
         return
            "[auth-method=" + authMethod +
            ", realm=" + realm +
            ", required=" + required + "]";
      }
   }

   /**
    * sas-context (related to CSIv2 security attribute service) element describes
    * the sas-context fields.
    */
   public class SasContext
   {
      public static final String CALLER_PROPAGATION_NONE = "NONE";
      public static final String CALLER_PROPAGATION_SUPPORTED = "SUPPORTED";

      /**
       * caller-propagation element indicates if the target will accept propagated caller identities
       * The values are NONE or SUPPORTED.
       * Required element.
       */
      private final String callerPropagation;

      private SasContext()
      {
         callerPropagation = CALLER_PROPAGATION_NONE;
      }
      private SasContext(Element element) throws DeploymentException
      {
         String value = MetaData.getUniqueChildContent(element, "caller-propagation");
         if(CALLER_PROPAGATION_NONE.equalsIgnoreCase(value))
         {
            callerPropagation = CALLER_PROPAGATION_NONE;
         }
         else if(CALLER_PROPAGATION_SUPPORTED.equalsIgnoreCase(value))
         {
            callerPropagation = CALLER_PROPAGATION_SUPPORTED;
         }
         else
         {
            throw new DeploymentException("Allowed values for caller-propagation are " +
               CALLER_PROPAGATION_NONE + " and " + CALLER_PROPAGATION_SUPPORTED + " but got " + value);
         }
      }

      public String getCallerPropagation()
      {
         return callerPropagation;
      }

      public boolean isCallerPropagationSupported()
      {
         return CALLER_PROPAGATION_SUPPORTED.equalsIgnoreCase(callerPropagation);
      }

      public String toString()
      {
         return "[caller-propagation=" + callerPropagation + "]";
      }
   }
}
