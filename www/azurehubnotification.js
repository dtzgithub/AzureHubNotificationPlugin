var exec = require("cordova/exec");

/**
* Constructor.
*
* @returns {AzureHubNotification}
*/
var ACTION_AZURE_HUB_NOTIFICATION_REGISTER = "ACTION_AZURE_HUB_NOTIFICATION_REGISTER",
 ACTION_AZURE_HUB_NOTIFICATION_UN_REGISTER = "ACTION_AZURE_HUB_NOTIFICATION_UN_REGISTER";
var azureHubNotification = function () {

};

azureHubNotification.register = function (tags) {
    exec(success, error, "AzureHubNotification", ACTION_AZURE_HUB_NOTIFICATION_REGISTER, tags);
};

var success = function () {
    console.log("Successfully register to Azure Hub Notification");
};

var error = function (errorMsg) {
    console.log("Ooopsie !" + errorMsg);
};

azureHubNotification.unRegister = function () {
    exec(successUnregister, error, "AzureHubNotification", ACTION_AZURE_HUB_NOTIFICATION_UN_REGISTER, ['']);
};

var successUnregister = function () {
    console.log("Successfully unregister from Azure Hub Notification");
};

module.exports = azureHubNotification;
