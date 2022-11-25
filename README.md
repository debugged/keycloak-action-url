# Keycloak: Get required actions login url

This is a simple Keycloak plugin that adds support for generating the password reset link with multiple actions. But instead of only returning it by email, this plugin also gives it in the response.

```json
{
    "link": "https://realm/..."
}
```

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/debugged/keycloak-action-url?sort=semver)
![Keycloak Dependency Version](https://img.shields.io/badge/Keycloak-20.0.1-blue)
![GitHub Release Date](https://img.shields.io/github/release-date-pre/debugged/keycloak-action-url)
![Github Last Commit](https://img.shields.io/github/last-commit/debugged/keycloak-action-url)

![CI build](https://github.com/debugged/keycloak-action-url/actions/workflows/buildAndTest.yml/badge.svg)
![open issues](https://img.shields.io/github/issues/debugged/keycloak-action-url)

## What is it good for?

By default keycloak does not allow you to create a temporary login url and use it elsewhere. Keycloak only send it per email. This plugin adds a extra api endpoint which you can use to generate this link on demand.

## How to use?
```shell
curl --location --request POST 'http://localhost:8080/realms/test-realm/action-url' \
--header 'Authorization: Bearer <access-token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "user_id": "139020a3-4459-43b1-a92f-d90e5cf093a3",
    "client_id": "account",
    "lifespan": 43200,
    "redirectUri: "https://debugged.nl",
    "actions": ["VERIFY_EMAIL", "UPDATE_PASSWORD"]
}'
```
Response:
```json
{
    "link": "http://localhost:8080/realms/test-realm/login-actions/action-token?key=..."
}
```
