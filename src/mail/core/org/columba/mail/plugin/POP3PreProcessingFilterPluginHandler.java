/*
 * Created on Apr 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.plugin;

import org.columba.core.plugin.AbstractPluginHandler;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class POP3PreProcessingFilterPluginHandler extends AbstractPluginHandler {
    /**
 * @param id
 * @param config
 */
    public POP3PreProcessingFilterPluginHandler() {
        super("org.columba.mail.pop3preprocessingfilter",
            "org/columba/mail/plugin/pop3preprocessingfilter.xml");

        parentNode = getConfig().getRoot().getElement("pop3preprocessingfilterlist");
    }
}
