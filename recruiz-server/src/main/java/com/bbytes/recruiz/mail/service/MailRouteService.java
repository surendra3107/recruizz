package com.bbytes.recruiz.mail.service;

import org.springframework.stereotype.Service;

/*
 * We are using webhook pipe project https://github.com/auklabs/webhookpipe
 * to route webhook post request to recruiz. We dont need to create a route
 * thru api from recruiz . SO code is commented
 */

/**
 * All emails in this domain go thru webhook. Refer: //
 * http://blog.mailgun.com/tips-tricks-guide-to-using-regular-expressions-to
 * -filter-incoming-email/#match-recipient
 * 
 */

@Service
public class MailRouteService extends AbstractMailgunService {

//	private static final Logger logger = LoggerFactory.getLogger(MailRouteService.class);
//
//	@Autowired
//	private RouteModelService routeModelService;
//
//	String webhookURL = null;
//
//	@PostConstruct
//	private void initWebhooks() {
//		webhookURL = baseURL + MailConstant.MAIL_WEBHOOK_URL;
//		logger.info("Webhook URL is " + webhookURL);
//		String fromDomain = "'.*@" + mailDomain + "'";
//		createRoute(fromDomain, webhookURL);
//	}
//
//	@Transactional
//	public boolean createRoute(String recipient, String webhookURL) {
//		logger.info("Webhook URL given is " + webhookURL);
//		String uniqueRoute = recipient + ":" + webhookURL;
//		RouteOperations routeOperations = client.roueOperations();
//
//		try {
//			if (!routeModelService.exists(uniqueRoute) && !routeExist(uniqueRoute, routeOperations)) {
//
//				String filter = "match_recipient('" + recipient + "')";
//				String endPoint = "store(notify=\"" + webhookURL + "\")";
//
//				// String stopEndPoint = "'stop()'";
//				MailgunRouteResponse routeResponse = routeOperations.createRoute(0, uniqueRoute, filter, endPoint);
//
//				if (routeResponse.getRoute() != null && routeResponse.getRoute().getId() != null) {
//					if (routeResponse.getRoute() != null && routeResponse.getRoute().getId() != null) {
//						RouteModel routeModel = new RouteModel();
//						routeModel.setId(recipient + ":" + webhookURL);
//						routeModel.setMailGunRouteId(routeResponse.getRoute().getId());
//						routeModel.setMailId(recipient);
//						routeModel.setWebHookURL(webhookURL);
//						routeModel = routeModelService.save(routeModel);
//					}
//					return true;
//				}
//			} else {
//				logger.info("Route already exists -> " + recipient + ":" + webhookURL);
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//
//		return false;
//	}
//
//	private boolean routeExist(String uniqueRoute, RouteOperations routeOperations) {
//		MailgunRouteListResponse response = routeOperations.getAllRoutes(100, 0);
//		List<MailgunRoute> mailrouteList = response.getRoutes();
//		if (mailrouteList != null) {
//			for (MailgunRoute mailgunRoute : mailrouteList) {
//				if (mailgunRoute.getDescription() != null && mailgunRoute.getDescription().equals(uniqueRoute))
//					return true;
//			}
//		}
//		return false;
//	}
//
//	public boolean createRoute(String recipient) {
//		return createRoute(recipient, webhookURL);
//	}

}
