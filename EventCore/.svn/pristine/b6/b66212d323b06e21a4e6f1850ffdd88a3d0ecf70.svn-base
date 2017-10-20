package com.ecity.android.eventcore;

public class CommonEvent {
    private int id;
    private Object data;
    private int status;
    private String message;
    private Class<?> targetClass;

    public CommonEvent(int id) {
        this(id, null, 0, "", null);
    }

    public CommonEvent(int id, Object data) {
        this(id, data, 0, "", null);
    }

    public CommonEvent(int id, Object data, int status) {
        this(id, data, status, "", null);
    }

    public CommonEvent(int id, Object data, int status, String message) {
        this(id, data, status, message, null);
    }

    public CommonEvent(int id, Object data, int status, String message,
            Class<?> targetClass) {
        this.id = id;
        this.data = data;
        this.status = status;
        this.message = message;
        this.targetClass = targetClass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
}
