package com.maintainproduct.entity;

import java.util.List;

/** 办理过程返回的结果数据模型 */
public class CaseProcedureResult {

	public int ResultCode;
	public String ResultMessage;

	public List<List<CaseProcedure>> DataList;

	public int CurrentPage;
	public int Total;
}
