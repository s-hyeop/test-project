package com.example.test_project.util;

import com.fasterxml.uuid.Generators;

public class UuidUtil {

    public static String generateUuidV7() {
        return Generators.timeBasedEpochRandomGenerator().generate().toString();
    }

}
