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
package org.columba.core.config;

import java.awt.Dimension;
import java.awt.Point;

import org.columba.core.xml.XmlElement;

public class WindowItem extends DefaultItem {

	public WindowItem(XmlElement root) {
		super(root);
	}

	public Point getPoint() {
		Point point = new Point();

		point.x = getInteger("x");
		point.y = getInteger("y");

		return point;
	}

	public Dimension getDimension() {
		Dimension dim = new Dimension();

		dim.width = getInteger("width");
		dim.height = getInteger("height");

		return dim;
	}

}