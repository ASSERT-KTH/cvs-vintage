/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

// standard imports
import java.util.Properties;

import java.security.Principal;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.JMSException;

import javax.transaction.Transaction;
import javax.transaction.Status;


// jboss imports
import org.jboss.ejb.Container;
import org.jboss.ejb.MethodInvocation;


/**
 * MetricsInterceptor is used for gathering data from the container for admin
 * interface.
 *
 * @since   jBoss 2.0
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class MetricsInterceptor extends AbstractInterceptor {

    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private Container container           = null;
    private Context  namingContext        = null;
    private TopicPublisher metricsPub     = null;
    private TopicSession metricsSession   = null;
    private Topic metricsTopic            = null;
    
    private String applicationName        = "<undefined>";
    private String beanName               = "<undefined>";
    
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
   
    // Public --------------------------------------------------------
    public void setContainer(Container container) {
        this.container  = container;
        
        applicationName = container.getApplication().getName();
        beanName        = container.getBeanMetaData().getJndiName();
    }
    
    public Container getContainer() {
        return container;
    }

    // Interceptor implementation ------------------------------------
   public Object invokeHome(MethodInvocation mi) throws Exception {
     
     try {
        sendMessage(System.currentTimeMillis(), createMessage(mi, "START"));
        return super.invokeHome(mi);
     }
     finally {
         sendMessage(System.currentTimeMillis(), createMessage(mi, "STOP"));
     }
   }

    public Object invoke(MethodInvocation mi) throws Exception {

        try {
            sendMessage(System.currentTimeMillis(), createMessage(mi, "START"));
            return super.invoke(mi);
        }
      
        finally {
            sendMessage(System.currentTimeMillis(), createMessage(mi, "STOP"));
        }
   }

   public void init() {

       try {
           final boolean TRANSACTED       = false;
           final int     ACKNOWLEDGE_MODE = Session.DUPS_OK_ACKNOWLEDGE;
           
           namingContext = new InitialContext();
           
           TopicConnectionFactory factory = (TopicConnectionFactory)
                namingContext.lookup("TopicConnectionFactory");

           TopicConnection connection = factory.createTopicConnection();

           metricsTopic     = (Topic)namingContext.lookup("topic/metrics");
           metricsSession   = connection.createTopicSession(TRANSACTED, ACKNOWLEDGE_MODE);
           metricsPub       = metricsSession.createPublisher(metricsTopic);     
           
           connection.start();
       }
       catch (NamingException e) {
           System.out.println(e);
       }
       catch (JMSException e) {
           System.out.println(e);
       }
       
   }

   
    // Private --------------------------------------------------------

    private void sendMessage(long time, Message msg) {        

        if ((metricsPub == null) || (msg == null))
            return;
            
        try {
            msg.setStringProperty("TIME",  String.valueOf(time));
            metricsPub.publish(metricsTopic, msg);
        }
        catch (Exception e) {
            // catch JMSExceptions, NPE's etc and prevent them from propagating
            // up if the metrics fail
        }
    }
    
    private Message createMessage(MethodInvocation mi, String checkpoint) {
        
        if (metricsSession == null)
            return null;

        try {            
            Message  msg    =  metricsSession.createMessage();
            Transaction tx  =  mi.getTransaction();
            Principal principal = mi.getPrincipal();
            
            msg.setStringProperty("CHECKPOINT",  checkpoint);
            msg.setStringProperty("APPLICATION", applicationName);
            msg.setStringProperty("BEAN",   beanName);
            msg.setObjectProperty("METHOD", mi.getMethod().toString());    
            
            if (tx == null) {
                // This is a workaround for SpyMessage throwing NPE if
                // getIntProperty(..) is called on a non-existant key.
                // javax.jms.MessageFormatException would seem more
                // appropriate (it's checked exception)
                msg.setIntProperty("STATUS", Status.STATUS_UNKNOWN);
            }
            else {
                msg.setStringProperty("ID",  String.valueOf(tx.hashCode()));
                msg.setIntProperty("STATUS", tx.getStatus());
            }
                        
            if (principal != null)
                msg.setStringProperty("PRINCIPAL", principal.getName());
                
            return msg;
        }
        catch (Exception e) {
            // catch JMSExceptions, tx.SystemExceptions, and NPE's
            // don't want to bother the container even if the metrics fail.
            return null;
        }
    }
    
}

