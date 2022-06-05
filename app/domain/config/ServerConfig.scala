package domain.config

import com.typesafe.config.Config

case class ServerConfig(host: String)

object ServerConfig {
  def fromConfig(config: Config): ServerConfig =
    ServerConfig(host = config.getString("host"))
}
