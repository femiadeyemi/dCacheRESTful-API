package org.dcache.restful;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import org.dcache.restful.controllers.CreateDirectory;
import org.dcache.restful.controllers.DeleteFile;
import org.dcache.restful.controllers.ListFile;
import org.dcache.restful.controllers.RenameFile;
import org.dcache.restful.filters.ResponseHeaderFilter;
import org.dcache.restful.resources.MyObjectProvider;

/**
 *
 */
public class DcacheRestApplication extends ResourceConfig
{
    public DcacheRestApplication()
    {
        packages("org.dcache.restful",
                "org.glassfish.jersey.jackson;",
                "com.fasterxml.jackson.jaxrs.json");

        //register application controller
        register(ListFile.class);
        register(RenameFile.class);
        register(CreateDirectory.class);
        register(DeleteFile.class);

        //register filters
        register(ResponseHeaderFilter.class);

        //register features/provider
        register(MyObjectProvider.class);
        register(JacksonJsonProvider.class);
        register(JacksonJaxbJsonProvider.class);
        register(EntityFilteringFeature.class);
        EncodingFilter.enableFor(this, GZipEncoder.class);

    }
}
