package com.humblecode.humblecode.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Question {

    String text = "";

    String tip = "";

    String answer = "";

    List<String> answers = new ArrayList<>();

    List<Integer> correctAnswers = new ArrayList<>();

}
