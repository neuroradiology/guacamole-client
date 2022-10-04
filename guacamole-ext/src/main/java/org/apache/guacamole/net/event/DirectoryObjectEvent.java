/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.net.event;

import org.apache.guacamole.net.auth.Directory;
import org.apache.guacamole.net.auth.Identifiable;

/**
 * Abstract basis for events which affect or directly relate to objects that
 * may be stored within a {@link Directory}.
 *
 * @param <ObjectType>
 *     The type of object stored within the {@link Directory}.
 */
public interface DirectoryObjectEvent<ObjectType extends Identifiable>
        extends AuthenticationProviderEvent, UserEvent {

    /**
     * Returns the type of {@link Directory} that contains the object affected
     * by the operation.
     *
     * @return
     *     The type of objects stored within the {@link Directory}.
     */
    Directory.Type getDirectoryType();

    /**
     * Returns the identifier of the object affected by the operation.
     *
     * @return
     *     The identifier of the object affected by the operation.
     */
    String getObjectIdentifier();

    /**
     * Returns the object affected by the operation, if available. Whether the
     * affected object is available is context- and implementation-dependent.
     * There is no general guarantee across all implementations of this event
     * that the affected object will be available. If the object is not
     * available, null is returned.
     *
     * @return
     *     The object affected by the operation performed, or null if that
     *     object is not available in the context of this event.
     */
    ObjectType getObject();

}
