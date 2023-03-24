package pt.tecnico.distledger.namingserver.domain;

public class ServerEntry {
    private String host;
    private String qualifier;

    public ServerEntry(String host, String qualifier) {
        this.host = host;
        this.qualifier = qualifier;
    }

    public String getHost() {
        return host;
    }

    public String getQualifier() {
        return qualifier;
    }
}
