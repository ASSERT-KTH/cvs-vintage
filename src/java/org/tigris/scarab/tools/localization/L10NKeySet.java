package org.tigris.scarab.tools.localization;

/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */

/**
 * This class defines the pool of <code>Localizationkey</code>s, which are used
 * within the java-code of Scarab.
 * <p>
 * <code>Localizationkey</code>s are used in conjunction with 
 * <code>LocalizableMessage</code>s to transport l10n enabled messages instead
 * of simple message strings.
 * 
 * The first part of the keyset defines keys to resources for exception messages.
 * The second part of the keyset defines keys to arbitrary resources used all
 * over the java-code.
 * 
 * Note: The velocity templates use much more resource keys, which are NOT
 * defined here, because they are not relevant for the java-part of this software.
 * 
 * @version $Id: L10NKeySet.java,v 1.3 2004/06/04 22:08:39 dabbous Exp $
 *
 * Default implementation of a Localization Key.
 * <p>
 * Acts as a simple wrapper around the real key. Instances are immutable.
 * 
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 */
public class L10NKeySet
{
    
    public static final LocalizationKey ExceptionIntegrityCheckFailure       = new L10NKey("ExceptionIntegrityCheckFailure");
    public static final LocalizationKey ExceptionDuplicateIssueType          = new L10NKey("ExceptionDuplicateissueType");
    public static final LocalizationKey ExceptionGeneral                     = new L10NKey("ExceptionGeneral");
    public static final LocalizationKey ExceptionRetrievingIssueId           = new L10NKey("ExceptionRetrievingIssueId");
    public static final LocalizationKey ExceptionMultipleProblems            = new L10NKey("ExceptionMultipleProblems");
    public static final LocalizationKey ExceptionNoAttributePermission       = new L10NKey("ExceptionNoAttributePermission");
    public static final LocalizationKey ExceptionLucene                      = new L10NKey("ExceptionLucene");
    public static final LocalizationKey ExceptionParseError                  = new L10NKey("ExceptionParseError");
    public static final LocalizationKey ExceptionMultipleAttValues           = new L10NKey("ExceptionMultipleAttValues");
    public static final LocalizationKey ExceptionMultipleAttachements        = new L10NKey("ExceptionMultipleAttachments");
    public static final LocalizationKey ExceptionMultipleJDMs                = new L10NKey("ExceptionMultipleJDMs");
    public static final LocalizationKey ExceptionNullIssueForbidden          = new L10NKey("ExceptionNullIssueForbidden");
    public static final LocalizationKey ExceptionNullReportForbidden         = new L10NKey("ExceptionNullReportForbidden");
    public static final LocalizationKey ExceptionForbiddenHomeModuleNotReady = new L10NKey("ExceptionForbiddenHomeModuleNotReady");
    public static final LocalizationKey ExceptionNeedToSaveAttachement       = new L10NKey("ExceptionNeedToSaveAttachement");
    public static final LocalizationKey ExceptionActivitySetTypenameNotFound = new L10NKey("ExceptionActivitySetTypenameNotFoud");
    public static final LocalizationKey ExceptionActivitySetDuplicateTypename= new L10NKey("ExceptionActivitySetDuplicateTypename");
    public static final LocalizationKey ExceptionPathNotSet                  = new L10NKey("ExceptionPathNotSet");
    public static final LocalizationKey ExceptionAttachementDuplicateTypename= new L10NKey("ExceptionAttachementDuplicateTypename");
    public static final LocalizationKey ExceptionGroupDeleteForbidden        = new L10NKey("ExceptionGroupDeleteForbidden");
    public static final LocalizationKey ExceptionDuplicateAttributeTypeName  = new L10NKey("ExceptionDuplicateAttributeTypeName");
    public static final LocalizationKey ExceptionCantChainAttributeValues    = new L10NKey("ExceptionCantChainAttributeValues");
    public static final LocalizationKey ExceptionCantChainIssues             = new L10NKey("ExceptionCantChainIssues");
    public static final LocalizationKey ExceptionCanNotStartActivitySet      = new L10NKey("ExceptionCanNotStartActivitySet");
    public static final LocalizationKey ExceptionActivitySetInProgress       = new L10NKey("ExceptionActivitySetInProgress");
    public static final LocalizationKey ExceptionGetUserIdsNotImplemented    = new L10NKey("ExceptionGetUserIdsNotImplemented");
    public static final LocalizationKey ExceptionCanNotSaveAttributeValue    = new L10NKey("ExceptionCanNotSaveAttributeValue");
    public static final LocalizationKey ExceptionModuleAllreadyExists        = new L10NKey("ExceptionModuleAllreadyExists");
    public static final LocalizationKey ExceptionTorqueGeneric               = new L10NKey("ExceptionTorqueGeneric");
    public static final LocalizationKey ExceptionGeneric                     = new L10NKey("ExceptionGeneric");
    public static final LocalizationKey ExceptionTopLevelModuleWithoutCode   = new L10NKey("ExceptionTopLevelModuleWithoutCode");
    public static final LocalizationKey ExceptionCantPropagateModuleCode     = new L10NKey("ExceptionCantPropagateModuleCode");
    public static final LocalizationKey ExceptionSaveNeedsOwner              = new L10NKey("ExceptionSaveNeedsOwner");
    public static final LocalizationKey ExceptionDependInternalWorkflow      = new L10NKey("ExceptionDependInternalWorkflow");
    public static final LocalizationKey ExceptionMultipleVoteForUnallowed    = new L10NKey("ExceptionMultipleVoteForUnallowed");
    public static final LocalizationKey ExceptionCommentSavedButErrors       = new L10NKey("ExceptionCommentSavedButErrors");
    public static final LocalizationKey ExceptionSavedButErrors              = new L10NKey("ExceptionSavedButErrors");
    public static final LocalizationKey ExceptionTemplateTypeForIssueType    = new L10NKey("ExceptionTemplateTypeForIssueType");    
    public static final LocalizationKey ExceptionInvalidIssueType            = new L10NKey("ExceptionInvalidIssueType");
    public static final LocalizationKey ExceptionMultipleReports             = new L10NKey("ExceptionMultipleReports");
    public static final LocalizationKey ExceptionIncompatibleIssueIds        = new L10NKey("ExceptionIncompatibleIssueIds");
    public static final LocalizationKey ExceptionMaxdateBeforeMindate        = new L10NKey("ExceptionMaxdateBeforeMindate");
    public static final LocalizationKey ExceptionSearchIsNotAllowed          = new L10NKey("ExceptionSearchIsNotAllowed");
    public static final LocalizationKey ExceptionOptionNotFound              = new L10NKey("ExceptionOptionNotFound");
    public static final LocalizationKey ExceptionDuplicateUsername           = new L10NKey("ExceptionDuplicateUsername");
    public static final LocalizationKey ExceptionQueryTooComplex             = new L10NKey("ExceptionQueryTooComplex");
    public static final LocalizationKey ExceptionDeleteOptionFromLockedIssueType = new L10NKey("ExceptionDeleteOptionFromLockedIssueType");
    public static final LocalizationKey ExceptionFailedToReadIdentifierList  = new L10NKey("ExceptionFailedToReadIdentifierList");
    public static final LocalizationKey ExceptionFailedToDeleteIdentifierList= new L10NKey("ExceptionFailedToDeleteIdentifierList");
    public static final LocalizationKey ExceptionEmailFailure                = new L10NKey("ExceptionEmailFailure");
    public static final LocalizationKey ExceptionIncompatibleMITListChanges  = new L10NKey("ExceptionIncompatibleMITListChanges");
    public static final LocalizationKey ExceptionMaxConcurrentSearch         = new L10NKey("ExceptionMaxConcurrentSearch");    
    public static final LocalizationKey ExceptionInstantiation               = new L10NKey("ExceptionInstantiation");
    public static final LocalizationKey ExceptionIllegalAccess               = new L10NKey("ExceptionIllegalAccess");
    public static final LocalizationKey ExceptionClassNotFound               = new L10NKey("ExceptionClassNotFound");
  
    
    public static final LocalizationKey ScarabBundle                         = new L10NKey("ScarabBundle");
    public static final LocalizationKey NoDataInComment                      = new L10NKey("NoDataInComment");
    public static final LocalizationKey CouldNotSendEmail                    = new L10NKey("CouldNotSendEmail");
    public static final LocalizationKey DependencyExists                     = new L10NKey("DependencyExists");
    public static final LocalizationKey UrlDescChangedDesc                   = new L10NKey("UrlDescChangedDesc");
    public static final LocalizationKey UrlChangedDesc                       = new L10NKey("UrlChangedDesc");
    public static final LocalizationKey CannotDeleteAttributeFromLockedIssueType = new L10NKey("CannotDeleteAttributeFromLockedIssueType");
    public static final LocalizationKey YouDoNotHavePermissionToAction       = new L10NKey("YouDoNotHavePermissionToAction");
    public static final LocalizationKey IssueTypeWasNull                     = new L10NKey("IssueTypeWasNull");
    public static final LocalizationKey AttributesContainsNull               = new L10NKey("AttributesContainsNull");
    public static final LocalizationKey AttributeMappingIsMissing            = new L10NKey("AttributeMappingIsMissing");
    public static final LocalizationKey AttributeToIssueTypeMappingIsMissing = new L10NKey("AttributeToIssueTypeMappingIsMissing");
    public static final LocalizationKey ListOfOptionsMissing                 = new L10NKey("ListOfOptionsMissing");
    public static final LocalizationKey ErrorProcessingQuery                 = new L10NKey("ErrorProcessingQuery");
    public static final LocalizationKey YourChangesWereSaved                 = new L10NKey("YourChangesWereSaved");
    public static final LocalizationKey MoreInformationWasRequired           = new L10NKey("MoreInformationWasRequired");
    public static final LocalizationKey NoChangesMade                        = new L10NKey("NoChangesMade");
    public static final LocalizationKey AttachmentDeletedDesc                = new L10NKey("AttachmentDeletedDesc");
    public static final LocalizationKey FileDeletedDesc                      = new L10NKey("FileDeletedDesc");
    public static final LocalizationKey FileNotDeletedDesc                   = new L10NKey("FileNotDeletedDesc");
    
    // The following set is originally been generated from the java sources.
    
    public static final LocalizationKey PasswordsDoNotMatch = new L10NKey("PasswordsDoNotMatch");
    public static final LocalizationKey NavIssueTypeLimit = new L10NKey("NavIssueTypeLimit");
    public static final LocalizationKey EditModuleAttribute = new L10NKey("EditModuleAttribute");
    public static final LocalizationKey MustModifyAttribute = new L10NKey("MustModifyAttribute");
    public static final LocalizationKey ManageIssueTypes = new L10NKey("ManageIssueTypes");
    public static final LocalizationKey RoleDeleted = new L10NKey("RoleDeleted");
    public static final LocalizationKey InsufficientPermissionsToEnterIssues = new L10NKey("InsufficientPermissionsToEnterIssues");
    public static final LocalizationKey NoGroupSelected = new L10NKey("NoGroupSelected");
    public static final LocalizationKey AccountConfirmedSuccess = new L10NKey("AccountConfirmedSuccess");
    public static final LocalizationKey ApplicationErrorListWasNull = new L10NKey("ApplicationErrorListWasNull");
    public static final LocalizationKey NoIssueTypeList = new L10NKey("NoIssueTypeList");
    public static final LocalizationKey SelectedUsersWereAdded = new L10NKey("SelectedUsersWereAdded");
    public static final LocalizationKey RecursiveParentChildRelationship = new L10NKey("RecursiveParentChildRelationship");
    public static final LocalizationKey DateFormatPrompt = new L10NKey("DateFormatPrompt");
    public static final LocalizationKey IssueChangeCollision = new L10NKey("IssueChangeCollision");
    public static final LocalizationKey IssueTypeUnavailable = new L10NKey("IssueTypeUnavailable");
    public static final LocalizationKey ReportUpdatedDoMore = new L10NKey("ReportUpdatedDoMore");
    public static final LocalizationKey Deny = new L10NKey("Deny");
    public static final LocalizationKey AllRolesProcessed = new L10NKey("AllRolesProcessed");
    public static final LocalizationKey userCreated = new L10NKey("userCreated");
    public static final LocalizationKey ReportNameNotUnique = new L10NKey("ReportNameNotUnique");
    public static final LocalizationKey LockedIssueType = new L10NKey("LockedIssueType");
    public static final LocalizationKey SearchIndexDoNoteLeavePage = new L10NKey("SearchIndexDoNoteLeavePage");
    public static final LocalizationKey HeadingRemoved = new L10NKey("HeadingRemoved");
    public static final LocalizationKey NoPredefinedXModuleListSelected = new L10NKey("NoPredefinedXModuleListSelected");
    public static final LocalizationKey IssueTypeNameExists = new L10NKey("IssueTypeNameExists");
    public static final LocalizationKey AttributeGroups = new L10NKey("AttributeGroups");
    public static final LocalizationKey NoIssuesSelectedToAddComment = new L10NKey("NoIssuesSelectedToAddComment");
    public static final LocalizationKey CannotCreateDuplicateAttribute = new L10NKey("CannotCreateDuplicateAttribute");
    public static final LocalizationKey ResubmitError = new L10NKey("ResubmitError");
    public static final LocalizationKey UrlDeleted = new L10NKey("UrlDeleted");
    public static final LocalizationKey IssueTypeListMoreThanOne = new L10NKey("IssueTypeListMoreThanOne");
    public static final LocalizationKey EnterIssues = new L10NKey("EnterIssues");
    public static final LocalizationKey CannotDeleteIssueTypesWithIssues = new L10NKey("CannotDeleteIssueTypesWithIssues");
    public static final LocalizationKey NumberItemsRemoved = new L10NKey("NumberItemsRemoved");
    public static final LocalizationKey SelectIssueType = new L10NKey("SelectIssueType");
    public static final LocalizationKey CannotMoveToSameModule = new L10NKey("CannotMoveToSameModule");
    public static final LocalizationKey SearchFieldPrompt = new L10NKey("SearchFieldPrompt");
    public static final LocalizationKey NoDateSelected = new L10NKey("NoDateSelected");
    public static final LocalizationKey ConfirmationCodeSent = new L10NKey("ConfirmationCodeSent");
    public static final LocalizationKey VoteForIssueAccepted = new L10NKey("VoteForIssueAccepted");
    public static final LocalizationKey ListWithAtLeastOneMITRequired = new L10NKey("ListWithAtLeastOneMITRequired");
    public static final LocalizationKey SelectIssueToMove = new L10NKey("SelectIssueToMove");
    public static final LocalizationKey HeadingTypeChanged = new L10NKey("HeadingTypeChanged");
    public static final LocalizationKey LoginToAccountWithPermissions = new L10NKey("LoginToAccountWithPermissions");
    public static final LocalizationKey MustSelectAtLeastOneAttribute = new L10NKey("MustSelectAtLeastOneAttribute");
    public static final LocalizationKey CannotPositionDuplicateCheckFirst = new L10NKey("CannotPositionDuplicateCheckFirst");
    public static final LocalizationKey SelectedDateDeleted = new L10NKey("SelectedDateDeleted");
    public static final LocalizationKey NoAttributeToEdit = new L10NKey("NoAttributeToEdit");
    public static final LocalizationKey EnterValidIssueId = new L10NKey("EnterValidIssueId");
    public static final LocalizationKey NewTemplateCreated = new L10NKey("NewTemplateCreated");
    public static final LocalizationKey EnterQuery = new L10NKey("EnterQuery");
    public static final LocalizationKey OnlySelectOneDestination = new L10NKey("OnlySelectOneDestination");
    public static final LocalizationKey EnterValidEmailAddress = new L10NKey("EnterValidEmailAddress");
    public static final LocalizationKey ReportUpdatedNotSavedDoMoreOrCalculate = new L10NKey("ReportUpdatedNotSavedDoMoreOrCalculate");
    public static final LocalizationKey AtLeastOneAttributeForTemplate = new L10NKey("AtLeastOneAttributeForTemplate");
    public static final LocalizationKey SelectedIssueTypesRemovedFromModule = new L10NKey("SelectedIssueTypesRemovedFromModule");
    public static final LocalizationKey userChangesSaved = new L10NKey("userChangesSaved");
    public static final LocalizationKey ThisAxisMustBeDatesUnlessHeadingsAreRemoved = new L10NKey("ThisAxisMustBeDatesUnlessHeadingsAreRemoved");
    public static final LocalizationKey CapModule = new L10NKey("CapModule");
    public static final LocalizationKey CouldNotSendNotification = new L10NKey("CouldNotSendNotification");
    public static final LocalizationKey ThisShouldNotHappenPleaseContactAdmin = new L10NKey("ThisShouldNotHappenPleaseContactAdmin");
    public static final LocalizationKey LostSessionStateError = new L10NKey("LostSessionStateError");
    public static final LocalizationKey InvalidData = new L10NKey("InvalidData");
    public static final LocalizationKey MissingXML = new L10NKey("MissingXML");
    public static final LocalizationKey HeadingAddedNowAddContent = new L10NKey("HeadingAddedNowAddContent");
    public static final LocalizationKey NoUsersSelected = new L10NKey("NoUsersSelected");
    public static final LocalizationKey EditGlobalUserAttribute = new L10NKey("EditGlobalUserAttribute");
    public static final LocalizationKey EnterNewIssueType = new L10NKey("EnterNewIssueType");
    public static final LocalizationKey FileAdded = new L10NKey("FileAdded");
    public static final LocalizationKey InsufficientPermissionsToViewIssues = new L10NKey("InsufficientPermissionsToViewIssues");
    public static final LocalizationKey SelectedGroupDeleted = new L10NKey("SelectedGroupDeleted");
    public static final LocalizationKey NotifyPendingApproval = new L10NKey("NotifyPendingApproval");
    public static final LocalizationKey AddAttributeGroup = new L10NKey("AddAttributeGroup");
    public static final LocalizationKey CouldNotLocateAttachmentGroup = new L10NKey("CouldNotLocateAttachmentGroup");
    public static final LocalizationKey SelectOption = new L10NKey("SelectOption");
    public static final LocalizationKey MultiIssueChangeCollision = new L10NKey("MultiIssueChangeCollision");
    public static final LocalizationKey CannotAddSelfDependency = new L10NKey("CannotAddSelfDependency");
    public static final LocalizationKey EditGlobalAttribute = new L10NKey("EditGlobalAttribute");
    public static final LocalizationKey IncompleteReportDefinition = new L10NKey("IncompleteReportDefinition");
    public static final LocalizationKey NotProvided = new L10NKey("NotProvided");
    public static final LocalizationKey UserAttributes = new L10NKey("UserAttributes");
    public static final LocalizationKey IssueList_vm_TitleWithQueryName = new L10NKey("IssueList.vm.TitleWithQueryName");
    public static final LocalizationKey SavedReportsMustHaveName = new L10NKey("SavedReportsMustHaveName");
    public static final LocalizationKey Query = new L10NKey("Query");
    public static final LocalizationKey EditIssueType = new L10NKey("EditIssueType");
    public static final LocalizationKey GroupAdded = new L10NKey("GroupAdded");
    public static final LocalizationKey NoValidIssuesCouldBeLocated = new L10NKey("NoValidIssuesCouldBeLocated");
    public static final LocalizationKey NoMatchingIssues = new L10NKey("NoMatchingIssues");
    public static final LocalizationKey ID = new L10NKey("ID");
    public static final LocalizationKey ViewIssue = new L10NKey("ViewIssue");
    public static final LocalizationKey MustSelectAtLeastOneIssueType = new L10NKey("MustSelectAtLeastOneIssueType");
    public static final LocalizationKey ReportUpdatedDoMoreOrCalculate = new L10NKey("ReportUpdatedDoMoreOrCalculate");
    public static final LocalizationKey PermissionExists = new L10NKey("PermissionExists");
    public static final LocalizationKey Unauthorized = new L10NKey("Unauthorized");
    public static final LocalizationKey NoHeadingSelected = new L10NKey("NoHeadingSelected");
    public static final LocalizationKey CannotDeleteSystemSpecifiedIssueType = new L10NKey("CannotDeleteSystemSpecifiedIssueType");
    public static final LocalizationKey PermissionDeleted = new L10NKey("PermissionDeleted");
    public static final LocalizationKey IssueTypeNotAvailable = new L10NKey("IssueTypeNotAvailable");
    public static final LocalizationKey FollowingIssueIdsAreInvalid = new L10NKey("FollowingIssueIdsAreInvalid");
    public static final LocalizationKey NoTextInCommentTextArea = new L10NKey("NoTextInCommentTextArea");
    public static final LocalizationKey SaveTemplate = new L10NKey("SaveTemplate");
    public static final LocalizationKey AddGlobalAttributeOptionToAttribute = new L10NKey("AddGlobalAttributeOptionToAttribute");
    public static final LocalizationKey IssueTypeAlreadyAssociated = new L10NKey("IssueTypeAlreadyAssociated");
    public static final LocalizationKey ConfirmFieldIsNullError = new L10NKey("ConfirmFieldIsNullError");
    public static final LocalizationKey FileDeleted = new L10NKey("FileDeleted");
    public static final LocalizationKey FilesPartiallyDeleted = new L10NKey("FilesPartiallyDeleted");
    public static final LocalizationKey RegisterSessionError = new L10NKey("RegisterSessionError");
    public static final LocalizationKey AssignUsersWithThisIssue = new L10NKey("AssignUsersWithThisIssue");
    public static final LocalizationKey CannotDetermineIssueEntryTemplate = new L10NKey("CannotDetermineIssueEntryTemplate");
    public static final LocalizationKey CannotZeroIssueType = new L10NKey("CannotZeroIssueType");
    public static final LocalizationKey ErrorOccurredCheckingMessage = new L10NKey("ErrorOccurredCheckingMessage");
    public static final LocalizationKey NoPermissionInParentModule = new L10NKey("NoPermissionInParentModule");
    public static final LocalizationKey EditTemplate = new L10NKey("EditTemplate");
    public static final LocalizationKey GlobalArtifactTypeCopied = new L10NKey("GlobalArtifactTypeCopied");
    public static final LocalizationKey ActionNotAssignedPermission = new L10NKey("ActionNotAssignedPermission");
    public static final LocalizationKey ChangesSavedButDefaultTextAttributeRequired = new L10NKey("ChangesSavedButDefaultTextAttributeRequired");
    public static final LocalizationKey ItemAlreadyRejected = new L10NKey("ItemAlreadyRejected");
    public static final LocalizationKey SelectIssueTypeToDeleteFromModule = new L10NKey("SelectIssueTypeToDeleteFromModule");
    public static final LocalizationKey UsernameGroupIsNullError = new L10NKey("UsernameGroupIsNullError");
    public static final LocalizationKey PermissionCreated = new L10NKey("PermissionCreated");
    public static final LocalizationKey UsernameExistsAlready = new L10NKey("UsernameExistsAlready");
    public static final LocalizationKey EmailHasBadDNS = new L10NKey("EmailHasBadDNS");
    public static final LocalizationKey NoUserAttributeSelected = new L10NKey("NoUserAttributeSelected");
    public static final LocalizationKey RoleExists = new L10NKey("RoleExists");
    public static final LocalizationKey InvalidEmailAddress = new L10NKey("InvalidEmailAddress");
    public static final LocalizationKey InvalidUsername = new L10NKey("InvalidUsername");
    public static final LocalizationKey ChoiceAlreadyAccountedAny = new L10NKey("ChoiceAlreadyAccountedAny");
    public static final LocalizationKey IncompatibleMITListReport = new L10NKey("IncompatibleMITListReport");
    public static final LocalizationKey ReportUpdatedNotSaved = new L10NKey("ReportUpdatedNotSaved");
    public static final LocalizationKey YourPasswordHasExpired = new L10NKey("YourPasswordHasExpired");
    public static final LocalizationKey EnterValidDependencyType = new L10NKey("EnterValidDependencyType");
    public static final LocalizationKey IssueSavedButEmailError = new L10NKey("IssueSavedButEmailError");
    public static final LocalizationKey NewModuleCreated = new L10NKey("NewModuleCreated");
    public static final LocalizationKey ModuleIssueTypeRequiredToEnterIssue = new L10NKey("ModuleIssueTypeRequiredToEnterIssue");
    public static final LocalizationKey ChangeOfTypeMessage = new L10NKey("ChangeOfTypeMessage");
    public static final LocalizationKey userSelect = new L10NKey("userSelect");
    public static final LocalizationKey IssueMoved = new L10NKey("IssueMoved");
    public static final LocalizationKey SelectModuleToWorkIn = new L10NKey("SelectModuleToWorkIn");
    public static final LocalizationKey SelectAttribute = new L10NKey("SelectAttribute");
    public static final LocalizationKey NoAttributeGroupSelected = new L10NKey("NoAttributeGroupSelected");
    public static final LocalizationKey DuplicateQueryName = new L10NKey("DuplicateQueryName");
    public static final LocalizationKey AttributeOptionAdded = new L10NKey("AttributeOptionAdded");
    public static final LocalizationKey DateAdded = new L10NKey("DateAdded");
    public static final LocalizationKey TemplateDeleted = new L10NKey("TemplateDeleted");
    public static final LocalizationKey UrlSaved = new L10NKey("UrlSaved");
    public static final LocalizationKey EditAttributeGroup = new L10NKey("EditAttributeGroup");
    public static final LocalizationKey IssueId = new L10NKey("IssueId");
    public static final LocalizationKey ReportUpdatedPleaseAddRowAndColumnCriteria = new L10NKey("ReportUpdatedPleaseAddRowAndColumnCriteria");
    public static final LocalizationKey userDeleteNotImplemented = new L10NKey("userDeleteNotImplemented");
    public static final LocalizationKey SelectedUsersWereRemoved = new L10NKey("SelectedUsersWereRemoved");
    public static final LocalizationKey ErrorExceptionMessage = new L10NKey("ErrorExceptionMessage");
    public static final LocalizationKey CouldNotMakeRequestedChange = new L10NKey("CouldNotMakeRequestedChange");
    public static final LocalizationKey NoUrlsChanged = new L10NKey("NoUrlsChanged");
    public static final LocalizationKey Defer = new L10NKey("Defer");
    public static final LocalizationKey IssueAddedToModule = new L10NKey("IssueAddedToModule");
    public static final LocalizationKey CommentAddedButEmailError = new L10NKey("CommentAddedButEmailError");
    public static final LocalizationKey CommentSaved = new L10NKey("CommentSaved");
    public static final LocalizationKey Author = new L10NKey("Author");
    public static final LocalizationKey PasswordSame = new L10NKey("PasswordSame");
    public static final LocalizationKey SelectedUsersWereModified = new L10NKey("SelectedUsersWereModified");
    public static final LocalizationKey ConfirmationSubject = new L10NKey("ConfirmationSubject");
    public static final LocalizationKey ReportUpdated = new L10NKey("ReportUpdated");
    public static final LocalizationKey ChangesSaved = new L10NKey("ChangesSaved");
    public static final LocalizationKey Attributes = new L10NKey("Attributes");
    public static final LocalizationKey IssueTypeAddedToModule = new L10NKey("IssueTypeAddedToModule");
    public static final LocalizationKey SelectModuleAndIssueType = new L10NKey("SelectModuleAndIssueType");
    public static final LocalizationKey CannotRequireAttributeWithNoOptions = new L10NKey("CannotRequireAttributeWithNoOptions");
    public static final LocalizationKey EnterId = new L10NKey("EnterId");
    public static final LocalizationKey DuplicateSequenceNumbersFound = new L10NKey("DuplicateSequenceNumbersFound");
    public static final LocalizationKey NoTemplateId = new L10NKey("NoTemplateId");
    public static final LocalizationKey VoteFailedException = new L10NKey("VoteFailedException");
    public static final LocalizationKey DeletedOptionsFromRequiredAttribute = new L10NKey("DeletedOptionsFromRequiredAttribute");
    public static final LocalizationKey InsufficientPermissionsToAssignIssues = new L10NKey("InsufficientPermissionsToAssignIssues");
    public static final LocalizationKey NoApproverAvailable = new L10NKey("NoApproverAvailable");
    public static final LocalizationKey RegisterGroupIsNullError = new L10NKey("RegisterGroupIsNullError");
    public static final LocalizationKey DuplicateGroupName = new L10NKey("DuplicateGroupName");
    public static final LocalizationKey PasswordChanged = new L10NKey("PasswordChanged");
    public static final LocalizationKey InvalidConfirmationCode = new L10NKey("InvalidConfirmationCode");
    public static final LocalizationKey NoAttributesSelected = new L10NKey("NoAttributesSelected");
    public static final LocalizationKey DuplicateTemplateName = new L10NKey("DuplicateTemplateName");
    public static final LocalizationKey CommentAdded = new L10NKey("CommentAdded");
    public static final LocalizationKey IssueTypeNotFound = new L10NKey("IssueTypeNotFound");
    public static final LocalizationKey TemplateModified = new L10NKey("TemplateModified");
    public static final LocalizationKey IssueLimitExceeded = new L10NKey("IssueLimitExceeded");
    public static final LocalizationKey ReportUpdatedNotSavedDoMore = new L10NKey("ReportUpdatedNotSavedDoMore");
    public static final LocalizationKey QueryModified = new L10NKey("QueryModified");
    public static final LocalizationKey Template = new L10NKey("Template");
    public static final LocalizationKey FileSaved = new L10NKey("FileSaved");
    public static final LocalizationKey GroupsChanged = new L10NKey("GroupsChanged");
    public static final LocalizationKey CreateNewGlobalUserAttribute = new L10NKey("CreateNewGlobalUserAttribute");
    public static final LocalizationKey ErrorPreventedSavingReport = new L10NKey("ErrorPreventedSavingReport");
    public static final LocalizationKey RoleRequestGranted = new L10NKey("RoleRequestGranted");
    public static final LocalizationKey NoActionSpecified = new L10NKey("NoActionSpecified");
    public static final LocalizationKey NoTemplateSelected = new L10NKey("NoTemplateSelected");
    public static final LocalizationKey InvalidGroupName = new L10NKey("InvalidGroupName");
    public static final LocalizationKey IssueType = new L10NKey("IssueType");
    public static final LocalizationKey NoCommentsChanged = new L10NKey("NoCommentsChanged");
    public static final LocalizationKey UserObjectNotInSession = new L10NKey("UserObjectNotInSession");
    public static final LocalizationKey NoAttributeSelected = new L10NKey("NoAttributeSelected");
    public static final LocalizationKey UserIsNotConfirmed = new L10NKey("UserIsNotConfirmed");
    public static final LocalizationKey CouldNotLocateTemplateToDelete = new L10NKey("CouldNotLocateTemplateToDelete");
    public static final LocalizationKey AttributeOptions = new L10NKey("AttributeOptions");
    public static final LocalizationKey RoleRequestAwaiting = new L10NKey("RoleRequestAwaiting");
    public static final LocalizationKey AccountConfirmedFailure = new L10NKey("AccountConfirmedFailure");
    public static final LocalizationKey QueryParserError = new L10NKey("QueryParserError");
    public static final LocalizationKey StateChangeOldEqualNew = new L10NKey("StateChangeOldEqualNew");
    public static final LocalizationKey ReportIsTooExpensive = new L10NKey("ReportIsTooExpensive");
    public static final LocalizationKey EditGlobalIssueType = new L10NKey("EditGlobalIssueType");
    public static final LocalizationKey ShortDateDisplay = new L10NKey("ShortDateDisplay");
    public static final LocalizationKey CannotDeleteIssueType = new L10NKey("CannotDeleteIssueType");
    public static final LocalizationKey InvalidUsernameOrPassword = new L10NKey("InvalidUsernameOrPassword");
    public static final LocalizationKey CreateNewIssueType = new L10NKey("CreateNewIssueType");
    public static final LocalizationKey HeadingTypeChangedOldDataDiscarded = new L10NKey("HeadingTypeChangedOldDataDiscarded");
    public static final LocalizationKey RoleCreated = new L10NKey("RoleCreated");
    public static final LocalizationKey InvalidId = new L10NKey("InvalidId");
    public static final LocalizationKey ReportUpdatedNotSavedPleaseAddRowAndColumnCriteria = new L10NKey("ReportUpdatedNotSavedPleaseAddRowAndColumnCriteria");
    public static final LocalizationKey IssueTypeRemovedFromModule = new L10NKey("IssueTypeRemovedFromModule");
    public static final LocalizationKey IssueTypes = new L10NKey("IssueTypes");
    public static final LocalizationKey NoFilesChanged = new L10NKey("NoFilesChanged");
    public static final LocalizationKey NoSavedXModuleQuerySelected = new L10NKey("NoSavedXModuleQuerySelected");
    public static final LocalizationKey VelocityUsersNotWrong = new L10NKey("VelocityUsersNotWrong");
    public static final LocalizationKey ScopeChangedToPersonal = new L10NKey("ScopeChangedToPersonal");
    public static final LocalizationKey UserAttributeRemoved = new L10NKey("UserAttributeRemoved");
    public static final LocalizationKey GlobalIssueTypesDeleted = new L10NKey("GlobalIssueTypesDeleted");
    public static final LocalizationKey ProcessingErrors = new L10NKey("ProcessingErrors");
    public static final LocalizationKey SystemSpecifiedIssueType = new L10NKey("SystemSpecifiedIssueType");
    public static final LocalizationKey CreateNewGlobalAttribute = new L10NKey("CreateNewGlobalAttribute");
    public static final LocalizationKey SearchIndexUpdated = new L10NKey("SearchIndexUpdated");
    public static final LocalizationKey LockedAttribute = new L10NKey("LockedAttribute");
    public static final LocalizationKey AttributeAdded = new L10NKey("AttributeAdded");
    public static final LocalizationKey CouldNotLocateModuleGroup = new L10NKey("CouldNotLocateModuleGroup");
    public static final LocalizationKey AnyHasReplacedPreviousChoices = new L10NKey("AnyHasReplacedPreviousChoices");
    public static final LocalizationKey userNotRetrieved = new L10NKey("userNotRetrieved");
    public static final LocalizationKey RolePreviouslyApprovedForUserInModule = new L10NKey("RolePreviouslyApprovedForUserInModule");
    public static final LocalizationKey ReasonRequired = new L10NKey("ReasonRequired");
    public static final LocalizationKey ReportSaved = new L10NKey("ReportSaved");
    public static final LocalizationKey CircularParentChildRelationship = new L10NKey("CircularParentChildRelationship");
    public static final LocalizationKey UserNotPossibleAssignee = new L10NKey("UserNotPossibleAssignee");
    public static final LocalizationKey ItemAlreadyApproved = new L10NKey("ItemAlreadyApproved");
    public static final LocalizationKey ChangesResultDuplicateNames = new L10NKey("ChangesResultDuplicateNames");
    public static final LocalizationKey NoItemsSelectedForRemoval = new L10NKey("NoItemsSelectedForRemoval");
    public static final LocalizationKey ModuleUpdated = new L10NKey("ModuleUpdated");
    public static final LocalizationKey InvalidIssueId = new L10NKey("InvalidIssueId");
    public static final LocalizationKey InitialEntry = new L10NKey("InitialEntry");
    public static final LocalizationKey NoPermissionInModule = new L10NKey("NoPermissionInModule");
    public static final LocalizationKey SelectIssues = new L10NKey("SelectIssues");

}
