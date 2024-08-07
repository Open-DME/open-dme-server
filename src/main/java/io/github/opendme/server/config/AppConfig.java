package io.github.opendme.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private User instanceOwner;

    public User getInstanceOwner() {
        return instanceOwner;
    }

    public void setInstanceOwner(User instanceOwner) {
        this.instanceOwner = instanceOwner;
    }

    @Component
    public static class User {
        private String name;
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
