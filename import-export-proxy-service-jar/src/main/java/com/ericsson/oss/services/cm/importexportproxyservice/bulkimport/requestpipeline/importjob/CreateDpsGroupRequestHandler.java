/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob;

import static com.ericsson.oss.services.cm.bulkimport.constant.DpsGroupConstants.GROUP_NAME;
import static com.ericsson.oss.services.cm.bulkimport.constant.DpsGroupConstants.GROUP_NAMESPACE;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.CSV_SEPARATOR;
import static com.ericsson.oss.services.cm.bulkimport.constant.ImportConstants.UNDERSCORE;
import static com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.importjob.CreateDpsGroupResponse.createDpsGroupResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.Group;
import com.ericsson.oss.itpf.datalayer.dps.GroupConfiguration;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestHandler.Handle;
import com.ericsson.oss.services.cm.importexportproxyservice.bulkimport.requestpipeline.RequestValidator;

/**
 * CreateDpsGroupRequestHandler class to handle create Dps Group.
 */
@Handle(CreateDpsGroupRequest.class)
public class CreateDpsGroupRequestHandler implements RequestHandler<CreateDpsGroupRequest, CreateDpsGroupResponse> {

    static final String NETWORKELEMENT_FDN = "NetworkElement=%s";

    @Inject
    @Any
    Instance<RequestValidator<CreateDpsGroupRequest>> validators;

    @EServiceRef
    DataPersistenceService dps;

    @Inject
    ImportNodesService importNodesService;

    @Override
    public CreateDpsGroupResponse handle(final CreateDpsGroupRequest request) {
        final List<String> nodeNames = importNodesService.getExecutedAndExecutionEligibleNodes(request.getJobId());
        final DataBucket dataBucket = dps.getLiveBucket();
        final List<ManagedObject> networkElementMoList = getNetworkElementMos(nodeNames, dataBucket);
        final String groupName = createAndPopulateGroup(networkElementMoList, dataBucket);
        return createDpsGroupResponse(groupName);
    }

    @Override
    public Iterable<RequestValidator<CreateDpsGroupRequest>> validators() {
        return validators;
    }

    private List<ManagedObject> getNetworkElementMos(final List<String> nodeNames, final DataBucket dataBucket) {
        final List<ManagedObject> networkElementMoList = new ArrayList<>();
        for (final String nodeName : nodeNames) {
            final ManagedObject networkElementMo = dataBucket.findMoByFdn(String.format(NETWORKELEMENT_FDN, nodeName));
            if (networkElementMo != null) {
                networkElementMoList.add(networkElementMo);
            }
        }
        return networkElementMoList;
    }

    private String createAndPopulateGroup(final List<ManagedObject> poList, final DataBucket dataBucket) {
        final GroupConfiguration groupConfiguration = new GroupConfiguration(GROUP_NAMESPACE, createGroupName(), false);
        final Group group = dataBucket.createGroup(groupConfiguration);
        group.add(poList.toArray(new ManagedObject[0]));
        // Returning the combination of groupname and namespace as NHM application is accepting the groupname in the format of namespace:groupname
        return group.getNamespace() + CSV_SEPARATOR + group.getName();
    }

    private String createGroupName() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        // Date format is used without any special characters as NHM does not allow any special characters in groupName.
        final SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        return GROUP_NAME + UNDERSCORE + dateFormat.format(cal.getTime());
    }
}
