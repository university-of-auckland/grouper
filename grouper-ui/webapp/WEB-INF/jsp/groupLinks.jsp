<%-- @annotation@
		  	Tile which displays a standard set of group management links based
		 	on the privileges of the current user for the current group
--%><%--
  @author Gary Brown.
  @version $Id: groupLinks.jsp,v 1.3 2005-12-08 15:33:28 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endGroupLinks" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.group-links"/></a>
<div class="groupLinks">
<div class="linkButton">
<c:if test="${groupPrivs.ADMIN}">
		<tiles:insert definition="selectGroupPrivilegeDef"/>
	</c:if>
	<c:if test="${groupPrivs.ADMIN}">
		
			<html:link page="/populateEditGroup.do" name="group">
				<fmt:message bundle="${nav}" key="groups.action.edit"/>
			</html:link>
		
	</c:if>
	
	
	
	<c:if test="${groupPrivs.ADMIN  || groupPrivs.READ}">
		
			<html:link page="/populateGroupMembers.do"  name="group">
				<fmt:message bundle="${nav}" key="groups.action.edit-members"/>
			</html:link>
		
		</c:if>
		<c:if test="${groupPrivs.ADMIN  || groupPrivs.UPDATE}">
		
			<html:link page="/populateFindNewMembers.do"  name="group">
				<fmt:message bundle="${nav}" key="find.groups.add-new-members"/>
			</html:link>
		
		</c:if>
		<c:if test="${groupPrivs.ADMIN}">
		
			<html:link page="/deleteGroup.do"  name="group">
				<fmt:message bundle="${nav}" key="groups.action.delete"/>
			</html:link>
		
	</c:if>
	<c:if test="${groupPrivs.OPTIN && !groupPrivs.MEMBER}">
		
			<html:link page="/joinGroup.do"  name="group">
				<fmt:message bundle="${nav}" key="groups.action.join"/>
			</html:link>
		
	</c:if>
	<c:if test="${groupPrivs.OPTOUT && groupPrivs.MEMBER}">
		
			<html:link page="/leaveGroup.do"  name="group">
				<fmt:message bundle="${nav}" key="groups.action.leave"/>
			</html:link>
		
	</c:if>
	<jsp:useBean id="subjSum" class="java.util.HashMap"/>
	<c:set target="${subjSum}" property="subjectId" value="${group.id}"/>
	<c:set target="${subjSum}" property="subjectType" value="group"/>
	<c:set target="${subjSum}" property="changeMode" value="true"/>
			<html:link page="/populateSubjectSummary.do" name="subjSum">
				<fmt:message bundle="${nav}" key="groups.action.summary.goto-this-subject"/>
			</html:link>
</div>
</div>
<a name="endGroupLinks" id="endGroupLinks"></a>
</grouper:recordTile>