package com.adventure.picturebackend.model.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Adventure
 * @date 2025/7/25
 * @description 空间等级枚举
 */
@Getter
@AllArgsConstructor
public enum SpaceLevelEnum {
    ORDINARY(0, "普通版"),
    PROFESSIONAL(1, "专业版"),
    FLAGSHIP(2, "旗舰版")
    ;
    // 普通版
    @EnumValue
    private final Integer value;
    private final String text;

    public static SpaceLevelEnum getEnumByValue(Integer value) {
        for (SpaceLevelEnum valueEnum : values()) {
            if (valueEnum.value.equals(value)) {
                return valueEnum;
            }
        }
        return null;
    }
}
