delete from onboarding_details_admin where id>0;

delete from onboardiing_sub_category where id>0;

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('1', 'BeforeJoining-Call employee', 'BeforeJoining', 'Call employee');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('2', 'BeforeJoining-Socialization', 'BeforeJoining', 'Socialization');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('3', 'BeforeJoining-Work Environment', 'BeforeJoining', 'Work Environment');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('4', 'BeforeJoining-Technology Access and Related', 'BeforeJoining', 'Technology Access and Related');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('5', 'BeforeJoining-Training/Development', 'BeforeJoining', 'Schedule and Job Duties');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('6', 'AfterJoining-Schedule, Job Duties, and Expectation', 'AfterJoining', 'Schedule, Job Duties, and Expectations');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('7', 'AfterJoining-Socialization', 'AfterJoining', 'Socialization');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('8', 'AfterJoining-Work Environment', 'AfterJoining', 'Work Environment');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('9', 'AfterJoining-Technology Access and Related', 'AfterJoining', 'Technology Access and Related');

INSERT INTO `onboardiing_sub_category` (`id`, `composite_key`, `onboard_category`, `sub_category_name`) VALUES ('10', 'AfterJoining-Training/Development', 'AfterJoining', 'Training/Development');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('1', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Confirm start date, time, place, parking, etc</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Call employee', 'Confirm start date, time, place, parking, etc');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('2', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Identify computer needs and requiremets</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Call employee', 'Identify computer needs and requiremets');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('3', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Provide name of their on-boarding buddy</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Call employee', 'Provide  name of their on-boarding buddy');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('4', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Submit the Hire transaction</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Schedule and Job Duties', 'Submit the Hire transaction');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('5', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Prepare employee`s calendar for the first two weeks</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Schedule and Job Duties', 'Prepare employee`s calendar for the first two weeks');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('6', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Plan the employee`s first assignment</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Schedule and Job Duties', 'Plan the employee`s first assignment');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('7', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Email department/team/functional area of the new hire. Include start date, employee`s role, and bio. Copy the new employee, if appropriate</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Socialization', 'Email department/team/functional area of the new hire');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('8', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Set up meetings with critical people for the employee`s first few weeks</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Socialization', 'Set up meetings with critical people for the employee`s first few weeks');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('9', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Arrange for a campus tour</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Socialization', 'Arrange for a campus tour');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('10', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Clean the work area, and set up cube/office space with supplies</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Work Environment', 'Clean the work area, and set up cube/office space with supplies');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('11', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Order office or work area keys</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Work Environment', 'Order office or work area keys');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('12', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Order business cards and name plate</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Work Environment', 'Order business cards and name plate');



INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('13', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Order technology equipment (computer, printer, iPad) and software</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Technology Access and Related', 'Order technology equipment (computer, printer, iPad) and software');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('14', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Contact local IT and/or IS&T to have the system set up in advance</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Technology Access and Related', 'Contact local IT and/or IS&T to have the system set up in advance');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('15', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Arrange for phone installation</p>', 'BeforeJoining', 'sourav@beyondbytes.co.in', 'Technology Access and Related', 'Arrange for phone installation');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('16', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Clarify the first week`s schedule, and confirm required and recommended training</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Schedule, Job Duties, and Expectations', 'Clarify the first week`s schedule, and confirm required and recommended training');

INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('17', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Provide an overview of the functional area – its purpose, organizational structure, and goals</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Schedule, Job Duties, and Expectations', 'Provide an overview of the functional area');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('18', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Review job description, outline of duties, and expectations</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Schedule, Job Duties, and Expectations', 'Review job description, outline of duties, and expectations');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('19', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Be available to greet the employee on the first day</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Socialization', 'Be available to greet the employee on the first day');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('20', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Introduce employee to others in the workplace</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Socialization', 'Introduce employee to others in the workplace');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('21', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Give employee key(s) and building access card</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Work Environment', 'Give employee key(s) and building access card');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('22', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Take employee on a campus tour</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Work Environment', 'Take employee on a campus tour');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('23', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Explain how to get additional supplies</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Work Environment', 'Explain how to get additional supplies');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('24', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Ensure employee has fully functioning computer and systems access and understands how to use them</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Technology Access and Related', 'Ensure employee has all resources');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('25', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Provide information on setting up voicemail and computer</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Technology Access and Related', 'Provide information on setting up voicemail and computer');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES
('26', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Remind employee to sign up for an in-person New Employee Orientation session</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Training/Development', 'Remind employee to sign up for an in-person New Employee Orientation session');


INSERT INTO `onboarding_details_admin` (`id`, `creation_date`, `modification_date`, `description`, `onboard_category`, `owner`, `sub_category_name`, `title`) VALUES 
('27', '2018-03-12 06:31:37', '2018-03-12 06:31:37', '<p>Remind employee to sign up for an in-person New Employee Orientation session</p>', 'AfterJoining', 'sourav@beyondbytes.co.in', 'Training/Development', 'Arrange pertinent trainings required for the job');