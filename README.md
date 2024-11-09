# Projeto Marketplace

Este projeto é uma aplicação de marketplace que permite aos usuários comprarem e venderem produtos online. A aplicação utiliza uma arquitetura de microserviços com autenticação, gerenciamento de produtos, carrinho de compras e um sistema de pedidos, além de observabilidade e integração com AWS SQS para comunicação assíncrona entre os serviços.

## Tecnologias Utilizadas

- **Backend**: Java, Spring Boot
- **Frontend**: Angular
- **Banco de Dados**: PostgreSQL (produção), H2 Database (desenvolvimento/teste), Redis (planejado)
- **Mensageria**: AWS SQS (Localstack para ambiente local)
- **Armazenamento**: Amazon S3 (Localstack para ambiente local)
- **Testes**: JUnit, Mockito
- **Observabilidade**: Grafana, Prometheus
- **API Gateway**: Kong
- **Orquestração**: Docker

## Funcionalidades

- **Auth**: Gerenciamento de autenticação de usuários com suporte a Tokens de Acesso e Refresh Tokens, armazenados em cookies para maior segurança.
- **Usuário**: CRUD de usuários, com diferentes tipos de conta (usuário, vendedor, admin) e gerenciamento de endereço.
- **Produto**: CRUD de produtos, verificação de estoque, e sistema de avaliações para os produtos.
- **Carrinho**: Gerenciamento do carrinho de compras, incluindo adição e remoção de itens, e finalização do pedido.
- **Pedido**: Processamento de pedidos com gerenciamento de endereços de entrega, valor da compra, status do pedido e formas de pagamento.
- **Observabilidade**: Monitoramento de métricas e logs dos microserviços usando Grafana e Prometheus.
- **API Gateway**: Kong como ponto de entrada para o backend.

## Estrutura do Projeto

1. **Auth**: Serviço de autenticação que gerencia o login e os tokens de sessão dos usuários.
2. **Usuário**: Serviço responsável por gerenciar os dados dos usuários, como dados pessoais e endereços.
3. **Produto**: Serviço que gerencia o catálogo de produtos, incluindo informações detalhadas e avaliações dos produtos.
4. **Carrinho**: Serviço que gerencia o carrinho de compras, permitindo adicionar e remover produtos e finalizar pedidos.
5. **Pedido**: Serviço responsável por processar e rastrear pedidos finalizados.
6. **Observabilidade**: Serviço que centraliza métricas e logs dos microserviços para monitoramento.

## Instalação

### Pré-requisitos

- **Docker**: Necessário para orquestrar os containers dos microserviços.
- **Java 17+**: Para execução do backend em Spring Boot.
- **Node.js + Angular CLI**: Para execução e desenvolvimento do frontend.
- **PostgreSQL**: Banco de dados utilizado na produção.

### Clone o repositório

Primeiro, clone o repositório do projeto para o seu ambiente local:

```bash
git clone https://github.com/riosbergdutra/Mercadox.git
cd marketplace

