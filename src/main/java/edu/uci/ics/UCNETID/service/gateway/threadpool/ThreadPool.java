package edu.uci.ics.UCNETID.service.gateway.threadpool;

public class ThreadPool {
    private int numWorkers;
    private Worker[] workers;
    private ClientRequestQueue queue;

    public ThreadPool(int numWorkers) {

    }

    public void add(ClientRequest clientRequest) {

    }

    public ClientRequest remove() {
        return null;
    }

    public ClientRequestQueue getQueue() {
        return null;
    }
}
