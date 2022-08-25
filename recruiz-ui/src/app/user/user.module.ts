import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from './../material-module/material.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UserComponent } from './user.component';
import { UserRoutingModule } from './user-routing.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { SidebarComponent } from './sidebar/sidebar.component';
//pipe filter
import { MyFilterPipe } from './filter-pipe/filter-pipes.component';
import { TeamMemberFilterPipe } from './filter-pipe/filter-team-member.component';
import { UserManagementFilterPipe } from './filter-pipe/filter-user-management.component';
import { OrderByPipe } from './filter-pipe/orderBy-pipes.component';
import { MAT_DATE_LOCALE } from '@angular/material/core'

//pagination
import { NgxPaginationModule } from 'ngx-pagination';

//directives
import { NumberDirective } from './directive/numbers-only.directive';

// Import your AvatarModule
import { AvatarModule } from 'ngx-avatar';

//copt to clipboad
import { ClipboardModule } from 'ngx-clipboard';

import { CKEditorModule } from '@ckeditor/ckeditor5-angular';
import { GooglePlaceModule } from "ngx-google-places-autocomplete";

import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ClientListComponent } from './clients/client-list/client-list.component';
import { ClientDetailComponent } from './clients/client-detail/client-detail.component';
import { SubHeaderComponent } from './sub-header/sub-header.component';

//pipeline
import { PipelineComponent } from './pipeline/pipeline/pipeline.component';
import { AddEditStagesDialog } from './pipeline/pipeline-modals/add-edit-stages.component';
import { AddExistingCandidateDialog } from './pipeline/pipeline-modals/add-existing-candidate.component';
import { ScheduleInterviewDialog } from './pipeline/pipeline-modals/schedule-interview.componen';

import { PipelineSubHeaderComponent } from './pipeline/pipeline-sub-header/pipeline-sub-header.component';
import { PipelineSourcingComponent } from './pipeline/pipeline-sourcing/pipeline-sourcing.component';
import { CandidateListComponent } from './Candidate/candidate-list/candidate-list.component';
import { CandidateDetailComponent } from './Candidate/candidate-detail/candidate-detail.component';
import { ProspectListComponent } from './Prospect/prospect-list/prospect-list.component';
import { ProspectDetailComponent } from './Prospect/prospect-detail/prospect-detail.component';
import { TasksComponent } from './tasks/tasks.component';
import { CalendarComponent } from './calendar/calendar.component';
import { NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { CalendarModule } from 'angular-calendar';
import { FlatpickrModule } from 'angularx-flatpickr';
import { ModalComponent } from './modal/modal.component';
import { CandidateFolderComponent } from './candidate-folder/candidate-folder.component';
import { EmployeeComponent } from './employee/employee.component';
import { PositionsListComponent } from './positions/positions-list/positions-list.component';
import { PositionsDetailComponent } from './positions/positions-detail/positions-detail.component';
//modals
import { InterviwerPannelDialog } from '../user/positions/modals/interviewer/interviwer-pannel.component';
import { HrPannelDialog } from '../user/positions/modals/hr-pannel/hr-pannel.component';
import { VendorPannelDialog } from '../user/positions/modals/vendors/vendor.component';
import { PositionNoteslDialog } from '../user/positions/modals/notes/notes.component';

import { ClientDecisionMakerlDialog } from '../user/clients/modals/decision-maker/decision-maker.component';
import { ClientInterviwerPannelDialog } from '../user/clients/modals/client-interviewer/client-interviwer-pannel.component';
import { ClientNoteslDialog } from '../user/clients/modals/client-notes/client-notes.component';
import { ClientRateslDialog } from '../user/clients/modals/client-rate/client-rate.component';

import { CandidateFolderlDialog } from '../user/candidate-folder/modals/candidate-folder/candidate-folder-modal.component';
import { CandidateShareFolderlDialog } from '../user/candidate-folder/modals/candidate-share-folder/candidate-share-folder-modal.component';
import { AddCandidateFolderlDialog } from '../user/candidate-folder/modals/add-candidate/add-candidate-folder-modal.component';
import { AddClientComponent } from '../user/clients/add-client/add-client.component';

import { CandidateResumelDialog } from '../user/Candidate/modals/candidate-resume/candidate-resume-dialog.component';

//admin
import { SettingManagementComponent } from '../user/admin/settings/setting-management/setting-management.component';
import { BankDetailDialog } from '../user/admin/settings/setting-modals/bank-detail.component';
import { TaxDetailDialog } from '../user/admin/settings/setting-modals/tax-detail.component';
import { CommonHeaderComponent } from '../user/admin/common-header/common-header.component';

import { UserManagementComponent } from '../user/admin/user-management/user-management/user-management.component';
import { UserBulkUploadDialog } from './admin/user-management/user-modal/user-upload.component';
import { InviteUserDialog } from './admin/user-management/user-modal/invite-user.component';
import { AssignUserDialog } from './admin/user-management/user-modal/assign-user.component';
import { TeamHierarchyDialog } from './admin/user-management/user-modal/team-hierarchy.component';
import { AddEditInterviewerDialog } from './admin/user-management/user-modal/add-edit-interviewer.component';
import { AddEditVendorDialog } from './admin/user-management/user-modal/add-edit-vendor.component';
import { AddVendorUserDialog } from './admin/user-management/user-modal/add-vendor-user.component';
import { AddDepartmetHeadUserDialog } from './admin/user-management/user-modal/add-department-user.component';

import { RoleManagementComponent } from './admin/role-management/role-management/role-management.component';
import { PermissionManagementComponent } from './admin/permission-management/permission-management/permission-management.component';
import { IntegrationComponent } from './admin/integration/integration/integration.component';
import { OnboardingComponent } from './admin/onboarding/onboarding/onboarding.component';
import { OnboardSubcategoryDialog } from './admin/onboarding/onboard-modals/add-new-category.component';

import { TeamManagementComponent } from './admin/team-management/team-management/team-management.component';
import { AddEditTeamDialog } from './admin/team-management/team-modals/add-edit-team.component';
import { AddMemberDialog } from './admin/team-management/team-modals/add-member.component';
import { TeamInfoDialog } from './admin/team-management/team-modals/team-info.component';
import { AddPositionComponent } from './positions/add-position/add-position.component';
@NgModule({
    declarations: [
        MyFilterPipe,
        TeamMemberFilterPipe,
        UserManagementFilterPipe,
        OrderByPipe,
        UserComponent,
        HeaderComponent,
        FooterComponent,
        SidebarComponent,
        DashboardComponent,
        ClientListComponent,
        ClientDetailComponent,
        SubHeaderComponent,
        PositionsListComponent,
        PositionsDetailComponent,
        PipelineComponent,
        PipelineSubHeaderComponent,
        PipelineSourcingComponent,
        CandidateListComponent,
        CandidateDetailComponent,
        ProspectListComponent,
        ProspectDetailComponent,
        TasksComponent,
        CalendarComponent,
        ModalComponent,
        CandidateFolderComponent,
        EmployeeComponent,
        InterviwerPannelDialog,
        HrPannelDialog,
        VendorPannelDialog,
        PositionNoteslDialog,
        ClientDecisionMakerlDialog,
        ClientInterviwerPannelDialog,
        ClientNoteslDialog,
        ClientRateslDialog,
        CandidateFolderlDialog,
        CandidateShareFolderlDialog,
        AddCandidateFolderlDialog,
        AddClientComponent,
        CandidateResumelDialog,
        SettingManagementComponent,
        CommonHeaderComponent,
        BankDetailDialog,
        TaxDetailDialog,
        UserManagementComponent,
        RoleManagementComponent,
        PermissionManagementComponent,
        IntegrationComponent,
        OnboardingComponent,
        TeamManagementComponent,
        OnboardSubcategoryDialog,
        AddEditTeamDialog,
        AddMemberDialog,
        TeamInfoDialog,
        UserBulkUploadDialog,
        InviteUserDialog,
        AssignUserDialog,
        TeamHierarchyDialog,
        AddEditInterviewerDialog,
        AddEditVendorDialog,
        AddVendorUserDialog,
        AddDepartmetHeadUserDialog,
        AddEditStagesDialog,
        AddExistingCandidateDialog,
        ScheduleInterviewDialog,
        NumberDirective,
        AddPositionComponent
    ],
    imports: [
        CommonModule,
        UserRoutingModule,
        MaterialModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule,
        NgbModalModule,
        NgxPaginationModule,
        ClipboardModule,
        AvatarModule,
        FlatpickrModule.forRoot(),
        CalendarModule.forRoot({ provide: DateAdapter, useFactory: adapterFactory }),
        CKEditorModule,
        GooglePlaceModule
    ],
    providers: [
    // { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    // { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class UserModule { }
