package com.simonebasile.http;

import java.io.IOException;

/**
 * This interface is used to write messages to a websocket.
 */
public interface WebsocketWriter {
    /**
     * Sends a text message to the client.
     *
     * @param s the message to send
     * @throws IOException if an I/O error occurs
     */
    void sendText(String s) throws IOException;

    /**
     * Sends a text message to the client.
     *
     * @param bytes the message to send in binary format
     * @throws IOException if an I/O error occurs
     */
    void sendTextBytes(byte[] bytes) throws IOException;

    /**
     * Sends a binary message to the client.
     *
     * @param bytes the message to send
     * @throws IOException if an I/O error occurs
     */
    void sendBytes(byte[] bytes) throws IOException;

    /**
     * sends a close message to the client.
     *
     * @throws IOException if an I/O error occurs
     */
    void sendClose() throws IOException;
}
