package com.github.adamldavis.akkhttp;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.github.adamldavis.akkahttp.WebApp;
import org.junit.*;
import static org.assertj.core.api.Assertions.assertThat;

public class WebAppTest {

    WebApp webApp;
    ActorSystem actorSystem;

    @Before
    public void setup() {
        actorSystem = ActorSystem.create("test-system");
        webApp = new WebApp(actorSystem);
    }
    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
    }

    @Test
    public void createWebsocketRoute_should_exist(){
        assertThat(webApp.createWebsocketRoute()).isNotNull();
    }

}
