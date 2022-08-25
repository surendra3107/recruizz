import { Component, OnInit } from '@angular/core';
import { UntypedFormControl, Validators, UntypedFormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

//service
import { AuthenticationService } from '../auth_service/authenticationService'

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent implements OnInit {

  returnUrl: string = '/web/dashboard';
  error = '';

  selectedSection: string = 'signIn';
  hide = true;
  userType: string = 'Corporate Name';

  email = new UntypedFormControl('', [Validators.required, Validators.email]);
  password = new UntypedFormControl('', [Validators.required]);
  name = new UntypedFormControl('', [Validators.required]);
  mobileNumber = new UntypedFormControl('', [Validators.required, Validators.minLength(10)]);
  signUpemail = new UntypedFormControl('', [Validators.required, Validators.email]);
  confirm = new UntypedFormControl('', [Validators.required]);
  signUpPassword = new UntypedFormControl('', [Validators.required]);

  signupForm: UntypedFormGroup;
  signinForm: UntypedFormGroup;


  constructor(private router: Router, private authenticationService: AuthenticationService) {}

  ngOnInit() {
    this.createSignupForm();
    this.createSigninForm();
  }

  toggleAuthDiv(value: string) {
    this.selectedSection = value;
    this.signupForm.reset();
    this.signinForm.reset();
  }

  public resolved(captchaResponse: string) {
    console.log(`Resolved captcha with response: ${captchaResponse}`);
  }

  getErrorMessage(type: string) {
    if (type == 'email') return this.email.hasError('required') ? '' : this.email.hasError('email') ? 'Not a valid email' : '';
    if (type == 'signUpemail') return this.signUpemail.hasError('required') ? '' : this.signUpemail.hasError('email') ? 'Please enter a valid email address' : '';
  }

  toggleSignup(type: string) {
    this.userType = type;
    this.signupForm.reset();
    this.signinForm.reset();
  }

  createSignupForm() {
    this.signupForm = new UntypedFormGroup({
      name: this.name,
      email: this.signUpemail,
      password: this.signUpPassword,
      confirmPassword: this.confirm,
      mobileNumber: this.mobileNumber
    });
  }

  createSigninForm() {
    this.signinForm = new UntypedFormGroup({
      email: this.email,
      password: this.password
    });
  }

  onSignIn() {
    if (this.signinForm.valid) {
      let username = this.signinForm.value.email;
      let password = this.signinForm.value.password;
      this.authenticationService.login(username, password).pipe(first()).subscribe(data => {
        console.log('RESPONSE : ' + JSON.stringify(data));
        if (data.success === true) {
          if (data.data.tenantType === 'multiple') {
            sessionStorage.setItem('currentUser', JSON.stringify(data.data));
            this.router.navigate(['/web/tenant']);
          } else {
           // sessionStorage.setItem('currentUser', JSON.stringify(data.data));
            this.router.navigate(['/user/dashboard']);
          }
        } else if (data.success === false) {
          Swal.fire({
            title: "Login failed...",
            text: data.data,
            type: 'warning',
            showConfirmButton: true
          });
        }
      },
        error => {
          this.error = error;
        });
    } else {
      return true;
    }
  }

  onSignup() {
    this.signupForm.reset();
  }

  goToSignIn() {
    this.selectedSection = 'signIn';
  }


}