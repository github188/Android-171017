package com.ecity.android.eventcore;

public class ResponseEvent implements IEvent{
    public static final int STATUS_OK = 1;
    public static final int STATUS_ERROR = 0;

    private int id;
    private int status;
    private Object data;
    private String message;
    private Class<?> targetClass;

    public ResponseEvent(int id) {
        this.id = id;
    }

    public ResponseEvent(int id, int status) {
        this(id, status, null, "");
    }  
    
    public ResponseEvent(int id, int status, String message) {
        this(id, status, null, message);
    }
    
    public ResponseEvent(int id, int status, Object data) {
        this(id, status, data, "");
    }

    public ResponseEvent(int id, int status, Object data, String message) {
        this.id = id;
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public boolean isForTarget(Object target) {
        if (target == null) {
            return false;
        }

        return target.getClass().getSimpleName().equals(targetClass.getSimpleName());
    }

    public boolean isOK() {
        return this.status == STATUS_OK;
    }
}
