/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.pce.pceservice;

import org.onosproject.net.resource.ResourceConsumer;

import com.google.common.annotations.Beta;
import org.onosproject.net.resource.ResourceConsumerId;

/**
 * Tunnel resource consumer identifier suitable to be used as a consumer id for
 * resource allocations.
 */
@Beta
public final class TunnelConsumerId implements ResourceConsumer {

    private final long value;

    /**
     * Creates a tunnel resource consumer identifier from the specified long value.
     *
     * @param value long value to be used as tunnel resource consumer id
     * @return tunnel resource consumer identifier
     */
    public static TunnelConsumerId valueOf(long value) {
        return new TunnelConsumerId(value);
    }

    /**
     * Initializes object for serializer.
     */
    public TunnelConsumerId() {
        this.value = 0;
    }

    /**
     * Constructs the tunnel resource consumer id corresponding to a given long
     * value.
     *
     * @param value the underlying value in long representation of this tunnel
     *            resource consumer id
     */
    public TunnelConsumerId(long value) {
        this.value = value;
    }

    /**
     * Returns the tunnel resource consumer id value in long format.
     *
     * @return value the tunnel resource consumer id's long value
     */
    public long value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TunnelConsumerId)) {
            return false;
        }
        TunnelConsumerId that = (TunnelConsumerId) obj;
        return this.value == that.value;
    }

    @Override
    public String toString() {
        return "0x" + Long.toHexString(value);
    }

    @Override
    public ResourceConsumerId consumerId() {
        // TODO
        return null;
    }
}
