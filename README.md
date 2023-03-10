# DistLedger

Distributed Systems Project 2022/2023

## Authors

**Group A10**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace **GXX** with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name          | User                          | Email                                      |
| ------ | ------------- | ----------------------------- | ------------------------------------------ |
| 99202  | Diogo Melita  | <https://github.com/d-melita> | <mailto:diogo.melita@tecnico.ulisboa.pt>   |
| 99209  | Diogo Cardoso | <https://github.com/D-Card>   | <mailto:diogocardoso67@tecnico.ulisboa.pt> |
| 99335  | Tiago Silva   | <https://github.com/trvds>    | <mailto:tiagovsilva@tecnico.ulisboa.pt>    |

## Getting Started

The overall system is made up of several modules. The main server is the _DistLedgerServer_. The clients are the _User_
and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/DistLedger) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

### Compile and Run the Server and Clients

To compile the server and clients:

```s
mvn compile
```

To run the server or clients navigate to the folder of each module and run:

- For the server:

```s
    mvn exec:java -Dexec.args="<port>"
```

- For the admin and user clients:

```s
    mvn exec:java -Dexec.args="<host> <port>"
```

You can also run the server and clients without arguments, in which case the default values will be used (see pom.xml for the default values):

```s
mvn exec:java
```

### Run the Server in debug mode

To run the server in debug mode you can use the flag:

```s
mvn exec:java -Ddebug
```

You can also add the port and host arguments:

```s
mvn exec:java -Ddebug -Dexec.args="<host> <port>"
```

### Run the tests for the server and clients

To run the tests for the server and clients:

```s
mvn clean verify
```

## Built With

- [Maven](https://maven.apache.org/) - Build and dependency management tool;
- [gRPC](https://grpc.io/) - RPC framework.
