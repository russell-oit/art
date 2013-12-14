<%-- 
    Document   : settings
    Created on : 23-Nov-2013, 20:53:16
    Author     : Timothy Anyona

Settings configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.configureSettings" var="pageTitle" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				toggleSmtpUsernameEnabled(); // enable/disable on page load
				toggleLdapBindDnEnabled();

				$(function() {
					$('a[href*="settings.do"]').parent().addClass('active');
				});

				$(function() {
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});

				$('#useSmtpAuthentication').change(function() {
					toggleSmtpUsernameEnabled();
				});

				$('#useLdapAnonymousBind').change(function() {
					toggleLdapBindDnEnabled();
				});

				$('input[name=artAuthenticationMethod]').change(function() {
					switch ($('input[name=artAuthenticationMethod]:checked').val()) {
						case 'WindowsDomain':
							$('#windowsDomainController').focus();
							break;
						case 'Database':
							$('#databaseAuthenticationDriver').focus();
							break;
						case 'LDAP':
							$('#ldapServer').focus();
							break;
						default:
							//do nothing
					}
				});
			});

			function toggleSmtpUsernameEnabled() {
				if ($('#useSmtpAuthentication').is(':checked')) {
					$('#smtpUsernameFields').prop('disabled', false);
					$('#smtpUsername').focus();
				} else {
					$('#smtpUsernameFields').prop('disabled', true);
				}
			}

			function toggleLdapBindDnEnabled() {
				if ($('#useLdapAnonymousBind').is(':checked')) {
					$('#ldapBindDnFields').prop('disabled', true);
				} else {
					$('#ldapBindDnFields').prop('disabled', false);
					$('#ldapBindDn').focus();
				}
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/manual.htm#settings">
				<spring:message code="page.link.help"/>
			</a>
		</div>

		<div class="row">
			<div class="col-md-8 col-md-offset-2">
				<div class="panel panel-success">
					<div class="panel-heading text-center">
						<h4 class="panel-title text-center">${pageTitle}</h4>
					</div>
					<div class="panel-body">
						<form:form class="form-horizontal" method="POST" action="" modelAttribute="settings">
							<fieldset>
								<c:if test="${not empty success}">
									<div class="alert alert-success alert-dismissable">
										<a class="close" data-dismiss="alert" href="#">x</a>
										<spring:message code="settings.message.settingsSaved"/>
									</div>
								</c:if>
								<c:if test="${not empty formErrors}">
									<div class="alert alert-danger alert-dismissable">
										<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
										<spring:message code="page.message.formErrors"/>
									</div>
								</c:if>
								<c:if test="${not empty error}">
									<div class="alert alert-danger alert-dismissable">
										<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
										<p><spring:message code="page.message.errorOccurred"/></p>
										<p>${error}</p>
									</div>
								</c:if>

								<fieldset>
									<legend>SMTP</legend>
									<div class="form-group">
										<label class="control-label col-md-5" for="smtpServer">
											<spring:message code="settings.label.smtpServer"/>
										</label>
										<div class="col-md-7">
											<form:input path="smtpServer" id="smtpServer" class="form-control" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="smtpPort">
											<spring:message code="settings.label.smtpPort"/>
										</label>
										<div class="col-md-7">
											<form:input path="smtpPort" id="smtpPort" maxlength="6" class="form-control" />
											<form:errors path="smtpPort" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="smtpUseStartTls">
											<spring:message code="settings.label.smtpUseStartTls"/>
										</label>
										<div class="col-md-7">
											<div class="checkbox">
												<form:checkbox path="smtpUseStartTls"
															   id="smtpUseStartTls" />
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="useSmtpAuthentication">
											<spring:message code="settings.label.useSmtpAuthentication"/>
										</label>
										<div class="col-md-7">
											<div class="checkbox">
												<form:checkbox path="useSmtpAuthentication"
															   id="useSmtpAuthentication" />
											</div>
										</div>
									</div>
									<fieldset id="smtpUsernameFields">
										<div class="form-group">
											<label class="control-label col-md-5" for="smtpUsername">
												<spring:message code="settings.label.smtpUsername"/>
											</label>
											<div class="col-md-7">
												<div class="input-group">
													<form:input path="smtpUsername" id="smtpUsername" class="form-control" />
													<spring:message code="settings.help.smtpUsername" var="help" />
													<span class="input-group-btn" >
														<button class="btn btn-default" type="button"
																data-toggle="tooltip" title="${help}">
															<i class="fa fa-info"></i>
														</button>
													</span>
												</div>
											</div>
										</div>
										<div class="form-group">
											<label class="control-label col-md-5" for="smtpPassword">
												<spring:message code="settings.label.smtpPassword"/>
											</label>
											<div class="col-md-7">
												<div class="input-group">
													<form:password path="smtpPassword" id="smtpPassword" autocomplete="off" class="form-control" />
													<spring:message code="page.help.password" var="help" />
													<span class="input-group-btn" >
														<button class="btn btn-default" type="button"
																data-toggle="tooltip" title="${help}">
															<i class="fa fa-info"></i>
														</button>
													</span>
												</div>
												<div class="checkbox">
													<label>
														<form:checkbox path="useBlankSmtpPassword" id="useBlankSmtpPassword" />
														<spring:message code="page.checkbox.useBlankPassword"/>
													</label>
												</div>
											</div>
										</div>
									</fieldset>
								</fieldset>

								<fieldset>
									<legend><spring:message code="settings.text.authentication"/></legend>
									<div class="form-group">
										<label class="control-label col-md-5" for="artAuthenticationMethod">
											<spring:message code="settings.label.artAuthenticationMethod"/>
										</label>
										<div class="col-md-7">
											<c:forEach var="artAuthenticationMethod" items="${artAuthenticationMethods}">
												<label class="radio-inline">
													<form:radiobutton path="artAuthenticationMethod"
																	  value="${artAuthenticationMethod}"/> ${artAuthenticationMethod.description}
												</label>
											</c:forEach>
										</div>
									</div>
									<hr>
									<div class="form-group">
										<label class="control-label col-md-5" for="windowsDomainController">
											<spring:message code="settings.label.windowsDomainController"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="windowsDomainController"
															id="windowsDomainController" class="form-control" />
												<spring:message code="settings.help.windowsDomainController" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="allowedWindowsDomains">
											<spring:message code="settings.label.allowedWindowsDomains"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="allowedWindowsDomains"
															id="allowedWindowsDomains" class="form-control" />
												<spring:message code="settings.help.allowedWindowsDomains" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<hr>
									<div class="form-group">
										<label class="control-label col-md-5" for="databaseAuthenticationDriver">
											<spring:message code="settings.label.databaseAuthenticationDriver"/>
										</label>
										<div class="col-md-7">
											<form:input path="databaseAuthenticationDriver"
														id="databaseAuthenticationDriver" class="form-control" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="databaseAuthenticationUrl">
											<spring:message code="settings.label.databaseAuthenticationUrl"/>
										</label>
										<div class="col-md-7">
											<form:input path="databaseAuthenticationUrl"
														id="databaseAuthenticationUrl" class="form-control" />
										</div>
									</div>
									<hr>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapServer">
											<spring:message code="settings.label.ldapServer"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="ldapServer" id="ldapServer" class="form-control" />
												<spring:message code="settings.help.ldapServer" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapPort">
											<spring:message code="settings.label.ldapPort"/>
										</label>
										<div class="col-md-7">
											<form:input path="ldapPort" id="ldapPort" class="form-control" />
											<form:errors path="ldapPort" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapServer">
											<spring:message code="settings.label.ldapConnectionEncryptionMethod"/>
										</label>
										<div class="col-md-7">
											<c:forEach var="ldapEncryptionMethod" items="${ldapConnectionEncryptionMethods}">
												<label class="radio-inline">
													<form:radiobutton path="ldapConnectionEncryptionMethod"
																	  value="${ldapEncryptionMethod}"/> ${ldapEncryptionMethod.description}
												</label>
											</c:forEach>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapUrl">
											<spring:message code="settings.label.ldapUrl"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="ldapUrl" id="ldapUrl" class="form-control" />
												<spring:message code="settings.help.ldapUrl" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapBaseDn">
											<spring:message code="settings.label.ldapBaseDn"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="ldapBaseDn" id="ldapBaseDn" class="form-control" />
												<spring:message code="settings.help.ldapBaseDn" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="useLdapAnonymousBind">
											<spring:message code="settings.label.useLdapAnonymousBind"/>
										</label>
										<div class="col-md-7">
											<div class="checkbox">
												<form:checkbox path="useLdapAnonymousBind"
															   id="useLdapAnonymousBind" />
											</div>
										</div>
									</div>
									<fieldset id="ldapBindDnFields">
										<div class="form-group">
											<label class="control-label col-md-5" for="ldapBindDn">
												<spring:message code="settings.label.ldapBindDn"/>
											</label>
											<div class="col-md-7">
												<div class="input-group">
													<form:input path="ldapBindDn" id="ldapBindDn" class="form-control" />
													<spring:message code="settings.help.ldapBindDn" var="help" />
													<span class="input-group-btn" >
														<button class="btn btn-default" type="button"
																data-toggle="tooltip" title="${help}">
															<i class="fa fa-info"></i>
														</button>
													</span>
												</div>
											</div>
										</div>
										<div class="form-group">
											<label class="control-label col-md-5" for="ldapBindPassword">
												<spring:message code="settings.label.ldapBindPassword"/>
											</label>
											<div class="col-md-7">
												<div class="input-group">
													<form:password path="ldapBindPassword" 
																   id="ldapBindPassword" autocomplete="off" class="form-control" />
													<spring:message code="page.help.password" var="help" />
													<span class="input-group-btn" >
														<button class="btn btn-default" type="button"
																data-toggle="tooltip" title="${help}">
															<i class="fa fa-info"></i>
														</button>
													</span>
												</div>
												<div class="checkbox">
													<label>
														<form:checkbox path="useBlankLdapBindPassword" id="useBlankLdapBindPassword" />
														<spring:message code="page.checkbox.useBlankPassword"/>
													</label>
												</div>
											</div>
										</div>
									</fieldset>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapUserIdAttribute">
											<spring:message code="settings.label.ldapUserIdAttribute"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="ldapUserIdAttribute" id="ldapUserIdAttribute" class="form-control" />
												<spring:message code="settings.help.ldapUserIdAttribute" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
											<form:errors path="ldapUserIdAttribute" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapAuthenticationMethod">
											<spring:message code="settings.label.ldapAuthenticationMethod"/>
										</label>
										<div class="col-md-7">
											<c:forEach var="ldapAuthenticationMethod" items="${ldapAuthenticationMethods}">
												<label class="radio-inline">
													<form:radiobutton path="ldapAuthenticationMethod"
																	  value="${ldapAuthenticationMethod}"/> ${ldapAuthenticationMethod.description}
												</label>
											</c:forEach>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="ldapRealm">
											<spring:message code="settings.label.ldapRealm"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="ldapRealm" id="ldapRealm" class="form-control" />
												<spring:message code="settings.help.ldapRealm" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
								</fieldset>

								<fieldset>
									<legend><spring:message code="settings.text.maxRows"/></legend>
									<div class="form-group">
										<label class="control-label col-md-5" for="maxRowsDefault">
											<spring:message code="settings.label.maxRowsDefault"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="maxRowsDefault" 
															id="maxRowsDefault" maxlength="6" class="form-control" />
												<spring:message code="settings.help.maxRowsDefault" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
											<form:errors path="maxRowsDefault" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="maxRowsSpecific">
											<spring:message code="settings.label.maxRowsSpecific"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="maxRowsSpecific" 
															id="maxRowsSpecific" class="form-control" />
												<spring:message code="settings.help.maxRowsSpecific" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
								</fieldset>

								<fieldset>
									<legend>PDF</legend>
									<div class="form-group">
										<label class="control-label col-md-5" for="pdfPageSize">
											<spring:message code="settings.label.pdfPageSize"/>
										</label>
										<div class="col-md-7">
											<c:forEach var="pageSize" items="${pdfPageSizes}">
												<label class="radio-inline">
													<form:radiobutton path="pdfPageSize"
																	  value="${pageSize}"/> ${pageSize.description}
												</label>
											</c:forEach>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="pdfFontName">
											<spring:message code="settings.label.pdfFontName"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="pdfFontName" id="pdfFontName" class="form-control" />
												<spring:message code="settings.help.pdfFontName" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="pdfFontFile">
											<spring:message code="settings.label.pdfFontFile"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="pdfFontFile" id="pdfFontFile" class="form-control" />
												<spring:message code="settings.help.pdfFontFile" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="pdfFontDirectory">
											<spring:message code="settings.label.pdfFontDirectory"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="pdfFontDirectory" id="pdfFontDirectory" class="form-control" />
												<spring:message code="settings.help.pdfFontDirectory" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="pdfFontEncoding">
											<spring:message code="settings.label.pdfFontEncoding"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="pdfFontEncoding" id="pdfFontEncoding" class="form-control" />
												<spring:message code="settings.help.pdfFontEncoding" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="pdfFontEmbedded">
											<spring:message code="settings.label.pdfFontEmbedded"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<div class="checkbox">
													<form:checkbox path="pdfFontEmbedded" id="pdfFontEmbedded" />
												</div>
												<spring:message code="settings.help.pdfFontEmbedded" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
								</fieldset>

								<fieldset>
									<legend><spring:message code="settings.text.general" /></legend>
									<div class="form-group">
										<label class="control-label col-md-5" for="administratorEmail">
											<spring:message code="settings.label.administratorEmail"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="administratorEmail" id="administratorEmail" class="form-control" />
												<spring:message code="settings.help.administratorEmail" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="dateFormat">
											<spring:message code="settings.label.dateFormat"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="dateFormat" id="dateFormat" class="form-control" />
												<spring:message code="settings.help.dateFormat" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
											<form:errors path="dateFormat" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="timeFormat">
											<spring:message code="settings.label.timeFormat"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="timeFormat" id="timeFormat" class="form-control" />
												<spring:message code="settings.help.timeFormat" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
											<form:errors path="timeFormat" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="reportFormats">
											<spring:message code="settings.label.reportFormats"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="reportFormats" id="reportFormats" class="form-control" />
												<spring:message code="settings.help.reportFormats" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
											<form:errors path="reportFormats" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="displayNull">
											<spring:message code="settings.label.displayNull"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<c:forEach var="displayNullOption" items="${displayNullOptions}">
													<label class="radio-inline">
														<form:radiobutton path="displayNull"
																		  value="${displayNullOption}"/>
														<spring:message code="${displayNullOption.localisedDescription}" />
													</label>
												</c:forEach>
												<spring:message code="settings.help.displayNull" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="maxRunningReports">
											<spring:message code="settings.label.maxRunningReports"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="maxRunningReports" id="maxRunningReports" class="form-control" />
												<spring:message code="settings.help.maxRunningReports" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
											<form:errors path="maxRunningReports" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="showHeaderInPublicUserSession">
											<spring:message code="settings.label.showHeaderInPublicUserSession"/>
										</label>
										<div class="col-md-7">
											<div class="checkbox">
												<form:checkbox path="showHeaderInPublicUserSession"
															   id="showHeaderInPublicUserSession" />
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="mondrianCacheExpiryPeriod">
											<spring:message code="settings.label.mondrianCacheExpiryPeriod"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="mondrianCacheExpiryPeriod" id="mondrianCacheExpiryPeriod" class="form-control" />
												<spring:message code="settings.help.mondrianCacheExpiryPeriod" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
											<form:errors path="mondrianCacheExpiryPeriod" cssClass="error" />
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="schedulingEnabled">
											<spring:message code="settings.label.schedulingEnabled"/>
										</label>
										<div class="col-md-7">
											<div class="checkbox">
												<form:checkbox path="schedulingEnabled"
															   id="schedulingEnabled" />
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-md-5" for="rssLink">
											<spring:message code="settings.label.rssLink"/>
										</label>
										<div class="col-md-7">
											<div class="input-group">
												<form:input path="rssLink" id="rssLink" class="form-control" />
												<spring:message code="settings.help.rssLink" var="help" />
												<span class="input-group-btn" >
													<button class="btn btn-default" type="button"
															data-toggle="tooltip" title="${help}">
														<i class="fa fa-info"></i>
													</button>
												</span>
											</div>
										</div>
									</div>
								</fieldset>

								<div class="form-group">
									<div class="col-md-12">
										<button type="submit" class="btn btn-primary pull-right">
											<spring:message code="page.button.save"/>
										</button>
									</div>
								</div>
							</fieldset>
						</form:form>
					</div>
				</div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>
