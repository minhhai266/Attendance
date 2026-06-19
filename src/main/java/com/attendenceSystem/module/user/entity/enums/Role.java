package com.attendenceSystem.module.user.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN(1), MANAGER(2), STAFF(3), ;

    public final int value;
    public static Role fromValue(int value){
        for(Role role : Role.values()){
            if(role.value == value){
                return role;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy vai trò: "+ value);
    }
}
