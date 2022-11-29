## Tire Change Workshop

### Run with Docker Compse

Start
```
docker-compose up -d
```

#### Simple JavaScript Front-end at [localhost:8080](http://localhost:8080)

Stop
```
docker-compose down
```

---

#### Adding new workshops

- Add a new class defining workshop and implementing [`WorkshopInterface`](src/main/java/com/karlaru/tcw/workshops/WorkshopInterface.java)
- Add api url and env parameters to [docker-compose](docker-compose.yaml) file

