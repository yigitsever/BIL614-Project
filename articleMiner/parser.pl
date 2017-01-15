use strict;
use warnings;

use LWP::Simple;
use Tie::File; # Not required at all but i like using this
use File::Basename;
use File::Path qw/make_path/;
use Smart::Comments;
use Data::Dumper;

use utf8;
binmode STDIN, ':encoding(UTF-8)';
binmode STDOUT, ':encoding(UTF-8)';

my ( $dataFileName, @rest ) = @ARGV;

if ( not defined $dataFileName ) {
	die "Need data file name";
}

if ( @rest ) {
	warn "Ignoring @rest";
}

my %months;

while (my $cheatRow = <DATA>) {
	chomp $cheatRow;
	my @monthPair = split " ", $cheatRow;
	$months{$monthPair[0]} = $monthPair[1];

}

tie my @dataRows, 'Tie::File', $dataFileName or die "Cannot initialize TieFile, $!";

my $success = 0;
for my $row (@dataRows) {

	my @urlPair = split ",", $row;
	my $url = $urlPair[1];
	my $lastResort = $urlPair[0];
	my $content = get($url);

	if ( not defined $content ) {
		### WHOLE CONTENT IS NOT DEFINED for: $url
		next;
	}

	my $month;


	if ($content =~ /<div class=\"date\">\d+\.0?(1[0-2]|[0-9])\./) { # Numeric, gundem
		# Found month: $1
		$month = $months{$1};
	} elsif ($content =~ /<div class=\"(?:dt|date)\">\d+ (.+?) \d+/) { # Textual, yerel spor
		my $testString = $1;
		chomp $testString;
		# for: $testString
		$month = lc $1;
	} elsif ($content =~ /<em class=\"_b ntime\"><\/em>\s?\d+\.0?(1[0-2]|[0-9])\./) { # siyaset
		$month = $months{$1};
	} elsif ($lastResort =~ /eksisozluk\.com\/\d+-(\w+)-/) {
		# haber icerisinde yayinlanma tarihi yok/cok sacma bir yerde
		$month = $months{$1};
	} else {
		### Does not work for: $url
	}

	my $articleOutputFile = "data/$month/$success";
	my $dir = dirname($articleOutputFile);

	unless ( -d $dir ) { # no exists
		make_path($dir);
	}

	open(my $articleWriter, '>:encoding(UTF-8)', $articleOutputFile) or die "Could not open $success to write, $!";

	my $parsedText;

	#-> headline #
	if ($content =~ /<h1 itemprop=\"headline\">(.+?)<\/h1>/i) {	# Gundem
		$parsedText .= lc " $1  ";
	} elsif ($content =~ /<div class=\"baslik-spot\">(.+?)<\/div>/i) {	# Spor
		$parsedText .= lc " $1 ";
	} elsif ($content =~ /<h1>(.+?)<\/h2>/i) { # magazin
		$parsedText .= lc " $1 ";
	} else {
		# No headline found for: $url
	}

	#-> sub-headline #
	if ($content =~ /<h2 itemprop=\"description\">(.+?)<\/h2>/i) { # Gundem
		$parsedText .= lc " $1 ";
	} else {
		# No subheadline found for: $url
		# Also we handled sub-headline for Spor, magazin at headline
	}

	#-> articleBody #
	if ($content =~ /<div itemprop=\"articleBody\">(.+?)<\/div>/i) { # Gundem
		$parsedText .= lc " $1 ";
	} elsif ($content =~ /<div id=\"divAdnetKeyword3\" class=\"text\" itemprop=\"articleBody\">/i) {
		$parsedText .= lc " $1 ";
	} elsif ($content =~ /<div id=\"divAdnetKeyword3\" class=\"article\">(.+?)<\/div>/) {
		$parsedText .= lc " $1 ";
	} else {
		# $parsedText = $url . "\t<===";
		# No articleBody found for: $url
	}

	if ( not defined $parsedText ) {
		### parsedText not defined for: $url
	} else {
		removeTags($parsedText);
		$success++;
		print $articleWriter $parsedText;
	}

	close $articleWriter;
}

print "Rules work for $success / $#dataRows\n";

sub removeTags {
	$_[0] =~ s/<.+?>//g;

	return $_[0];
}


__DATA__
0 null
1 ocak
2 şubat
3 mart
4 nisan
5 mayıs
6 haziran
7 temmuz
8 ağustos
9 eylül
10 ekim
11 kasım
12 aralık
ocak ocak
subat şubat
mart mart
nisan nisan
mayis mayıs
haziran haziran
temmuz temmuz
agustos ağustos
eylul eylül
ekim ekim
kasim kasım
aralik aralık
