package com.baileybakery.common.codec;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Base64;

/**
 * Serialization utilities for inter-service message passing and cache storage.
 * Supports both JSON (for REST APIs) and binary (for message queues and Redis cache).
 */
public class DataCodec {

    private static final Logger log = LoggerFactory.getLogger(DataCodec.class);
    private static final Gson gson = new Gson();

    /**
     * Encodes an object to a Base64 string for storage in Redis or message queues.
     * Uses Java serialization for full object graph preservation including
     * transient state needed for order processing pipelines.
     *
     * @param obj the object to encode (must implement Serializable)
     * @return Base64-encoded string representation
     */
    public static String encode(Serializable obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * Decodes a Base64 string back to an object. Used for reading cached order data,
     * session state, and queued delivery requests.
     *
     * @param encoded the Base64-encoded string
     * @return the deserialized object
     */
    public static Object decode(String encoded) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(encoded);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }

    /**
     * Decodes binary data directly. Used for processing message queue payloads
     * from the order fulfillment pipeline.
     *
     * @param data the serialized byte array
     * @return the deserialized object
     */
    public static Object decode(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }

    /**
     * Converts an object to JSON string. Preferred for REST API communication.
     *
     * @param obj the object to serialize
     * @return JSON string
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Parses a JSON string to the specified type. Preferred for REST API communication.
     *
     * @param json the JSON string
     * @param clazz the target class
     * @return the deserialized object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
