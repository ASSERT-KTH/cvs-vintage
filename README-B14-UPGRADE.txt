With the latest release of Scarab b14, we have added new functionality
which associates a DNS domain with each issue so that globally unique
issues can be created. At some point in the far future, we would like
to see issue trackers all around the world communicating with each other
using these globally unique issue id's.

In previous versions of Scarab, this feature was not implemented and the
domain was not associated with the issue. Now, we need to rectify this 
missing data by manually entering it. In order to do this, we need to 
walk you through the steps for upgrading. There are two sections below
which cover the upgrade possibilities. Please choose the one which is 
most appropriate for you.

You will need to decide on is the domain name for your server. Let's
assume it is something like: 'issuetracker.domain.com'. Of course you
would replace that string with your own domain in the SQL commands
below.

          BACKUP YOUR DATABASE FIRST BEFORE MAKING CHANGES!

------------------------------------------------------------------------
Upgrading from b13 to b14 only
------------------------------------------------------------------------

You need to execute the following SQL in your database (replacing the
domain with your own):

    update SCARAB_ISSUE set ID_DOMAIN='issuetracker.domain.com';

Now, for each module you have created, you will need to update the entry
in the ID_TABLE. Right now, you might see something like this:

    select * from ID_TABLE;

+-------------+--------------------------------+---------+----------+
| ID_TABLE_ID | TABLE_NAME                     | NEXT_ID | QUANTITY |
+-------------+--------------------------------+---------+----------+
|           1 | TURBINE_PERMISSION             |     100 |       10 |
|           2 | TURBINE_ROLE                   |     100 |       10 |
|           3 | TURBINE_GROUP                  |     100 |       10 |
|           4 | TURBINE_ROLE_PERMISSION        |     100 |       10 |
|           5 | TURBINE_USER                   |    2828 |        1 |
|           6 | TURBINE_USER_GROUP_ROLE        |     100 |       10 |
|           7 | TURBINE_SCHEDULED_JOB          |     100 |       10 |
.....
|         999 | ID_TABLE                       |    1002 |        1 |
|           0 | GLO                            |       0 |        1 |
|        1000 | SCB                            |     750 |        1 |
|        1001 | ABCD                           |     150 |        1 |
+-------------+--------------------------------+---------+----------+

Where the SCB is the module code. Now, we need to prefix that with your
domain name. So, you would execute something like this:

    update ID_TABLE set TABLE_NAME='issuetracker.domain.com-SCB' where ID_TABLE_ID=1000;
    update ID_TABLE set TABLE_NAME='issuetracker.domain.com-ABCD' where ID_TABLE_ID=1001;

You will need to do this for each module you have created. Because your
instance has never been used in multiple domains before, it will always
have the same domain for each module.

-----------------------------------------------------------------------
Upgrading from b13 -> cvs head -> b14
-----------------------------------------------------------------------

If you have upgraded from b13 to CVS head of Scarab before b14 was
released and you have noticed that newly created issues seem to have
started over from scratch, you will need to make the following manual
modifications:

select ISSUE_ID, ID_COUNT from SCARAB_ISSUE;

+----------+----------+
| ISSUE_ID | ID_COUNT |
+----------+----------+
|      100 |        1 |
|      101 |        2 |
|      120 |        4 |
|      140 |        6 |
|      141 |        7 |
|      142 |        8 |
|      143 |        9 |
|      144 |       10 |
|      145 |       11 |
|      160 |       13 |
.....
|     1692 |      691 |
|     1693 |      692 |
|     1694 |      693 |
|     1695 |      694 |
|     1696 |      695 |
|     1697 |      696 |
|     1698 |        1 |
|     1699 |        2 |
|     1700 |        3 |
+----------+----------+

If you notice above, the last three rows in the table have the wrong ID_COUNT.
We need to fix this by executing the following SQL. Of course you would replace
your ID's with the ones we have shown in this example.

update SCARAB_ISSUE set ID_COUNT=697 where ISSUE_ID=1698;
update SCARAB_ISSUE set ID_COUNT=698 where ISSUE_ID=1699;
update SCARAB_ISSUE set ID_COUNT=699 where ISSUE_ID=1700;

Next, you will need to do this:

    select * from ID_TABLE;

+-------------+--------------------------------+---------+----------+
| ID_TABLE_ID | TABLE_NAME                     | NEXT_ID | QUANTITY |
+-------------+--------------------------------+---------+----------+
|           1 | TURBINE_PERMISSION             |     100 |       10 |
|           2 | TURBINE_ROLE                   |     100 |       10 |
|           3 | TURBINE_GROUP                  |     100 |       10 |
|           4 | TURBINE_ROLE_PERMISSION        |     100 |       10 |
|           5 | TURBINE_USER                   |    2828 |        1 |
|           6 | TURBINE_USER_GROUP_ROLE        |     100 |       10 |
|           7 | TURBINE_SCHEDULED_JOB          |     100 |       10 |
.....
|         999 | ID_TABLE                       |    1002 |        1 |
|           0 | GLO                            |       0 |        1 |
|        1000 | SCB                            |     696 |        1 |
|        1001 | issuetracker.domain.com-SCB    |       4 |        1 |
+-------------+--------------------------------+---------+----------+

You will need to do the following:

    update ID_TABLE set TABLE_NAME='ignore-me' where ID_TABLE_ID=1001;
    update ID_TABLE set TABLE_NAME='issuetracker.domain.com-SCB' where ID_TABLE_ID=1000;

Also, make sure that the ID_TABLE.NEXT_ID is greater than the largest
SCARAB_ISSUE.ID_COUNT. You can see in the above examples that NEXT_ID is
696 for the SCB module. In reality, it will need to be set to be 700 because
three issues have been added while things were in this bad state.

    update ID_TABLE set NEXT_ID=700 where ID_TABLE_ID=1000;
