package com.mapgis.mmt.net;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.util.ArrayList;
import java.util.Hashtable;

public class BaseTableTask extends BaseTask<ArrayList<Hashtable<String, String>>> {

	ArrayList<Hashtable<String, String>> plans;

	public BaseTableTask(BaseTaskParameters paramTaskParameters) {
		super(paramTaskParameters);
	}

	public BaseTableTask(BaseTaskParameters paramTaskParameters,
			BaseTaskListener<ArrayList<Hashtable<String, String>>> paramTaskListener) {
		super(paramTaskParameters, paramTaskListener);
	}

	@Override
	public ArrayList<Hashtable<String, String>> execute() {
		try {

			JsonParser jsonParser = HttpRequestJsonParse
					.executeFromMap(this.actionInput.getUrl(), this.actionInput.generateRequestParams());

			plans = new ArrayList<Hashtable<String, String>>();

			if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
				return plans;
			}

			String name = "", text = "";
			JsonToken token;

			Hashtable<String, String> current = null;

			while (jsonParser.nextToken() != null) {

				token = jsonParser.getCurrentToken();
				name = jsonParser.getCurrentName();
				text = jsonParser.getText();

				switch (token) {
				case START_OBJECT:
					current = new Hashtable<String, String>();

					break;
				case VALUE_NUMBER_INT:
				case VALUE_STRING:
					current.put(name, text);

					break;
				case END_OBJECT:
					plans.add(current);
					current = null;

					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return plans;
	}
}
