package com.ferragem.avila.pdv.model.enums;

public enum RoleValue {
    ADMIN(1L),
    BASIC(2L);

    long roleId;

    RoleValue(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }
}