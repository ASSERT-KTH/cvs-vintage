/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mail;

import java.io.InputStream;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.mail.Session;
import javax.mail.PasswordAuthentication;
import javax.mail.Authenticator;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.naming.NonSerializableFactory;

/**
 * MBean that gives support for JavaMail. Object of class javax.mail.Session will be bound
 * in JNDI under java:/ namespace with the name provided with method {@link #setJNDIName}.
 *
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.7 $
 */
public class MailService
	extends ServiceMBeanSupport
	implements MailServiceMBean
{
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	private String m_user;
	private String m_password;
	private String m_properties;
	private String m_jndiName;
	private String m_bindName;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------

	// Public --------------------------------------------------------
	public String getName()
	{
		return "Mail Service";
	}
	public void setUser(String user) {m_user = user;}
	protected String getUser() {return m_user;}
	public void setPassword(String password) {m_password = password;}
	protected String getPassword() {return m_password;}
	public void setConfigurationFile(String file) {m_properties = file;}
	protected String getConfigurationFile() {return m_properties;}
	public void setJNDIName(String name) {m_jndiName = name;}
	protected String getJNDIName() {return m_jndiName;}

	public void startService()
		throws Exception
	{
		// Setup password authentication
		final PasswordAuthentication pa = new PasswordAuthentication(getUser(), getPassword());
		Authenticator a = new Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return pa;
			}
		};

		// Read mail properties from configuration directory
		String properties = getConfigurationFile();
		// If MBean does not provide configuration file, default to mail.properties
		if (properties == null) {properties = "mail.properties";}
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(properties);
		if (is == null) {throw new java.io.FileNotFoundException("Cannot find file '" + properties + "'");}
		Properties p = new Properties();
		p.load(is);

		// Finally create a mail session
		Session session = Session.getInstance(p, a);
		bind(session);
	}

	public void stopService()
	{
		// Unbind from JNDI
		try
		{
			unbind();
		}
		catch (NamingException x)
		{
			log.error("Unbind failed", x);
		}
	}


	// Private -----------------------------------------------------
	private void bind(Session session) throws NamingException
	{
		Context ctx = new InitialContext();
		String name = getJNDIName();
		if (name == null) {name = "java:/Mail";}
		else if (!name.startsWith("java:/")) {name = "java:/" + name;}
		m_bindName = name;

		// Ah ! Session isn't serializable, so we use a helper class
		NonSerializableFactory.bind(m_bindName, session);

		Name n = ctx.getNameParser("").parse(m_bindName);
		while (n.size() > 1)
		{
			String ctxName = n.get(0);
			try
			{
				ctx = (Context)ctx.lookup(ctxName);
			}
			catch (NameNotFoundException e)
			{
				ctx = ctx.createSubcontext(ctxName);
			}
			n = n.getSuffix(1);
		}

		// The helper class NonSerializableFactory uses address type nns, we go on to
		// use the helper class to bind the javax.mail.Session object in JNDI
		StringRefAddr addr = new StringRefAddr("nns", m_bindName);
		Reference ref = new Reference(Session.class.getName(), addr, NonSerializableFactory.class.getName(), null);
		ctx.bind(n.get(0), ref);

            if (log.isInfoEnabled())
               log.info("Mail Service '" + getJNDIName() + "' bound to " + m_bindName);
	}

	private void unbind() throws NamingException
	{
		if (m_bindName != null)
		{
			new InitialContext().unbind(m_bindName);
			NonSerializableFactory.unbind(m_bindName);
                  if (log.isInfoEnabled())
                     log.info("Mail service '" + getJNDIName() + "' removed from JNDI");
		}
	}
}
