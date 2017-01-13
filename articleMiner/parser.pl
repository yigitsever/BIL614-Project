use strict;
use warnings;

use LWP::Simple;
use Tie::File; # Not required at all but i like using this
use Smart::Comments;
use Data::Dumper;

use utf8;

my ( $dataFileName ) = @ARGV;

if ( not defined $dataFileName ) {
	die "Need data file name";
}

tie my @dataRows, 'Tie::File', $dataFileName or die "Cannot initialize TieFile, $!";

open(my $fh, '>:encoding(UTF-8)', 'output') or die "Could not open bla bla $!";

my $success = 0;

for my $row (@dataRows) {
	my @urlPair = split ",", $row;
	my $url = $urlPair[1];
	my $content = get($url);

	my $parsedText;

	#-> headline #
	if ($content =~ /<h1 itemprop=\"headline\">(.+?)<\/h1>/i) {	# Gundem
		$parsedText .= lc "$1  ";
	} elsif ($content =~ /<div class=\"baslik-spot\">(.+?)<\/div>/i) {	# Spor
		$parsedText .= lc "$1 ";
	} else {
		# No headline found for: $url
	}

	#-> sub-headline #
	if ($content =~ /<h2 itemprop=\"description\">(.+?)<\/h2>/i) { # Gundem
		$parsedText .= lc "$1 ";
	} else {
		# No subheadline found for: $url
		# Also we handled sub-headline for Spor at headline
	}

	#-> articleBody #
	if ($content =~ /<div itemprop=\"articleBody\">(.+?)<\/div>/i) { # Gundem
		$parsedText .= lc "$1 ";
	} elsif ($content =~ /<div id=\"divAdnetKeyword3\" class=\"text\" itemprop=\"articleBody\">/i) {
		$parsedText .= lc "$1 ";
	} else {
		# $parsedText = $url . "\t<===";
		# No articleBody found for: $url
	}

	if ( not defined $parsedText ) {
		### parsedText not defined for: $url
	} else {
		removeTags($parsedText);
		$success++;
		print $fh "$parsedText\n";
	}
}

print "Rules work for $success / $#dataRows\n";

sub removeTags {
	$_[0] =~ s/<.+?>//g;

	return $_[0];
}
