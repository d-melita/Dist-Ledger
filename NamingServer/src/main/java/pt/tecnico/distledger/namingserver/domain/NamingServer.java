package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.utils.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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
            serviceEntry.addServer(new ServerEntry(host, qualifier));
        } else {
            addService(service, host, qualifier);
        }
    }

    public void addService(String service, String host, String qualifier) {
        ServiceEntry serviceEntry = new ServiceEntry(service);
        serviceEntry.addServer(new ServerEntry(host, qualifier));
        services.put(service, serviceEntry);
    }
}
