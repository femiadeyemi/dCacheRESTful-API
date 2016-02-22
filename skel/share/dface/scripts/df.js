(function(document) {
  'use strict';

  var app = document.querySelector('#app');

  // Sets app default base URL
  app.baseUrl = '/';
  if (window.location.port === '') {  // if production
    // Uncomment app.baseURL below and
    // set app.baseURL to '/your-pathname/' if running from folder in production
    // app.baseUrl = '/polymer-starter-kit/app/';
  }

  // Listen for template bound event to know when bindings
  // have resolved and content has been stamped to the page
  app.addEventListener('dom-change', function() {
    
  });

  // See https://github.com/Polymer/polymer/issues/1381
  window.addEventListener('WebComponentsReady', function() {
    // imports are loaded and elements have been registered
  });

  app.menuAction = function(){
    app.$.dfDrawerPanel.togglePanel();
  };
  
  // app.closeFileMetadataPanel = function(){
  //   app.$.metadata.closeDrawer();
  // } //I comment this out because I've factor this section out and it can now be located inside the file-metadata element

})(document);
