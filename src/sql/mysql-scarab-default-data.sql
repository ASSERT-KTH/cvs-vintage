/*
 *  Attribute classes
 */

insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(1, 'free-form', 'Free-form atribute', 'org.tigris.scarab.attribute.FreeFormAttribute');
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(2, 'select-one', 'Select_one attribute', 'org.tigris.scarab.attribute.SelectOneAttribute');
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(3, 'voted', 'Voted attribute', 'org.tigris.scarab.attribute.VotedAttribute');
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(4, 'visitor', 'Visitor attribute', 'org.tigris.scarab.attribute.VisitorAttribute');

/*
 *  Attribute types
 */

insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(1, 1, 'string', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(2, 1, 'date', 'org.tigris.scarab.attribute.DateAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(3, 1, 'integer', 'org.tigris.scarab.attribute.IntegerAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(4, 1, 'float', 'org.tigris.scarab.attribute.FloatAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(5, 2, 'combo-box', 'org.tigris.scarab.attribute.ComboBoxAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(6, 3, 'voted-average', 'org.tigris.scarab.attribute.VotedAverageAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(7, 3, 'voted-simple-majority', 'org.tigris.scarab.attribute.VotedSimpleMajorityAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(8, 4, 'visitor', 'org.tigris.scarab.attribute.ComboBoxAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME) /* Bugzilla-style vote */
        values(9, 3, 'voted-total', 'org.tigris.scarab.attribute.VotedTotalAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME) /* Tracking */
        values(10, 3, 'voted-tracking', 'org.tigris.scarab.attribute.TrackingAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(11, 1, 'email', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(12, 1, 'long-string', 'org.tigris.scarab.attribute.StringAttribute');

/*
 *  Attributes
 */

insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Description */
        values(1, 'description', 12);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Assigned to */
        values(2, 'assigned to', 8);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Status */
        values(3, 'status', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* resolution */
        values(4, 'resolution', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Platform */
        values(5, 'platform', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Operating System */
        values(6, 'operating system', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Priority select-one*/
        values(7, 'priority', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Priority voted*/
        values(8, 'priority', 6);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Severity */
        values(9, 'severity', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Tracking */
        values(10, 'tracking', 10);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Brief (one-line) Description */
        values(11, 'summary', 1);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* A relevant pointer */
        values(13, 'url', 1);


/*
 *
 */

insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Unconfirmed */
        values(1, 3, 'unconfirmed', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* New */
        values(2, 3, 'new', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Assigned */
        values(3, 3, 'assigned', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Reopened */
        values(4, 3, 'reopened', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Resolved */
        values(5, 3, 'resolved', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Verified */
        values(6, 3, 'verified', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Closed */
        values(7, 3, 'closed', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Fixed */
        values(8, 4, 'fixed', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Invalid */
        values(9, 4, 'invalid', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* WONTFIX */
        values(10, 4, 'wontfix', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* LATER */
        values(11, 4, 'later', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* REMIND */
        values(12, 4, 'remind', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* DUPLICATE */
        values(13, 4, 'duplicate', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* WORKSFORME */
        values(14, 4, 'worksforme', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* MOVED */
        values(15, 4, 'moved', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* All */
        values(16, 5, 'all', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* DEC */
        values(17, 5, 'DEC', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* HP */
        values(18, 5, 'HP', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Macintosh */
        values(19, 5, 'Macintosh', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* PC */
        values(20, 5, 'PC', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* SGI */
        values(21, 5, 'SGI', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Sun */
        values(22, 5, 'sun', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Other */
        values(23, 5, 'other', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Operating systems */
        values(24, 6, 'all', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(25, 6, 'windows 3.1', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(26, 6, 'windows 95', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(27, 6, 'windows 98', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(28, 6, 'windows ME', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(29, 6, 'windows 2000', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(30, 6, 'windows NT', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(31, 6, 'mac system 7', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(32, 6, 'mac system 7.5', 9);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(33, 6, 'mac system 7.6.1', 10);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(34, 6, 'mac system 8.0', 11);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(35, 6, 'mac system 8.5', 12);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(36, 6, 'mac system 8.6', 13);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(37, 6, 'mac system 9.0', 14);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(38, 6, 'linux', 15);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(39, 6, 'BSDI', 16);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(40, 6, 'FreeBSD', 17);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(41, 6, 'NetBSD', 18);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(42, 6, 'OpenBSD', 19);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(43, 6, 'AIX', 20);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(44, 6, 'BeOS', 21);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(45, 6, 'HP-UX', 22);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(46, 6, 'IRIX', 23);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(47, 6, 'Neutrino', 24);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(48, 6, 'OpenVMS', 25);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(49, 6, 'OS/2', 26);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(50, 6, 'OSF/1', 27);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(51, 6, 'Solaris', 28);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(52, 6, 'SunOS', 29);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(53, 6, 'other', 30);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Priorities select-one */
        values(54, 7, 'P1', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(55, 7, 'P2', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(56, 7, 'P3', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(57, 7, 'P4', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(58, 7, 'P5', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Priorities voted */
        values(59, 8, 'P1', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(60, 8, 'P2', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(61, 8, 'P3', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(62, 8, 'P4', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(63, 8, 'P5', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Severities */
        values(64, 9, 'blocker', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(65, 9, 'critical', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(66, 9, 'major', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(67, 9, 'normal', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(68, 9, 'minor', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(69, 9, 'trivial', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(70, 9, 'enhancement', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /* Tracking options */
        values(71, 10, 'never', 1); /* never send notification */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(72, 10, 'major', 2); /* send notification on major change */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, DISPLAY_VALUE, NUMERIC_VALUE) /*  */
        values(73, 10, 'any', 3); /* send notification on any change */
/*
 *
 */

insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(1, 'BLOCK');
insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(2, 'DUPLICATE');

/*
 *
 */

insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(1, 'ATTACHMENT');
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(2, 'COMMENT');

/*
 * root module
 */

insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL)
        values(0, "ROOT", "Built-in root module, parent for all top-level modules(projects)", "/");
