package dataaccess;

public class MySqlDAO {

        public MySqlDAO() throws DataAccessException {
            configureDatabase();
        }

        private void configureDatabase() throws DataAccessException {
            DatabaseManager.createDatabase();

            String[] statements = {
"""
CREATE TABLE IF NOT EXISTS user (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL
)
""",
"""
CREATE TABLE IF NOT EXISTS auth (
    authToken VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) NOT NULL
)
""",
"""
CREATE TABLE IF NOT EXISTS game (
    gameID INT AUTO_INCREMENT PRIMARY KEY,
    whiteUsername VARCHAR(50),
    blackUsername VARCHAR(50),
    gameName VARCHAR(100) NOT NULL,
    game TEXT
)
"""
            };

            try (var conn = DatabaseManager.getConnection()) {
                for (String statement : statements) {
                    try (var ps = conn.prepareStatement(statement)) {
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException("Unable to configure database", e);
            }
        }
}
