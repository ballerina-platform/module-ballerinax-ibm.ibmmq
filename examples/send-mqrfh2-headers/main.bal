import ballerinax/ibm.ibmmq;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN"
    );
    ibmmq:Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", ibmmq:MQOO_OUTPUT);

    ibmmq:MQRFH2 mqrfh2Header = {
        flags: 12,
        fieldValues: table [
            {folder: "mcd", 'field: "Msd", value: "TestMcdValue"},
            {folder: "jms", 'field: "Dlv", value: 134},
            {folder: "mqps", 'field: "Ret", value: true}
        ]
    };

    check producer->put({
        headers: [mqrfh2Header],
        payload: "This is a sample message to IBM MQ queue".toBytes()
    });
    check producer->close();
    check queueManager.disconnect();
}
