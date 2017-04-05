package com.ciphercloud.parsers;

import com.ciphercloud.parsers.htmlcontentextractor.Node;
import com.ciphercloud.parsers.htmlcontentextractor.SimpleHtmlContentExtractor;
import com.ciphercloud.parsers.htmlcontentextractor.TagNode;

public class SimpleHtmlContentExtractorHandler extends HtmlContentExtractorHandler {

	public SimpleHtmlContentExtractorHandler() {
	}
	
	public SimpleHtmlContentExtractorHandler(boolean doPrint) {
		super(doPrint);
	}

	@Override
	public String rewrite(String originalHtml) {
		String rewrittenHtml = originalHtml;

		if (originalHtml != null && originalHtml.length() > 0) {
			SimpleHtmlContentExtractor parser = new SimpleHtmlContentExtractor(
					originalHtml);
			// StringBuilder resultBuilder = new StringBuilder();
			// int lastSegmentEnd = 0;
			for (Node node : parser.getNodes()) {
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
