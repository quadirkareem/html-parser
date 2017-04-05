package com.ciphercloud.parsers.htmlcontentextractor;

public class SkipNode extends Node {
	
	protected String name;
	
	public SkipNode(String name, String text) {
		this.text = text;
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return "(" + this.name + ") " + this.text;
	}
}