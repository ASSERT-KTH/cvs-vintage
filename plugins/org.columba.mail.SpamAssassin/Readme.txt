============================ SpamAssassin Plugin ===========================

1. Introduction
2. Installation
3. Usage
4. Author


1. Introduction

This should be the complete solution for your spam problems. Making use of
the commandline tool spamassassin (http://www.spamassassin.org), this 
plugin combines quite a number of plugins to integrate spamassassin in
Columba. 

2. Installation
Download spamassassin at http://www.spamassassin.org. After installing make
sure the spamc demon is running. Before starting Columba, make sure that 
all the commandline tools work properly. Starting spamassassin, spamc and
sa-learn manually will be sufficient.
Use the External Tools Dialog to customize spamassassin commandline tool
paths (Utilities->External Tools).

3. Usage
You will find several new menuitems in Utilities->SpamAssassin submenu:
- Add address to whitelist
- Add address to blacklist
- Remove address from whitelist
- Analyze messages
- Mark messages as spam
- Mark messages as ham


Use the whitelisting/blacklisting actions on messages of your friends to make
sure that the spam filter doesn't mark those message as spam accidently.
Analyzing selected messages, passes the messages along to spamassassin to 
determine if the messages are spam or non spam.
Mark messages as spam or non spam to train SpamAssassin's built-in bayesian
filter.

4. Contact

Frederik Dietz
fdietz@users.sourceforge.net
