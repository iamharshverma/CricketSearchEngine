package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class GreetingController {


    private final AtomicLong counter = new AtomicLong();
    private String expanded_query="";

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="query", defaultValue="cricket") String query,@RequestParam(value = "qe",defaultValue = "association") String qe) {
        return new Greeting(counter.incrementAndGet(), query,expanded_query,qe);
    }

}
