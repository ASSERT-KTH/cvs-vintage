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
package org.columba.core.gui.statusbar.event;

public class WorkerListChangedEvent {
	
	public static final int SIZE_CHANGED = 0;
	
	private int type;
	private int oldValue;
	private int newValue;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOldValue() {
		return oldValue;
	}

	public void setOldValue(int oldSize) {
		this.oldValue = oldSize;
	}

	public int getNewValue() {
		return newValue;
	}

	public void setNewValue(int newSize) {
		this.newValue = newSize;
	}

}
