package org.tigris.scarab.screens;

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


// Turbine Stuff 
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.torque.om.NumberKey;

// Scarab Stuff
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;

/**
 * Sends file contents directly to the output stream.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ViewAttachment.java,v 1.11 2003/02/04 11:26:02 jon Exp $
 */
public class ViewAttachment extends Default
{
    /**
     * builds up the context for display of variables on the page.
     */
    public void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception 
    {
        super.doBuildTemplate(data, context);
        
        String attachId = data.getParameters().getString("attachId");
        Attachment attachment = AttachmentManager
            .getInstance(new NumberKey(attachId), false);
        String contentType = attachment.getMimeType();
        HttpServletResponse res = data.getResponse();
        res.setContentType(contentType);
        // disposition header does not seem to have that much value, but
        // adding it in a blind attempt to help win2K, IE client
        // to handle the downloads better
        if (!contentType.startsWith("text")) 
        {
            res.setHeader("Content-Disposition", 
                          "attachment; filename=" + attachment.getFileName());
        }
        
        File f = new File(attachment.getFullPath());
        res.setContentLength((int)f.length());

        BufferedInputStream bis = null;
        OutputStream os = data.getResponse().getOutputStream();
        try
        {
            bis = new BufferedInputStream(new FileInputStream(f));
            byte[] bytes = new byte[2048];
            int s = 0;
            while ((s = bis.read(bytes)) != -1)
            {
                try
                {
                    os.write(bytes,0,s);
                }
                catch (java.io.IOException ioe)
                {
                    Log.get().debug("File download was aborted: " + 
                                    attachment.getFullPath());
                    break;
                }
            }
        }
        finally
        {
            if (bis != null)
            {
                bis.close();
            }
        }

        // we already sent the response, there is no target to render
        data.setTarget(null);
    }
}

