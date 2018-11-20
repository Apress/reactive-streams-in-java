package com.humblecode.humblecode.web;

import com.humblecode.humblecode.data.CourseRepository;
import com.humblecode.humblecode.data.UserRepository;
import com.humblecode.humblecode.model.Course;
import com.humblecode.humblecode.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.security.Principal;

@Controller
public class WebControl {

    @Value("${app.name}")
    String appName;

    @Autowired
    CourseRepository courseRepository;
    @Autowired
    UserRepository userRepository;

    @PostConstruct
    public void setup() {
        courseRepository.count().blockOptional().filter(count -> count == 0).ifPresent(it ->
            Flux.just(
                    new Course("Beginning Java"),
                    new Course("Advanced Java"),
                    new Course("Reactive Streams in Java"))
                .doOnNext(c -> System.out.println(c.toString()))
                .flatMap(courseRepository::save).subscribeOn(Schedulers.single())
                .subscribe() // need to actually execute save*/
        );
        // just adding dummy user for demo purposes:
        userRepository.count().blockOptional().filter(count -> count == 0).ifPresent(it ->
                Flux.just(new User("user", "password"))
                    .flatMap(userRepository::save).subscribeOn(Schedulers.single())
                    .subscribe()
        );
    }

    @GetMapping("/")
    public Mono<String> home(Model model, Principal principal) {
        model.addAttribute("name", principal == null ? "" : principal.getName());
        model.addAttribute("applicationName", appName);
        return Mono.just("home");
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("applicationName", appName);
        model.addAttribute("error", "Login failed.");
        return "login";
    }

    @GetMapping("/user/account")
    public String userAccount(Model model, Principal principal) {
        model.addAttribute("applicationName", appName);
        model.addAttribute("username", principal.getName());
        return "account";
    }
}
