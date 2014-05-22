package net.maeph.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PhoneNumberFilterTest {


    
    public static final Version MATCH_VERSION = Version.LUCENE_47;
    private Directory index;
    
    private Analyzer analyzerUnderTest = new PhoneNumberAnalyzer(MATCH_VERSION);
    private IndexWriterConfig config;

    @Before 
    public void setUp() {
        index = new RAMDirectory();

        config = new IndexWriterConfig(MATCH_VERSION, analyzerUnderTest);

    }
    
    @Test
    public void shouldIndexPolishPhoneNumbers() throws IOException, ParseException {

        IndexWriter w = new IndexWriter(index, config);

        addDoc(w, "maeph", "+48555781744");
        w.close();

        Query q = new QueryParser(MATCH_VERSION, "phone", new PhoneNumberAnalyzer(MATCH_VERSION)).parse("555781744");

        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        getIndexSearcher().search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        
        Document doc = getIndexSearcher().doc(hits[0].doc);
        Assert.assertEquals("maeph", doc.get("name"));
        
    }

    private IndexSearcher getIndexSearcher() throws IOException {
        IndexReader reader = DirectoryReader.open(index);
        return new IndexSearcher(reader);
    }

    private static void addDoc(IndexWriter w, String name, String phone) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("name", name, Field.Store.YES));
        doc.add(new TextField("phone", phone, Field.Store.YES));
        w.addDocument(doc);
    }
}
