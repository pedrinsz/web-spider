package crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@PropertySource({ "file:config/application.properties" })
@SpringBootApplication
public class SintegraApplication {
  
  public static void main(String[] args) {
    SpringApplication.run(SintegraApplication.class, args);
  }
}
