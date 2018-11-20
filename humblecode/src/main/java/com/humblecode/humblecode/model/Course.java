package com.humblecode.humblecode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Document
public class Course {

    @Id UUID id = UUID.randomUUID();

    public String name;
    public long price = 2000; // $20.00 is default price

    public final List<Segment> segments = new ArrayList<>();

    public Course(String name) {
        this.name = name;
    }

    public void setSegments(List<Segment> segments) {
        this.segments.clear();
        this.segments.addAll(segments);
    }

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
