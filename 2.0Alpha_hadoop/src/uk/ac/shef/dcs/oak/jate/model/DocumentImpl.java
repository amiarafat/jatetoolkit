package uk.ac.shef.dcs.oak.jate.model;

import uk.ac.shef.wit.commons.UtilFiles;

import java.net.URL;
import java.io.IOException;

/**
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */

public class DocumentImpl implements Document {

	protected URL _url;
	protected String fileContent;

	public DocumentImpl(URL url) {
		_url = url;
	}

	public DocumentImpl(String fileContent) {
		this.fileContent = fileContent;
	}

	public URL getUrl() {
		return _url;
	}

	public String getContent() {

		
		if (null == _url && fileContent != null) {
			return fileContent;
		}

		String content = null;
		try {
			content = UtilFiles.getContent(_url).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public String toString() {
		return fileContent.toString();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final DocumentImpl that = (DocumentImpl) o;

		return that.getUrl().equals(getUrl());

	}

	public int hashCode() {
		return fileContent.hashCode();
	}
}
