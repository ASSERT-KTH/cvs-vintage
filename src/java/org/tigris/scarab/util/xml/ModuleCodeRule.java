package org.tigris.scarab.util.xml;

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

import org.tigris.scarab.util.ScarabException;

import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;

import org.apache.torque.om.NumberKey;

/**
 * Handler for the xpath "scarab/module/code"
 *
 * This class should always be the last element which is processed.
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class ModuleCodeRule extends BaseRule
{
    public ModuleCodeRule(ImportBean ib)
    {
        super(ib);
    }
    
    /**
     * This method is called when the body of a matching XML element
     * is encountered.  If the element has no body, this method is
     * not called at all.
     *
     * @param text The text of the body of this element
     */
    public void body(String text) throws Exception
    {
        log().debug("(" + getImportBean().getState() + 
            ") module code body: " + text);

        Module startModule = getImportBean().getModule();
        Module module = null;
        startModule.setCode(text);
        // try to find the module in the database
        try
        {
            module = ModuleManager
                .getInstance(startModule.getRealName(), startModule.getCode());
            log().debug("(" + getImportBean().getState() + ") module found!");
            // NOTE: don't forget to add additional fields here when
            // more elements are added to the DTD to describe the module.
            // otherwise, they won't get saved into the database when the
            // XML file makes changes to the data.
            module.setDescription(startModule.getDescription());
            module.setOwnerId(startModule.getOwnerId());
            module.setUrl(startModule.getUrl());
            module.setDomain(startModule.getDomain());
        }
        catch (Exception e)
        {
            // once again, assume a new module on error
            log().debug("(" + getImportBean().getState() + ") module not found!");
            module = getImportBean().getModule();
        }
        getImportBean().setModule(module);

        super.doInsertionOrValidationAtBody(text);
    }

    protected void doInsertionAtBody(String moduleCode)
        throws Exception
    {
        Module module = getImportBean().getModule();
        module.save();
        log().debug("(" + getImportBean().getState() + ") module saved!");
    }
    
    protected void doValidationAtBody(String moduleCode)
        throws Exception
    {
        Module module = getImportBean().getModule();
        //make sure the existing module has the same code
        String existingModuleCode = module.getCode();
        if(existingModuleCode != null && !existingModuleCode.equals(moduleCode))
        {
            throw new Exception("The existing module with module id: " + 
                                    module.getModuleId() + " has module code: " + 
                                    existingModuleCode + 
                                    " which is not same as the import module code: " + 
                                    moduleCode);
        }
    }
}
