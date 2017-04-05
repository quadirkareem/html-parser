package com.ciphercloud.parsers.htmlstreamreader;

import com.ciphercloud.parsers.htmlcontentextractor.ContentNode;
import com.ciphercloud.parsers.htmlcontentextractor.Node;
import com.ciphercloud.parsers.htmlcontentextractor.TagNode;


public class SimpleHtmlStreamReader {

	enum State {
		INIT, TAG, DATA
	};

	enum TagState {
		START_TAG, END_TAG, ATR_NAME, ATR_VAL, ATR_VAL_QUOTE, ATR_VAL_APOS
	};

	private static final int DATA_INIT_SIZE = 1024;
	private static final int TAGNAME_INIT_SIZE = 32;

	private static final char LT = '<';
	private static final char GT = '>';
	private static final char QUOTE = '"';
	private static final char APOS = '\'';
	private static final char EXCL = '!';
	private static final char SLASH = '/';
	private static final char EQUAL = '=';

	private static final String CDATA = "<![CDATA[";

	private char[] htmlChars;
	private State state;
	private TagState tagState;
	StringBuilder nodeData;
	StringBuilder tagname;
	int currentIndex;

	public SimpleHtmlStreamReader(String html) {
		htmlChars = html.toCharArray();
		state = State.INIT;
		nodeData = new StringBuilder(DATA_INIT_SIZE);
		tagname = new StringBuilder(TAGNAME_INIT_SIZE);
		currentIndex = 0;
	}

	public boolean hasNext() {
		if (currentIndex >= htmlChars.length && nodeData.length() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public Node next() throws IllegalStateException {
		if (hasNext()) {
			return parseNext();
		} else {
			throw new IllegalStateException(
					"End of HTML Document, no more nodes available");
		}
	}

	private Node parseNext() {
		Node node = null;
		char current = '\0';
		char next = '\0';
		for (; currentIndex < htmlChars.length; currentIndex++) {
			if (node != null) {
				break;
			}
			current = htmlChars[currentIndex];
			if (currentIndex + 1 < htmlChars.length) {
				next = htmlChars[currentIndex + 1];
			} else {
				next = '\0';
			}

			switch (state) {
			case INIT:
				if (isTag(current, next)) {
					state = State.TAG;
					tagState = TagState.START_TAG;
				} else {
					state = State.DATA;
				}
				break;

			case DATA:
				if (isTag(current, next)) {
					state = State.TAG;
					tagState = TagState.START_TAG;
					node = createContentNode();
				}
				break;

			case TAG:
				node = parseTag(current, next);
				break;

			}

			nodeData.append(current);
		}

		if (node == null && currentIndex >= htmlChars.length && nodeData.length() > 0) {
			if (state == State.TAG && tagState == TagState.END_TAG) {
				node = createTagNode();
			} else {
				node = createContentNode();
			}
		}

		return node;
	}

	private TagNode parseTag(char current, char next) {
		TagNode node = null;

		switch (tagState) {
		case START_TAG:
			if (current == GT) {
				tagState = TagState.END_TAG;
			} else if (Character.isSpaceChar(current)) {
				tagState = TagState.ATR_NAME;
			} else {
				tagname.append(current);
			}

			if (nodeData.length() == CDATA.length()
					&& CDATA.equalsIgnoreCase(nodeData.toString())) {
				state = State.DATA;
				tagState = null;
			}
			break;

		case ATR_NAME:
			if (current == GT) {
				tagState = TagState.END_TAG;
			} else if (current == EQUAL) {
				tagState = TagState.ATR_VAL;
			}
			break;

		case ATR_VAL:
			if (current == GT) {
				tagState = TagState.END_TAG;
			} else if (current == QUOTE) {
				tagState = TagState.ATR_VAL_QUOTE;
			} else if (current == APOS) {
				tagState = TagState.ATR_VAL_APOS;
			}
			break;

		case ATR_VAL_QUOTE:
			if (current == QUOTE) {
				tagState = TagState.ATR_NAME;
			}
			break;

		case ATR_VAL_APOS:
			if (current == APOS) {
				tagState = TagState.ATR_NAME;
			}
			break;

		case END_TAG:
			if (isTag(current, next)) {
				tagState = TagState.START_TAG;
			} else {
				state = State.DATA;
				tagState = null;
			}
			node = createTagNode();
			break;

		}

		return node;
	}

	private boolean isTag(char current, char next) {
		return current == LT
				&& (next == EXCL || next == SLASH || Character.isLetter(next));
	}

	private ContentNode createContentNode() {
		ContentNode contentNode = new ContentNode(nodeData.toString());
		nodeData.delete(0, nodeData.length());
		return contentNode;
	}

	private TagNode createTagNode() {
		TagNode tagNode = new TagNode(tagname.toString(), nodeData.toString());
		tagname.delete(0, tagname.length());
		nodeData.delete(0, nodeData.length());
		return tagNode;
	}

	/*
	 * public static void main(String[] args) { StringBuilder sb = new
	 * StringBuilder(256); System.out.println(sb.toString() + ": length = " +
	 * sb.length() + ", capacity = " + sb.capacity()); sb.append("hello");
	 * System.out.println(sb.toString() + ": length = " + sb.length() +
	 * ", capacity = " + sb.capacity()); String s = sb.toString(); sb.delete(0,
	 * 2); System.out.println(sb.toString() + ": length = " + sb.length() +
	 * ", capacity = " + sb.capacity());
	 * 
	 * sb.append("chalo bolo"); System.out.println(sb.toString() + ": length = "
	 * + sb.length() + ", capacity = " + sb.capacity());
	 * 
	 * }
	 */
}
