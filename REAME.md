# Tennis Games Duo's

Lists all possible game combinations for the specified men and women.

Takes into account that each team should play every other team and no duplicate matches should occur

Only supports max 4 players per side.

Future versions may do this lazily based on amount of games requested and allow for larger max size

## Examples

`http://localhost:8080/games?games=10&courts=2&interval=WEEKS&men=1&men=2&men=3&men=4&women=A&women=B&women=C&women=D`


## Kubernetes 

Authenticate

    gcloud auth login
    gcloud auth configure-docker
    
    
Build & Update

    mvn jib:build
    kubectl rollout restart deployment/tennis-games-service
    
Config
    
    kubectl apply -f kubernetes.yaml
    
Cloud Endpoints

* Generate swagger.json from api.yaml
* `gcloud endpoints services deploy swagger.json` 
* Ensure container runtime configured in kubernetes

    
## Reference gcloud

Global IP

    gcloud compute addresses create tennis-games-service-ip --global
     gcloud compute addresses describe tennis-games-service-ip --global --format 'value(address)'

Deploy Services

    gcloud endpoints services deploy swagger.json
    
## Firebase

* Serve local

      cd ui && firebase serve --only hosting
      
* Prod deploy

      cd ui && firebase deploy


## References

* [Cloud endpoints](https://cloud.google.com/endpoints/docs/openapi/get-started-kubernetes-engine)
* [GKE lets encrypt](https://github.com/ahmetb/gke-letsencrypt)
