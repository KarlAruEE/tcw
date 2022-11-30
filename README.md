## Tire Change Workshop

### Run with Docker Compse

Backend API's by [Surmus/tire-change-workshop](https://github.com/Surmus/tire-change-workshop)

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

### Adding new workshops

- Add a new class defining workshop and implementing [`WorkshopInterface`](src/main/java/com/karlaru/tcw/workshops/WorkshopInterface.java)
- Add api url and env parameters to [docker-compose](docker-compose.yaml) file

---

### API

GET workshops
```
curl -X GET http://localhost:8080/api/v1/workshop
```

Get available times
```
curl -X GET http://localhost:8080/api/v1/workshop/{workshop}/tire-change-times?from={from}&until={until}&vehicle={vehicle}
```

Book time
```
curl -X POST -H "Content-Type: application/json" -d '{"contactInformation": "{contactinformation}"}' http://localhost:8080/api/v1/{workshop}/tire-change-times/{id}/booking
```
