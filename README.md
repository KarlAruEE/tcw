## Tire Change Workshop

### Run with Docker Compse

Backend API's by [Surmus/tire-change-workshop](https://github.com/Surmus/tire-change-workshop)

Start
```
docker-compose up -d
```

#### Simple JavaScript Front-end at [localhost:8080](http://localhost:8080)

---

### Adding new workshops

- Add a new class defining workshop and implementing [`WorkshopInterface`](src/main/java/com/karlaru/tcw/workshops/WorkshopInterface.java)
- Add api url and env parameters to [docker-compose](docker-compose.yaml) file

---

### API Docs

### [Swagger at localhost:8080](http://localhost:8080/swagger-ui.html)

---
### Stop
```
docker-compose down
```