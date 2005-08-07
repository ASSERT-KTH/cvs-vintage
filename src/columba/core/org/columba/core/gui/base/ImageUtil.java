// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.base;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;

import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class ImageUtil {
    /** Create a 20% Transparent icon */
    public static ImageIcon createTransparentIcon(ImageIcon icon) {
        return createTransparentIcon(icon, 20);
    }

    /** Create a x% Transparent icon */
    public static ImageIcon createTransparentIcon(ImageIcon icon, int percentage) {
        return createIcon(icon, new TransparentFilter(percentage));
    }

    /** Create a new icon which is filtered by some ImageFilter */
    private static synchronized ImageIcon createIcon(ImageIcon icon,
        ImageFilter filter) {
        ImageProducer ip;
        Image image;
        MediaTracker tracker;

        ip = new FilteredImageSource(icon.getImage().getSource(), filter);
        image = Toolkit.getDefaultToolkit().createImage(ip);

        tracker = new MediaTracker(new JPanel());
        tracker.addImage(image, 1);

        try {
            tracker.waitForID(1);
        } catch (InterruptedException e) {
            e.printStackTrace();

            return null;
        }

        return new ImageIcon(image);
    }
}
