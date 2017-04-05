package com.ciphercloud.parsers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NaiveHtmlHandler implements HtmlHandler {

	private static final char OPEN = '<';
	private static final char CLOSE = '>';
	private static final char QUOTE = '"';
	private static final char APOSTROPHE = '\'';
	private static final String HTML_COMMENT_START = "<!--";
	private static final String HTML_COMMENT_END = "-->";

	public static final String[] SKIP_TAG_ARRAY = new String[] { "script",
			"applet" };
	public static final Set<String> SKIP_TAGS = new HashSet<String>(
			Arrays.asList(SKIP_TAG_ARRAY));

	enum State {
		TAG_START, TAG_END, DATA, HTML_END, SKIP_TAG_REGION, TAG_COMMENT
	};

	@Override
	public String rewrite(String originalHtml) {
		String rewrittenHtml = originalHtml;

		if (originalHtml != null && originalHtml.length() > 0) {
			String lowerHtml = originalHtml;// .toLowerCase();
			int length = originalHtml.length();
			State state = State.TAG_COMMENT;

			int tagOpenIndex = lowerHtml.indexOf(OPEN);
			if (tagOpenIndex < 0) {
				tagOpenIndex = length;
				state = State.HTML_END;
			}
			if (tagOpenIndex > 0) {
				// printData(originalHtml, 0, tagOpenIndex);
			}

			int current = tagOpenIndex;
			String currentTag = "";
			char currentChar = '\0';
			while (state != State.HTML_END) {
				switch (state) {

				case TAG_COMMENT:
					int commentTagStartEndIndex = current
							+ HTML_COMMENT_START.length() - 1;
					if (commentTagStartEndIndex < length) {
						String commentTagStart = lowerHtml.substring(current,
								commentTagStartEndIndex + 1);
						if (commentTagStart.equals(HTML_COMMENT_START)) {
							int commentTagEndStartIndex = lowerHtml.indexOf(
									HTML_COMMENT_END,
									commentTagStartEndIndex + 1);
							if (commentTagEndStartIndex < 0) {
								state = State.HTML_END;
							} else {
								current = commentTagEndStartIndex
										+ HTML_COMMENT_END.length();
								state = State.TAG_END;
							}
						} else {
							state = State.TAG_START;
						}
					} else {
						state = State.TAG_START;
					}
					break;

				case TAG_START:
					int tagCloseIndex = getTagCloseIndex(lowerHtml, length,
							current);
					if (tagCloseIndex < 0) {
						// System.out.println("INFO: Could not find >");
						// printTag(originalHtml, "", current, length);
						state = State.HTML_END;
					} else {
						int spaceIndex = getSpaceIndex(lowerHtml, length,
								current);
						if (spaceIndex < 0) {
							// System.out
							// .println("INFO: Could not find > or [space]");
							state = State.DATA;
						} else {
							currentTag = lowerHtml.substring(current + 1,
									spaceIndex);
							// printTag(originalHtml, currentTag, current,
							// tagCloseIndex + 1);
							if (SKIP_TAGS.contains(currentTag)) {
								current = tagCloseIndex;
								state = State.SKIP_TAG_REGION;
							} else {
								current = tagCloseIndex + 1;
								state = State.TAG_END;
							}
						}
					}
					break;

				case TAG_END:
					if (current < length) {
						currentChar = lowerHtml.charAt(current);
						if (currentChar == OPEN) {
							state = State.TAG_COMMENT;
						} else {
							state = State.DATA;
						}
					} else {
						state = State.HTML_END;
					}
					break;

				case SKIP_TAG_REGION:
					int regionEndIndex = getRegionEndIndex(lowerHtml, length,
							currentTag, current);
					// System.out
					// .println("Skipping Region for Tag: " + currentTag);
					// printData(originalHtml, current, regionEndIndex);
					current = regionEndIndex;
					state = State.TAG_END;
					break;

				case DATA:
					currentChar = lowerHtml.charAt(current);
					if (currentChar == OPEN) {
						tagOpenIndex = lowerHtml.indexOf(OPEN, current + 1);
					} else {
						tagOpenIndex = lowerHtml.indexOf(OPEN, current);
					}
					if (tagOpenIndex < 0) {
						state = State.HTML_END;
						tagOpenIndex = length;
					} else {
						state = State.TAG_COMMENT;
					}
					// printData(originalHtml, current, tagOpenIndex);
					current = tagOpenIndex;
					break;
				default:
					break;
				}
			}
		}

		return rewrittenHtml;
	}

	private int getRegionEndIndex(String lowerHtml, int length,
			String currentTag, int tagCloseIndex) {
		int regionEndIndex = tagCloseIndex + 1;
		if (lowerHtml.charAt(tagCloseIndex - 1) != '/') {
			String endTag = "</" + currentTag + ">";
			// </hello>dfkjdsfk
			regionEndIndex = lowerHtml.indexOf(endTag, regionEndIndex)
					+ endTag.length();
			/*
			 * boolean foundEndTag = false; while(!foundEndTag) { int
			 * tagOpenIndex = lowerHtml.indexOf("</" + currentTag + ">",
			 * regionEndIndex); if(tagOpenIndex < 0) {
			 * System.out.println("ERROR: Closing tag not found for " +
			 * currentTag); break; } else { if(lowerHtml.charAt(tagOpenIndex+1)
			 * != '/') { tagOpenIndex = lowerHtml.indexOf(OPEN, tagOpenIndex+1);
			 * } else { tagOpenIndex = } if(currentChar == OPEN) { tagOpenIndex
			 * = lowerHtml.indexOf(OPEN, current); }
			 */
		}
		return regionEndIndex;
	}

	public int getSpaceIndex(final String lowerHtml, final int length,
			final int tagOpenIndex) {
		int spaceIndex = -1;
		for (int i = tagOpenIndex + 1; i < length; i++) {
			char c = lowerHtml.charAt(i);
			if (c == CLOSE) {
				spaceIndex = i;
				break;
			}
			if (c == OPEN) {
				break;
			}
			if (Character.isSpaceChar(c)) {
				if (i > tagOpenIndex + 1) {
					spaceIndex = i;
				}
				break;
			}
		}

		return spaceIndex;
	}

	// private int getTagCloseIndex(String s, int length, int start) {
	// return s.indexOf(CLOSE, start);
	// }

	private int getTagCloseIndex(final String s, final int length, int start) {
		int tagCloseIndex = -1;
		int quoteStartIndex = -1;
		int apostropheStartIndex = -1;
		int delimiterStartIndex = -1;
		char delimiter = '\0';

		char close = CLOSE;
		char quote = QUOTE;
		char apos = APOSTROPHE;
		char open = OPEN;
		if (s != null && s.length() > 0) {
			while (start > -1 && start < length) {
				tagCloseIndex = s.indexOf(close, start);
				if (tagCloseIndex < 0) {
					break;
				}
				quoteStartIndex = s.indexOf(quote, start);
				apostropheStartIndex = s.indexOf(apos, start);
				delimiter = quote;
				delimiterStartIndex = quoteStartIndex;
				if (apostropheStartIndex < quoteStartIndex) {
					delimiter = apos;
					delimiterStartIndex = apostropheStartIndex;
				}

				if ((delimiterStartIndex < 0)
						|| (delimiterStartIndex > tagCloseIndex)) {
					// e.g. <html> OR <html>"
					int newTagOpenIndex = s.indexOf(open, start + 1);
					if (newTagOpenIndex > 0 && newTagOpenIndex < tagCloseIndex) {
						// e.g. <hello dskjsdk <bolo
						tagCloseIndex = newTagOpenIndex - 1;
					}
					break;
				}

				int delimiterEndIndex = s.indexOf(delimiter,
						delimiterStartIndex + 1);
				if (delimiterEndIndex < 0) {
					// e.g. <html"a>
					// System.out.println("ERROR: End " + delimiter
					// + " NOT found in Tag");
					break;
				} else {
					start = delimiterEndIndex + 1;
				}
			}

			if (tagCloseIndex < 0) {
				// System.out.println("ERROR: End CLOSE not found");
			}
		}

		return tagCloseIndex;
	}

	public void printTag(String s, String tagName, int start, int end) {
		System.out.println("TAG: " + tagName);
		printSegment(s, start, end);
	}

	public void printData(String s, int start, int end) {
		System.out.println("DATA");
		printSegment(s, start, end);
	}

	public void printSegment(String s, int start, int end) {
		String segment = s.substring(start, end);
		System.out.println(start + "," + end + ":\n" + segment);
	}

}
