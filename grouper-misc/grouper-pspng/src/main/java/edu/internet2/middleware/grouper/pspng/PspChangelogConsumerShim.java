package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * This class connects a PSPNG provsioner with the changelog. This is only necessary
 * until the PSPNG provisioners are written as daemons that pull events from queues.
 * 
 * @author Bert Bee-Lindgren
 *
 */
public class PspChangelogConsumerShim extends ChangeLogConsumerBase {

  final private static Logger LOG = LoggerFactory
      .getLogger(PspChangelogConsumerShim.class);

  public PspChangelogConsumerShim() {
    LOG.debug("Constructing PspngChangelogConsumerShim");
  }

  /**
   * 
   * @return
   */
  static Map<String, Object> createSubstitutionMap() {
    Map<String, Object> substituteMap = new HashMap<String, Object>();
    substituteMap.put("grouperUtil", new GrouperUtil());
    return substituteMap;
  }

  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    // This determines which changelog entries to acknowledge: The highest
    // number entry before we have a failure
    long lastSuccessfulChangelogEntry = -1;
    int numSuccessfulWorkItems = 0;
    int numFailedWorkItems = 0;
    int numSuccessfulWorkItems_thatWillBeRetried = 0;

    try {
      String consumerName = changeLogProcessorMetadata.getConsumerName();
      MDC.put("why", "CLog/");
      MDC.put("who", consumerName + "/");
      LOG.info("{}: +processChangeLogEntries({})", consumerName,
          changeLogEntryList.size());

      String elFilter = GrouperLoaderConfig.retrieveConfig()
          .propertyValueString("changeLog.consumer."
              + consumerName + ".elfilter", "");
      if (LOG.isDebugEnabled()) {
        LOG.debug("elFilter {}", elFilter);
      }

      if (!StringUtils.isBlank(elFilter)) {
        String[] types = elFilter.split(",");
        List<String> filteredTypes = Arrays.asList(types);
        List<ChangeLogEntry> filteredout = new ArrayList<>();
        for (ChangeLogEntry entry : changeLogEntryList) {
          EsbEvent esbEvent = mapChangeLogEntryToEsbEvent(entry);
          if (!matchesFilter(esbEvent, elFilter)) {
            if (entry.getSequenceNumber() > lastSuccessfulChangelogEntry) {
              lastSuccessfulChangelogEntry = entry.getSequenceNumber();
            }
            filteredout.add(entry);
          }
        }
        changeLogEntryList.removeAll(filteredout);
      }

      Map<String, Object> substitutionMap = createSubstitutionMap();
      substitutionMap.put("externalSubject", this);

      String description = GrouperUtil.substituteExpressionLanguage(elFilter,
          substitutionMap,
          false, true, true);
      System.out.println(description);

      Provisioner provisioner;
      try {
        provisioner = ProvisionerFactory.getIncrementalProvisioner(consumerName);

        // Make sure the full syncer is also created and running
        FullSyncProvisionerFactory.getFullSyncer(provisioner);
      } catch (PspException e1) {
        LOG.error("Provisioner {} could not be created", consumerName, e1);
        throw new RuntimeException(
            "provisioner could not be created: " + e1.getMessage());
      }

      List<ProvisioningWorkItem> workItems = new ArrayList<ProvisioningWorkItem>();

      for (ChangeLogEntry entry : changeLogEntryList) {
        workItems.add(new ProvisioningWorkItem(entry));
      }

      provisioner.provisionBatchOfItems(workItems);

      String firstErrorMessage = null;

      for (ProvisioningWorkItem workItem : workItems) {
        if (workItem.wasSuccessful()) {
          numSuccessfulWorkItems++;

          // If we haven't seen a failure yet, then keep counting up the successes
          if (numFailedWorkItems == 0)
            lastSuccessfulChangelogEntry = workItem.getChangelogEntry()
                .getSequenceNumber();
          else
            numSuccessfulWorkItems_thatWillBeRetried++;
        } else if (firstErrorMessage == null) {
          numFailedWorkItems++;
          firstErrorMessage = workItem.getStatusMessage();
        }
      }

      StringBuilder summary = new StringBuilder();
      if (firstErrorMessage != null)
        summary.append(String.format("Summary: %d successes/%d failures.  ",
            numSuccessfulWorkItems, numFailedWorkItems));

      if (numSuccessfulWorkItems_thatWillBeRetried > 0)
        summary.append(String.format(
            "(%d successful entries will be retried because they follow a failure in the queue.) ",
            numSuccessfulWorkItems_thatWillBeRetried));

      if (firstErrorMessage != null)
        summary.append(String.format("First error was: %s", firstErrorMessage));

      if (numFailedWorkItems > 0)
        LOG.warn("Provisioning summary: {}", summary);
      else
        LOG.info("Provisioning summary: {}", summary);

      changeLogProcessorMetadata.getHib3GrouperLoaderLog()
          .appendJobMessage(summary.toString());
      return lastSuccessfulChangelogEntry;
    } finally {
      MDC.remove("why");
      MDC.remove("who");
    }
  }

  /**
   * 
   * @param changeLogEntry
   * @param changeLogLabel
   * @return label value
   */
  private String getLabelValue(ChangeLogEntry changeLogEntry,
      ChangeLogLabel changeLogLabel) {
    try {
      return changeLogEntry.retrieveValueForLabel(changeLogLabel);
    } catch (Exception e) {
      //cannot get value for label
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot get value for label: " + changeLogLabel.name());
      }
      return null;
    }
  }

  /**
   * @param entry
   * @return
   */
  private EsbEvent mapChangeLogEntryToEsbEvent(ChangeLogEntry changeLogEntry) {
    EsbEvent esbEvent = new EsbEvent();
    // convert 
    final String changeLogEventTypeKey = changeLogEntry.getChangeLogType()
        .getChangeLogCategory() + "_"
        + changeLogEntry.getChangeLogType().getActionName();

    esbEvent.setCreatedOnMicros(changeLogEntry.getCreatedOn().getTime());
    esbEvent.setGroupId(changeLogEntry.getString07());
    EsbEvent.EsbEventType eventType = null;
    String groupName = null;
    switch (changeLogEventTypeKey) {
      case "group_addGroup":
        esbEvent.setEventType(EsbEvent.EsbEventType.GROUP_ADD.name());
        esbEvent.setId(this.getLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.id));
        esbEvent.setName(this
            .getLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.name));
        esbEvent.setParentStemId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.GROUP_ADD.parentStemId));
        esbEvent.setDisplayName(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.GROUP_ADD.displayName));
        esbEvent.setDescription(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.GROUP_ADD.description));

        groupName = this.getLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.name);
        break;
      case "membership_addMembership":
        eventType = EsbEvent.EsbEventType.MEMBERSHIP_ADD;
        esbEvent.setEventType(EsbEvent.EsbEventType.MEMBERSHIP_ADD.name());
        // throws error
        esbEvent.setId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.id));
        esbEvent.setFieldName(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
        esbEvent.setSubjectId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
        esbEvent.setSourceId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
        // throws error
        esbEvent.setMembershipType(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
        esbEvent.setGroupId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.groupId));
        esbEvent.setGroupName(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.groupName));

        groupName = this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_ADD.groupName);
        break;
      case "membership_deleteMembership":
        eventType = EsbEvent.EsbEventType.MEMBERSHIP_ADD;
        esbEvent.setEventType(EsbEvent.EsbEventType.MEMBERSHIP_DELETE.name());
        esbEvent.setId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.id));
        esbEvent.setFieldName(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
        esbEvent.setSubjectId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
        esbEvent.setSourceId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
        esbEvent.setMembershipType(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
        esbEvent.setGroupId(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
        esbEvent.setGroupName(this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.groupName));

        groupName = this.getLabelValue(changeLogEntry,
            ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
        break;
      default:
        esbEvent.setEventType("");
        break;
    }
    if (null != eventType) {
      esbEvent.setEventType(eventType.toString());
    }
    return esbEvent;
  }

  /**
   * @see EsbConsumer#matchesFilter(EsbEvent, String)
   * 
   * see if the esb event matches an EL filter.  Note the available objects are
   * event for the EsbEvent, and grouperUtil for the GrouperUtil class which has
   * a lot of utility methods
   * @param filterString
   * @param esbEvent
   * @return true if matches, false if doesnt
   */
  public static boolean matchesFilter(EsbEvent esbEvent, String filterString) {

    Map<String, Object> elVariables = new HashMap<String, Object>();
    elVariables.put("event", esbEvent);
    elVariables.put("grouperUtilElSafe", new GrouperUtil());

    String resultString = GrouperUtil.substituteExpressionLanguage(
        "${" + filterString + "}", elVariables, true, true, true);

    boolean result = GrouperUtil.booleanValue(resultString, false);

    return result;
  }
}
