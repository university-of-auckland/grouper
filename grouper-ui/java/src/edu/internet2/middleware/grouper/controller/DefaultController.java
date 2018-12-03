package edu.internet2.grouper.controller;
  
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class DefaultController {

  //@Value("${encrypted.property}")
  private String testProperty;

  //@Value("${test.local.property}")
  private String localTestProperty;

  @PostConstruct
  public void postConstruct() {
    System.out.println("hello");
  }

  @RequestMapping("/")
  public String test() {
    StringBuilder builder = new StringBuilder();
    builder.append("global property - ").append(testProperty).append(" || ")
        .append("local property - ").append(localTestProperty).append(" || ");
    return builder.toString();
  }
}

