package org.tigris.scarab.util.xmlissues;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.BuildException;

/**
 * This is a very thin wrapper around the ImportIssues.java code
 * to make it possible to call this from Ant.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ImportIssuesTask.java,v 1.6 2004/05/10 21:04:50 dabbous Exp $
 */
public class ImportIssuesTask extends MatchingTask
{
    private ImportIssues importIssues = null;

    public ImportIssuesTask()
    {
        importIssues = new ImportIssues();
    }

    public boolean getSendEmail()
    {
        return importIssues.getSendEmail();
    }

    public void setSendEmail(boolean state)
    {
        importIssues.setSendEmail(state);
    }

    public File getXmlFile()
    {
        return importIssues.getXmlFile();
    }

    public void setXmlFile(File xmlFile)
    {
        importIssues.setXmlFile(xmlFile);
    }

    public File getConfigDir()
    {
        return importIssues.getConfigDir();
    }

    public void setConfigDir(File configDir)
    {
        importIssues.setConfigDir(configDir);
    }

    public String getConfigFile()
    {
        return importIssues.getConfigFile();
    }

    public void setConfigFile(String configProps)
    {
        importIssues.setConfigFile(configProps);
    }

    public String getTurbineResources()
    {
        return importIssues.getTurbineResources();
    }

    public void setTurbineResources(String trProps)
    {
        importIssues.setTurbineResources(trProps);
    }

    public void execute() 
        throws BuildException
    {
        try
        {
            importIssues.execute();
        }
        catch (Exception e)
        {
            throw new BuildException(e); //EXCEPTION
        }
    }
}
