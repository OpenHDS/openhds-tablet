package org.openhds.mobile;

public class BadXmlException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadXmlException(String expectedXmlElement) {
		super("Bad XML: Expected element with tag name: " + expectedXmlElement);
	}
}
