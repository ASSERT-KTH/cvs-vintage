package org.columba.core.scripting;

import org.columba.core.plugin.PluginInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractInterpreter implements PluginInterface {

	abstract public Object instanciate(
		String fileName,
		String className,
		Object[] args,
		Object parent);

}
