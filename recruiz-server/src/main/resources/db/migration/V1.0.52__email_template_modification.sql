UPDATE `email_template_data` SET `body`='<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"\n width=\"560\" style=\"border-collapse: collapse; border-spacing: 0; padding: 0; width: inherit;\n max-width: 560px;\" class=\"wrapper\">\n\n <tr>\n  <td align=\"center\" valign=\"top\" style=\"border-collapse: collapse; border-spacing: 0; margin: 0; padding: 0; padding-left: 6.25%; padding-right: 6.25%; width: 87.5%;\n   padding-top: 20px;\n   padding-bottom: 20px;\">\n\n   <!-- PREHEADER -->\n   <!-- Set text color to background color -->\n   <div style=\"display: none; visibility: hidden; overflow: hidden; opacity: 0; font-size: 1px; line-height: 1px; height: 0; max-height: 0; max-width: 0;\n   color: #F0F0F0;\" class=\"preheader\"></div>\n\n   <!-- LOGO -->\n   <!-- Image text color should be opposite to background color. Set your url, image src, alt and title. Alt text should fit the image size. Real image size should be x2. URL format: http://domain.com/?utm_source={{Campaign-Source}}&utm_medium=email&utm_content=logo&utm_campaign={{Campaign-Name}} -->\n   <a target=\"_blank\" style=\"text-decoration: none;\" href=\"${orgWebsite}\">\n    <!-- <img border=\"0\" vspace=\"0\" hspace=\"0\"\n    src=\"${logo}\"\n    width=\"100\" height=\"30\"\n    alt=\"${orgName}\" title=\"Logo\" style=\"\n    color: #000000;\n    font-size: 10px; margin: 0; padding: 0; outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; border: none; display: block;\" /> -->\n    <h3 style=\"font-size: 20px; color:#29bbb0;\">${orgName}</h3>\n    </a>\n\n  </td>\n </tr>\n\n<!-- End of WRAPPER  -->\n</table>' WHERE `id`='18';