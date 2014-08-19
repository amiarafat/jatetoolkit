package uk.ac.shef.dcs.oak.jate.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */

public class CorpusImpl implements Corpus {

	private Set<Document> _docs;

	public CorpusImpl() {
		_docs = new HashSet<Document>();
	}

	public Iterator<Document> iterator() {
		return _docs.iterator();
	}

	public CorpusImpl(String hdfsPath) {
		_docs = new HashSet<Document>();
		// File targetFolder = new File(path);
		//
		// File[] files = targetFolder.listFiles();
		// for (File f : files) {
		// try {
		// this.add(new DocumentImpl(f.toURI().toURL()));
		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// }
		// }

		FileSystem fs;

		try {
			fs = FileSystem.get(new Configuration());
			FileStatus[] status = fs.listStatus(new Path(hdfsPath));
			
			for (FileStatus fileStatus : status) {
				System.out.println("**** file status "+fileStatus);
				this.add(new DocumentImpl(fileStatus,fs));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}

	public boolean add(Document document) {
		return _docs.add(document);
	}

	public boolean contains(Document document) {
		return _docs.contains(document);
	}

	public int size() {
		return _docs.size();
	}

	public boolean remove(Document doc) {
		return _docs.remove(doc);
	}

}
