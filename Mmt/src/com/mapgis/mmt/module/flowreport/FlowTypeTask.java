package com.mapgis.mmt.module.flowreport;

import com.mapgis.mmt.net.BaseTask;
import com.mapgis.mmt.net.BaseTaskListener;
import com.mapgis.mmt.net.BaseTaskParameters;
import com.mapgis.mmt.net.HttpRequestJsonParse;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.util.ArrayList;

public class FlowTypeTask extends BaseTask<ArrayList<FlowTypeModel>> {

	ArrayList<FlowTypeModel> modes = new ArrayList<FlowTypeModel>();
	FlowTypeModel currenTypeModel = null;
	boolean isDescription = false;

	public FlowTypeTask(BaseTaskParameters Parameters, BaseTaskListener<ArrayList<FlowTypeModel>> taskListener) {
		super(Parameters, taskListener);
	}

	public FlowTypeTask(BaseTaskParameters Parameters) {
		super(Parameters);
	}

	@Override
	public ArrayList<FlowTypeModel> execute() {
		try {
			JsonParser jsonParser = HttpRequestJsonParse
					.executeFromMap(this.actionInput.getUrl(), this.actionInput.generateRequestParams());

			if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
				return modes;
			}

			String name = "", text = "";
			JsonToken token;

			while (jsonParser.nextToken() != null) {

				token = jsonParser.getCurrentToken();
				name = jsonParser.getCurrentName();
				text = jsonParser.getText();

				if (token == JsonToken.START_OBJECT) {
					currenTypeModel = new FlowTypeModel();
				} else if (token == JsonToken.START_ARRAY && name == "Descriptions") {
					isDescription = true;
				} else if (isDescription && token == JsonToken.VALUE_STRING) {
					currenTypeModel.Descriptions.add(text);
				} else if (token == JsonToken.END_ARRAY && name == "Descriptions") {
					isDescription = false;
				} else if (token == JsonToken.VALUE_STRING && name == "FlowID") {
					currenTypeModel.FlowID = text;
				} else if (token == JsonToken.VALUE_STRING && name == "FlowName") {
					currenTypeModel.FlowName = text;
				} else if (token == JsonToken.END_OBJECT) {
					modes.add(currenTypeModel.Clone());
					currenTypeModel = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return modes;
	}

}
