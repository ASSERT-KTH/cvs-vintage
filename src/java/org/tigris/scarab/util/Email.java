package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.mimetype.TurbineMimeTypes;
import org.apache.fulcrum.template.TemplateContext;
import org.apache.fulcrum.template.TemplateEmail;
import org.apache.fulcrum.velocity.ContextAdapter;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.email.VelocityEmail;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.localization.L10NKeySet;

/**
 * Sends a notification email.
 *
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: Email.java,v 1.44 2004/05/07 05:57:47 dabbous Exp $
 */
public class Email extends TemplateEmail
{
    private static final int TO = 0;
    private static final int CC = 1;

    /**
     * Sends email to a single recipient. Returns false if it fails
     * to send the email for any reason.
     */
    public static boolean sendEmail(EmailContext context, Module module,
                                    Object fromUser, Object replyToUser,
                                    ScarabUser toUser, String template)
            throws Exception
    {
        Collection toUsers = new ArrayList(2);
        toUsers.add(toUser);
        return sendEmail(context, module, fromUser, replyToUser, toUsers, null,
                template);
    }

    /** 
     * Sends email to multiple recipients. Returns false if it fails
     * to send the email for any reason.
     */
    public static boolean sendEmail(EmailContext context, Module module,
                                    Object fromUser, Object replyToUser,
                                    Collection toUsers, Collection ccUsers,
                                    String template) 
        throws Exception
    {
        if (!GlobalParameterManager.getBoolean(GlobalParameter.EMAIL_ENABLED,
                module))
        {
            return true;
        }

        boolean success = true;

        //
        // To avoid any NullPointerExceptions, create
        // empty lists of to: and cc: users if the
        // collections are null.
        //
        if (toUsers == null)
        {
            toUsers = new ArrayList();
        }

        if (ccUsers == null)
        {
            ccUsers = new ArrayList();
        }

        //
        // Remove duplicate addresses from the cc: list
        //
        ccUsers.removeAll(toUsers);

        String archiveEmail = module.getArchiveEmail();
        if (archiveEmail != null && archiveEmail.trim().length() == 0)
        {
            archiveEmail = null;
        }

        Map userLocaleMap = groupAddressesByLocale(module, toUsers, ccUsers,
                archiveEmail);

        for (Iterator i = userLocaleMap.keySet().iterator(); i.hasNext();)
        {
            Locale locale = (Locale) i.next();
            List[] toAndCC = (List[]) userLocaleMap.get(locale);
            List to = toAndCC[TO];
            List cc = toAndCC[CC];

            sendEmailInLocale(context, module, fromUser, replyToUser, to, cc,
                              template, locale);
        }

        return success;
    }

    /** Sends email in a specific locale. */
    private static void sendEmailInLocale(EmailContext context, Module module,
                                          Object fromUser, Object replyToUser,
                                          List toAddresses, List ccAddresses,
                                          String template, Locale locale)
        throws Exception
    {
        Log.get().debug("Sending email for locale=" + locale);

        // get reference to l10n tool, so we can alter the locale per email
        ScarabLocalizationTool l10n = new ScarabLocalizationTool();
        context.setLocalizationTool(l10n);
        l10n.init(locale);

        Email te = getEmail(context, module, fromUser, replyToUser, template);
        te.setCharset(getCharset(locale));

        boolean atLeastOneTo = false;
        for (Iterator iTo = toAddresses.iterator(); iTo.hasNext();)
        {
            InternetAddress a = (InternetAddress) iTo.next();
            te.addTo(a.getAddress(), a.getPersonal());
            atLeastOneTo = true;
            Log.get().debug("Added To: " + a.getAddress());
        }
        for (Iterator iCC = ccAddresses.iterator(); iCC.hasNext();)
        {
            InternetAddress a = (InternetAddress) iCC.next();
            String email = a.getAddress();
            String name = a.getPersonal();

            // template email requires a To: user, it does seem possible
            // to send emails with only a CC: user, so not sure if this
            // is a bug to be fixed in TemplateEmail.  Might not be good
            // form anyway.  So if there are no To: users, upgrade CC's.
            if (atLeastOneTo)
            {
                te.addCc(email, name);
            }
            else
            {
                te.addTo(email, name);
                // We've added one To: user and TemplateEmail should be
                // happy. No need to move all CC: into TO:
                atLeastOneTo = true;
            }
            Log.get().debug("Added CC: " + email);
        }
        try{
        te.sendMultiple();
    }
        catch(SendFailedException sfe)
        {
            Throwable t = sfe.getNextException();
            throw ScarabException.create(L10NKeySet.ExceptionEmailFailure,t);
        }
    }

    /**
     * Creates a map of Locale objects -> List[2], where the first
     * element of the list array is a list of "To:" addresses, and
     * the second is a list of "Cc:" addresses. For example, if
     * user "Pierre" is in <code>toUsers</code> and requires emails
     * in french, his email address will be in
     * <code>userLocaleMap[Locale.FRANCE][TO]</code>. The same applies
     * to "Cc:" addresses, while the archive email address is associated
     * with the default module locale.
     */
    private static Map groupAddressesByLocale(Module module,
                                              Collection toUsers,
                                              Collection ccUsers,
                                              String archiveEmail)
        throws Exception
    {
        Map result = new HashMap();
        for (Iterator iter = toUsers.iterator(); iter.hasNext();)
        {
            fileUser(result, (ScarabUser) iter.next(), module, TO);
        }

        for (Iterator iter = ccUsers.iterator(); iter.hasNext();)
        {
            fileUser(result, (ScarabUser) iter.next(), module, CC);
        }
        if (archiveEmail != null)
        {
            fileAddress(result, new InternetAddress(archiveEmail),
                    chooseLocale(null, module), CC);
        }
        return result;
    }

    private static void fileAddress(Map userLocaleMap, InternetAddress address,
                                    Locale locale, int toOrCC)
    {
        List[] toAndCC = (List[]) userLocaleMap.get(locale);
        if (toAndCC == null)
        {
            toAndCC = new List[2];
            toAndCC[0] = new ArrayList();
            toAndCC[1] = new ArrayList();
            userLocaleMap.put(locale, toAndCC);
        }
        toAndCC[toOrCC].add(address);
    }

    private static void fileUser(Map userLocaleMap, ScarabUser user,
                                 Module module, int toOrCC) throws Exception
    {
        fileAddress(userLocaleMap, new InternetAddress(user.getEmail(), user
                .getName()), chooseLocale(user, module), toOrCC);
    }

    /**
     * Override the super.handleRequest() and process the template
     * our own way.
     * This could have been handled in a more simple way, which was
     * to create a new service and associate the emails with a different
     * file extension which would have prevented the need to override
     * this method, however, that was discovered after the fact and it
     * also seemed to be a bit more work to change the file extension. 
     */
    protected String handleRequest() throws ServiceException
    {
        String result = null;
        try
        {
            result = VelocityEmail.handleRequest(new ContextAdapter(
                    getContext()), getTemplate());
        }
        catch (Exception e)
        {
            throw new ServiceException(e); //EXCEPTION
        }
        return result;
    }

    /**
     * @param context The context in which to send mail, or
     * <code>null</code> to create a new context.
     * @param fromUser Can be any of the following: ScarabUser, two
     * element String[] composed of name and address, base portion of
     * the key used for a name and address property lookup.
     * @param replyToUser Can be any of the following: ScarabUser, two
     * element String[] composed of name and address, base portion of
     * the key used for a name and address property lookup.
     */
    private static Email getEmail(EmailContext context, Module module,
                                  Object fromUser, Object replyToUser,
                                  String template) throws Exception
    {
        Email te = new Email();
        if (context == null)
        {
            context = new EmailContext();
        }
        te.setContext(context);

        EmailLink el = EmailLinkFactory.getInstance(module);
        context.setLinkTool(el);

        String[] nameAndAddr = getNameAndAddress(fromUser);
        te.setFrom(nameAndAddr[0], nameAndAddr[1]);

        nameAndAddr = getNameAndAddress(replyToUser);
        te.addReplyTo(nameAndAddr[0], nameAndAddr[1]);

        if (template == null)
        {
            template = Turbine.getConfiguration().getString(
                    "scarab.email.default.template", "Default.vm");
        }
        te.setTemplate(prependDir(template));
        String subjectTemplate = context.getSubjectTemplate();
        if (subjectTemplate == null)
        {
            int templateLength = template.length();
            // The magic number 7 represents "Subject"
            StringBuffer templateSB = new StringBuffer(templateLength + 7);
            // The magic number 3 represents ".vm"
            templateSB.append(template.substring(0, templateLength - 3));
            subjectTemplate = templateSB.append("Subject.vm").toString();
        }

        te.setSubject(getSubject(context, subjectTemplate));
        return te;
    }

    /**
     * Leverages the <code>fromName</code> and
     * <code>fromAddress</code> properties when <code>input</code> is
     * neither a <code>ScarabUser</code> nor <code>String[]</code>.
     */
    private static String[] getNameAndAddress(Object input)
    {
        String[] nameAndAddr;
        if (input instanceof ScarabUser)
        {
            ScarabUser u = (ScarabUser) input;
            nameAndAddr = new String[]{u.getName(), u.getEmail()};
        }
        else if (input instanceof String[])
        {
            nameAndAddr = (String[]) input;
        }
        else
        {
            // Assume we want a property lookup, and the base portion
            // of the key to use for that lookup was passed in.
            String keyBase = (String) input;
            if (keyBase == null)
            {
                keyBase = "scarab.email.default";
            }

            // TODO: Discover a better sending host/domain than
            // "localhost"

            nameAndAddr = new String[2];
            nameAndAddr[0] = Turbine.getConfiguration().getString(
                    keyBase + ".fromName", "Scarab System");
            nameAndAddr[1] = Turbine.getConfiguration().getString(
                    keyBase + ".fromAddress", "help@localhost");
        }
        return nameAndAddr;
    }

    private static String getSubject(TemplateContext context, String template)
    {
        template = prependDir(template);
        String result = null;
        try
        {
            // render the template
            result = VelocityEmail.handleRequest(new ContextAdapter(context),
                    template);
            if (result != null)
            {
                result = result.trim();
            }
            // in some of the more complicated templates, we set a context
            // variable so that there is not a whole bunch of whitespace
            // that can make it into the subject...
            String subject = (String) context.get("emailSubject");
            if (subject != null)
            {
                result = subject.trim();
            }
        }
        catch (Exception e)
        {
            Log.get()
                    .error("Error rendering subject for " + template + ". ", e);
            result = "Scarab System Notification";
        }
        return result;
    }

    private static String prependDir(String template)
    {
        boolean b = false;
        try
        {
            b = GlobalParameterManager
                    .getBoolean(GlobalParameter.EMAIL_INCLUDE_ISSUE_DETAILS);
        }
        catch (Exception e)
        {
            Log.get().debug("", e);
            // use the basic email
        }
        return b ? "email/" + template : "basic_email/" + template;
    }

    /**
     * Returns a charset for the given locale that is generally
     * preferred by email clients.  If not specified by the property
     * named by {@link
     * org.tigris.scarab.util.ScarabConstants#DEFAULT_EMAIL_ENCODING_KEY},
     * ask the <code>MimeTypeService</code> for a good value (except
     * for Japanese, which always uses the encoding
     * <code>ISO-2022-JP</code>).
     *
     * @param locale a <code>Locale</code> value
     * @return a <code>String</code> value
     */
    public static String getCharset(Locale locale)
    {
        String charset = Turbine.getConfiguration().getString(
                ScarabConstants.DEFAULT_EMAIL_ENCODING_KEY, "").trim();
        if (charset.length() == 0 || "native".equalsIgnoreCase(charset))
        {
            if ("ja".equals(locale.getLanguage()))
            {
                charset = "ISO-2022-JP";
            }
            else
            {
                charset = TurbineMimeTypes.getCharSet(locale);
            }
        }

        return charset;
    }

    private static Locale chooseLocale(ScarabUser user, Module module)
    {
        Locale locale = null;
        if (user != null)
        {
            try
            {
                locale = user.getPreferredLocale();
            }
            catch (Exception e)
            {
                Log.get().error(
                        "Couldn't determine locale for user " + user
                                .getUserName(), e);
            }
        }
        if (locale == null)
        {
            if (module != null && module.getLocale() != null)
            {
                locale = module.getLocale();
            }
            else
            {
                locale = ScarabConstants.DEFAULT_LOCALE;
            }
        }
        return locale;
    }
}
