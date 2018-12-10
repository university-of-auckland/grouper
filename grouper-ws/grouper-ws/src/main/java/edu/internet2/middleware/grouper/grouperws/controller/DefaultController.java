package edu.internet2.middleware.grouper.grouperws.controller;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
// import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// import edu.internet2.middleware.grouper.grouperws.configuration.PropertyConfiguration;

@OpenAPIDefinition(info = @Info(title = "Grouper ", version = "0.0.1.b1", description = "Grouper API", license = @License(name = "Apache 2.0", url = "http://www.internet2.edu"), contact = @Contact(url = "http://www.internet2.edu", name = "Nick", email = "n.ivanov@auckland.ac.nz")))

@Api(value = "rest2", tags = { "rest2" })

@RestController
// @RefreshScope
@Path("/grouper/v2")
@Tags(@Tag(name = "test", description = ""))
@Produces({ "application/json", "application/xml" })
@Configuration
@EnableSwagger2
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

  @GET
  @Path("/home}")
  @RequestMapping("/home")
  @ApiOperation(value = "Home", response = String.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Version resource found"),
      @ApiResponse(code = 404, message = "Version resource not found")
  })
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
