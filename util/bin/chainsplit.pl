#!/usr/bin/perl -w
#
my $cloud = shift;
my $cname;
my $printing = 0;
while($line = <>) {
    if ($line =~ /\/CN=/) {
        $cname= $line;
        chomp $cname;
        $cname =~ s/^(.+)\/CN=([^\/]+)(.*)$/$2/;
        $cname =~ s/ /_/g;
    } elsif ($line =~ /---BEGIN CERTIFICATE---/) {
        $printing = 1;
        open(CN, ">$cloud.$cname.pem");
    } elsif ($line =~ /---END CERTIFICATE---/) {
        $printing = 0;
        print CN $line;
        close CN;
    }
    print CN $line unless ($printing == 0);
}
