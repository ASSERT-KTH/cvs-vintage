/* ================================================================
 * Copyright (c) 2004 CollabNet.  All rights reserved.
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
package org.tigris.scarab.migration.b18b19;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class MigrateProperties extends Task
{
    //
    // Need to initialise this so that we have an object to lock on.
    //
    private static String[] propertyMap = new String[0];
    
    static
    {
        synchronized(propertyMap)
        {
            if (propertyMap.length == 0) {
                //
                // This array maps the old properties (source) to the new
                // ones (target), where the first of each pair is the target
                // and the second is the source.
                //
                // Some of the source properties have a '+' character in them
                // - this means that the text following the '+' should be
                // appended to the property value before being assigned to the
                // target property.
                //
                propertyMap = new String[] {
                    // Target prop.          Source prop.
                    
                    // Scarab.properties
                    "scarab.site.name", "scarab.site.name",
                    "scarab.http.domain", "scarab.http.domain",
                    "scarab.http.scheme", "scarab.http.scheme",
                    "scarab.http.scriptname", "scarab.http.scriptname",
                    "scarab.http.port", "scarab.http.port",
                    "scarab.automatic.role.approval", "scarab.automatic.role.approval",
                    "scarab.timezone", "scarab.timezone",
                    "scarab.email.encoding", "scarab.email.encoding",
                    "scarab.email.default.fromName", "scarab.email.default.fromName",
                    "scarab.email.default.fromAddress", "scarab.email.default.fromAddress",
                    "scarab.register.email.checkValidA", "scarab.register.email.checkValidA",
                    "scarab.register.email.badEmails", "scarab.register.email.badEmails",
                    "scarab.email.register.fromName", "scarab.email.register.fromName",
                    "scarab.email.register.fromAddress", "scarab.email.register.fromAddress",
                    "scarab.email.forgotpassword.fromName", "scarab.email.forgotpassword.fromName",
                    "scarab.email.forgotpassword.fromAddress", "scarab.email.forgotpassword.fromAddress",
                    "scarab.attachments.repository", "scarab.attachments.path",
                    "searchindex.path", "scarab.lucene.index.path",
                    "services.TorqueService.classname", "scarab.torque.service",
                    "services.DatabaseInitializer.classname", "scarab.dbinit.service",
                    "torque.managed_class.org.tigris.scarab.om.Module.manager", "scarab.module.service",
                    "torque.managed_class.org.tigris.scarab.om.ScarabUser.manager", "scarab.user.service",
                    "scarab.dataexport.encoding", "scarab.dataexport.encoding",
                    
                    // TurbineResources.properties
                    "turbine.mode", "scarab.mode",
                    "template.homepage", "scarab.homepage",
                    "session.timeout", "scarab.session.timeout",
                    "system.mail.host", "scarab.system.mail.host",
                    "services.LocalizationService.locale.default.language", "scarab.locale.default.language",
                    "services.LocalizationService.locale.default.country", "scarab.locale.default.country",
                    "services.UploadService.repository", "scarab.file.upload.path",
                    "services.UploadService.size.max", "scarab.file.max.size",
                    "resolver.cache.template", "scarab.template.cache",
                    "resolver.cache.module", "scarab.template.cache",
                    "services.VelocityService.file.resource.loader.cache", "scarab.template.cache",
                    "services.EmailService.file.resource.loader.cache", "scarab.template.cache",
                    "services.VelocityService.file.resource.loader.path", "template.path+/templates",
                    "services.EmailService.file.resource.loader.path", "template.path+/templates",
                    "module.packages", "scarab.module.packages",
                    
                    "services.LocalizationService.classname", "scarab.localization.service",
                    "torque.manager.useCache", "scarab.torque.manager.cache",
                    "action.sessionvalidator", "scarab.sessionvalidator",
                    "pipeline.default.descriptor", "scarab.default.pipeline.descriptor",
                    "exceptionHandler.default", "scarab.request.error.handler",
                    "services.PullService.tool.request.link", "scarab.pull.link",
                    "services.PullService.tool.request.staticLink", "scarab.pull.staticlink",
                    "services.SecurityService.user.manager", "scarab.security.user.manager",
                    "services.IntakeService.serialize.path", "scarab.intake.serialize.file",
                    
                    "log4j.category.default", "scarab.log.level.turbine+, turbine",
                    "log4j.category.org.tigris.scarab", "scarab.log.level.scarab+, scarab",
                    "log4j.appender.scarab.file", "scarab.log.file.scarab",
                    "log4j.appender.scarab.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.scarab.append", "scarab.log.append.scarab",
                    "log4j.category.org.tigris.scarab.util.xmlissues", "scarab.log.level.scarabxmlimport+, scarabxmlimport",
                    "log4j.appender.scarabxmlimport.file", "scarab.log.file.scarabxmlimport",
                    "log4j.appender.scarabxmlimport.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.scarabxmlimport.append", "scarab.log.append.scarabxmlimport",
                    "log4j.category.org.apache.turbine", "scarab.log.level.turbine+, turbine",
                    "log4j.appender.turbine.file", "scarab.log.file.turbine",
                    "log4j.appender.turbine.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.turbine.append", "scarab.log.append.turbine",
                    "log4j.category.org.apache.torque", "scarab.log.level.torque+, torque",
                    "log4j.appender.torque.file", "scarab.log.file.torque",
                    "log4j.appender.torque.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.torque.append", "scarab.log.append.torque",
                    "log4j.category.org.apache.fulcrum", "scarab.log.level.fulcrum+, services",
                    "log4j.appender.services.file", "scarab.log.file.fulcrum",
                    "log4j.appender.services.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.services.append", "scarab.log.append.fulcrum",
                    "log4j.category.org.apache.stratum", "scarab.log.level.stratum+, stratum",
                    "log4j.appender.stratum.file", "scarab.log.file.stratum",
                    "log4j.appender.stratum.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.stratum.append", "scarab.log.append.stratum",
                    "log4j.category.org.apache.jcs", "scarab.log.level.jcs+, jcs",
                    "log4j.appender.jcs.file", "scarab.log.file.jcs",
                    "log4j.appender.jcs.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.jcs.append", "scarab.log.append.jcs",
                    "log4j.category.org.apache.fulcrum.db", "scarab.log.level.torque+, torque",
                    "log4j.category.org.apache.commons", "scarab.log.level.turbine+, turbine",
                    "log4j.category.org.apache.commons.beanutils", "scarab.log.level.beanutils+, turbine",
                    "log4j.category.org.apache.velocity", "scarab.log.level.velocity+, velocity",
                    "log4j.appender.velocity.file", "scarab.log.file.velocity",
                    "log4j.appender.velocity.layout.conversionPattern", "scarab.log.pattern",
                    "log4j.appender.velocity.append", "scarab.log.append.velocity"
                };
            }
        }
    }
    
    private String outputFile = "CustomSettings.properties";
    
    public void execute() throws BuildException
    {
        PrintWriter writer = null;
        try
        {
            //
            // Open the output file for writing.
            //
            writer = new PrintWriter(
                         new BufferedOutputStream(
                             new FileOutputStream(this.outputFile)));
            
            //
            // Now iterate through each property in the map, check whether it
            // exists within the ant properties, and if so add it to the output
            // file.
            //
            for (int i = 0; i < propertyMap.length; i += 2)
            {
                //
                // Work out the name of the ant property associated with
                // this output property. The value from the map may contain
                // a '+' character which separates the ant property name
                // from the string that should be appended to its value
                // when it is written to the output file.
                //
                String outProperty = propertyMap[i];
                String inProperty = propertyMap[i + 1];
                String appendString = "";
                int splitPos = inProperty.indexOf('+');
                if (splitPos >= 0)
                {
                    appendString = inProperty.substring(splitPos + 1);
                    inProperty = inProperty.substring(0, splitPos);
                }
                
                String antPropertyValue = getProject().getProperty(inProperty);
                if (antPropertyValue != null)
                {
                    //
                    // The ant property may contain a '=' character due
                    // to the way the property has been set up to work
                    // with filtered copies. In these cases, only the
                    // part of the value after the '=' needs to be written
                    // to the output file.
                    //
                    splitPos = antPropertyValue.indexOf('=');
                    if (splitPos >= 0)
                    {
                        antPropertyValue =
                            antPropertyValue.substring(splitPos + 1);
                    }
                    
                    //
                    // Now we have all the information we need, so write the
                    // property to the output file.
                    //
                    writer.println(outProperty + '='
                                   + antPropertyValue
                                   + appendString);
                }
            }
        }
        catch (IOException ex)
        {
            throw new BuildException(ex);
        }
        finally
        {
            writer.close();
        }
    }
    
    public void setOutput(String filename)
    {
        this.outputFile = filename;
    }
}
