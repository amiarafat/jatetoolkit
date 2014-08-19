package uk.ac.shef.dcs.oak.jate.model;

import uk.ac.shef.wit.commons.UtilFiles;

import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

/**
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */

public class DocumentImpl implements Document {

	protected URL _url;
	protected String fileContent;
	protected FileStatus fileStatus;
	private FileSystem fs;

	public DocumentImpl(URL url) {
		_url = url;
	}

	public DocumentImpl(String fileContent) {
		this.fileContent = fileContent;
	}

	public DocumentImpl(FileStatus fileStatus, FileSystem fs) {
		this.fileStatus = fileStatus;
		this.fs = fs;
	}

	public URL getUrl() {
		return _url;
	}

	public String getContent() {
		// String content = null;
		// try {
		// content = UtilFiles.getContent(_url).toString();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		BufferedReader br = null;
		StringBuffer content = new StringBuffer();
		try {
			if (!getFileStatus().isDirectory()) {
				br = new BufferedReader(new InputStreamReader(getFs().open(
						getFileStatus().getPath())));
				String line;
				line = br.readLine();
				content.append(line + "\n");
				while (line != null) {
					line = br.readLine();
					content.append(line + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != br)
					br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	//	System.out.println("file contens "+ content.toString());
		return content.toString();
	}

	public String toString() {
		return fileStatus.toString();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final DocumentImpl that = (DocumentImpl) o;

		return that.getFileStatus().equals(getFileStatus());

	}

	public int hashCode() {
		return fileStatus.hashCode();
	}

	public FileStatus getFileStatus() {
		return fileStatus;
	}

	public FileSystem getFs() {
		return fs;
	}

}
