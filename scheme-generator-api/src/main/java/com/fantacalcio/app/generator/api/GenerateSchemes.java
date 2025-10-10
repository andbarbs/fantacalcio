package com.fantacalcio.app.generator.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateSchemes {
    GenerateScheme[] value();
}