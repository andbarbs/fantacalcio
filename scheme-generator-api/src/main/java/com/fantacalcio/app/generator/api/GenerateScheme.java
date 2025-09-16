package com.fantacalcio.app.generator.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(GenerateSchemes.class)
public @interface GenerateScheme {
    int defenders();
    int midfielders();
    int forwards();
}