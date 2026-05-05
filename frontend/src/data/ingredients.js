export const INGREDIENTS = [
  { id: 1, name: "Chicken Breast", unit: "grams" },
  { id: 2, name: "Brown Rice", unit: "grams" },
  { id: 3, name: "Broccoli", unit: "grams" },
  { id: 4, name: "Olive Oil", unit: "liters" },
  { id: 5, name: "Salt", unit: "grams" },
  { id: 6, name: "Pepper", unit: "grams" },
  { id: 7, name: "Salmon", unit: "grams" },
  { id: 8, name: "Asparagus", unit: "grams" },
  { id: 9, name: "Lemon", unit: "pieces" },
  { id: 10, name: "Avocado", unit: "pieces" },
  { id: 11, name: "Whole Wheat Bread", unit: "grams" },
  { id: 12, name: "Eggs", unit: "pieces" },
  { id: 13, name: "Tofu", unit: "grams" },
  { id: 14, name: "Spinach", unit: "grams" },
  { id: 15, name: "Quinoa", unit: "grams" },
  { id: 16, name: "Coconut Milk", unit: "liters" },
  { id: 17, name: "Cherry Tomatoes", unit: "pieces" },
  { id: 18, name: "Garlic", unit: "pieces" },
  { id: 19, name: "Onion", unit: "pieces" },
  { id: 20, name: "Beef", unit: "grams" },
  { id: 21, name: "Shrimp", unit: "grams" },
  { id: 22, name: "Mushrooms", unit: "grams" },
  { id: 23, name: "Sweet Potato", unit: "grams" },
  { id: 24, name: "Zucchini", unit: "grams" },
  { id: 25, name: "Bell Pepper", unit: "pieces" },
  { id: 26, name: "Carrot", unit: "pieces" },
  { id: 27, name: "Lentils", unit: "grams" },
  { id: 28, name: "Chickpeas", unit: "grams" },
  { id: 29, name: "Black Beans", unit: "grams" },
  { id: 30, name: "Greek Yogurt", unit: "grams" },
  { id: 31, name: "Feta Cheese", unit: "grams" },
  { id: 32, name: "Parmesan", unit: "grams" },
  { id: 33, name: "Cheddar", unit: "grams" },
  { id: 34, name: "Almonds", unit: "grams" },
  { id: 35, name: "Walnuts", unit: "grams" },
  { id: 36, name: "Chia Seeds", unit: "grams" },
  { id: 37, name: "Flax Seeds", unit: "grams" },
  { id: 38, name: "Honey", unit: "grams" },
  { id: 39, name: "Maple Syrup", unit: "liters" },
  { id: 40, name: "Cinnamon", unit: "grams" },
  { id: 41, name: "Cumin", unit: "grams" },
  { id: 42, name: "Paprika", unit: "grams" },
  { id: 43, name: "Soy Sauce", unit: "liters" },
  { id: 44, name: "Ginger", unit: "grams" },
  { id: 45, name: "Apple", unit: "pieces" },
  { id: 46, name: "Banana", unit: "pieces" },
  { id: 47, name: "Blueberries", unit: "grams" },
  { id: 48, name: "Strawberries", unit: "grams" },
  { id: 49, name: "Peanut Butter", unit: "grams" },
  { id: 50, name: "Oats", unit: "grams" },
  { id: 51, name: "Kale", unit: "grams" },
  { id: 52, name: "Turkey Breast", unit: "grams" }
];

export function findIngredientByName(name) {
  if (!name) {
    return null;
  }
  const normalized = name.trim().toLowerCase();
  return INGREDIENTS.find((item) => item.name.toLowerCase() === normalized) || null;
}

