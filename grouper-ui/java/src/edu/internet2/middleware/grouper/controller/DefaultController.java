package edu.internet2.middleware.grouper.controller;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// @RefreshScope
@RequestMapping("/")
public class DefaultController {

  //@Value("${encrypted.property}")
  private String testProperty;

  //@Value("${test.local.property}")
  private String localTestProperty;

  @PostConstruct
  public void postConstruct() {
    System.out.println("hello");
  }

  @RequestMapping("/test")
  public String test() {
    StringBuilder builder = new StringBuilder();
    builder.append("global property - ").append(testProperty).append(" || ")
        .append("local property - ").append(localTestProperty).append(" || ");
    return builder.toString();
  }
}
