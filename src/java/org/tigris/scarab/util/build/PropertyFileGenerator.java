package org.tigris.scarab.util.build;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * This class is used as ant task backend for the generation
 * of a property file by use of a template file.
 *
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 * @version $Id: PropertyFileGenerator.java,v 1.2 2004/11/06 15:25:34 dabbous Exp $
 */

public class PropertyFileGenerator
{
    /**
     * This file will be read in  and all property values
     * in it will be replaced by the actual values as they
     * are in the online environment.
     */
    private File templateFile;

    /**
     * This is the file, which shall contain the
     * final property name/value pairs. It's overall
     * layout is similar to the templateFile, but the
     * property values are generated from the current 
     * online property settings.
     */
    private File customFile;
    
    /**
     * Setter: set the path to the template file.
     * Throws an exception, if the template file does not exist.
     * @param theTemplatePath
     */
    public boolean setTemplate(String theTemplatePath)
    {
        boolean status = true;
        templateFile = new File(theTemplatePath);
        if(!templateFile.exists())
        {
            status = false;
        }
        return status;
    }
    
    /**
     * Return the absolute path to the templateFile, or null, if
     * no template file has been set.
     * @return
     */
    public String getTemplate()
    {
        return (templateFile==null)? null:templateFile.getAbsolutePath();
    }

    /**
     * Setter: set the path to the final property file.
     * Throws an exception, if the customFile exist, 
     * but can't be overwritten (due to permission settings).
     * @param theCustomPath
     */
    public boolean setCustom(String theCustomPath)
    {
        boolean status = true;
        customFile = new File(theCustomPath);
        if(!customFile.getParentFile().exists())
        {
            customFile.getParentFile().mkdir();
        }
        
        if(customFile.exists() &&
           !customFile.canWrite())
        {
            status = false;
        }
        
        return status;
    }

    /**
     * Return the absoute path to the customFile, or null, if no path
     * has been set.
     * @return
     */
    public String getCustom()
    {
        return (customFile==null)? null:customFile.getAbsolutePath();
    }
    
    /**
     * Read the templateFile and behave according to 
     * following rule set:
     * <ul>
     * <li> rule 1: Copy every line, which does NOT contain
     *      a property verbatim to the customFile.</li>
     * 
     * <li> rule 2: Retrieve the current online value of each 
     *      property found in the templateFile and generate an 
     *      appropriate name/value pair in the customFile.</li>
     * 
     * <li> rule 3: If a property value starts with a "${" in 
     *      the templateFile, keep the value as is. By this we 
     *      can propagate ${variables} to the customFile, which 
     *      will be resolved during startup of Scarab.</li>
     * </ul>
     * 
     */
    public void execute(PropertyGetter props) throws IOException {
        
 
            Reader reader = new FileReader(templateFile);
            Writer writer = new FileWriter(customFile);
            BufferedReader br = new BufferedReader(reader);
            PrintWriter    pw = new PrintWriter(writer);

            String line;

            while((line=br.readLine()) != null)
            {
                String trimmedLine = line.trim();
                if (  trimmedLine.equals("")
                    ||trimmedLine.startsWith("#") )
                {
                    pw.println(line);
                }
                else 
                {
                    String name  = null;
                    String value = null;
                    String resultLine;
                    int index = line.indexOf("=");
                    if(index >=0)
                    {
                        name  = line.substring(0,index).trim();
                        value = line.substring(index+1).trim();
                        int beginOfValue = line.indexOf(value,index+1);
                        resultLine = (beginOfValue == -1) ?
                                     line : line.substring(0,beginOfValue);
                    }
                    else
                    {   
                        name  = line.trim();
                        value = "";
                        int endOfLine = line.indexOf(name)+name.length();
                        resultLine = ((endOfLine == -1) ? line : line.substring(0,endOfLine)) + " = ";
                    }
                    
                    Object newValue = props.getProperty(name,value);

                    if(newValue == null)
                    {
                        newValue = "";
                    }
                    
                    if(newValue.equals(""))
                    {
                        // this is a temporary hack.
                        // I need this for convenience at the moment [HD]
                        // it will be removed when the setup wizzard is running
                        name += "." + props.getProperty("scarab.database.type","");
                        newValue = props.getProperty(name,"");
                        if(!newValue.equals(""))
                        {
                            newValue="${"+name+"}";
                        }
                    }

                    if (newValue.equals(value))
                    {
                        resultLine = line;
                    }
                    else
                    {
                        resultLine += newValue;
                    }

                    pw.println(resultLine);

                
                }
            }
            pw.close();
            br.close();
        
    }
}
