/*
 * Copyright 2015 Open Networking Laboratory
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

package org.onosproject.netconf;

/**
 * Allows for providers interested in node events to be notified.
 */
public interface NetconfDeviceListener {

    /**
     * Notifies that the node was added.
     *
     * @param nodeId the node where the event occurred
     */
    void deviceAdded(NetconfDeviceInfo nodeId);

    /**
     * Notifies that the node was removed.
     *
     * @param nodeId the node where the event occurred
     */
    void deviceRemoved(NetconfDeviceInfo nodeId);
}