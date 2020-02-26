package edu.uci.ics.chakkl.service.gateway.resources;

import edu.uci.ics.chakkl.service.gateway.GatewayService;
import edu.uci.ics.chakkl.service.gateway.configs.MoviesConfigs;
import edu.uci.ics.chakkl.service.gateway.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.gateway.util.Util;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Path("movies")
public class MoviesEndpoints {
    @Path("search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@Context HttpHeaders headers, @QueryParam("title") String title, @QueryParam("year") Integer year,
                           @QueryParam("director") String director, @QueryParam("genre") String genre, @QueryParam("hidden") Boolean hidden,
                           @QueryParam("limit") Integer limit, @QueryParam("orderby") String orderby, @QueryParam("direction") String direction)
    {
        ServiceLogger.LOGGER.info("Requesting search.");
        HashMap<String,String> query = new HashMap<>();
        if(title != null)
            query.put("title", title);
        if(year != null)
            query.put("year", year.toString());
        if(director != null)
            query.put("director", director);
        if(genre != null)
            query.put("genre", genre);
        if(hidden != null)
            query.put("hidden", hidden.toString());
        if(limit != null)
            query.put("limit", limit.toString());
        if(orderby != null)
            query.put("orderby", orderby);
        if(direction != null)
            query.put("direction", direction);
        MoviesConfigs config = GatewayService.getMoviesConfigs();
        return Util.buildGet(headers, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getSearchPath(), query);
    }

    @Path("browse/{phrase}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response browse(@Context HttpHeaders headers, @PathParam("phrase") String phrase, @QueryParam("limit") Integer limit,
                           @QueryParam("offset") Integer offset, @QueryParam("orderby") String orderby,
                           @QueryParam("direction") String direction)
    {
        ServiceLogger.LOGGER.info("Requesting browse.");
        HashMap<String,String> query = new HashMap<>();
        if(limit != null)
            query.put("limit", limit.toString());
        if(offset != null)
            query.put("offset", offset.toString());
        if(orderby != null)
            query.put("orderby", orderby);
        if(direction != null)
            query.put("direction", direction);
        MoviesConfigs config = GatewayService.getMoviesConfigs();
        return Util.buildGet(headers, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getBrowsePath()+"/"+phrase, query);
    }

    @Path("get/{movie_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpHeaders headers, @PathParam("movie_id") String movie_id)
    {
        ServiceLogger.LOGGER.info("Requesting get.");
        MoviesConfigs config = GatewayService.getMoviesConfigs();
        return Util.buildGet(headers, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getGetPath()+"/"+movie_id, null);
    }

    @Path("thumbnail")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response thumbnail(@Context HttpHeaders headers, byte[] jsonBytes)
    {
        ServiceLogger.LOGGER.info("Requesting thumbnail");
        MoviesConfigs config = GatewayService.getMoviesConfigs();
        return Util.buildPost(headers, jsonBytes, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getThumbnailPath());
    }

    @Path("people")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response people(@Context HttpHeaders headers, @QueryParam("name") String name, @QueryParam("limit") Integer limit,
                           @QueryParam("offset") Integer offset, @QueryParam("orderby") String orderby,
                           @QueryParam("direction") String direction)
    {
        ServiceLogger.LOGGER.info("Requesting people");
        HashMap<String,String> query = new HashMap<>();
        if(name != null)
            query.put("name", name);
        if(limit != null)
            query.put("limit", limit.toString());
        if(offset != null)
            query.put("offset", offset.toString());
        if(orderby != null)
            query.put("orderby", orderby);
        if(direction != null)
            query.put("direction", direction);
        MoviesConfigs config = GatewayService.getMoviesConfigs();
        return Util.buildGet(headers, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getPeoplePath(), query);
    }

    @Path("people/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response peopleSearch(@Context HttpHeaders headers, @QueryParam("name") String name,
                                 @QueryParam("birthday")String birthday, @QueryParam("movie_title") String title,
                                 @QueryParam("limit") Integer limit, @QueryParam("offsets") Integer offset,
                                 @QueryParam("orderby") String orderby, @QueryParam("direction") String direction)
    {
        ServiceLogger.LOGGER.info("Requesting people search");
        HashMap<String,String> query = new HashMap<>();
        if(name != null)
            query.put("name", name);
        if(birthday != null)
            query.put("birthday", birthday);
        if(title != null)
            query.put("movie_title", title);
        if(limit != null)
            query.put("limit", limit.toString());
        if(offset != null)
            query.put("offset", offset.toString());
        if(orderby != null)
            query.put("orderby", orderby);
        if(direction != null)
            query.put("direction", direction);
        MoviesConfigs config = GatewayService.getMoviesConfigs();
        return Util.buildGet(headers, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getPeopleSearchPath(), query);
    }

    @Path("people/get/{person_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response peopleGet(@Context HttpHeaders headers, @PathParam("person_id") String person_id)
    {
        ServiceLogger.LOGGER.info("Requesting people get.");
        MoviesConfigs config = GatewayService.getMoviesConfigs();
        return Util.buildGet(headers, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getPeopleGetPath()+"/"+person_id, null);
    }
}
