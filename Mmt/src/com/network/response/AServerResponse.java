package com.network.response;

/**
 * For a request for which server will just return a "success" or "error", use this class when sending request.
 *
 */
public class AServerResponse {
    protected boolean success;

    public boolean isSuccess() {
        return success;
    }
}
