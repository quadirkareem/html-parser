package com.ciphercloud.parsers;

import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

public class HtmlCleanerHandler	implements HtmlHandler {
	
	@Override
	public String rewrite(String originalHtml) {
		String rewrittenHtml = originalHtml;

		if (originalHtml != null && originalHtml.length() > 0) {
			// TODO Auto-generated method stub
			HtmlCleaner cleaner = new HtmlCleaner();
			try {
				TagNode node = cleaner.clean(originalHtml);

				// traverse whole DOM and update images to absolute URLs
				node.traverse(new TagNodeVisitor() {
					public boolean visit(TagNode parentNode, HtmlNode htmlNode) {
						if (htmlNode instanceof TagNode) {
							String tag = htmlNode.toString();
//							System.out.println("Tag: " + tag);
						} else if (htmlNode instanceof ContentNode) {
							String content = htmlNode.toString();
//							System.out.println("Content: " + content);
						}
						// tells visitor to continue traversing the DOM tree
						return true;
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(
						"Exception while handling Rich Text", e);
			}
		}

		return rewrittenHtml;
	}

}
