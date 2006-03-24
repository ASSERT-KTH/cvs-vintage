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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;

/**
 * Component containing an animated gif, which can be started and 
 * stopped. 
 * 
 * @author fdietz
 */

public class AnimatedGIFComponent extends Component {
	private Image image;
	private Image restImage;
	private boolean stop = false;

	public AnimatedGIFComponent(Image image, Image restImage) {

		super();
		
		this.image = image;
		this.restImage = restImage;
		
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 9);
		try {
			mt.waitForAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		stop();
	}

	public void paint(Graphics g) {
		if ( stop ) 
			g.drawImage(restImage, 0, 0, this);
		else
			g.drawImage(image, 0, 0, this);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		if (stop)
			return false;
		
		if ((infoflags & FRAMEBITS) != 0) {
			//repaint(x, y, width, height);
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					repaint();
				}
			});
			
		}
		
		return true;
	}
	
	public void stop() {
		this.stop = true;
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				repaint();
			}
		});
	}

	public void go() {
		this.stop = false;
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				repaint();
			}
		});
	}

	public boolean stopped() {
		return this.stop;
	}

	public Dimension getMinimumSize() {
		return new Dimension(image.getWidth(this), image.getHeight(this));
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

}
