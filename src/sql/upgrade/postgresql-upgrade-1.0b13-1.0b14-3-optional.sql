/*
 * Changes to index names since some non-MySQL databases require
 * index names to be unique by schema rather than by table.
 * The indexes are recreated in the next script, the steps have been
 * separated to help avoid skipping the creation step in the event
 * these error out.
 * IMPORTANT: If upgrading a database that was first created prior to 1.0b11
 * this script is unnecessary and will give harmless errors if run.
 *
 * Created By: Sean Jackson <sean@pnc.com.au>
 */

DROP INDEX IX_REQUIRED;

DROP INDEX IX_QUICKSEARCH;


