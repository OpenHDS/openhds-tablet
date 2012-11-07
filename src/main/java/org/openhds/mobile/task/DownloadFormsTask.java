package org.openhds.mobile.task;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.FormSubmissionRecord;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.Context;
import org.openhds.mobile.BadXmlException;

/**
 * AsyncTask that fetches partial forms (and potentially associated validation
 * failure messages for those forms) and stores them in SQL Lite DB
 */
public class DownloadFormsTask extends AbstractHttpTask<Void, Void> {

	private DatabaseAdapter storage;


	public DownloadFormsTask(RequestContext requestCtx, TaskListener listener,
			Context context) {
		super(requestCtx, listener);
		storage = new DatabaseAdapter(context);
	}

	protected EndResult handleResponseData(HttpResponse response) {
		try {
			HttpEntity entity = response.getEntity();
			StringReader reader;
			reader = new StringReader(EntityUtils.toString(entity));
			List<FormSubmissionRecord> records = parseResponseXml(reader);
			for(FormSubmissionRecord rec : records) {
				rec.setFormOwnerId(requestCtx.user);
			}
			
			saveRecords(records);
			return EndResult.SUCCESS;
		} catch (ParseException e) {
			return EndResult.FAILURE;
		} catch (IOException e) {
			return EndResult.FAILURE;
		} catch (BadXmlException e) {
			return EndResult.FAILURE;
		}
	}

	private List<FormSubmissionRecord> parseResponseXml(StringReader reader)
			throws BadXmlException, IOException {
		try {
			XmlPullParser parser = buildXmlPullParser(reader);
			validateStartOfXmlDocument(parser);
			validateDocumentElement(parser);
			return parseSubmissionSet(parser);
		} catch (XmlPullParserException e) {
			throw new BadXmlException("Bad XML Document");
		}
	}

	private XmlPullParser buildXmlPullParser(StringReader reader)
			throws XmlPullParserException {
		XmlPullParser parser = XmlPullParserFactory.newInstance()
				.newPullParser();
		parser.setInput(reader);
		return parser;
	}

	private void validateStartOfXmlDocument(XmlPullParser parser)
			throws XmlPullParserException, BadXmlException, IOException {
		int eventType = parser.getEventType();
		if (eventType != XmlPullParser.START_DOCUMENT) {
			throw new BadXmlException("Start of document");
		}
	}

	private void validateDocumentElement(XmlPullParser parser)
			throws XmlPullParserException, IOException, BadXmlException {
		int eventType = parser.next();
		if (!isStartTag(eventType)
				&& !"formSubmissionSet".equals(parser.getName())) {
			throw new BadXmlException("formSubmissionSet");
		}
	}

	private boolean isStartTag(int eventType) {
		return eventType == XmlPullParser.START_TAG;
	}

	private List<FormSubmissionRecord> parseSubmissionSet(XmlPullParser parser)
			throws XmlPullParserException, IOException, BadXmlException {
		List<FormSubmissionRecord> records = new ArrayList<FormSubmissionRecord>();
		int eventType = parser.next();
		if (!isStartTag(eventType) && !"submissions".equals(parser.getName())) {
			throw new BadXmlException("submissions");
		}
		while (!isEndTag(eventType) || !"submissions".equals(parser.getName())) {
			records.add(parseSubmission(parser));
			eventType = parser.next();
		}

		return records;
	}

	private boolean isEndTag(int eventType) {
		return eventType == XmlPullParser.END_TAG;
	}

	private FormSubmissionRecord parseSubmission(XmlPullParser parser)
			throws XmlPullParserException, IOException, BadXmlException {
		int eventType = parser.getEventType();
		if (!isStartTag(eventType)
				&& !"formSubmission".equals(parser.getName())) {
			throw new BadXmlException("formSubmission");
		}

		FormSubmissionRecord record = new FormSubmissionRecord();

		while (!isEndTag(eventType)
				|| !"formSubmission".equals(parser.getName())) {
			eventType = parser.next();
			if (isStartTag(eventType)
					&& "formType".equals(parser.getName())) {
				checkTextPresent(parser);
				record.setFormType(parser.getText());
			} else if (isStartTag(eventType)
					&& "formInstanceXml".equals(parser.getName())) {
				checkTextPresent(parser);
				record.setPartialForm(parser.getText());
			} else if (isStartTag(eventType)
					&& "formErrors".equals(parser.getName())) {
				parseFormErrors(parser, record);
			} else if (isStartTag(eventType) && "formId".equals(parser.getName())) {
				checkTextPresent(parser);
				record.setFormId(parser.getText());
			} else if (isStartTag(eventType) && "id".equals(parser.getName())) {
				checkTextPresent(parser);
				try {
					record.setRemoteId(Integer.parseInt(parser.getText()));
				} catch(NumberFormatException e) { }
			}
		}

		return record;
	}

	private void checkTextPresent(XmlPullParser parser)
			throws XmlPullParserException, BadXmlException, IOException {
		int eventType = parser.next();
		if (eventType != XmlPullParser.TEXT) {
			throw new BadXmlException("formOwnerId");
		}

	}

	private void parseFormErrors(XmlPullParser parser,
			FormSubmissionRecord record) throws XmlPullParserException,
			IOException, BadXmlException {
		int eventType = parser.next();

		if (isEndTag(eventType)) {
			return; // no form errors
		}

		while (!isEndTag(eventType) || !"formErrors".equals(parser.getName())) {
			if (!isStartTag(eventType) && !"formError".equals(parser.getName())) {
				throw new BadXmlException("formErrors");
			}

			eventType = parser.next();
			if (!"error".equals(parser.getName())) {
				throw new BadXmlException("error");
			}

			eventType = parser.next();
			if (!isTextEvent(eventType)) {
				throw new BadXmlException("No error text");
			}

			record.addErrorMessage(parser.getText());

			parser.next(); // error tag
			parser.next(); // formError tag

			eventType = parser.next();
		}
	}

	private boolean isTextEvent(int eventType) {
		return eventType == XmlPullParser.TEXT;
	}

	private void saveRecords(List<FormSubmissionRecord> records) {
		for (FormSubmissionRecord record : records) {
			storage.saveFormSubmission(record);
		}
	}
}
