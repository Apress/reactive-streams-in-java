package com.humblecode.humblecode.rest;

import com.humblecode.humblecode.data.CourseRepository;
import com.humblecode.humblecode.model.Course;
import com.humblecode.humblecode.model.Segment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
public class CourseControl {

    final CourseRepository courseRepository;

    public CourseControl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping("/api/courses")
    public Flux<Course> getCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/api/course/{id}")
    public Mono<Course> getCourse(@PathVariable("id") String id) {
        return courseRepository.findById(UUID.fromString(id));
    }

    @PostMapping(value = "/api/course", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Course> saveCourse(@RequestBody Map body) {
        Course course = new Course((String) body.get("name"));

        course.price = Long.parseLong(body.get("price").toString());

        return courseRepository.insert(course);
    }

    @PutMapping(value = "/api/course/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Course> updateCourse(@PathVariable("id") String id, @RequestBody Map<String,Object> body) {

        Mono<Course> courseMono = courseRepository.findById(UUID.fromString(id));

        return courseMono.flatMap(course -> {
            if (body.containsKey("price")) course.price = Long.parseLong(body.get("price").toString());
            if (body.containsKey("name")) course.name = (String) body.get("name");
            return courseRepository.save(course);
        });
    }

    @PostMapping(value = "/api/course/{id}/segment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono saveSegment(@PathVariable("id") String id, Segment segment) {
        Mono<Course> courseMono = courseRepository.findById(UUID.fromString(id));

        return courseMono.flatMap(course -> {
            int index = course.segments.indexOf(segment);

            course.segments.remove(segment); // remove it if already there
            if (index >= 0) {
                course.segments.add(index, segment);
            } else {
                course.segments.add(segment);
            }
            return courseRepository.save(course);
        });
    }


}
