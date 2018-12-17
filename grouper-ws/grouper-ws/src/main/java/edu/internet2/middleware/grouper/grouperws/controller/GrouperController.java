/**
 * @author nick
 *
 */
package edu.internet2.middleware.grouper.grouperws.controller;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
public class GrouperController {

  @PostConstruct
  public void postConstruct() {
    System.out.println("hello from " + this.getClass().getSimpleName());
  }
}
