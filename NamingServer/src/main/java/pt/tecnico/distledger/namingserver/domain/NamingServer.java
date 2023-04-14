package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.utils.Logger;
import pt.tecnico.distledger.namingserver.exceptions.RemovalFailedException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class NamingServer {

    private Map<String, ServiceEntry> services;
    private final int maxServersPerService;

    public NamingServer(int maxServersPerService) {
        Logger.log("Initializing NamingServer");
        this.services = new ConcurrentHashMap<>();
        Logger.log("NamingServer initialized");
        this.maxServersPerService = maxServersPerService;
    }

    public int getMaxServersofService(String service) {
        return services.get(service).getMaxServers();
    }

    public synchronized int register(String service, String host, String qualifier) {
        Logger.log("Register operation: " + service + " " + host + " " + qualifier);
        if (services.containsKey(service)) {
            Logger.log("Service already exists");
            ServiceEntry serviceEntry = services.get(service);
            serviceEntry.addServer(host, qualifier);
        } else {
            addService(service, host, qualifier, maxServersPerService);
        }
        return (services.get(service).getNumServers() - 1);
    }

    public synchronized void addService(String service, String host, String qualifier, int maxServersPerService) {
        Logger.log("Adding new service " + service);
        ServiceEntry serviceEntry = new ServiceEntry(service, maxServersPerService);
        serviceEntry.addServer(host, qualifier);
        services.put(service, serviceEntry);
    }

    public synchronized List<String> lookup(String service, String qualifier) {
        Logger.log("Lookup operation: " + service + " " + qualifier);
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            return serviceEntry.lookupServer(qualifier);
        }
        return new ArrayList<String>();
    }

    public synchronized List<String> lookup(String service) {
        Logger.log("Lookup operation: " + service);
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            return serviceEntry.lookupServer();
        }
        return new ArrayList<String>();
    }

    public synchronized void delete(String service, String host) {
        Logger.log("Delete operation: " + service + " " + host);
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            serviceEntry.removeServer(host);
        } else {
            throw new RemovalFailedException(service);
        }
    }
}
