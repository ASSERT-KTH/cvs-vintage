package org.tigris.scarab.util.xml;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
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
 * individuals on behalf of Collab.Net.
 */ 

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.text.SimpleDateFormat;

import org.apache.log4j.Category;

import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Issue.FederatedId;
import org.tigris.scarab.om.ScarabModule;

import org.tigris.scarab.services.module.ModuleManager;
import org.tigris.scarab.services.user.UserManager;

import org.tigris.scarab.util.TurbineInitialization;

/**
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @version $Id: XMLExport.java,v 1.2 2001/12/13 00:38:23 jon Exp $
 */
public class XMLExport
{
    private static final String USAGE = "Usage: XMLExport [<federatedidstart1>-<federatedidend1> | <federatedid1>][,<federatedidstart2>-<federatedidend2> | ,<federatedid2>][,<federatedidstart3>-<federatedidend3> | ,<federatedid3>]...";
    private static final String format = "yyyy-MM-dd HH:mm:ss";
    
    private static Category cat = Category.getInstance(org.tigris.scarab.util.xml.DBImport.class);
    private static boolean initialized = false;
    
    public XMLExport()
    {
    }
    
    public static void main (String[] args) throws Exception
    {
        // FIXME: should be a nicer way to accomplish this.
        // this simply determines the directory where this class resides.
        // working in the directory, we can relatively place ourselves to
        // where we need to be to configure turbine for startup.
        String path = new File(DBImport.class.getResource("XMLExport.class").getFile()).getParent();
        String configDir;
        if (path.indexOf("target") == -1)
        {
            configDir = path + "/../../../../../../../target/webapps/scarab";
        }
        else
        {
            configDir = path + "/../../../../../../../";
        }
        
        //args = new String[] {"PACS1,TBNS1-TBNS130"};
        
        XMLExport exporter = new XMLExport();
        TurbineInitialization.setUp(configDir, "/xmlexport.properties");
        
        // validate input
        if (args.length != 1)
        {
            cat.error(USAGE);
            return;
        }
        
        FederatedId[] fids = exporter.parseIssueList(args[0]);
        String export = exporter.buildXMLExport(fids);
        
        cat.debug("\n\n\n" + export);
    }
    
    public String buildXMLExport(FederatedId[] fids)
        throws Exception
    {
        StringBuffer results = new StringBuffer();
        ScarabModule currentModule = null;
        
        results.append(getXMLScarabHeader());
        for (int i = 0; i < fids.length; i++)
        {
            Issue issue = Issue.getIssueById(fids[i]);
            if (issue == null)
            {
                cat.warn("Issue does not exist: " + fids[i].getPrefix() + fids[i].getCount());
            }
            else
            {
                ScarabModule module = (ScarabModule)issue.getModule();
                if (currentModule == null)
                {
                    // just starting
                    results.append(getXMLModuleHeader(module));
                    currentModule = module;
                }
                else if (currentModule.getModuleId() != module.getModuleId())
                {
                    results.append(getXMLModuleFooter());
                    results.append(getXMLModuleHeader(module));
                    currentModule = module;
                }
                results.append(getXMLIssue(issue));
            }
        }
        if (currentModule != null)
        {
            results.append(getXMLModuleFooter());
        }
        results.append(getXMLScarabFooter());
        
        return results.toString();
    }
    
    private static String getXMLScarabHeader()
    {
        return "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
            "<!DOCTYPE scarab SYSTEM \"scarab.dtd\">\n" +
            "<scarab>\n";
    }
    
    private static String getXMLScarabFooter()
    {
        return "</scarab>";
    }
    
    private static String getXMLModuleHeader(ScarabModule module)
    {
        return "\t<module id=\"" + module.getModuleId() +
            "\" parent=\"" + module.getParentId() + "\">\n" +
            "\t\t<name>" + module.getRealName() + "</name>\n" +
            "\t\t<code>" + module.getCode() + "</code>\n";
    }
    
    private static String getXMLModuleFooter()
    {
        return "\t</module>\n";
    }
    
    private static String getXMLIssue(Issue issue)
        throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        StringBuffer results = new StringBuffer();
        
        results.append("\t\t<issue id=\"" + issue.getIdCount() + "\">\n" +
                           "\t\t\t<artifact-type>" + issue.getIssueType().getName() + "</artifact-type>\n" +
                           "\t\t\t<committed-by>" + issue.getCreatedBy().getUserName() + "</committed-by>\n");
        
        // attributes
        Vector attributeValues = issue.getAttributeValues();
        Enumeration enum = attributeValues.elements();
        while (enum.hasMoreElements())
        {
            AttributeValue attributeValue = (AttributeValue)enum.nextElement();
            results.append("\t\t\t<issue-attribute>\n" +
                               "\t\t\t\t<name>" + attributeValue.getAttribute().getName() + "</name>\n" +
                               "\t\t\t\t<value>" + attributeValue.getValue() + "</value>\n" +
                               "\t\t\t\t<type>" + attributeValue.getAttribute().getAttributeType().getName() + "</type>\n" +
                               "\t\t\t</issue-attribute>\n");
        }
        // child dependencies
        Vector parents = issue.getDependsRelatedByObserverId();
        enum = parents.elements();
        while (enum.hasMoreElements())
        {
            Depend depend = (Depend)enum.nextElement();
            results.append("\t\t\t<dependency>\n" +
                               "\t\t\t\t<type>" + depend.getDependType().getName() + "</type>\n" +
                               "\t\t\t\t<parent>" + depend.getIssueRelatedByObservedId().getFederatedId() + "</parent>\n" +
                               "\t\t\t</dependency>\n");
        }
        // parent dependencies
        Vector children = issue.getDependsRelatedByObservedId();
        enum = children.elements();
        while (enum.hasMoreElements())
        {
            Depend depend = (Depend)enum.nextElement();
            results.append("\t\t\t<dependency>\n" +
                               "\t\t\t\t<type>" + depend.getDependType().getName() + "</type>\n" +
                               "\t\t\t\t<child>" + depend.getIssueRelatedByObserverId().getFederatedId() + "</child>\n" +
                               "\t\t\t</dependency>\n");
        }
        // attachments
        Vector attachments = issue.getAttachments();
        enum = attachments.elements();
        while (enum.hasMoreElements())
        {
            Attachment attachment = (Attachment)enum.nextElement();
            results.append("\t\t\t<attachment>\n" +
                               "\t\t\t\t<name>" + attachment.getName() + "</name>\n" +
                               "\t\t\t\t<type>" + attachment.getAttachmentType().getName() + "</type>\n" +
                               "\t\t\t\t<path>" + attachment.getFilePath() + "</path>\n" +
                               "\t\t\t\t<data>" + attachment.getDataAsString() + "</data>\n" +
                               "\t\t\t\t<mimetype>" + attachment.getMimeType() + "</mimetype>\n" +
                               "\t\t\t\t<created-date format=\"" + format + "\">" + sdf.format(attachment.getCreatedDate()) + "</created-date>\n" +
                               "\t\t\t\t<modified-date format=\"" + format + "\">" + sdf.format(attachment.getModifiedDate()) + "</modified-date>\n" +
                               "\t\t\t\t<created-by>" + UserManager.getInstance(attachment.getCreatedBy()).getUserName() + "</created-by>\n" +
                               "\t\t\t\t<modified-by>" + UserManager.getInstance(attachment.getModifiedBy()).getUserName() + "</modified-by>\n" +
                               "\t\t\t</attachment>\n");
        }
        
        results.append("\t\t</issue>\n");
        
        return results.toString();
    }
    
    /**
     * Parses a list of issues.
     *
     * @param issueList a comma separated list of federated id's and federated id ranges.
     * @return an <code>ArrayList</code> of <code>FederatedId</code>
     */
    public FederatedId[] parseIssueList(String issueList)
        throws Exception
    {
        String[] issues = split(issueList, ",");
        int resultsSize = issues.length;
        ArrayList results = new ArrayList(resultsSize);
        for (int i = 0; i < issues.length; i++)
        {
            if (issues[i].indexOf("-") == -1)
            {
                addFederatedId(results, new FederatedId(issues[i]));
            }
            else
            {
                String[] issue = split(issues[i], "-");
                if (issue.length != 2)
                {
                    throw new Exception("Federated id range not valid: " + issues[i]);
                }
                FederatedId fidStart = new FederatedId(issue[0]);
                FederatedId fidStop = new FederatedId(issue[1]);
                if (!fidStart.getPrefix().equals(fidStop.getPrefix()))
                {
                    throw new Exception("Federated id prefix does not match: " + issues[i]);
                }
                if (fidStart.getCount() > fidStop.getCount())
                {
                    throw new Exception("Federated id range not valid: " + issues[i]);
                }
                resultsSize += fidStop.getCount() - fidStart.getCount() + 1;
                results.ensureCapacity(resultsSize);
                addFederatedId(results, fidStart);
                for (int j = fidStart.getCount() + 1; j < fidStop.getCount(); j++)
                {
                    addFederatedId(results, new FederatedId(fidStart.getPrefix() + j));
                }
                addFederatedId(results, fidStop);
            }
        }
        
        return (FederatedId[])results.toArray(new FederatedId[results.size()]);
    }
    
    /**
     * Adds the specified federated id to the array list
     */
    private static void addFederatedId(ArrayList al, FederatedId fid)
        throws Exception
    {
        String fidPrefix = fid.getPrefix();
        int fidCount = fid.getCount();
        Iterator iter = al.iterator();
        while (iter.hasNext())
        {
            FederatedId test = (FederatedId)iter.next();
            if (test.getPrefix().equals(fidPrefix) && test.getCount() == fidCount)
            {
                throw new Exception("Federated id already specified: " + fid.getPrefix() + fid.getCount());
            }
        }
        al.add(fid);
    }
    
    /**
     * FIXME: Commons-Util.jar StringUtils should have this method in it already
     */
    private static String[] split(String source, String searchFor)
    {
        ArrayList split = new ArrayList();
        int idx;
        int lastIdx = 0;
        String newStr;
        
        if ((source == null) || (searchFor == null))
        {
            if (source != null)
            {
                split.add(source);
            }
            return (String[])split.toArray(new String[split.size()]);
        }
        
        if (searchFor.equals(""))
        {
            split.add(source);
            return (String[])split.toArray(new String[split.size()]);
        }
        
        idx = source.indexOf(searchFor);
        while (idx != -1)
        {
            newStr = source.substring(lastIdx, idx);
            if (!newStr.trim().equals(""))
            {
                split.add(newStr);
            }
            
            lastIdx = idx + searchFor.length();
            idx = source.indexOf(searchFor, lastIdx);
        }
        
        newStr = source.substring(lastIdx);
        if (!newStr.trim().equals(""))
        {
            split.add(source.substring(lastIdx));
        }
        
        return (String[])split.toArray(new String[split.size()]);
    }
}
