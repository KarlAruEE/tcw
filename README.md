## Tire Workshop

### Run with Docker Compse

Start data source docker images
```
docker run -d -p 9003:80 surmus/london-tire-workshop:2.0.0
docker run -d -p 9004:80 surmus/manchester-tire-workshop:2.0.0
```

Docke-Compose and just running container dont work at the moment :(
```
docker run -d -p 8080:8080 karlaru/tcw
```
#### Simple JavaScript Front-end at [localhost:8080](http://localhost:8080)

---

