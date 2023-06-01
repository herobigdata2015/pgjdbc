/*
 * Copyright 2017, OnGres.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package org.herodbx.jre7.sasl;

import org.herodbx.scram100b2.common.ScramFunctions;
import org.herodbx.scram100b2.common.ScramMechanism;
import org.herodbx.scram100b2.common.exception.ScramInvalidServerSignatureException;
import org.herodbx.scram100b2.common.exception.ScramParseException;
import org.herodbx.scram100b2.common.exception.ScramServerErrorException;
import org.herodbx.scram100b2.common.gssapi.Gs2CbindFlag;
import org.herodbx.scram100b2.common.message.ClientFinalMessage;
import org.herodbx.scram100b2.common.message.ClientFirstMessage;
import org.herodbx.scram100b2.common.message.ServerFinalMessage;
import org.herodbx.scram100b2.common.message.ServerFirstMessage;
import org.herodbx.scram100b2.common.stringprep.StringPreparation;
import org.herodbx.herossl.HeroSP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.herodbx.scram100b2.common.util.Preconditions.checkNotEmpty;
import static org.herodbx.scram100b2.common.util.Preconditions.checkNotNull;


/**
 * A class that represents a SCRAM client. Use this class to perform a SCRAM negotiation with a SCRAM server.
 * This class performs an authentication execution for a given user, and has state related to it.
 * Thus, it cannot be shared across users or authentication executions.
 */
public class HeroScramSession {
  static private Logger LOGGER = Logger.getLogger(HeroScramSession.class.getName());

  private final ScramMechanism scramMechanism;
  private final StringPreparation stringPreparation;
  private final String user;
  private final String nonce;
  private ClientFirstMessage clientFirstMessage;
  private String serverFirstMessageString;

  /**
   * Constructs a SCRAM client, to perform an authentication for a given user.
   * This class can be instantiated directly,
   * but it is recommended that a {@link HeroScramClient} is used instead.
   *
   * @param scramMechanism    The SCRAM mechanism that will be using this client
   * @param stringPreparation
   * @param user
   * @param nonce
   */
  public HeroScramSession(ScramMechanism scramMechanism, StringPreparation stringPreparation, String user, String nonce) {
    this.scramMechanism = checkNotNull(scramMechanism, "scramMechanism");
    this.stringPreparation = checkNotNull(stringPreparation, "stringPreparation");
    this.user = checkNotEmpty(user, "user");
    this.nonce = checkNotEmpty(nonce, "nonce");
  }

  private String setAndReturnClientFirstMessage(ClientFirstMessage clientFirstMessage) {
    this.clientFirstMessage = clientFirstMessage;

    return clientFirstMessage.toString();
  }

  /**
   * Returns the text representation of a SCRAM client-first-message, with the GSS-API header values indicated.
   *
   * @param gs2CbindFlag The channel binding flag
   * @param cbindName    The channel binding algorithm name, if channel binding is supported, or null
   * @param authzid      The optional
   * @return The message
   */
  public String clientFirstMessage(Gs2CbindFlag gs2CbindFlag, String cbindName, String authzid) {
    return setAndReturnClientFirstMessage(new ClientFirstMessage(gs2CbindFlag, authzid, cbindName, user, nonce));
  }

  /**
   * Returns the text representation of a SCRAM client-first-message, with no channel binding nor authzid.
   *
   * @return The message
   */
  public String clientFirstMessage() {
    return setAndReturnClientFirstMessage(new ClientFirstMessage(user, nonce));
  }

  /**
   * Process a received server-first-message.
   * Generate by calling {@link #receiveServerFirstMessage(String)}.
   */
  public class ServerFirstProcessor {
    private final ServerFirstMessage serverFirstMessage;

    private ServerFirstProcessor(String receivedServerFirstMessage) throws ScramParseException {
      serverFirstMessageString = receivedServerFirstMessage;
      serverFirstMessage = ServerFirstMessage.parseFrom(receivedServerFirstMessage, nonce);
    }

    public String getSalt() {
      return serverFirstMessage.getSalt();
    }

    public int getIteration() {
      return serverFirstMessage.getIteration();
    }

    /**
     * Generates a {@link ClientFinalProcessor}, that allows to generate the client-final-message and also
     * receive and parse the server-first-message. It is based on the user's password.
     *
     * @param password The user's password
     * @return The handler
     * @throws IllegalArgumentException If the message is null or empty
     */
    public ClientFinalProcessor clientFinalProcessor(String password) throws IllegalArgumentException {
      String algName = scramMechanism.getName();
      if ("SCRAM-GM-256".equalsIgnoreCase(algName)) {
        byte[] saltedPassword = CalSaltedPasswordGM(password.getBytes(), getSalt(), getIteration());
//        LOGGER.log(Level.FINE, "saltedPassword:{0}", String.valueOf(Hex.encodeHex(saltedPassword)));
//        LOGGER.log(Level.FINE, "saltedPassword:{0}", new String(Base64.getEncoder().encode(saltedPassword)));

        byte[] clientKey = CalClientKeyGM(saltedPassword);
//        LOGGER.log(Level.FINE, "clientKey:{0}", String.valueOf(Hex.encodeHex(clientKey)));
//        LOGGER.log(Level.FINE, "clientKey:{0}", new String(Base64.getEncoder().encode(clientKey)));

        byte[] storedKey = HeroSP.CalcuteBlockHash("herodb".getBytes(), clientKey);
//        LOGGER.log(Level.FINE, "storedKey:{0}", String.valueOf(Hex.encodeHex(storedKey)));
//        LOGGER.log(Level.FINE, "storedKey:{0}", new String(Base64.getEncoder().encode(storedKey)));

        byte[] serverKey = CalServerKeyGM(saltedPassword);
//        LOGGER.log(Level.FINE, "serverKey:{0}", new String(Base64.getEncoder().encode(serverKey)));

        return new ClientFinalProcessor(
          serverFirstMessage.getNonce(),
          clientKey,
          storedKey,
          serverKey
        );
      } else {
        return new ClientFinalProcessor(
          serverFirstMessage.getNonce(),
          checkNotEmpty(password, "password"),
          getSalt(),
          getIteration()
        );
      }
    }

    /**
     * Generates a {@link ClientFinalProcessor}, that allows to generate the client-final-message and also
     * receive and parse the server-first-message. It is based on the clientKey and storedKey,
     * which, if available, provide an optimized path versus providing the original user's password.
     *
     * @param clientKey The client key, as per the SCRAM algorithm.
     *                  It can be generated with:
     *                  {@link ScramFunctions#clientKey(ScramMechanism, StringPreparation, String, byte[], int)}
     * @param storedKey The stored key, as per the SCRAM algorithm.
     *                  It can be generated from the client key with:
     *                  {@link ScramFunctions#storedKey(ScramMechanism, byte[])}
     * @return The handler
     * @throws IllegalArgumentException If the message is null or empty
     */
    public ClientFinalProcessor clientFinalProcessor(byte[] clientKey, byte[] storedKey)
      throws IllegalArgumentException {
      return new ClientFinalProcessor(
        serverFirstMessage.getNonce(),
        checkNotNull(clientKey, "clientKey"),
        checkNotNull(storedKey, "storedKey")
      );
    }
  }

  public static byte[] CalServerKeyGM(byte[] saltedPassword) {
    final byte[] SERVER_KEY_HMAC_KEY = "Server Key".getBytes(StandardCharsets.UTF_8);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      baos.write(saltedPassword);
      baos.write(SERVER_KEY_HMAC_KEY);

      return HeroSP.CalcuteBlockHash("herodb".getBytes(), baos.toByteArray());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "出错：" + e.getMessage(), e);
      throw new RuntimeException("用国密杂凑计算ServerKey出错：" + e.getMessage(), e);
    }
  }

  public static byte[] CalClientKeyGM(byte[] saltedPassword) {
    final byte[] CLIENT_KEY_HMAC_KEY = "Client Key".getBytes(StandardCharsets.UTF_8);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      baos.write(saltedPassword);
      baos.write(CLIENT_KEY_HMAC_KEY);

      return HeroSP.CalcuteBlockHash("herodb".getBytes(), baos.toByteArray());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "出错：" + e.getMessage(), e);
      throw new RuntimeException("用国密杂凑计算ClientKey出错：" + e.getMessage(), e);
    }
  }

  public static byte[] CalSaltedPasswordGM(byte[] password, String salt, int iterCount) {
    final byte[] keyBytes = "herodb".getBytes();
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      baos.write(password);
      byte[] saltBytes = Base64.getDecoder().decode(salt);
//      LOGGER.log(Level.FINE, "salt bytes: {0}", String.valueOf(Hex.encodeHex(saltBytes)));
      baos.write(saltBytes);
      baos.write(new byte[]{0x00, 0x00, 0x00, 0x01});

      byte[] step1PlainBytes = baos.toByteArray();
//      LOGGER.log(Level.FINE, "第一次迭代前的明文：{0}，字节数{1}", new Object[]{String.valueOf(Hex.encodeHex(step1PlainBytes)), step1PlainBytes.length});
//      LOGGER.log(Level.FINE, "第一次迭代前的明文：{0}", new String(Base64.getEncoder().encode(step1PlainBytes)));

      byte[] step1Result = HeroSP.CalcuteBlockHash(keyBytes, step1PlainBytes);
//      LOGGER.log(Level.FINE, "第一次迭代结果：{0}", String.valueOf(Hex.encodeHex(step1Result)));
//      LOGGER.log(Level.FINE, "第一次迭代结果：{0}", new String(Base64.getEncoder().encode(step1Result)));
      byte[] stepXResult = step1Result;

      for (int i = 2; i <= iterCount; i++) {
        baos.reset();
        baos.write(password);
        baos.write(stepXResult);
        byte[] plainX = baos.toByteArray();
        stepXResult = HeroSP.CalcuteBlockHash(keyBytes, plainX);
        for (int ii = 0; ii < step1Result.length; ii++) {
          step1Result[ii] ^= stepXResult[ii];
        }
//        LOGGER.log(Level.FINE, "X轮迭代：明文：{0}，结果：{1}", new Object[]{String.valueOf(Hex.encodeHex(plainX)), String.valueOf(Hex.encodeHex(stepXResult))});
      }
//      LOGGER.log(Level.FINE, "最后一轮迭代结果：{0}", String.valueOf(Hex.encodeHex(step1Result)));
//      LOGGER.log(Level.FINE, "最后一轮迭代结果：{0}", new String(Base64.getEncoder().encode(step1Result)));
      return step1Result;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "计算saltedpassword(gm)出错：" + e.getMessage(), e);
      throw new RuntimeException("计算saltedpassword出错(gm)：" + e.getMessage(), e);
    }
  }

  /**
   * Processor that allows to generate the client-final-message,
   * as well as process the server-final-message and verify server's signature.
   * Generate the processor by calling either {@link ServerFirstProcessor#clientFinalProcessor(String)}
   * or {@link ServerFirstProcessor#clientFinalProcessor(byte[], byte[])}.
   */
  public class ClientFinalProcessor {
    private Logger LOGGER = Logger.getLogger(ClientFinalProcessor.class.getName());

    private final String nonce;
    private final byte[] clientKey;
    private final byte[] storedKey;
    private final byte[] serverKey;
    private String authMessage;

    private ClientFinalProcessor(String nonce, byte[] clientKey, byte[] storedKey, byte[] serverKey) {
      assert null != clientKey : "clientKey";
      assert null != storedKey : "storedKey";
      assert null != serverKey : "serverKey";

      this.nonce = nonce;
      this.clientKey = clientKey;
      this.storedKey = storedKey;
      this.serverKey = serverKey;
    }

    private ClientFinalProcessor(String nonce, byte[] clientKey, byte[] serverKey) {
      this(nonce, clientKey, ScramFunctions.storedKey(scramMechanism, clientKey), serverKey);
    }

    private ClientFinalProcessor(String nonce, byte[] saltedPassword) {
      this(
        nonce,
        ScramFunctions.clientKey(scramMechanism, saltedPassword),
        ScramFunctions.serverKey(scramMechanism, saltedPassword)
      );
    }

    private ClientFinalProcessor(String nonce, String password, String salt, int iteration) {
      this(
        nonce,
        ScramFunctions.saltedPassword(
          scramMechanism, stringPreparation, password, Base64.getDecoder().decode(salt), iteration
        )
      );
    }

    private synchronized void generateAndCacheAuthMessage(Optional<byte[]> cbindData) {
      if (null != authMessage) {
        return;
      }

      authMessage = clientFirstMessage.writeToWithoutGs2Header(new StringBuffer())
        .append(",")
        .append(serverFirstMessageString)
        .append(",")
        .append(ClientFinalMessage.writeToWithoutProof(clientFirstMessage.getGs2Header(), cbindData, nonce))
        .toString();
    }

    private String clientFinalMessage(Optional<byte[]> cbindData) {
      if (null == authMessage) {
        generateAndCacheAuthMessage(cbindData);
      }

      byte[] clientSignature = new byte[0];
      if ("scram-gm-256".equalsIgnoreCase(scramMechanism.getName())) {
//        LOGGER.log(Level.FINE, "authMessage:[{0}]", authMessage);
        byte[] auth = authMessage.getBytes(StandardCharsets.UTF_8);
//        LOGGER.log(Level.FINE, "authMessage:[{0}]", new String(Base64.getEncoder().encode(auth)));
//        LOGGER.log(Level.FINE, "authMessage:[{0}]", String.valueOf(Hex.encodeHex(auth)));
        try {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          baos.write(storedKey);
          baos.write(auth);
          clientSignature = HeroSP.CalcuteBlockHash("herodb".getBytes(), baos.toByteArray());
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "国密杂凑计算签名出错：" + e.getMessage(), e);
          throw new RuntimeException("国密杂凑计算签名出错：" + e.getMessage(), e);
        }
      } else {
        clientSignature = ScramFunctions.clientSignature(scramMechanism, storedKey, authMessage);
      }
//      LOGGER.log(Level.FINE, "clientSignature:[{0}]", new String(Base64.getEncoder().encode(clientSignature)));
//      LOGGER.log(Level.FINE, "clientSignature:[{0}]", String.valueOf(Hex.encodeHex(clientSignature)));
      ClientFinalMessage clientFinalMessage = new ClientFinalMessage(
        clientFirstMessage.getGs2Header(),
        cbindData,
        nonce,
        ScramFunctions.clientProof(
          clientKey,
          clientSignature
        )
      );

      return clientFinalMessage.toString();
    }

    /**
     * Generates the SCRAM representation of the client-final-message, including the given channel-binding data.
     *
     * @param cbindData The bytes of the channel-binding data
     * @return The message
     * @throws IllegalArgumentException If the channel binding data is null
     */
    public String clientFinalMessage(byte[] cbindData) throws IllegalArgumentException {
      return clientFinalMessage(Optional.of(checkNotNull(cbindData, "cbindData")));
    }

    /**
     * Generates the SCRAM representation of the client-final-message.
     *
     * @return The message
     */
    public String clientFinalMessage() {
      return clientFinalMessage(Optional.empty());
    }

    /**
     * Receive and process the server-final-message.
     * Server SCRAM signatures is verified.
     *
     * @param serverFinalMessage The received server-final-message
     * @throws ScramParseException       If the message is not a valid server-final-message
     * @throws ScramServerErrorException If the server-final-message contained an error
     * @throws IllegalArgumentException  If the message is null or empty
     */
    public void receiveServerFinalMessage(String serverFinalMessage)
      throws ScramParseException, ScramServerErrorException, ScramInvalidServerSignatureException,
      IllegalArgumentException {
      checkNotEmpty(serverFinalMessage, "serverFinalMessage");

      ServerFinalMessage message = ServerFinalMessage.parseFrom(serverFinalMessage);
      if (message.isError()) {
        throw new ScramServerErrorException(message.getError().get());
      }

      byte[] serverSignature = message.getVerifier().get();
      boolean verifyServerSignature = false;
      if ("scram-gm-256".equalsIgnoreCase(scramMechanism.getName())) {
//        LOGGER.log(Level.FINE, "服务端签名：{0}", String.valueOf(Hex.encodeHex(serverSignature)));
//        LOGGER.log(Level.FINE, "服务端签名：{0}", new String(Base64.getEncoder().encode(serverSignature)));
        try {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          baos.write(serverKey);
          baos.write(authMessage.getBytes(StandardCharsets.UTF_8));
          byte[] serverSignature_c = HeroSP.CalcuteBlockHash("herodb".getBytes(), baos.toByteArray());
          if (serverSignature.length == serverSignature_c.length) {
            int i = 0;
            for (; i < serverSignature.length; i++) {
              if (serverSignature[i] != serverSignature_c[i]) {
                break;
              }
            }
            verifyServerSignature = (i >= serverSignature.length);
          }
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "国密杂凑计算服务端签名出错：" + e.getMessage(), e);
          throw new RuntimeException("国密杂凑计算服务端签名出错：" + e.getMessage(), e);
        }
      } else {
        verifyServerSignature = ScramFunctions.verifyServerSignature(
          scramMechanism, serverKey, authMessage, serverSignature
        );
      }
      if (!verifyServerSignature) {
        throw new ScramInvalidServerSignatureException("Invalid server SCRAM signature");
      }
    }
  }

  /**
   * Constructs a handler for the server-first-message, from its String representation.
   *
   * @param serverFirstMessage The message
   * @return The handler
   * @throws ScramParseException      If the message is not a valid server-first-message
   * @throws IllegalArgumentException If the message is null or empty
   */
  public ServerFirstProcessor receiveServerFirstMessage(String serverFirstMessage)
    throws ScramParseException, IllegalArgumentException {
    return new ServerFirstProcessor(checkNotEmpty(serverFirstMessage, "serverFirstMessage"));
  }
}
