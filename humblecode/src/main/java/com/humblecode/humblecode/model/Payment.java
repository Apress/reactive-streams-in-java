package com.humblecode.humblecode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document
@Data
@AllArgsConstructor
public class Payment {

    Date date;
    UUID userId;
    int amount;

}
