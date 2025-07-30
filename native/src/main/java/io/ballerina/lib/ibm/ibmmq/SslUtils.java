/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.lib.ibm.ibmmq;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.crypto.nativeimpl.Decode;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static io.ballerina.lib.ibm.ibmmq.Constants.CERT;
import static io.ballerina.lib.ibm.ibmmq.Constants.CERT_FILE;
import static io.ballerina.lib.ibm.ibmmq.Constants.CRYPTO_TRUSTSTORE_PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.CRYPTO_TRUSTSTORE_PATH;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_FILE;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_STORE_PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_STORE_PATH;
import static io.ballerina.lib.ibm.ibmmq.Constants.NATIVE_DATA_PRIVATE_KEY;
import static io.ballerina.lib.ibm.ibmmq.Constants.NATIVE_DATA_PUBLIC_KEY_CERTIFICATE;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_0;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_0_CIPHER_SPEC;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_2;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_3;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_3_CIPHER_SPEC;

/**
 * {@code CommonUtils} contains the common utility functions for the Ballerina IBM MQ connector SSL support.
 *
 * @since 1.3.0.
 */
public final class SslUtils {
    private SslUtils() {
    }

    public static String getSslProtocol(String cipherSuite) {
        if (Objects.isNull(cipherSuite) || cipherSuite.isBlank()) {
            return TLS_V_1_2;
        }
        if (TLS_V_1_0_CIPHER_SPEC.contains(cipherSuite)) {
            return TLS_V_1_0;
        }
        if (TLS_V_1_3_CIPHER_SPEC.contains(cipherSuite)) {
            return TLS_V_1_3;
        }
        return TLS_V_1_2;
    }

    @SuppressWarnings("unchecked")
    public static SSLSocketFactory getSecureSocketFactory(String protocol, BMap<BString, Object> secureSocket)
            throws Exception {
        Object bCert = secureSocket.get(CERT);
        BMap<BString, BString> keyRecord = (BMap<BString, BString>) secureSocket.getMapValue(KEY);
        KeyManagerFactory kmf = null;
        TrustManagerFactory tmf;
        if (bCert instanceof BString cert) {
            tmf = getTrustManagerFactory(cert);
        } else {
            BMap<BString, BString> trustStore = (BMap<BString, BString>) bCert;
            tmf = getTrustManagerFactory(trustStore);
        }
        if (Objects.nonNull(keyRecord)) {
            if (keyRecord.containsKey(CERT_FILE)) {
                BString certFile = keyRecord.get(CERT_FILE);
                BString keyFile = keyRecord.get(KEY_FILE);
                BString keyPassword = keyRecord.getStringValue(KEY_PASSWORD);
                kmf = getKeyManagerFactory(certFile, keyFile, keyPassword);
            } else {
                kmf = getKeyManagerFactory(keyRecord);
            }
        }
        SSLContext sslContext = SSLContext.getInstance(protocol);
        if (Objects.nonNull(kmf)) {
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } else {
            sslContext.init(null, tmf.getTrustManagers(), null);
        }
        return sslContext.getSocketFactory();
    }

    @SuppressWarnings("unchecked")
    private static TrustManagerFactory getTrustManagerFactory(BString cert) throws Exception {
        Object publicKeyMap = Decode.decodeRsaPublicKeyFromCertFile(cert);
        if (publicKeyMap instanceof BMap) {
            X509Certificate x509Certificate = (X509Certificate) ((BMap<BString, Object>) publicKeyMap)
                    .getNativeData(NATIVE_DATA_PUBLIC_KEY_CERTIFICATE);
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(null, "".toCharArray());
            ts.setCertificateEntry(UUID.randomUUID().toString(), x509Certificate);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            return tmf;
        } else {
            throw new Exception("Failed to get the public key from Crypto API. " +
                    ((BError) publicKeyMap).getErrorMessage().getValue());
        }
    }

    private static TrustManagerFactory getTrustManagerFactory(BMap<BString, BString> trustStore) throws Exception {
        BString trustStorePath = trustStore.getStringValue(CRYPTO_TRUSTSTORE_PATH);
        BString trustStorePassword = trustStore.getStringValue(CRYPTO_TRUSTSTORE_PASSWORD);
        KeyStore ts = getKeyStore(trustStorePath, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        return tmf;
    }

    private static KeyManagerFactory getKeyManagerFactory(BMap<BString, BString> keyStore) throws Exception {
        BString keyStorePath = keyStore.getStringValue(KEY_STORE_PATH);
        BString keyStorePassword = keyStore.getStringValue(KEY_STORE_PASSWORD);
        KeyStore ks = getKeyStore(keyStorePath, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword.getValue().toCharArray());
        return kmf;
    }

    @SuppressWarnings("unchecked")
    private static KeyManagerFactory getKeyManagerFactory(BString certFile, BString keyFile, BString keyPassword)
            throws Exception {
        Object publicKey = Decode.decodeRsaPublicKeyFromCertFile(certFile);
        if (publicKey instanceof BMap) {
            X509Certificate publicCert = (X509Certificate) ((BMap<BString, Object>) publicKey).getNativeData(
                    NATIVE_DATA_PUBLIC_KEY_CERTIFICATE);
            Object privateKeyMap = Decode.decodeRsaPrivateKeyFromKeyFile(keyFile, keyPassword);
            if (privateKeyMap instanceof BMap) {
                PrivateKey privateKey = (PrivateKey) ((BMap<BString, Object>) privateKeyMap).getNativeData(
                        NATIVE_DATA_PRIVATE_KEY);
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, "".toCharArray());
                ks.setKeyEntry(UUID.randomUUID().toString(), privateKey, "".toCharArray(),
                        new X509Certificate[]{publicCert});
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, "".toCharArray());
                return kmf;
            } else {
                throw new Exception("Failed to get the private key from Crypto API. " +
                        ((BError) privateKeyMap).getErrorMessage().getValue());
            }
        } else {
            throw new Exception("Failed to get the public key from Crypto API. " +
                    ((BError) publicKey).getErrorMessage().getValue());
        }
    }

    private static KeyStore getKeyStore(BString path, BString password) throws Exception {
        try (FileInputStream is = new FileInputStream(path.getValue())) {
            char[] passphrase = password.getValue().toCharArray();
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, passphrase);
            return ks;
        }
    }
}
