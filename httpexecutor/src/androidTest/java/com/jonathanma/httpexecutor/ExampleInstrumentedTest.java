package com.jonathanma.httpexecutor;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ecity.android.httpexecutor.ARequestCallback;
import com.ecity.android.httpexecutor.RequestExecutor;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.jonathanma.httpexecutor.test", appContext.getPackageName());
    }

    @Test
    public void testExecutor() throws Exception {
        RequestExecutor.execute(new ARequestCallback() {
            @Override
            public int getEventId() {
                return 0;
            }

            @Override
            public boolean isPost() {
                return false;
            }

            @Override
            public String getUrl() {
                return "http://192.168.8.135:9999/ServiceEngine/rest/services/BpmServer/workflow/form/formdata";
            }

            @Override
            public Map<String, String> getParameter() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("f", "json");
                map.put("processinstanceid", "476595");
                return new HashMap<String, String>();
            }
        });
    }
}
