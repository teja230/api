package com.enterprise.agents.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "external")
public class ExternalServicesConfig {
    private Slack slack = new Slack();
    private OpenAI openai = new OpenAI();
    private Redis redis = new Redis();

    @Data
    public static class Slack {
        private Webhook webhook = new Webhook();
        private OAuth oauth = new OAuth();
        private boolean enabled = false;

        @Data
        public static class Webhook {
            private String url;
        }

        @Data
        public static class OAuth {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
            private String scopes;
            private int tokenExpirationSeconds = 3600;
        }
    }

    @Data
    public static class OpenAI {
        private Api api = new Api();
        private boolean enabled = false;

        @Data
        public static class Api {
            private String key;
            private String url;
        }
    }

    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private boolean enabled = false;
    }
} 