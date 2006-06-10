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
package org.columba.addressbook.model;

public class AddressModel {

	public static final String[] NAMES = new String[] { "work", "home", "other" };

	public static final int TYPE_WORK = 0;

	public static final int TYPE_HOME = 1;

	public static final int TYPE_OTHER = 2;

	private String poBox;

	private String street;

	private String city;

	private String zipPostalCode;

	private String stateProvinceCounty;

	private String country;
	
	private String label;

	int type;

	public AddressModel(String poBox, String street, String city,
			String zipPostalCode, String stateProvinceCounty, String country, String label, 
			String type) {

		// TODO: throw IllegalArgumentException in case a variable == null
		boolean foundMatch = false;
		for (int i = 0; i < NAMES.length; i++) {
			if (type.equals(NAMES[i])) {
				foundMatch = true;
				this.type = i;
			}
		}

		if (!foundMatch)
			throw new IllegalArgumentException("unsupported type ="+type);

		this.poBox = poBox;
		this.street = street;
		this.city = city;
		this.zipPostalCode = zipPostalCode;
		this.stateProvinceCounty = stateProvinceCounty;
		this.country = country;
		this.label = label;
	}

	public AddressModel(String poBox, String street, String city,
			String zipPostalCode, String stateProvinceCounty, String country, String label,
			int type) {
		
		// TODO: throw IllegalArgumentException in case a variable == null

		if (type < 0 || type > 2)
			throw new IllegalArgumentException("unsupported type =" + type);

		this.poBox = poBox;
		this.street = street;
		this.city = city;
		this.zipPostalCode = zipPostalCode;
		this.stateProvinceCounty = stateProvinceCounty;
		this.country = country;
		this.type = type;
		this.label = label;
	}

	public String getTypeString() {
		if (type == 0)
			return "work";
		if (type == 1)
			return "home";
		if (type == 2)
			return "other";
		else
			throw new IllegalArgumentException("type not supported");
	}

	/**
	 * @return Returns the city.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return Returns the country.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return Returns the poBox.
	 */
	public String getPoBox() {
		return poBox;
	}

	/**
	 * @return Returns the stateProvinceCounty.
	 */
	public String getStateProvinceCounty() {
		return stateProvinceCounty;
	}

	/**
	 * @return Returns the zipPostalCode.
	 */
	public String getZipPostalCode() {
		return zipPostalCode;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return Returns the street.
	 */
	public String getStreet() {
		return street;
	}
	
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
}
