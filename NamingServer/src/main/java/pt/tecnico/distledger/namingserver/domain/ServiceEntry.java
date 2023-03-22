package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.utils.Logger;
import pt.tecnico.distledger.namingserver.exceptions.RegistryFailedException;
import pt.tecnico.distledger.namingserver.exceptions.RemovalFailedException;

import java.util.List;
import java.util.ArrayList;

public class ServiceEntry {
    private String service;

    private List<ServerEntry> servers;

    public ServiceEntry(String service) {
        this.service = service;
        this.servers = new ArrayList<ServerEntry>();
    }

    public String getService() {
        return service;
    }

    public synchronized ServerEntry getServer(String host) {
        for (ServerEntry server : servers) {
            if (server.getHost().equals(host)) {
                return server;
            }
        }
        return null;
    }

    private void addServer(ServerEntry server) {
        servers.add(server);
    }

    private void removeServer(ServerEntry server) {
        servers.remove(server);
    }

    public synchronized void addServer(String host, String qualifier) {
        Logger.log("Adding server " + host + " to service " + service);
        if (this.getServer(host) != null) {
            throw new RegistryFailedException(host);
        }
        this.addServer(new ServerEntry(host, qualifier));
    }

    public synchronized List<String> lookupServer(String qualifier) {
        Logger.log("Looking up servers with qualifier " + qualifier + " in service " + service);
        List<String> hosts = new ArrayList<String>();
        for (ServerEntry server : servers) {
            if (server.getQualifier().equals(qualifier)) {
                hosts.add(server.getHost());
            }
        }
        return hosts;
    }

    public synchronized List<String> lookupServer() {
        Logger.log("Looking up servers in service " + service);
        List<String> hosts = new ArrayList<String>();
        for (ServerEntry server : servers) {
            hosts.add(server.getHost());
        }
        return hosts;
    }

    public synchronized void removeServer(String host) {
        Logger.log("Removing server " + host + " from service " + service);
        ServerEntry server = this.getServer(host);
        if (server != null) {
            this.removeServer(server);
        } else {
            throw new RemovalFailedException(host);
        }
    }
}
