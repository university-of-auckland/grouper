/**
 * @author nick
 *
 */
package edu.internet2.middleware.grouper.grouperws.controller;

import javax.annotation.PostConstruct;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 */
@Controller
public class GrouperErrorController implements ErrorController {

  @PostConstruct
  public void postConstruct() {
    System.out.println("hello from " + this.getClass().getSimpleName());
  }

  @RequestMapping("/error")
  public String handleError() {
    //do something like logging
    return "error";
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }
}