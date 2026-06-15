DROP TABLE IF EXISTS user_dietary_preferences;
DROP TABLE IF EXISTS recipe_diet_classifications;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS plan_details;
DROP TABLE IF EXISTS plans;
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
                         features JSON NULL,
                         CONSTRAINT ck_recipe_time CHECK (total_prep_time_minutes > 0),
                         CONSTRAINT ck_recipe_calories CHECK (calories_kcal >= 0),
                         CONSTRAINT ck_recipe_fats CHECK (fats_g >= 0),
                         CONSTRAINT ck_recipe_sat_fats CHECK (saturated_fats_g >= 0),
                         CONSTRAINT ck_recipe_carbs CHECK (carbohydrates_g >= 0),
                         CONSTRAINT ck_recipe_sugars CHECK (sugars_g >= 0),
                         CONSTRAINT ck_recipe_proteins CHECK (proteins_g >= 0),
                         CONSTRAINT ck_recipe_salt CHECK (salt_g >= 0),
                         CONSTRAINT ck_features_json_valid CHECK (features IS NULL OR JSON_VALID(features))
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

CREATE TABLE plans (
    plan_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_plan_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT ck_plan_dates CHECK (start_date <= end_date)
) ENGINE=InnoDB;

CREATE TABLE plan_details (
    plan_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    plan_id INT NOT NULL,
    recipe_id INT NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    day_number INT NOT NULL,
    is_followed BOOLEAN NOT NULL DEFAULT FALSE,
    quantity INT NOT NULL,
    CONSTRAINT fk_plan_details_plan FOREIGN KEY (plan_id) REFERENCES plans(plan_id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_details_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id) ON DELETE RESTRICT,
    CONSTRAINT ck_plan_day CHECK (day_number BETWEEN 1 AND 31),
    CONSTRAINT ck_plan_meal_type CHECK (meal_type IN ('breakfast', 'lunch', 'dinner', 'snack')),
    CONSTRAINT ck_plan_quantity CHECK (quantity > 0 && quantity <= 15),
    CONSTRAINT uq_plan_day_meal UNIQUE (plan_id, day_number, meal_type)
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
                                                     ('Peanut Butter', 'grams'), ('Oats', 'grams'), ('Kale', 'grams'), ('Turkey Breast', 'grams'),
                                                     ('Pasta', 'grams'), ('Tomato Sauce', 'liters'), ('Basil', 'grams'), ('Mozzarella', 'grams'),
                                                     ('Tortilla Wraps', 'pieces'), ('Potatoes', 'grams'), ('Chicken Thighs', 'grams'), ('Milk', 'liters'),
                                                     ('Ground Beef', 'grams'), ('Burger Buns', 'pieces'), ('Mayonnaise', 'grams'), ('Ketchup', 'grams'),
                                                     ('Cucumber', 'pieces'), ('Tomato', 'pieces'), ('Lettuce', 'grams'), ('Pickles', 'grams'),
                                                     ('Pizza Dough', 'grams'), ('Pesto', 'grams'), ('Tomato Paste', 'grams'), ('Mozzarella Light', 'grams'),
                                                     ('Tuna', 'grams'), ('Olive Oil Extra Virgin', 'liters'), ('Red Cabbage', 'grams'), ('Wasabi', 'grams'),
                                                     ('Sushi Rice', 'grams'), ('Nori Seaweed', 'grams'), ('Sesame Seeds', 'grams'), ('Rice Vinegar', 'liters'),
                                                     ('Duck Breast', 'grams'), ('White Fish Fillet', 'grams'), ('Basmati Rice', 'grams'), ('Turmeric', 'grams'),
                                                     ('Coconut Oil', 'liters'), ('Lime', 'pieces'), ('Cilantro', 'grams'), ('Butter', 'grams'),
                                                     ('Heavy Cream', 'liters'), ('Beef Broth', 'liters'), ('Worcestershire Sauce', 'liters'), ('Garlic Powder', 'grams'),
                                                     ('White Vinegar', 'liters'), ('Brown Sugar', 'grams'), ('Soy Sauce Premium', 'liters'), ('Sesame Oil', 'liters');

INSERT INTO recipes (name, description, total_prep_time_minutes, calories_kcal, fats_g, saturated_fats_g, carbohydrates_g, sugars_g, proteins_g, salt_g) VALUES
                                                                                                                                                             ('Chicken with Rice and Broccoli', 'Boil the rice according to package instructions and set aside. Heat a large skillet over medium-high heat with 1 tbsp oil. Season chicken cubes with salt and pepper and sear 4-5 minutes per side until browned and cooked through — the chicken is done when the juices run clear and internal temperature reaches 75°C (165°F). Remove chicken and set aside. In the same skillet, add a splash of oil if needed and sauté minced garlic 30 seconds. Add broccoli florets and cook 3-4 minutes until bright green and slightly tender; add 2 tbsp water and cover 1-2 minutes for softer broccoli. Return chicken to the pan, add the cooked rice and toss to combine; warm through for 1-2 minutes. Adjust seasoning with salt, pepper and a squeeze of lemon before serving. Let rest 2 minutes before plating.\\n\\nPrep/Cook notes: cook rice separately; total stovetop time ~25 minutes. Use medium-high heat to sear the chicken; rest briefly so juices redistribute.', 35, 450.00, 10.50, 2.00, 45.00, 3.00, 42.00, 1.20),
                                                                                                                                                             ('Baked Salmon with Asparagus', 'Preheat oven to 200°C (400°F). Line a baking tray with parchment. Place salmon fillets skin-side down and arrange trimmed asparagus alongside. Drizzle with olive oil, season with salt and pepper and add lemon slices on top of the salmon. Bake in the preheated oven for 12-18 minutes depending on fillet thickness, until salmon flakes easily with a fork and asparagus is tender. For a crisp top, finish under the broiler for 1-2 minutes.\\n\\nPrep/Cook notes: oven bake 12-18 minutes at 200°C; internal salmon temperature should reach 63°C (145°F).', 25, 520.00, 35.00, 6.00, 8.00, 2.50, 45.00, 1.50),
                                                                                                                                                             ('Avocado Toast with Egg', 'Toast the bread to your preferred level. In a bowl, mash ripe avocado with salt, pepper and a squeeze of lemon. Spread the mashed avocado evenly on the toast. Cook an egg sunny-side-up or poach for 3-4 minutes for a runny yolk; place the egg on top of the avocado. Sprinkle with black pepper, a pinch of chili flakes or chopped herbs if desired and serve immediately.\\n\\nPrep/Cook notes: toast and cook egg quickly; total time ~10 minutes. For poached eggs, use simmering water and vinegar to help coagulation.', 10, 380.00, 22.00, 4.50, 30.00, 2.00, 16.00, 0.80),
                                                                                                                                                             ('Tofu with Spinach', 'Press tofu to remove excess water and cut into cubes. Heat 1 tbsp oil in a non-stick pan over medium-high heat and fry tofu until golden on all sides, 6-8 minutes — look for a crisp golden-brown crust on each face. Remove tofu and set aside. In the same pan, add a little oil and sauté minced garlic and sliced onion until fragrant and translucent, about 2 minutes. Add fresh spinach and cook, stirring, until fully wilted (2-3 minutes). Return tofu to the pan, add a splash of soy sauce and a pinch of black pepper, toss to combine and heat through for 1 minute. Serve hot.\\n\\nPrep/Cook notes: press tofu 15-30 minutes beforehand for best texture; cook time on stove ~15 minutes. Medium-high heat (about 190°C / 375°F in the pan) gives the best sear.', 20, 280.00, 12.00, 1.50, 20.00, 2.00, 18.00, 0.90),
                                                                                                                                                             ('Beef with Mushrooms', 'Slice beef thinly against the grain. Heat a skillet or wok on high heat with 1 tbsp oil until the oil just begins to shimmer. Sear beef in batches for 1-2 minutes per side until browned but still pink inside; remove and keep warm. In the same pan, add more oil if needed and sauté sliced mushrooms with a pinch of salt until they release their liquid and start to brown, about 4-5 minutes. Add minced garlic and cook 30 seconds. Return beef to the pan, add a splash of beef broth or soy sauce, stir to combine and reduce the sauce slightly, about 1-2 minutes. Finish with freshly ground black pepper and let rest 2-3 minutes before serving.\\n\\nPrep/Cook notes: high-heat searing keeps beef tender; total time ~20-25 minutes. For medium-rare beef, aim for an internal temperature of 57°C (135°F).', 40, 550.00, 28.00, 9.00, 15.00, 3.00, 48.00, 1.80),
                                                                                                                                                             ('Garlic Shrimp', 'Pat shrimp dry and season lightly with salt and pepper. Heat a skillet over medium-high heat with 1-2 tbsp oil or butter. Add minced garlic and sauté for 30 seconds until fragrant, then add shrimp in a single layer. Cook 1-2 minutes per side until shrimp turn pink and opaque. Add a squeeze of lemon and chopped parsley at the end and toss. Serve immediately.\\n\\nPrep/Cook notes: shrimp cook quickly; avoid overcooking. Total stovetop time ~6-8 minutes.', 15, 250.00, 10.00, 1.50, 5.00, 1.00, 30.00, 1.10),
                                                                                                                                                             ('Quinoa Salad', 'Rinse quinoa under cold water and cook according to package directions; fluff with a fork and cool slightly. In a bowl, combine halved cherry tomatoes, chopped fresh herbs and mixed greens. Whisk a dressing of lemon juice, olive oil, salt and pepper. Toss the cooled quinoa with the vegetables and dressing, adjust seasoning and serve at room temperature or chilled.\\n\\nPrep/Cook notes: allow quinoa to cool before mixing to avoid wilting greens; total time ~25 minutes.', 25, 320.00, 14.00, 2.00, 40.00, 4.00, 12.00, 0.50),
                                                                                                                                                             ('Spinach Soup', 'Heat 1 tbsp oil in a pot over medium heat and sauté chopped onion until translucent, about 3-4 minutes. Add garlic and cook 30 seconds until fragrant. Add fresh spinach and vegetable broth to cover and bring to a simmer. Cook for 10-15 minutes until spinach is completely tender and the broth has developed flavor. Use an immersion blender or transfer to a blender and puree until smooth — the soup should be velvety with no visible leaf pieces. Return to heat, adjust seasoning with salt and pepper and serve warm with a swirl of cream or olive oil if desired.\\n\\nPrep/Cook notes: blend until smooth for a creamy texture; total time ~30 minutes. Soup can be kept warm at 65°C (150°F) until serving.', 30, 150.00, 5.00, 1.00, 20.00, 5.00, 6.00, 1.20),
                                                                                                                                                             ('Grilled Tofu', 'Press tofu to remove moisture and slice into firm slabs. Marinate briefly with soy sauce, a little oil and optional ginger for 15-30 minutes. Heat grill or grill pan to medium-high and oil grates. Grill tofu 3-4 minutes per side until grill marks appear and tofu is heated through. Serve with a drizzle of sesame oil or a dipping sauce.\\n\\nPrep/Cook notes: press tofu for best result; grilling time ~8-10 minutes.', 15, 200.00, 11.00, 1.50, 5.00, 1.00, 16.00, 1.00),
                                                                                                                                                             ('Beef with Onion', 'Slice beef thinly against the grain and slice onions into rings. Heat a large pan with oil over medium-low heat and sauté onions slowly until soft, golden and caramelized, about 15-20 minutes — stir occasionally and reduce heat if they start to burn. Push onions to the side, increase heat to high and sear beef strips quickly, about 1-2 minutes per side, until browned and cooked to preferred doneness. Combine beef and onions, add a splash of broth to deglaze the pan and simmer 1-2 minutes until the sauce reduces slightly. Season to taste and let rest 3 minutes before serving.\\n\\nPrep/Cook notes: caramelize onions low and slow for sweetness; total time ~30-40 minutes. Medium-rare beef should reach 57°C (135°F); well-done 71°C (160°F).', 45, 580.00, 30.00, 10.00, 12.00, 4.00, 45.00, 2.00),
                                                                                                                                                             ('Coconut Milk Shrimp', 'In a skillet, heat 1 tbsp oil over medium heat and sauté chopped onion until soft. Add garlic and curry or turmeric and cook 30 seconds. Add shrimp and cook 1-2 minutes per side until starting to turn pink. Pour in coconut milk, bring to a gentle simmer and cook 3-4 minutes until sauce thickens slightly. Finish with lime juice and chopped cilantro. Serve over rice.\\n\\nPrep/Cook notes: moderate heat to avoid curdling coconut milk; total time ~20-25 minutes.', 25, 420.00, 26.00, 15.00, 10.00, 3.00, 28.00, 1.40),
                                                                                                                                                             ('Sauteed Mushrooms', 'Clean and slice mushrooms evenly. Heat a skillet on high with 1-2 tbsp oil or butter until the fat begins to shimmer. Add mushrooms in a single layer without overcrowding — work in batches if needed. Let them brown undisturbed for 3-4 minutes until deep golden on the bottom, then stir and continue cooking until golden and slightly crispy on all sides, about 3 more minutes. Add minced garlic and fresh herbs (thyme or parsley) in the last 30 seconds and toss. Season with salt and serve hot.\\n\\nPrep/Cook notes: high heat (around 220°C / 430°F pan temperature) promotes the Maillard reaction for best browning; total time ~10 minutes.', 10, 110.00, 8.00, 1.00, 6.00, 2.00, 4.00, 0.60),
                                                                                                                                                             ('Quinoa with Spinach', 'Cook quinoa according to package instructions. In a pan, sauté garlic briefly in olive oil, add fresh spinach and cook until wilted. Combine the spinach with the cooked quinoa, season with salt and pepper and warm through for a minute. Serve immediately.\\n\\nPrep/Cook notes: this is a quick side dish or base; total stovetop time ~15-20 minutes.', 20, 290.00, 9.00, 1.00, 42.00, 2.00, 10.00, 0.80),
                                                                                                                                                             ('Baked Sweet Potato', 'Preheat oven to 200°C (400°F). Scrub sweet potatoes and prick with a fork several times. Bake whole for 45-60 minutes until tender when pierced with a fork. For faster results, cut into cubes, toss with oil and roast at 200°C for 25-30 minutes until browned and soft. Serve hot with desired toppings.\\n\\nPrep/Cook notes: whole bake 45-60 minutes; roast cubes ~25 minutes.', 30, 150.00, 4.00, 0.50, 25.00, 6.00, 2.00, 0.30),
                                                                                                                                                             ('Lentil Soup', 'Rinse lentils and set aside. In a large pot, heat oil and sauté chopped onion, carrot and celery until softened. Add garlic, cumin and paprika and cook 30 seconds. Add lentils, diced tomatoes (optional) and vegetable broth to cover. Simmer for 25-35 minutes until lentils are tender. Season with salt and pepper and blend partially for a creamier texture if desired.\\n\\nPrep/Cook notes: total simmer time ~30-40 minutes; adjust liquid for desired thickness.', 45, 320.00, 6.00, 1.00, 45.00, 4.00, 18.00, 1.10),
                                                                                                                                                             ('Greek Salad', 'Chop tomatoes, cucumber and red onion into bite-sized pieces. Combine in a bowl with cubed feta, olives and a drizzle of olive oil and red wine vinegar. Season with salt, pepper and oregano and toss gently. Serve fresh.\\n\\nPrep/Cook notes: serve immediately for best texture; total time ~10-15 minutes.', 15, 280.00, 22.00, 6.00, 12.00, 5.00, 8.00, 1.50),
                                                                                                                                                             ('Chia Pudding', 'In a jar, combine chia seeds with coconut milk and a sweetener of choice. Stir well, cover and refrigerate for at least 4 hours or overnight until thickened. Stir again before serving and top with fruit or nuts.\\n\\nPrep/Cook notes: refrigerate 4+ hours; quick to prepare and store.', 10, 200.00, 12.00, 2.00, 18.00, 8.00, 6.00, 0.10),
                                                                                                                                                             ('Turkey Meatballs', 'Preheat oven to 200°C (400°F). In a bowl combine ground turkey, beaten egg, breadcrumbs, grated onion, minced garlic, salt and pepper. Form small meatballs and place on a lined baking tray. Bake for 20-25 minutes until cooked through and golden. Optionally finish with a quick pan sauce by deglazing the tray with broth and simmering with herbs.\\n\\nPrep/Cook notes: bake 20-25 minutes; ensure internal temperature reaches 74°C (165°F).', 35, 400.00, 18.00, 4.00, 10.00, 2.00, 45.00, 1.30),
                                                                                                                                                             ('Oatmeal with Berries', 'Bring milk or water to a simmer and add oats. Cook according to package instructions until creamy, stirring occasionally. Remove from heat and stir in fresh berries and a drizzle of honey. Serve warm.\\n\\nPrep/Cook notes: quick breakfast, total time ~10 minutes.', 10, 350.00, 8.00, 1.50, 55.00, 12.00, 10.00, 0.20),
                                                                                                                                                             ('Chicken Fajitas', 'Slice chicken and bell peppers into thin strips. Heat a large skillet over high heat with oil and sear chicken quickly until cooked through, about 5-7 minutes. Remove chicken, add peppers and onions to the pan and sauté until softened and slightly charred. Return chicken to the pan, toss with fajita seasoning or spices and heat through. Serve with warm tortillas and optional toppings.\\n\\nPrep/Cook notes: high heat searing gives best texture; total time ~20-30 minutes.', 25, 450.00, 15.00, 3.00, 20.00, 5.00, 48.00, 1.60),
                                                                                                                                                             ('Homemade Hummus', 'Drain and rinse cooked chickpeas. In a blender or food processor combine chickpeas, minced garlic, tahini, lemon juice and olive oil and blend until smooth, adding water or reserved bean liquid to reach desired consistency. Season with salt and serve with a drizzle of olive oil and paprika.\\n\\nPrep/Cook notes: blend to smooth texture; total time ~10 minutes.', 10, 220.00, 14.00, 2.00, 20.00, 1.00, 8.00, 0.80),
                                                                                                                                                             ('Tomato Basil Pasta', 'Bring a large pot of salted water to a boil and cook the pasta until al dente according to the package instructions. Meanwhile, heat olive oil in a pan over medium heat and sauté chopped onion until soft, then add minced garlic and cook for 30 seconds. Pour in the tomato sauce, reduce the heat and simmer for 8-10 minutes so the flavors blend. Stir in chopped basil and season with salt and pepper. Drain the pasta, toss it with the sauce and finish with grated Parmesan and a little pasta water if the sauce needs loosening. Serve hot.\\n\\nPrep/Cook notes: total stovetop time ~20 minutes; keep the sauce at a gentle simmer so it does not burn.', 20, 430.00, 12.00, 3.50, 62.00, 9.00, 15.00, 1.30),
                                                                                                                                                             ('Sheet Pan Chicken Thighs and Potatoes', 'Preheat the oven to 210°C (410°F). Cut the potatoes into even wedges or chunks and place them on a baking tray. Add chicken thighs, sliced onion and bell pepper to the tray. Drizzle everything with olive oil and season generously with salt, pepper, paprika and a little cumin. Toss the vegetables so they are coated, then arrange the chicken skin-side up. Roast for 35-45 minutes, flipping the vegetables halfway through, until the potatoes are golden and the chicken is cooked through. Let the tray rest for 5 minutes before serving.\\n\\nPrep/Cook notes: oven roast at 210°C; chicken should reach 74°C (165°F) internally and potatoes should be tender when pierced.', 45, 610.00, 30.00, 8.00, 38.00, 4.00, 44.00, 1.70),
                                                                                                                                                             ('Chicken Quesadillas', 'Season diced chicken with salt, pepper and paprika, then cook it in a skillet over medium-high heat until browned and fully cooked. In the same pan, soften sliced onion and bell pepper for 3-4 minutes. Lay a tortilla wrap in a clean skillet, sprinkle mozzarella on half, add the chicken and vegetables, then top with a little more cheese and fold the tortilla over. Cook for 2-3 minutes per side until the tortilla is crisp and the cheese has melted. Slice into wedges and serve warm.\\n\\nPrep/Cook notes: keep the heat medium so the tortilla crisps without burning; total time ~25 minutes.', 25, 520.00, 22.00, 7.00, 36.00, 5.00, 34.00, 1.60),
                                                                                                                                                             ('Overnight Oats with Banana', 'In a jar or bowl, combine oats, milk, chia seeds and cinnamon. Stir thoroughly, cover and refrigerate overnight or for at least 4 hours until the oats soften and the mixture thickens. In the morning, slice a banana on top and add blueberries or strawberries if desired. Stir before eating and adjust sweetness with a little honey if you want a sweeter breakfast.\\n\\nPrep/Cook notes: no cooking required; chilling time is essential for the right texture.', 10, 340.00, 9.00, 2.00, 52.00, 14.00, 11.00, 0.25),
                                                                                                                                                             ('Classic Cheeseburger', 'Form ground beef into patties about 6 cm wide and season generously with salt and pepper. Heat a skillet over high heat and cook the burgers 3-4 minutes per side for medium doneness; add a slice of cheese in the final minute and cover to melt. Toast burger buns lightly in the pan. Assemble with burger, cheese, lettuce, tomato slices, pickles, ketchup and mayonnaise. Serve immediately while warm.\\n\\nPrep/Cook notes: do not press down on burgers while cooking; total time ~15 minutes. Ensure internal temperature reaches 71°C (160°F) for ground beef.', 15, 520.00, 28.00, 12.00, 32.00, 6.00, 38.00, 2.10),
                                                                                                                                                             ('Homemade Pizza Margherita', 'Preheat oven to 230°C (450°F). Stretch or roll out pizza dough on a floured surface to desired thickness. Place on a pizza stone or baking sheet and brush lightly with olive oil. Spread a thin layer of tomato paste or tomato sauce evenly over the dough, leaving a border for the crust. Tear fresh mozzarella into pieces and distribute over the sauce. Drizzle with extra virgin olive oil and scatter fresh basil leaves. Bake for 10-14 minutes until the crust is golden and the cheese is bubbly.\\n\\nPrep/Cook notes: oven bake at 230°C; if using store-bought dough, follow package instructions for thickness and rising time.', 20, 380.00, 11.00, 4.50, 48.00, 5.00, 14.00, 1.60),
                                                                                                                                                             ('Sushi Rolls', 'Cook sushi rice according to package instructions and cool to room temperature. Spread rice thinly on a sheet of nori seaweed placed on a bamboo mat. Layer strips of cucumber, avocado and cooked tuna in the center of the rice. Using the mat, roll tightly from the bottom, sealing the edge with a bit of water. Slice with a sharp knife into 6-8 pieces, wiping the blade clean between cuts. Serve with wasabi, pickled ginger and soy sauce for dipping.\\n\\nPrep/Cook notes: sushi rice must be cooled; rolling requires practice but becomes easier. Total time ~30 minutes once rice is cooked.', 30, 280.00, 5.50, 1.00, 42.00, 4.00, 18.00, 2.80),
                                                                                                                                                             ('Grilled Steak with Garlic Butter', 'Remove steak from refrigeration 20 minutes before cooking. Pat dry with paper towels and season generously with salt and pepper. Heat a cast iron skillet or grill to very high heat (smoke should appear). Sear steak 3-4 minutes per side for medium-rare; baste with garlic butter in the final minute. Let rest for 5 minutes before slicing. Serve hot with the pan juices spooned over top.\\n\\nPrep/Cook notes: high heat is crucial for a good crust; use meat thermometer for accuracy (medium-rare is 52-57°C / 125-135°F). Resting is essential for tenderness.', 25, 650.00, 45.00, 18.00, 2.00, 0.20, 52.00, 1.50),
                                                                                                                                                             ('Pad Thai', 'Soak rice noodles in warm water for 15-20 minutes until softened, then drain. Heat a wok or large skillet over high heat with coconut oil. Stir-fry chopped garlic and chicken quickly until cooked through, about 5-7 minutes. Add the drained noodles, tamarind paste (or lime juice), fish sauce and a little soy sauce. Toss constantly for 3-4 minutes until the noodles are heated through. Add bean sprouts and chopped cilantro at the end and toss once more. Serve with lime wedges and crushed peanuts on the side.\\n\\nPrep/Cook notes: high heat and quick tossing prevent sticking; total stovetop time ~12-15 minutes.', 20, 420.00, 18.00, 3.50, 48.00, 8.00, 28.00, 2.00),
                                                                                                                                                             ('Pan-Seared Duck Breast', 'Score the skin of the duck breast in a cross-hatch pattern without cutting into the meat. Season with salt and pepper. Place skin-side down in a cold skillet and heat over medium, rendering the fat for 6-8 minutes until the skin is golden and crispy. Flip and cook meat-side for 3-4 minutes for medium-rare (internal temperature 58-63°C / 135-145°F). Let rest for 5 minutes before slicing.\\n\\nPrep/Cook notes: rendering fat slowly prevents burning; resting is essential for texture.', 20, 540.00, 42.00, 14.00, 3.00, 0.50, 45.00, 1.10),
                                                                                                                                                             ('Fried Rice with Vegetables', 'Heat oil in a wok or large skillet over high heat until the oil just begins to smoke (230°C / 450°F). Stir-fry diced onion, carrot and peas for 3-4 minutes until the vegetables are tender-crisp and slightly charred at the edges. Push vegetables to the side and add cooked cold rice, breaking up clumps. Stir-fry for 2-3 minutes until the rice starts to lightly toast, then drizzle in soy sauce and sesame oil. Toss in beaten egg or scrambled eggs and cook, stirring constantly, until the egg is set and evenly distributed. Season with garlic powder and white pepper. Serve hot immediately.\\n\\nPrep/Cook notes: using day-old cold rice prevents clumping; high heat gives "wok hei" flavor. Total time ~12-15 minutes.', 15, 310.00, 14.00, 2.50, 38.00, 4.00, 12.00, 1.40),
                                                                                                                                                             ('Caesar Salad', 'Wash and tear romaine lettuce into bite-sized pieces and place in a large bowl. In a small bowl, whisk together minced garlic, anchovies (if using), lemon juice, Worcestershire sauce, egg yolk and olive oil to create the dressing. Toss the lettuce with the dressing until well coated. Top with shaved or grated Parmesan cheese and croutons. Serve immediately.\\n\\nPrep/Cook notes: the dressing can be made ahead; for raw egg concerns, use pasteurized eggs or substitute with mayonnaise. Total time ~10 minutes.', 10, 200.00, 16.00, 3.50, 8.00, 1.50, 9.00, 1.20),
                                                                                                                                                             ('Beef Stew', 'Cut beef into 2-3 cm cubes and pat dry. Brown meat in batches in a hot pot with oil over high heat; set aside. In the same pot, sauté diced onion and garlic until softened. Add tomato paste and stir for 1 minute. Return meat to the pot and add beef broth, red wine (optional), potatoes, carrots and mushrooms. Bring to a simmer, cover and cook for 90-120 minutes until meat is tender (internal temperature reaching 95°C / 200°F ensures tenderness). Season with salt, pepper and Worcestershire sauce. Serve hot.\\n\\nPrep/Cook notes: slow cooking on low heat ensures tender meat; check periodically and add broth if the stew becomes too thick.', 120, 380.00, 16.00, 5.00, 25.00, 5.00, 35.00, 1.80),
                                                                                                                                                             ('Fish and Chips', 'Cut white fish fillet into portions and coat lightly with flour seasoned with salt and pepper. Heat oil in a deep skillet to 180°C (350°F). Fry fish 3-4 minutes per side until golden and crispy; remove and drain on paper towels. Cut potatoes into thin chips, fry in batches for 5-7 minutes until golden and crispy (internal fish temperature should be 63°C / 145°F). Serve fish and chips hot with malt vinegar, tartar sauce or lemon wedges.\\n\\nPrep/Cook notes: oil temperature is critical for crispy results; use a thermometer. Drain well to avoid excess oil.', 30, 450.00, 22.00, 4.50, 45.00, 2.00, 30.00, 1.60);

INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES
                                                                         (1, 1, 200), (1, 2, 80), (1, 3, 150), (2, 7, 250), (2, 8, 200), (3, 10, 1), (3, 11, 100),
                                                                         (4, 13, 150), (4, 14, 100), (5, 20, 250), (5, 22, 150), (6, 21, 200), (6, 18, 2), (7, 15, 100),
                                                                         (7, 17, 10), (8, 14, 200), (8, 19, 1), (9, 13, 200), (9, 4, 0.02), (10, 20, 300), (10, 19, 2),
                                                                         (11, 21, 250), (11, 16, 0.24), (12, 22, 300), (12, 18, 1), (13, 15, 80), (13, 14, 150),
                                                                         (14, 23, 250), (14, 4, 0.02), (14, 5, 2), (14, 42, 3), (15, 27, 200), (15, 26, 2), (15, 19, 1),
                                                                         (15, 18, 2), (15, 4, 0.02), (15, 41, 2), (16, 17, 15), (16, 25, 1), (16, 19, 1), (16, 31, 100),
                                                                         (16, 4, 0.03), (17, 36, 40), (17, 16, 0.24), (17, 39, 0.02), (17, 47, 50), (18, 52, 400), (18, 18, 2),
                                                                         (18, 19, 1), (18, 12, 1), (18, 32, 30), (19, 50, 60), (19, 16, 0.24), (19, 46, 1), (19, 48, 80),
                                                                         (19, 38, 10), (20, 1, 300), (20, 25, 2), (20, 19, 1), (20, 4, 0.02), (20, 42, 5), (21, 28, 250),
                                                                         (21, 18, 2), (21, 4, 0.03), (21, 9, 1), (21, 5, 2), (22, 53, 120), (22, 54, 0.35), (22, 18, 2),
                                                                         (22, 19, 1), (22, 55, 5), (22, 32, 20), (22, 4, 0.03), (23, 59, 450), (23, 58, 600), (23, 19, 1),
                                                                         (23, 25, 2), (23, 4, 0.03), (23, 5, 2), (23, 41, 2), (24, 59, 300), (24, 57, 2), (24, 56, 120),
                                                                         (24, 25, 1), (24, 19, 1), (24, 4, 0.02), (25, 50, 60), (25, 60, 0.24), (25, 36, 15), (25, 40, 1),
                                                                         (25, 46, 1), (25, 48, 50), (26, 61, 150), (26, 62, 2), (26, 63, 30), (26, 64, 20), (26, 65, 50),
                                                                         (26, 66, 2), (26, 67, 30), (26, 76, 20), (26, 5, 3), (26, 6, 1), (27, 69, 250), (27, 71, 100),
                                                                         (27, 72, 150), (27, 55, 10), (27, 74, 0.03), (28, 77, 200), (28, 65, 80), (28, 10, 2), (28, 73, 100),
                                                                         (28, 78, 4), (28, 79, 10), (28, 76, 5), (28, 95, 0.03), (29, 20, 400), (29, 88, 20), (29, 5, 2),
                                                                         (29, 6, 1), (30, 1, 300), (30, 85, 0.02), (30, 18, 2), (30, 87, 10), (30, 86, 1), (30, 95, 0.02),
                                                                         (31, 81, 250), (31, 5, 2), (31, 6, 1), (31, 88, 15), (31, 18, 3), (32, 2, 200), (32, 85, 0.02),
                                                                         (32, 19, 1), (32, 25, 1), (32, 26, 1), (32, 95, 0.02), (32, 96, 0.01), (33, 67, 200), (33, 18, 1),
                                                                         (33, 86, 1), (33, 91, 0.01), (33, 32, 20), (33, 74, 0.06), (34, 20, 400), (34, 19, 1), (34, 18, 2),
                                                                         (34, 59, 300), (34, 26, 1), (34, 22, 100), (34, 90, 0.75), (34, 91, 0.02), (34, 5, 2), (34, 6, 1),
                                                                         (35, 82, 300), (35, 59, 300), (35, 85, 0.50), (35, 86, 1), (35, 93, 0.03), (35, 5, 2), (35, 6, 1);

INSERT INTO recipe_diet_classifications (recipe_id, diet_id) VALUES
                                                                  (1, 5), (1, 2), (2, 7), (2, 5), (2, 2), (3, 4), (3, 2), (4, 1), (4, 4), (4, 2),
                                                                  (5, 5), (5, 2), (5, 6), (6, 7), (6, 5), (6, 2), (7, 1), (7, 4), (7, 2), (8, 1), (8, 4), (8, 2),
                                                                  (9, 1), (9, 4), (9, 2), (10, 5), (10, 2), (10, 6), (11, 7), (11, 5), (11, 2), (12, 1), (12, 4), (12, 2),
                                                                  (13, 1), (13, 4), (13, 2), (14, 1), (14, 4), (14, 2), (14, 8), (15, 1), (15, 4), (15, 2), (16, 4), (16, 2), (16, 8),
                                                                  (17, 1), (17, 4), (17, 2), (17, 8), (18, 5), (18, 2), (19, 1), (19, 4), (20, 5), (20, 2), (21, 1), (21, 4), (21, 2),
                                                                  (22, 4), (23, 5), (23, 2), (24, 5), (24, 2), (25, 1), (25, 4), (26, 5), (26, 2), (27, 4), (27, 2), (28, 7), (28, 2),
                                                                  (29, 5), (29, 2), (29, 6), (29, 3), (30, 5), (30, 2), (31, 5), (31, 2), (31, 3), (31, 6), (32, 5), (32, 2), (33, 4), (33, 2),
                                                                  (34, 5), (34, 2), (35, 7);

INSERT INTO user_dietary_preferences (user_id, diet_id) VALUES
                                                            (1, 5), (1, 6), (2, 3), (2, 7), (2, 8), (3, 4), (3, 9), (4, 10);

INSERT INTO inventory (user_id, ingredient_id, quantity) VALUES
                                                              (1, 1, 500), (1, 2, 1000), (1, 4, 0.75), (1, 13, 400), (1, 14, 200), (1, 23, 800), (1, 50, 1000), (1, 52, 600),
                                                              (2, 7, 300), (2, 8, 250), (2, 9, 3), (2, 15, 500), (2, 16, 0.80), (2, 36, 300), (2, 47, 150), (2, 31, 200),
                                                              (3, 10, 2), (3, 11, 400), (3, 12, 10), (3, 17, 15), (3, 18, 5), (3, 27, 500), (3, 28, 400), (3, 42, 50),
                                                              (4, 5, 1000), (4, 6, 100), (4, 19, 10), (4, 20, 800), (4, 21, 400), (4, 22, 500), (4, 25, 4), (4, 30, 450), (4, 46, 5),
                                                              (1, 53, 500), (1, 54, 0.50), (1, 55, 20), (1, 56, 300), (1, 58, 1500), (1, 59, 1000),
                                                              (2, 56, 250), (2, 57, 8), (2, 59, 700), (2, 25, 6), (2, 19, 4),
                                                              (3, 50, 700), (3, 60, 1.50), (3, 46, 6), (3, 48, 200),
                                                              (4, 53, 250), (4, 54, 0.50), (4, 58, 800), (4, 59, 900),
                                                              (1, 61, 800), (1, 62, 20), (1, 63, 200), (1, 64, 150), (1, 65, 4), (1, 66, 3), (1, 67, 200), (1, 76, 50),
                                                              (2, 69, 500), (2, 71, 300), (2, 72, 400), (2, 55, 30), (2, 74, 0.50),
                                                               (3, 77, 400), (3, 65, 3), (3, 73, 150), (3, 78, 8), (3, 79, 40), (3, 76, 20), (3, 95, 0.35),
                                                               (4, 88, 200), (4, 82, 500), (4, 85, 0.30),
                                                              (1, 81, 350), (1, 83, 500), (2, 82, 600), (2, 90, 0.50), (3, 67, 300), (3, 86, 2),
                                                              (4, 61, 1000), (4, 62, 30), (4, 75, 200), (1, 87, 100), (1, 86, 1);
