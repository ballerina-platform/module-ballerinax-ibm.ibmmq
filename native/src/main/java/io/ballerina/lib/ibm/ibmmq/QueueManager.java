/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.ibm.ibmmq;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQTopic;
import com.ibm.mq.constants.MQConstants;
import io.ballerina.lib.ibm.ibmmq.listener.ConnectionMap;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.crypto.nativeimpl.Decode;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.CommonUtils.getOptionalStringProperty;
import static io.ballerina.lib.ibm.ibmmq.Constants.BQUEUE;
import static io.ballerina.lib.ibm.ibmmq.Constants.BTOPIC;
import static io.ballerina.lib.ibm.ibmmq.Constants.CERT;
import static io.ballerina.lib.ibm.ibmmq.Constants.CERT_FILE;
import static io.ballerina.lib.ibm.ibmmq.Constants.CHANNEL;
import static io.ballerina.lib.ibm.ibmmq.Constants.CRYPTO_TRUSTSTORE_PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.CRYPTO_TRUSTSTORE_PATH;
import static io.ballerina.lib.ibm.ibmmq.Constants.HOST;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_FILE;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_STORE_PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.KEY_STORE_PATH;
import static io.ballerina.lib.ibm.ibmmq.Constants.NATIVE_DATA_PRIVATE_KEY;
import static io.ballerina.lib.ibm.ibmmq.Constants.NATIVE_DATA_PUBLIC_KEY_CERTIFICATE;
import static io.ballerina.lib.ibm.ibmmq.Constants.NATIVE_QUEUE_MANAGER;
import static io.ballerina.lib.ibm.ibmmq.Constants.PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.PORT;
import static io.ballerina.lib.ibm.ibmmq.Constants.QUEUE_MANAGER_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.SECURE_SOCKET;
import static io.ballerina.lib.ibm.ibmmq.Constants.SSL_CIPHER_SUITE;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_0;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_0_CIPHER_SPEC;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_2;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_3;
import static io.ballerina.lib.ibm.ibmmq.Constants.TLS_V_1_3_CIPHER_SPEC;
import static io.ballerina.lib.ibm.ibmmq.Constants.USER_ID;
import static io.ballerina.lib.ibm.ibmmq.listener.Listener.NATIVE_CONNECTION_MAP;

/**
 * Representation of {@link com.ibm.mq.MQQueueManager} with utility methods to invoke as inter-op functions.
 */
public class QueueManager {

    /**
     * Creates an IBM MQ queue manager with the provided configurations.
     *
     * @param queueManager   Ballerina queue-manager object
     * @param configurations IBM MQ connection configurations
     * @return A Ballerina `ibmmq:Error` if there are connection problems
     */
    public static Object init(BObject queueManager, BMap<BString, Object> configurations) {
        try {
            ConnectionMap connectionMap = new ConnectionMap(configurations);
            connectionMap.setupConnectionFactory();
            queueManager.addNativeData(NATIVE_CONNECTION_MAP, connectionMap);
            Hashtable<String, Object> connectionProperties = getConnectionProperties(configurations);
            String queueManagerName = configurations.getStringValue(QUEUE_MANAGER_NAME).getValue();
            MQQueueManager mqQueueManager = new MQQueueManager(queueManagerName, connectionProperties);
            queueManager.addNativeData(NATIVE_QUEUE_MANAGER, mqQueueManager);
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Error occurred while initializing the connection manager: %s",
                            e.getMessage()), e);
        } catch (Exception e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Unexpected error occurred while initializing the connection manager: %s",
                            e.getMessage()), e);
        }
        return null;
    }

    static Hashtable<String, Object> getConnectionProperties(BMap<BString, Object> configurations)
            throws Exception {
        Hashtable<String, Object> properties = new Hashtable<>();
        String host = configurations.getStringValue(HOST).getValue();
        properties.put(MQConstants.HOST_NAME_PROPERTY, host);
        Long port = configurations.getIntValue(PORT);
        properties.put(MQConstants.PORT_PROPERTY, port.intValue());
        String channel = configurations.getStringValue(CHANNEL).getValue();
        properties.put(MQConstants.CHANNEL_PROPERTY, channel);
        getOptionalStringProperty(configurations, USER_ID)
                .ifPresent(userId -> properties.put(MQConstants.USER_ID_PROPERTY, userId));
        getOptionalStringProperty(configurations, PASSWORD)
                .ifPresent(password -> properties.put(MQConstants.PASSWORD_PROPERTY, password));
        updateSSlConfig(properties, configurations);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static void updateSSlConfig(Hashtable<String, Object> properties,
                                        BMap<BString, Object> configurations) throws Exception {
        getOptionalStringProperty(configurations, SSL_CIPHER_SUITE)
                .ifPresent(sslCipherSuite -> properties.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY, sslCipherSuite));
        Object secureSocket = configurations.get(SECURE_SOCKET);
        if (Objects.nonNull(secureSocket)) {
            String sslProtocol = getSslProtocol(configurations);
            SSLSocketFactory sslSocketFactory = getSecureSocketFactory(
                    sslProtocol, (BMap<BString, Object>) secureSocket);
            properties.put(MQConstants.SSL_SOCKET_FACTORY_PROPERTY, sslSocketFactory);
        }
    }

    public static String getSslProtocol(BMap<BString, Object> configurations) {
        Optional<String> cipherSuiteOpt = getOptionalStringProperty(configurations, SSL_CIPHER_SUITE);
        if (cipherSuiteOpt.isEmpty()) {
            return TLS_V_1_2;
        }
        String cipherSuite = cipherSuiteOpt.get();
        return TLS_V_1_0_CIPHER_SPEC.contains(cipherSuite)
                ? TLS_V_1_0 : TLS_V_1_3_CIPHER_SPEC.contains(cipherSuite) ? TLS_V_1_3 : TLS_V_1_2;

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

    public static Object accessQueue(BObject queueManagerObject, BString queueName, Long options) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        try {
            MQQueue mqQueue = queueManager.accessQueue(queueName.getValue(), options.intValue());
            BObject bQueue = ValueCreator.createObjectValue(ModuleUtils.getModule(), BQUEUE);
            bQueue.addNativeData(Constants.NATIVE_QUEUE, mqQueue);
            return bQueue;
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Error occurred while accessing queue: %s", e.getMessage()), e);
        }
    }

    public static Object accessTopic(BObject queueManagerObject, BString topicName,
                                     BString topicString, Long openTopicOption, Long options) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        ConnectionMap connectionMap = (ConnectionMap) queueManagerObject.getNativeData(NATIVE_CONNECTION_MAP);
        try {
            MQTopic mqTopic = queueManager.accessTopic(topicName.getValue(), topicString.getValue(),
                    openTopicOption.intValue(), options.intValue());
            BObject bTopic = ValueCreator.createObjectValue(ModuleUtils.getModule(), BTOPIC);
            bTopic.addNativeData(NATIVE_CONNECTION_MAP, connectionMap);
            bTopic.addNativeData(Constants.NATIVE_TOPIC, mqTopic);

            return bTopic;
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Error occurred while accessing topic: %s", e.getMessage()), e);
        }
    }

    public static Object disconnect(BObject queueManagerObject) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        try {
            ConnectionMap connectionMap = (ConnectionMap) queueManagerObject.getNativeData(NATIVE_CONNECTION_MAP);
            if (connectionMap != null) {
                connectionMap.close();
            }
            queueManager.disconnect();
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Error occurred while disconnecting queue manager: %s", e.getMessage()), e);
        }
        return null;
    }
}
