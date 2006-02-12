package org.columba.calendar.model;


/**
 * All iCalendar features Columba is able to handle.
 * <p>
 * These are the actually used xml-tags, which are mapped to the corresponding
 * iCalendar properties.
 * 
 * @author fdietz
 */
public interface ICALENDAR {

	public final static String ICALENDAR = "icalendar";

	/**
	 * Description: A "VEVENT" calendar component is a grouping of component
	 * properties, and possibly including "VALARM" calendar components, that
	 * represents a scheduled amount of time on a calendar. For example, it can
	 * be an activity; such as a one-hour long, department meeting from 8:00 AM
	 * to 9:00 AM, tomorrow. Generally, an event will take up time on an
	 * individual calendar. Hence, the event will appear as an opaque interval
	 * in a search for busy time. Alternately, the event can have its Time
	 * Transparency set to "TRANSPARENT" in order to prevent blocking of the
	 * event in searches for busy time.
	 * <p>
	 * The "VEVENT" is also the calendar component used to specify an
	 * anniversary or daily reminder within a calendar. These events have a DATE
	 * value type for the "DTSTART" property instead of the default data type of
	 * DATE-TIME. If such a "VEVENT" has a "DTEND" property, it MUST be
	 * specified as a DATE value also. The anniversary type of "VEVENT" can span
	 * more than one date (i.e, "DTEND" property value is set to a calendar date
	 * after the "DTSTART" property value).
	 */
	public final static String VCALENDAR = "vcalendar";

	/**
	 * Property Name: PRODID
	 * 
	 * Purpose: This property specifies the identifier for the product that
	 * created the iCalendar object.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: The property MUST be specified once in an iCalendar object.
	 * 
	 * Description: The vendor of the implementation SHOULD assure that this is
	 * a globally unique identifier; using some technique such as an FPI value,
	 * as defined in [ISO9070].
	 * 
	 * This property SHOULD not be used to alter the interpretation of an
	 * iCalendar object beyond the semantics specified in this memo. For
	 * example, it is not to be used to further the understanding of non-
	 * standard properties.
	 */
	public final static String VCALENDAR_VERSION = "version";

	/**
	 * Property Name: VERSION
	 * 
	 * Purpose: This property specifies the identifier corresponding to the
	 * highest version number or the minimum and maximum range of the iCalendar
	 * specification that is required in order to interpret the iCalendar
	 * object.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: This property MUST be specified by an iCalendar object, but
	 * MUST only be specified once.
	 */
	public final static String VCALENDAR_PRODID = "prodid";

	/**
	 * Property Name: METHOD
	 * 
	 * Purpose: This property defines the iCalendar object method associated
	 * with the calendar object.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: The property can be specified in an iCalendar object.
	 * 
	 * Description: When used in a MIME message entity, the value of this
	 * property MUST be the same as the Content-Type "method" parameter value.
	 * This property can only appear once within the iCalendar object. If either
	 * the "METHOD" property or the Content-Type "method" parameter is
	 * specified, then the other MUST also be specified.
	 * 
	 * No methods are defined by this specification. This is the subject of
	 * other specifications, such as the iCalendar Transport-independent
	 * 
	 * Interoperability Protocol (iTIP) defined by [ITIP].
	 * 
	 * If this property is not present in the iCalendar object, then a
	 * scheduling transaction MUST NOT be assumed. In such cases, the iCalendar
	 * object is merely being used to transport a snapshot of some calendar
	 * information; without the intention of conveying a scheduling semantic.
	 */
	public final static String VCALENDAR_METHOD = "method";

	public final static String VEVENT = "vevent";

	/**
	 * Description: A "VTODO" calendar component is a grouping of component
	 * properties and possibly "VALARM" calendar components that represent an
	 * action-item or assignment. For example, it can be used to represent an
	 * item of work assigned to an individual; such as "turn in travel expense
	 * today".
	 * <p>
	 * The "VTODO" calendar component cannot be nested within another calendar
	 * component. However, "VTODO" calendar components can be related to each
	 * other or to a "VTODO" or to a "VJOURNAL" calendar component with the
	 * "RELATED-TO" property.
	 */
	public final static String VTODO = "vtodo";

	/**
	 * Property Name: UID
	 * 
	 * Purpose: This property defines the persistent, globally unique identifier
	 * for the calendar component.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: The property MUST be specified in the "VEVENT", "VTODO",
	 * "VJOURNAL" or "VFREEBUSY" calendar components.
	 * 
	 * Description: The UID itself MUST be a globally unique identifier. The
	 * generator of the identifier MUST guarantee that the identifier is unique.
	 * There are several algorithms that can be used to accomplish this. The
	 * identifier is RECOMMENDED to be the identical syntax to the [RFC822]
	 * addr-spec. A good method to assure uniqueness is to put the domain name
	 * or a domain literal IP address of the host on which the identifier was
	 * created on the right hand side of the "@", and on the left hand side, put
	 * a combination of the current calendar date and time of day (i.e.,
	 * formatted in as a DATE-TIME value) along with some other currently unique
	 * (perhaps sequential) identifier available on the system (for example, a
	 * process id number). Using a date/time value on the left hand side and a
	 * domain name or domain literal on the right hand side makes it possible to
	 * guarantee uniqueness since no two hosts should be using the same domain
	 * name or IP address at the same time. Though other algorithms will work,
	 * it is RECOMMENDED that the right hand side contain some domain identifier
	 * (either of the host itself or otherwise) such that the generator of the
	 * message identifier can guarantee the uniqueness of the left hand side
	 * within the scope of that domain.
	 * 
	 * This is the method for correlating scheduling messages with the
	 * referenced "VEVENT", "VTODO", or "VJOURNAL" calendar component.
	 * 
	 * The full range of calendar components specified by a recurrence set is
	 * referenced by referring to just the "UID" property value corresponding to
	 * the calendar component. The "RECURRENCE-ID" property allows the reference
	 * to an individual instance within the recurrence set.
	 * 
	 * This property is an important method for group scheduling applications to
	 * match requests with later replies, modifications or deletion requests.
	 * Calendaring and scheduling applications MUST generate this property in
	 * "VEVENT", "VTODO" and "VJOURNAL" calendar components to assure
	 * interoperability with other group scheduling applications. This
	 * identifier is created by the calendar system that generates an iCalendar
	 * object.
	 * 
	 * Implementations MUST be able to receive and persist values of at least
	 * 255 characters for this property.
	 */
	public final static String UID = "uid";

	/**
	 * Property Name: DTSTAMP
	 * 
	 * Purpose: The property indicates the date/time that the instance of the
	 * iCalendar object was created.
	 * 
	 * Value Type: DATE-TIME
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: This property MUST be included in the "VEVENT", "VTODO",
	 * "VJOURNAL" or "VFREEBUSY" calendar components.
	 * 
	 * Description: The value MUST be specified in the UTC time format.
	 * 
	 * This property is also useful to protocols such as [IMIP] that have
	 * inherent latency issues with the delivery of content. This property will
	 * assist in the proper sequencing of messages containing iCalendar objects.
	 * 
	 * This property is different than the "CREATED" and "LAST-MODIFIED"
	 * properties. These two properties are used to specify when the particular
	 * calendar data in the calendar store was created and last modified. This
	 * is different than when the iCalendar object representation of the
	 * calendar service information was created or last modified.
	 */
	public final static String DTSTAMP = "dtstamp";

	/**
	 * Property Name: DTSTART
	 * 
	 * Purpose: This property specifies when the calendar component begins.
	 * 
	 * Value Type: The default value type is DATE-TIME. The time value MUST be
	 * one of the forms defined for the DATE-TIME value type. The value type can
	 * be set to a DATE value type.
	 * 
	 * Property Parameters: Non-standard, value data type, time zone identifier
	 * property parameters can be specified on this property.
	 * 
	 * Conformance: This property can be specified in the "VEVENT", "VTODO",
	 * "VFREEBUSY", or "VTIMEZONE" calendar components.
	 * 
	 * Description: Within the "VEVENT" calendar component, this property
	 * defines the start date and time for the event. The property is REQUIRED
	 * in "VEVENT" calendar components. Events can have a start date/time but no
	 * end date/time. In that case, the event does not take up any time.
	 * 
	 * Within the "VFREEBUSY" calendar component, this property defines the
	 * start date and time for the free or busy time information. The time MUST
	 * be specified in UTC time.
	 * 
	 * Within the "VTIMEZONE" calendar component, this property defines the
	 * effective start date and time for a time zone specification. This
	 * property is REQUIRED within each STANDARD and DAYLIGHT part included in
	 * "VTIMEZONE" calendar components and MUST be specified as a local
	 * DATE-TIME without the "TZID" property parameter.
	 */
	public final static String DTSTART = "dtstart";

	/**
	 * Property Name: DTEND
	 * 
	 * Purpose: This property specifies the date and time that a calendar
	 * component ends.
	 * 
	 * Value Type: The default value type is DATE-TIME. The value type can be
	 * set to a DATE value type.
	 * 
	 * Property Parameters: Non-standard, value data type, time zone identifier
	 * property parameters can be specified on this property.
	 * 
	 * Conformance: This property can be specified in "VEVENT" or "VFREEBUSY"
	 * calendar components.
	 * 
	 * Description: Within the "VEVENT" calendar component, this property
	 * defines the date and time by which the event ends. The value MUST be
	 * later in time than the value of the "DTSTART" property.
	 * 
	 * Within the "VFREEBUSY" calendar component, this property defines the end
	 * date and time for the free or busy time information. The time MUST be
	 * specified in the UTC time format. The value MUST be later in time than
	 * the value of the "DTSTART" property.
	 */
	public final static String DTEND = "dtend";

	/**
	 * Property Name: SUMMARY
	 * 
	 * Purpose: This property defines a short summary or subject for the
	 * calendar component.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard, alternate text representation and
	 * language property parameters can be specified on this property.
	 * 
	 * Conformance: The property can be specified in "VEVENT", "VTODO",
	 * "VJOURNAL" or "VALARM" calendar components.
	 * 
	 * Description: This property is used in the "VEVENT", "VTODO" and
	 * "VJOURNAL" calendar components to capture a short, one line summary about
	 * the activity or journal entry.
	 * 
	 * This property is used in the "VALARM" calendar component to capture the
	 * subject of an EMAIL category of alarm.
	 */
	public final static String SUMMARY = "summary";

	/**
	 * Property Name: DESCRIPTION
	 * 
	 * Purpose: This property provides a more complete description of the
	 * calendar component, than that provided by the "SUMMARY" property.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard, alternate text representation and
	 * language property parameters can be specified on this property.
	 * 
	 * Conformance: The property can be specified in the "VEVENT", "VTODO",
	 * "VJOURNAL" or "VALARM" calendar components. The property can be specified
	 * multiple times only within a "VJOURNAL" calendar component.
	 * 
	 * Description: This property is used in the "VEVENT" and "VTODO" to capture
	 * lengthy textual decriptions associated with the activity.
	 * 
	 * This property is used in the "VJOURNAL" calendar component to capture one
	 * more textual journal entries.
	 * 
	 * This property is used in the "VALARM" calendar component to capture the
	 * display text for a DISPLAY category of alarm, to capture the body text
	 * for an EMAIL category of alarm and to capture the argument string for a
	 * PROCEDURE category of alarm.
	 */
	public final static String DESCRIPTION = "description";

	/**
	 * Property Name: CLASS
	 * 
	 * Purpose: This property defines the access classification for a calendar
	 * component.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: The property can be specified once in a "VEVENT", "VTODO" or
	 * "VJOURNAL" calendar components.
	 * 
	 * Description: An access classification is only one component of the
	 * general security system within a calendar application. It provides a
	 * method of capturing the scope of the access the calendar owner intends
	 * for information within an individual calendar entry. The access
	 * classification of an individual iCalendar component is useful when
	 * measured along with the other security components of a calendar system
	 * (e.g., calendar user authentication, authorization, access rights, access
	 * role, etc.). Hence, the semantics of the individual access
	 * classifications cannot be completely defined by this memo alone.
	 * Additionally, due to the "blind" nature of most exchange processes using
	 * this memo, these access classifications cannot serve as an enforcement
	 * statement for a system receiving an iCalendar object. Rather, they
	 * provide a method for capturing the intention of the calendar owner for
	 * the access to the calendar component.
	 */
	public final static String CLASS = "class";

	/**
	 * Property Name: TRANSP
	 * 
	 * Purpose: This property defines whether an event is transparent or not to
	 * busy time searches.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: This property can be specified once in a "VEVENT" calendar
	 * component.
	 * 
	 * Description: Time Transparency is the characteristic of an event that
	 * determines whether it appears to consume time on a calendar. Events that
	 * consume actual time for the individual or resource associated with the
	 * calendar SHOULD be recorded as OPAQUE, allowing them to be detected by
	 * free-busy time searches. Other events, which do not take up the
	 * individual's (or resource's) time SHOULD be recorded as TRANSPARENT,
	 * making them invisible to free-busy time searches.
	 */
	public final static String TRANSP = "transp";

	/**
	 * Property Name: CATEGORIES
	 * 
	 * Purpose: This property defines the categories for a calendar component.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard and language property parameters can be
	 * specified on this property.
	 * 
	 * Conformance: The property can be specified within "VEVENT", "VTODO" or
	 * "VJOURNAL" calendar components.
	 * 
	 * Description: This property is used to specify categories or subtypes of
	 * the calendar component. The categories are useful in searching for a
	 * calendar component of a particular type and category. Within the
	 * "VEVENT", "VTODO" or "VJOURNAL" calendar components, more than one
	 * category can be specified as a list of categories separated by the COMMA
	 * character (US-ASCII decimal 44).
	 */
	public final static String CATEGORIES = "categories";
	public final static String ITEM = "item";
	/**
	 * Property Name: RRULE
	 * 
	 * Purpose: This property defines a rule or repeating pattern for recurring
	 * events, to-dos, or time zone definitions.
	 * 
	 * Value Type: RECUR
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: This property can be specified one or more times in
	 * recurring "VEVENT", "VTODO" and "VJOURNAL" calendar components. It can
	 * also be specified once in each STANDARD or DAYLIGHT sub-component of the
	 * "VTIMEZONE" calendar component.
	 * 
	 * Description: The recurrence rule, if specified, is used in computing the
	 * recurrence set. The recurrence set is the complete set of recurrence
	 * instances for a calendar component. The recurrence set is generated by
	 * considering the initial "DTSTART" property along with the "RRULE",
	 * "RDATE", "EXDATE" and "EXRULE" properties contained within the iCalendar
	 * object. The "DTSTART" property defines the first instance in the
	 * recurrence set. Multiple instances of the "RRULE" and "EXRULE" properties
	 * can also be specified to define more sophisticated recurrence sets. The
	 * final recurrence set is generated by gathering all of the start
	 * date/times generated by any of the specified "RRULE" and "RDATE"
	 * properties, and excluding any start date/times which fall within the
	 * union of start date/times generated by any specified "EXRULE" and
	 * "EXDATE" properties. This implies that start date/times within exclusion
	 * related properties (i.e., "EXDATE" and "EXRULE") take precedence over
	 * those specified by inclusion properties (i.e., "RDATE" and "RRULE").
	 * Where duplicate instances are generated by the "RRULE" and "RDATE"
	 * properties, only one recurrence is considered. Duplicate instances are
	 * ignored.
	 * 
	 * The "DTSTART" and "DTEND" property pair or "DTSTART" and "DURATION"
	 * property pair, specified within the iCalendar object defines the first
	 * instance of the recurrence. When used with a recurrence rule, the
	 * "DTSTART" and "DTEND" properties MUST be specified in local time and the
	 * appropriate set of "VTIMEZONE" calendar components MUST be included. For
	 * detail on the usage of the "VTIMEZONE" calendar component, see the
	 * "VTIMEZONE" calendar component definition.
	 * 
	 * Any duration associated with the iCalendar object applies to all members
	 * of the generated recurrence set. Any modified duration for specific
	 * recurrences MUST be explicitly specified using the "RDATE" property.
	 */
	public final static String RRULE = "rrule";

	/**
	 * Property Name: ORGANIZER
	 * 
	 * Purpose: The property defines the organizer for a calendar component.
	 * 
	 * Value Type: CAL-ADDRESS
	 * 
	 * Property Parameters: Non-standard, language, common name, directory entry
	 * reference, sent by property parameters can be specified on this property.
	 * 
	 * Conformance: This property MUST be specified in an iCalendar object that
	 * specifies a group scheduled calendar entity. This property MUST be
	 * specified in an iCalendar object that specifies the publication of a
	 * calendar user's busy time. This property MUST NOT be specified in an
	 * iCalendar object that specifies only a time zone definition or that
	 * defines calendar entities that are not group scheduled entities, but are
	 * entities only on a single user's calendar.
	 * 
	 * Description: The property is specified within the "VEVENT", "VTODO",
	 * "VJOURNAL calendar components to specify the organizer of a group
	 * scheduled calendar entity. The property is specified within the
	 * "VFREEBUSY" calendar component to specify the calendar user requesting
	 * the free or busy time. When publishing a "VFREEBUSY" calendar component,
	 * the property is used to specify the calendar that the published busy time
	 * came from.
	 * 
	 * The property has the property parameters CN, for specifying the common or
	 * display name associated with the "Organizer", DIR, for specifying a
	 * pointer to the directory information associated with the "Organizer",
	 * SENT-BY, for specifying another calendar user that is acting on behalf of
	 * the "Organizer". The non-standard parameters may also be specified on
	 * this property. If the LANGUAGE property parameter is specified, the
	 * identified language applies to the CN parameter value.
	 */
	public final static String ORGANIZER = "organizer";

	/**
	 * Property Name: ATTENDEE
	 * 
	 * Purpose: The property defines an "Attendee" within a calendar component.
	 * 
	 * Value Type: CAL-ADDRESS
	 * 
	 * Property Parameters: Non-standard, language, calendar user type, group or
	 * list membership, participation role, participation status, RSVP
	 * expectation, delegatee, delegator, sent by, common name or directory
	 * entry reference property parameters can be specified on this property.
	 * 
	 * Conformance: This property MUST be specified in an iCalendar object that
	 * specifies a group scheduled calendar entity. This property MUST NOT be
	 * specified in an iCalendar object when publishing the calendar information
	 * (e.g., NOT in an iCalendar object that specifies the publication of a
	 * calendar user's busy time, event, to-do or journal). This property is not
	 * specified in an iCalendar object that specifies only a time zone
	 * definition or that defines calendar entities that are not group scheduled
	 * entities, but are entities only on a single user's calendar.
	 * 
	 * Description: The property MUST only be specified within calendar
	 * components to specify participants, non-participants and the chair of a
	 * group scheduled calendar entity. The property is specified within an
	 * "EMAIL" category of the "VALARM" calendar component to specify an email
	 * address that is to receive the email type of iCalendar alarm.
	 * 
	 * The property parameter CN is for the common or displayable name
	 * associated with the calendar address; ROLE, for the intended role that
	 * the attendee will have in the calendar component; PARTSTAT, for the
	 * status of the attendee's participation; RSVP, for indicating whether the
	 * favor of a reply is requested; CUTYPE, to indicate the type of calendar
	 * user; MEMBER, to indicate the groups that the attendee belongs to;
	 * DELEGATED-TO, to indicate the calendar users that the original request
	 * was delegated to; and DELEGATED-FROM, to indicate whom the request was
	 * delegated from; SENT-BY, to indicate whom is acting on behalf of the
	 * ATTENDEE; and DIR, to indicate the URI that points to the directory
	 * information corresponding to the attendee. These property parameters can
	 * be specified on an "ATTENDEE" property in either a "VEVENT", "VTODO" or
	 * "VJOURNAL" calendar component. They MUST not be specified in an
	 * "ATTENDEE" property in a "VFREEBUSY" or "VALARM" calendar component. If
	 * the LANGUAGE property parameter is specified, the identified language
	 * applies to the CN parameter.
	 * 
	 * A recipient delegated a request MUST inherit the RSVP and ROLE values
	 * from the attendee that delegated the request to them.
	 * 
	 * Multiple attendees can be specified by including multiple "ATTENDEE"
	 * properties within the calendar component.
	 */
	public final static String ATTENDEE = "attendee";

	/**
	 * Property Name: URL
	 * 
	 * Purpose: This property defines a Uniform Resource Locator (URL)
	 * associated with the iCalendar object.
	 * 
	 * Value Type: URI
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: This property can be specified once in the "VEVENT",
	 * "VTODO", "VJOURNAL" or "VFREEBUSY" calendar components.
	 * 
	 * Description: This property may be used in a calendar component to convey
	 * a location where a more dynamic rendition of the calendar information
	 * associated with the calendar component can be found. This memo does not
	 * attempt to standardize the form of the URI, nor the format of the
	 * resource pointed to by the property value. If the URL property and
	 * Content-Location MIME header are both specified, they MUST point to the
	 * same resource.
	 */
	public final static String URL = "url";

	/**
	 * Property Name: FREEBUSY
	 * 
	 * Purpose: The property defines one or more free or busy time intervals.
	 * 
	 * Value Type: PERIOD. The date and time values MUST be in an UTC time
	 * format.
	 * 
	 * Property Parameters: Non-standard or free/busy time type property
	 * parameters can be specified on this property.
	 * 
	 * Conformance: The property can be specified in a "VFREEBUSY" calendar
	 * component.
	 * 
	 * Property Parameter: "FBTYPE" and non-standard parameters can be specified
	 * on this property.
	 * 
	 * Description: These time periods can be specified as either a start and
	 * end date-time or a start date-time and duration. The date and time MUST
	 * be a UTC time format.
	 * 
	 * "FREEBUSY" properties within the "VFREEBUSY" calendar component SHOULD be
	 * sorted in ascending order, based on start time and then end time, with
	 * the earliest periods first.
	 * 
	 * The "FREEBUSY" property can specify more than one value, separated by the
	 * COMMA character (US-ASCII decimal 44). In such cases, the "FREEBUSY"
	 * property values SHOULD all be of the same "FBTYPE" property parameter
	 * type (e.g., all values of a particular "FBTYPE" listed together in a
	 * single property).
	 */
	public final static String FREEBUSY = "freebusy";

	/**
	 * Property Name: PRIORITY
	 * 
	 * Purpose: The property defines the relative priority for a calendar
	 * component.
	 * 
	 * Value Type: INTEGER
	 * 
	 * Property Parameters: Non-standard property parameters can be specified on
	 * this property.
	 * 
	 * Conformance: The property can be specified in a "VEVENT" or "VTODO"
	 * calendar component.
	 * 
	 * Description: The priority is specified as an integer in the range zero to
	 * nine. A value of zero (US-ASCII decimal 48) specifies an undefined
	 * priority. A value of one (US-ASCII decimal 49) is the highest priority. A
	 * value of two (US-ASCII decimal 50) is the second highest priority.
	 * Subsequent numbers specify a decreasing ordinal priority. A value of nine
	 * (US-ASCII decimal 58) is the lowest priority.
	 * 
	 * A CUA with a three-level priority scheme of "HIGH", "MEDIUM" and "LOW" is
	 * mapped into this property such that a property value in the range of one
	 * (US-ASCII decimal 49) to four (US-ASCII decimal 52) specifies "HIGH"
	 * priority. A value of five (US-ASCII decimal 53) is the normal or "MEDIUM"
	 * priority. A value in the range of six (US- ASCII decimal 54) to nine
	 * (US-ASCII decimal 58) is "LOW" priority.
	 * 
	 * A CUA with a priority schema of "A1", "A2", "A3", "B1", "B2", ..., "C3"
	 * is mapped into this property such that a property value of one (US-ASCII
	 * decimal 49) specifies "A1", a property value of two (US- ASCII decimal
	 * 50) specifies "A2", a property value of three (US-ASCII decimal 51)
	 * specifies "A3", and so forth up to a property value of 9 (US-ASCII
	 * decimal 58) specifies "C3".
	 * 
	 * Other integer values are reserved for future use.
	 * 
	 * Within a "VEVENT" calendar component, this property specifies a priority
	 * for the event. This property may be useful when more than one event is
	 * scheduled for a given time period.
	 * 
	 * Within a "VTODO" calendar component, this property specified a priority
	 * for the to-do. This property is useful in prioritizing multiple action
	 * items for a given time period.
	 */
	public final static String PRIORITY = "priority";

	/**
	 * Property Name: ATTACH
	 * 
	 * Purpose: The property provides the capability to associate a document
	 * object with a calendar component.
	 * 
	 * Value Type: The default value type for this property is URI. The value
	 * type can also be set to BINARY to indicate inline binary encoded content
	 * information.
	 * 
	 * Property Parameters: Non-standard, inline encoding, format type and value
	 * data type property parameters can be specified on this property.
	 * 
	 * Conformance: The property can be specified in a "VEVENT", "VTODO",
	 * "VJOURNAL" or "VALARM" calendar components.
	 * 
	 * Description: The property can be specified within "VEVENT", "VTODO",
	 * "VJOURNAL", or "VALARM" calendar components. This property can be
	 * specified multiple times within an iCalendar object.
	 */
	public final static String ATTACH = "attach";

	/**
	 * Property Name: LOCATION
	 * 
	 * Purpose: The property defines the intended venue for the activity defined
	 * by a calendar component.
	 * 
	 * Value Type: TEXT
	 * 
	 * Property Parameters: Non-standard, alternate text representation and
	 * language property parameters can be specified on this property.
	 * 
	 * Conformance: This property can be specified in "VEVENT" or "VTODO"
	 * calendar component.
	 * 
	 * Description: Specific venues such as conference or meeting rooms may be
	 * explicitly specified using this property. An alternate representation may
	 * be specified that is a URI that points to directory information with more
	 * structured specification of the location. For example, the alternate
	 * representation may specify either an LDAP URI pointing to an LDAP server
	 * entry or a CID URI pointing to a MIME body part containing a vCard
	 * [RFC2426] for the location.
	 */
	public final static String LOCATION = "location";

}
