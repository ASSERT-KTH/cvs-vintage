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

public class WorkerStatusChangedEvent {
	
	public final static int DISPLAY_TEXT_CHANGED = 0;
	public final static int PROGRESSBAR_VALUE_CHANGED = 1;
	public final static int PROGRESSBAR_MAXANDVALUE_CHANGED = 2;
	public final static int PROGRESSBAR_MAX_CHANGED = 3;
	public final static int FINISHED = 4;

	private int type;
	
	private Object oldValue;
	private Object newValue;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

}
