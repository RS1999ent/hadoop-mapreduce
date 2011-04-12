/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.hadoop.yarn.server.nodemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.NodeHealthCheckerService;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.UnsupportedFileSystemException;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.yarn.api.protocolrecords.StartContainerRequest;
import org.apache.hadoop.yarn.api.protocolrecords.StopContainerRequest;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.event.AsyncDispatcher;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
import org.apache.hadoop.yarn.factories.RecordFactory;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.apache.hadoop.yarn.server.api.ResourceTracker;
import org.apache.hadoop.yarn.server.nodemanager.NodeManager.NMContext;
import org.junit.Test;

public class TestEventFlow {

  private static final Log LOG = LogFactory.getLog(TestEventFlow.class);
  private static final RecordFactory recordFactory = RecordFactoryProvider.getRecordFactory(null);

  private static File localDir = new File("target",
      TestEventFlow.class.getName() + "-localDir").getAbsoluteFile();
  private static File logDir = new File("target",
      TestEventFlow.class.getName() + "-logDir").getAbsoluteFile();

  @Test
  public void testSuccessfulContainerLaunch() throws InterruptedException,
      IOException {

    FileContext localFS = FileContext.getLocalFSFileContext();

    localFS.delete(new Path(localDir.getAbsolutePath()), true);
    localFS.delete(new Path(logDir.getAbsolutePath()), true);
    localDir.mkdir();
    logDir.mkdir();

    Context context = new NMContext();

    YarnConfiguration conf = new YarnConfiguration();
    conf.set(NMConfig.NM_LOCAL_DIR, localDir.getAbsolutePath());
    conf.set(NMConfig.NM_LOG_DIR, logDir.getAbsolutePath());

    ContainerExecutor exec = new DefaultContainerExecutor();
    DeletionService del = new DeletionService(exec);
    Dispatcher dispatcher = new AsyncDispatcher();
    NodeHealthCheckerService healthChecker = null;
    NodeStatusUpdater nodeStatusUpdater =
        new NodeStatusUpdaterImpl(context, dispatcher, healthChecker) {
      @Override
      protected ResourceTracker getRMClient() {
        return new LocalRMInterface();
      };

      @Override
      protected void startStatusUpdater() throws InterruptedException,
          YarnRemoteException {
        return; // Don't start any updating thread.
      }
    };

    DummyContainerManager containerManager =
        new DummyContainerManager(context, exec, del, nodeStatusUpdater);
    containerManager.init(conf);
    containerManager.start();

    ContainerLaunchContext launchContext = recordFactory.newRecordInstance(ContainerLaunchContext.class);
    ContainerId cID = recordFactory.newRecordInstance(ContainerId.class);
    cID.setAppId(recordFactory.newRecordInstance(ApplicationId.class));
    launchContext.setContainerId(cID);
    launchContext.setUser("testing");
    launchContext.setResource(recordFactory.newRecordInstance(Resource.class));
    StartContainerRequest request = recordFactory.newRecordInstance(StartContainerRequest.class);
    request.setContainerLaunchContext(launchContext);
    containerManager.startContainer(request);

    DummyContainerManager.waitForContainerState(containerManager, cID,
        ContainerState.RUNNING);

    StopContainerRequest stopRequest = recordFactory.newRecordInstance(StopContainerRequest.class);
    stopRequest.setContainerId(cID);
    containerManager.stopContainer(stopRequest);
    DummyContainerManager.waitForContainerState(containerManager, cID,
        ContainerState.COMPLETE);

    containerManager.stop();
  }
}