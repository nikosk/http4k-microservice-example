This project is a proof of concept microservice written in Kotlin with http4k and Arrow.
It was written to test if Kotlin and Http4k is a good fit for writing microservices in a 
larger project. 

The microservice demonstrates:

- Function as a server API architecture with [Http4k](https://www.http4k.org/)
- Functional programming in Kotlin with [Arrow](https://arrow-kt.io/)
- Parameterizable configuration with [Typesafe Config](https://github.com/lightbend/config)
- Database migration with Maven and [Flyway](https://flywaydb.org/)
- Testable endpoints with Junit 5
- Json handling and validation with Http4k Lens and Arrow

A lot of ideas shamelessly stolen from [here](https://github.com/BranislavLazic/http4k-arrowkt-poc).

**Versions**

- Kotlin version: 1.5.31
- Http4k version: 4.14.1.1
- Arrow version: 1.0.0
