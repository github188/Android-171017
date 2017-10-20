/**
 * Exception style class encapsulating Business errors
 */
package com.android.business.exception;


@SuppressWarnings("serial")
public class BusinessException extends Exception{
	/**
	 * 执行错误码
	 */
	public int errorCode = BusinessErrorCode.BEC_COMMON_UNKNOWN; 
	/**
	 * 扩展错误码
	 */
	public int expandCode = BusinessErrorCode.BEC_COMMON_UNKNOWN;
	
	/**
	 * 错误描述
	 */
	public String errorDescription = null;
	
	public BusinessException(){
		
	}
	
	public BusinessException(String e) {
		super(e);
	}
	
	public BusinessException(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
    }
	
	public BusinessException(Throwable cause) {
        super(cause);
    }
	
	public BusinessException(int errorCode,Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }
	
	public BusinessException(int errorCode,int expandCode) {
        this.errorCode = errorCode;
        this.expandCode =expandCode;
    }
	public BusinessException(int errorCode) {
        this.errorCode = errorCode;
    }
	
	public BusinessException(int errorCode,String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription =errorDescription;
    }
}
