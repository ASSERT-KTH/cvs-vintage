ALTER TABLE SCARAB_MODULE drop DEDUPE;
alter table SCARAB_R_MODULE_ISSUE_TYPE add column Dedupe int(1) not null default 1;     
