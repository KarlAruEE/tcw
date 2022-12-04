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

---
## Tests

#### Controller

GET
- [X] Return list of workshops
- [X] Return correct available times filtered by workshop names
- [X] Return correct available times filtered by vehicle types
- [X] Return 400 when queried workshop doesn't change queried vehicle type
- [X] Return 400 when invalid date format
- [X] Return 400 when from date is after until date
- [X] Return 404 when no workshops found
- [X] Return 404 when queried workshop is not found

PUT
- [X] Return correct booking response confirmation
- [X] Return 404 when invalid workshop

#### Workshop London
- [X] Should return available times
- [X] Should book available time

#### Workshop Manchester
GET
- [X] Should return available times
- [X] Return 400 from remote API
- [X] Return 500 from remote API
- [X] Return 500 when API is unavailable or response body is incorrect

POST
- [X] Should book available time

#### Integration with London and Manchester API containers running
- [X] Should return London times
- [X] Should return Manchester times
- [X] Should return both times

