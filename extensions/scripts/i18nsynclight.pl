#!/usr/bin/perl 

# ================================================================
# Copyright (c) 2000-2002 CollabNet.  All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
# 
# 1. Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
# 
# 2. Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
# 
# 3. The end-user documentation included with the redistribution, if
# any, must include the following acknowlegement: "This product includes
# software developed by Collab.Net <http://www.Collab.Net/>."
# Alternately, this acknowlegement may appear in the software itself, if
# and wherever such third-party acknowlegements normally appear.
# 
# 4. The hosted project names must not be used to endorse or promote
# products derived from this software without prior written
# permission. For written permission, please contact info@collab.net.
# 
# 5. Products derived from this software may not use the "Tigris" or 
# "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
# prior written permission of Collab.Net.
# 
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
# ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# ====================================================================
# 
# This software consists of voluntary contributions made by many
# individuals on behalf of Collab.Net. 

# given two properties files, syncs the first "target" file to the 
# second "canonical" file, so that they have the same number of keys

# Author: Chris Dolan <cdolan@collab.net>

use strict;
use Getopt::Long;

my $options = getOptions();    

my $error;
($options->{'canonical'}) or $error .= "You must provide a canonical bundle\n";
($options->{'target'}) or $error .= "You must provide a target bundle\n";
($error) and showUsage($error);

updateBundle($options->{'target'}, $options->{'canonical'});

sub showUsage
{
    my $error = shift;
    print <<"EOH";

Make sure a translation of a properties file has all of the same keys 
as a canonical version, removing keys and adding keys (with their 
canonical values) where necessary.

Usage: i18nsynclight.pl --canonical=canoncal.file --target=target.file \
    [--verbose] [--backup]
    
    canonical       path to the reference or canonical file
    target          path to the target file
    backup          save a .bak version of the target file 
    verbose         explain what was done

EOH
    ($error) and print "$error\n";
    exit; 
}

sub getOptions
{
    my %options;
    (!GetOptions ( \%options, 
        'canonical=s', 
        'target=s',
        'verbose',
        'backup',
        'help')
        or $options{'help'}) and showUsage();
    return \%options;
}


sub updateBundle 
{
    my ($target, $canonical) = @_;
    my $bundle;
    $bundle = new ScarabResourceBundle("",$target);
    $bundle->setCanonicalBundleFromFile($canonical);
    # target bundle is what we use to compare against for changes, so
    # is the original version of the source file (before changes).
    $bundle->setTargetBundleFromFile($target);
    my $status = $bundle->updateBundle();
    if ($status)
    {
        if ($options->{'backup'})
        {
            rename($bundle->filename(), $bundle->filename().".bak");
        }
        open NEW, ">" . $bundle->filename()
            or die "Unable to open " . $bundle->filename() . " for writing: $!";
        print NEW @{$bundle->getUpdated()};
        close NEW;
        ($options->{'verbose'}) and print "\n $target: $status\n";
    }
}

# this kind of object is used for a resource bundle
package ScarabResourceBundle;     

sub new
{
    my ($type, $project, $filename) = @_;
    my $self = {
        'type' => $type,
        'project' => $project,
        'filename' => $filename,
        'lines' => [],
        'keys' => {},
        'orderedKeys' => [],
    };
    bless $self, $type;
    return $self;
}

sub filename
{
    my ($self) = @_;
    return $self->{filename};
}

sub read
{
    my ($self) = @_;
    open(IN, $self->{filename}) or
        die "unable to read ".$self->filename().": $!\n";
    my $current_key;
    while (my $line = <IN>)
    {
        chomp($line);
        
        # account for multi line values
        # (for instance in viewcvs)
        if ($line =~ m/^\s+/)
        {
            $self->{keys}{$current_key} .= "\n$line";
            next;
        }
         
        if (($line =~ m/=/) and !($line =~ m/^#/))
        {
            my ($key, $value) = split /\s*=\s*/, $line, 2;
            $current_key = $key;
            unless (${$self->{keys}}{$key})
            {
                $self->{keys}{$key} = $value;
                print "parsing ", $self->filename(), "got $key => $value\n"
                    if $options->{'debug'} > 3;
                push @{$self->{orderedKeys}}, $key;
            }
            else
            {
                print "parsing ", $self->filename(), "found duplicate $key => $value\n"
                    if $options->{'debug'} > 3;
                next;
            }
        }
        else 
        {
            push @{$self->{orderedKeys}}, "!$line"
                unless ($line =~ /\$Id/);
            
        }
        push @{$self->{lines}}, $line;
    }
    close IN;
}

sub getKeyValue
{
    my ($self, $key) = @_;
    return $self->{keys}{$key};
}

sub getKeys
{
    my ($self) = @_;
    return [ keys %{$self->{keys}} ];
}

sub getOrderedKeys
{
    my ($self) = @_;
    return $self->{orderedKeys};

}

sub getLines
{
    my ($self) = @_;
    return $self->{lines};
}

sub addKey
{
    my ($self, $key, $value) = @_;
    $self->{keys}{$key} = $value;
    push @{$self->{lines}}, "$key = $value\n";
}

sub getAdded
{
    my ($self) = @_;
    return $self->{'addedKeys'};
}

sub getRemoved
{
    my ($self) = @_;
    return $self->{'removedKeys'};
}

sub getUpdated
{
    my ($self) = @_;
    return $self->{'updated'};
}

# doesn't do any type checking (major drawback to perl oo) 
# anyway, expects another Bundle as an argument
sub setCanonicalBundle
{
    my ($self, $canonicalBundle) = @_;
    $self->{'canonicalBundle'} = $canonicalBundle;
}

sub getCanonicalBundle
{
    my ($self) = @_;
    return ($self->{'canonicalBundle'});
}

# expects a filename argument
# shortcut routine if you need the project values set, just create
# the canonical bundle separately and call setCanonicalBundle
# directly
sub setCanonicalBundleFromFile
{
    my ($self, $filename) = @_;
    my $canonicalBundle = new $self->{'type'}, ("",$filename);
    $self->{'canonicalBundle'} = $canonicalBundle;
}

sub setTargetBundle
{
    my ($self, $targetBundle) = @_;
    $self->{'targetBundle'} = $targetBundle;
}

sub getTargetBundle
{
    my ($self) = @_;
    return ($self->{'targetBundle'});
}

sub setTargetBundleFromFile
{
    my ($self, $filename) = @_;
    my $targetBundle = new $self->{'type'}, ("",$filename);
    $self->{'targetBundle'} = $targetBundle;
}

# right now theres a lot of copies of data floating around...
# going to head to working, and revisit efficiency later 
# 
sub updateBundle 
{   
    my ($self) = @_;
    my $canonical = $self->getCanonicalBundle();
    my $target = $self->getTargetBundle();
    $self->read();
    $canonical->read();
    $target->read();
    my (@added, @removed, @updated, $modified);
    foreach my $key (@{$canonical->getOrderedKeys()})
    {   
        # ScarabResourceBundle adds a ! at the beginning 
        # of any line that is not a key, usually a spacer
        # or comment
        if ($key =~ s/^\!//)
        {   
            push @updated, "$key\n";
            next;
        }
        my $value = $self->getKeyValue($key);
        if (!defined($value))
        {   
            $value = $canonical->getKeyValue($key);
            push @added, "$key = $value\n";
        }
        elsif ($value ne $target->getKeyValue($key))
        {
            $modified++;
        }
        push @updated, "$key = $value\n";
    }
    foreach my $key (@{$self->getKeys()})
    {   
        my $value = $self->getKeyValue($key);
        if (!defined($canonical->getKeyValue($key)))
        {   
            push @removed, "$key = $value\n";
        }
    }
    $self->{'addedKeys'} = \@added;
    $self->{'removedKeys'} = \@removed;
    $self->{'updated'} = \@updated;
    my $status = (@added) ? "Added " . @added . " key(s). " : ""; 
    $status .= (@removed) ? "Removed " . @removed . " key(s). " : "";
    $status .= ($modified) ? "Modified $modified value(s)." : "";
    return $status;
}

1;

