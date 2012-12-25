package com.lin.stride.search;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.lin.stride.search.request.NovelHit;
import com.lin.stride.search.request.NovelSearchRequst;
import com.lin.stride.utils.ConfigReader;

public class LinIndexSearcher {
	private final Logger LOG = Logger.getLogger(LinIndexSearcher.class);

	private Directory directory = null;
	private IndexReader indexReader = null;
	private IndexSearcher indexSearcher = null;
	private static NovelQueryAnalyzer queryAnalyzer = new NovelQueryAnalyzer();
	private final File indexStrorageDir;

	public LinIndexSearcher() {
		indexStrorageDir = new File(ConfigReader.INSTANCE().getIndexStorageDir());
		init();
	}

	private void init() {
		try {
			directory = FSDirectory.open(indexStrorageDir);
			indexReader = DirectoryReader.open(directory);
			indexSearcher = new IndexSearcher(indexReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public NovelHit[] search(NovelSearchRequst keyWord) {
		BooleanQuery query = queryAnalyzer.parser(keyWord);
		NovelHit[] result;
		try {
			TopDocs topdocs = indexSearcher.search(query, 10);
			ScoreDoc[] hits = topdocs.scoreDocs;
			result = new NovelHit[hits.length];
			for (int i = 0; i < hits.length; i++) {
				Document doc = indexSearcher.doc(hits[i].doc);
				NovelHit nhit = new NovelHit();
				nhit.setName(doc.get("name"));
				result[i] = nhit;
			}
			LOG.info(query.toString() + "\t" + hits.length);
		} catch (IOException e) {
			result = new NovelHit[0];
			e.printStackTrace();
		}
		return result;
	}

	public void clearIndexFile() throws IOException {
		indexReader.close();
		directory.close();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
		FileUtils.cleanDirectory(indexStrorageDir);
		LOG.info("clear index success !");
	}

	public int switchIndex() {
		init();
		LOG.info("index object init success !");
		return indexReader.maxDoc();
	}

	public void close() {
		try {
			indexReader.close();
			directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		LinIndexSearcher s = new LinIndexSearcher();
		NovelSearchRequst q = new NovelSearchRequst();
		q.setNovelName("斗");
		NovelHit[] l = s.search(q);
		for (NovelHit qp : l) {
			System.out.println(qp.getName());
		}
		s.close();
	}

}
