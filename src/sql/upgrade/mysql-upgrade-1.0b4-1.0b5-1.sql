/*
 * This upgrade script moves the renames the DEFAULT_SUBSCRIPTION_FREQUENCY_ID 
 * column.  Some databases choke on the longer name.
 *
 * Created By: John McNally
 * $Id: mysql-upgrade-1.0b4-1.0b5-1.sql,v 1.1 2002/02/23 22:03:20 jmcnally Exp $
 */

alter table SCARAB_QUERY change DEFAULT_SUBSCRIPTION_FREQUENCY_ID SUBSCRIPTION_FREQUENCY_ID int(11); 

