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

### Prerequisites

* Install `cert-manager`
* Create Cluster issuer

```
cat << EOF| kubectl create -n ingress -f -
apiVersion: cert-manager.io/v1alpha2
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: williescholtz@gmail.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
       ingress:
         class: nginx
EOF
```

* Install `nginx-ingress`
* Verify installation
      
      k get all --namespace ingress-nginx --watch

### Configure static IP for nginx controller

* Without `loadBalancerIp` specified (`type=LoadBalancer` will reserve address):
        
      k apply -f nginx-service.yaml
      k get svc ingress-nginx --namespace ingress-nginx --watch
       
* When assigned, promote to static: 

      kubectl patch svc ingress-nginx -p '{"spec": {"loadBalancerIP": "35.205.70.155"}}' --namespace ingress-nginx
      gcloud compute addresses create ingress-nginx-ip --addresses 35.205.70.155 --region europe-west1  
      gcloud compute addresses describe ingress-nginx-ip --region europe-west1 --format 'value(address)'

* Update `swagger.yaml` with IP so cloud endpoints dns know about it

      cd app/src/main/resources && gcloud endpoints services deploy swagger.yaml
      
### Install Tennis games service on cluster 

    k apply -f kubernetes.yaml
    
Wait for ingress

    k get ing tennis-games-ingress --watch
    k describe ing tennis-games-ingress
    
Verify 

    curl https://tennis-api.endpoints.tennis-games.cloud.goog
    
# Deployment

How to rollout a new version of backend and frontend

## Backend

* `mvn clean install`
* `mvn jib:build`
* `k rollout restart deployment/tennis-games-service`

## Firebase

* Build & Deploy

      yarn deploy

* Serve local

      cd ui && firebase serve --only hosting
      
* Prod deploy

      cd ui && firebase deploy
      
## References

* [Cloud endpoints](https://cloud.google.com/endpoints/docs/openapi/get-started-kubernetes-engine)
* [Cert manager](https://cert-manager.io/)
* [NGINX Ingress](https://kubernetes.github.io/ingress-nginx/deploy/)
