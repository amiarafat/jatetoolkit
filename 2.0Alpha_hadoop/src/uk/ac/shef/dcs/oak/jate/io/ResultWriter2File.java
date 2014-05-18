package uk.ac.shef.dcs.oak.jate.io;

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndex;
import uk.ac.shef.dcs.oak.jate.model.Term;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * A helper class which outputs term recognition results
 * 
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */

public class ResultWriter2File {

	private GlobalIndex _index;

	/**
	 * @param index
	 *            an instance of GlobalIndexMem. The writer will read mapping
	 *            data from term canonical from to variant forms and output the
	 *            result
	 */

	/* modified code begins:RAKE */

	public ResultWriter2File() {
		_index = null;
	}

	/* modified code ends:RAKE */

	public ResultWriter2File(GlobalIndex index) {
		this._index = index;
	}

	/**
	 * Output the result. This writes one term on a line in the format of:
	 * <p>
	 * canonical |variant1 |variant2 |variantX confidence
	 * </p>
	 * 
	 * @param result
	 *            term recognition result sorted descendingly by confidence
	 * @param path
	 *            output file path
	 * @throws uk.ac.shef.dcs.oak.jate.JATEException
	 */
	public void output(Term[] result, String path) throws JATEException {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(path));
			if (_index == null) {
				for (Term c : result) {
					pw.println(c.getConcept() + "\t\t\t" + c.getConfidence());
				}
			} else {
				for (Term c : result) {
					Set<String> originals = _index
							.retrieveVariantsOfTermCanonical(c.getConcept());
					if (originals == null || originals.size() == 0) {
						pw.println(c.getConcept() + "\t\t\t"
								+ c.getConfidence());
					} else {
						pw.println(c.getConcept() + " |"
								+ writeToString(originals) + "\t\t\t"
								+ c.getConfidence());
					}
				}
			}
			pw.close();
		} catch (IOException ioe) {
			throw new JATEException(ioe);
		}
	}

	public void output(Term[] result, Context context) throws JATEException,
			InterruptedException {
		try {
			if (_index == null) {
				for (Term c : result) {
					context.write(new Text(c.getConcept() + "\t\t\t"),
							new Text(String.valueOf(c.getConfidence())));
				}
			} else {
				for (Term c : result) {
					Set<String> originals = _index
							.retrieveVariantsOfTermCanonical(c.getConcept());
					if (originals == null || originals.size() == 0) {
						context.write(new Text(c.getConcept() + "\t\t\t"),
								new Text(String.valueOf(c.getConfidence())));
					} else {
						context.write(new Text(c.getConcept() + " |"
								+ writeToString(originals) + "\t\t\t"),
								new Text(String.valueOf(c.getConfidence())));
					}
				}
			}
		} catch (IOException ioe) {
			throw new JATEException(ioe);
		}
	}

	private String writeToString(Set<String> container) {
		StringBuilder sb = new StringBuilder();
		for (String s : container) {
			sb.append(s).append(" |");
		}
		return sb.toString().substring(0, sb.lastIndexOf("|"));
	}
}
