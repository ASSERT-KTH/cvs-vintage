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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import javax.mail.SendFailedException;

import org.apache.fulcrum.template.TurbineTemplate;
import org.apache.fulcrum.template.TemplateContext;
import org.apache.fulcrum.template.TemplateEmail;
import org.apache.fulcrum.mimetype.TurbineMimeTypes;

import org.apache.turbine.Turbine;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;

/**
 * Sends a notification email.
 *
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: Email.java,v 1.26 2003/04/21 19:39:15 jackrepenning Exp $
 */
public class Email
{
    private static final int TO = 0;
    private static final int CC = 1;

    public static boolean sendEmail(EmailContext context, Module module, 
                                    Object fromUser, Object replyToUser,
                                    Collection toUsers, Collection ccUsers,
                                    String template)
        throws Exception
    {
        if (!GlobalParameterManager
            .getBoolean(GlobalParameter.EMAIL_ENABLED, module))
        {
            return true;
        }

        boolean success = true;

        // get reference to l10n tool, so we can alter the locale per email
        ScarabLocalizationTool l10n = new ScarabLocalizationTool();
        context.setLocalizationTool(l10n);

        Map userLocaleMap = new HashMap();
        for (Iterator iter = toUsers.iterator(); iter.hasNext();) 
        {
            ScarabUser toUser = (ScarabUser)iter.next();
            // remove any CC users that are also in the To
            if (ccUsers != null)
            {
                ccUsers.remove(toUser);
            }
            fileUser(userLocaleMap, toUser, module, TO);
        }

        if (ccUsers != null)
        {
            for (Iterator iter = ccUsers.iterator(); iter.hasNext();) 
            {
                ScarabUser ccUser = (ScarabUser)iter.next();
                fileUser(userLocaleMap, ccUser, module, CC);
            }
        }

        Locale moduleLocale = null;
        String archiveEmail = module.getArchiveEmail();
        boolean sendArchiveEmail = false;
        if (archiveEmail != null && archiveEmail.trim().length() > 0)
        {
            moduleLocale = chooseLocale(null, module);
            Log.get().debug("archive email locale=" + moduleLocale);
            sendArchiveEmail = true;
        }

        for (Iterator i = userLocaleMap.keySet().iterator(); i.hasNext();) 
        {
            Locale locale = (Locale)i.next();
            Log.get().debug("Sending email for locale=" + locale);
            l10n.init(locale);
            TemplateEmail te = getTemplateEmail(context, fromUser, 
                                                replyToUser, template);        
            te.setCharset(getCharset(locale));
       
            List[] toAndCC = (List[])userLocaleMap.get(locale);
            boolean atLeastOneTo = false;
            for (Iterator iTo = toAndCC[TO].iterator(); iTo.hasNext();) 
            {
                ScarabUser user = (ScarabUser)iTo.next();
                te.addTo(user.getEmail(), user.getName());
                atLeastOneTo = true;
                Log.get().debug("Added To: " + user.getEmail());
            }
            for (Iterator iCC = toAndCC[CC].iterator(); iCC.hasNext();) 
            {
                ScarabUser user = (ScarabUser)iCC.next();
                // template email requires a To: user, it does seem possible
                // to send emails with only a CC: user, so not sure if this
                // is a bug to be fixed in TemplateEmail.  Might not be good
                // form anyway.  So if there are no To: users, upgrade CC's.
                if (atLeastOneTo) 
                {
                    te.addCc(user.getEmail(), user.getName());
                }
                else 
                {
                    te.addTo(user.getEmail(), user.getName());
                }
                Log.get().debug("Added CC: " + user.getEmail());
            }

            if (sendArchiveEmail && locale.equals(moduleLocale)) 
            {
                te.addCc(archiveEmail, null);
                sendArchiveEmail = false;
                Log.get().debug("Archive was sent with other users.");
            }

            try
            {
                te.sendMultiple();
            }
            catch (SendFailedException e)
            {
                success = false;
            }
        }
        
        // make sure the archive email is sent
        if (sendArchiveEmail) 
        {
            Log.get().debug("Archive was sent separately.");
            l10n.init(moduleLocale);
            TemplateEmail te = getTemplateEmail(context, fromUser, 
                                                replyToUser, template);        
            te.setCharset(getCharset(moduleLocale));
            te.addTo(archiveEmail, null);
            try
            {
                te.sendMultiple();
            }
            catch (SendFailedException e)
            {
                success = false;
            }            
        }
        
        return success;
    }

    private static void fileUser(Map userLocaleMap, ScarabUser user, 
                                 Module module, int toOrCC)
    {
        Locale locale = chooseLocale(user, module);
        List[] toAndCC = (List[])userLocaleMap.get(locale);
        if (toAndCC == null) 
        {
            toAndCC = new List[2];
            toAndCC[0] = new ArrayList();
            toAndCC[1] = new ArrayList();
            userLocaleMap.put(locale, toAndCC);
        }
        toAndCC[toOrCC].add(user);
    }

    /**
     * Single user recipient.
     */ 
    public static boolean sendEmail(EmailContext context, Module module,
                                     Object fromUser, Object replyToUser, 
                                     ScarabUser toUser, String template)
        throws Exception
    {
        Collection toUsers = new ArrayList(2);
        toUsers.add(toUser);
        return sendEmail(context, module, fromUser, replyToUser, toUsers, 
                          null, template);
    }


    private static TemplateEmail getTemplateEmail(EmailContext context,
        Object fromUser, Object replyToUser, String template)
        throws Exception
    {
        TemplateEmail te = new TemplateEmail();
        if (context == null) 
        {
            context = new EmailContext();
        }        
        te.setContext(context);
        
        if (fromUser instanceof ScarabUser)
        {
            ScarabUser u = (ScarabUser)fromUser;
            te.setFrom(u.getName(), u.getEmail());
        }
        else if (fromUser instanceof String[])
        {
            String[] s = (String[])fromUser;
            te.setFrom(s[0], s[1]);
        }
        else
        {
            // assume string
            String key = (String)fromUser;      
            if (fromUser == null)
            {
                key = "scarab.email.default";
            } 
            
            te.setFrom(Turbine.getConfiguration().getString
                       (key + ".fromName", "Scarab System"), 
                       Turbine.getConfiguration().getString
                       (key + ".fromAddress",
                        "help@localhost"));
        }

        if (replyToUser instanceof ScarabUser)
        {
            ScarabUser u = (ScarabUser)replyToUser;
            te.addReplyTo(u.getName(), u.getEmail());
        }
        else if (replyToUser instanceof String[])
        {
            String[] s = (String[])replyToUser;
            te.addReplyTo(s[0], s[1]);
        }
        else
        {
            // assume string
            String key = (String)replyToUser;       
            if (fromUser == null)
            {
                key = "scarab.email.default";
            } 
            
            te.addReplyTo(Turbine.getConfiguration()
                          .getString(key + ".fromName", "Scarab System"), 
                          Turbine.getConfiguration()
                          .getString(key + ".fromAddress",
                                     "help@localhost"));
        }
        
        if (template == null)
        {
            template = Turbine.getConfiguration().
                getString("scarab.email.default.template");
        }
        te.setTemplate(prependDir(template));
    
        String subjectTemplate = context.getSubjectTemplate();
        if (subjectTemplate == null) 
        {
            StringBuffer templateSB = 
                new StringBuffer(template.length() + 7);
            templateSB.append(
                template.substring(0, template.length()-3));
            subjectTemplate = templateSB.append("Subject.vm").toString();
        }

        te.setSubject(getSubject(context, subjectTemplate));
        return te;
    }

    private static String getSubject(TemplateContext context, String template)
    {
        template = prependDir(template);
        String result = null;
        try 
        {            
            result = TurbineTemplate
                .handleRequest(context, template).trim();
            String subject = (String)context.get("emailSubject");
            if (subject != null) 
            {
                result = subject;
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
            b = GlobalParameterManager.getBoolean(
                GlobalParameter.EMAIL_INCLUDE_ISSUE_DETAILS);
        }
        catch (Exception e)
        {
            Log.get().debug("", e);
            // use the basic email
        }
        return b ? "email/" + template : "basic_email/" + template;
    }

    /**
     * Returns a charset for the given locale that is generally preferred
     * by email clients.
     *
     * @param locale a <code>Locale</code> value
     * @return a <code>String</code> value
     */
    private static String getCharset(Locale locale)
    {
        String charset = TurbineMimeTypes.getCharSet(locale);
        if ("ja".equals(locale.getLanguage())) 
        {
            charset = "ISO-2022-JP";
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
                locale = user.getLocale();
            }
            catch (Exception e)
            {
                Log.get().error("Couldn't determine locale for user " 
                                + user.getUserName(), e);
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
