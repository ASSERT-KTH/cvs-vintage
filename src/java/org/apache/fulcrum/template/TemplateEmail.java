package org.apache.fulcrum.template;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and 
 *    "Apache Turbine" must not be used to endorse or promote products 
 *    derived from this software without prior written permission. For 
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without 
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.mail.SimpleEmail;
import org.apache.fulcrum.ServiceException;

/**
 * This is a simple class for sending email from within the TemplateService.
 * Essentially, the body of the email is processed with a 
 * TemplateContext object.
 * The beauty of this is that you can send email from within your
 * template layer or from your business logic in your Java code.
 * The body of the email is just a TemplateService template so you can use
 * all the template functionality of your TemplateService within your emails!
 *
 * <p>Example Usage (This all needs to be on one line in your
 * template):
 *
 * <p>Setup your imports:
 *
 * <p>import org.apache.fulcrum.template.TemplateEmail;
 * <p>import org.apache.turbine.modules.ContextAdapter;
 *
 * <p>Setup your context:
 *
 * <p>context.put ("TemplateEmail", new TemplateEmail() );
 * <p>context.put ("contextAdapter", new ContextAdapter(context) );
 *
 * <p>Then, in your template (Velocity Example):
 *
 * <pre>
 * $TemplateEmail.setTo("Jon Stevens", "jon@latchkey.com")
 *     .setFrom("Mom", "mom@mom.com").setSubject("Eat dinner")
 *     .setTemplate("email/momEmail.vm")
 *     .setContext($contextAdapter)
 * </pre>
 *
 * The email/momEmail.vm template will then be parsed with the
 * Context that was defined with setContext().
 *
 * <p>If you want to use this class from within your Java code all you
 * have to do is something like this:
 *
 * <p>import org.apache.fulcrum.template.TemplateEmail;
 * <p>import org.apache.turbine.modules.ContextAdapter;
 *
 * <pre>
 * TemplateEmail ve = new TemplateEmail();
 * ve.setTo("Jon Stevens", "jon@latchkey.com");
 * ve.setFrom("Mom", "mom@mom.com").setSubject("Eat dinner");
 * ve.setContext(new ContextAdapter(context));
 * ve.setTemplate("email/momEmail.vm")
 * ve.send();
 * </pre>
 *
 * <p>(Note that when used within a Velocity template, the send method
 * will be called for you when Velocity tries to convert the
 * TemplateEmail to a string by calling toString()).</p>
 *
 * <p>If you need your email to be word-wrapped, you can add the 
 * following call to those above:
 *
 * <pre>
 * ve.setWordWrap (60);
 * </pre>
 *
 * <p>This class is just a wrapper around the SimpleEmail class.
 * Thus, it uses the JavaMail API and also depends on having the
 * mail.host property set in the System.properties().
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:gcoladonato@yahoo.com">Greg Coladonato</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: TemplateEmail.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public class TemplateEmail
{
    /** The to name field. */
    private String toName = null;

    /** The to email field. */
    private String toEmail = null;

    /** The from name field. */
    private String fromName = null;

    /** The from email field. */
    private String fromEmail = null;

    /** The cc name field. */
    private String ccName = null;

    /** The cc email field. */
    private String ccEmail = null;

    /** The subject of the message. */
    private String subject = null;

    /** The to email list. */
    private List toList = null;

    /** The cc list. */
    private List ccList = null;

    /** The cc list. */
    private List replyToList = null;

    private List headersList;

    /** The column to word-wrap at.  <code>0</code> indicates no wrap. */
    private int wordWrap = 0;

    /**
     * The template to process, relative to the TemplateService template
     * directory.
     */
    private String template = null;

    /**
     * A Context
     */
    private TemplateContext context = null;

    /**
     * The charset
     */
    private String charset = null;

    /**
     * Constructor
     */
    public TemplateEmail()
    {
        this(null);
    }

    /**
     * Constructor
     */
    public TemplateEmail(TemplateContext context)
    {
        this.context = context;
    }

    public String getCharSet()
    {
        return charset;
    }

    public String getTemplate()
    {
        return template;
    }

    public int getWordWrap()
    {
        return wordWrap;
    }

    public List getToList()
    {
        return toList == null ? toList = new ArrayList() : toList;
    }

    public void setToList(List v)
    {
        toList = v;
    }

    public List getCCList()
    {
        return ccList == null ? ccList = new ArrayList() : ccList;
    }

    public List getReplyToList()
    {
        return replyToList == null ? replyToList = new ArrayList(3) : replyToList;
    }

    public List getHeadersList()
    {
        return headersList == null ? headersList = new ArrayList(3) : headersList;
    }

    public String getToName()
    {
        return toName;
    }

    public String getToEmail()
    {
        return toEmail;
    }

    public String getFromName()
    {
        return fromName;
    }

    public String getFromEmail()
    {
        return fromEmail;
    }

    public String getCCName()
    {
        return ccName;
    }

    public String getCCEmail()
    {
        return ccEmail;
    }

    /** Add a recipient TO to the email.
     *
     * @param email A String.
     * @param name A String.
     */
    public void addTo(String email, String name) 
        throws Exception
    {
        try
        {
            if ((name == null) || (name.trim().equals("")))
            {
                name = email;
            }

            if (getCharSet() != null) 
            {
                getToList().add(new InternetAddress(email, name, getCharSet()));
            }
            else
            {
                getToList().add(new InternetAddress(email, name));
            }
        }
        catch (Exception e)
        {
            throw new Exception("Cannot add 'To' recipient: " + e);
        }
    }
 
    /**
     * Add a recipient CC to the email.
     *
     * @param email A String.
     * @param name A String.
     */
    public void addCc(String email, String name) 
        throws Exception
    {
        try
        {
            if ((name == null) || (name.trim().equals("")))
            {
                name = email;
            }

            if (getCharSet() != null) 
            {
                getCCList().add(new InternetAddress(email, name, getCharSet()));
            }
            else
            {
                getCCList().add(new InternetAddress(email, name));
            }
        }
        catch (Exception e)
        {
            throw new Exception("Cannot add 'CC' recipient: " + e);
        }
    }


    /**
     * The given Unicode string will be charset-encoded using the specified 
     * charset. The charset is also used to set the "charset" parameter.
     *
     * @param charset a <code>String</code> value
     */
    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    /**
     * To: name, email
     *
     * @param to A String with the TO name.
     * @param email A String with the TO email.
     * @return A TemplateEmail (self).
     */
    public TemplateEmail setTo(String to,
                               String email)
    {
        this.toName = to;
        this.toEmail = email;
        return (this);
    }

    /**
     * From: name, email.
     *
     * @param from A String with the FROM name.
     * @param email A String with the FROM email.
     * @return A TemplateEmail (self).
     */
    public TemplateEmail setFrom(String from,
                                 String email)
    {
        this.fromName = from;
        this.fromEmail = email;
        return (this);
    }

    /**
     * CC: name, email.
     *
     * @param from A String with the CC name.
     * @param email A String with the CC email.
     * @return A TemplateEmail (self).
     */
    public TemplateEmail setCC(String cc,
                                 String email)
    {
        this.ccName = cc;
        this.ccEmail = email;
        return (this);
    }


    /**
     * Add a reply to address to the email.
     *
     * @param email A String.
     * @param name A String.
     * @return An Email.
     * @exception MessagingException.
     */
    public TemplateEmail addReplyTo( String name, String email) 
    {
        String[] emailName = new String[2];
        emailName[0] = email;
        emailName[1] = name;
        getReplyToList().add(emailName);
        return this;
    }

    public TemplateEmail addHeader(String name, String value)
    {
        String[] pair = new String[2];
        pair[0] = name;
        pair[1] = value;
        getHeadersList().add(pair);
        return this;
    }

    public String getSubject()
    {
        return this.subject;
    }

    /**
     * Subject.
     *
     * @param subject A String with the subject.
     * @return A TemplateEmail (self).
     */
    public TemplateEmail setSubject(String subject)
    {
        if (subject == null)
        {
            this.subject = "";
        }
        else
        {
            this.subject = subject;
        }
        return (this);
    }

    /**
     * TemplateService template to execute. Path is relative to the
     * TemplateService templates directory.
     *
     * @param template A String with the template.
     * @return A TemplateEmail (self).
     */
    public TemplateEmail setTemplate(String template)
    {
        this.template = template;
        return (this);
    }

    /**
     * Set the column at which long lines of text should be word-
     * wrapped. Setting to zero turns off word-wrap (default).
     *
     * NOTE: don't use tabs in your email template document,
     * or your word-wrapping will be off for the lines with tabs
     * in them.
     *
     * @param wordWrap The column at which to wrap long lines.
     * @return A TemplateEmail (self).
     */
    public TemplateEmail setWordWrap(int wordWrap)
    {
        this.wordWrap = wordWrap;
        return (this);
    }

    /**
     * Set the context object that will be merged with the 
     * template.
     *
     * @param context A TemplateContext context object.
     * @return A TemplateEmail (self).
     */
    public TemplateEmail setContext(TemplateContext context)
    {
        this.context = context;
        return (this);
    }

    /**
     * Get the context object that will be merged with the 
     * template.
     *
     * @return A TemplateContext.
     */
    public TemplateContext getContext()
    {
        return this.context;
    }

    /**
     * This method sends the email. It will throw an exception
     * if the To name or To Email values are null.
     */
    public void send()
        throws Exception
    {
        if (getToEmail() == null || getToName() == null)
        {
            throw new Exception ("Must set a To:");
        }

        // this method is only supposed to send to one user (additional cc:
        // users are ok.)
        setToList(null);
        addTo(toEmail, toName);
        sendMultiple();
    }

    protected String handleRequest()
        throws ServiceException
    {
        StringWriter sw = new StringWriter();
        TurbineTemplate.handleRequest(getContext(),getTemplate(),sw);
        return sw.toString();
    }

    /**
     * This method sends the email to multiple addresses.
     */
    public void sendMultiple()
        throws Exception
    {
        if (getToList() == null || getToList().isEmpty())
        {
            throw new Exception ("Must set a To:");
        }

        // Process the template.
        String body = handleRequest();

        // If the caller desires word-wrapping, do it here
        if (getWordWrap() > 0)
        {
            body = WordUtils.wrap(body, getWordWrap());
        }

        SimpleEmail se = new SimpleEmail();
        if (getCharSet() != null) 
        {
            se.setCharset(getCharSet());            
        }
        se.setFrom(getFromEmail(), getFromName());
        se.setTo(getToList());
        if (getCCList() != null && !getCCList().isEmpty())
        {
            se.setCc(getCCList());
        }
        addReplyTo(se);
        se.setSubject(getSubject());
        se.setMsg(body);

        if (getHeadersList() != null) 
        {
            for (Iterator i = getHeadersList().iterator();i.hasNext();) 
            {
                String[] pair = (String[])i.next();
                se.addHeader(pair[0], pair[1]);
            }
        }
        
        se.send();
    }

    /**
     * if any reply-to email addresses exist, add them to the SimpleEmail
     *
     * @param se a <code>SimpleEmail</code> value
     * @exception Exception if an error occurs
     */
    private void addReplyTo(SimpleEmail se)
        throws Exception
    {
        if (getReplyToList() != null) 
        {
            for (Iterator i = getReplyToList().iterator();i.hasNext();) 
            {
                String[] emailName = (String[])i.next();
                se.addReplyTo(emailName[0], emailName[1]);
            }
        }
    }

    /**
     * The method toString() calls send() for ease of use within a
     * TemplateService template (see example usage above).
     *
     * @return An empty string.
     */
    public String toString()
    {
        try
        {
            send();
        }
        catch (Exception e)
        {
//            Log.error ("TemplateEmail error", e);
        }
        return "";
    }
}
