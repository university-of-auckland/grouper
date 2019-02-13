<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <c:set var="isGroupEditable" value="${grouperRequestContainer.groupContainer.editable}"/>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.groupContainer.guiPaging}" formName="groupPagingForm" ajaxFormIds="groupFilterFormId"
                    refreshOperation="../app/UiV2Group.filter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                </div>
                <form id="membersToDeleteFormId">
                  <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                    <thead>
                      <tr>
                        <td colspan="4" class="table-toolbar gradient-background">
                          <c:if test="${isGroupEditable && grouperRequestContainer.groupContainer.canUpdate}">
                            <a href="#" onclick="ajax('../app/UiV2Group.removeMembers?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterFormId,groupPagingFormId,membersToDeleteFormId'}); return false;" class="btn" role="button">${textContainer.text['groupRemoveSelectedMembersButton'] }</a>
                          </c:if>
                        </td>
                      </tr>
                      <tr>
                        <th>
                          <c:if test="${isGroupEditable && grouperRequestContainer.groupContainer.canUpdate}">
                            <label class="checkbox checkbox-no-padding">
                              <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.membershipCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                            </label>
                          </c:if>
                        </th>
                        <th class="sorted">${textContainer.text['groupViewDetailsHeaderEntityName']}</th>
                        <th data-hide="phone">${textContainer.text['groupViewDetailsHeaderMembership']}</th>
                        <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:set var="i" value="0" />
                      <c:forEach items="${grouperRequestContainer.groupContainer.guiMembershipSubjectContainers}" 
                        var="guiMembershipSubjectContainer" >
                        <c:set var="guiMembershipContainer" value="${guiMembershipSubjectContainer.guiMembershipContainers['members']}" />
                        <tr>
                          <td>
                            <c:if test="${isGroupEditable && grouperRequestContainer.groupContainer.canUpdate}">
                              <label class="checkbox checkbox-no-padding">
                                <c:choose>
                                  <c:when test="${guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                    <input type="checkbox" aria-label="${textContainer.text['groupViewDetailsMembershipCheckboxAriaLabel']}" name="membershipRow_${i}" value="${guiMembershipContainer.membershipContainer.immediateMembership.uuid}" class="membershipCheckbox" />
                                  </c:when>
                                  <c:otherwise>
                                    <input type="checkbox" disabled="disabled"/>
                                  </c:otherwise>
                                </c:choose>
                              </label>
                            </c:if>
                          </td>
                          <td class="expand foo-clicker">${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon} <br/>
                          </td>
                          <td data-hide="phone">
                            ${textContainer.text[grouper:concat2('groupMembershipAssignType_',guiMembershipContainer.membershipContainer.membershipAssignType)] }
                          </td>
                          <td>
                            <c:if test="${grouperRequestContainer.groupContainer.canRead
                                || (guiMembershipContainer.membershipContainer.membershipAssignType.immediate && grouperRequestContainer.groupContainer.canUpdate)
                                || guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate
                                || guiMembershipSubjectContainer.guiSubject.group}">
                              <div class="btn-group"><a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreMembershipActions']}" href="#" class="btn btn-mini dropdown-toggle"
                              	aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                              		${textContainer.text['groupViewActionsButton'] } 
                              		<span class="caret"></span>
                              	</a>
                                <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
                                  <c:if test="${grouperRequestContainer.groupContainer.canRead}">
                                    <li><a href="#" onclick="return guiV2link('operation=UiV2Membership.editMembership&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&field=members');" class="actions-revoke-membership">${textContainer.text['groupViewEditMembershipsAndPrivilegesButton'] }</a></li>
                                  </c:if>
                                  <c:if test="${isGroupEditable && guiMembershipContainer.membershipContainer.membershipAssignType.immediate && grouperRequestContainer.groupContainer.canUpdate}">
                                    <li><a href="#" onclick="ajax('../app/UiV2Group.removeMember?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'groupFilterFormId,groupPagingFormId'}); return false;" class="actions-revoke-membership">${textContainer.text['groupViewRevokeMembershipButton'] }</a></li>
                                  </c:if>
                                  <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                    <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceMembership&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}&field=members');" class="actions-revoke-membership">${textContainer.text['groupViewTraceMembershipButton'] }</a></li>
                                  </c:if>
                                  <c:if test="${guiMembershipSubjectContainer.guiSubject.group}">
                                    <li><a href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${guiMembershipSubjectContainer.guiSubject.subject.id}');">${textContainer.text['groupViewViewGroupButton'] }</a></li>
                                  </c:if>
                                </ul>
                              </div>
                            </c:if>
                          </td>
                        </tr>
                        <c:set var="i" value="${i+1}" />
                      </c:forEach>
                    </tbody>
                  </table>
                </form>
                <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.groupContainer.guiPaging}" formName="groupPagingForm2" ajaxFormIds="groupFilterFormId"
                    refreshOperation="../app/UiV2Group.filter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                </div>
                
