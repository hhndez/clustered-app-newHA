package com.solacesystems.poc.model;

import com.solacesystems.solclientj.core.handle.MessageHandle;

import java.nio.ByteBuffer;

/**
 * Used by the ClusterConnector to serialize/deserialize application messages. Decoupling this from the
 * ClusterConnector and ClusterModel allows different application instances to define their own messages
 * for application input and application state output.
 *
 * @param <InputType> -- input message type
 * @param <OutputType>-- output message type
 */
public interface ClusteredAppSerializer<InputType, OutputType> {

    InputType DeserializeInput(MessageHandle msg);

    OutputType DeserializeOutput(MessageHandle msg);

    ByteBuffer SerializeOutput(OutputType output);
}
