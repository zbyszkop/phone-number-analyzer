package net.maeph.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;


public class PhoneNumberAnalyzer extends Analyzer {
    private Version matchVersion;
    private String country = "PL";

    public PhoneNumberAnalyzer(Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    public PhoneNumberAnalyzer(Version matchVersion, String country) {
        this(matchVersion);
        this.country = country;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final Tokenizer source = new StandardTokenizer(matchVersion, reader);
        TokenStream sink = new StandardFilter(matchVersion, source);
        sink = new PhoneNumberFilter(sink, country);
        return new TokenStreamComponents(source, sink);
    }
}
