package com.mapgis.mmt.module.flowreport;

import java.util.ArrayList;

public class FlowTypeModel {
	public String FlowID;
	public String FlowName;

	public ArrayList<String> Descriptions = new ArrayList<String>();

	public FlowTypeModel Clone() {
		FlowTypeModel model = new FlowTypeModel();

		model.FlowID = this.FlowID;
		model.FlowName = this.FlowName;

		for (String s : this.Descriptions) {
			model.Descriptions.add(s);
		}

		return model;
	}

	@Override
	public boolean equals(Object o) {

        return ((FlowTypeModel) o).FlowID.equals(this.FlowID) && ((FlowTypeModel) o).FlowName.equals(this.FlowName);
	}

	@Override
	public String toString() {
		return FlowName;
	}
}
