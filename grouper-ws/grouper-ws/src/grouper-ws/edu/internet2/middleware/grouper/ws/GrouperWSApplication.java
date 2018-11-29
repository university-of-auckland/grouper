package edu.internet2.middleware.grouper.ws;

import javax.ws.rs.ApplicationPath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 *
 */
@ApplicationPath("/*")
@SpringBootApplication
public class GrouperWSApplication extends SpringBootServletInitializer { // extends ResourceConfig {

  public GrouperWSApplication() {
    //public MyApplication(@Context ServletConfig servletConfig) {
    super();
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(GrouperWSApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(GrouperWSApplication.class, args);
  }
}
