/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.manager.workflow.core.impl;

import org.apache.inlong.manager.common.exceptions.WorkflowException;
import org.apache.inlong.manager.common.util.Preconditions;
import org.apache.inlong.manager.dao.entity.WorkflowEventLogEntity;
import org.apache.inlong.manager.dao.mapper.WorkflowEventLogEntityMapper;
import org.apache.inlong.manager.workflow.WorkflowAction;
import org.apache.inlong.manager.workflow.WorkflowContext;
import org.apache.inlong.manager.workflow.core.EventListenerService;
import org.apache.inlong.manager.workflow.core.WorkflowContextBuilder;
import org.apache.inlong.manager.workflow.definition.Element;
import org.apache.inlong.manager.workflow.definition.WorkflowProcess;
import org.apache.inlong.manager.workflow.definition.WorkflowTask;
import org.apache.inlong.manager.workflow.event.process.ProcessEvent;
import org.apache.inlong.manager.workflow.event.process.ProcessEventListener;
import org.apache.inlong.manager.workflow.event.process.ProcessEventListenerManager;
import org.apache.inlong.manager.workflow.event.process.ProcessEventNotifier;
import org.apache.inlong.manager.workflow.event.task.TaskEvent;
import org.apache.inlong.manager.workflow.event.task.TaskEventListener;
import org.apache.inlong.manager.workflow.event.task.TaskEventListenerManager;
import org.apache.inlong.manager.workflow.event.task.TaskEventNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Event listener service
 */
@Service
public class EventListenerServiceImpl implements EventListenerService {

    @Autowired
    private WorkflowContextBuilder workflowContextBuilder;
    @Autowired
    private ProcessEventNotifier processEventNotifier;
    @Autowired
    private TaskEventNotifier taskEventNotifier;
    @Autowired
    private ProcessEventListenerManager processListenerManager;
    @Autowired
    private TaskEventListenerManager taskListenerManager;
    @Autowired
    private WorkflowEventLogEntityMapper eventLogMapper;

    @Override
    public void executeEventListener(Integer eventLogId) {
        WorkflowEventLogEntity eventLogEntity = eventLogMapper.selectById(eventLogId);
        Preconditions.checkNotNull(eventLogEntity, "event log not exist with id: " + eventLogId);
        if (ProcessEvent.class.getSimpleName().equals(eventLogEntity.getEventType())) {
            this.executeProcessEventListener(eventLogEntity.getProcessId(), eventLogEntity.getListener());
            return;
        }

        if (TaskEvent.class.getSimpleName().equals(eventLogEntity.getEventType())) {
            this.executeTaskEventListener(eventLogEntity.getTaskId(), eventLogEntity.getListener());
            return;
        }

        throw new WorkflowException("unknown event type: " + eventLogEntity.getEventType());
    }

    @Override
    public void executeProcessEventListener(Integer processId, String listener) {
        WorkflowContext context = workflowContextBuilder.buildContextForProcess(processId);
        ProcessEvent processEvent = getProcessEventListener(context.getProcess(), listener).event();
        context.setCurrentElement(getCurrentElement(context.getProcess(), processEvent));

        processEventNotifier.notify(listener, true, context);
    }

    @Override
    public void executeTaskEventListener(Integer taskId, String listener) {
        WorkflowContext context = workflowContextBuilder.buildContextForTask(taskId, null);
        TaskEventListener eventListener = getTaskEventListener((WorkflowTask) context.getCurrentElement(), listener);
        context.getActionContext().setAction(WorkflowAction.fromTaskEvent(eventListener.event()));
        taskEventNotifier.notify(listener, true, context);
    }

    @Override
    public void triggerProcessEvent(Integer processId, ProcessEvent processEvent) {
        WorkflowContext context = workflowContextBuilder.buildContextForProcess(processId);
        context.setCurrentElement(getCurrentElement(context.getProcess(), processEvent));
        processEventNotifier.notify(processEvent, context);
    }

    @Override
    public void triggerTaskEvent(Integer taskId, TaskEvent taskEvent) {
        WorkflowContext context = workflowContextBuilder
                .buildContextForTask(taskId, WorkflowAction.fromTaskEvent(taskEvent));
        taskEventNotifier.notify(taskEvent, context);
    }

    private Element getCurrentElement(WorkflowProcess process, ProcessEvent processEvent) {
        if (ProcessEvent.CREATE.equals(processEvent)) {
            return process.getStartEvent();
        }
        return process.getEndEvent();
    }

    private ProcessEventListener getProcessEventListener(WorkflowProcess process, String listenerName) {
        ProcessEventListener listener = process.listener(listenerName);
        if (listener != null) {
            return listener;
        }

        listener = processListenerManager.listener(listenerName);
        Preconditions.checkNotNull(listener, "process listener not exist with name: " + listenerName);
        return listener;
    }

    private TaskEventListener getTaskEventListener(WorkflowTask task, String listenerName) {
        TaskEventListener listener = task.listener(listenerName);
        if (listener != null) {
            return listener;
        }

        listener = taskListenerManager.listener(listenerName);
        Preconditions.checkNotNull(listener, "task listener not exist with name: " + listenerName);
        return listener;
    }

}
