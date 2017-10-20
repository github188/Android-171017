package com.ecity.android.eventcore;

import de.greenrobot.event.EventBus;

/**
 * A helper class of {@link EventBus}.
 *
 */
public class EventBusUtil {
    public static void register(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
        EventBus.getDefault().register(subscriber);
    }

    public static void register(Object subscriber, String methodName) {
        EventBus.getDefault().register(subscriber, methodName);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void post(Object event) {
        EventBus.getDefault().post(event);
    }
}
