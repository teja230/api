package com.service.api;

import com.service.api.helpers.HttpHelper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;

class APIServiceTest {
    @Test
    void run_shouldCallHttpHelper() {
        try (MockedStatic<HttpHelper> mocked = Mockito.mockStatic(HttpHelper.class)) {
            APIService service = new APIService();
            service.run();
            mocked.verify(() -> HttpHelper.sendHttpRequest(Mockito.anyString(), Mockito.anyString()));
        }
    }

    @Test
    void run_shouldHandleIOException() {
        try (MockedStatic<HttpHelper> mocked = Mockito.mockStatic(HttpHelper.class)) {
            mocked.when(() -> HttpHelper.sendHttpRequest(Mockito.anyString(), Mockito.anyString())).thenThrow(new IOException("fail"));
            APIService service = new APIService();
            service.run();
            // No exception should be thrown
        }
    }
}
