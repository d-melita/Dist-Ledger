package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.SecondaryServerWriteOperationException;
import pt.tecnico.distledger.server.grpc.NamingServerService;

public class SecondaryServerState extends ServerState {

    public SecondaryServerState(String service, String ns_host, int ns_port, NamingServerService namingServerService) {
        super(service, ns_host, ns_port, namingServerService);
    }

    @Override
    public synchronized void createAccount(String name) {
        throw new SecondaryServerWriteOperationException();
    }

    @Override
    public synchronized void deleteAccount(String name) {
        throw new SecondaryServerWriteOperationException();
    }

    @Override
    public synchronized void transfer(String from, String to, Integer amount) {
        throw new SecondaryServerWriteOperationException();
    }
}
