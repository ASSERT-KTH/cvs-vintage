// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.xml;

import java.io.IOException;

public class XmlTest {
	public XmlTest() {}
	
	public static void main(String argv[]) {
		if (argv.length != 1) {
			System.err.println("Usage: cmd filename");
			System.exit(1);
		}

		XmlIO X = new XmlIO();
		X.load(argv[0]);

		XmlElement.printNode(X.getRoot(), "");

		System.out.println("---------------------------------------------");
		XmlElement E = X.getRoot().getElement("options");
		if (E != null) {
			System.out.println("options: '" + E.getData() + "'");
		}
		E = X.getRoot().getElement("/options/gui/window/width");
		if (E != null) {
			System.out.println(
				"options/gui/window/width: '" + E.getData() + "'");
		} else {
			System.out.println(
				"options/gui/window/width: "
					+ "**Not found in this XML document**");
		}

		System.out.println("---------------------------------------------");

		try {
			X.write(System.out);
		} catch (IOException e) {
			System.out.println("Error in write: " + e.toString());
		}
	}
}
