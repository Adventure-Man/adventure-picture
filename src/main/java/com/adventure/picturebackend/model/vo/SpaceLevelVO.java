package com.adventure.picturebackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpaceLevelVO {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
