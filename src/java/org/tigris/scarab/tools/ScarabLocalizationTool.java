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

import org.apache.stratum.configuration.Configuration;
import org.apache.turbine.Log;
import org.apache.turbine.RunData;
import org.apache.turbine.Turbine;
import org.apache.turbine.tool.LocalizationTool;

/**
 * Scarab-specific localiztion tool.  Uses the following property
 * format to access Turbine's properties (generally defined in
 * <code>Scarab.properties</code>):
 *
 * <blockquote><code><pre>
 * template.[dir/]<scope>.<title>
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
    private Configuration properties;

    /**
     * Creates a new instance.
     */
    public ScarabLocalizationTool()
    {
        super();
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
        String title = findProperty(TITLE_PROP, false);
        if (title == null)
        {
            // Either the property name doesn't correspond to a
            // localization key, or the localization property pointed
            // to by the key doesn't have a value.  Try the default.
            title = findProperty(TITLE_PROP, true);

            // If no default localization this category of template
            // property was available, we return null so the VTL
            // renders literally and the problem can be detected.
        }
        return title;
    }

    /**
     * Retrieves the localized version of the value of
     * <code>property</code>.
     *
     * @param property The name of the property whose value to
     * retrieve.
     * @param useDefaultScope Whether or not to use the default scope
     * (defined by the <code>DEFAULT_SCOPE</code> constant).
     * @return The localized property value.
     */
    protected String findProperty(String property, boolean useDefaultScope)
    {
        String value = null;
        if (properties != null)
        {
            // $l10n.get($props.get($template, "title"))

            String templateName =
                (useDefaultScope ? DEFAULT_SCOPE : data.getTarget());
            if (templateName == null)
            {
                templateName = DEFAULT_SCOPE;
            }
            setPrefix(templateName + '.');

            String propName = "template." + getPrefix(null) + property;
            String l10nKey = (String) properties.getString(propName);
            Log.debug("ScarabLocalizationTool: Property name '" + propName +
                      "' -> localization key '" + l10nKey + '\'');

            if (l10nKey != null)
            {
                value = get(l10nKey);
                Log.debug("ScarabLocalizationTool: Localized value is '" +
                          value + '\'');
            }
        }
        return value;
    }

    // ---- ApplicationTool implementation  ----------------------------------

    /**
     * Sets the localization prefix to the name of the target for the
     * current request plus dot (i.e. <code>Prefix.vm.</code>).
     */
    public void init(Object runData)
    {
        super.init(runData);
        if (runData instanceof RunData)
        {
            data = (RunData) runData;
            properties = Turbine.getConfiguration();
        }
    }

    public void refresh()
    {
        super.refresh();
        data = null;
        properties = null;
    }
}
