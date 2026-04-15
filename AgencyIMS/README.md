# AgencyIMS

AgencyIMS is a JavaFX + MySQL desktop project for managing:
- customers
- student permits
- driver's licenses
- car insurance

## Database Configuration

Database settings are loaded in this order:
1. Environment variables
2. `src/agencyims.properties`
3. Built-in defaults

Environment variable keys:
- `AGENCYIMS_DB_URL`
- `AGENCYIMS_DB_USER`
- `AGENCYIMS_DB_PASSWORD`

Default URL in source:
- `jdbc:mysql://localhost:3306/agencyims`

## Notes

- Keep credentials out of source control for production use.
- `DBConnection` now throws SQL exceptions directly, and DAOs handle failures with structured logging.
