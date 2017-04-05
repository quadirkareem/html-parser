package com.ciphercloud.parsers;

import com.ciphercloud.parsers.htmlcontentextractor.HtmlContentExtractor;
import com.ciphercloud.parsers.htmlcontentextractor.Node;
import com.ciphercloud.parsers.htmlcontentextractor.SkipNode;
import com.ciphercloud.parsers.htmlcontentextractor.TagNode;

public class HtmlContentExtractorHandler implements HtmlHandler {

	protected boolean doPrint;

	public HtmlContentExtractorHandler() {
	}

	public HtmlContentExtractorHandler(boolean doPrint) {
		this.doPrint = doPrint;
	}

	@Override
	public String rewrite(String originalHtml) {
		String rewrittenHtml = originalHtml;

		if (originalHtml != null && originalHtml.length() > 0) {
			HtmlContentExtractor parser = new HtmlContentExtractor(originalHtml);
			// StringBuilder resultBuilder = new StringBuilder();
			// int lastSegmentEnd = 0;
			for (Node node : parser.getNodes()) {
				String nodeData = node.getText();
				String nodeType;
				if (node instanceof TagNode) {
					String s1 = nodeData;
					nodeType = "Tag: ";
				} else if (node instanceof SkipNode) {
					String s2 = nodeData;
					nodeType = "Skip: ";
				} else {
					String s3 = nodeData;
					nodeType = "Content: ";
				}
				if (doPrint) {
					System.out.println(nodeType + node);
				}
			}
		}
		return rewrittenHtml;
	}
}
