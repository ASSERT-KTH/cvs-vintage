//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.externaltools;

import org.columba.core.plugin.Plugin;

import java.io.File;

import java.net.URL;


/**
 * Provides easy configuration for external tools.
 *
 * @author fdietz
 */
public abstract class AbstractExternalToolsPlugin implements Plugin {
    public AbstractExternalToolsPlugin() {
    }

    /**
 * Gets the path to the commandline tool.
 * <p>
 * If this method returns "null", a dialog will ask
 * the user the location of the tool
 *
 * @return        path to external tool, or "null" if tool can't be located automatically
 */
    public File locate() {
        return null;
    }

    /**
 * Gets description which should be presented to the user.
 *
 * @return        description of external tool
 */
    public abstract String getDescription();

    /**
 * Gets web address of this tool.
 * <p>
 * This will be presented to the user as clickable Hyperlink in
 * the configuration wizard.
 *
 * @return        website of this tool
 */
    public abstract URL getWebsite();
}
