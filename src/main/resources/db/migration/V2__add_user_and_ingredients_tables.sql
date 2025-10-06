-- V2__Add_user_and_ingredient_tables.sql

-- 1. Create the new 'users' table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       hashed_password VARCHAR(255) NOT NULL,
                       is_verified BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add the user_id foreign key to the existing 'recipes' table
-- NOTE: If your 'recipes' table already has data, this migration will fail
-- because existing rows would have a NULL user_id. You would need to handle
-- backfilling data in a more complex migration.
ALTER TABLE recipes ADD COLUMN user_id UUID;
ALTER TABLE recipes ADD CONSTRAINT fk_recipes_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


-- 3. Create the new 'ingredients' table
CREATE TABLE ingredients (
                             id UUID PRIMARY KEY,
                             user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             name VARCHAR(255) NOT NULL,
                             category VARCHAR(100),
                             default_unit VARCHAR(50),
                             description TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE(user_id, name)
);

-- 4. Create the new join table for recipes and ingredients
CREATE TABLE recipe_ingredients (
                                    id UUID PRIMARY KEY,
                                    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
                                    ingredient_id UUID NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
                                    quantity DECIMAL(10, 3),
                                    unit VARCHAR(50),
                                    notes TEXT,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    UNIQUE (recipe_id, ingredient_id)
);