<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <%-- tell add member to refresh audits --%>
              <form id="groupRefreshPartFormId">
                <input type="hidden" name="groupRefreshPart" value="thisGroupsAttributeDefPrivileges" /> 
              </form> 

              <form class="form-inline form-small" name="groupPrivilegeFormName" id="groupPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="11" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">${textContainer.text['groupPrivilegesUpdateBulkLabel']}</label>
                          </div>
                          <div class="span4">

                            <select id="people-update" class="span12" name="groupPrivilegeBatchUpdateOperation">
                              <%-- create group should be the default, so list it first --%>

                              <option value="assign_attrAdmins">${textContainer.text['groupPrivilegesAssignAttrAdminPrivilege'] }</option>
                              <option value="assign_attrUpdaters">${textContainer.text['groupPrivilegesAssignAttrUpdatePrivilege'] }</option>
                              <option value="assign_readersUpdaters">${textContainer.text['groupPrivilegesAssignAttrReadUpdatePrivilege'] }</option>
                              <option value="assign_attrReaders">${textContainer.text['groupPrivilegesAssignAttrReadPrivilege'] }</option>
                              <option value="assign_attrViewers">${textContainer.text['groupPrivilegesAssignAttrViewPrivilege'] }</option>
                              <option value="assign_attrDefAttrReaders">${textContainer.text['groupPrivilegesAssignAttrDefAttributeReadPrivilege'] }</option>
                              <option value="assign_attrDefAttrUpdaters">${textContainer.text['groupPrivilegesAssignAttrDefAttributeUpdatePrivilege'] }</option>
                              <%--<option value="assign_attrOptins">${textContainer.text['groupPrivilegesAssignAttrOptinPrivilege'] }</option>--%>
                              <%--<option value="assign_attrOptouts">${textContainer.text['groupPrivilegesAssignAttrOptoutPrivilege'] }</option>--%>
                              <option value="revoke_attrAdmins">${textContainer.text['groupPrivilegesRevokeAttrAdminPrivilege'] }</option>
                              <option value="revoke_attrUpdaters">${textContainer.text['groupPrivilegesRevokeAttrUpdatePrivilege'] }</option>
                              <option value="revoke_readersUpdaters">${textContainer.text['groupPrivilegesRevokeAttrReadUpdatePrivilege'] }</option>
                              <option value="revoke_attrReaders">${textContainer.text['groupPrivilegesRevokeAttrReadPrivilege'] }</option>
                              <option value="revoke_attrViewers">${textContainer.text['groupPrivilegesRevokeAttrViewPrivilege'] }</option>
                              <option value="revoke_attrDefAttrReaders">${textContainer.text['groupPrivilegesRevokeAttrDefAttributeReadPrivilege'] }</option>
                              <option value="revoke_attrDefAttrUpdaters">${textContainer.text['groupPrivilegesRevokeAttrDefAttributeUpdatePrivilege'] }</option>
                              <%--<option value="revoke_attrOptins">${textContainer.text['groupPrivilegesRevokeAttrOptinPrivilege'] }</option>--%>
                              <%--<option value="revoke_attrOptouts">${textContainer.text['groupPrivilegesRevokeAttrOptoutPrivilege'] }</option>--%>
                              <option value="revoke_all">${textContainer.text['groupPrivilegesRevokeAttrAllPrivilege'] }</option>

                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Group.thisGroupsPrivilegesAssignAttributeDefPrivilegeBatch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId,groupPrivilegeFormId'}); return false;">${textContainer.text['thisGroupPrivilegeUpdateSelectedButton'] }</button>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.privilegeCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                        </label>
                      </th>
                      <th>
                        ${textContainer.text['thisGroupsPrivilegesAttributeDefColumn'] }
                      </th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrAdmin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrUpdate'] }</th>
                      <%--<th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrOptin'] }</th>--%>
                      <%--<th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrOptout'] }</th>--%>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrDefAttributeRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrDefAttributeUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center; width: 10em;">${textContainer.text['priv.colAttrView'] }</th>
                      <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.groupContainer.privilegeGuiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" aria-label="${textContainer.text['groupPrivilegesInAttributeDefCheckboxAriaLabel']}"
                            value="${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker" style="white-space: nowrap">${guiMembershipSubjectContainer.guiAttributeDef.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for groups --%>
                        <c:forEach items="attrAdmins,attrReaders,attrUpdaters,attrDefAttrReaders,attrDefAttrUpdaters,attrViewers" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null 
                                   && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <i class="fa fa-check fa-direct" tabindex="0" aria-label="${textContainer.textEscapeXml['thisGroupsPrivilegesRemoveTitle'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignAttributeDefPrivilege?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;}"></i>
                                <a title="${textContainer.textEscapeXml['thisGroupsPrivilegesRemoveTitle'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignAttributeDefPrivilege?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times"></i></a>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><i class="fa fa-check fa-disabled" tabindex="0" aria-label="${textContainer.textEscapeXml['thisGroupsPrivilegesAssignTitle'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignAttributeDefPrivilege?assign=true&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;}"></i></c:if>
                                <a title="${textContainer.textEscapeXml['thisGroupsPrivilegesAssignTitle'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['groupConfirmChanges']}')) {ajax('../app/UiV2Group.thisGroupsPrivilegesAssignAttributeDefPrivilege?assign=true&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&fieldName=${fieldName}&parentAttributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId,groupPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus"></i></a>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group">
                          	<a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-mini dropdown-toggle"
                          		aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                          		${textContainer.text['thisGroupsPrivilegesActionsButton']} 
                          			<span class="caret"></span>
                          	</a>
                            <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                              
                              <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceAttributeDefPrivileges&attributeDefId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&backTo=subject'); return false;" class="actions-revoke-membership">${textContainer.text['thisGroupsPrivilegesActionsMenuTracePrivileges'] }</a></li>
                              </c:if>
                              
                              <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef&groupId=${guiMembershipSubjectContainer.guiAttributeDef.attributeDef.id}');">${textContainer.text['thisGroupsPrivilegesActionsMenuViewAttributeDef']}</a></li>
                            </ul>
                          </div>
                        </td>
                      </tr>
                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
              </form>
              <div class="data-table-bottom gradient-background">
                <grouper:paging2 guiPaging="${grouperRequestContainer.groupContainer.privilegeGuiPaging}" formName="groupPagingPrivilegesForm" ajaxFormIds="groupFilterPrivilegesFormId"
                  refreshOperation="../app/UiV2Group.filterThisGroupsAttributeDefPrivileges?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
              </div>