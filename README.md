# ZeroRebut-DASI12-g3 Plateforme anti-gaspillage alimentaire

Projet AWD · Classe 4DAS-I12 · Groupe 3 · ESPRIT 2026-2027

## Description
Plateforme en microservices qui met en relation commerçants, consommateurs et associations : paniers à prix réduits, dons alimentaires et récupération sécurisée par QR code.

## Équipe et microservices
| Microservice | Port | Base |
| MS1 · Stocks | 8081 | MySQL |
| MS2 · Offres | 8082 | MySQL |
| MS3 · Réservations | 8083 | MySQL |
| MS4 · Dons | 8084 | MySQL |
| MS5 · Collecte | 8085 | MySQL |
| MS6 · Signalements | 8086 | MySQL |
| MS7 · Notifications | 8087 | MongoDB |

## Technologies
Spring Boot · Spring Cloud Gateway · Eureka · Config Server · OpenFeign ·
RabbitMQ · Keycloak · MySQL · MongoDB · Angular · Docker

## Structure du dépôt
- `Backend/` : microservices, gateway, eureka, config server
- `Frontend/` : application Angular
- `Documentation/` : présentation, diagrammes, dossier technique
