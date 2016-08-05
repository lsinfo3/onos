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
package org.onosproject.driver.pipeline;


import org.onlab.osgi.ServiceDirectory;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.behaviour.PipelinerContext;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleOperations;
import org.onosproject.net.flow.FlowRuleOperationsContext;
import org.onosproject.net.flowobjective.FilteringObjective;
import org.onosproject.net.flowobjective.FlowObjectiveStore;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.NextObjective;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.flowobjective.ObjectiveError;

import org.slf4j.Logger;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;


import static org.slf4j.LoggerFactory.getLogger;

/**
 *  Driver for Hp. Default table starts from 200.
 */
public class HpPipeline extends AbstractHandlerBehaviour implements Pipeliner {


    private final Logger log = getLogger(getClass());

    private ServiceDirectory serviceDirectory;
    private FlowRuleService flowRuleService;
    private CoreService coreService;
    private DeviceId deviceId;
    private ApplicationId appId;
    protected FlowObjectiveStore flowObjectiveStore;

    //FIXME: hp table numbers are configurable . Set this parameter configurable
    // using OpenFlow 1.3 multi-table model in standard mode (default)
    private static final int TABLE_0 = 0;
    private static final int POLICY_ENGINE = 100; // Hardware matching table
    private static final int SOFTWARE_TABLE = 200;
    private static final int TIME_OUT = 30;

    @Override
    public void init(DeviceId deviceId, PipelinerContext context) {
        log.warn("Initiate HP pipeline");
        this.serviceDirectory = context.directory();
        this.deviceId = deviceId;

        flowRuleService = serviceDirectory.get(FlowRuleService.class);
        coreService = serviceDirectory.get(CoreService.class);
        flowObjectiveStore = context.store();

        appId = coreService.registerApplication(
                "org.onosproject.driver.HpPipeline");

        addGotoPolicyEngineRule();
        addGotoSoftwareTableRule();
        addTableMissAction();

    }

    // installing flow rule to table 0 sending all unmatched traffic to table 100
    private void addGotoPolicyEngineRule() {

        log.info("Installing flow rule to send all unmatched traffic in table 0 to table 100.");

        FlowRule gotoTbl100 = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .withSelector(DefaultTrafficSelector.builder().build())
                .withTreatment(DefaultTrafficTreatment.builder().transition(POLICY_ENGINE).build())
                .withPriority(0)
                .fromApp(appId)
                .forTable(TABLE_0)
                .makePermanent()
                .build();

        flowRuleService.applyFlowRules(gotoTbl100);
    }

    // installing flow rule to table 100, sending all unmatched traffic to table 200
    private void addGotoSoftwareTableRule(){
        log.info("Installing flow rule to send all unmatched traffic in table 100 to table 200.");

        FlowRule gotoTbl200 = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .withSelector(DefaultTrafficSelector.builder().build())
                .withTreatment(DefaultTrafficTreatment.builder().transition(SOFTWARE_TABLE).build())
                .withPriority(0)
                .fromApp(appId)
                .forTable(POLICY_ENGINE)
                .makePermanent()
                .build();

        flowRuleService.applyFlowRules(gotoTbl200);
    }

    private void addTableMissAction(){
        log.info("Installing table miss action for table 200 (software table).");

        FlowRule tableMiss = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .withSelector(DefaultTrafficSelector.builder().build())
                .withTreatment(DefaultTrafficTreatment.builder().drop().build())
                .withPriority(0)
                .fromApp(appId)
                .forTable(SOFTWARE_TABLE)
                .makePermanent()
                .build();

        flowRuleService.applyFlowRules(tableMiss);
    }

    @Override
    public void filter(FilteringObjective filter)  {
        //Do nothing
    }

    @Override
    public void forward(ForwardingObjective forwardObjective) {
        log.warn("HP forward called");

        Collection<FlowRule> rules;
        FlowRuleOperations.Builder flowOpsBuilder = FlowRuleOperations.builder();

        rules = processForward(forwardObjective);

        switch (forwardObjective.op()) {
            case ADD:
                rules.stream()
                        .filter(Objects::nonNull)
                        .forEach(flowOpsBuilder::add);
                break;
            case REMOVE:
                rules.stream()
                        .filter(Objects::nonNull)
                        .forEach(flowOpsBuilder::remove);
                break;
            default:
                fail(forwardObjective, ObjectiveError.UNKNOWN);
                log.warn("Unknown forwarding type {}");
        }

        flowRuleService.apply(flowOpsBuilder.build(new FlowRuleOperationsContext() {
            @Override
            public void onSuccess(FlowRuleOperations ops) {
                // log.info("HpPipeline: Success! Flow rule operations applied. ForwardingObjective = {}",
                //        forwardObjective);
                pass(forwardObjective);
            }

            @Override
            public void onError(FlowRuleOperations ops) {
                log.warn("HpPipeline: Fail! Flow rule operations not applied. ForwardingObjective = {}",
                        forwardObjective);
                fail(forwardObjective, ObjectiveError.FLOWINSTALLATIONFAILED);
            }
        }));

    }

    private Collection<FlowRule> processForward(ForwardingObjective fwd) {

        log.debug("Processing forwarding object");

        FlowRule.Builder ruleBuilder = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .withSelector(fwd.selector())
                .withTreatment(fwd.treatment())
                .withPriority(fwd.priority())
                .fromApp(fwd.appId());

        // distinguishing between hardware matching flow rules and software matching flow rules
        if(fwd.priority() >= 100){
            ruleBuilder.forTable(POLICY_ENGINE);
        } else{
            ruleBuilder.forTable(SOFTWARE_TABLE);
        }

        if (fwd.permanent()) {
            ruleBuilder.makePermanent();
        } else {
            ruleBuilder.makeTemporary(TIME_OUT);
        }

        return Collections.singletonList(ruleBuilder.build());
    }

    @Override
    public void next(NextObjective nextObjective) {
        // Do nothing
    }

    private void pass(Objective obj) {
        obj.context().ifPresent(context -> context.onSuccess(obj));
    }

    private void fail(Objective obj, ObjectiveError error) {
        obj.context().ifPresent(context -> context.onError(obj, error));
    }


}
