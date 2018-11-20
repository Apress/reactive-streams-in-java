package com.humblecode.humblecode.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TestResult {

    UUID testId;
    Date date;
    boolean passed;
    float percent;

}
