# Relatório da fase 3 do projeto - Grupo A10

## Introdução
A fase três deste projeto pretende que o serviço DistLedger seja fornecido por 2 ou mais servidores, conhecidos à priori, e usando arquitetura gossip. O número máximo de servidores é definido no Naming Server e de forma a que clientes e servidores saibam quantos existem, adicionámos esta chamada no protocolo.

## Gossip
A arquitetura gossip é uma forma descentralizada de comunicação em que os servidores trocam informações periodicamente, em background. O objetivo desta arquitetura é oferecer sempre acesso rápido aos clientes mesmo em situações de partição contudo, inevitavelmente, a coerência entre réplicas acaba por ser sacrificada. No DistLedger, o gossip é feito pelo administrador, que indica que réplica deve trocar informação com as outras pelo que não é feito periodicamente tal como foi referido.

## Timestamps
Para ser possível utilizar arquitetura gossip, é necessário que tanto os utilizadores como as réplicas utilizem timestamps no formato de relógios vetoriais. Estes timestamps irão ser bastante úteis para que as réplicas consigam saber quando podem realizar as operações pedidas pelos clientes. 

### Implementação
Na nossa implementação, as réplicas respondem sempre OK aos clientes após cada pedido em situações de instabilidade, em vez de ficarem indeterminadamente à espera que sejam atualizadas, isto é, nos casos em que o prevTS do cliente é maior que o replicaTS do servidor ao qual , tal como foi apresentado em aula pois o gossip utilizado neste serviço só acontece após ordem do administrador, como referido acima. Além disso, optámos por utilizar um Set registeredOps que terá a função de verificar se uma operação já foi executada pelo servidor quando este a recebe via gossip evitando assim ter operações repetidas na ledger. Este Set guarda os vários operation.TS que funcionam como IDs destas.