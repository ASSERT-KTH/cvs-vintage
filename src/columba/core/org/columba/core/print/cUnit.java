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
