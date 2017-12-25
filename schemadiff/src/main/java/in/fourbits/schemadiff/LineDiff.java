package in.fourbits.schemadiff;

import difflib.Chunk;

public class LineDiff {

	public LineDiff() {

	}

	public LineDiff(Chunk before, Chunk after) {
		this.before = before;
		this.after = after;
	}

	private Chunk before;
	private Chunk after;

	public Chunk getBefore() {
		return before;
	}

	public void setBefore(Chunk before) {
		this.before = before;
	}

	public Chunk getAfter() {
		return after;
	}

	public void setAfter(Chunk after) {
		this.after = after;
	}

}
