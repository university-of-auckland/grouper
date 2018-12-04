package edu.internet2.middleware.grouper.grouperws.controller;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// import edu.internet2.middleware.grouper.grouperws.configuration.PropertyConfiguration;

@RestController
// @RefreshScope
public class DefaultController {

  // @Value("${encrypted.property}")
  private String testProperty;

  //@Value("${test.local.property}")
  private String localTestProperty;

  @Autowired
  //  private PropertyConfiguration propertyConfiguration;

  @PostConstruct
  public void postConstruct() {
    System.out.println("hello from " + this.getClass().getSimpleName());
  }

  @RequestMapping("/home")
  public String home() {
    StringBuilder builder = new StringBuilder();
    builder.append("global property - ").append(testProperty).append(" <br/>|| ")
        .append("local property - ").append(localTestProperty).append(" || ");
    return builder.toString();
  }

  // @RequestMapping("/error")
  public String error() {
    Set keys = new HashSet();
    if (keys.contains("")) {

    }
    StringBuilder builder = new StringBuilder();
    builder.append("global property - ").append(testProperty).append(" || ")
        .append("local property - ").append(localTestProperty).append(" || ")
        .append("property configuration value - ")
        .append("propertyConfiguration.getProperty()");
    return builder.toString();
  }
}
