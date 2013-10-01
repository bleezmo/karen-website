var shapp = angular.module('sh',['ui.bootstrap', 'ngResource', 'ngRoute']);

shapp.factory('Hunts', ['$resource',function($resource){
    return $resource(
        "/scavengerhunt/hunts/:hunt",
        {},
        {
            newHunt: {method: "POST"},
            getAll: {method: "GET", isArray: true},
            get: {method: "GET"}
        }
    );
}]);
shapp.factory("SMS", ['$resource', function($resource){
    return $resource(
        "/scavengerhunt/:method",
        {},
        {
            sendText: {method: "POST", params: {method: "sendtext"}},
            receiveText: {method: "GET", params: {method: "receivetext"}}
        }
    )
}]);
function ModalInstanceCtrl($scope, $modalInstance, Hunts) {
  $scope.groups = [{id:0, name: "", participants: [{id: 0, name: "", number: "", group: ""}]}]
  $scope.ok = function () {
    console.log($scope.groups);
    Hunts.newHunt({},{groups: $scope.groups},function(hunt){
        $modalInstance.close(hunt);
    },function(error){
        console.error(error)
    })
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };

  $scope.addParticipant = function(group){
    group.participants.push({id: group.participants.length, name: "", number: "", group: ""})
  };

  $scope.removeParticipant = function(group,participant){
    group.participants.splice(_.indexOf(group.participants,participant),1)
  };

  $scope.addGroup = function(){
    $scope.groups.push({id:$scope.groups.length, name: "", participants: [{id: 0, name: "", number: "", group: ""}]})
  };

  $scope.removeGroup = function(group){
    $scope.groups.splice(_.indexOf($scope.groups,group),1);
  };
};
shapp.controller('ModalInstanceCtrl', ['$scope','$modalInstance','Hunts', ModalInstanceCtrl])

shapp.controller("ScavengerHuntCtrl",['$scope', '$modal', 'SMS', 'Hunts',
function ($scope, $modal, SMS, Hunts){

    $scope.showNewModal = false;
    $scope.open = function () {

        var modalInstance = $modal.open({
          templateUrl: 'myModalContent.html',
          controller: ModalInstanceCtrl
        });

        modalInstance.result.then(function (hunt) {
          console.log(hunt)
          $scope.hunt = hunt;
        }, function () {
          console.log('Modal dismissed at: ' + new Date());
        });
    };
    $scope.newHunt = function(){
        Hunts.newHunt({},{groups: []},function(hunt){
            $scope.setSelectedHunt(hunt)
        },function(error){
            console.error(error)
        })
    }
    $scope.setSelectedHunt = function(hunt){
        console.log(hunt)
        $scope.selectedHunt = hunt
        $scope.showSelectedHunt = true;
    }
    Hunts.getAll({},function(hunts){
        $scope.hunts = hunts
    },function(error){
        console.log(error)
    })
    $scope.sendText = function(group,textmsg){
        if(textmsg && group.participants){
           _.each(group.participants,function(p){
               SMS.sendText({to: p.number},{message: textmsg},function(response){
                    console.log(response)
                },function(error){
                    console.log(error)
                })
           })
        }
    }

}]);