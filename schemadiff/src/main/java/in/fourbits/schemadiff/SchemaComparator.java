package in.fourbits.schemadiff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * Using com.googlecode.java-diff-utils library to compare files This provides 3
 * types for Delta Types to find out the differences. (CHANGE, INSERT, DELETE)
 *
 */
public class SchemaComparator {

	private final File original;
	private final File revised;

	public SchemaComparator(File originalFile, File revisedFile) {
		this.original = originalFile;
		this.revised = revisedFile;
	}

	public List<LineDiff> getChangesFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.CHANGE);
	}

	public List<LineDiff> getInsertsFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.INSERT);
	}

	public List<LineDiff> getDeletionFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.DELETE);
	}

	private List<LineDiff> getChunksByType(Delta.TYPE type) throws IOException {
		final List<LineDiff> listOfChanges = new ArrayList<LineDiff>();
		final List<Delta> deltas = getDeltas();
		for (Delta delta : deltas) {
			if (delta.getType() == type) {
				listOfChanges.add(new LineDiff(delta.getOriginal(), delta.getRevised()));
			}
		}
		return listOfChanges;
	}

	private List<Delta> getDeltas() throws IOException {

		final List<String> originalFileLines = fileToLines(original);
		final List<String> revisedFileLines = fileToLines(revised);

		final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines);

		return patch.getDeltas();
	}

	private List<String> fileToLines(File file) throws IOException {
		final List<String> lines = new ArrayList<String>();
		String line;
		final BufferedReader in = new BufferedReader(new FileReader(file));
		while ((line = in.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}

}
