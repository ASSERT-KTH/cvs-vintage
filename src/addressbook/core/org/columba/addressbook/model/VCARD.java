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

/**
 * All VCARD features Columba is able to handle.
 * <p>
 * vCard standard: http://www.zvon.org/tmRFC/RFC2426/Output/index.html vCard in
 * RDF/XML: http://www.w3.org/TR/vcard-rdf
 * 
 * @author fdietz
 */
public class VCARD {

	/** *********************** Identification Types *********************** */

	/**
	 * These types are used in the vCard profile to capture information
	 * associated with the identification and naming of the person or resource
	 * associated with the vCard.
	 */

	/**
	 * Type purpose: To specify the formatted text corresponding to the name of
	 * the object the vCard represents.
	 * <p>
	 * Example: Mr. John Q. Public
	 */
	public static String FN = "fn";

	/**
	 * Type purpose: To specify the components of the name of the object the
	 * vCard represents.
	 * <p>
	 * The structured type value corresponds, in sequence, to the Family Name,
	 * Given Name, Additional Names, Honorific Prefixes, and Honorific Suffixes.
	 * <p>
	 * Example: Public;John;Quinlan;Mr.
	 */
	public static String N = "n";

	public static String N_PREFIX = "prefix";
	public static String N_FAMILY = "family";
	public static String N_GIVEN = "given";
	public static String N_MIDDLE = "middle";
	public static String N_SUFFIX = "suffix";

	/**
	 * Type purpose: To specify the text corresponding to the nickname of the
	 * object the vCard represents.
	 * <p>
	 * The nickname is the descriptive name given instead of or in addition to
	 * the one belonging to a person, place, or thing. It can also be used to
	 * specify a familiar form of a proper name specified by the FN or N types.
	 * <p>
	 * Example: Robbie
	 */
	public static String NICKNAME = "nickname";

	/**
	 * Columba-specific extension
	 * <p>
	 * Name which is displayed throughout Columba.
	 */
	public static String DISPLAYNAME = "displayname";

	/**
	 * ****************** Delivery Addressing Types ********************
	 */

	/**
	 * These types are concerned with information related to the delivery
	 * addressing or label for the vCard object.
	 */

	/**
	 * Type purpose: To specify the components of the delivery address for the
	 * vCard object.
	 * <p>
	 * The structured type value consists of a sequence of address components.
	 * The structured type value corresponds, in sequence, to the post office
	 * box; the extended address; the street address; the locality (e.g., city);
	 * the region (e.g., state or province); the postal code; the country name.
	 * <p>
	 * The type can include the type parameter "TYPE" to specify the delivery
	 * address type. The TYPE parameter values can include "dom" to indicate a
	 * domestic delivery address; "intl" to indicate an international delivery
	 * address; "postal" to indicate a postal delivery address; "parcel" to
	 * indicate a parcel delivery address; "home" to indicate a delivery address
	 * for a residence; "work" to indicate delivery address for a place of work;
	 * and "pref" to indicate the preferred delivery address when more than one
	 * address is specified.
	 */
	public static String ADR = "adr";

	public static String ADR_POSTOFFICEBOX = "pobox";
	public static String ADR_EXTENDEDADDRESS = "extadd";
	public static String ADR_STREETADDRESS = "street";
	public static String ADR_LOCALITY = "locality";
	public static String ADR_REGIN = "region";
	public static String ADR_POSTALCODE = "pcode";
	public static String ADR_COUNTRY = "country";

	public static String ADR_TYPE_DOM = "dom";
	public static String ADR_TYPE_INTL = "intl";
	public static String ADR_TYPE_POSTAL = "postal";
	public static String ADR_TYPE_PARCEL = "parcel";
	public static String ADR_TYPE_HOME = "home";
	public static String ADR_TYPE_WORK = "work";
	public static String ADR_TYPE_PREF = "pref";

	/**
	 * Type purpose: To specify the formatted text corresponding to delivery
	 * address of the object the vCard represents.
	 * <p>
	 * The type value is formatted text that can be used to present a delivery
	 * address label for the vCard object. The type can include the type
	 * parameter "TYPE" to specify delivery label type. The TYPE parameter
	 * values can include all the different types already found in ADR,
	 * specified above.
	 */
	public static String LABEL = "label";

	public static String LABEL_TYPE_DOM = "dom";
	public static String LABEL_TYPE_INTL = "intl";
	public static String LABEL_TYPE_POSTAL = "postal";
	public static String LABEL_TYPE_PARCEL = "parcel";
	public static String LABEL_TYPE_HOME = "home";
	public static String LABEL_TYPE_WORK = "work";
	public static String LABEL_TYPE_PREF = "pref";

	/**
	 * *************Telecommunications Addressing Types *************
	 */

	/**
	 * These types are concerned with information associated with the
	 * telecommunications addressing of the object the vCard represents.
	 */

	/**
	 * Type purpose: To specify the telephone number for telephony communication
	 * with the object the vCard represents.
	 * <p>
	 * The value of this type is specified in a canonical form in order to
	 * specify an unambiguous representation of the globally unique telephone
	 * endpoint. This type is based on the X.500 Telephone Number attribute.
	 * <p>
	 * The type can include the type parameter "TYPE" to specify intended use
	 * for the telephone number. The TYPE parameter values can include: "home"
	 * to indicate a telephone number associated with a residence, "msg" to
	 * indicate the telephone number has voice messaging support, "work" to
	 * indicate a telephone number associated with a place of work, "pref" to
	 * indicate a preferred-use telephone number, "voice" to indicate a voice
	 * telephone number, "fax" to indicate a facsimile telephone number, "cell"
	 * to indicate a cellular telephone number, "video" to indicate a video
	 * conferencing telephone number, "pager" to indicate a paging device
	 * telephone number, "bbs" to indicate a bulletin board system telephone
	 * number, "modem" to indicate a MODEM connected telephone number, "car" to
	 * indicate a car-phone telephone number, "isdn" to indicate an ISDN service
	 * telephone number, "pcs" to indicate a personal communication services
	 * telephone number. The default type is "voice".
	 */
	public static String TEL = "tel";

	public static String TEL_TYPE_HOME = "home";
	public static String TEL_TYPE_MSG = "msg";
	public static String TEL_TYPE_WORK = "work";
	public static String TEL_TYPE_PREF = "pref";
	public static String TEL_TYPE_VOICE = "voice";
	public static String TEL_TYPE_FAX = "fax";
	public static String TEL_TYPE_CELL = "cell";
	public static String TEL_TYPE_VIDEO = "video";
	public static String TEL_TYPE_PAGER = "pager";
	public static String TEL_TYPE_BBS = "bbs";
	public static String TEL_TYPE_MODEM = "modem";
	public static String TEL_TYPE_CAR = "car";
	public static String TEL_TYPE_ISDN = "isdn";
	public static String TEL_TYPE_PCS = "pcs";

	/**
	 * Type purpose: To specify the electronic mail address for communication
	 * with the object the vCard represents.
	 * <p>
	 * The type can include the type parameter "TYPE" to specify the format or
	 * preference of the electronic mail address. The TYPE parameter values can
	 * include: "internet" to indicate an Internet addressing type, "x400" to
	 * indicate a X.400 addressing type or "pref" to indicate a preferred-use
	 * email address when more than one is specified.
	 */
	public static String EMAIL = "email";
	
	public static String EMAIL_TYPE_INTERNET = "internet";
	public static String EMAIL_TYPE_X400 = "x400";
	public static String EMAIL_TYPE_PREF = "pref";
	
	

	/** ********************* Organizational Types ********************* */

	/**
	 * These types are concerned with information associated with
	 * characteristics of the organization or organizational units of the object
	 * the vCard represents.
	 */

	/**
	 * Type purpose: To specify information concerning the role, occupation, or
	 * business category of the object the vCard represents.
	 * <p>
	 * This type is based on the X.520 Business Category explanatory attribute.
	 * This property is included as an organizational type to avoid confusion
	 * with the semantics of the TITLE type and incorrect usage of that type
	 * when the semantics of this type is intended.
	 */
	public static String ROLE = "role";

	/**
	 * Type purpose: To specify the organizational name and units associated
	 * with the vCard.
	 * <p>
	 * The type is based on the X.520 Organization Name and Organization Unit
	 * attributes. The type value is a structured type consisting of the
	 * organization name, followed by one or more levels of organizational unit
	 * names.
	 */
	public static String ORG = "org";
	
	
	public static String URL = "url";
}