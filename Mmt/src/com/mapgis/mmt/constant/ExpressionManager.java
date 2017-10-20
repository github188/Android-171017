package com.mapgis.mmt.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 惯用语管理器
 */
public class ExpressionManager {
	/** 默认惯用语 */
	public static final int EXPRESSION_NORMAL = 0;
	/** 事件上报惯用语 */
	public static final int EXPRESSION_FLOWREPORTER = 1;
	/** 其他惯用语 */
	public static final int EXPRESSION_OTHER = 2;

	public List<String> getExpression(int expressionFlag) {

		List<String> expressions = null;

		switch (expressionFlag) {

		case EXPRESSION_FLOWREPORTER:
			expressions = new FlowReporterExpression().getFlowReporterExpressions();
			break;
		case EXPRESSION_OTHER:
			expressions = new OthersExpression().getOtherExpressions();
			break;
		default:
			expressions = new NormalExpression().getNormalExpressions();
			break;

		}

		return expressions;
	}

	/** 默认惯用语 */
	class NormalExpression {
		public final List<String> normalExpressions = new ArrayList<String>();

		public NormalExpression() {
			normalExpressions.add("默认惯用语1");
			normalExpressions.add("默认惯用语2");
			normalExpressions.add("默认惯用语3");
			normalExpressions.add("默认惯用语4");
		}

		public List<String> getNormalExpressions() {
			return normalExpressions;
		}

	}

	/** 事件上报惯用语 */
	class FlowReporterExpression extends NormalExpression {
		public final List<String> flowReporterExpressions = new ArrayList<String>();

		public FlowReporterExpression() {
			super();
			flowReporterExpressions.addAll(normalExpressions);
		}

		public List<String> getFlowReporterExpressions() {
			return flowReporterExpressions;
		}

	}

	/** 其他惯用语 */
	class OthersExpression extends NormalExpression {
		public final List<String> otherExpressions = new ArrayList<String>();

		public OthersExpression() {
			super();
			otherExpressions.addAll(normalExpressions);
		}

		public List<String> getOtherExpressions() {
			return otherExpressions;
		}

	}

}
