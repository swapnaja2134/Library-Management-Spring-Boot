# Library Management System (Backend)

**Live demo:** https://library-frontend-h1jx.onrender.com
**Frontend code:** https://github.com/swapnaja2134/library-frontend

> The project runs on free hosting, so if nobody has used it for a while, the first page can take about a minute to load. After that it is fast.

## What this project is

This is a library app. Students (members) can search for books, borrow them and return them. The admin can add and remove books and look after the members.

I built the backend with Spring Boot and the frontend with React. I also added an AI search, where a student can type what they want in normal words, like "a book to start learning web development".

<!-- Add 2 or 3 screenshots here, for example: ![Books page](screenshots/books.png) -->

## What works right now

- Register and login with JWT tokens
- Three roles (ADMIN, LIBRARIAN, MEMBER) who can do different things
- The admin and librarian can add books. Only the admin can remove a book, and not while a member still has a copy of it
- Members can search books by title, borrow a book and return it
- A member cannot borrow the same book twice at the same time
- The due date is 14 days after borrowing. If a book is late, it shows as overdue and a fine of Rs. 2 per day is calculated and shown (the fine is only displayed, I did not build payment)
- AI book search using the Gemini API. If the AI is busy or fails, the app shows normal title search results instead
- Forgot password: the admin can set a temporary password for a member
- Input validation and clear error messages through a global exception handler
- 19 tests with JUnit and Mockito

## Tech used

- Java 17, Spring Boot 4.1
- Spring Security with JWT (jjwt)
- Spring Data JPA and Hibernate
- MySQL (H2 database for tests)
- Docker
- Hosting: Render (backend and frontend) and Aiven free MySQL (database)

## How the AI search works

1. The student types what they want and the frontend sends it to `/ai/search` with the login token.
2. The backend makes a prompt from the list of books (id, title, author, category) and the question.
3. It calls the Gemini API and asks for only the ids of the best matching books.
4. The backend keeps only the ids that really exist in my database, so the AI cannot make up a book.
5. If the AI call fails, it falls back to the normal title search.

## Main API endpoints

| What | Endpoint |
|---|---|
| Register | `POST /user/register` |
| Login | `POST /user/login` |
| All books | `GET /book/all` |
| Search by title | `GET /book/search?title=java` |
| Add a book | `POST /book/add` |
| Remove a book (admin) | `DELETE /book/delete/{id}` |
| Borrow a book | `POST /borrow/{bookId}` |
| Return a book | `PUT /borrow/return/{borrowId}` |
| My borrowed books | `GET /borrow/my` |
| AI search | `POST /ai/search` |
| List members (admin) | `GET /admin/members` |
| Reset a member's password (admin) | `PUT /admin/reset-password/{id}` |

## Run it on my computer

You need Java 17 or higher and MySQL.

Set these environment variables (do not write real values in any file):

| Name | What it is |
|---|---|
| `DB_PASSWORD` | my local MySQL password |
| `JWT_SECRET` | a long random text, at least 64 characters |
| `ADMIN_PASSWORD` | password for the admin account that is created on first start |
| `AI_API_KEY` | my Gemini API key (optional, without it the app uses normal search) |
| `AI_MODEL` | the Gemini model name (optional) |

Then run:

```
mvnw.cmd spring-boot:run
```

The app starts on port 8081 and creates the `library_db` database and tables by itself. To run the tests:

```
mvnw.cmd test
```

## How I deployed it

- The backend runs on Render as a Docker service.
- The database is an Aiven free MySQL service.
- The frontend is a static site on Render.
- All passwords and keys are set as environment variables on the hosting sites. They are not in the code or on GitHub.

## Known limitations

- No email. Forgot password works through the admin, and there are no due-date reminder emails yet
- Members cannot change their own password yet
- The fine is calculated and shown, but there is no payment
- On the free hosting the app sleeps when it is not used, and the free database can be switched off after a long quiet time
- Removing a book also removes its old borrow records

## What I learned

- Building a REST API in layers (controller, service, repository)
- Protecting endpoints with JWT and roles
- Keeping secrets out of GitHub by using environment variables (GitHub blocked one of my pushes because I had typed a key in a file, and I fixed it)
- Writing unit tests with Mockito
- Calling an outside API (Gemini) and handling the case when it fails
- Deploying a full project: Docker, a cloud database and a static site

## What I want to add next

- Admin dashboard with counts and all borrow records
- Change password page
- Email reminders for due dates
