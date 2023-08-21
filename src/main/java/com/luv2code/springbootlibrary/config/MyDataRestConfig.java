package com.luv2code.springbootlibrary.config;

import com.luv2code.springbootlibrary.entity.Book;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.config.RepositoryConfiguration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class MyDataRestConfig implements RepositoryRestConfigurer {
    private String theAllowedOrigins = "http://localhost:4000";

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors){
        HttpMethod[] theUnsopportedActions = {
                HttpMethod.POST,
                HttpMethod.PATCH,
                HttpMethod.PUT,
                HttpMethod.DELETE
        };
        config.exposeIdsFor(Book.class);
        disableHttpMethods(Book.class,config, theUnsopportedActions);

        /* Configure CORS Mapping */
        cors.addMapping(config.getBasePath() + "/**")
                .allowedOrigins(theAllowedOrigins);
    }

    private void disableHttpMethods(Class theClass, RepositoryRestConfiguration config, HttpMethod[] theUnsopportedActions) {
        config.getExposureConfiguration()
                .forDomainType(theClass)
                .withItemExposure((metdata, httpMethods) -> httpMethods.disable(theUnsopportedActions))
                .withCollectionExposure((metdata, httpMethods) -> httpMethods.disable(theUnsopportedActions));
    }
}
