DELETE from `email_template_category_variable`  where id in ('43', '44','45','46','47');

INSERT INTO `email_template_category_variable` (`id`, `categoryName`, `categoryValue`, `variable`) VALUES ('43', 'apply', 'Application Responses', '${emailSignature}');

INSERT INTO `email_template_category_variable` (`id`, `categoryName`, `categoryValue`, `variable`) VALUES ('44', 'interviewer', 'Interviewer Templates', '${emailSignature}');

INSERT INTO `email_template_category_variable` (`id`, `categoryName`, `categoryValue`, `variable`) VALUES ('45', 'email', 'Candidate Email', '${emailSignature}');

INSERT INTO `email_template_category_variable` (`id`, `categoryName`, `categoryValue`, `variable`) VALUES ('46', 'forward', 'Forward Profile', '${emailSignature}');

INSERT INTO `email_template_category_variable` (`id`, `categoryName`, `categoryValue`, `variable`) VALUES ('47', 'interview', 'Candidate Interview', '${emailSignature}');