#!/usr/bin/perl -w
#
# Leibniz Bioactives Cloud
# Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#==============================================================================
#
# Update the resource files for different languages. All keys must 
# be present in the english localization, otherwise they will get lost.
#
# Do not call this Perl script directly, use i18n.sh instead.
#
# Note: Currently, the resource strings can NOT contain equal signs "="
# 
#
use feature qw( fc );

open(EN, $ARGV[0]) or die "Could not open $ARGV[0]\n";
open(XX, $ARGV[1]) or die "Could not open $ARGV[1]\n";

while($l = <EN>) {
    chomp $l;
    next if($l =~ /^#/);
    @f = split /=/, $l;
    $key = shift @f;
    $en{$key} = join "=", @f; 
}

while($l = <XX>) {
    chomp $l;
    next if($l =~ /^#/);
    @f = split /=/, $l;
    $key = shift @f;
    $xx{$key} = join "=", @f;
#    $xx{$f[0]} = $f[1];
}

close(EN);
close(XX);
open(XX, ">$ARGV[1]") or die "Could not open $ARGV[1]\n";

print XX "# 00
# 01  Leibniz Bioactives Cloud
# 02  Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
# 03
# 04  Licensed under the Apache License, Version 2.0 (the \"License\");
# 05  you may not use this file except in compliance with the License.
# 06  You may obtain a copy of the License at
# 07
# 08     http://www.apache.org/licenses/LICENSE-2.0
# 09
# 10  Unless required by applicable law or agreed to in writing, software
# 11  distributed under the License is distributed on an \"AS IS\" BASIS,
# 12  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# 13  See the License for the specific language governing permissions and
# 14  limitations under the License.
# 15
# 16  This file may be modified by the i18n utility!
# 17\n";

foreach $i ( sort { (fc $a) cmp (fc $b)} keys %en )
{
    if(defined $xx{$i})
    {
        print XX "$i=$xx{$i}\n";
    } else {
        print XX "$i=TRANSLATE: $en{$i}\n";
    }
}
close(XX);
