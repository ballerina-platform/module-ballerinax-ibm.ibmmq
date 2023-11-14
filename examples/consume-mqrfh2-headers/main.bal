import ballerinax/ibm.ibmmq;
import ballerina/io;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN"
    );
    ibmmq:Queue consumer = check queueManager.accessQueue(
            "DEV.QUEUE.1", ibmmq:MQOO_INPUT_AS_Q_DEF);
    while true {
        ibmmq:Message? message = check consumer->get(options = ibmmq:MQGMO_WAIT);
        if message is () {
            continue;
        }
        io:println(string:fromBytes(message.payload));
        ibmmq:Header[]? headers = message.headers;
        if headers is () {
            continue;
        }
        ibmmq:Header header = headers[0];
        if header is ibmmq:MQRFH2 {
            table<ibmmq:MQRFH2Field> key(folder, 'field) fieldTable = header.fieldValues;
            if fieldTable.hasKey(["mcd", "Msd"]) {
                io:println(fieldTable.get(["mcd", "Msd"]));
            }
        }
    }
}
