package io.pivotal.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Cf {
    @Value("${cf.api}")
    private String api;

    @Value("${cf.username}")
    private String username;

    @Value("${cf.password}")
    private String password;

    @Value("${cf.org}")
    private String org;

    @Value("${cf.space}")
    private String space;

    public Cf() {
    }

    public String getApi() {
        return api;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getOrg() {
        return org;
    }

    public String getSpace() {
        return space;
    }
}
