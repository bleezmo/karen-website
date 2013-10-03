var shapp = angular.module('sh',['ui.bootstrap', 'ngResource', 'ngRoute']);

shapp.factory('Hunts', ['$resource',function($resource){
    return $resource(
        "/scavengerhunt/hunts/:hunt",
        {},
        {
            newHunt: {method: "POST"},
            getAll: {method: "GET", isArray: true},
            get: {method: "GET"},
            remove: {method: "DELETE"}
        }
    );
}]);
shapp.factory("SMS", ['$resource', function($resource){
    return $resource(
        "/scavengerhunt/:method",
        {},
        {
            sendText: {method: "POST", params: {method: "sendtext"}},
            receiveTexts: {method: "GET", isArray: true, params: {method: "receivetexts"}}
        }
    )
}]);
shapp.factory("Items", ['$resource', function($resource){
    return $resource(
        "/scavengerhunt/items/:item",
        {},
        {
            getAll: {method: "GET", isArray: true},
            insert: {method: "POST"}
        }
    )
}]);