package org.tigris.scarab.tools;

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
import java.util.Properties;

import org.apache.turbine.Log;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.LocalizationTool;

/**
 * Scarab-specific localiztion tool.  Makes use of the property
 * format:
 *
 * <blockquote><code><pre>
 * [dir/]<scope>.<title>
 * </pre></code></blockquote>
 *
 * Defaults for scope can be specified using the
 * <code>default.somevar</code> syntax, where <code>somevar</code> is
 * the variable you want to specify a default scope for.
 *
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 */
public class ScarabLocalizationTool
    extends LocalizationTool
{
    /**
     * The portion of a key denoting the default scope (the default
     * target name, for instance).
     */
    private static final String DEFAULT_SCOPE = "default";

    /**
     * The portion of a key denoting the 
     */
    private static final String TITLE_PROP = "title";

    /**
     * We need to keep a reference to the request's
     * <code>RunData</code> so that we can extract the name of the
     * target <i>after</i> the <code>Action</code> has run (which may
     * have changed the target from its original value as a sort of
     * internal redirect).
     */
    private RunData data;

    /**
     * Initialized by <code>init()</code>, cleared by
     * <code>refresh()</code>.
     */
    private Map properties;

    /**
     * Creates a new instance.
     */
    public ScarabLocalizationTool()
    {
        super();

        // FIXME: Remove this hard coding
        properties = new Properties();
        //properties.put(DEFAULT_SCOPE + '.' + TITLE_PROP, "DefaultTitle");
    }

    /**
     * Provides <code>$l10n.Title</code> to templates, grabbing it
     * from the <code>title</code> property for the current template.
     *
     * @return The title for the template used in the current request,
     * or the text <code>Scarab</code> if not set.
     */
    public String getTitle()
    {
        String title = null;

        if (properties != null)
        {
            // $l10n.get($props.get($template, "title"))
            // HELP: Not sure if values like "entry/Wizard1.vm.title"
            // will be valid keys for Java .properties files...
            String templateName = data.getTarget();
            if (templateName == null)
            {
                templateName = DEFAULT_SCOPE;
            }
            String propName = templateName + '.' + TITLE_PROP;
            String l10nKey = (String) properties.get(propName);
            Log.debug("ScarabLocalizationTool: Property name '" + propName +
                      "' -> localization key '" + l10nKey + '\'');
            if (l10nKey != null)
            {
                title = get(l10nKey);
            }
        }

        if (title == null)
        {
            // Either the property name doesn't correspond to a
            // localization key, or the localization property pointed
            // to by the key doesn't have a value.
            // TODO: Supply a *localized* default.
            title = "Scarab";
        }

        return title;
    }


    // ---- ApplicationTool implementation  ----------------------------------

    public void init(Object runData)
    {
        super.init(runData);
        if (runData instanceof RunData)
        {
            data = (RunData) runData;
            // TODO: Use Zope like property overlays (must implement
            // them first in Turbine).
            //properties = ;
        }
    }

    public void refresh()
    {
        super.refresh();
        data = null;
        properties = null;
    }
}
