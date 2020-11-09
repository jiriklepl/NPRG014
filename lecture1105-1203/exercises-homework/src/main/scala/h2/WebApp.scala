package h2

import scala.collection.mutable


class Session

trait SessionProvider {
  def session: Session
}

trait DefaultSessionProvider extends SessionProvider {
  val dummySession = new Session

  override def session = dummySession
}


abstract class Identity {
}

trait IdentityCache {
  type IdentityType <: Identity

  def getOrAuthenticate(): IdentityType
}

trait InMemoryIdentityCache extends IdentityCache with SessionProvider with Authenticator {
  val cache = mutable.Map.empty[Session, IdentityType]

  override def getOrAuthenticate(): IdentityType = {
    cache.getOrElseUpdate(session, authenticate)
  }
}


trait Authenticator {
  type IdentityType <: Identity

  def authenticate(): IdentityType
}


trait UsesSAMLIdentity {
  type IdentityType = SAMLIdentity
}

class SAMLIdentity(val saml: String) extends Identity

trait SAMLAuthenticator extends Authenticator with UsesSAMLIdentity {
  val dummySAMLIdentity = new SAMLIdentity("XXX")

  override def authenticate() = dummySAMLIdentity
}


trait RoleManager {
  def hasRole(role: String): Boolean
}

trait SAMLRoleManager extends RoleManager with InMemoryIdentityCache with SAMLAuthenticator {
  override def hasRole(role: String) = {
    val identity = getOrAuthenticate()
    identity.saml == "XXX"
  }
}


object WebApp extends SAMLRoleManager with DefaultSessionProvider {
  def main(args: Array[String]): Unit = {
    println(hasRole("YYY")) // Prints "true"
  }
}
