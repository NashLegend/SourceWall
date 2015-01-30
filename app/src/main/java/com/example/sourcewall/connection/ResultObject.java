package com.example.sourcewall.connection;

/**
 * Created by NashLegend on 2014/9/23 0023
 */
public class ResultObject {
    public ResultCode code = ResultCode.CODE_OK;
    public boolean ok = false;
    public int statusCode = 200;
    public String message = "";
    public Object result;

    @Override
    public String toString() {
        return result == null ? "" : result.toString();
    }

    public static enum ResultCode {
        CODE_OK,
        CODE_LOGIN_FAILED,
        CODE_TOKEN_INVALID,
        CODE_NETWORK_ERROR,
        CODE_JSON_ERROR,
        CODE_ALREADY_LIKED,
        CODE_ALREADY_THANKED,
        CODE_ALREADY_BURIED,
        CODE_UNKNOWN,
        CODE_NO_TOKEN,
        CODE_FORBIDDEN,
    }
}
