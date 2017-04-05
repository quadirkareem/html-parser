package com.ciphercloud.parsers.htmlcontentextractor;

public class TagNode extends Node {

	enum TagType { START, END, BOTH }
	
	protected TagType type;
	protected String name;
	
	public TagNode(String name, String text) {
		this.text = text;
		if(name.charAt(0) == '/') {
			this.name = name.substring(1);
			this.type = TagType.END;
		}
		else {
			this.name = name;
			if(text.charAt(text.length()-2) == '/') {
				this.type = TagType.BOTH;
			}
			else {
				this.type = TagType.START;
			}
		}
	}
	
	public String getName() {
		return this.name;
	}

	public TagType getType() {
		return this.type;
	}
	

	@Override
	public String toString() {
		return "(" + this.type + ":" + this.name + ") " + this.text;
	}
	
}