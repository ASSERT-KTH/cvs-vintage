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
package org.columba.mail.filter.plugins;

import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.MessageFolder;

import java.awt.Color;


/**
 * Filter for filtering on a message color.
 * @author redsolo
 */
public class ColorFilter extends AbstractFilter {
    private int defaultColorRGB;
    private int criteriaRGB;
    private int criteriaCondition;

    /**
 * @param f filter containing the configuration.
 */
    public ColorFilter() {
        super();

        defaultColorRGB = Color.black.getRGB();
    }

    /** {@inheritDoc} */
    public boolean process(MessageFolder folder, Object uid) throws Exception {
        int messageRGB = defaultColorRGB;
        Color messageColor = (Color) folder.getAttribute(uid, "columba.color");

        if (messageColor != null) {
            messageRGB = messageColor.getRGB();
        }

        boolean result = false;

        if ((criteriaCondition == FilterCriteria.IS) &&
                (messageRGB == criteriaRGB)) {
            result = true;
        } else if ((criteriaCondition == FilterCriteria.IS_NOT) &&
                (messageRGB != criteriaRGB)) {
            result = true;
        }

        return result;
    }

    /**
 * @see org.columba.mail.filter.plugins.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
 */
    public void setUp(FilterCriteria f) {
        criteriaRGB = f.getInteger("rgb");
        criteriaCondition = FilterCriteria.getCriteria(f.getCriteriaString());
    }
}
