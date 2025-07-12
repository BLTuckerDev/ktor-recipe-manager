# Recipe Manager

A modern recipe management application built with Ktor 3.2.0, showcasing the new modular architecture and dependency injection features.

## Quick Start

```bash
# Run the application
./gradlew run

# Visit http://localhost:8080
```

## Development

- **Framework**: Ktor 3.2.0
- **Database**: PostgreSQL + Exposed ORM
- **Frontend**: Ktor HTML DSL + Tailwind CSS

## API Endpoints

- `GET /` - Web interface
- `GET /health` - Health check
- `GET /api/v1/recipes` - Recipe API (coming soon)

## Environment Variables

```
DATABASE_URL=jdbc:postgresql://localhost:5432/recipemanager_dev
JWT_SECRET=your-secret-key
APP_ENV=dev
```