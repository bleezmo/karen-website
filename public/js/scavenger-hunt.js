var ModalInstanceCtrl = function ($scope, $modalInstance, item, groups) {
  $scope.item = item;
  $scope.groups = groups;
  $scope.ok = function () {
    $modalInstance.dismiss('cancel');
  };
};
shapp.controller("ModalInstanceCtrl",['$scope', '$modal','item', 'groups', ModalInstanceCtrl])
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
            },
            groups: function(){
                var groups = []
                if($scope.selectedHunt) groups = $scope.selectedHunt.groups
                return groups;
            }
          }
        });
    };
    $scope.newHunt = function(){
        Hunts.newHunt({},{active: true},function(hunt){
            $scope.setSelectedHunt(hunt)
        },function(error){
            console.error(error)
        })
    }
    $scope.setSelectedHunt = function(hunt){
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
    $scope.sendText = function(group){
        if(group.textAreaBinding){
           SMS.sendText({to: group.id},{message: group.textAreaBinding},function(response){
                console.log(response)
                group.textAreaBinding = "";
            },function(error){
                console.log(error)
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
    $scope.addToText = function(txt){
        $scope.textmsg = txt;
    }
    $scope.toDateDisplay = function(timestamp){
        return (new Date(timestamp)).toString()
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
        var activeHunt = _.find($scope.hunts, function(hunt){
            return hunt.active;
        })
        if(activeHunt) {
            for(var i = 0; i < activeHunt.groups.length; i++){
                activeHunt.groups[i].textAreaBinding = ""
            }
            $scope.selectedHunt = activeHunt;
            $scope.showSelectedHunt = true;
        }
    },function(error){
        console.log(error)
    })

    function checkTexts(timestamp){
        if($scope.selectedHunt && $scope.selectedHunt.active){
            cutoff = {cutoff: timestamp.toString()}
            SMS.receiveTexts(cutoff,function(data){
                console.log(data)
                var newTimestamp = timestamp
                if($scope.selectedHunt && $scope.selectedHunt.active){
                    _.each(data,function(group){
                        var internalGroup = _.find($scope.selectedHunt.groups,function(g){return g.id == group.id})
                        if(internalGroup){
                            _.each(group.messages,function(m){
                                if(newTimestamp < m.timestamp) newTimestamp = m.timestamp
                                internalGroup.messages.splice(0,0,m)
                            })
                        } else {
                            $scope.selectedHunt.groups.push(group)
                        }
                    })
                }
                $timeout(function(){checkTexts(newTimestamp)},2000)
            },function(error){
                console.log("error "+JSON.stringify(error))
                $timeout(function(){checkTexts(timestamp)},2000)
            })
        }  else {
            $timeout(function(){checkTexts(timestamp)},2000)
        }
    }
    checkTexts((new Date).getTime())
}]);