/*
 * Adds email configuration.
 */
insert into SCARAB_GLOBAL_PARAMETER values (2, 'email-enabled', 'T', NULL);
insert into SCARAB_GLOBAL_PARAMETER values (3, 'email-include-issue-details', 'T', NULL);
insert into SCARAB_GLOBAL_PARAMETER values (4, 'email-allow-module-overrides', 'T', NULL);

alter table drop index NAME;