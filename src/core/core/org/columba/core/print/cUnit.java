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

package org.columba.core.print;


public abstract class cUnit implements Cloneable {

	private double points = 0.0;
	private double units = 0.0;

	public cUnit() {}

	public double getUnits() {
		return units;
	}

	public void setUnits(double units) {
		this.units = units;
	}

	public abstract void setPoints(double p);

	public abstract double getPoints();

	public cUnit add(double units) {
		cUnit temp = (cUnit) clone();
		temp.setUnits(this.getUnits() + units);

		return temp;
	}

	public cUnit add(cUnit units) {
		cUnit temp = (cUnit) clone();
		temp.setPoints(this.getPoints() + units.getPoints());

		return temp;
	}

	public void addI(cUnit units) {
		setPoints(getPoints() + units.getPoints());
	}

	public cUnit sub(double units) {
		cUnit temp = (cUnit) clone();
		temp.setUnits(this.getUnits() - units);

		return temp;
	}

	public cUnit sub(cUnit units) {
		cUnit temp = (cUnit) clone();
		temp.setPoints(this.getPoints() - units.getPoints());

		return temp;
	}

	public void subI(cUnit units) {
		setPoints(getPoints() - units.getPoints());
	}

	public cUnit mul(double units) {
		cUnit temp = (cUnit) clone();
		temp.setUnits(this.getUnits() * units);

		return temp;
	}

	public cUnit mul(cUnit units) {
		cUnit temp = (cUnit) clone();
		temp.setPoints(this.getPoints() * units.getPoints());

		return temp;
	}

	public void mulI(cUnit units) {
		setPoints(getPoints() * units.getPoints());
	}

	public cUnit div(double units) {
		cUnit temp = (cUnit) clone();
		temp.setUnits(this.getUnits() / units);

		return temp;
	}

	public cUnit div(cUnit units) {
		cUnit temp = (cUnit) clone();
		temp.setPoints(this.getPoints() / units.getPoints());

		return temp;
	}

	public void divI(cUnit units) {
		setPoints(getPoints() / units.getPoints());
	}

	public boolean equals(Object unit) {
		if (unit instanceof cUnit) {
			return (getPoints() == ((cUnit) unit).getPoints());
		}
		return false;
	}

	public Object clone() {
		cUnit clone;
		
		try {
			clone = (cUnit) super.clone();
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}

		clone.setUnits(getPoints());

		return clone;
	}

}
