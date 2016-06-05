package net.shiroka.tools.ofx

import com.amazonaws.auth._

package object aws {
  def getCredentials = sys.env.get("AWS_SESSION_TOKEN") match {
    case None => new DefaultAWSCredentialsProviderChain()
    case Some(token) =>
      new AWSCredentialsProviderChain(
        new AWSCredentialsProvider {
          def getCredentials: AWSCredentials =
            new BasicSessionCredentials(
              sys.env("AWS_ACCESS_KEY_ID"),
              sys.env("AWS_SECRET_ACCESS_KEY"),
              token
            )
          def refresh = Unit;
        }
      )
  }
}
