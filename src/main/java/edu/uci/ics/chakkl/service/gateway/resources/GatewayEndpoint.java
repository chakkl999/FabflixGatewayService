package edu.uci.ics.chakkl.service.gateway.resources;

import edu.uci.ics.chakkl.service.gateway.GatewayService;
import edu.uci.ics.chakkl.service.gateway.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.gateway.models.SessionResponseModel;
import edu.uci.ics.chakkl.service.gateway.util.Header;
import edu.uci.ics.chakkl.service.gateway.util.Util;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/")
public class GatewayEndpoint {
    @Path("report")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response report(@Context HttpHeaders headers)
    {
        ServiceLogger.LOGGER.info("Requesting report");
        Header header = new Header(headers);
        if(header.getEmail() != null) {
            SessionResponseModel emailValid = Util.isSessionValid(header);
            if (emailValid != null) {
                ServiceLogger.LOGGER.info("Session invalid.");
                Response.ResponseBuilder builder = Response.status(Response.Status.OK).entity(emailValid);
                builder = builder.header("email", header.getEmail());
                builder = builder.header("session_id", header.getSession_id());
                builder = builder.header("transaction_id", header.getTransaction_id());
                return builder.build();
            }
        }
//        ServiceLogger.LOGGER.info("Check if there's a response");
        Connection con;
        Response response;
        try {
            con = GatewayService.getConnectionPoolManager().requestCon();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM responses WHERE transaction_id LIKE ?");
            ps.setString(1, header.getTransaction_id());
//            ps.setString(2, header.getEmail());
//            ps.setString(3, header.getSession_id());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                ServiceLogger.LOGGER.info("Has response.");
//                System.out.println(rs.getString("response"));
                Response.ResponseBuilder builder = Response.status(rs.getInt("http_status")).entity(rs.getString("response"));
                builder = builder.header("email", header.getEmail());
                builder = builder.header("session_id", header.getSession_id());
                builder = builder.header("transaction_id", header.getTransaction_id());
                response = builder.build();
                ps = con.prepareStatement("DELETE FROM responses WHERE transaction_id LIKE ?");
                ps.setString(1, header.getTransaction_id());
                ps.executeUpdate();
            } else {
                System.out.println("not ready yet");
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder = builder.header("message", "Not ready.");
                builder = builder.header("transaction_id", header.getTransaction_id());
                builder = builder.header("request_delay", GatewayService.getThreadConfigs().getRequestDelay());
                builder = builder.header("email", header.getEmail());
                builder = builder.header("session_id", header.getSession_id());
                response = builder.build();
            }
            GatewayService.getConnectionPoolManager().releaseCon(con);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return Util.internal_server_error(header);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Util.internal_server_error(header);
        }
//        System.out.println("not ready yet");
        return response;
    }
}
