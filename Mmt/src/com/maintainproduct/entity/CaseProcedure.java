package com.maintainproduct.entity;

import com.google.gson.annotations.SerializedName;

/** 办理过程数据模型 */
public class CaseProcedure {

	@SerializedName("步骤名称")
	public String StepName;

	@SerializedName("承办人")
	public String UndertakeMan;

	@SerializedName("承办部门")
	public String UndertakeDept;

	@SerializedName("承办时间")
	public String UndertakeTime;

	@SerializedName("办完时间")
	public String FinishTime;

	@SerializedName("承办意见")
	public String UndertakeOpinion;

    @SerializedName("移交方向")
    public int HandOverDirection;



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("步骤名称").append("：").append(StepName == null ? "" : StepName).append("\n");
		builder.append("承办人    ").append("：").append(UndertakeMan == null ? "" : UndertakeMan).append("\n");
		builder.append("承办时间").append("：").append(UndertakeTime == null ? "" : UndertakeTime.replace("T", " ")).append("\n");
		builder.append("办完时间").append("：").append(FinishTime == null ? "" : FinishTime.replace("T", " ")).append("\n");
		builder.append("{承办}意见").append("：").append(UndertakeOpinion == null ? "" : UndertakeOpinion);
		return builder.toString();
	}
}