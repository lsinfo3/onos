/*
 * Copyright 2016 Open Networking Laboratory
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
package org.onosproject.pce.web;

import static javax.ws.rs.core.Response.Status.OK;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.LinkedList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.net.DeviceId;
import org.onosproject.net.intent.Constraint;
import org.onosproject.pce.pceservice.PcePath;
import org.onosproject.pce.pceservice.DefaultPcePath;
import org.onosproject.pce.pceservice.LspType;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Query and program pce path.
 */

@Path("path")
public class PcePathWebResource extends AbstractWebResource {

    private final Logger log = LoggerFactory.getLogger(PcePathWebResource.class);
    public static final String PCE_PATH_NOT_FOUND = "Path not found";
    public static final String PCE_PATH_ID_EXIST = "Path exists";
    public static final String PCE_PATH_ID_NOT_EXIST = "Path does not exist for the identifier";

    /**
     * Retrieve details of all paths created.
     *
     * @return 200 OK
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryAllPath() {
        log.debug("Query all paths.");
        //TODO: need to uncomment below line once queryAllPath method is added to PceService
        Iterable<Tunnel> tunnels = null; // = get(PceService.class).queryAllPath();
        ObjectNode result = mapper().createObjectNode();
        ArrayNode pathEntry = result.putArray("paths");
        if (tunnels != null) {
            for (final Tunnel tunnel : tunnels) {
                PcePath path = DefaultPcePath.builder().of(tunnel).build();
                pathEntry.add(codec(PcePath.class).encode(path, this));
            }
        }
        return ok(result.toString()).build();
    }

    /**
     * Retrieve details of a specified path id.
     *
     * @param id path id
     * @return 200 OK, 404 if given identifier does not exist
     */
    @GET
    @Path("{path_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryPath(@PathParam("path_id") String id) {
        log.debug("Query path by identifier {}.", id);
        //TODO: need to uncomment below lines once queryPath method is added to PceService
        Tunnel tunnel = null; // = nullIsNotFound(get(PceService.class).queryPath(PcePathId.of(id)),
                //PCE_PATH_NOT_FOUND);
        PcePath path = DefaultPcePath.builder().of(tunnel).build();
        ObjectNode result = mapper().createObjectNode();
        result.set("path", codec(PcePath.class).encode(path, this));
        return ok(result.toString()).build();
    }

    /**
     * Creates a new path.
     *
     * @param stream pce path from json
     * @return status of the request
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setupPath(InputStream stream) {
        log.debug("Setup path.");
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
            JsonNode port = jsonTree.get("path");
            PcePath path = codec(PcePath.class).decode((ObjectNode) port, this);

            DeviceId srcDevice = DeviceId.deviceId(path.source());
            DeviceId dstDevice = DeviceId.deviceId(path.destination());
            LspType lspType = path.lspType();
            List<Constraint> listConstrnt = new LinkedList<Constraint>();

            // add cost
            //TODO: need to uncomment below lines once Bandwidth and Cost constraint classes are ready
            //CostConstraint.Type costType = CostConstraint.Type.values()[Integer.valueOf(path.constraint().cost())];
            //listConstrnt.add(CostConstraint.of(costType));

            // add bandwidth. Data rate unit is in BPS.
            //listConstrnt.add(LocalBandwidthConstraint.of(Double.valueOf(path.constraint().bandwidth()), DataRateUnit
            //        .valueOf("BPS")));

            //TODO: need to uncomment below lines once setupPath method is modified in PceService
            Boolean issuccess = true; // = (null != get(PceService.class)
                    //.setupPath(srcDevice, dstDevice, path.name(), listConstrnt, lspType)) ? true : false;
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (IOException e) {
            log.error("Exception while creating path {}.", e.toString());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Update details of a specified path id.
     *
     * @param id path id
     * @param stream pce path from json
     * @return 200 OK, 404 if given identifier does not exist
     */
    @PUT
    @Path("{path_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePath(@PathParam("path_id") String id,
            final InputStream stream) {
        log.debug("Update path by identifier {}.", id);
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
            JsonNode pathNode = jsonTree.get("path");
            PcePath path = codec(PcePath.class).decode((ObjectNode) pathNode, this);
            // Assign cost
            List<Constraint> constrntList = new LinkedList<Constraint>();
            //TODO: need to uncomment below lines once CostConstraint class is ready
            if (path.costConstraint() != null) {
                //CostConstraint.Type costType = CostConstraint.Type.values()[path.constraint().cost()];
                //constrntList.add(CostConstraint.of(costType));
            }

            // Assign bandwidth. Data rate unit is in BPS.
            if (path.bandwidthConstraint() != null) {
                //TODO: need to uncomment below lines once BandwidthConstraint class is ready
                //constrntList.add(LocalBandwidthConstraint
                //        .of(path.constraint().bandwidth(), DataRateUnit.valueOf("BPS")));
            }

            //TODO: need to uncomment below line once updatePath is added to PceService
            Boolean result = true; // = (null != (get(PceService.class).updatePath(PcePathId.of(id), constrntList)))
                                      //? true : false;
            return Response.status(OK).entity(result.toString()).build();
        } catch (IOException e) {
            log.error("Update path failed because of exception {}.", e.toString());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Release a specified path.
     *
     * @param id path id
     * @return 200 OK, 404 if given identifier does not exist
     */
    @Path("{path_id}")
    @DELETE
    public Response releasePath(@PathParam("path_id") String id) {
        log.debug("Deletes path by identifier {}.", id);

        //TODO: need to uncomment below lines once releasePath method is added to PceService
        Boolean isSuccess = true; // = nullIsNotFound(get(PceService.class).releasePath(PcePathId.of(id)),
                //PCE_PATH_NOT_FOUND);
        if (!isSuccess) {
            log.debug("Path identifier {} does not exist", id);
        }

        return Response.status(OK).entity(isSuccess.toString()).build();
    }
}
