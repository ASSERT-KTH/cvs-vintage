package org.jboss.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.Container;
import org.jboss.ejb.plugins.*;
import org.jboss.logging.Logger;
import org.jboss.util.CounterService;

/**
 * Interceptor that uses the CounterService MBean to record the length of time
 * spent in 'lower' interceptors (below it in the stack). 
 * <p><b>How to use:</b></p>
 * <p>First, the CounterService MBean must be installed in JBoss. To do this,
 * place the following in your JBoss.jcml file, near the very end.</p>
 * <code>
 * &lt;mbean code="org.jboss.util.CounterService" name="DefaultDomain:service=CounterService" &gt; &lt;/mbean&gt;
 * </code>
 * <p>This will start up and enable the centralized counter in your JBoss server
 * instance.
 * <p>Next, you need to configure this interceptor into the interceptor stacks
 * of any beans you wish to monitor. This can be done either globally for a 
 * container-config in standardjboss.xml, or on a per-bean basis in a jar's 
 * jboss.jcml. Just insert the following at the top of the &lt;container-interceptors&gt;
 * section. If you're overriding this for a bean in jboss.xml, you'll need to
 * override the entire container-interceptors section.</p>
 * <code>
 * &lt;interceptor&gt;org.jboss.util.CounterInterceptor&lt;/interceptor&gt;
 * </code>
 * <p>This can go anywhere in the container-interceptors section, but either
 * the top or the bottom will probably be best for gathering application 
 * statistics.
 * @author <a href="mailto:danch@nvisia.com">Dan Christopherson</href>
 */
public class CounterInterceptor extends AbstractInterceptor {

   Container container = null;
   CounterService counter = null;
   boolean loggedNoCounter = false;
   StringBuffer baseCounterName = null;
   int baseNameLength = 0;
   
   public CounterInterceptor() {
   }
   public void setContainer(Container container) {
      baseCounterName = new StringBuffer(container.getBeanClass().getName());
      baseNameLength = baseCounterName.length();
      this.container = container;
   }
   public Container getContainer() {
      return container;
   }
   
   public Object invokeHome(MethodInvocation mi) throws Exception {
      long startTime=System.currentTimeMillis();
      try {
         return super.invokeHome(mi);
      } finally {
         if (getCounter() != null) {
            long endTime=System.currentTimeMillis();
            baseCounterName.append("Home.");
            baseCounterName.append(mi.getMethod().getName());
            counter.accumulate(baseCounterName.toString(), endTime-startTime);
            baseCounterName.setLength(baseNameLength);
         }
      }
   }
      
   public Object invoke(MethodInvocation mi) throws Exception {
      long startTime=System.currentTimeMillis();
      try {
         return super.invoke(mi);
      } finally {
         if (getCounter() != null) {
            long endTime=System.currentTimeMillis();
            baseCounterName.append('.');
            baseCounterName.append(mi.getMethod().getName());
            counter.accumulate(baseCounterName.toString(), endTime-startTime);
            baseCounterName.setLength(baseNameLength);
         }
      }
   }
   
   public void init() throws java.lang.Exception {
      //get a reference to the CounterService from JNDI
      Logger.debug("CounterInterceptor initializing");
   }
   
   private CounterService getCounter() {
      if (counter == null) {
         try {
            InitialContext ctx = new InitialContext();
            counter = (CounterService)ctx.lookup("java:/CounterService");
         } catch (NamingException ne) {
            if (!loggedNoCounter) {
               Logger.warning("CounterInterceptor can't get counter service "+ne);
               loggedNoCounter = true;
            }
         }
      }
      return counter;
   }
}
