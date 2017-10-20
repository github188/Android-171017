package com.mapgis.mmt.net;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class BaseStringTask extends BaseTask<String> {

	public BaseStringTask(BaseTaskParameters paramTaskParameters) {
		super(paramTaskParameters);
	}

	public BaseStringTask(BaseTaskParameters paramTaskParameters, BaseTaskListener<String> paramTaskListener) {
		super(paramTaskParameters, paramTaskListener);
	}

	@Override
	public String execute() {
		try {
			JsonParser jsonParser = HttpRequestJsonParse.executeFromMap(this.actionInput.getUrl(),
					this.actionInput.generateRequestParams());

			if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
				return jsonParser.getText();
			}

			String name = "", text = "";
			JsonToken token;

			while (jsonParser.nextToken() != null) {

				token = jsonParser.getCurrentToken();
				name = jsonParser.getCurrentName();
				text = jsonParser.getText();
				
				if (name != null && (name.equals("ResultMessage")||name.equals("Msg")) && token == JsonToken.VALUE_STRING) {
					return text;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}
}
