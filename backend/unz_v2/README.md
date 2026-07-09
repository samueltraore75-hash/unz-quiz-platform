---
title: UNZ Quiz Backend
emoji: 🎓
colorFrom: green
colorTo: blue
sdk: docker
app_port: 7860
pinned: false
---

# UNZ Quiz — Backend API

API REST Spring Boot de la plateforme d'évaluation en ligne de l'Université Norbert Zongo (UNZ).

- Authentification JWT + refresh token
- Gestion des filières, classes, matières, quiz, notes et bulletins
- Voir `/api/...` pour les endpoints (protégés par rôle : ADMIN / ENSEIGNANT / ETUDIANT)

Déployé en conteneur Docker. La configuration (base de données, secrets, CORS) se fait via les
variables d'environnement définies dans l'onglet **Settings → Variables and secrets** de ce Space.
