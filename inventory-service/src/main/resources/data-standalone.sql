CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_code VARCHAR(255) NOT NULL UNIQUE,
    quantity INT NOT NULL
);

INSERT INTO inventory (sku_code, quantity) VALUES ('iphone_13', 10);
INSERT INTO inventory (sku_code, quantity) VALUES ('iphone_13_red', 0);
