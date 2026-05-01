DROP TABLE IF EXISTS user_dietary_preferences;
DROP TABLE IF EXISTS recipe_diet_classifications;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS recipe_ingredients;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS diets;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password VARCHAR(32) NOT NULL
) ENGINE=InnoDB;

ALTER TABLE users ADD CONSTRAINT uq_user_email UNIQUE (email);
ALTER TABLE users ADD CONSTRAINT uq_user_username UNIQUE (username);

CREATE TABLE ingredients (
                             ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,
                             measurement_unit VARCHAR(20) NOT NULL
) ENGINE=InnoDB;

ALTER TABLE ingredients ADD CONSTRAINT uq_ingredient_name UNIQUE (name);
ALTER TABLE ingredients ADD CONSTRAINT ck_ingredient_measurement_unit CHECK (measurement_unit = 'grams' or measurement_unit = 'liters' or measurement_unit = 'pieces');

CREATE TABLE diets (
                       diet_id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       healthy_score DECIMAL(5, 2) NOT NULL
) ENGINE=InnoDB;

ALTER TABLE diets ADD CONSTRAINT uq_diet_name UNIQUE (name);
ALTER TABLE diets ADD CONSTRAINT ck_recipe_healthy_score CHECK (healthy_score >= 0 AND healthy_score <= 100);

CREATE TABLE recipes (
                         recipe_id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(150) NOT NULL,
                         total_prep_time_minutes INT,
                         calories_kcal DECIMAL(6, 2) NOT NULL,
                         fats_g DECIMAL(6, 2) NOT NULL,
                         saturated_fats_g DECIMAL(6, 2) DEFAULT 0 NOT NULL,
                         carbohydrates_g DECIMAL(6, 2) NOT NULL,
                         sugars_g DECIMAL(6, 2) DEFAULT 0 NOT NULL,
                         proteins_g DECIMAL(6, 2) NOT NULL,
                         salt_g DECIMAL(6, 2) DEFAULT 0 NOT NULL
) ENGINE=InnoDB;

ALTER TABLE recipes ADD CONSTRAINT uq_recipe_name UNIQUE (name);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_time CHECK (total_prep_time_minutes > 0);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_calories CHECK (calories_kcal >= 0);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_fats CHECK (fats_g >= 0);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_sat_fats CHECK (saturated_fats_g >= 0);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_carbs CHECK (carbohydrates_g >= 0);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_sugars CHECK (sugars_g >= 0);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_proteins CHECK (proteins_g >= 0);
ALTER TABLE recipes ADD CONSTRAINT ck_recipe_salt CHECK (salt_g >= 0);

CREATE TABLE recipe_ingredients (
                                    recipe_id INT NOT NULL,
                                    ingredient_id INT NOT NULL,
                                    quantity DECIMAL(8, 2) NOT NULL
) ENGINE=InnoDB;

ALTER TABLE recipe_ingredients ADD CONSTRAINT pk_recipe_ingredients PRIMARY KEY (recipe_id, ingredient_id);
ALTER TABLE recipe_ingredients ADD CONSTRAINT fk_req_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id) ON DELETE CASCADE;
ALTER TABLE recipe_ingredients ADD CONSTRAINT fk_req_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE RESTRICT;
ALTER TABLE recipe_ingredients ADD CONSTRAINT ck_req_quantity CHECK (quantity > 0);

CREATE TABLE inventory (
                           user_id INT NOT NULL,
                           ingredient_id INT NOT NULL,
                           quantity DECIMAL(8, 2) NOT NULL
) ENGINE=InnoDB;

ALTER TABLE inventory ADD CONSTRAINT pk_inventory PRIMARY KEY (user_id, ingredient_id);
ALTER TABLE inventory ADD CONSTRAINT fk_inv_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE inventory ADD CONSTRAINT fk_inv_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE;
ALTER TABLE inventory ADD CONSTRAINT ck_inv_quantity CHECK (quantity > 0);

CREATE TABLE recipe_diet_classifications (
                                             recipe_id INT NOT NULL,
                                             diet_id INT NOT NULL
) ENGINE=InnoDB;

ALTER TABLE recipe_diet_classifications ADD CONSTRAINT pk_recipe_diet PRIMARY KEY (recipe_id, diet_id);
ALTER TABLE recipe_diet_classifications ADD CONSTRAINT fk_class_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id) ON DELETE CASCADE;
ALTER TABLE recipe_diet_classifications ADD CONSTRAINT fk_class_diet FOREIGN KEY (diet_id) REFERENCES diets(diet_id) ON DELETE CASCADE;

CREATE TABLE user_dietary_preferences (
                                          user_id INT NOT NULL,
                                          diet_id INT NOT NULL
) ENGINE=InnoDB;

ALTER TABLE user_dietary_preferences ADD CONSTRAINT pk_user_diet PRIMARY KEY (user_id, diet_id);
ALTER TABLE user_dietary_preferences ADD CONSTRAINT fk_pref_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE user_dietary_preferences ADD CONSTRAINT fk_pref_diet FOREIGN KEY (diet_id) REFERENCES diets(diet_id) ON DELETE CASCADE;


INSERT INTO users (username, email, password) VALUES ('Andrei', 'andrei@email.com', 'passAndrei1');
INSERT INTO users (username, email, password) VALUES ('Elena', 'elena@email.com', 'passElena2');
INSERT INTO users (username, email, password) VALUES ('Cristian', 'cristian@email.com', 'passCristian3');
INSERT INTO users (username, email, password) VALUES ('Ioana', 'ioana@email.com', 'passIoana4');


INSERT INTO diets (name, healthy_score) VALUES ('Vegan', 95.00);
INSERT INTO diets (name, healthy_score) VALUES ('Gluten Free', 85.00);
INSERT INTO diets (name, healthy_score) VALUES ('Keto', 75.00);
INSERT INTO diets (name, healthy_score) VALUES ('Vegetarian', 90.00);
INSERT INTO diets (name, healthy_score) VALUES ('High Protein', 88.00);
INSERT INTO diets (name, healthy_score) VALUES ('Low Carb', 82.00);
INSERT INTO diets (name, healthy_score) VALUES ('Pescatarian', 92.00);
INSERT INTO diets (name, healthy_score) VALUES ('Low Fat', 80.00);
INSERT INTO diets (name, healthy_score) VALUES ('Carnivore', 60.00);
INSERT INTO diets (name, healthy_score) VALUES ('Raw Vegan', 98.00);


INSERT INTO ingredients (name, measurement_unit) VALUES ('Chicken Breast', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Brown Rice', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Broccoli', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Olive Oil', 'liters');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Salt', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Pepper', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Salmon', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Asparagus', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Lemon', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Avocado', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Whole Wheat Bread', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Eggs', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Tofu', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Spinach', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Quinoa', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Coconut Milk', 'liters');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Cherry Tomatoes', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Garlic', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Onion', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Beef', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Shrimp', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Mushrooms', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Sweet Potato', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Zucchini', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Bell Pepper', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Carrot', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Lentils', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Chickpeas', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Black Beans', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Greek Yogurt', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Feta Cheese', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Parmesan', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Cheddar', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Almonds', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Walnuts', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Chia Seeds', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Flax Seeds', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Honey', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Maple Syrup', 'liters');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Cinnamon', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Cumin', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Paprika', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Soy Sauce', 'liters');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Ginger', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Apple', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Banana', 'pieces');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Blueberries', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Strawberries', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Peanut Butter', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Oats', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Kale', 'grams');
INSERT INTO ingredients (name, measurement_unit) VALUES ('Turkey Breast', 'grams');


INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Chicken with Rice and Broccoli', 35, 450.00, 10.50, 2.00, 45.00, 3.00, 42.00, 1.20);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Baked Salmon with Asparagus', 25, 520.00, 35.00, 6.00, 8.00, 2.50, 45.00, 1.50);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Avocado Toast with Egg', 10, 380.00, 22.00, 4.50, 30.00, 2.00, 16.00, 0.80);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Tofu with Spinach', 20, 280.00, 12.00, 1.50, 20.00, 2.00, 18.00, 0.90);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Beef with Mushrooms', 40, 550.00, 28.00, 9.00, 15.00, 3.00, 48.00, 1.80);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Garlic Shrimp', 15, 250.00, 10.00, 1.50, 5.00, 1.00, 30.00, 1.10);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Quinoa Salad', 25, 320.00, 14.00, 2.00, 40.00, 4.00, 12.00, 0.50);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Spinach Soup', 30, 150.00, 5.00, 1.00, 20.00, 5.00, 6.00, 1.20);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Grilled Tofu', 15, 200.00, 11.00, 1.50, 5.00, 1.00, 16.00, 1.00);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Beef with Onion', 45, 580.00, 30.00, 10.00, 12.00, 4.00, 45.00, 2.00);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Coconut Milk Shrimp', 25, 420.00, 26.00, 15.00, 10.00, 3.00, 28.00, 1.40);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Sauteed Mushrooms', 10, 110.00, 8.00, 1.00, 6.00, 2.00, 4.00, 0.60);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Quinoa with Spinach', 20, 290.00, 9.00, 1.00, 42.00, 2.00, 10.00, 0.80);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Baked Sweet Potato', 30, 150.00, 4.00, 0.50, 25.00, 6.00, 2.00, 0.30);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Lentil Soup', 45, 320.00, 6.00, 1.00, 45.00, 4.00, 18.00, 1.10);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Greek Salad', 15, 280.00, 22.00, 6.00, 12.00, 5.00, 8.00, 1.50);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Chia Pudding', 10, 200.00, 12.00, 2.00, 18.00, 8.00, 6.00, 0.10);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Turkey Meatballs', 35, 400.00, 18.00, 4.00, 10.00, 2.00, 45.00, 1.30);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Oatmeal with Berries', 10, 350.00, 8.00, 1.50, 55.00, 12.00, 10.00, 0.20);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Chicken Fajitas', 25, 450.00, 15.00, 3.00, 20.00, 5.00, 48.00, 1.60);
INSERT INTO recipes (name, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES ('Homemade Hummus', 10, 220.00, 14.00, 2.00, 20.00, 1.00, 8.00, 0.80);


INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (1, 1, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (1, 2, 80);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (1, 3, 150);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (2, 7, 250);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (2, 8, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (3, 10, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (3, 11, 100);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (4, 13, 150);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (4, 14, 100);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (5, 20, 250);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (5, 22, 150);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (6, 21, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (6, 18, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (7, 15, 100);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (7, 17, 10);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (8, 14, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (8, 19, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (9, 13, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (9, 4, 15);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (10, 20, 300);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (10, 19, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (11, 21, 250);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (11, 16, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (12, 22, 300);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (12, 18, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (13, 15, 80);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (13, 14, 150);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (14, 23, 250);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (14, 4, 10);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (14, 5, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (14, 42, 3);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (15, 27, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (15, 26, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (15, 19, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (15, 18, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (15, 4, 15);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (15, 41, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (16, 17, 15);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (16, 25, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (16, 19, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (16, 31, 100);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (16, 4, 20);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (17, 36, 40);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (17, 16, 200);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (17, 39, 15);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (17, 47, 50);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (18, 52, 400);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (18, 18, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (18, 19, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (18, 12, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (18, 32, 30);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (19, 50, 60);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (19, 16, 150);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (19, 46, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (19, 48, 80);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (19, 38, 10);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (20, 1, 300);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (20, 25, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (20, 19, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (20, 4, 15);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (20, 42, 5);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (21, 28, 250);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (21, 18, 2);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (21, 4, 30);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (21, 9, 1);
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (21, 5, 2);

INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (1, 2);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (2, 3);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (3, 4);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (4, 1);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (6, 7);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (8, 8);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (9, 1);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (11, 6);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (12, 1);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (14, 1);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (14, 2);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (15, 1);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (15, 2);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (15, 5);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (16, 4);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (16, 2);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (16, 6);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (17, 1);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (17, 2);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (17, 10);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (18, 5);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (18, 6);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (19, 4);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (19, 8);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (20, 5);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (20, 6);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (21, 1);
INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES (21, 2);

INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (1, 5);
INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (2, 3);
INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (3, 4);
INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (1, 6);
INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (2, 7);
INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (2, 8);
INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (3, 9);
INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES (4, 10);

INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 1, 500);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 2, 1000);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 4, 250);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 13, 400);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 14, 200);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 23, 800);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 50, 1000);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (1, 52, 600);

INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 7, 300);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 8, 250);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 9, 3);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 15, 500);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 16, 400);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 36, 300);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 47, 150);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (2, 31, 200);

INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 10, 2);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 11, 400);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 12, 10);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 17, 15);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 18, 5);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 27, 500);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 28, 400);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (3, 42, 50);

INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 5, 1000);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 6, 100);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 19, 10);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 20, 800);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 21, 400);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 22, 500);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 25, 4);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 30, 450);
INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES (4, 46, 5);

COMMIT;