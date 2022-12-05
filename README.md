# Tire Change Workshop

**Tech-stack**
- Java 19
- JavaScript
- Spring WebFlux

## Run with Docker Compse

Backend API's by [Surmus/tire-change-workshop](https://github.com/Surmus/tire-change-workshop)

Start
```
docker-compose up -d
```

#### Simple JavaScript Front-end at [localhost:8080](http://localhost:8080)



## Adding new workshops

- Add a new class defining workshop and implementing [`WorkshopInterface`](src/main/java/com/karlaru/tcw/workshops/WorkshopInterface.java)
- Add api url and env parameters to [docker-compose](docker-compose.yaml) file



## API Docs

**Swagger at [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Remote apis:

[London API](http://localhost:9003/swagger/index.html)

[Manchester API](http://localhost:9004/swagger/index.html)

## Stop
```
docker-compose down
```

---
### Tests

#### Controller

GET
- [X] Return list of workshops
- [X] Return correct available times filtered by workshop names
- [X] Return correct available times filtered by vehicle types
- [X] Return 400 when queried workshop doesn't change queried vehicle type
- [X] Return 404 when no workshops found
- [X] Return 404 when queried workshop is not found

PUT
- [X] Return correct booking confirmation response
- [X] Return 404 when invalid workshop

#### Workshop Manchester
GET
- [X] Should return available times
- [X] Return 400 when invalid date format
- [X] Return 400 when from date is after until date
- [X] Return 400 from remote API
- [X] Return 500 from remote API
- [X] Return 500 when API is unavailable and/or response body mapping fails

POST
- [X] Should book available time
- [X] Return 400 from remote API
- [X] Return 422 from remote API
- [X] Return 500 from remote API
- [X] Return 500 when API is unavailable and/or response body mapping fails

#### Workshop London
GET
- [X] Should return available times
- [X] Should return empty list if no times are found
- [X] Return 400 from remote API
- [X] Return 500 from remote API
- [X] Return 500 when API is unavailable and/or response body mapping fails

PUT
- [X] Should book available time
- [X] Return 400 from remote API
- [X] Return 422 from remote API
- [X] Return 500 from remote API
- [X] Return 500 when API is unavailable and/or response body mapping fails

#### Integration tests (London and Manchester containers must be running)
GET
- [X] Should return London times
- [X] Should return Manchester times
- [X] Should return times for two workshops with one vehicle type specified
- [X] Should return times for two workshops with both vehicle types specified
