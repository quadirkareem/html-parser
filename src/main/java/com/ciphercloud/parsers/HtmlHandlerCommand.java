package com.ciphercloud.parsers;

import com.ciphercloud.parsers.metrics.Command;

public class HtmlHandlerCommand implements Command {

	private String html;
	private HtmlHandler handler;

	public HtmlHandlerCommand(String html, HtmlHandler htmlHandler) {
		this.html = html;
		handler = htmlHandler;
	}

	@Override
	public void run() {
		handler.rewrite(html);
	}

}
