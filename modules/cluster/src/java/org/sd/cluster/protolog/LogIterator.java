/*
    Copyright 2011 Semantic Discovery, Inc. (www.semanticdiscovery.com)

    This file is part of the Semantic Discovery Toolkit.

    The Semantic Discovery Toolkit is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The Semantic Discovery Toolkit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The Semantic Discovery Toolkit.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.sd.cluster.protolog;


import com.google.protobuf.Message;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;


/**
 * Iterator over messages contained within a protobuf log file.
 * <p>
 * @author Spence Koehler
 */
public class LogIterator implements Iterator<Message> {

  private DataInputStream logInputStream;
  private Method parseMethod;
  private Message nextMessage;
  private ProtoLogStreamer streamer;
  private int maxMessageBytes;

  // /**
  //  * Construct with the given input stream for iterating over a log file
  //  * containing instances of the given protobuf message class.
  //  *
  //  * @param logInputStream  The input stream over the log from which to read
  //  *                        messages.
  //  * @param messageClass  The protobuf class of the messages in the log.
  //  */
  // public LogIterator(InputStream logInputStream, Class<? extends Message> messageClass) {
  //   this(new DataInputStream(logInputStream), messageClass, ProtoLogStreamer.DEFAULT_INSTANCE, ProtoLogStreamer.MAX_MESSAGE_BYTES);
  // }

  /**
   * Construct with the given input stream for iterating over a log file
   * containing instances of the given protobuf message class.
   *
   * @param logInputStream  The input stream over the log from which to read
   *                        messages.
   * @param messageClass  The protobuf class of the messages in the log.
   * @param streamer  The ProtoLogStreamer to use to deserialize messages.
   * @param maxMessageBytes  The maximum number of message bytes used while serializing.
   */
  public LogIterator(DataInputStream logInputStream, Class<? extends Message> messageClass, ProtoLogStreamer streamer, int maxMessageBytes) {
    this.logInputStream = logInputStream;
    this.parseMethod = ProtoLogUtil.getParseMethod(messageClass);
    this.streamer = streamer;
    this.maxMessageBytes = maxMessageBytes;

    if (parseMethod == null) {
      throw new IllegalArgumentException("Can't find 'parseFrom' method from '" + messageClass + "'!");
    }

    this.nextMessage = readNextMessage();
  }

  /**
   * Determine whether there is another message to retrieve.
   *
   * @return true if there is another message; otherwise, false.
   */
  public boolean hasNext() {
    return (nextMessage != null);
  }

  /**
   * Get the next message from the stream.
   *
   * @return the next message or null if the end has been reached.
   */
  public Message next() {
    Message result = nextMessage;
    this.nextMessage = readNextMessage();
    return result;
  }

  /**
   * Throws an UnsupportedOperationException.
   */
  public void remove() {
    throw new UnsupportedOperationException("Not supported.");
  }

  /**
   * Close this instance's underlying stream.
   */
  public void close() throws IOException {
    if (logInputStream != null) {
      logInputStream.close();
    }
  }

  /**
   * Do the work of reading the next message.
   *
   * @return the next message or null.
   */
  private final Message readNextMessage() {
    Message result = null;

    try {
      result = streamer.readMessageFrom(logInputStream, parseMethod, maxMessageBytes);
    }
    catch (EOFException e) {  // hit end of file
      result = null;
    }
    catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return result;
  }


  /**
   * Auxiliary to iterate over the messages in a log file, printing them to
   * stdout.
   * <ul>
   * <li>args[0] holds the classpath for the message instances in the log
   *     (i.e. 'mypackage.MyProtoWrapper$MyMessageType')</li>
   * <li>args[1+] hold the paths to log files to dump.</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  public static final void main(String[] args) throws Exception {
    // arg0: protobuf class
    // args1+: proto log files

    final Class<? extends Message> messageClass = (Class<? extends Message>)Class.forName(args[0]);
    for (int i = 1; i < args.length; ++i) {
      final LogCat logCat = new LogCat(new MultiLogIterator(new File(args[i]), messageClass, ProtoLogStreamer.DEFAULT_INSTANCE, ProtoLogStreamer.MAX_MESSAGE_BYTES), System.out) {
          protected void catMessage(PrintStream out, Message message, int messageNum) {
            out.println(messageNum + ": " + message);
          }
        };

      logCat.doCat();
    }
  }
}

