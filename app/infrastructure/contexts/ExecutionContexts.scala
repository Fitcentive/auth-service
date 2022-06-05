package infrastructure.contexts

import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}

@Singleton
class KeycloakClientExecutionContext @Inject() (actorSystem: akka.actor.ActorSystem)
  extends CustomExecutionContext(actorSystem, "contexts.keycloak-client-execution-context")

@Singleton
class KeycloakServerExecutionContext @Inject() (actorSystem: akka.actor.ActorSystem)
  extends CustomExecutionContext(actorSystem, "contexts.keycloak-server-execution-context")
