package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.SecondaryServerWriteOperationException;

public class SecondaryServerState extends ServerState {

    public SecondaryServerState(String service, String ns_host, int ns_port) {
        super(service, ns_host, ns_port);
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
