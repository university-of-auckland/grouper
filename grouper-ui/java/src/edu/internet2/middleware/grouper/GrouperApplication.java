package edu.internet2.middleware.grouper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @EnableConfigServer
//@EnableDiscoveryClient
public class GrouperApplication {

  public static void main(String[] args) {
    SpringApplication.run(GrouperApplication.class, args);
  }
}
