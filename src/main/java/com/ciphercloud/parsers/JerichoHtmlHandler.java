package com.ciphercloud.parsers;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.StreamedSource;
import net.htmlparser.jericho.Tag;

public class JerichoHtmlHandler implements HtmlHandler {

	@Override
	public String rewrite(String originalHtml) {
		String rewrittenHtml = originalHtml;

		if (originalHtml != null && originalHtml.length() > 0) {
			try {
				StartTagType.XML_PROCESSING_INSTRUCTION.deregister();
				Attributes.setDefaultMaxErrorCount(15);
				MicrosoftConditionalCommentTagTypes.register();
				StreamedSource streamedSource = new StreamedSource(originalHtml);
				streamedSource.setLogger(null);
				// StringBuilder resultBuilder = new StringBuilder();
				int lastSegmentEnd = 0;
				for (Segment segment : streamedSource) {
					// if (segment.getEnd() <= lastSegmentEnd)
					// continue;
					// lastSegmentEnd = segment.getEnd();
					// // do not encrypt within <head> tags
					String segmentString = segment.toString();
					if (segment instanceof Tag) {
						String s1 = segmentString;
					} else {
						String s2 = segmentString;
					}
				}
				streamedSource.close();
			} catch (Exception e) {
				throw new RuntimeException(
						"Exception while handling Rich Text", e);
			}
		}
		return rewrittenHtml;
	}
}
