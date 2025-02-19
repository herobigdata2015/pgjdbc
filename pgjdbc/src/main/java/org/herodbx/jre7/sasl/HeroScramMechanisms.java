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


import org.herodbx.scram100b2.common.ScramMechanism;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.herodbx.scram100b2.common.util.Preconditions.checkNotNull;
import static org.herodbx.scram100b2.common.util.Preconditions.gt0;


/**
 * SCRAM Mechanisms supported by this library.
 * At least, SCRAM-SHA-1 and SCRAM-SHA-256 are provided, since both the hash and the HMAC implementations
 * are provided by the Java JDK version 6 or greater.
 * <p>
 * {@link MessageDigest}: "Every implementation of the Java platform is required to support the
 * following standard MessageDigest algorithms: MD5, SHA-1, SHA-256".
 * <p>
 * {@link Mac}: "Every implementation of the Java platform is required to support the following
 * standard Mac algorithms: HmacMD5, HmacSHA1, HmacSHA256".
 *
 * @see <a href="https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml#scram">
 * SASL SCRAM Family Mechanisms</a>
 */
public enum HeroScramMechanisms implements ScramMechanism {
  SCRAM_SHA_1("SHA-1", "SHA-1", 160, "HmacSHA1", false, 1),
  SCRAM_SHA_1_PLUS("SHA-1", "SHA-1", 160, "HmacSHA1", true, 1),
  SCRAM_SHA_256("SHA-256", "SHA-256", 256, "HmacSHA256", false, 10),
  SCRAM_SHA_256_PLUS("SHA-256", "SHA-256", 256, "HmacSHA256", true, 10),
  SCRAM_GM_256("GM-256", "GM-256", 256, "HmacGM256", false, 10);

  static private Logger LOGGER = Logger.getLogger(HeroScramMechanisms.class.getName());

  private static final String SCRAM_MECHANISM_NAME_PREFIX = "SCRAM-";
  private static final String CHANNEL_BINDING_SUFFIX = "-PLUS";
  private static final String PBKDF2_PREFIX_ALGORITHM_NAME = "PBKDF2With";
  private static final Map<String, HeroScramMechanisms> BY_NAME_MAPPING =
    Arrays.stream(values()).collect(Collectors.toMap(v -> v.getName(), v -> v));

  private final String mechanismName;
  private final String hashAlgorithmName;
  private final int keyLength;
  private final String hmacAlgorithmName;
  private final boolean channelBinding;
  private final int priority;

  HeroScramMechanisms(
    String name, String hashAlgorithmName, int keyLength, String hmacAlgorithmName, boolean channelBinding,
    int priority
  ) {
    this.mechanismName = SCRAM_MECHANISM_NAME_PREFIX
      + checkNotNull(name, "name")
      + (channelBinding ? CHANNEL_BINDING_SUFFIX : "")
    ;
    this.hashAlgorithmName = checkNotNull(hashAlgorithmName, "hashAlgorithmName");
    this.keyLength = gt0(keyLength, "keyLength");
    this.hmacAlgorithmName = checkNotNull(hmacAlgorithmName, "hmacAlgorithmName");
    this.channelBinding = channelBinding;
    this.priority = gt0(priority, "priority");
  }

  /**
   * Method that returns the name of the hash algorithm.
   * It is protected since should be of no interest for direct users.
   * The instance is supposed to provide abstractions over the algorithm names,
   * and are not meant to be directly exposed.
   *
   * @return The name of the hash algorithm
   */
  protected String getHashAlgorithmName() {
    return hashAlgorithmName;
  }

  /**
   * Method that returns the name of the HMAC algorithm.
   * It is protected since should be of no interest for direct users.
   * The instance is supposed to provide abstractions over the algorithm names,
   * and are not meant to be directly exposed.
   *
   * @return The name of the HMAC algorithm
   */
  protected String getHmacAlgorithmName() {
    return hmacAlgorithmName;
  }

  @Override
  public String getName() {
    return mechanismName;
  }

  @Override
  public boolean supportsChannelBinding() {
    return channelBinding;
  }

  @Override
  public MessageDigest getMessageDigestInstance() {
    try {
      return MessageDigest.getInstance(hashAlgorithmName);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Algorithm " + hashAlgorithmName + " not present in current JVM");
    }
  }

  @Override
  public Mac getMacInstance() {
    try {
      return Mac.getInstance(hmacAlgorithmName);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MAC Algorithm " + hmacAlgorithmName + " not present in current JVM");
    }
  }

  @Override
  public SecretKeySpec secretKeySpec(byte[] key) {
    return new SecretKeySpec(key, hmacAlgorithmName);
  }

  @Override
  public SecretKeyFactory secretKeyFactory() {
    try {
      return SecretKeyFactory.getInstance(PBKDF2_PREFIX_ALGORITHM_NAME + hmacAlgorithmName);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Unsupported PBKDF2 for " + mechanismName);
    }
  }

  @Override
  public int algorithmKeyLength() {
    return keyLength;
  }

  /**
   * Gets a SCRAM mechanism, given its standard IANA name.
   *
   * @param name The standard IANA full name of the mechanism.
   * @return An Optional instance that contains the ScramMechanism if it was found, or empty otherwise.
   */
  public static Optional<HeroScramMechanisms> byName(String name) {
    checkNotNull(name, "name");

    return Optional.ofNullable(BY_NAME_MAPPING.get(name));
  }

  /**
   * This class classifies SCRAM mechanisms by two properties: whether they support channel binding;
   * and a priority, which is higher for safer algorithms (like SHA-256 vs SHA-1).
   * <p>
   * Given a list of SCRAM mechanisms supported by the peer, pick one that matches the channel binding requirements
   * and has the highest priority.
   *
   * @param channelBinding The type of matching mechanism searched for
   * @param peerMechanisms The mechanisms supported by the other peer
   * @return The selected mechanism, or null if no mechanism matched
   */
  public static Optional<ScramMechanism> selectMatchingMechanism(boolean channelBinding, String... peerMechanisms) {
    return Arrays.stream(peerMechanisms)
      .map(s -> BY_NAME_MAPPING.get(s))
      .filter(m -> m != null)             // Filter out invalid names
      .flatMap(m -> Arrays.stream(values())
        .filter(
          v -> channelBinding == v.channelBinding && v.mechanismName.equals(m.mechanismName)
        )
      )
      .max(Comparator.comparing(c -> c.priority))
      .map(m -> (ScramMechanism) m)
      ;
  }
}
