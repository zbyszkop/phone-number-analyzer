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
import org.junit.Test;

import java.io.IOException;

/**
 * Created by mephi_000 on 22.05.14.
 */
public class PhoneNumberFilterTest {


    public static final Version MATCH_VERSION = Version.LUCENE_47;

    @Test
    public void shouldIndexPolishPhoneNumbers() throws IOException, ParseException {


        Analyzer analyzer = new PhoneNumberAnalyzer(MATCH_VERSION);
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(MATCH_VERSION, analyzer);

        IndexWriter w = new IndexWriter(index, config);
        addDoc(w, "maeph", "0048555781744");
        w.close();

        String querystr = "maeph";
        Query q = new QueryParser(MATCH_VERSION, "name", new StandardAnalyzer(MATCH_VERSION)).parse(querystr);

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        Document doc = searcher.doc(hits[0].doc);
        Assert.assertEquals("+48555781744", doc.get("phone"));
        
    }

    private static void addDoc(IndexWriter w, String name, String phone) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("name", name, Field.Store.YES));
        doc.add(new TextField("phone", phone, Field.Store.YES));
        w.addDocument(doc);
    }
}
