package com.spring_cloud.eureka.client.auth.entity;

public enum UserRoleEnum {

    MANAGER(Authority.MANAGER),
    MEMBER(Authority.MEMBER);

    private final String authority;

    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return this.authority;
    }

    public static class Authority {
        public static final String MANAGER = "MANAGER";
        public static final String MEMBER = "MEMBER";
    }
}
