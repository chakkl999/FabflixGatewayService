package edu.uci.ics.chakkl.service.gateway.threadpool;

import edu.uci.ics.chakkl.service.gateway.GatewayService;
import edu.uci.ics.chakkl.service.gateway.logger.ServiceLogger;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class Worker extends Thread {
    int id;
    ThreadPool threadPool;

    private Worker(int id, ThreadPool threadPool) {
        this.id = id;
        this.threadPool = threadPool;
    }

    public static Worker CreateWorker(int id, ThreadPool threadPool) {
        return new Worker(id, threadPool);
    }

    public void process(ClientRequest request, Connection con) {
        ServiceLogger.LOGGER.info("Thread " + id + " is processing request " + request.getEndpoint());
        if(request.getMethod().toString().equals("GET")) {
            ServiceLogger.LOGGER.info("Building client...");
            Client client = ClientBuilder.newClient();
            client.register(JacksonFeature.class);

            ServiceLogger.LOGGER.info("Building WebTarget...");
            WebTarget webTarget = client.target(request.getURI()).path(request.getEndpoint());
            if(request.getQuery() != null) {
                for(Map.Entry<String, String> entry : request.getQuery().entrySet()) {
                    webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
                }
            }
            ServiceLogger.LOGGER.info("Sending to path: " + request.getURI() + request.getEndpoint());
            ServiceLogger.LOGGER.info("Starting invocation builder...");
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            ServiceLogger.LOGGER.info("Sending request...");
            invocationBuilder.header("email", request.getEmail());
            invocationBuilder.header("session_id", request.getSession_id());
            invocationBuilder.header("transaction_id", request.getTransaction_id());
            Response response = invocationBuilder.get();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO responses VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, request.getTransaction_id());
                ps.setString(2, request.getEmail());
                ps.setString(3, request.getSession_id());
                ps.setString(4, response.readEntity(String.class));
                ps.setInt(5, response.getStatus());
                ps.executeUpdate();
            } catch (Exception e) {
                ServiceLogger.LOGGER.info("Failed to insert response into database.");
                ServiceLogger.LOGGER.info(e.getMessage());
            }
            ServiceLogger.LOGGER.info("Request sent.");
        } else {
            ServiceLogger.LOGGER.info("Building client...");
            Client client = ClientBuilder.newClient();
            client.register(JacksonFeature.class);

            ServiceLogger.LOGGER.info("Building WebTarget...");
            WebTarget webTarget = client.target(request.getURI()).path(request.getEndpoint());
            ServiceLogger.LOGGER.info("Sending to path: " + request.getEndpoint());

            ServiceLogger.LOGGER.info("Starting invocation builder...");
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            ServiceLogger.LOGGER.info("Sending request...");
            invocationBuilder.header("email", request.getEmail());
            invocationBuilder.header("session_id", request.getSession_id());
            invocationBuilder.header("transaction_id", request.getTransaction_id());
            Response response = invocationBuilder.post(Entity.entity(request.getRequestBytes(), MediaType.APPLICATION_JSON));
//            System.out.println(response.toString());
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO responses VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, request.getTransaction_id());
                ps.setString(2, request.getEmail());
                ps.setString(3, request.getSession_id());
                ps.setString(4, response.readEntity(String.class));
                ps.setInt(5, response.getStatus());
                ps.executeUpdate();
            } catch (Exception e) {
                ServiceLogger.LOGGER.info("Failed to insert response into database.");
                ServiceLogger.LOGGER.info(e.getMessage());
            }
            ServiceLogger.LOGGER.info("Request sent.");
        }
        ServiceLogger.LOGGER.info("Request complete");
    }

    @Override
    public void run() {
        while (true) {
            try {
                ClientRequest request = threadPool.takeRequest();
                Connection con = GatewayService.getConnectionPoolManager().requestCon();
                process(request, con);
                GatewayService.getConnectionPoolManager().releaseCon(con);
            } catch (SQLException e) {
                ServiceLogger.LOGGER.info("Thread " + id + " failed to get/release a connection.");
                ServiceLogger.LOGGER.info(e.getMessage());
            } catch (InterruptedException e) {
                ServiceLogger.LOGGER.info("Thread " + id + " failed to get a request.");
                ServiceLogger.LOGGER.info(e.getMessage());
            } catch (Exception e) {
                ServiceLogger.LOGGER.info("Something went wrong");
                ServiceLogger.LOGGER.info(e.getMessage());
            }
        }
    }

}
