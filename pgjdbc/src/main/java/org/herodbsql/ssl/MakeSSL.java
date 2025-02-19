/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.ssl;

import org.herodbsql.PGProperty;
import org.herodbsql.core.PGStream;
import org.herodbsql.core.SocketFactoryFactory;
import org.herodbx.herossl.HeroSSLSocketHelper;
import org.herodbsql.jdbc.SslMode;
import org.herodbsql.util.GT;
import org.herodbsql.util.ObjectFactory;
import org.herodbsql.util.PSQLException;
import org.herodbsql.util.PSQLState;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MakeSSL extends ObjectFactory {

  private static final Logger LOGGER = Logger.getLogger(MakeSSL.class.getName());

  public static void convert(PGStream stream, Properties info, String user)
      throws PSQLException, IOException {
    LOGGER.log(Level.FINE, "converting regular socket connection to ssl");
    if (true) {
      //create herosslsocket and use it
      HeroSSLSocketHelper.switchToHeroSSLSocket(stream, user);
      return;
    }

    SSLSocketFactory factory = SocketFactoryFactory.getSslSocketFactory(info);
    SSLSocket newConnection;
    try {
      newConnection = (SSLSocket) factory.createSocket(stream.getSocket(),
          stream.getHostSpec().getHost(), stream.getHostSpec().getPort(), true);
      // We must invoke manually, otherwise the exceptions are hidden
      newConnection.setUseClientMode(true);
      newConnection.startHandshake();
    } catch (IOException ex) {
      throw new PSQLException(GT.tr("SSL error: {0}", ex.getMessage()),
          PSQLState.CONNECTION_FAILURE, ex);
    }
    if (factory instanceof LibPQFactory) { // throw any KeyManager exception
      ((LibPQFactory) factory).throwKeyManagerException();
    }

    SslMode sslMode = SslMode.of(info);
    if (sslMode.verifyPeerName()) {
      verifyPeerName(stream, info, newConnection);
    }

    stream.changeSocket(newConnection);
  }

  private static void verifyPeerName(PGStream stream, Properties info, SSLSocket newConnection)
      throws PSQLException {
    HostnameVerifier hvn;
    String sslhostnameverifier = PGProperty.SSL_HOSTNAME_VERIFIER.get(info);
    if (sslhostnameverifier == null) {
      hvn = PGjdbcHostnameVerifier.INSTANCE;
      sslhostnameverifier = "PgjdbcHostnameVerifier";
    } else {
      try {
        hvn = (HostnameVerifier) instantiate(sslhostnameverifier, info, false, null);
      } catch (Exception e) {
        throw new PSQLException(
            GT.tr("The HostnameVerifier class provided {0} could not be instantiated.",
                sslhostnameverifier),
            PSQLState.CONNECTION_FAILURE, e);
      }
    }

    if (hvn.verify(stream.getHostSpec().getHost(), newConnection.getSession())) {
      return;
    }

    throw new PSQLException(
        GT.tr("The hostname {0} could not be verified by hostnameverifier {1}.",
            stream.getHostSpec().getHost(), sslhostnameverifier),
        PSQLState.CONNECTION_FAILURE);
  }

}
