package com.ciphercloud.parsers.htmlcontentextractor;

public class ContentNode extends Node {
	
	public ContentNode(String data) {
		this.text = data;
	}
	
	@Override
	public String toString() {
		return this.text;
	}
	
}