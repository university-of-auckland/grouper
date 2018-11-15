<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['adminDaemonJobsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['adminDaemonJobsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['adminDaemonJobsTitle'] }</h1>
              </div>

            </div>
            <script language="javascript">
              var daemonJobsRefreshCount = ${grouperRequestContainer.adminContainer.daemonJobsRefreshInterval};
              var daemonJobsRefreshInterval = setInterval(function() {
                if (!$("#daemonJobsNextRefreshSeconds").length) {
                  clearInterval(daemonJobsRefreshInterval);
                } else {
                  if ($("#daemonJobsRefreshed").val() != "1") {
                  } else if (daemonJobsRefreshCount == 0) {
                    $("#daemonJobsRefreshed").val("0");
                    daemonJobsRefreshCount = ${grouperRequestContainer.adminContainer.daemonJobsRefreshInterval};
                    ajax('../app/UiV2Admin.daemonJobsSubmit', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId, daemonJobsPagingFormPageNumberId'});
                  } else {
                    daemonJobsRefreshCount = daemonJobsRefreshCount - 1;
                  }
                  
                  $("#daemonJobsNextRefreshSeconds").text(daemonJobsRefreshCount);
                }
              }, 1000);
            </script>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-inline form-filter" id="daemonJobsFilterFormId"
                    onsubmit="ajax('../app/UiV2Admin.daemonJobsSubmit', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId'}); return false;">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="daemonJobsFilterId" style="white-space: nowrap;">${textContainer.text['daemonJobsFilterFor'] }</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <input type="text" name="daemonJobsFilter" placeholder="${textContainer.textEscapeXml['daemonJobsSearchNamePlaceholder'] }" id="daemonJobsFilterId" class="span12"/>
                    </div>

                    <div class="span3" style="white-space: nowrap;">
                      <label class="checkbox">
                        <input type="checkbox" name="daemonJobsFilterShowExtendedResults">${textContainer.text['daemonJobsFilterShowExtendedResults']}
                      </label>
                      &nbsp; &nbsp;
                      <a class="btn" role="button" aria-controls="daemonJobsResultsId" href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId'}); return false;">${textContainer.text['daemonJobsSearchButton'] }</a> &nbsp;
                      <a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsReset', {formIds: 'daemonJobsPagingFormId'}); return false;" class="btn" role="button">${textContainer.text['daemonJobsResetButton'] }</a>
                    </div>
                  </div>
                </form>
                <div class="span3">${textContainer.text['daemonJobsNextRefresh']} <span id="daemonJobsNextRefreshSeconds">${grouperRequestContainer.adminContainer.daemonJobsRefreshInterval}</span></div>
                <div id="daemonJobsResultsId" role="region" aria-live="polite">
                </div>
              </div>
            </div>
