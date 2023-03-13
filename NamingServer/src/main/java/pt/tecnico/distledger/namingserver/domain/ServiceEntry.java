package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.utils.Logger;

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

    public List<ServerEntry> getServers() {
        return servers;
    }

    public void addServer(ServerEntry server) {
        servers.add(server);
    }

    public void removeServer(ServerEntry server) {
        servers.remove(server);
    }
}
