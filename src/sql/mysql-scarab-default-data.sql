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
        values(4, 'user', 'User attribute', 'org.tigris.scarab.attribute.UserAttribute');

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
        values(8, 4, 'user', 'org.tigris.scarab.attribute.UserAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME) /* Bugzilla-style vote */
        values(9, 3, 'voted-total', 'org.tigris.scarab.attribute.VotedTotalAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME) /* Tracking */
        values(10, 3, 'voted-tracking', 'org.tigris.scarab.attribute.TrackingAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(11, 1, 'email', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(12, 1, 'long-string', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME, VALIDATION_KEY)
    values(13, 1, 'url', 'org.tigris.scarab.attribute.StringAttribute', 'Url');

/*
 *  Attributes
 */

insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Description */
        values(1, 'Description', 12);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Assigned to */
        values(2, 'Assigned To', 8);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Status */
        values(3, 'Status', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* resolution */
        values(4, 'Resolution', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Platform */
        values(5, 'Platform', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Operating System */
        values(6, 'Operating System', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Priority select-one*/
        values(7, 'Priority', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Priority voted*/
        values(8, 'Vote', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Severity */
        values(9, 'Severity', 5);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Tracking */
        values(10, 'Tracking', 10);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* Brief (one-line) Description */
        values(11, 'Summary', 1);
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID) /* A relevant pointer */
        values(13, 'Url', 1);


/*
 *
 */

insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Unconfirmed */
        values(1, 3, 'unconfirmed', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* New */
        values(2, 3, 'new', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Assigned */
        values(3, 3, 'assigned', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Reopened */
        values(4, 3, 'reopened', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Resolved */
        values(5, 3, 'resolved', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Verified */
        values(6, 3, 'verified', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Closed */
        values(7, 3, 'closed', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Fixed */
        values(8, 4, 'fixed', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Invalid */
        values(9, 4, 'invalid', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* WONTFIX */
        values(10, 4, 'wontfix', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* LATER */
        values(11, 4, 'later', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* REMIND */
        values(12, 4, 'remind', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* DUPLICATE */
        values(13, 4, 'duplicate', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* WORKSFORME */
        values(14, 4, 'worksforme', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* MOVED */
        values(15, 4, 'moved', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* All */
        values(16, 5, 'all', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* DEC */
        values(17, 5, 'DEC', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* HP */
        values(18, 5, 'HP', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Macintosh */
        values(19, 5, 'Macintosh', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* PC */
        values(20, 5, 'PC', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* SGI */
        values(21, 5, 'SGI', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Sun */
        values(22, 5, 'sun', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Other */
        values(23, 5, 'other', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Operating systems */
        values(24, 6, 'All', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(75, 6, 'Windows', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(25, 6, 'windows 3.1', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(26, 6, 'windows 95', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(27, 6, 'windows 98', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(28, 6, 'windows ME', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(29, 6, 'windows 2000', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(30, 6, 'windows NT', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(76, 6, 'MacOS', 9);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(31, 6, 'mac system 7', 10);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(32, 6, 'mac system 7.5', 11);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(33, 6, 'mac system 7.6.1', 12);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(34, 6, 'mac system 8.0', 13);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(35, 6, 'mac system 8.5', 14);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(36, 6, 'mac system 8.6', 15);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(37, 6, 'mac system 9.0', 16);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(77, 6, 'OSX', 17);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(38, 6, 'Linux', 18);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(78, 6, 'Redhat', 19);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(79, 6, 'Suse', 20);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(80, 6, 'Debian', 21);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(81, 6, 'Other Linux', 22);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(82, 6, '*BSD', 23);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(40, 6, 'FreeBSD', 24);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(41, 6, 'NetBSD', 25);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(42, 6, 'OpenBSD', 26);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(83, 6, 'Commercial Unix', 27);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(39, 6, 'BSDI', 28);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(43, 6, 'AIX', 29);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(44, 6, 'BeOS', 30);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(45, 6, 'HP-UX', 31);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(46, 6, 'IRIX', 34);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(47, 6, 'Neutrino', 39);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(48, 6, 'OpenVMS', 42);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(49, 6, 'OS/2', 41);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(50, 6, 'OSF/1', 35);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(51, 6, 'Solaris', 32);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(52, 6, 'SunOS', 33);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(53, 6, 'Other', 40);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(84, 6, 'Realtime/Embedded', 38);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(85, 6, 'Handheld/PDA', 36);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(86, 6, 'PalmOS', 37);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(87, 6, 'Unix', 17);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Priorities select-one */
        values(54, 7, 'High', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(55, 7, 'Medium', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(56, 7, 'Low', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(57, 7, 'Undecided', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Priorities voted */
        values(58, 8, 'High', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(59, 8, 'Medium', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(60, 8, 'Low', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(61, 8, 'Undecided', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Severities */
        values(62, 9, 'blocker', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(63, 9, 'critical', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(64, 9, 'major', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(65, 9, 'normal', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(66, 9, 'minor', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(67, 9, 'trivial', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(68, 9, 'enhancement', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(69, 9, 'cosmetic', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(70, 9, 'serious', 9);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(71, 9, 'undecided', 10);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /* Tracking options */
        values(72, 10, 'never', 1); /* never send notification */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(73, 10, 'major', 2); /* send notification on major change */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, NUMERIC_VALUE) /*  */
        values(74, 10, 'any', 3); /* send notification on any change */

/*
 * Option relationships
 * id, name
 */
insert into SCARAB_OPTION_RELATIONSHIP values (1, '1parent-child2'); 
insert into SCARAB_OPTION_RELATIONSHIP values (2, '1required-prior2'); 
insert into SCARAB_OPTION_RELATIONSHIP values (3, '1required-after2'); 

/*
 * Option_Option relationships
 * option1_id, option2_id, relationship_id, deleted
 */
insert into SCARAB_R_OPTION_OPTION values (24, 75, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (24, 76, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (24, 87, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (24, 53, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (24, 84, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (24, 85, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (75, 25, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (75, 26, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (75, 27, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (75, 28, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (75, 29, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (75, 30, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 31, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 32, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 33, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 34, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 35, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 36, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 37, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (76, 77, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (38, 78, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (38, 79, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (38, 80, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (38, 81, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (82, 40, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (82, 41, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (82, 42, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 39, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 43, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 44, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 45, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 46, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 50, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 51, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (83, 52, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (53, 48, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (53, 49, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (84, 47, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (85, 86, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (87, 38, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (87, 82, 1, 0); 
insert into SCARAB_R_OPTION_OPTION values (87, 83, 1, 0); 


/*
 * Issue Dependencies
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

/*
 * populate the root module with all attributes.
 * module_id, attr_id, display_value, active, required, preferred order
 */
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,1,'Description',1,1,1,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,2,'Assigned To',0,0,2,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,3,'Status',1,0,3,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,4,'Resolution',1,0,4,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,5,'Platform',1,1,5,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES 
    (0,6,'Operating System',1,1,6,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,7,'Priority',1,0,7,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,8,'Vote',1,0,8,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,9,'Severity',1,0,9,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,10,'Tracking',0,0,10,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,11,'Summary',1,1,11,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,13,'Url',1,0,12,0,0);

/*
 * populate the root module with all options.
 * module_id, attr_id, display_value, active, preferred order
 */
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,1,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,2,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,3,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,4,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,5,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,6,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,7,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,8,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,9,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,10,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,11,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,12,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,13,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,14,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,15,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,16,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,17,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,18,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,19,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,20,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,21,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,22,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,23,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,24,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,25,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,26,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,27,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,28,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,29,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,30,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,31,NULL,1,10);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,32,NULL,1,11);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,33,NULL,1,12);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,34,NULL,1,13);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,35,NULL,1,14);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,36,NULL,1,15);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,37,NULL,1,16);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,38,NULL,1,18);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,39,NULL,1,28);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,40,NULL,1,24);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,41,NULL,1,25);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,42,NULL,1,26);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,43,NULL,1,29);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,44,NULL,1,30);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,45,NULL,1,31);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,46,NULL,1,34);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,47,NULL,0,39);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,48,NULL,0,42);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,49,NULL,0,41);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,50,NULL,1,35);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,51,NULL,1,32);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,52,NULL,1,33);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,53,NULL,1,40);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,54,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,55,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,56,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,57,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,58,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,59,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,60,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,61,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,62,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,63,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,64,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,65,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,66,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,67,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,68,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,69,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,70,NULL,1,9);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,71,NULL,1,10);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,72,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,73,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,74,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,75,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,76,NULL,1,9);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,77,NULL,1,17);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,78,NULL,0,19);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,79,NULL,0,20);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,80,NULL,0,21);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,81,NULL,0,22);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,82,NULL,1,23);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,83,NULL,1,27);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,84,NULL,1,38);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,85,NULL,1,36);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,86,NULL,0,37);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,87,NULL,1,17);





