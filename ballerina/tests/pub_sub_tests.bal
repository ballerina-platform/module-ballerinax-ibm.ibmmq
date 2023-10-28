import ballerina/test;

@test:Config {}
function basicPublisherSubscriberTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subTopic = check queueManager.accessTopic("", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    future<Message|Error> messageFuture = start getMessageFromTopic(subTopic);
    Topic pubTopic = check queueManager.accessTopic("", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check pubTopic->put({
        payload: "Hello World".toBytes()
    });
    Message message = check wait messageFuture;
    test:assertEquals(string:fromBytes(message.payload), "Hello World");
}


function getMessageFromTopic(Topic topic) returns Message|Error {
    return topic->get();
}