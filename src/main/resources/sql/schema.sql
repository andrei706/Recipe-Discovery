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
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE ingredients (
                             ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             measurement_unit VARCHAR(20) NOT NULL,
                             CONSTRAINT ck_ingredient_measurement_unit CHECK (measurement_unit IN ('grams', 'liters', 'pieces'))
) ENGINE=InnoDB;

CREATE TABLE diets (
                       diet_id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       healthy_score DECIMAL(5, 2) NOT NULL,
                       CONSTRAINT ck_recipe_healthy_score CHECK (healthy_score >= 0 AND healthy_score <= 100)
) ENGINE=InnoDB;

CREATE TABLE recipes (
                         recipe_id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(150) NOT NULL UNIQUE,
                         description TEXT,
                         total_prep_time_minutes INT,
                         calories_kcal DECIMAL(6, 2) NOT NULL,
                         fats_g DECIMAL(6, 2) NOT NULL,
                         saturated_fats_g DECIMAL(6, 2) DEFAULT 0 NOT NULL,
                         carbohydrates_g DECIMAL(6, 2) NOT NULL,
                         sugars_g DECIMAL(6, 2) DEFAULT 0 NOT NULL,
                         proteins_g DECIMAL(6, 2) NOT NULL,
                         salt_g DECIMAL(6, 2) DEFAULT 0 NOT NULL,
                         CONSTRAINT ck_recipe_time CHECK (total_prep_time_minutes > 0),
                         CONSTRAINT ck_recipe_calories CHECK (calories_kcal >= 0),
                         CONSTRAINT ck_recipe_fats CHECK (fats_g >= 0),
                         CONSTRAINT ck_recipe_sat_fats CHECK (saturated_fats_g >= 0),
                         CONSTRAINT ck_recipe_carbs CHECK (carbohydrates_g >= 0),
                         CONSTRAINT ck_recipe_sugars CHECK (sugars_g >= 0),
                         CONSTRAINT ck_recipe_proteins CHECK (proteins_g >= 0),
                         CONSTRAINT ck_recipe_salt CHECK (salt_g >= 0)
) ENGINE=InnoDB;

CREATE TABLE recipe_ingredients (
                                    recipe_id INT NOT NULL,
                                    ingredient_id INT NOT NULL,
                                    quantity DECIMAL(8, 2) NOT NULL,
                                    PRIMARY KEY (recipe_id, ingredient_id),
                                    CONSTRAINT fk_req_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id) ON DELETE CASCADE,
                                    CONSTRAINT fk_req_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE RESTRICT,
                                    CONSTRAINT ck_req_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;

CREATE TABLE inventory (
                           user_id INT NOT NULL,
                           ingredient_id INT NOT NULL,
                           quantity DECIMAL(8, 2) NOT NULL,
                           PRIMARY KEY (user_id, ingredient_id),
                           CONSTRAINT fk_inv_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                           CONSTRAINT fk_inv_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE,
                           CONSTRAINT ck_inv_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;

CREATE TABLE recipe_diet_classifications (
                                             recipe_id INT NOT NULL,
                                             diet_id INT NOT NULL,
                                             PRIMARY KEY (recipe_id, diet_id),
                                             CONSTRAINT fk_class_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id) ON DELETE CASCADE,
                                             CONSTRAINT fk_class_diet FOREIGN KEY (diet_id) REFERENCES diets(diet_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE user_dietary_preferences (
                                          user_id INT NOT NULL,
                                          diet_id INT NOT NULL,
                                          PRIMARY KEY (user_id, diet_id),
                                          CONSTRAINT fk_pref_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                          CONSTRAINT fk_pref_diet FOREIGN KEY (diet_id) REFERENCES diets(diet_id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO users (username, email, password) VALUES
                                                  ('Andrei', 'andrei@email.com', '$2a$10$r8hu2ouoWg7FA1ZhjhXnquYyueFogOE3c36YPFM.IpyKJx.Pf66QG'),
                                                  ('Elena', 'elena@email.com', '$2a$10$F5pKo53umejJubg0oaw8deqmXJNtXykitAky373FHX1GLXeqpEQpe'),
                                                  ('Cristian', 'cristian@email.com', '$2a$10$kfJzG42OzoS66IqBdEj5d.nVPe3v8OgKKYXjzDR6egtfYgzj1wKrm'),
                                                  ('Ioana', 'ioana@email.com', '$2a$10$myK1Gn0e0GT8fsWTbzKDYuKELmb5lAXYhri3mH1WwlLTpxX3/FPYK');

INSERT INTO diets (name, healthy_score) VALUES
                                            ('Vegan', 95.00),
                                            ('Gluten Free', 85.00),
                                            ('Keto', 75.00),
                                            ('Vegetarian', 90.00),
                                            ('High Protein', 88.00),
                                            ('Low Carb', 82.00),
                                            ('Pescatarian', 92.00),
                                            ('Low Fat', 80.00),
                                            ('Carnivore', 60.00),
                                            ('Raw Vegan', 98.00);

INSERT INTO ingredients (name, measurement_unit) VALUES
                                                     ('Chicken Breast', 'grams'), ('Brown Rice', 'grams'), ('Broccoli', 'grams'), ('Olive Oil', 'liters'),
                                                     ('Salt', 'grams'), ('Pepper', 'grams'), ('Salmon', 'grams'), ('Asparagus', 'grams'),
                                                     ('Lemon', 'pieces'), ('Avocado', 'pieces'), ('Whole Wheat Bread', 'grams'), ('Eggs', 'pieces'),
                                                     ('Tofu', 'grams'), ('Spinach', 'grams'), ('Quinoa', 'grams'), ('Coconut Milk', 'liters'),
                                                     ('Cherry Tomatoes', 'pieces'), ('Garlic', 'pieces'), ('Onion', 'pieces'), ('Beef', 'grams'),
                                                     ('Shrimp', 'grams'), ('Mushrooms', 'grams'), ('Sweet Potato', 'grams'), ('Zucchini', 'grams'),
                                                     ('Bell Pepper', 'pieces'), ('Carrot', 'pieces'), ('Lentils', 'grams'), ('Chickpeas', 'grams'),
                                                     ('Black Beans', 'grams'), ('Greek Yogurt', 'grams'), ('Feta Cheese', 'grams'), ('Parmesan', 'grams'),
                                                     ('Cheddar', 'grams'), ('Almonds', 'grams'), ('Walnuts', 'grams'), ('Chia Seeds', 'grams'),
                                                     ('Flax Seeds', 'grams'), ('Honey', 'grams'), ('Maple Syrup', 'liters'), ('Cinnamon', 'grams'),
                                                     ('Cumin', 'grams'), ('Paprika', 'grams'), ('Soy Sauce', 'liters'), ('Ginger', 'grams'),
                                                     ('Apple', 'pieces'), ('Banana', 'pieces'), ('Blueberries', 'grams'), ('Strawberries', 'grams'),
                                                     ('Peanut Butter', 'grams'), ('Oats', 'grams'), ('Kale', 'grams'), ('Turkey Breast', 'grams');

INSERT INTO recipes (name, description, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES
                                                                                                                                                             ('Chicken with Rice and Broccoli', 'Boil the rice according to the instructions. Cut the chicken breast into cubes and sauté in a pan with a little oil. Steam the broccoli for 5-7 minutes. Mix everything together and season.', 35, 450.00, 10.50, 2.00, 45.00, 3.00, 42.00, 1.20),
                                                                                                                                                             ('Baked Salmon with Asparagus', 'Preheat the oven to 200°C. Place the salmon and asparagus on a tray, drizzle with olive oil and lemon, then bake for 15-20 minutes.', 25, 520.00, 35.00, 6.00, 8.00, 2.50, 45.00, 1.50),
                                                                                                                                                             ('Avocado Toast with Egg', 'Toast the slice of bread. Mash the avocado and spread it on the bread. Cook the egg (fried or poached) and place it on top.', 10, 380.00, 22.00, 4.50, 30.00, 2.00, 16.00, 0.80),
                                                                                                                                                             ('Tofu with Spinach', 'Cut the tofu into cubes and fry until golden. Add fresh spinach and let it wilt for 2-3 minutes. Season with garlic.', 20, 280.00, 12.00, 1.50, 20.00, 2.00, 18.00, 0.90),
                                                                                                                                                             ('Beef with Mushrooms', 'Cut the beef into strips and fry over high heat. Add sliced mushrooms and cook until the water they release evaporates.', 40, 550.00, 28.00, 9.00, 15.00, 3.00, 48.00, 1.80),
                                                                                                                                                             ('Garlic Shrimp', 'In a pan with hot oil, add the crushed garlic and shrimp. Cook for 2-3 minutes on each side until they turn pink.', 15, 250.00, 10.00, 1.50, 5.00, 1.00, 30.00, 1.10),
                                                                                                                                                             ('Quinoa Salad', 'Boil the quinoa and let it cool. Mix with halved cherry tomatoes, greens, and a lemon and olive oil dressing.', 25, 320.00, 14.00, 2.00, 40.00, 4.00, 12.00, 0.50),
                                                                                                                                                             ('Spinach Soup', 'Sauté the onion, add the spinach and vegetable broth. Let it boil for 15 minutes, then blend until it becomes a smooth cream.', 30, 150.00, 5.00, 1.00, 20.00, 5.00, 6.00, 1.20),
                                                                                                                                                             ('Grilled Tofu', 'Press the tofu to remove excess water, marinate with soy sauce, and cook on the grill until grill marks appear.', 15, 200.00, 11.00, 1.50, 5.00, 1.00, 16.00, 1.00),
                                                                                                                                                             ('Beef with Onion', 'Sauté plenty of onions until translucent. Add the beef and simmer over low heat until the meat is tender.', 45, 580.00, 30.00, 10.00, 12.00, 4.00, 45.00, 2.00),
                                                                                                                                                             ('Coconut Milk Shrimp', 'Cook the shrimp in coconut milk with a little curry or turmeric until the sauce reduces and becomes creamy.', 25, 420.00, 26.00, 15.00, 10.00, 3.00, 28.00, 1.40),
                                                                                                                                                             ('Sauteed Mushrooms', 'Sauté the mushrooms over high heat with garlic and aromatic herbs until they become brown and crispy.', 10, 110.00, 8.00, 1.00, 6.00, 2.00, 4.00, 0.60),
                                                                                                                                                             ('Quinoa with Spinach', 'Boil the quinoa separately. Finally, fold the fresh spinach into the hot quinoa so it wilts slightly.', 20, 290.00, 9.00, 1.00, 42.00, 2.00, 10.00, 0.80),
                                                                                                                                                             ('Baked Sweet Potato', 'Prick the sweet potato with a fork and bake whole in the oven (45-60 min) or cut into cubes (25 min).', 30, 150.00, 4.00, 0.50, 25.00, 6.00, 2.00, 0.30),
                                                                                                                                                             ('Lentil Soup', 'Boil the lentils, carrots, and onion. Add spices (cumin, paprika) and let simmer until the lentils are soft.', 45, 320.00, 6.00, 1.00, 45.00, 4.00, 18.00, 1.10),
                                                                                                                                                             ('Greek Salad', 'Chop the tomatoes, cucumbers, and onion. Add cubed feta cheese, olives, and a generous pinch of oregano.', 15, 280.00, 22.00, 6.00, 12.00, 5.00, 8.00, 1.50),
                                                                                                                                                             ('Chia Pudding', 'Mix the chia seeds with coconut milk and sweetener. Leave in the refrigerator for at least 4 hours or overnight.', 10, 200.00, 12.00, 2.00, 18.00, 8.00, 6.00, 0.10),
                                                                                                                                                             ('Turkey Meatballs', 'Mix the ground turkey with egg, breadcrumbs, and spices. Form small balls and bake in the oven for 25-30 minutes.', 35, 400.00, 18.00, 4.00, 10.00, 2.00, 45.00, 1.30),
                                                                                                                                                             ('Oatmeal with Berries', 'Boil the oats in milk or water. At the end, add fresh berries and a little honey.', 10, 350.00, 8.00, 1.50, 55.00, 12.00, 10.00, 0.20),
                                                                                                                                                             ('Chicken Fajitas', 'Cut the chicken and peppers into strips. Sauté in a pan with Mexican spices until the vegetables are lightly caramelized.', 25, 450.00, 15.00, 3.00, 20.00, 5.00, 48.00, 1.60),
                                                                                                                                                             ('Homemade Hummus', 'Blend the cooked chickpeas with garlic, lemon, olive oil, and salt until a smooth paste is formed.', 10, 220.00, 14.00, 2.00, 20.00, 1.00, 8.00, 0.80);

INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES
                                                                        (1, 1, 200), (1, 2, 80), (1, 3, 150), (2, 7, 250), (2, 8, 200), (3, 10, 1), (3, 11, 100),
                                                                        (4, 13, 150), (4, 14, 100), (5, 20, 250), (5, 22, 150), (6, 21, 200), (6, 18, 2), (7, 15, 100),
                                                                        (7, 17, 10), (8, 14, 200), (8, 19, 1), (9, 13, 200), (9, 4, 15), (10, 20, 300), (10, 19, 2),
                                                                        (11, 21, 250), (11, 16, 200), (12, 22, 300), (12, 18, 1), (13, 15, 80), (13, 14, 150),
                                                                        (14, 23, 250), (14, 4, 10), (14, 5, 2), (14, 42, 3), (15, 27, 200), (15, 26, 2), (15, 19, 1),
                                                                        (15, 18, 2), (15, 4, 15), (15, 41, 2), (16, 17, 15), (16, 25, 1), (16, 19, 1), (16, 31, 100),
                                                                        (16, 4, 20), (17, 36, 40), (17, 16, 200), (17, 39, 15), (17, 47, 50), (18, 52, 400), (18, 18, 2),
                                                                        (18, 19, 1), (18, 12, 1), (18, 32, 30), (19, 50, 60), (19, 16, 150), (19, 46, 1), (19, 48, 80),
                                                                        (19, 38, 10), (20, 1, 300), (20, 25, 2), (20, 19, 1), (20, 4, 15), (20, 42, 5), (21, 28, 250),
                                                                        (21, 18, 2), (21, 4, 30), (21, 9, 1), (21, 5, 2);

INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES
                                                                 (1, 2), (2, 3), (3, 4), (4, 1), (6, 7), (8, 8), (9, 1), (11, 6), (12, 1), (14, 1), (14, 2),
                                                                 (15, 1), (15, 2), (15, 5), (16, 4), (16, 2), (16, 6), (17, 1), (17, 2), (17, 10), (18, 5),
                                                                 (18, 6), (19, 4), (19, 8), (20, 5), (20, 6), (21, 1), (21, 2);

INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES
                                                            (1, 5), (1, 6), (2, 3), (2, 7), (2, 8), (3, 4), (3, 9), (4, 10);

INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES
                                                             (1, 1, 500), (1, 2, 1000), (1, 4, 250), (1, 13, 400), (1, 14, 200), (1, 23, 800), (1, 50, 1000), (1, 52, 600),
                                                             (2, 7, 300), (2, 8, 250), (2, 9, 3), (2, 15, 500), (2, 16, 400), (2, 36, 300), (2, 47, 150), (2, 31, 200),
                                                             (3, 10, 2), (3, 11, 400), (3, 12, 10), (3, 17, 15), (3, 18, 5), (3, 27, 500), (3, 28, 400), (3, 42, 50),
                                                             (4, 5, 1000), (4, 6, 100), (4, 19, 10), (4, 20, 800), (4, 21, 400), (4, 22, 500), (4, 25, 4), (4, 30, 450), (4, 46, 5);