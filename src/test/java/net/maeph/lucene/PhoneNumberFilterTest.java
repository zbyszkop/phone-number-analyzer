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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PhoneNumberFilterTest {


    
    public static final Version MATCH_VERSION = Version.LUCENE_47;
    private Directory index;
    
    private Analyzer analyzerUnderTest = new PhoneNumberAnalyzer(MATCH_VERSION);
    private IndexWriterConfig config;
    private IndexWriter writer;

    @Before 
    public void setUp() throws IOException {
        prepareIndexer(analyzerUnderTest);
    }

    private void prepareIndexer(Analyzer analyzer) throws IOException {
        index = new RAMDirectory();

        config = new IndexWriterConfig(MATCH_VERSION, analyzer);

        writer = new IndexWriter(index, config);
    }

    @Test
    public void shouldAllowSearchingByPolishNumbers() throws ParseException, IOException {
        //given
        addDoc(writer, "maeph", "+48555781744");
        writer.close();
        //when
        Query q = new QueryParser(MATCH_VERSION, "phone", new PhoneNumberAnalyzer(MATCH_VERSION)).parse("555781744");

        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        getIndexSearcher().search(q, collector);
        //then
        Assert.assertEquals("maeph", getFirstDoc(collector).get("name"));
        
    }

    @Test
    public void shouldNotAllowSearchingByNonPolishNumbers() throws ParseException, IOException {
        //given
        addDoc(writer, "maeph", "555781744");
        writer.close();
        //when
        Query q = new QueryParser(MATCH_VERSION, "phone", new PhoneNumberAnalyzer(MATCH_VERSION)).parse("+47555781744");

        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        getIndexSearcher().search(q, collector);
        
        //then
        Assert.assertTrue(collector.getTotalHits() == 0);

    }

    @Test
    public void shouldAllowSearchingByNonPolishNumbersWhenLocaleSet() throws ParseException, IOException {
        PhoneNumberAnalyzer analyzer = new PhoneNumberAnalyzer(MATCH_VERSION, "GB");
        prepareIndexer(analyzer);

        //given
        addDoc(writer, "maeph", "20749990000");
        writer.close();
        //when
        Query q = new QueryParser(MATCH_VERSION, "phone", analyzer).parse("+4420749990000");

        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        getIndexSearcher().search(q, collector);

        //then
        Assert.assertEquals("maeph", getFirstDoc(collector).get("name"));

    }

    private Document getFirstDoc(TopScoreDocCollector collector) throws IOException {
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        return getIndexSearcher().doc(hits[0].doc);
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
