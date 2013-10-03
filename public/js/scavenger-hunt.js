var ModalInstanceCtrl = function ($scope, $modalInstance, item) {
  $scope.item = item
  $scope.ok = function () {
    $modalInstance.dismiss('cancel');
  };
};
shapp.controller("ModalInstanceCtrl",['$scope', '$modal','item', ModalInstanceCtrl])
shapp.controller("ScavengerHuntCtrl",['$timeout','$scope', '$modal', 'SMS', 'Hunts', 'Items',
function ($timeout, $scope, $modal, SMS, Hunts, Items){

    $scope.showNewModal = false;
    $scope.openItem = function (item) {
        var modalInstance = $modal.open({
          templateUrl: 'myModalContent.html',
          controller: ModalInstanceCtrl,
          resolve: {
            item: function(){
                return item;
            }
          }
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
    function deselectHunt(){
        $scope.selectedHunt = null
        $scope.showSelectedHunt = false
    }
    $scope.deleteHunt = function(){
        if($scope.selectedHunt){
            Hunts.remove({hunt: $scope.selectedHunt.id},function(success){
                Hunts.getAll({},function(hunts){
                    $scope.hunts = hunts
                    deselectHunt()
                },function(error){
                 console.log(error)
                })
            },function(error){
                console.log(error)
            })
        } else console.log("no hunt selected")
    }
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
    $scope.submitItem = function(clue,answer){
        Items.insert({},{clue: clue, answer: answer},function(item){
            $scope.clue = ""
            $scope.answer = ""
            $scope.items.push(item)
        },function(error){
            console.log(error);
        })
    }
    $scope.items = []
    $scope.hunts = []
    $scope.rawMessages = []
    Items.getAll({},function(items){
        $scope.items = items
    }, function(error){
        console.log(error)
    })
    Hunts.getAll({},function(hunts){
        $scope.hunts = hunts
    },function(error){
        console.log(error)
    })
    function checkTexts(timestamp){
        if($scope.selectedHunt){
            var cutoff = {}
            if(timestamp) cutoff = {cutoff: timestamp}
            var newTimestamp = (new Date).getTime()
            SMS.receiveTexts(cutoff,function(data){
                $scope.rawMessages.push(data)
                $timeout(function(){checkTexts(newTimestamp)},1000)
            },function(error){
                console.log(error)
                $timeout(function(){checkTexts()},1000)
            })
        }  else {
            $timeout(function(){checkTexts()},1000)
        }
    }
    checkTexts()
}]);