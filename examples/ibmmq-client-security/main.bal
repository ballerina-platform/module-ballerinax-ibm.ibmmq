import ballerinax/ibm.ibmmq;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1",
        host = "localhost",
        port = 1415,
        channel = "DEV.APP.SVRCONN",
        // Provide the relevant SSL cipher-suite.
        sslCipherSuite = ibmmq:TLS12ORHIGHER,
        secureSocket = {
            // Provide the client truststrore here.
            cert: {
                path: "./resources/clientTrustStore.p12",
                password: "password"
            },
            // Provide the client keystore here.
            'key: {
                path: "./resources/clientKeyStore.p12",
                password: "password"
            }
        }
    );
    ibmmq:Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", ibmmq:MQOO_OUTPUT);
    check producer->put({
        payload: "This is a sample message to IBM MQ queue".toBytes()
    });
    check producer->close();
    check queueManager.disconnect();
}
