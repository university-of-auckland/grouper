package edu.internet2.middleware.grouper.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
// @ApplicationPath("/*")
@Configuration
@SpringBootApplication
@ComponentScan(basePackages = {
    "edu.internet2" } /*, excludeFilters = @Filter({
                      RestController.class, Configuration.class })*/
)
public class GrouperWSApplication { // extends SpringBootServletInitializer { // extends ResourceConfig {

  public GrouperWSApplication() {
    //public MyApplication(@Context ServletConfig servletConfig) {
    super();
  }

  //  @Override
  //  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
  //    return application.sources(GrouperWSApplication.class);
  //  }

  public static void main(String[] args) {
    SpringApplication.run(GrouperWSApplication.class, args);
  }
}
