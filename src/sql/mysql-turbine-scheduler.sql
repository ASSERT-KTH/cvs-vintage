

                                        
# -----------------------------------------------------------------------
# TURBINE_SCHEDULED_JOB
# -----------------------------------------------------------------------
drop table if exists TURBINE_SCHEDULED_JOB;

CREATE TABLE TURBINE_SCHEDULED_JOB
(
    JOB_ID INTEGER NOT NULL,
    SECOND INTEGER default -1 NOT NULL,
    MINUTE INTEGER default -1 NOT NULL,
    HOUR INTEGER default -1 NOT NULL,
    WEEK_DAY INTEGER default -1 NOT NULL,
    DAY_OF_MONTH INTEGER default -1 NOT NULL,
    TASK VARCHAR (99) NOT NULL,
    EMAIL VARCHAR (99),
    PROPERTY MEDIUMBLOB,
    PRIMARY KEY(JOB_ID)
);

                                                          

                        