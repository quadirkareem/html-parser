package com.ciphercloud.parsers;

import com.ciphercloud.parsers.htmlcontentextractor.ContentNode;
import com.ciphercloud.parsers.htmlcontentextractor.Node;
import com.ciphercloud.parsers.htmlcontentextractor.TagNode;
import com.ciphercloud.parsers.htmlstreamreader.SimpleHtmlStreamReader;

public class SimpleHtmlStreamReaderHandler extends HtmlContentExtractorHandler {

	public SimpleHtmlStreamReaderHandler() {
	}
	
	public SimpleHtmlStreamReaderHandler(boolean doPrint) {
		super(doPrint);
	}

	@Override
	public String rewrite(String originalHtml) {
		String rewrittenHtml = originalHtml;

		if (originalHtml != null && originalHtml.length() > 0) {
			SimpleHtmlStreamReader parser = new SimpleHtmlStreamReader(
					originalHtml);
			// StringBuilder resultBuilder = new StringBuilder();
			// int lastSegmentEnd = 0;
			while(parser.hasNext()) {
				Node node = parser.next();
				String nodeData = node.getText();
				String nodeType;
				if (node instanceof TagNode) {
					String s1 = nodeData;// System.out.println("TAG:\n" + seg);
					nodeType = "Tag: ";
				} else {
					String s3 = nodeData;
					nodeType = "Content: ";
				}
				if(doPrint) {
					System.out.println(nodeType + node);
				}
			}
		}
		return rewrittenHtml;
	}
}
