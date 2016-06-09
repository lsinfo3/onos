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
    private static final int HARDWARE_TABLE = 100;
    private static final int SOFTWARE_TABLE_START = 200;
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

        addGotoTable100Rule();

    }

    // adding Flow rule to table 0 where all packets are send to table 100
    // as otherwise the packets get dropped in table 0
    private void addGotoTable100Rule() {

        log.warn("Adding flow rule to send all traffic from table 0 to table 100.");

        FlowRule gotoTbl100 = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .withSelector(DefaultTrafficSelector.builder().build())
                .withTreatment(DefaultTrafficTreatment.builder().transition(100).build())
                .withPriority(0)
                .fromApp(appId)
                .forTable(0)
                .makePermanent()
                .build();

        flowRuleService.applyFlowRules(gotoTbl100);
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
                log.info("HpPipeline: Success! Flow rule operations applied. ForwardingObjective = {}", forwardObjective);
                pass(forwardObjective);
            }

            @Override
            public void onError(FlowRuleOperations ops) {
                log.warn("HpPipeline: Fail! Flow rule operations not applied. ForwardingObjective = {}", forwardObjective);
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
                .fromApp(fwd.appId())
                .forTable(HARDWARE_TABLE);

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
