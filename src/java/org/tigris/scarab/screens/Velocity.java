package org.tigris.scarab.screens;

import java.util.Iterator;
import org.apache.turbine.TemplateSecureScreen;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.Module;
import org.apache.velocity.VelocityContext;

public class Velocity extends TemplateSecureScreen
{
    /**
     * builds up the context for display of variables on the page.
     */
    public void doBuildTemplate( RunData data, TemplateContext context )
        throws Exception 
    {
        VelocityContext vc = new VelocityContext();
        Iterator keys = context.keySet().iterator();
        while (keys.hasNext()) 
        {
            String key = (String)keys.next();
            vc.put(key, context.get(key));
        }
        
        data.getResponse().setContentType("text/html; charset=EUC-KR");
        org.apache.velocity.app.Velocity
            .mergeTemplate("screens/VTest.vm",
                           vc, data.getResponse().getWriter());

        Module.handleRequest(context, "screens/VTest.vm", data.getOut());

        // we already sent the response, there is no target to render
        data.setTarget("VTest.vm");
    }

    public boolean isAuthorized(RunData data)
    {
        return true;
    }
}
