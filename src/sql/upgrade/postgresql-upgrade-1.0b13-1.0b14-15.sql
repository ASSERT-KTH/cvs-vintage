/*
 * LOCALE is a more precise name than LANGUAGE.
 * 
 * Created: Sean Jackson <sean@pnc.com.au>
 * $Id: postgresql-upgrade-1.0b13-1.0b14-15.sql,v 1.1 2003/05/01 23:18:47 jon Exp $
 */

ALTER TABLE SCARAB_USER_PREFERENCE RENAME LANGUAGE TO LOCALE;
