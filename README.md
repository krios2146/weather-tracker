# Weather Tracker

Created according to the technical specifications presented
in [this course](https://zhukovsd.github.io/java-backend-learning-course/Projects/WeatherViewer/)

## Overview

A web application for viewing current weather. The user can register and add one or multiple locations (cities, villages, or other places) to their collection. Afterward, the main page of the application will display a list of user places with their current weather.

## Table of Content

- [Application Features](#application-features)
- [Application architecture](#application-architecture)
  - [Technologies / tools used](#technologies--tools-used)
  - [Data Model](#data-model)
  - [Design patterns](#design-patterns)
  - [Sessions & Cookies](#sessions--cookies)
  - [Interface overview](#interface-overview)
- [Implementation details](#implementation-details)
  - [Sign in](#sign-in)
  - [Sign up](#sign-up)
  - [Sign out](#sign-out)
  - [Search](#search)
  - [Add](#add)
  - [View](#view)
  - [Delete](#delete)

## Application Features

For more details see - [implementation details](#implementation-details)

User related
> Classic authorization

- Sign in
- Sign up
- Sign out

Locations related
> Classic CRUD

- **Search** a location to track the weather
- **Add** a location to the tracked list
- **View** a list of locations with weather for each location
- **Delete** a location from the tracked list

## Application architecture

### Technologies / tools used

Logic layer

- Java Servlet

Data access layer

- Hibernate
- PostgreSQL

Interface

- Thymeleaf
- HTML + Bootstrap

Misc

- Jackson
- Maven

### Data Model

![ER Diagram](img/ER%20Diagram.svg)

#### Relationships

One user can have 0 or more different sessions, therefore relationship between `users` and `sessions` is one-to-many (optional)

One user can have 0 or more locations in their list, same with location and their user list. Relationship between `users` and `locations` is many-to-many. Junction table locations_users have been created in order to implement this relationship. Note that relationship between `users` and junction table is one-to-many and optional. This is because a user can use the app without adding any location to their list, whereas a location entry cannot be created in the database without a user request.

### Design patterns

Application uses [MVC](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) pattern.

The servlets act as **Controllers**. They handles user's requests and perform buisness logic.

The application contains **Model** classes which represents entities from database. [DAO](https://en.wikipedia.org/wiki/Data_access_object) objects are used to retrieve and persist data in the database.

The **View** layer of the application is managed by the Thymeleaf. HTML templates are used to dynamically generate the user interface.

### Sessions & Cookies

The application does not use Java servlet session objects, instead it creates a custom session objects. The reason for this is to learn how to work with the sessions manually. The Spring handles the sessions automaticaly out of the box which is great, but not for the learning process.

After creating the session object is stored in the database. Id of this session object is stored inside the cookie which is transfered to the client. This allows the client to access their session on subsequent requests, as the server can retrieve the session object from the cookie.

### Interface overview

The application have 4 available to user pages

- Home page
- Search page
- Sign in page
- Sign up page

They have the following relationships

![Pages relationships](img/Pages%20simple.svg)

From the home page user can access any other page.

From the search only available option is home page. The application will not allow user to search locations if thiey are not authorized.

The sign in and sign up pages have links to each other, but to the home page yet.

#### More detailed view with interface elements

![Detailed pages view](img/Pages.svg)

Actually, the home and the search pages almost identical, and the same can be said about the sign in and sign up pages, but each page serves it's own purpose.

## Implementation details

### Sign in

![Sign in feature diagram](img/Sign-in.svg)

### Sign up

![Sign up feature diagram](img/Sign-up.svg)

### Sign out

![Sign out feature diagram](img/Sign-out.svg)

### Search

![Search feature diagram](img/Search.svg)

### Add

![Add feature diagram](img/Add.svg)

### View

![View feature diagram](img/View.svg)

### Delete

![Delete feature diagram](img/Delete.svg)
