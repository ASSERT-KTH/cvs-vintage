/*
 * ACCEPT_LANGUAGE was not an acceptable name so we are
 * changing it to just LANGUAGE to be more generic.
 * 
 * Created: Sean Jackson <sean@pnc.com.au>
 * $Id: postgresql-upgrade-1.0b13-1.0b14-14.sql,v 1.1 2003/05/01 23:18:47 jon Exp $
 */

ALTER TABLE SCARAB_USER_PREFERENCE RENAME ACCEPT_LANGUAGE TO LANGUAGE;
