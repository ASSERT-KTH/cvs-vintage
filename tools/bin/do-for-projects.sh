#!/bin/sh
# $Id: do-for-projects.sh,v 1.1 2006/03/21 21:04:29 linus Exp $

# Do the same thing for each project involved in the release.

CHECKOUT_PROJECTS="argouml \
    argouml-csharp \
    argouml-de argouml-es argouml-en-gb argouml-fr argouml-nb argouml-ru \
    argouml-i18n-zh"

PROJECTS="$CHECKOUT_PROJECTS \
    argouml/modules/cpp \
    argouml/modules/classfile \
    argouml/modules/idl \
    argouml/modules/php"

case $1 in
--checkout)
    cvs co -r $2 $CHECKOUT_PROJECTS
    ;;
*)
    for dir in $PROJECTS
    do
        ( cd $dir && $* )
    done
    ;;
esac

