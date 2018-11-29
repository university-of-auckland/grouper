package edu.internet2.middleware.grouper.ws;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/*")
public class MyApplication extends ResourceConfig {

    public MyApplication() {
    //public MyApplication(@Context ServletConfig servletConfig) {
        super();
    }
}