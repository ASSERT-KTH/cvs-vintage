/*
 * Created on 03.01.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.tigris.scarab.screens;

import java.util.ArrayList;

import org.apache.turbine.RunData;
import org.apache.turbine.tool.TemplateLink;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabLink;

/**
 * @author hdab
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ModuleSwitchingLink extends ScarabLink
{
    private RunData data;

    public ModuleSwitchingLink(RunData data)
    {
        super();
        this.data = data;
        init((Object)data);
    }
    
    /**
     * override super method and make it public
     */
    public TemplateLink setPage(String moduleId)
    {
        String homePage = null;
        try
        {
            Module module = ModuleManager
                .getInstance(new Integer(moduleId));
            ScarabUser user = (ScarabUser)data.getUser();
            homePage = user.getHomePage(module);
        }
        catch (Exception e)
        {
            Log.get().error("Could not determine homepage", e);
            homePage = "Index.vm";
        }
        TemplateLink link = super.setPage(homePage, moduleId);
        return link;
    }
    
    /**
     * Add a key value pair (in the form of a 2 object array) to the provided
     * list
     *
     * @param list List to add to.
     * @param name A String with the name to add.
     * @param value A String with the value to add.
     */
    protected void addPair(ArrayList list,
                           String name,
                           String value)
    {
        int hah = 0;
        Object[] tmp = new Object[2];

        tmp[0] = data.getParameters().convertAndTrim(name);
        tmp[1] = value;

        list.add(tmp);
    }
    
}

