#!/usr/bin/perl

# $Id: bugzilla-xml-dump.pl,v 1.1 2002/02/12 22:27:04 jon Exp $
#
# A little hacky script for dumping bugzilla XML files to disk.
# Depends on having the 'wget' utility installed on your machine 
# and available in your PATH.
#
# Usage:   ./bugzilla-xml-dump.pl URL START END
# Example: ./bugzilla-xml-dump.pl http://nagoya.apache.org/bugzilla/xml.cgi 2 100

$url = $ARGV[0];
if ($url eq "")
{
    $url = "http://nagoya.apache.org/bugzilla/xml.cgi";
}
$rangeA = $ARGV[1];
if ($rangeA eq "")
{
    $rangeA = "2";
}
$rangeB = $ARGV[2];
if ($rangeB eq "")
{
    $rangeB = "100";
}

$outFile = "bugzilla-$rangeA-$rangeB.xml";

$idStr = "?id=";
for ($i = $rangeA; $i <= $rangeB; $i++)
{
    $idStr = $idStr . "${i}%2C";
}

# ugly, i forget how to write good perl...
# chop off the last %2C crap...
chop $idStr;chop $idStr;chop $idStr;

`wget -O $outFile "$url$idStr"`;
