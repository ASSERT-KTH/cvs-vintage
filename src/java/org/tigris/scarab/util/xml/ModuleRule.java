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
import org.xml.sax.Attributes;

import org.apache.commons.digester.Rule;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Category;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.om.ScarabModule;
import org.tigris.scarab.services.module.ModuleManager;

/**
 * Handler for xpath "scarab/module"
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class ModuleRule extends BaseRule
{
    public ModuleRule(Digester digester, String state, 
                      DependencyTree dependTree)
    {
        super(digester, state, dependTree);
    }
    
    /**
     * This method is called when the beginning of a matching XML element
     * is encountered.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(Attributes attributes) throws Exception
    {
        cat.debug("(" + state + ") module begin()");
        if(state.equals(DBImport.STATE_DB_INSERTION))
        {
            doInsertionAtBegin(attributes);
        }
        else if (state.equals(DBImport.STATE_DB_VALIDATION))
        {
            doValidationAtBegin(attributes);
        }
    }
    
    private void doInsertionAtBegin(Attributes attributes) throws Exception
    {
        ScarabModule module = null;
        
        // try to find the module
        try
        {
            module = (ScarabModule)ModuleManager
                .getInstance(new NumberKey(attributes.getValue("id")));
        }
        catch (Exception e)
        {
            module = (ScarabModule)ModuleManager.getInstance();
        }
        module.setParentId(attributes.getValue("parent"));
        module.setOwnerId("0");
        digester.push(module);
    }
    
    private void doValidationAtBegin(Attributes attributes) throws Exception
    {
        digester.push(attributes.getValue("id"));
        
        try
        {
            ScarabModule parentModule = (ScarabModule)ModuleManager
                .getInstance(new NumberKey(attributes.getValue("parent")));
        }
        catch (Exception e)
        {
            // store it for check later in file
            dependTree.addModuleDependency(
                new NumberKey(attributes.getValue("id")), 
                new NumberKey(attributes.getValue("parent")));
        }
    }
    
    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     */
    public void end() throws Exception
    {
        cat.debug("(" + state + ") module end()");
        if(state.equals(DBImport.STATE_DB_INSERTION))
        {
            ScarabModule module = (ScarabModule)digester.pop();
        }
        else if (state.equals(DBImport.STATE_DB_VALIDATION))
        {
            String moduleCode = (String)digester.pop();
        }
    }
}
