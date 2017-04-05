package com.ciphercloud.parsers.htmlcontentextractor;

import java.util.ArrayList;
import java.util.List;


public class SimpleHtmlContentExtractor {

	enum State {
		INIT, TAG, DATA
	};

	enum TagState {
		START_TAG, END_TAG, ATR_NAME, ATR_VAL, ATR_VAL_QUOTE, ATR_VAL_APOS
	};

	private static final int LIST_INIT_SIZE = 1024;
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
	
	private List<Node> nodes;
	private char[] htmlChars;
	private State state;
	private TagState tagState;
	
	public SimpleHtmlContentExtractor(String html) {
		htmlChars = html.toCharArray();
		parse();
	}

	private void parse() {
		nodes = new ArrayList<Node>(LIST_INIT_SIZE);
		state = State.INIT;

		StringBuilder nodeData = new StringBuilder(DATA_INIT_SIZE);
		StringBuilder tagname = new StringBuilder(TAGNAME_INIT_SIZE);

		char current = '\0';
		char next = '\0';
		for (int i = 0; i < htmlChars.length; i++) {
			current = htmlChars[i];
			if (i + 1 < htmlChars.length) {
				next = htmlChars[i + 1];
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
					saveContent(nodeData.toString());
					nodeData.delete(0, nodeData.length());
				}
				break;

			case TAG:
				parseTag(nodeData, tagname, current, next);
				break;

			}

			nodeData.append(current);
		}

		if (nodeData.length() > 0) {
			if (state == State.TAG && tagState == TagState.END_TAG) {
				saveTag(tagname.toString(), nodeData.toString());
			}
			else {
				saveContent(nodeData.toString());
			}
		}
	}
	

	private void parseTag(StringBuilder nodeData, StringBuilder tagname,
			char current, char next) {
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
			saveTag(tagname.toString(), nodeData.toString());
			nodeData.delete(0, nodeData.length());
			tagname.delete(0, tagname.length());
			break;

		}
	}

	private boolean isTag(char current, char next) {
		return current == LT
				&& (next == EXCL || next == SLASH || Character.isLetter(next));
	}

	private void saveContent(String data) {
		nodes.add(new ContentNode(data));
	}

	private void saveTag(String tagname, String data) {
		nodes.add(new TagNode(tagname, data));
	}

	public List<Node> getNodes() {
		return nodes;
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
