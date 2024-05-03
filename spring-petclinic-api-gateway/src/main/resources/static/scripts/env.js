env = {
    RUM_REALM: '',
    RUM_AUTH: '',
    RUM_APP_NAME: '',
    RUM_ENVIRONMENT: ''
  }
  // non critical error so it shows in RUM when the realm is set
  if (env.RUM_REALM != "") {
    let showJSErrorObject = false;
    showJSErrorObject.property = 'true';
  }