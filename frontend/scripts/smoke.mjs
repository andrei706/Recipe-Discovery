const baseUrl = process.env.API_BASE_URL || "http://localhost:8080";
const token = process.env.AUTH_TOKEN;

if (!token) {
  console.log("Missing AUTH_TOKEN. Set it before running smoke.");
  process.exit(0);
}

const response = await fetch(`${baseUrl}/api/recipes/available`, {
  headers: {
    Authorization: `Bearer ${token}`
  }
});

const text = await response.text();
console.log(`Status: ${response.status}`);
console.log(text.slice(0, 400));

