package edu.uci.ics.chakkl.service.gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.chakkl.service.gateway.GatewayService;
import edu.uci.ics.chakkl.service.gateway.configs.IdmConfigs;
import edu.uci.ics.chakkl.service.gateway.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.gateway.models.PlevelRequestModel;
import edu.uci.ics.chakkl.service.gateway.models.PlevelResponseModel;
import edu.uci.ics.chakkl.service.gateway.models.SessionRequestModel;
import edu.uci.ics.chakkl.service.gateway.models.SessionResponseModel;
import edu.uci.ics.chakkl.service.gateway.threadpool.ClientRequest;
import edu.uci.ics.chakkl.service.gateway.threadpool.HTTPMethod;
import edu.uci.ics.chakkl.service.gateway.transaction.TransactionGenerator;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Util {
    public static <T> T mapping(String jsonText, Class<T> className)
    {
        if(jsonText == null) {
            ServiceLogger.LOGGER.info("Nothing to map.");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
//        ServiceLogger.LOGGER.info("Mapping text: " + jsonText);
        ServiceLogger.LOGGER.info("Mapping object: " + className.getName());

        try {
            return mapper.readValue(jsonText, className);
        } catch (IOException e) {
            ServiceLogger.LOGGER.info("Mapping Object Failed: " + e.getMessage());
            return null;
        }
    }

    public static Response internal_server_error(Header header)
    {
        Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        header.setHeader(builder);
        return builder.build();
    }

    public static Response getPlevel(String email, int plevel)
    {
        PlevelRequestModel requestModel = new PlevelRequestModel(email, plevel);

        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);


        ServiceLogger.LOGGER.info("Building WebTarget...");
        IdmConfigs temp = GatewayService.getIdmConfigs();
        WebTarget webTarget = client.target(temp.getScheme()+temp.getHostName()+":"+temp.getPort()+temp.getPath()).path(temp.getPrivilegePath());
        ServiceLogger.LOGGER.info("Sending to path: " + temp.getPath() + temp.getPrivilegePath());

        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);


        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");
        PlevelResponseModel responseModel = Util.mapping(response.readEntity(String.class), PlevelResponseModel.class);
        if(responseModel.getResultCode() == 140)
            return null;
        return response;
    }

    public static Response isSessionValid(Header header)
    {
        SessionRequestModel requestModel = new SessionRequestModel(header.getEmail(), header.getSession_id());

        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);


        ServiceLogger.LOGGER.info("Building WebTarget...");
        IdmConfigs temp = GatewayService.getIdmConfigs();
        WebTarget webTarget = client.target(temp.getScheme()+temp.getHostName()+":"+temp.getPort()+temp.getPath()).path(temp.getSessionPath());

        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);


        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");
//        SessionResponseModel responseModel = response.readEntity(SessionResponseModel.class);
//        String jsonText = response.readEntity(String.class);
//        System.out.println(response.readEntity(String.class));
//        ServiceLogger.LOGGER.info("Jsontext: " + jsonText);
        SessionResponseModel responseModel = Util.mapping(response.readEntity(String.class), SessionResponseModel.class);
        if(responseModel.getResultCode() == 130) {
            header.setSession_id(responseModel.getSession_id());
            return null;
        }
        return response;
    }

    public static Response return204(String transaction_id, long request_delay, Header header)
    {
        Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
        builder = builder.header("transaction_id", transaction_id);
        builder = builder.header("request_delay", request_delay);
        if(header.getEmail() != null)
            builder = builder.header("email", header.getEmail());
        if(header.getSession_id() != null)
            builder = builder.header("session_id", header.getSession_id());
        if(header.getTransaction_id() != null)
            builder = builder.header("transaction_id", header.getTransaction_id());
        return builder.build();
    }

    public static Response buildPost(HttpHeaders headers, byte[] jsonBytes, String url, String path)
    {
        Header header = new Header(headers);
        if(header.getEmail() != null) {
            Response emailValid = isSessionValid(header);
            if (emailValid != null) {
                ServiceLogger.LOGGER.info("Session invalid.");
                Response.ResponseBuilder builder = Response.status(Response.Status.OK).entity(emailValid.readEntity(String.class));
                builder = builder.header("email", header.getEmail());
                builder = builder.header("session_id", header.getSession_id());
                builder = builder.header("transaction_id", header.getTransaction_id());
                return builder.build();
            }
        }
        String transaction_id = TransactionGenerator.generate();
        ClientRequest request = new ClientRequest(header.getEmail(), header.getSession_id(), transaction_id,
                                                    url, path, HTTPMethod.POST, jsonBytes, null);
        try {
            GatewayService.getThreadPool().putRequest(request);
        } catch (InterruptedException e) {
            ServiceLogger.LOGGER.info("Failed to put request in queue.");
            return internal_server_error(header);
        }
        return return204(transaction_id, GatewayService.getThreadConfigs().getRequestDelay(), header);
    }

    public static Response buildGet(HttpHeaders headers, String url, String path, HashMap<String,String> query)
    {
        Header header = new Header(headers);
        Response emailValid = isSessionValid(header);
        if(header.getEmail() != null) {
            if (emailValid != null) {
                ServiceLogger.LOGGER.info("Session invalid.");
                Response.ResponseBuilder builder = Response.status(Response.Status.OK).entity(emailValid.readEntity(String.class));
                builder = builder.header("email", header.getEmail());
                builder = builder.header("session_id", header.getSession_id());
                builder = builder.header("transaction_id", header.getTransaction_id());
                return builder.build();
            }
        }
        String transaction_id = TransactionGenerator.generate();
        ServiceLogger.LOGGER.info("Sending request to: " + url + path);
        ClientRequest request = new ClientRequest(header.getEmail(), header.getSession_id(), transaction_id,
                url, path, HTTPMethod.GET, null, query);
        try {
            GatewayService.getThreadPool().putRequest(request);
        } catch (InterruptedException e) {
            ServiceLogger.LOGGER.info("Failed to put request in queue.");
            return internal_server_error(header);
        }
        return return204(transaction_id, GatewayService.getThreadConfigs().getRequestDelay(), header);
    }
}
