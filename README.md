# deliverizr

A hack day project by Alex Blease, David Evans and Josh Hill.

## Local development

### Prerequisites

Java 8 SDK
```
brew cask install java
```

Spring Boot CLI
```
brew tap pivotal/tap
brew install springboot
```

Concourse Lite
```
vagrant init concourse/lite
vagrant up
```

Concourse CLI
```
open http://192.168.100.4:8080
```
Download the `fly` binary from Concourse Lite, by clicking on the Apple icon.

Then make it executable and put it in your `$PATH`, for example:

```
install ~/Downloads/fly /usr/local/bin/fly
```

## Configuration

```
export GITHUB_USERNAME=REPLACE_WITH_GITHUB_USERNAME
export GITHUB_PASSWORD=REPLACE_WITH_GITHUB_PASSWORD_OR_PERSONAL_ACCESS_TOKEN

export CF_API=REPLACE_WITH_CF_API_URL
export CF_USERNAME=REPLACE_WITH_CF_USERNAME
export CF_PASSWORD=REPLACE_WITH_CF_PASSWORD
export CF_ORG=REPLACE_CF_ORG_NAME
export CF_SPACE=REPLACE_WITH_CF_SPACE_NAME
```

If you use `direnv` then a `.envrc.example` is provided for convenience.

## Run

```
./mvnw clean spring-boot:run
```

## Deliverize a new Spring Boot app

```
curl -X POST localhost:8080?project_name=test42
```

This will:
- Generate a new Spring Boot app using Spring Initializr 
- Add a Cloud Foundry app manifest
- Add a basic Concourse pipeline
- Create a repo on GitHub
- Set a new pipeline on Concourse Lite
- Unpause the new pipeline
- Concourse deploys the app to Cloud Foundry
