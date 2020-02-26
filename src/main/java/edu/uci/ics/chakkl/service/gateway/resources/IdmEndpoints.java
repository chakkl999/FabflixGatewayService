package edu.uci.ics.chakkl.service.gateway.resources;

import edu.uci.ics.chakkl.service.gateway.GatewayService;
import edu.uci.ics.chakkl.service.gateway.configs.IdmConfigs;
import edu.uci.ics.chakkl.service.gateway.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.gateway.threadpool.ClientRequest;
import edu.uci.ics.chakkl.service.gateway.threadpool.HTTPMethod;
import edu.uci.ics.chakkl.service.gateway.transaction.TransactionGenerator;
import edu.uci.ics.chakkl.service.gateway.util.Header;
import edu.uci.ics.chakkl.service.gateway.util.Util;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Path("idm")
public class IdmEndpoints {
    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Context HttpHeaders headers, byte[] jsonBytes)
    {
        ServiceLogger.LOGGER.info("Requesting register.");
//        Client client = ClientBuilder.newClient();
//        client.register(JacksonFeature.class);
//        IdmConfigs temp = GatewayService.getIdmConfigs();
//        WebTarget webTarget = client.target(temp.getScheme()+temp.getHostName()+":"+temp.getPort()+temp.getPath()).path(temp.getRegisterPath());
//        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
//        Response response = invocationBuilder.post(Entity.entity(jsonBytes, MediaType.APPLICATION_JSON));
        IdmConfigs config = GatewayService.getIdmConfigs();
        return Util.buildPost(headers, jsonBytes, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getRegisterPath());
    }

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context HttpHeaders headers, byte[] jsonBytes)
    {
        ServiceLogger.LOGGER.info("Requesting login.");
//        Client client = ClientBuilder.newClient();
//        client.register(JacksonFeature.class);
//        IdmConfigs temp = GatewayService.getIdmConfigs();
//        WebTarget webTarget = client.target(temp.getScheme()+temp.getHostName()+":"+temp.getPort()+temp.getPath()).path(temp.getLoginPath());
//        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
//        Response response = invocationBuilder.post(Entity.entity(jsonBytes, MediaType.APPLICATION_JSON));
//        ServiceLogger.LOGGER.info("Login complete.");
//        ServiceLogger.LOGGER.info(String.valueOf(response.getStatus()));
//        return Response.status(response.getStatus()).entity(response.readEntity(String.class).getBytes()).build();
        IdmConfigs config = GatewayService.getIdmConfigs();
        return Util.buildPost(headers, jsonBytes, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getLoginPath());
    }

    @Path("session")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response session(@Context HttpHeaders headers, byte[] jsonBytes)
    {
        ServiceLogger.LOGGER.info("Requesting session.");
        IdmConfigs config = GatewayService.getIdmConfigs();
        return Util.buildPost(headers, jsonBytes, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getSessionPath());
    }

    @Path("privilege")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response privilege(@Context HttpHeaders headers, byte[] jsonBytes)
    {
        ServiceLogger.LOGGER.info("Requesting privilege.");
        IdmConfigs config = GatewayService.getIdmConfigs();
        return Util.buildPost(headers, jsonBytes, config.getScheme()+config.getHostName()+":"+config.getPort()+config.getPath(), config.getPrivilegePath());
    }
}
