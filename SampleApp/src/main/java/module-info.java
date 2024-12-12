module SampleApp {
    requires org.slf4j;
    requires WebServer;
    requires SSRSupport;
    requires org.bouncycastle.provider;
    requires com.fasterxml.jackson.databind;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires static lombok;
    requires java.xml;

    exports com.simonebasile.sampleapp.dto to org.mongodb.bson, com.fasterxml.jackson.databind;
    exports com.simonebasile.sampleapp.model to org.mongodb.bson, com.fasterxml.jackson.databind;
}
