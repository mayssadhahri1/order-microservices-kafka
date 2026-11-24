# Order Microservices with Kafka

Système de commandes en microservices Spring Boot qui communiquent de façon **asynchrone** avec Apache Kafka, derrière une **API Gateway**.

![CI](https://github.com/mayssadhahri1/order-microservices-kafka/actions/workflows/ci.yml/badge.svg)

## Architecture

```
                         ┌──────────────────┐
  Client ──HTTP──►  API Gateway :8080       │  Spring Cloud Gateway
                         └───┬─────┬─────┬──┘
                /api/orders  │     │     │  /api/notifications
                             ▼     │     ▼
                  ┌──────────────┐ │ ┌────────────────────┐
                  │ order-service│ │ │notification-service│
                  │    :8081     │ │ │       :8083        │
                  └──┬────────▲──┘ │ └─────────▲──────────┘
     orders.created  │        │    │/api/products
                     ▼        │    ▼           │
                  ┌───────────┴────────────┐   │
                  │         Kafka          │───┘ orders.status
                  └───────────┬────────────┘
                              ▼
                  ┌────────────────────────┐
                  │   inventory-service    │
                  │        :8082           │
                  └────────────────────────┘
```

### Parcours d'une commande

1. Le client envoie `POST /api/orders`. L'order-service enregistre la commande au statut `PENDING` et répond **202 Accepted**.
2. Après la validation de la transaction, il publie `OrderCreated` sur le topic `orders.created`.
3. L'inventory-service réserve le stock et publie `OrderStatusChanged` (`CONFIRMED` ou `REJECTED`) sur `orders.status`.
4. L'order-service met à jour la commande, et le notification-service enregistre un message pour le client. Chacun utilise son propre groupe de consommateurs et reçoit donc sa propre copie de l'événement.

## Choix techniques

| Problème | Solution |
|---|---|
| Annoncer une commande qui n'existe pas en base | Publication Kafka déclenchée **après le commit** (`TransactionSynchronization`) |
| Message Kafka reçu deux fois (livraison « au moins une fois ») | Table `processed_orders` : traitement **idempotent** du stock |
| Statut d'une commande modifié deux fois | Une commande ne quitte l'état `PENDING` qu'une seule fois |
| Deux réservations simultanées sur le même produit | Verrou optimiste JPA (`@Version`) |
| Ordre des messages d'une même commande | Clé Kafka = id de la commande, donc toujours la même partition |
| Un point d'entrée pour le client | Spring Cloud Gateway, routage par chemin |

## Services

| Service | Port | Rôle | Endpoints |
|---|---|---|---|
| api-gateway | 8080 | Routage | `/api/**` |
| order-service | 8081 | Commandes | `POST /api/orders`, `GET /api/orders`, `GET /api/orders/{id}` |
| inventory-service | 8082 | Stock | `GET /api/products`, `POST /api/products/{code}/restock` |
| notification-service | 8083 | Notifications | `GET /api/notifications?recipient=` |

## Lancer le projet

```bash
docker compose up --build
```

Puis, à travers la passerelle :

```bash
# Commande acceptée (stock suffisant)
curl -X POST localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"customerEmail":"client@test.tn","productCode":"LAPTOP-01","quantity":2}'

# Commande refusée (le stock de SCREEN-03 est de 3)
curl -X POST localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"customerEmail":"client@test.tn","productCode":"SCREEN-03","quantity":10}'

curl localhost:8080/api/orders          # statuts mis à jour
curl localhost:8080/api/products        # stock restant
curl localhost:8080/api/notifications   # messages envoyés aux clients
```

## Tests

```bash
mvn verify
```

Tests unitaires de la logique métier (réservation, refus, doublons, transitions de statut, messages) et test de configuration des routes de la passerelle.

## Pistes d'amélioration

- Dead Letter Topic pour les messages impossibles à traiter.
- Pattern Outbox pour garantir la publication même en cas de panne juste après le commit.
- Tests d'intégration avec Testcontainers (Kafka réel).
- Traçage distribué avec Micrometer Tracing et Zipkin.

## Auteure

Mayssa Dhahri, élève-ingénieure en génie logiciel à l'École Polytechnique de Sousse.
[LinkedIn](https://www.linkedin.com/in/mayssa-dhahri-38b1373bb/)
