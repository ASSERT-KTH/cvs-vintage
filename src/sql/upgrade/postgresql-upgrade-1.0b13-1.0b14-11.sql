/*
 * Drops the unique constraint on the NAME column.  
 *
 * Created: Sean Jackson <sean@pnc.com.au>
 * $Id: postgresql-upgrade-1.0b13-1.0b14-11.sql,v 1.1 2003/05/01 23:18:47 jon Exp $
 */

DROP INDEX scarab_global_paramete_name_key;
