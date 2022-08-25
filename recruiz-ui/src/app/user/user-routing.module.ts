import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { UserComponent } from './user.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ClientListComponent } from './clients/client-list/client-list.component';
import { ClientDetailComponent } from './clients/client-detail/client-detail.component';
import { PipelineComponent } from './pipeline/pipeline/pipeline.component';
import { PipelineSourcingComponent } from './pipeline/pipeline-sourcing/pipeline-sourcing.component';
import { CandidateListComponent } from './Candidate/candidate-list/candidate-list.component';
import { CandidateDetailComponent } from './Candidate/candidate-detail/candidate-detail.component';
import { ProspectListComponent } from './Prospect/prospect-list/prospect-list.component';
import { ProspectDetailComponent } from './Prospect/prospect-detail/prospect-detail.component';
import { TasksComponent } from './tasks/tasks.component';
import { CalendarComponent } from './calendar/calendar.component';
import { CandidateFolderComponent } from './candidate-folder/candidate-folder.component';
import { EmployeeComponent } from './employee/employee.component';
import { PositionsListComponent } from './positions/positions-list/positions-list.component';
import { PositionsDetailComponent } from './positions/positions-detail/positions-detail.component';
import { AddPositionComponent } from './positions/add-position/add-position.component';
import { AddClientComponent } from './clients/add-client/add-client.component';

//admin
import { SettingManagementComponent } from "./admin/settings/setting-management/setting-management.component";
import { UserManagementComponent } from "./admin/user-management/user-management/user-management.component";
import { RoleManagementComponent } from "./admin/role-management/role-management/role-management.component";
import { PermissionManagementComponent } from './admin/permission-management/permission-management/permission-management.component';
import { IntegrationComponent } from './admin/integration/integration/integration.component';
import { OnboardingComponent } from './admin/onboarding/onboarding/onboarding.component';
import { TeamManagementComponent } from './admin/team-management/team-management/team-management.component';

const user_routes: Routes = [
    {
        path:'',
        component:UserComponent,
        // canActivate: [AuthGuard],
        children:[
            {
                path:'dashboard',
                component:DashboardComponent
            },
            {
                path: 'prospects',
                component: ProspectListComponent
            },
            {
                path: 'peospect-detail',
                component: ProspectDetailComponent
            },
            {
                path: 'add-client',
                component: AddClientComponent
            },
            {
                path: 'client-list',
                component: ClientListComponent
            },
            {
                path: 'client-details',
                component: ClientDetailComponent
            },
            {
                path: 'add-position',
                component : AddPositionComponent
            },
            {
                path: 'positions-list',
                component : PositionsListComponent
            },
            {
                path: 'positions-detail',
                component : PositionsDetailComponent
            },
            {
                path: 'pipeline',
                component : PipelineComponent
            },
            {
                path : 'pipeline-sourcing',
                component : PipelineSourcingComponent
            },
            {
                path: 'candidate-list',
                component : CandidateListComponent
            },
            {
                path : 'candidate-detail',
                component : CandidateDetailComponent
            },
            {
                path : 'tasks',
                component : TasksComponent
            },
            {
                path : 'calendar',
                component : CalendarComponent
            },
            {
                path : 'candidate-folder',
                component : CandidateFolderComponent
            },
            {
                path : 'employees',
                component : EmployeeComponent
          },
          {
            path: 'admin/setting-management',
            component: SettingManagementComponent
          },
          {
            path: 'admin/user-management',
            component: UserManagementComponent
          },
          {
            path: 'admin/role-management',
            component: RoleManagementComponent
          },
          {
            path: 'admin/permission-management',
            component: PermissionManagementComponent
          },
          {
            path: 'admin/integration-management',
            component: IntegrationComponent
          },
          {
            path: 'admin/onboard-management',
            component: OnboardingComponent
          },
          {
            path: 'admin/team-management',
            component: TeamManagementComponent
          }
        ]
    }
];

@NgModule({
  imports: [RouterModule.forChild(user_routes)],
  exports: [RouterModule]
})
export class UserRoutingModule { }
