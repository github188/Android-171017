package com.mapgis.mmt.net;

import java.util.Map;

public class BaseTaskParameters {

	protected String url;

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	protected Map<String, String> requestParams;

	public Map<String, String> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(Map<String, String> requestParams) {
		this.requestParams = requestParams;
	}

	public BaseTaskParameters() {
	}

	public BaseTaskParameters(String url) {
		this.url = url;
	}

	public BaseTaskParameters(String url, Map<String, String> paramters) {
		this.url = url;
		this.requestParams = paramters;
	}

	@Override
	public int hashCode() {
		int j = 1;
		j = 31 * j + (this.url == null ? 0 : this.url.hashCode());
		return j;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		BaseTaskParameters localTaskParameters = (BaseTaskParameters) obj;

		if (this.url == null) {
			if (localTaskParameters.url != null) {
				return false;
			}
		} else if (!this.url.equals(localTaskParameters.url)) {
			return false;
		}
		return true;
	}

	public Map<String, String> generateRequestParams() {
		return this.requestParams;
	}

	public boolean validate() {
		return true;
	}
}
