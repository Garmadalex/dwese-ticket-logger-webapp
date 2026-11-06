-- Crear tabla para las Comunidades Autónomas de España by Alejandro Madrigal
CREATE TABLE IF NOT EXISTS regions (
        id INT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(10) NOT NULL UNIQUE,
        name VARCHAR(100) NOT NULL
    );