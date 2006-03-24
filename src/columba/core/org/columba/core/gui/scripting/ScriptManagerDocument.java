/*

The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License") you may not use this file except in compliance with the License. 

You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

The Original Code is "BshInterpreter plugin for The Columba Project"

The Initial Developer of the Original Code is Celso Pinto
Portions created by Celso Pinto are Copyright (C) 2005.
Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

All Rights Reserved.

*/
package org.columba.core.gui.scripting;


import java.util.List;

import org.columba.core.scripting.IScriptsObserver;
import org.columba.core.scripting.model.ColumbaScript;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public interface ScriptManagerDocument
{

    public void removeScript(ColumbaScript[] scripts);

    public void refreshScriptList();

    public ColumbaScript getScript(String path);

    public List getScripts();

    public void addObserver(IScriptsObserver obs);

    public void removeObserver(IScriptsObserver obs);

}
