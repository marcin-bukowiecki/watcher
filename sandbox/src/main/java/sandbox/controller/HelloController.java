package sandbox.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * @author Marcin Bukowiecki
 */
@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @GetMapping
    public String hello() {
        final int randomInt = getRandomInt();
        final String message = "Hello World. Random int: " + randomInt;
        log.info("Got message to return: " + message);
        return message;
    }

    private int getRandomInt() {
        return new Random().nextInt() * 1000;
    }
}
