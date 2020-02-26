package edu.uci.ics.chakkl.service.gateway.util;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class Header {
    private String email, session_id, transaction_id;

    public Header(HttpHeaders headers)
    {
        email = headers.getHeaderString("email");
        session_id = headers.getHeaderString("session_id");
        transaction_id = headers.getHeaderString("transaction_id");
    }

    public String getEmail() {
        return email;
    }

    public String getSession_id() {
        return session_id;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public Response.ResponseBuilder setHeader(Response.ResponseBuilder builder)
    {
        if(email != null)
            builder = builder.header("email", email);
        if(session_id != null)
            builder = builder.header("session_id", session_id);
        if(transaction_id != null)
            builder = builder.header("transaction_id", transaction_id);
        return builder;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }
}
