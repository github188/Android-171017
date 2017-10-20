package com.ecity.android.eventcore;

import com.ecity.android.eventcore.IEvent;
import com.ecity.android.eventcore.ReservedEvent;


public class UIEvent implements IEvent{
    private int id;
    private int status;
    private Object data;
    private String message;
    private Object target;
    private Class<?> targetClass;

    public UIEvent(int id) {
        this(id, 0, null, "", null);
    }
    
   
    public UIEvent(int id, Object data) {
        this(id, 0, data, "", null);
    }

    public UIEvent(int id, String msg, Object target) {
        this(id, 0, null, msg, target);
    }

    public UIEvent(int id, int status, String msg, Object target) {
        this(id, status, null, msg, target);
    }

    public UIEvent(int id, int status, Object data, String message, Object target) {
        this.id = id;
        this.status = status;
        this.data = data;
        this.message = message;
        this.target = target;

        if (target != null) {
            this.targetClass = target.getClass();
        }
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

    /**
     * Check if this is a toast UIEvent to display in target.
     * @param target
     * @return
     */
    public boolean isToastForTarget(Object target) {
        if (target == null) {
            return false;
        }

        if ((id == ReservedEvent.UI.TOAST) && (target.equals(this.target))) {
            return true;
        }

        return false;
    }

    public boolean isForTarget(Object target) {
        if (target == null) {
            return false;
        }

        return target.getClass().getSimpleName().equals(targetClass.getSimpleName());
    }
}
