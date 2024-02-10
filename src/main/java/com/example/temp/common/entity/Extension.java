package com.example.temp.common.entity;

import static com.example.temp.common.entity.Extension.Type.IMAGE;

import java.util.Objects;
import lombok.Getter;

@Getter
public enum Extension {
    JPEG(IMAGE),
    JPG(IMAGE),
    PNG(IMAGE);

    private final Type type;

    Extension(Type type) {
        this.type = type;
    }

    public boolean isImageType() {
        return Objects.equals(this.type, IMAGE);
    }

    enum Type {
        IMAGE;
    }
}
