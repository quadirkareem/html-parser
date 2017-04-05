package com.ciphercloud.parsers.htmlcontentextractor;

import java.util.ArrayList;
import java.util.List;

public class HtmlContentExtractor {

	enum State {
		INIT, TAG, DATA, SKIP_TAGS, SKIP_DATA
	};

	enum TagState {
		START_TAG, END_TAG, ATR_NAME, ATR_VAL, ATR_VAL_QUOTE, ATR_VAL_APOS, DATA
	};

	enum SkipDataState {
		START_TAG, END_TAG, ATR_NAME, ATR_VAL, ATR_VAL_QUOTE, ATR_VAL_APOS, DATA
	}

	private static final int REGION_INIT_SIZE = 1024;
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
	private static final String[] SKIP_TAG_ARRAY = new String[] { "textarea", "object", "applet", "frameset", "head" };
	private static final String[] SKIP_DATA_ARRAY = new String[] { "script", "style" };
	
	private List<Node> nodes;
	private char[] htmlChars;
	private State state;
	private TagState tagState;
	private boolean isClosingTag;
	private String currentSkipTag;
	private String currentSkipDataTag;
	private StringBuilder skipTagRegion;
	private StringBuilder skipDataRegion;
	
	public HtmlContentExtractor(String html) {
		htmlChars = html.toCharArray();
		parse();
	}

	private void parse() {
		nodes = new ArrayList<Node>(LIST_INIT_SIZE);
		state = State.INIT;
		skipTagRegion = new StringBuilder(REGION_INIT_SIZE);
		skipDataRegion = new StringBuilder(REGION_INIT_SIZE);

		StringBuilder nodeData = new StringBuilder(DATA_INIT_SIZE);
		StringBuilder tagname = new StringBuilder(TAGNAME_INIT_SIZE);

		char current = '\0';
		char prev = '\0';
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
				parseTag(nodeData, tagname, current, prev, next);
				break;

			case SKIP_TAGS:
				parseSkipTag(nodeData, tagname, current, next);
				break;

			case SKIP_DATA:
				parseSkipData(nodeData, tagname, current, next);
				break;

			}

			nodeData.append(current);
			prev = current;
		}

		if (nodeData.length() > 0) {
			if (state == State.TAG && tagState == TagState.END_TAG) {
				saveTag(tagname.toString(), nodeData.toString());
			} else if (state == State.SKIP_TAGS) {
				if (tagState == TagState.END_TAG) {
					saveSkipNode(tagname.toString(), skipTagRegion.toString());
					saveTag(tagname.toString(), nodeData.toString());
				} else {
					saveSkipNode(tagname.toString(),
							skipTagRegion.append(nodeData).toString());
				}
			} else if (state == State.SKIP_DATA) {
				if (tagState == TagState.END_TAG) {
					saveSkipNode(tagname.toString(), skipDataRegion.toString());
					saveTag(tagname.toString(), nodeData.toString());
				} else {
					saveSkipNode(tagname.toString(),
							skipDataRegion.append(nodeData).toString());
				}
			}
			else {
				saveContent(nodeData.toString());
			}
		}
	}

	private void parseSkipData(StringBuilder nodeData, StringBuilder tagname,
			char current, char next) {
		switch (tagState) {
		case START_TAG:
			if (current == GT) {
				tagState = TagState.END_TAG;
			} else {
				tagname.append(current);
			}
			break;

		case END_TAG:
			if(isSkipDataEnd(tagname)) {
				if (isTag(current, next)) {
					state = State.TAG;
					tagState = TagState.START_TAG;
				} else {
					state = State.DATA;
					tagState = null;
				}
				saveSkipNode(tagname.toString(), skipDataRegion.toString());
				saveTag(tagname.toString(), nodeData.toString());
				skipDataRegion.delete(0, skipDataRegion.length());
			}
			else {
				skipDataRegion.append(nodeData);
				if (isTag(current, next)) {
					tagState = TagState.START_TAG;
				} else {
					tagState = TagState.DATA;
					skipDataRegion.append(current);
				}
			}
			nodeData.delete(0, nodeData.length());
			tagname.delete(0, tagname.length());
			break;
		
		case DATA:
			if (isTag(current, next)) {
				tagState = TagState.START_TAG;
				nodeData.delete(0, nodeData.length());
			} else {
				skipDataRegion.append(current);
			}
			break;
		}
	}

	private void parseSkipTag(StringBuilder nodeData, StringBuilder tagname,
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
			if (isSkipTagEnd(tagname)) {
				if (isTag(current, next)) {
					state = State.TAG;
					tagState = TagState.START_TAG;
				} else {
					state = State.DATA;
					tagState = null;
				}
				saveSkipNode(tagname.toString(), skipTagRegion.toString());
				saveTag(tagname.toString(), nodeData.toString());
				skipTagRegion.delete(0, skipTagRegion.length());
			} else {
				skipTagRegion.append(nodeData);
				if (isTag(current, next)) {
					tagState = TagState.START_TAG;
				} else {
					tagState = TagState.DATA;
					skipTagRegion.append(current);
				}
			}
			nodeData.delete(0, nodeData.length());
			tagname.delete(0, tagname.length());
			break;

		case DATA:
			if (isTag(current, next)) {
				tagState = TagState.START_TAG;
				nodeData.delete(0, nodeData.length());
			} else {
				skipTagRegion.append(current);
			}
			break;
		}
	}

	private void parseTag(StringBuilder nodeData, StringBuilder tagname,
			char current, char prev, char next) {
		switch (tagState) {
		case START_TAG:
			if (current == GT) {
				tagState = TagState.END_TAG;
				if (prev == SLASH) {
					isClosingTag = true;
				}
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
				if (prev == SLASH) {
					isClosingTag = true;
				}
			} else if (current == EQUAL) {
				tagState = TagState.ATR_VAL;
			}
			break;

		case ATR_VAL:
			if (current == GT) {
				tagState = TagState.END_TAG;
				if (prev == SLASH) {
					isClosingTag = true;
				}
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
			if (!isClosingTag && isSkipTagStart(tagname)) {
				state = State.SKIP_TAGS;
				skipTagRegion.delete(0, skipTagRegion.length());
				if (isTag(current, next)) {
					tagState = TagState.START_TAG;
				} else {
					tagState = TagState.DATA;
				}
				isClosingTag = false;
			} else if (!isClosingTag && isSkipDataStart(tagname)) {
				state = State.SKIP_DATA;
				skipDataRegion.delete(0, skipTagRegion.length());
				if (isTag(current, next)) {
					tagState = TagState.START_TAG;
				} else {
					tagState = TagState.DATA;
				}
				isClosingTag = false;
			} else if (isTag(current, next)) {
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

	private boolean isSkipTagStart(StringBuilder tagname) {
		boolean isSkipTagStart = false;
		
		for(String skipTag : SKIP_TAG_ARRAY) {
			if((tagname.length() == skipTag.length())
					&& skipTag.equalsIgnoreCase(tagname.toString())) {
				isSkipTagStart = true;
				currentSkipTag = skipTag;
				break;
			}
		}
		
		return isSkipTagStart;
	}

	private boolean isSkipTagEnd(StringBuilder tagname) {
		final String currentSkipTagEnd = "/" + currentSkipTag;
		boolean isSkipTagEnd = false;
		
		if((tagname.length() == currentSkipTagEnd.length())
				&& currentSkipTagEnd.equalsIgnoreCase(tagname.toString())) {
			isSkipTagEnd = true;
			currentSkipTag = null;
		}
		
		return isSkipTagEnd;
	}

	private boolean isSkipDataStart(StringBuilder tagname) {
		boolean isSkipDataStart = false;
		
		for(String skipData : SKIP_DATA_ARRAY) {
			if((tagname.length() == skipData.length())
				&& skipData.equalsIgnoreCase(tagname.toString())) {
				isSkipDataStart = true;
				currentSkipDataTag = skipData;
				break;
			}
		}
		
		return isSkipDataStart;
	}

	private boolean isSkipDataEnd(StringBuilder tagname) {
		final String currentSkipDataTagEnd = "/" + currentSkipDataTag;
		boolean isSkipDataEnd = false;
		
		if((tagname.length() == currentSkipDataTagEnd.length())
				&& currentSkipDataTagEnd.equalsIgnoreCase(tagname.toString())) {
			isSkipDataEnd = true;
			currentSkipDataTag = null;
		}
		
		return isSkipDataEnd;
	}


	private boolean isTag(char current, char next) {
		return current == LT
				&& (next == EXCL || next == SLASH || Character.isLetter(next));
	}

	private void saveContent(String data) {
		nodes.add(new ContentNode(data));
	}

	private void saveSkipNode(String tagname, String data) {
		nodes.add(new SkipNode(tagname, data));
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
