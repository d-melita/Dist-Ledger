package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.utils.Logger;
import pt.tecnico.distledger.namingserver.exceptions.RemovalFailedException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class NamingServer {

    private Map<String, ServiceEntry> services;

    public NamingServer() {
        Logger.log("Initializing NamingServer");
        this.services = new ConcurrentHashMap<>();
        Logger.log("NamingServer initialized");
    }

    public void register(String service, String host, String qualifier) {
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            serviceEntry.addServer(host, qualifier);
        } else {
            addService(service, host, qualifier);
        }
    }

    public void addService(String service, String host, String qualifier) {
        ServiceEntry serviceEntry = new ServiceEntry(service);
        serviceEntry.addServer(host, qualifier);
        services.put(service, serviceEntry);
    }

    public List<String> lookup(String service, String qualifier) {
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            return serviceEntry.lookupServer(qualifier);
        }
        return new ArrayList<String>();
    }

    public List<String> lookup(String service) {
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            return serviceEntry.lookupServer();
        }
        return new ArrayList<String>();
    }

    public void delete(String service, String host) {
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            serviceEntry.removeServer(host);
        } else {
            throw new RemovalFailedException(service);
        }
    }
}
